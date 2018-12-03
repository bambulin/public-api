package io.whalebone.publicapi.ejb.dto;

import io.whalebone.publicapi.ejb.json.ArchiveMapped;
import io.whalebone.publicapi.ejb.json.ArchiveMappedField;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class GeoIpDTO implements Serializable, ArchiveMapped {
    private static final long serialVersionUID = -7957256798196555250L;

    @ArchiveMappedField("latitude")
    private Double latitude;
    @ArchiveMappedField("longitude")
    private Double longitude;
    @ArchiveMappedField("country_code2")
    private String countryCode2;
}
