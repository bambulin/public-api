package io.whalebone.publicapi.rest.endpoint;

import io.whalebone.publicapi.ejb.dto.EDnsQueryType;
import io.whalebone.publicapi.ejb.dto.aggregate.EDnsAggregate;
import io.whalebone.publicapi.rest.EnumParamUtils;
import io.whalebone.publicapi.rest.validation.EnumValue;
import io.whalebone.publicapi.rest.validation.RangedInteger;
import org.apache.commons.lang3.StringUtils;

import javax.ws.rs.QueryParam;

public abstract class AbstractDnsEndpoint extends AbstractEndpoint {
    private static final long serialVersionUID = 2744945027050689603L;

    @QueryParam("query_type")
    private String queryTypeParam;
    @QueryParam("aggregate")
    private String aggregateParam;
    @QueryParam("tld")
    private String tld;

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

    public EDnsQueryType getQueryType() {
        if (StringUtils.isNotBlank(queryTypeParam)) {
            return EnumParamUtils.getEnumValue(EDnsQueryType.class, queryTypeParam);
        } else {
            return null;
        }
    }

}
