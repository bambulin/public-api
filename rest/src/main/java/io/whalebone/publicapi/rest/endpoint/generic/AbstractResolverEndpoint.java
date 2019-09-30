package io.whalebone.publicapi.rest.endpoint.generic;

import io.whalebone.publicapi.rest.validation.ValidInteger;
import org.apache.commons.lang3.StringUtils;

import javax.ws.rs.QueryParam;

public abstract class AbstractResolverEndpoint extends AbstractDaysRestrictedEndpoint {
    private static final long serialVersionUID = -7549648281994746833L;

    @QueryParam("resolver_id")
    private String resolverIdParam;

    @ValidInteger
    public String getResolverIdParam() {
        return resolverIdParam;
    }

    public void setResolverIdParam(String resolverIdParam) {
        this.resolverIdParam = resolverIdParam;
    }

    public Integer getResolverId() {
        if (StringUtils.isNotBlank(resolverIdParam)) {
            return Integer.parseInt(resolverIdParam);
        }
        return null;
    }
}
