package com.milwar.kaosuarina.data.model;

import com.google.gson.annotations.SerializedName;

public class PlayerAttributeData {
    public String attrId;
    public String name;
    @SerializedName("short")
    public String shortLabel;
    public String governs;
    public String description;
}
