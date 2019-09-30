package io.whalebone.publicapi.ejb.criteria;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
abstract class AbstractCriteria {
    private String clientId;
}
