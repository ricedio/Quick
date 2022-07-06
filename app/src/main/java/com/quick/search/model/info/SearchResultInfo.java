package com.quick.search.model.info;

import android.content.Intent;
import android.graphics.drawable.Drawable;

public class SearchResultInfo {
    private Drawable icon;
    private String title;

    public SearchResultInfo(Drawable icon, String title) {
        this.icon = icon;
        this.title = title;
    }

    public Drawable getIcon() {
        return icon;
    }

    public String getTitle() {
        return title;
    }


}
