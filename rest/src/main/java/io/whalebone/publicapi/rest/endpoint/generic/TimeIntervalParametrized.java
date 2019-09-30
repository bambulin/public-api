package io.whalebone.publicapi.rest.endpoint.generic;

import io.whalebone.publicapi.ejb.dto.ETimeInterval;
import io.whalebone.publicapi.rest.EnumParamUtils;
import org.apache.commons.lang3.StringUtils;

public interface TimeIntervalParametrized {
    ETimeInterval getDefaultInterval();

    String getIntervalParam();

    default ETimeInterval getInterval() {
        if (StringUtils.isNotBlank(getIntervalParam())) {
            return EnumParamUtils.getEnumValue(ETimeInterval.class, getIntervalParam());
        } else {
            return getDefaultInterval();
        }
    }
}
