package com.quick.search.model.info;

import android.content.Intent;
import android.graphics.drawable.Drawable;

public class ContactSearchResultInfo extends SearchResultInfo{

    private Intent click;
    private String phone;

    public ContactSearchResultInfo(Drawable icon, String title,String phone,Intent click) {
        super(icon, title);
        this.click=click;
        this.phone=phone;
    }

    public String getPhone() {
        return phone;
    }

    public Intent getClick() {
        return click;
    }
}
