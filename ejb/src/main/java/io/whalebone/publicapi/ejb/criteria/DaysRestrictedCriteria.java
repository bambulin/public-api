package io.whalebone.publicapi.ejb.criteria;

import lombok.Getter;

@Getter
abstract class DaysRestrictedCriteria extends AbstractCriteria {
    private int days;

    DaysRestrictedCriteria(final String clientId, final int days) {
        super(clientId);
        this.days = days;
    }
}
