package io.whalebone.publicapi.rest.endpoint;

import io.whalebone.publicapi.ejb.PublicApiService;
import io.whalebone.publicapi.ejb.criteria.DnsTimelineCriteria;
import io.whalebone.publicapi.ejb.dto.DnsTimeBucketDTO;
import io.whalebone.publicapi.ejb.dto.aggregate.EDnsSecAggregate;
import io.whalebone.publicapi.rest.EnumParamUtils;
import io.whalebone.publicapi.rest.validation.EnumValue;
import org.apache.commons.lang3.StringUtils;

import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/1/dnssec")
@RequestScoped
public class DnsSecEndpoint extends AbstractDnsEndpoint {
    private static final long serialVersionUID = 3680186395638851454L;

    private static final EDnsSecAggregate DEFAULT_AGGREGATE = EDnsSecAggregate.QUERY_TYPE;

    @EJB
    private PublicApiService publicApiService;

    @EnumValue(EDnsSecAggregate.class)
    public String getAggregateParam() {
        return super.getAggregateParam();
    }

    private EDnsSecAggregate getAggregate() {
        if (StringUtils.isNotBlank(getAggregateParam())) {
            return EnumParamUtils.getEnumValue(EDnsSecAggregate.class, getAggregateParam());
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
                .days(getDays())
                .domain(getDomain())
                .query(getQuery())
                .resolverId(getResolverId())
                .tld(getTld())
                .build();

        List<DnsTimeBucketDTO> dnsRecords = publicApiService.dnsSecTimeline(criteria);
        return Response.ok(dnsRecords).build();
    }
}
