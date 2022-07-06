package com.quick.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.quick.content.Contants;
import com.quick.service.InitService;
import com.quick.utils.MySharedPreferences;

public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent=new Intent(this,InitService.class);
        startService(intent);
        //创建对象
        final MySharedPreferences.SharedPreferencesUtil sharedPreferencesUtil = MySharedPreferences.SharedPreferencesUtil.getInstance(this);
//获取存储的判断是否是第一次启动，默认为true
        boolean isFirst = (boolean) sharedPreferencesUtil.getData(Contants.IS_FIRST_START, true);
//你可以在这里做你想要的效果
        if (isFirst) {
            sharedPreferencesUtil.saveData(Contants.IS_FIRST_START, false);
            //"第一次安装app跳转到导航页");
        } else {
            Intent i=new Intent(this,MainActivity.class);
            startActivity(i);
            finish();
        }
    }
}
