package io.whalebone.publicapi.ejb.criteria;

import io.whalebone.publicapi.ejb.dto.EReason;
import io.whalebone.publicapi.ejb.dto.EThreatType;
import lombok.Builder;
import lombok.Getter;

@Getter
public class EventsCriteria extends ClientCriteria {
    private EThreatType threatType;
    private EReason reason;

    @Builder
    public EventsCriteria(String clientId,
                          String domain,
                          int days,
                          Integer resolverId,
                          String clientIp,
                          EThreatType threatType,
                          EReason reason) {
        super(clientId, domain, days, resolverId, clientIp);
        this.threatType = threatType;
        this.reason = reason;
    }
}
