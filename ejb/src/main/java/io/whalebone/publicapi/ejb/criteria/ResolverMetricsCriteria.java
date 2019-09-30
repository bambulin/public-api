package io.whalebone.publicapi.ejb.criteria;

import io.whalebone.publicapi.ejb.dto.ETimeInterval;
import lombok.Builder;
import lombok.Getter;

@Getter
public class ResolverMetricsCriteria extends DaysRestrictedCriteria {
    private Integer resolverId;
    private ETimeInterval interval;

    @Builder
    public ResolverMetricsCriteria(final String clientId,
                                   final int days,
                                   final Integer resolverId,
                                   final ETimeInterval interval) {
        super(clientId, days);
        this.resolverId = resolverId;
        this.interval = interval;
    }
}
