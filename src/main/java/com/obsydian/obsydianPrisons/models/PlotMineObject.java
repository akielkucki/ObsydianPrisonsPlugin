package com.obsydian.obsydianPrisons.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public record PlotMineObject(
        @SerializedName("Mine")
        List<MineData> mines
) {
}



