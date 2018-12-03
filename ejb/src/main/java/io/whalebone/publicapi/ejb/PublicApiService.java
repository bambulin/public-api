package io.whalebone.publicapi.ejb;

import com.google.gson.Gson;
import io.whalebone.publicapi.ejb.criteria.DnsTimelineCriteria;
import io.whalebone.publicapi.ejb.criteria.EventsCriteria;
import io.whalebone.publicapi.ejb.dto.DnsTimeBucketDTO;
import io.whalebone.publicapi.ejb.dto.EventDTO;
import io.whalebone.publicapi.ejb.elastic.DnsTimeBucketDTOProducer;
import io.whalebone.publicapi.ejb.elastic.Elastic;
import io.whalebone.publicapi.ejb.elastic.ElasticService;
import org.elasticsearch.common.lang3.StringUtils;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogram;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramBuilder;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Inject;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Stateless
public class PublicApiService {
    private static final String WILDCARD = "*";
    private static final int TERM_AGGREGATION_SIZE = 10;

    @EJB
    private ElasticService elasticService;
    @Inject
    @Elastic
    private Gson gson;

    public List<EventDTO> eventsSearch(EventsCriteria criteria) {
        List<QueryBuilder> queries = new ArrayList<>();
        prepareFieldParamQuery("request.ip", criteria.getClientIp(), queries, true);
        prepareFieldParamQuery("matched_iocs.classification.type", serializeEnumParam(criteria.getType()), queries, false);
        prepareFieldParamQuery("action_reason", serializeEnumParam(criteria.getReason()), queries, false);
        prepareFieldParamQuery("resolver_id", criteria.getResolverId(), queries, false);
        prepareFieldParamQuery("reason.fqdn", criteria.getDomain(), queries, true);

        BoolQueryBuilder bool = QueryBuilders.boolQuery();
        for (QueryBuilder query : queries) {
            bool.must(query);
        }

        List<FilterBuilder> filters = new ArrayList<>();
        filters.add(FilterBuilders.rangeFilter("logged")
                .lte("now")
                .gte(LocalDateTime.now().minusDays(criteria.getDays()))
        );
        filters.add(FilterBuilders.termFilter("client_id", criteria.getClientId()));

        FilterBuilder[] filterArray = new FilterBuilder[filters.size()];
        filterArray = filters.toArray(filterArray);
        QueryBuilder search = QueryBuilders.filteredQuery(bool, FilterBuilders.andFilter(filterArray));

        return elasticService.search(search, null, ElasticService.LOGS_INDEX, ElasticService.LOGS_TYPE , EventDTO.class);
    }

    public List<DnsTimeBucketDTO> dnsTimeline(DnsTimelineCriteria criteria) {
        List<QueryBuilder> queries = new ArrayList<>();
        prepareFieldParamQuery("client", criteria.getClientIp(), queries, true);
        prepareFieldParamQuery("query_type", serializeEnumParam(criteria.getType()), queries, false);
        prepareFieldParamQuery("domain_l2", criteria.getDomain(), queries, true);
        prepareFieldParamQuery("query", criteria.getQuery(), queries, true);
        prepareFieldParamQuery("resolver_id", criteria.getResolverId(), queries, false);
        prepareFieldParamQuery("answer", criteria.getAnswer(), queries, true);
        prepareFieldParamQuery("domain_l1", criteria.getTld(), queries, true);
        if (criteria.isDga()) {
            prepareFieldParamQuery("dga.class", 1, queries, false);
        }


        BoolQueryBuilder bool = QueryBuilders.boolQuery();
        for (QueryBuilder query : queries) {
            bool.must(query);
        }

        List<FilterBuilder> filters = new ArrayList<>();
        filters.add(FilterBuilders.rangeFilter("timestamp")
                .lte("now")
                .gte(LocalDateTime.now().minusDays(criteria.getDays()))
        );
        filters.add(FilterBuilders.termFilter("client_id", criteria.getClientId()));

        FilterBuilder[] filterArray = new FilterBuilder[filters.size()];
        filterArray = filters.toArray(filterArray);
        QueryBuilder search = QueryBuilders.filteredQuery(bool, FilterBuilders.andFilter(filterArray));

        DateHistogramBuilder aggregation = AggregationBuilders.dateHistogram(DnsTimeBucketDTOProducer.TIME_AGGREGATION)
                .field("timestamp")
                .interval(DateHistogram.Interval.HOUR);
        if (criteria.getAggregate() != null) {
            aggregation.subAggregation(AggregationBuilders.terms(DnsTimeBucketDTOProducer.TERM_AGGREGATION)
                    .field(criteria.getAggregate().getElasticField())
                    .size(TERM_AGGREGATION_SIZE));
        }

        DnsTimeBucketDTOProducer bucketDTOProducer = new DnsTimeBucketDTOProducer(criteria.getAggregate());
        return elasticService.searchWithAggregation(search, aggregation, ElasticService.PASSIVE_DNS_INDEX,
                ElasticService.PASSIVE_DNS_TYPE, bucketDTOProducer::produce);
    }

    private void prepareFieldParamQuery(String fieldName, Object value, List<QueryBuilder> queries,
                                               boolean canContainWildcard) {
        if (value != null) {
            if (value.getClass().isEnum()) {
                queries.add(QueryBuilders.termQuery(fieldName, gson.toJson(value)));
            } else if (value instanceof String && StringUtils.isNotBlank((String) value)
                    && canContainWildcard && ((String) value).contains(WILDCARD)) {
                queries.add(QueryBuilders.wildcardQuery(fieldName, (String) value));
            } else {
                queries.add(QueryBuilders.termQuery(fieldName, value));
            }
        }
    }

    private String serializeEnumParam(Object enumConstant) {
        if (enumConstant == null) {
            return null;
        }
        return gson.toJson(enumConstant).replaceAll("\"", "");
    }
}
