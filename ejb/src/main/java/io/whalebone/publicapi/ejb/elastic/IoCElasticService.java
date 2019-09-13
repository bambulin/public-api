package io.whalebone.publicapi.ejb.elastic;

import com.google.gson.Gson;
import io.whalebone.publicapi.ejb.dto.ActiveIoCStatsDTO;
import io.whalebone.publicapi.ejb.dto.EThreatType;
import io.whalebone.publicapi.ejb.dto.ThreatTypeCountDTO;
import org.apache.commons.collections4.CollectionUtils;
import org.elasticsearch.forarchive.action.search.SearchResponse;
import org.elasticsearch.forarchive.action.search.SearchType;
import org.elasticsearch.forarchive.client.Client;
import org.elasticsearch.forarchive.index.query.QueryBuilders;
import org.elasticsearch.forarchive.search.aggregations.AggregationBuilders;
import org.elasticsearch.forarchive.search.aggregations.bucket.terms.Terms;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * !!IMPORTANT!! all imported elastic search classes must be imported from
 * org.elasticsearch.forarchive.* packages
 */
@Stateless
public class IoCElasticService implements Serializable {
    public static final String IOCS_INDEX = "iocs";
    public static final String IOCS_TYPE = "intelmq";
    private static final long serialVersionUID = -8270634771093200980L;

    @Inject
    private Client archiveClient;
    @Inject
    @Elastic
    private Gson gson;

    public ActiveIoCStatsDTO getActiveIoCStats() {
        SearchResponse sr;
        try {
            sr = archiveClient.prepareSearch(IOCS_INDEX)
                    .setTypes(IOCS_TYPE)
                    .setSearchType(SearchType.COUNT)
                    .setQueryCache(true)
                    .setQuery(QueryBuilders.termQuery("active", true))
                    .addAggregation(AggregationBuilders.terms("by_type")
                            .field("classification.type")
                            .order(Terms.Order.count(true))
                    )
                    .execute().actionGet();
        } catch (Throwable t) {
            t.printStackTrace();
            throw t;
        }
        ActiveIoCStatsDTO stats = new ActiveIoCStatsDTO();
        stats.setTotalCount(0);
        List<ThreatTypeCountDTO> threatTypeCounts = new ArrayList<>();
        Terms byType = sr.getAggregations().get("by_type");
        for (Terms.Bucket bucket : byType.getBuckets()) {
            // if the returned type is not supported (unserializable) then gson returns null
            EThreatType type = gson.fromJson(bucket.getKey(), EThreatType.class);
            // drop buckets of not supported types
            if (type == null) {
                continue;
            }
            ThreatTypeCountDTO threatTypeCount = new ThreatTypeCountDTO();
            threatTypeCount.setThreatType(type);
            threatTypeCount.setCount(bucket.getDocCount());
            threatTypeCounts.add(threatTypeCount);
            stats.setTotalCount(stats.getTotalCount() + bucket.getDocCount());
        }
        if (CollectionUtils.isNotEmpty(threatTypeCounts)) {
            stats.setThreatTypeCounts(threatTypeCounts);
        }
        return stats;
    }
}
