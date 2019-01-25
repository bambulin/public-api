package io.whalebone.publicapi.rest.endpoint;

import io.whalebone.publicapi.ejb.PublicApiService;
import io.whalebone.publicapi.ejb.criteria.DnsTimelineCriteria;
import io.whalebone.publicapi.ejb.dto.DnsTimeBucketDTO;
import io.whalebone.publicapi.ejb.dto.EAggregate;
import io.whalebone.publicapi.ejb.dto.EDnsQueryType;
import io.whalebone.publicapi.rest.EnumParamUtils;
import io.whalebone.publicapi.rest.exception.AppException;
import io.whalebone.publicapi.rest.validation.EnumValue;
import io.whalebone.publicapi.rest.validation.RangedInteger;
import org.apache.commons.lang3.StringUtils;

import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/1/dns")
@RequestScoped
public class DnsEndpoint extends AbstractEndpoint {
    private static final long serialVersionUID = 2399760037640668858L;
    private static final EAggregate DEFAULT_AGGREGATE = EAggregate.QUERY_TYPE;

    @EJB
    private PublicApiService publicApiService;

    /*
     * parameters must be String so we can provide meaningful validation message in case of invalid parameter
     * otherwise wildfly returns 404 NOT FOUND which is misleading
     */
    @QueryParam("query_type")
    private String queryTypeParam;
    @QueryParam("aggregate")
    private String aggregateParam;
    @QueryParam("answer")
    private String answer;
    @QueryParam("tld")
    private String tld;
    @QueryParam("dga")
    private boolean dga;

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

    @EnumValue(EAggregate.class)
    public String getAggregateParam() {
        return aggregateParam;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public void setTld(String tld) {
        this.tld = tld;
    }

    public void setDga(boolean dga) {
        this.dga = dga;
    }

    private EDnsQueryType getQueryType() {
        if (StringUtils.isNotBlank(queryTypeParam)) {
            return EnumParamUtils.getEnumValue(EDnsQueryType.class, queryTypeParam);
        } else {
            return null;
        }
    }

    private EAggregate getAggregate() {
        if (StringUtils.isNotBlank(aggregateParam)) {
            return EnumParamUtils.getEnumValue(EAggregate.class, aggregateParam);
        } else {
            return DEFAULT_AGGREGATE;
        }
    }

    @GET
    @Path("/timeline")
    @Produces("application/json;charset=UTF-8")
    @Consumes("application/json;charset=UTF-8")
    public Response timeline() {
        DnsTimelineCriteria criteria = DnsTimelineCriteria.builder()
                .clientId(getClientId())
                .queryType(getQueryType())
                .aggregate(getAggregate())
                .clientIp(getClientIp())
                .answer(answer)
                .days(getDays())
                .dga(dga)
                .domain(getDomain())
                .resolverId(getResolverId())
                .tld(tld)
                .build();

        List<DnsTimeBucketDTO> dnsRecords = publicApiService.dnsTimeline(criteria);
        return Response.ok(dnsRecords).build();
    }
}
