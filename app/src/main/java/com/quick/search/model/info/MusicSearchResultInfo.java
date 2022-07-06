package com.quick.search.model.info;

import android.content.Intent;
import android.graphics.drawable.Drawable;

public class MusicSearchResultInfo extends SearchResultInfo {

    private Intent click;
    private String author;

    public MusicSearchResultInfo(Drawable icon, String name, String author, Intent click) {
        super(icon, name);
        this.author = author;
        this.click = click;
    }

    public Intent getClick() {
        return click;
    }

    public String getAuthor() {
        return author;
    }
}
