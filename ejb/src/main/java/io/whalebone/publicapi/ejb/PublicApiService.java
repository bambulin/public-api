package io.whalebone.publicapi.ejb;

import com.google.gson.Gson;
import io.whalebone.publicapi.ejb.criteria.DnsTimelineCriteria;
import io.whalebone.publicapi.ejb.criteria.EventsCriteria;
import io.whalebone.publicapi.ejb.dto.ActiveIoCStatsDTO;
import io.whalebone.publicapi.ejb.dto.DnsTimeBucketDTO;
import io.whalebone.publicapi.ejb.dto.EventDTO;
import io.whalebone.publicapi.ejb.elastic.*;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramAggregationBuilder;

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
    public static final String LOGS_INDEX_PREFIX = "logs-";
    public static final String LOGS_INDEX_TIME_FORMAT = "yyyy-MM-'*'";
    public static final String LOGS_TYPE = "match";
    public static final String PASSIVE_DNS_INDEX_PREFIX = "passivedns-";
    public static final String PASSIVE_DNS_INDEX_TIME_FORMAT = "yyyy.MM.'*'";
    public static final String PASSIVE_DNS_TYPE = "logs";
    public static final String DNSSEC_INDEX_PREFIX = "dnssec-";
    public static final String DNSSEC_INDEX_TIME_FORMAT = "yyyy.MM.'*'";
    public static final String DNSSEC_TYPE = "log";

    @EJB
    private ElasticService elasticService;
    @EJB
    private IoCElasticService iocElasticService;

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

        BoolQueryBuilder search = QueryBuilders.boolQuery();
        for (QueryBuilder query : queries) {
            search.must(query);
        }

        ZonedDateTime timestampTo = now();
        ZonedDateTime timestampFrom = timestampTo.minusDays(criteria.getDays());
        search.filter(QueryBuilders.boolQuery()
                .must(QueryBuilders.rangeQuery("logged")
                        .gte(formatTimestamp(timestampFrom))
                        .lte(formatTimestamp(timestampTo))
                )
                .must(QueryBuilders.termQuery("client_id", criteria.getClientId()))
        );

        String[] indices = ElasticUtils.indicesByMonths(LOGS_INDEX_PREFIX, LOGS_INDEX_TIME_FORMAT, timestampFrom, timestampTo);
        return elasticService.search(search, null, indices, LOGS_TYPE , EventDTO.class);
    }

    private List<DnsTimeBucketDTO> dnsAggregations(DnsTimelineCriteria criteria,
                                                   String[] indices,
                                                   String type,
                                                   ZonedDateTime timestampFrom,
                                                   ZonedDateTime timestampTo,
                                                   String timestampField) {
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


        BoolQueryBuilder search = QueryBuilders.boolQuery();
        for (QueryBuilder query : queries) {
            search.must(query);
        }

        search.filter(QueryBuilders.boolQuery()
                .must(QueryBuilders.rangeQuery(timestampField)
                        .gte(formatTimestamp(timestampFrom))
                        .lte(formatTimestamp(timestampTo))
                )
                .must(QueryBuilders.termQuery("client_id", criteria.getClientId()))
        );

        DateHistogramAggregationBuilder aggregation = AggregationBuilders.dateHistogram(DnsTimeBucketDTOProducer.TIME_AGGREGATION)
                .field(timestampField)
                // don't return empty buckets
                .minDocCount(1)
                .dateHistogramInterval(BucketIntervalMapper.getMappedInterval(criteria.getInterval()));
        if (criteria.getAggregate() != null) {
            aggregation.subAggregation(AggregationBuilders.terms(DnsTimeBucketDTOProducer.TERM_AGGREGATION)
                    .field(criteria.getAggregate().getElasticField())
                    // don't return empty buckets
                    .minDocCount(1)
                    .size(TERM_AGGREGATION_SIZE));
        }

        DnsTimeBucketDTOProducer bucketDTOProducer = new DnsTimeBucketDTOProducer(criteria.getAggregate());
        return elasticService.searchWithAggregation(search, aggregation, indices, type, bucketDTOProducer::produce);
    }

    public List<DnsTimeBucketDTO> dnsTimeline(DnsTimelineCriteria criteria) {
        ZonedDateTime timestampTo = now();
        ZonedDateTime timestampFrom = timestampTo.minusDays(criteria.getDays());
        String[] indices = ElasticUtils.indicesByMonths(PASSIVE_DNS_INDEX_PREFIX, PASSIVE_DNS_INDEX_TIME_FORMAT, timestampFrom, timestampTo);
        return dnsAggregations(criteria, indices, PASSIVE_DNS_TYPE, timestampFrom, timestampTo, "timestamp");
    }

    public List<DnsTimeBucketDTO> dnsSecTimeline(DnsTimelineCriteria criteria) {
        ZonedDateTime timestampTo = now();
        ZonedDateTime timestampFrom = timestampTo.minusDays(criteria.getDays());
        String[] indices = ElasticUtils.indicesByMonths(DNSSEC_INDEX_PREFIX, DNSSEC_INDEX_TIME_FORMAT, timestampFrom, timestampTo);
        return dnsAggregations(criteria, indices, DNSSEC_TYPE, timestampFrom, timestampTo, "@timestamp");
    }

    public ActiveIoCStatsDTO getActiveIoCStats() {
        return iocElasticService.getActiveIoCStats();
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

    public static ZonedDateTime now() {
        return ZonedDateTime.now().withSecond(0).withNano(0);
    }

    private static String formatTimestamp(ZonedDateTime timestamp) {
        return timestamp.format(DateTimeFormatter.ofPattern(TIME_PATTERN));
    }
}
