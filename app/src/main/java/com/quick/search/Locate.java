package com.quick.search;

import android.content.Context;

import com.quick.search.model.info.SearchResultInfo;

import java.util.ArrayList;
import java.util.List;

public class Locate {

    public enum Range {
        APP, //软件
        CONTACT, //联系人
        SMS, //短信
        PLUGIN, //插件
        MUSIC, //音乐
        WEB //浏览器
    }
    private static Locate locate;
    private static Context context;

    public static Locate getInstance(Context c){
        if (locate==null){
            locate=new Locate();
            context=c;
        }
        return locate;
    }


    public List<SearchResultInfo> search(String str){
        List<SearchResultInfo> resultList = new ArrayList<SearchResultInfo>();
        List<Range> range = range();
        for (Range i : range)
            resultList.addAll(SearchFactory.searchFactoryMethod(i).search(String.valueOf(str)));
        return resultList;
    }

    private List<Range> range(){
        List<Range> list=new ArrayList<>();
        list.add(Range.APP);
        list.add(Range.CONTACT);
        list.add(Range.SMS);
        //list.add(Range.PLUGIN);
        //list.add(Range.WEB);
        return list;
    }

}
