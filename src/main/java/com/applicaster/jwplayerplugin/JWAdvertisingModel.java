package com.applicaster.jwplayerplugin;

import com.google.gson.annotations.SerializedName;

public class JWAdvertisingModel {

    @SerializedName("ad_url")
    String url;
    @SerializedName("type")
    String type;
    @SerializedName("offest")
    String offest;
    @SerializedName("skip_offset")
    String skip_offset;

    public String getUrl() {
        return url;
    }

    public String getType() {
        return type;
    }

    public String getOffest() {
        return offest;
    }

    public String getSkip_offset() {
        return skip_offset;
    }
}
