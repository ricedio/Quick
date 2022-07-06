package com.quick.search.model.info;

import android.content.Intent;
import android.graphics.drawable.Drawable;

public class PluginSearchResultInfo extends SearchResultInfo{

    private Intent click;
    private String content;
    private String author;

    public PluginSearchResultInfo(Drawable icon, String title,String author,String content,Intent c) {
        super(icon, title);
        this.click=c;
        this.author=author;
        this.content=content;
    }

    public String getContent() {
        return content;
    }

    public String getAuthor() {
        return author;
    }

    public Intent getClick() {
        return click;
    }
}
