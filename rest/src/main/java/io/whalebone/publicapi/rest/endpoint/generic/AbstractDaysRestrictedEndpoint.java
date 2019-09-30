package io.whalebone.publicapi.rest.endpoint.generic;

import org.apache.commons.lang3.StringUtils;

import javax.ws.rs.QueryParam;

public abstract class AbstractDaysRestrictedEndpoint extends AbstractEndpoint{
    private static final long serialVersionUID = 8760989389794683990L;
    private static final int DEFAULT_DAYS = 1;

    @QueryParam("days")
    private String daysParam;

    public String getDaysParam() {
        return daysParam;
    }

    public void setDaysParam(String daysParam) {
        this.daysParam = daysParam;
    }

    public int getDays() {
        if (StringUtils.isNotBlank(daysParam)) {
            return Integer.parseInt(daysParam);
        } else {
            return DEFAULT_DAYS;
        }
    }
}
