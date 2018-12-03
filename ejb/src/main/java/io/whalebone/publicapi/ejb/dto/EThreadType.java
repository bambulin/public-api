package io.whalebone.publicapi.ejb.dto;

import com.google.gson.annotations.SerializedName;

public enum EThreadType {
    @SerializedName("c&c")
    C_AND_C,
    BLACKLIST,
    MALWARE,
    PHISHING,
    EXPLOIT
}
