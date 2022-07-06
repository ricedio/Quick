package com.quick.search.model;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;

import com.quick.Quick;
import com.quick.search.model.info.ContactSearchResultInfo;
import com.quick.search.model.info.PluginSearchResultInfo;
import com.quick.search.model.info.SearchResultInfo;
import com.quick.search.model.info.Searchable;
import com.quick.utils.BitmapUtil;
import com.quick.utils.PinyinUtil;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class Contact implements Searchable {
    private static Contact contact;

    private List<ContactSearchResultInfo> contactSearchResultInfoList = new ArrayList<>();
    private List<String> searchablestr = new ArrayList<>();

    public static Contact getInstance() {
        if (contact == null)
            contact = new Contact();
        return contact;
    }

    @SuppressLint("Range")
    private Contact() {
        ContentResolver cr = Quick.getInstance().getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        if (cur.getCount() > 0) {
            while (cur.moveToNext()) {
                String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
                String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

                if (Integer.parseInt(cur.getString(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                    Cursor pCur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[]{id}, null);
                    while (pCur.moveToNext()) {
                        InputStream is = openPhoto(Long.valueOf(id));
                        Bitmap photo = BitmapFactory.decodeStream(is);
                        Bitmap circlePhoto = BitmapUtil.getRoundedCornerBitmap(photo);
                        // when clicked get into the detail information of the contact
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, String.valueOf(Long.valueOf(id)));
                        intent.setData(uri);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK );
                        String phone =   pCur.getString( pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        if (is != null)
                            contactSearchResultInfoList.add(new ContactSearchResultInfo(new BitmapDrawable(Quick.getInstance().getResources(), circlePhoto), name,phone, intent));
                        else
                            contactSearchResultInfoList.add(new ContactSearchResultInfo(null, name,phone, intent));
                    }
                    pCur.close();
                }
            }
        }
        for (ContactSearchResultInfo info : contactSearchResultInfoList) {
            String name = info.getTitle();
            String pinyin = PinyinUtil.chinese2pinyin(name);
            String pinyin_short = PinyinUtil.getFirstLetter(pinyin);
            String phone=info.getPhone();
            String searchable_name = name + pinyin + pinyin_short + phone;
            searchablestr.add(searchable_name);
        }
    }

    @Override
    public List<SearchResultInfo> search(String str) {
        List<SearchResultInfo> contactInfo = new ArrayList<>();
        for (String name : searchablestr) {
            if (name.toLowerCase().contains(str.toLowerCase())) {
                contactInfo.add(contactSearchResultInfoList.get(searchablestr.indexOf(name)));
            }
        }
        return contactInfo;
    }

    /**
     * Load the contact photo
     *
     * @param contactId
     * @return
     */
    private InputStream openPhoto(long contactId) {
        Uri contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId);
        Uri photoUri = Uri.withAppendedPath(contactUri, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY);
        Cursor cursor = Quick.getInstance().getContentResolver().query(photoUri, new String[]{ContactsContract.Contacts.Photo.PHOTO}, null, null, null);
        if (cursor == null) {
            return null;
        }
        try {
            if (cursor.moveToFirst()) {
                byte[] data = cursor.getBlob(0);
                if (data != null) {
                    return new ByteArrayInputStream(data);
                }
            }
        } finally {
            cursor.close();
        }
        return null;
    }
}
