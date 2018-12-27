package io.whalebone.publicapi.rest.endpoint;

import io.whalebone.publicapi.rest.validation.ValidInteger;
import org.apache.commons.lang3.StringUtils;

import javax.ws.rs.QueryParam;
import javax.ws.rs.HeaderParam;
import java.io.Serializable;

abstract class AbstractEndpoint implements Serializable {
    private static final long serialVersionUID = -2618560708692266417L;
    private static final int DEFAULT_DAYS = 1;

    //TODO will be discussed
    @HeaderParam("whalebone_client_id")
    private String clientId;
    @QueryParam("client_ip")
    private String clientIp;
    @QueryParam("resolver_id")
    private String resolverIdParam;
    @QueryParam("domain")
    private String domain;
    @QueryParam("days")
    private String daysParam;

    String getClientId() { return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }


    String getClientIp() {
        return clientIp;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

    @ValidInteger
    public String getResolverIdParam() {
        return resolverIdParam;
    }

    public void setResolverIdParam(String resolverIdParam) {
        this.resolverIdParam = resolverIdParam;
    }

    public String getDaysParam() {
        return daysParam;
    }

    String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public void setDaysParam(String daysParam) {
        this.daysParam = daysParam;
    }

    Integer getResolverId() {
        if (StringUtils.isNotBlank(resolverIdParam)) {
            return Integer.parseInt(resolverIdParam);
        }
        return null;
    }

    int getDays() {
        if (StringUtils.isNotBlank(daysParam)) {
            return Integer.parseInt(daysParam);
        } else {
            return DEFAULT_DAYS;
        }
    }
}
