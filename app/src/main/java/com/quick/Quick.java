package com.quick;

import android.app.Application;
import android.content.Context;

import com.quick.utils.LuaUtil;

public class Quick extends Application {

	private static Context context;
	@Override
	public void onCreate() {
		super.onCreate();
		context=this;
		LuaUtil.rmDir(getExternalFilesDir("dexfiles"));
	}
    
	public static Context getInstance(){
		return context;
	}
    
    
}
