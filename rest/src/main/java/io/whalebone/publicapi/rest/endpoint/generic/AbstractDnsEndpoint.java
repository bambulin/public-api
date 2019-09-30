package io.whalebone.publicapi.rest.endpoint.generic;

import io.whalebone.publicapi.ejb.dto.EDnsQueryType;
import io.whalebone.publicapi.ejb.dto.ETimeInterval;
import io.whalebone.publicapi.rest.EnumParamUtils;
import io.whalebone.publicapi.rest.validation.EnumValue;
import io.whalebone.publicapi.rest.validation.RangedInteger;
import org.apache.commons.lang3.StringUtils;

import javax.ws.rs.QueryParam;

public abstract class AbstractDnsEndpoint extends AbstractResolverEndpoint implements TimeIntervalParametrized {
    private static final long serialVersionUID = 2744945027050689603L;
    private static final ETimeInterval DEFAULT_INTERVAL = ETimeInterval.HOUR;

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
    @QueryParam("domain")
    private String domain;

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

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public EDnsQueryType getQueryType() {
        if (StringUtils.isNotBlank(queryTypeParam)) {
            return EnumParamUtils.getEnumValue(EDnsQueryType.class, queryTypeParam);
        } else {
            return null;
        }
    }

    @EnumValue(ETimeInterval.class)
    public String getIntervalParam() {
        return intervalParam;
    }

    public void setIntervalParam(String intervalParam) {
        this.intervalParam = intervalParam;
    }

    @Override
    public ETimeInterval getDefaultInterval() {
        return DEFAULT_INTERVAL;
    }
}
