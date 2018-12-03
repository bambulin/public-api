package io.whalebone.publicapi.rest.endpoint;

import com.google.gson.Gson;
import io.whalebone.publicapi.ejb.PublicApiService;
import io.whalebone.publicapi.ejb.criteria.EventsCriteria;
import io.whalebone.publicapi.ejb.dto.EReason;
import io.whalebone.publicapi.ejb.dto.EThreadType;
import io.whalebone.publicapi.ejb.dto.EventDTO;
import io.whalebone.publicapi.rest.ClientIdProvider;
import io.whalebone.publicapi.rest.EnumParamUtils;
import io.whalebone.publicapi.rest.validation.EnumValue;
import io.whalebone.publicapi.rest.validation.RangedInteger;
import org.apache.commons.lang3.StringUtils;

import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/1/events")
@RequestScoped
public class EventEndpoint extends AbstractEndpoint {
    private static final long serialVersionUID = -4829163700948200095L;

    @EJB
    private PublicApiService publicApiService;
    @Inject
    private ClientIdProvider clientIdProvider;

    @QueryParam("type")
    private String typeParam;
    @QueryParam("reason")
    private String reasonParam;

    public void setTypeParam(String typeParam) {
        this.typeParam = typeParam;
    }

    @EnumValue(EThreadType.class)
    public String getTypeParam() {
        return typeParam;
    }

    public void setReasonParam(String reasonParam) {
        this.reasonParam = reasonParam;
    }

    @EnumValue(EReason.class)
    public String getReasonParam() {
        return reasonParam;
    }

    private EThreadType getType() {
        if (StringUtils.isNotBlank(typeParam)) {
            return EnumParamUtils.getEnumValue(EThreadType.class, typeParam);
        }
        return null;
    }

    private EReason getReason() {
        if (StringUtils.isNotBlank(reasonParam)) {
            return EnumParamUtils.getEnumValue(EReason.class, reasonParam);
        }
        return null;
    }

    @Override
    @RangedInteger(min = 1, max = 90)
    public String getDaysParam() {
        return super.getDaysParam();
    }

    @GET
    @Path("/search")
    @Produces("application/json;charset=UTF-8")
    @Consumes("application/json;charset=UTF-8")
    public Response search() {
        EventsCriteria criteria = EventsCriteria.builder()
                .clientId(clientIdProvider.getClientId())
                .clientIp(getClientIp())
                .days(getDays())
                .reason(getReason())
                .domain(getDomain())
                .type(getType())
                .resolverId(getResolverId())
                .build();
        List<EventDTO> events = publicApiService.eventsSearch(criteria);
        return Response.ok(events, MediaType.APPLICATION_JSON_TYPE).build();
    }
}
