package io.whalebone.publicapi.rest.endpoint.generic;

import io.whalebone.publicapi.ejb.PublicApiService;
import io.whalebone.publicapi.rest.auth.AuthInterceptor;
import io.whalebone.publicapi.rest.validation.ValidInteger;
import org.apache.commons.lang3.StringUtils;

import javax.ejb.EJB;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.QueryParam;
import java.io.Serializable;

abstract class AbstractEndpoint implements Serializable {
    private static final long serialVersionUID = -2618560708692266417L;

    @EJB
    protected PublicApiService publicApiService;

    @HeaderParam(AuthInterceptor.CLIENT_ID_HEADER)
    private String clientId;

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
}
