package com.quick.search.model;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;

import com.quick.Quick;
import com.quick.search.model.info.AppSearchResultInfo;
import com.quick.search.model.info.SearchResultInfo;
import com.quick.search.model.info.Searchable;
import com.quick.utils.PinyinUtil;

import java.util.ArrayList;
import java.util.List;

public class App implements Searchable {

    private static App app;
    private List<AppSearchResultInfo> appSearchResultInfoList=new ArrayList<>();
    private List<String> searchablestr=new ArrayList<>();

    public static App getInstance(){
        if (app==null)
            app=new App();
        return app;
    }

    private App(){
        PackageManager packageManager= Quick.getInstance().getPackageManager();
        Intent intent=new Intent(Intent.ACTION_MAIN,null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        for(ResolveInfo info:packageManager.queryIntentActivities(intent,0)){
            String original=String.valueOf(info.loadLabel(packageManager));
            String py= PinyinUtil.chinese2pinyin(original);
            String name=original+py+PinyinUtil.getFirstLetter(py);
            searchablestr.add(name);
            Drawable icon=info.loadIcon(packageManager);
            Intent click=packageManager.getLaunchIntentForPackage(info.activityInfo.applicationInfo.packageName);
            appSearchResultInfoList.add(new AppSearchResultInfo(icon,original,click));

        }

    }

    @Override
    public List<SearchResultInfo> search(String str) {
        List<SearchResultInfo> appInfo=new ArrayList<>();
        for (String name:searchablestr){
            if (name.toLowerCase().contains(str.toLowerCase())){
                appInfo.add(appSearchResultInfoList.get(searchablestr.indexOf(name)));
            }
        }
        return appInfo;
    }
}
