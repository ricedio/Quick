package com.quick.search.model;

import com.quick.search.model.info.MusicSearchResultInfo;
import com.quick.search.model.info.SearchResultInfo;
import com.quick.search.model.info.Searchable;
import com.quick.search.model.info.SmsSearchResultInfo;

import java.util.ArrayList;
import java.util.List;

public class Music implements Searchable {
    private static Music music;

    private List<MusicSearchResultInfo> musicSearchResultInfoList=new ArrayList<>();
    private List<String> searchablestr=new ArrayList<>();

    public static Music getInstance(){
        if (music==null)
            music=new Music();
        return music;
    }

    private Music(){

    }

    @Override
    public List<SearchResultInfo> search(String str) {
        List<SearchResultInfo> smsInfo=new ArrayList<>();
        for (String name:searchablestr){
            if (name.toLowerCase().contains(str.toLowerCase())){
                smsInfo.add(musicSearchResultInfoList.get(searchablestr.indexOf(name)));
            }
        }
        return smsInfo;

    }
}
