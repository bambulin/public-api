package io.whalebone.publicapi.ejb.dto;

import com.google.gson.annotations.JsonAdapter;
import io.whalebone.publicapi.ejb.json.ArchiveMapped;
import io.whalebone.publicapi.ejb.json.ArchiveMappedField;
import io.whalebone.publicapi.ejb.json.ZonedDateTimeAdapter;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.ZonedDateTime;

@Getter
@Setter
public class EventDTO implements Serializable, ArchiveMapped {
    private static final long serialVersionUID = -6393906363174373694L;

    private String eventId;
    @ArchiveMappedField("logged")
    @JsonAdapter(ZonedDateTimeAdapter.class)
    private ZonedDateTime timestamp;
    @ArchiveMappedField("accuracy.accuracy")
    private Integer accuracy;
    @ArchiveMappedField("resolver_id")
    private Integer resolverId;
    @ArchiveMappedField("action")
    private String action;
    @ArchiveMappedField("action_reason")
    private EReason reason;
    @ArchiveMappedField("request.ip")
    private String clientIp;
    @ArchiveMappedField("reason.fqdn")
    private String domain;
    @ArchiveMappedField("matched_iocs.classification.type")
    private EThreadType[] type;
    @ArchiveMappedField("matched_iocs.classification.identifier")
    private String[] identifier;
    @ArchiveMappedField("geoip")
    private GeoIpDTO geoIp;
}
