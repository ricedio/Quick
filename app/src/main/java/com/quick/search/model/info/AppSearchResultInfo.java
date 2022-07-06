package com.quick.search.model.info;

import android.content.Intent;
import android.graphics.drawable.Drawable;


public class AppSearchResultInfo extends SearchResultInfo {

    private Intent click;
    public AppSearchResultInfo(Drawable icon, String title,Intent click) {
        super(icon, title);
        this.click=click;
    }

    public Intent getClick() {
        return click;
    }

}
