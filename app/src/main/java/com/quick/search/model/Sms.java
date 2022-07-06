package com.quick.search.model;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.net.Uri;

import com.quick.Quick;
import com.quick.search.model.info.SearchResultInfo;
import com.quick.search.model.info.Searchable;
import com.quick.search.model.info.SmsSearchResultInfo;
import com.quick.utils.PinyinUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Sms implements Searchable {

    private static Sms sms;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault());

    private List<SmsSearchResultInfo> smsSearchResultInfoList = new ArrayList<>();
    private List<String> searchablestr = new ArrayList<>();

    public static Sms getInstance() {
        if (sms == null)
            sms = new Sms();
        return sms;
    }

    @SuppressLint("Range")
    private Sms() {
        String[] projection = {"thread_id AS group_id", "address AS contact", "body AS msg_content", "date"};
        Cursor c = Quick.getInstance().getContentResolver().query(
                Uri.parse("content://sms"),
                //new String[]{"_id", "address", "body", "date", "person", "type"},
                projection,
                null, null, "date desc");

        if (c != null) {
            while (c.moveToNext()) {
                String groupId = c.getString(c.getColumnIndex("group_id"));
                String contact = c.getString(c.getColumnIndex("contact"));
                String msgContent = c.getString(c.getColumnIndex("msg_content"));
                String date = dateFormat.format(c.getLong(c.getColumnIndex("date")));


                String pinyin = PinyinUtil.chinese2pinyin(contact);
                String pinyin_short = PinyinUtil.getFirstLetter(pinyin);

                //String msgpinyin = PinyinUtil.chinese2pinyin(msgContent);
                //String msgpinyin_short = PinyinUtil.getFirstLetter(msgpinyin);

                String searchable_name = contact + pinyin + pinyin_short + msgContent ;
                searchablestr.add(searchable_name);

                SmsSearchResultInfo s = new SmsSearchResultInfo(null, groupId, contact, msgContent, date, null);
                smsSearchResultInfoList.add(s);
            }
            c.close();
        }

    }

    @Override
    public List<SearchResultInfo> search(String str) {
        List<SearchResultInfo> smsInfo = new ArrayList<>();
        for (String name : searchablestr) {
            if (name.toLowerCase().contains(str.toLowerCase())) {
                smsInfo.add(smsSearchResultInfoList.get(searchablestr.indexOf(name)));
            }
        }
        return smsInfo;

    }
}
