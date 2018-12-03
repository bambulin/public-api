package io.whalebone.publicapi.ejb.criteria;

import io.whalebone.publicapi.ejb.dto.EReason;
import io.whalebone.publicapi.ejb.dto.EThreadType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
public class EventsCriteria extends ClientCriteria {
    private EThreadType type;
    private EReason reason;

    @Builder
    public EventsCriteria(String clientId,
                          String domain,
                          int days,
                          Integer resolverId,
                          String clientIp,
                          EThreadType type,
                          EReason reason) {
        super(clientId, domain, days, resolverId, clientIp);
        this.type = type;
        this.reason = reason;
    }
}
