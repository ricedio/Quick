package com.quick.search.model.info;

import android.content.Intent;
import android.graphics.drawable.Drawable;

public class SmsSearchResultInfo extends SearchResultInfo{
    private String content;
    private String date;
    private String groupId;
    private Intent click;
    public SmsSearchResultInfo(Drawable icon,String groupId, String title,String content,String date,Intent c) {
        super(icon, title);
        this.content=content;
        this.click=c;
        this.groupId=groupId;
        this.date=date;
    }

    public String getDate() {
        return date;
    }

    public String getGroupId() {
        return groupId;
    }

    public Intent getClick() {
        return click;
    }

    public String getContent() {
        return content;
    }
}
