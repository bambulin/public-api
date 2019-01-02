package io.whalebone.publicapi.ejb.elastic;

import com.google.gson.Gson;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AbstractAggregationBuilder;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogram;
import org.elasticsearch.search.sort.SortBuilder;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

@Stateless
public class ElasticService implements Serializable {
    private static final long serialVersionUID = 8884963960567953481L;
    public static final String LOGS_INDEX_ALIAS = "logs";
    public static final String LOGS_TYPE = "match";
    public static final String PASSIVE_DNS_INDEX_ALIAS = "passivedns";
    public static final String PASSIVE_DNS_TYPE = "logs";

    @Inject
    private Client elasticClient;
    @Inject
    @Elastic
    private Gson gson;

    public <T> List<T> search(final QueryBuilder query,
                              final SortBuilder sort,
                              final String index,
                              final String type,
                              final Type beanType) throws ElasticSearchException {
        try {
            final SearchRequestBuilder search = elasticClient.prepareSearch(index)
                    .setQueryCache(false)
                    .setExplain(true)
                    .setTypes(type)
                    .setQuery(query)
                    .setSearchType(SearchType.DFS_QUERY_THEN_FETCH);
            if (sort != null) {
                search.addSort(sort);
            }

            final SearchResponse response = search.execute()
                    .actionGet();
            if (response.isTimedOut()) {
                throw new ElasticSearchException("Elastic search timed out");
            }
//            if (response.isTerminatedEarly()) {
//                throw new ArchiveException("Elastic search has been terminated early");
//            }
            if (!RestStatus.OK.equals(response.status())) {
                throw new ElasticSearchException("Elastic search has not ended successfully: " + response.status());
            }
            if (response.getHits() == null || response.getHits().totalHits() == 0) {
                return new ArrayList<>();
            } else {
                List<T> docs = new ArrayList<>((int) response.getHits().getTotalHits());
                for (SearchHit hit : response.getHits()) {
                    T doc = gson.fromJson(hit.sourceAsString(), beanType);
                    DocIdSetter.setDocIdIfApplicable(doc, hit.getId());
                    docs.add(doc);
                }
                return docs;
            }
        } catch (Exception ex) {
            throw new ElasticSearchException("Elastic search has failed", ex);
        }
    }

    public <T> List<T> searchWithAggregation(final QueryBuilder query,
                                             final AbstractAggregationBuilder aggregation,
                                             final String index,
                                             final String type,
                                             final Function<Aggregations, List<T>> aggregationBeanProducer
    ) throws ElasticSearchException {
        try {
            final SearchRequestBuilder search = elasticClient.prepareSearch(index)
                    .setQueryCache(false)
                    .setExplain(true)
                    .setTypes(type)
                    .setQuery(query)
                    .addAggregation(aggregation)
                    .setSearchType(SearchType.DFS_QUERY_THEN_FETCH);

            final SearchResponse response = search.execute()
                    .actionGet();
            if (response.isTimedOut()) {
                throw new ElasticSearchException("Elastic search timed out");
            }
//            if (response.isTerminatedEarly()) {
//                throw new ArchiveException("Elastic search has been terminated early");
//            }
            if (!RestStatus.OK.equals(response.status())) {
                throw new ElasticSearchException("Elastic search has not ended successfully: " + response.status());
            }
            if (response.getAggregations() != null) {
                return aggregationBeanProducer.apply(response.getAggregations());
            } else {
                return Collections.emptyList();
            }
        } catch (Exception ex) {
            throw new ElasticSearchException("Elastic search has failed", ex);
        }
    }
}
