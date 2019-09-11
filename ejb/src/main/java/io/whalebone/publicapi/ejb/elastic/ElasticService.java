package io.whalebone.publicapi.ejb.elastic;

import com.google.gson.Gson;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.action.support.IndicesOptions;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AbstractAggregationBuilder;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortBuilder;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

@Stateless
public class ElasticService implements Serializable {
    private static final long serialVersionUID = 8884963960567953481L;
    private static final int SEARCH_QUERY_SIZE = Integer.parseInt(System.getenv().getOrDefault("ELASTIC_QUERY_MAX_SIZE", "10000"));

    @Inject
    private RestHighLevelClient elasticClient;
    @Inject
    @Elastic
    private Gson gson;

    public <T> List<T> search(final QueryBuilder query,
                              final SortBuilder sort,
                              final String[] indices,
                              final String type,
                              final Type beanType) throws ElasticSearchException {
        try {
            SearchSourceBuilder searchSource = new SearchSourceBuilder()
                    .query(query)
                    .size(SEARCH_QUERY_SIZE); // TODO remove when scroll is implemented
            if (sort != null) {
                searchSource.sort(sort);
            }
            final SearchRequest search = new SearchRequest()
                    .source(searchSource)
                    .indices(indices)
                    .types(type)
                    .requestCache(true)
                    .searchType(SearchType.DFS_QUERY_THEN_FETCH)
                    // closed indices issue, see https://github.com/elastic/elasticsearch/issues/20105
                    .indicesOptions(IndicesOptions.fromOptions(
                            true, true, true, false,
                            SearchRequest.DEFAULT_INDICES_OPTIONS
                    ));

            final SearchResponse response = elasticClient.search(search, RequestOptions.DEFAULT);
            if (response.isTimedOut()) {
                throw new ElasticSearchException("Elastic search timed out");
            }
            if (!RestStatus.OK.equals(response.status())) {
                throw new ElasticSearchException("Elastic search has not ended successfully: " + response.status());
            }
            if (response.getHits() == null || response.getHits().getTotalHits() == 0) {
                return new ArrayList<>();
            } else {
                List<T> docs = new ArrayList<>((int) response.getHits().getTotalHits());
                for (SearchHit hit : response.getHits()) {
                    T doc = gson.fromJson(hit.getSourceAsString(), beanType);
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
                                             final String[] indices,
                                             final String type,
                                             final Function<Aggregations, List<T>> aggregationBeanProducer
    ) throws ElasticSearchException {
        try {
            final SearchRequest search = new SearchRequest()
                    .source(new SearchSourceBuilder()
                            .query(query)
                            .aggregation(aggregation)
                    )
                    .indices(indices)
                    .types(type)
                    .requestCache(true)
                    .searchType(SearchType.DFS_QUERY_THEN_FETCH)
                    // closed indices issue, see https://github.com/elastic/elasticsearch/issues/20105
                    .indicesOptions(IndicesOptions.fromOptions(
                            true, true, true, false,
                            SearchRequest.DEFAULT_INDICES_OPTIONS
                    ));

            final SearchResponse response = elasticClient.search(search, RequestOptions.DEFAULT);
            if (response.isTimedOut()) {
                throw new ElasticSearchException("Elastic search timed out");
            }
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
