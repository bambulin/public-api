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
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Stateless
public class PublicApiService {
    private static final String WILDCARD = "*";
    private static final int TERM_AGGREGATION_SIZE = 10;
    public static final String TIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ssZ";

    @EJB
    private ElasticService elasticService;
    @Inject
    @Elastic
    private Gson gson;

    public List<EventDTO> eventsSearch(EventsCriteria criteria) {
        List<QueryBuilder> queries = new ArrayList<>();
        prepareFieldParamQuery("request.ip", criteria.getClientIp(), queries, true);
        prepareFieldParamQuery("matched_iocs.classification.type", serializeEnumParam(criteria.getThreatType()), queries, false);
        prepareFieldParamQuery("action_reason", serializeEnumParam(criteria.getReason()), queries, false);
        prepareFieldParamQuery("resolver_id", criteria.getResolverId(), queries, false);
        prepareFieldParamQuery("reason.fqdn", criteria.getDomain(), queries, true);

        BoolQueryBuilder bool = QueryBuilders.boolQuery();
        for (QueryBuilder query : queries) {
            bool.must(query);
        }

        List<FilterBuilder> filters = new ArrayList<>();
        filters.add(FilterBuilders.rangeFilter("logged")
                .lte(nowFormatted())
                .gte(nowMinusDaysFormatted(criteria.getDays()))
        );
        filters.add(FilterBuilders.termFilter("client_id", criteria.getClientId()));

        FilterBuilder[] filterArray = new FilterBuilder[filters.size()];
        filterArray = filters.toArray(filterArray);
        QueryBuilder search = QueryBuilders.filteredQuery(bool, FilterBuilders.andFilter(filterArray));

        return elasticService.search(search, null, ElasticService.LOGS_INDEX_ALIAS, ElasticService.LOGS_TYPE , EventDTO.class);
    }

    public List<DnsTimeBucketDTO> dnsAggregations(DnsTimelineCriteria criteria, String index, String type) {
        List<QueryBuilder> queries = new ArrayList<>();
        prepareFieldParamQuery("client", criteria.getClientIp(), queries, true);
        prepareFieldParamQuery("query_type", serializeEnumParam(criteria.getQueryType()), queries, false);
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

        String timestampField;
        if (ElasticService.DNSSEC_INDEX_ALIAS.equals(index)) {
            timestampField = "@timestamp";
        } else {
            timestampField = "timestamp";
        }

        List<FilterBuilder> filters = new ArrayList<>();
        filters.add(FilterBuilders.rangeFilter(timestampField)
                .lte(nowFormatted())
                .gte(nowMinusDaysFormatted(criteria.getDays()))
        );
        filters.add(FilterBuilders.termFilter("client_id", criteria.getClientId()));

        FilterBuilder[] filterArray = new FilterBuilder[filters.size()];
        filterArray = filters.toArray(filterArray);
        QueryBuilder search = QueryBuilders.filteredQuery(bool, FilterBuilders.andFilter(filterArray));

        DateHistogramBuilder aggregation = AggregationBuilders.dateHistogram(DnsTimeBucketDTOProducer.TIME_AGGREGATION)
                .field(timestampField)
                .interval(DateHistogram.Interval.HOUR);
        if (criteria.getAggregate() != null) {
            aggregation.subAggregation(AggregationBuilders.terms(DnsTimeBucketDTOProducer.TERM_AGGREGATION)
                    .field(criteria.getAggregate().getElasticField())
                    .size(TERM_AGGREGATION_SIZE));
        }

        DnsTimeBucketDTOProducer bucketDTOProducer = new DnsTimeBucketDTOProducer(criteria.getAggregate());
        return elasticService.searchWithAggregation(search, aggregation, index, type, bucketDTOProducer::produce);
    }

    public List<DnsTimeBucketDTO> dnsTimeline(DnsTimelineCriteria criteria) {
        return dnsAggregations(criteria, ElasticService.PASSIVE_DNS_INDEX_ALIAS, ElasticService.PASSIVE_DNS_TYPE);
    }

    public List<DnsTimeBucketDTO> dnsSecTimeline(DnsTimelineCriteria criteria) {
        return dnsAggregations(criteria, ElasticService.DNSSEC_INDEX_ALIAS, ElasticService.DNSSEC_TYPE);
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

    private static ZonedDateTime now() {
        return ZonedDateTime.now().withSecond(0).withNano(0);
    }

    private static String nowFormatted() {
        return now().format(DateTimeFormatter.ofPattern(TIME_PATTERN));
    }

    private static String nowMinusDaysFormatted(int days) {
        return now().minusDays(days).format(DateTimeFormatter.ofPattern(TIME_PATTERN));
    }
}
