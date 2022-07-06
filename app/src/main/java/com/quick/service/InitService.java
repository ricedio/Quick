package com.quick.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.quick.search.model.App;
import com.quick.search.model.Contact;
import com.quick.search.model.Plugin;
import com.quick.search.model.Sms;

public class InitService extends Service {


    @Override
    public void onCreate() {
        super.onCreate();
        /**
         * 初始化搜索
         */
        new Thread(new Runnable() {
            @Override
            public void run() {
                App.getInstance();
                Contact.getInstance();
                Sms.getInstance();
                Plugin.getInstance();
            }
        }).start();


    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
