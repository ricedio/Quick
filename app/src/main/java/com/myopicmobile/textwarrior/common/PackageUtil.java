package com.myopicmobile.textwarrior.common;

import android.content.Context;
import com.quick.BuildConfig;
import dalvik.system.DexFile;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import org.json.JSONException;
import org.json.JSONObject;

public class PackageUtil {
    private static JSONObject packages;
    private static HashMap<String, ArrayList<String>> classs = new HashMap<>();

    public static void load(Context context) {
        if (packages != null)
            return;

        try {
            InputStream stream = context.getAssets().open("android.json");
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
            StringBuilder stringBuilder = new StringBuilder(8196);
            String input;
            while ((input = reader.readLine()) != null) {
                stringBuilder.append(input);
            }
            stream.close();
            packages = new JSONObject(stringBuilder.toString());
            Enumeration<String> es = new DexFile(context.getPackageCodePath()).entries();
            while (es.hasMoreElements()){
                JSONObject json = packages;
                String[] ss = es.nextElement().replace("$",".").split("\\.");
                try {
                    for (String s : ss) {
                        if(s.length()<=2)
                            continue;
                        if (json.has(s)) {
                            json = json.getJSONObject(s);
                        } else {
                            JSONObject j = new JSONObject();
                            json.put(s, j);
                            json=j;
                        }
                    }
                } catch (Exception e){
                }
            }
            imports(packages, "");
        } catch (Exception e) {
            if(BuildConfig.DEBUG)
			    e.printStackTrace();
        }
    }

    public static void load(Context context, String path) {
        if (packages != null)
            return;
        try {
            InputStream stream =new FileInputStream(path);
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
            StringBuilder stringBuilder = new StringBuilder(8196);
            String input;
            while ((input = reader.readLine()) != null) {
                stringBuilder.append(input);
            }
            stream.close();
            packages = new JSONObject(stringBuilder.toString());
            Enumeration<String> es = new DexFile(context.getPackageCodePath()).entries();
            while (es.hasMoreElements()){
                JSONObject json = packages;
                String[] ss = es.nextElement().replace("$",".").split("\\.");
                try {
                    for (String s : ss) {
                        if(s.length()<=2)
                            continue;
                        if (json.has(s)) {
                            json = json.getJSONObject(s);
                        } else {
                            JSONObject j = new JSONObject();
                            json.put(s, j);
                            json=j;
                        }
                    }
                } catch (Exception e){

                }
            }
            imports(packages, "");
        } catch (Exception e) {
            load(context);
            if(BuildConfig.DEBUG)
			    e.printStackTrace();
        }
    }

    private static void imports(JSONObject json, String pkg) {
        Iterator<String> ks = json.keys();
        while (ks.hasNext()) {
            String k = ks.next();
            try {
                JSONObject j = json.getJSONObject(k);
                if (Character.isUpperCase(k.charAt(0))) {
                    ArrayList<String> cls = classs.get(k);
                    if (cls == null) {
                        cls = new ArrayList<>();
                        classs.put(k, cls);
                    }
                    cls.add(pkg + k);
                }
                imports(j, pkg + k + ".");
            } catch (JSONException e) {
                if(BuildConfig.DEBUG)
			    e.printStackTrace();
            }
        }
    }

    public static ArrayList<String> fix(String name) {
        return classs.get(name);
    }

    public static ArrayList<String> filter(String name) {
        ArrayList<String> ret = new ArrayList<>();
        if (packages == null)
            return ret;
        String[] ns = name.split("\\.");
        int len = ns.length - 1;
        if (name.endsWith(".")) {
            len = ns.length;
            name = "";
        } else {
            name = ns[ns.length - 1].toLowerCase();
        }

        JSONObject j = packages;
        for (int i = 0; i < len; i++) {
            try {
                j = j.getJSONObject(ns[i]);
            } catch (JSONException e) {
                /*if(BuildConfig.DEBUG)
			    e.printStackTrace();*/
                return ret;
            }
        }
        if (j == null)
            return ret;
        Iterator<String> ks = j.keys();
        while (ks.hasNext()) {
            String k = ks.next();
            if (k.toLowerCase().startsWith(name))
                ret.add(k + " :java");
        }

        return ret;
    }

}
