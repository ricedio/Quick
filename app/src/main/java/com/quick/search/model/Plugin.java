package com.quick.search.model;

import com.quick.search.model.info.PluginSearchResultInfo;
import com.quick.search.model.info.SearchResultInfo;
import com.quick.search.model.info.Searchable;
import com.quick.utils.PinyinUtil;

import java.util.ArrayList;
import java.util.List;

public class Plugin implements Searchable {
    private static Plugin plugin;

    private List<PluginSearchResultInfo> pluginSearchResultInfoList=new ArrayList<>();
    private List<String> searchablestr=new ArrayList<>();

    //测试插件数据
    private String[] cs={"测试插件","hello world","Json格式化","Json2JavaBean","编码转换","聚合翻译","插件开发","插件管理"};


    public static Plugin getInstance(){
        if (plugin==null)
            plugin=new Plugin();
        return plugin;
    }

    private Plugin(){
        for(String s:cs){
            String name = s;
            String pinyin = PinyinUtil.chinese2pinyin(name);
            String pinyin_short = PinyinUtil.getFirstLetter(pinyin);
            String searchable_name = name + pinyin + pinyin_short;
            searchablestr.add( searchable_name );
            pluginSearchResultInfoList.add( new PluginSearchResultInfo(null, s , s , "测试介绍"+s ,null) );
        }

    }

    @Override
    public List<SearchResultInfo> search(String str) {
        List<SearchResultInfo> pluginInfo=new ArrayList<>();
        for (String name:searchablestr){
            if (name.toLowerCase().contains(str.toLowerCase())){
                pluginInfo.add(pluginSearchResultInfoList.get(searchablestr.indexOf(name)));
            }
        }
        return pluginInfo;
    }
}
