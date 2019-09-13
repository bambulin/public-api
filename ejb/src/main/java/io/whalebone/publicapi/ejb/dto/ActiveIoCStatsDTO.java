package io.whalebone.publicapi.ejb.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class ActiveIoCStatsDTO {
    private long totalCount;
    private Map<EThreatType, Long> countsPerTypeMap;
}
