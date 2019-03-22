package io.whalebone.publicapi.rest.endpoint;

import javax.ws.rs.QueryParam;

public abstract class ClientAbstractEndpoint extends AbstractEndpoint {
    private static final long serialVersionUID = 1525891904714600400L;

    @QueryParam("client_ip")
    private String clientIp;

    String getClientIp() {
        return clientIp;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }
}
