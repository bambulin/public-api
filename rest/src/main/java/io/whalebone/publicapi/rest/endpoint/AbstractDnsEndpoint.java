package io.whalebone.publicapi.rest.endpoint;

import io.whalebone.publicapi.ejb.dto.EDnsBucketInterval;
import io.whalebone.publicapi.ejb.dto.EDnsQueryType;
import io.whalebone.publicapi.ejb.dto.aggregate.EDnsAggregate;
import io.whalebone.publicapi.ejb.dto.aggregate.EDnsSecAggregate;
import io.whalebone.publicapi.rest.EnumParamUtils;
import io.whalebone.publicapi.rest.validation.EnumValue;
import io.whalebone.publicapi.rest.validation.RangedInteger;
import org.apache.commons.lang3.StringUtils;

import javax.ws.rs.QueryParam;

public abstract class AbstractDnsEndpoint extends AbstractEndpoint {
    private static final long serialVersionUID = 2744945027050689603L;
    private static final EDnsBucketInterval DEFAULT_INTERVAL = EDnsBucketInterval.HOUR;

    @QueryParam("query_type")
    private String queryTypeParam;
    @QueryParam("aggregate")
    private String aggregateParam;
    @QueryParam("tld")
    private String tld;
    @QueryParam("query")
    private String query;
    @QueryParam("interval")
    private String intervalParam;

    public void setQueryTypeParam(String typeParam) {
        this.queryTypeParam = typeParam;
    }

    @EnumValue(EDnsQueryType.class)
    public String getQueryTypeParam() {
        return queryTypeParam;
    }

    @RangedInteger(min = 1, max = 14)
    @Override
    public String getDaysParam() {
        return super.getDaysParam();
    }

    public void setAggregateParam(String aggregateParam) {
        this.aggregateParam = aggregateParam;
    }

    public String getAggregateParam() {
        return aggregateParam;
    }

    public void setTld(String tld) {
        this.tld = tld;
    }

    public String getTld() {
        return tld;
    }

    public String getQuery() {
        return query;
    }

    public EDnsQueryType getQueryType() {
        if (StringUtils.isNotBlank(queryTypeParam)) {
            return EnumParamUtils.getEnumValue(EDnsQueryType.class, queryTypeParam);
        } else {
            return null;
        }
    }

    @EnumValue(EDnsBucketInterval.class)
    public String getIntervalParam() {
        return intervalParam;
    }

    public void setIntervalParam(String intervalParam) {
        this.intervalParam = intervalParam;
    }

    public EDnsBucketInterval getInterval() {
        if (StringUtils.isNotBlank(intervalParam)) {
            return EnumParamUtils.getEnumValue(EDnsBucketInterval.class, intervalParam);
        } else {
            return DEFAULT_INTERVAL;
        }
    }

}
