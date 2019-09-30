package io.whalebone.publicapi.rest.endpoint;

import io.whalebone.publicapi.ejb.criteria.ResolverMetricsCriteria;
import io.whalebone.publicapi.ejb.dto.ETimeInterval;
import io.whalebone.publicapi.ejb.dto.ResolverMetricsDTO;
import io.whalebone.publicapi.rest.endpoint.generic.AbstractResolverEndpoint;
import io.whalebone.publicapi.rest.endpoint.generic.TimeIntervalParametrized;
import io.whalebone.publicapi.rest.validation.EnumValue;
import io.whalebone.publicapi.rest.validation.RangedInteger;

import javax.enterprise.context.RequestScoped;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/1/resolver")
@RequestScoped
public class ResolverEndpoint extends AbstractResolverEndpoint implements TimeIntervalParametrized {
    private static final long serialVersionUID = 1788068693890493159L;
    private static final ETimeInterval DEFAULT_INTERVAL = ETimeInterval.HOUR;

    @QueryParam("interval")
    private String intervalParam;

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

    @Override
    @RangedInteger(min = 1, max = 30)
    public String getDaysParam() {
        return super.getDaysParam();
    }

    @GET
    @Path("/metrics")
    @Produces("application/json;charset=UTF-8")
    @Consumes("application/json;charset=UTF-8")
    public Response metrics() {
        ResolverMetricsCriteria criteria = ResolverMetricsCriteria.builder()
                .clientId(getClientId())
                .resolverId(getResolverId())
                .days(getDays())
                .interval(getInterval())
                .build();

        List<ResolverMetricsDTO> metrics = publicApiService.resolverMetrics(criteria);
        return Response.ok(metrics, MediaType.APPLICATION_JSON_TYPE).build();
    }
}
