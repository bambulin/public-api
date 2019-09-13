package io.whalebone.publicapi.ejb.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ThreatTypeCountDTO {
    private EThreatType threatType;
    private long count;
}
