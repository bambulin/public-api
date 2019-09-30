package io.whalebone.publicapi.ejb.criteria;

import lombok.Getter;

@Getter
abstract class ClientCriteria extends DaysRestrictedCriteria {
    private String domain;
    private Integer resolverId;
    private String clientIp;
    private String deviceId;

    ClientCriteria(final String clientId,
                   final int days,
                   final String domain,
                   final Integer resolverId,
                   final String clientIp,
                   final String deviceId) {
        super(clientId, days);
        this.domain = domain;
        this.resolverId = resolverId;
        this.clientIp = clientIp;
        this.deviceId = deviceId;
    }
}
