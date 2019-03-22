package io.whalebone.publicapi.rest.endpoint;

import io.whalebone.publicapi.ejb.PublicApiService;
import io.whalebone.publicapi.ejb.criteria.DnsTimelineCriteria;
import io.whalebone.publicapi.ejb.dto.DnsTimeBucketDTO;
import io.whalebone.publicapi.ejb.dto.aggregate.EDnsAggregate;
import io.whalebone.publicapi.rest.EnumParamUtils;
import io.whalebone.publicapi.rest.validation.EnumValue;
import org.apache.commons.lang3.StringUtils;

import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/1/dns")
@RequestScoped
public class DnsEndpoint extends AbstractDnsEndpoint {
    private static final long serialVersionUID = 2399760037640668858L;
    private static final EDnsAggregate DEFAULT_AGGREGATE = EDnsAggregate.QUERY_TYPE;

    @EJB
    private PublicApiService publicApiService;

    /*
     * parameters must be String so we can provide meaningful validation message in case of invalid parameter
     * otherwise wildfly returns 404 NOT FOUND which is misleading
     */
    @QueryParam("client_ip")
    private String clientIp;
    @QueryParam("answer")
    private String answer;
    @QueryParam("dga")
    private boolean dga;

    @EnumValue(EDnsAggregate.class)
    public String getAggregateParam() {
        return super.getAggregateParam();
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public void setDga(boolean dga) {
        this.dga = dga;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

    private EDnsAggregate getAggregate() {
        if (StringUtils.isNotBlank(getAggregateParam())) {
            return EnumParamUtils.getEnumValue(EDnsAggregate.class, getAggregateParam());
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
                .clientIp(clientIp)
                .query(getQuery())
                .answer(answer)
                .days(getDays())
                .dga(dga)
                .domain(getDomain())
                .resolverId(getResolverId())
                .tld(getTld())
                .build();

        List<DnsTimeBucketDTO> dnsRecords = publicApiService.dnsTimeline(criteria);
        return Response.ok(dnsRecords).build();
    }
}
