package io.whalebone.publicapi.ejb.criteria;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
abstract class ClientCriteria {
    private String clientId;
    private String domain;
    private int days;
    private Integer resolverId;
    private String clientIp;
    private String deviceId;
}
