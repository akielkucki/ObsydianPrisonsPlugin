package com.obsydian.obsydianprisons.mine;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public record PlotMineObject(
        @SerializedName("Mine")
        List<MineData> mines
) {
}



