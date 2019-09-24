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
    public EventsCriteria(final String clientId,
                          final String domain,
                          final int days,
                          final Integer resolverId,
                          final String clientIp,
                          final String deviceId,
                          final EThreatType threatType,
                          final EReason reason) {
        super(clientId, domain, days, resolverId, clientIp, deviceId);
        this.threatType = threatType;
        this.reason = reason;
    }
}
