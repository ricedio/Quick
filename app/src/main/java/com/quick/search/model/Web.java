package com.quick.search.model;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;

import com.quick.Quick;
import com.quick.R;
import com.quick.search.model.info.SearchResultInfo;
import com.quick.search.model.info.Searchable;
import com.quick.search.model.info.WebSearchResultInfo;
import com.quick.utils.CommonUtil;

import java.util.ArrayList;
import java.util.List;

public class Web implements Searchable {

    private static Web mWeb;

    private Web() {
    }

    public static Web getInstance() {
        if (mWeb == null)
            mWeb = new Web();
        return mWeb;
    }

    @Override
    public List<SearchResultInfo> search(String str) {
        List<SearchResultInfo> appInfo = new ArrayList<>();
        Resources resources = Quick.getInstance().getResources();
        String title = "调用浏览器";
        Drawable icon = resources.getDrawable(R.drawable.ic_launcher_foreground);
        Intent click = CommonUtil.getWebSearchIntent(str);
        appInfo.add(new WebSearchResultInfo(icon, title, click));
        return appInfo;
    }
}