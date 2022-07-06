package com.quick.core;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.quick.BuildConfig;
import com.quick.common.ArrayListAdapter;
import com.quick.common.LuaContext;
import com.quick.common.LuaGcable;
import com.quick.core.lib.file;
import com.quick.core.lib.http;
import com.quick.core.lib.json;

import org.luaj.Globals;
import org.luaj.LuaString;
import org.luaj.LuaTable;
import org.luaj.LuaValue;
import org.luaj.Varargs;
import org.luaj.lib.ResourceFinder;
import org.luaj.lib.VarArgFunction;
import org.luaj.lib.jse.CoerceJavaToLua;
import org.luaj.lib.jse.JavaPackage;
import org.luaj.lib.jse.JsePlatform;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


public class widget extends Activity implements ResourceFinder, LuaContext {
    private final static String ARG = "arg";
    private final static String DATA = "data";
    private final static String NAME = "name";

    private Globals globals;
    private StringBuilder toastbuilder = new StringBuilder();
    private Toast toast;
    private long lastShow;
    public static ArrayList<String> logs = new ArrayList<>();
    private ArrayListAdapter<String> adapter;
    private String mExtDir;
    private int mWidth;
    private int mHeight;
    private static final HashMap sGlobalData = new HashMap();
    private boolean debug;
    private String mDir;
    private String luaDir;
    private String luaFile = "main.lua";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        super.onCreate(savedInstanceState);
        Uri d = getIntent().getData();
        //Log.i("luaj", "onCreate: "+d);
        mDir = getLuaExtDir();
        if (d != null) {
            String p = d.getPath();
            if (!TextUtils.isEmpty(p)) {
                File f = new File(p);
                if (f.isFile()) {
                    p = f.getParent();
                    luaFile = f.getName();
                }
                luaDir = p;
                mDir = p;
            }
        }
		
        initSize();
        globals = JsePlatform.standardGlobals();
        globals.finder = this;
        ListView list = new ListView(this);
        list.setFastScrollEnabled(true);
        list.setFastScrollAlwaysVisible(true);
        setContentView(list);
        adapter = new ArrayListAdapter<String>(this, android.R.layout.simple_list_item_1) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView view = (TextView) super.getView(position, convertView, parent);
                if (convertView == null)
                    view.setTextIsSelectable(true);
                return view;
            }
        };
        list.setAdapter(adapter);
        try {
            LuaValue activity = CoerceJavaToLua.coerce(this);
            globals.set("print", new VarArgFunction() {
					public Varargs invoke(Varargs args) {
						LuaValue tostring = globals.get("tostring");
						StringBuilder buf = new StringBuilder();
						for (int i = 1, n = args.narg(); i <= n; i++) {
							LuaString s = tostring.call(args.arg(i)).strvalue();
							buf.append((s.tojstring())).append("    ");
						}
						String ss = buf.toString();
						sendMsg(ss);
						return NONE;
					}
				});
            globals.set("printf", new VarArgFunction() {
					public Varargs invoke(Varargs args) {
						String ss = globals.get("string").get("format").invoke(args).tojstring();
						sendMsg(ss);
						return NONE;
					}
				});
            globals.set("task", new VarArgFunction() {
					@SuppressLint("StaticFieldLeak")
					@Override
					public Varargs invoke(final Varargs args) {
						final int n = args.narg();
						int i = n - 2;
						i = i >= 0 ? i : 0;
						final LuaValue[] as = new LuaValue[i];
						final LuaValue func = args.arg1();
						for (int i1 = 0; i1 < n - 2; i1++) {
							as[i1] = args.arg(i1 + 2);
						}
						new AsyncTask<Varargs, Varargs, Varargs>() {
							@Override
							protected Varargs doInBackground(Varargs... objects) {
								if (func.isnumber()) {
									try {
										Thread.sleep(func.tolong());
									} catch (Exception e) {
										if (BuildConfig.DEBUG)
											e.printStackTrace();
									}
									return LuaValue.varargsOf(as);
								}
								try {
									return func.invoke(as);
								} catch (Exception e) {
									sendError("task", e);
									return LuaValue.varargsOf(new LuaValue[]{LuaValue.NIL, LuaValue.valueOf(e.toString())});
								}
							}

							@Override
							protected void onPostExecute(Varargs varargs) {
								if (n > 1) {
									args.arg(n).invoke(varargs);
								}
							}
						}.execute();
						return NONE;
					}
				});
            globals.set("ui", activity);

            globals.load(new json());
            globals.load(new file());
            globals.jset("http", http.class);
            globals.set("android", new JavaPackage("android"));
            globals.set("java", new JavaPackage("java"));
            globals.set("com", new JavaPackage("com"));
            globals.set("org", new JavaPackage("org"));



            globals.loadfile(luaFile).jcall((Object[]) getIntent().getSerializableExtra(ARG));
        } catch (final Exception e) {
            sendError("Error", e);
            if (BuildConfig.DEBUG)
                e.printStackTrace();
        }
    }

    public void layout(LuaTable view) {
		LuaTable ui=new LuaTable();
        setContentView(new viewInflater(this).load(view,ui).touserdata(View.class));	
		globals.set("id",ui);
    }

	public void Title(String title){
		setTitle(title);
	}


    public void setDebug(boolean bool) {
        debug = bool;
    }

    private void initSize() {
        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        mWidth = outMetrics.widthPixels;
        mHeight = outMetrics.heightPixels;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        runFunc("onCreateOptionsMenu", menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        runFunc("onOptionsItemSelected", item);
        return super.onOptionsItemSelected(item);
    }

    public Object runFunc(String name, Object... arg) {
        try {
            LuaValue func = globals.get(name);
            if (func.isfunction())
                return func.jcall(arg);
        } catch (Exception e) {
            sendError(name, e);
        }
        return null;
    }

    @Override
    public InputStream findResource(String name) {
        try {
            if (new File(name).exists())
                return new FileInputStream(name);
        } catch (Exception e) {

        }
        try {
            return getAssets().open(name);
        } catch (Exception ioe) {
            try {
                return new FileInputStream(new File(getLuaPath(name)));
            } catch (Exception e) {
                /*if (BuildConfig.DEBUG)
				 e.printStackTrace();*/
            }
            return null;
        }
    }

    @Override
    public String findFile(String filename) {
        if(filename.startsWith("/"))
            return filename;
        return getLuaPath(filename);
    }

    //显示toast
    @SuppressLint("ShowToast")
    public void showToast(String text) {
        if (!debug)
            return;
        long now = System.currentTimeMillis();
        if (toast == null || now - lastShow > 1000) {
            toastbuilder.setLength(0);
            toast = Toast.makeText(this, text, Toast.LENGTH_LONG);
            toastbuilder.append(text);
            toast.show();
        } else {
            toastbuilder.append("\n");
            toastbuilder.append(text);
            toast.setText(toastbuilder.toString());
            toast.setDuration(Toast.LENGTH_LONG);
        }
        lastShow = now;
    }

    @Override
    public ArrayList<ClassLoader> getClassLoaders() {
        return null;
    }

    @Override
    public void call(String func, Object... args) {
        globals.get(func).jcall(args);
    }

    @Override
    public void set(String name, Object value) {
        globals.jset(name, value);
    }

    @Override
    public String getLuaPath() {
        return null;
    }

    @Override
    public String getLuaPath(String path) {
        return new File(getLuaDir(), path).getAbsolutePath();
    }

    @Override
    public String getLuaPath(String dir, String name) {
        return new File(getLuaDir(dir), name).getAbsolutePath();
    }

    @Override
    public String getLuaDir() {
        return mDir;
    }

    @Override
    public String getLuaDir(String dir) {
        return getLuaExtDir(dir);
    }

    @Override
    public String getLuaExtDir() {
        if (mExtDir != null)
            return mExtDir;
        File d = new File(Environment.getExternalStorageDirectory(), "Luaj");
        if (!d.exists())
            d.mkdirs();
        mExtDir = d.getAbsolutePath();
        return mExtDir;
    }

    @Override
    public String getLuaExtDir(String dir) {
        File d = new File(getLuaExtDir(), dir);
        if (!d.exists())
            d.mkdirs();
        return d.getAbsolutePath();
    }

    @Override
    public void setLuaExtDir(String dir) {
        mExtDir = dir;
    }

    @Override
    public String getLuaExtPath(String path) {
        return new File(getLuaExtDir(), path).getAbsolutePath();
    }

    @Override
    public String getLuaExtPath(String dir, String name) {
        return new File(getLuaExtDir(dir), name).getAbsolutePath();
    }

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public Globals getLuaState() {
        return globals;
    }

    @Override
    public Object doFile(String path, Object... arg) {
        return globals.loadfile(path).jcall(arg);
    }

    @Override
    public void sendMsg(final String msg) {
        runOnUiThread(new Runnable() {
				@Override
				public void run() {
					showToast(msg);
					adapter.add(msg);
					logs.add(msg);
				}
			});
        //Log.i("luaj", "sendMsg: " + msg);
    }

    @Override
    public void sendError(String title, Exception msg) {
        sendMsg(title + ": " + msg.getMessage());
        logs.add(title + ": " + msg.toString());
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        // TODO: Implement this method
        super.onConfigurationChanged(newConfig);
        initSize();
    }

    public int getWidth() {
        return mWidth;
    }

    public int getHeight() {
        return mHeight;
    }

    @Override
    public Map getGlobalData() {
        return sGlobalData;
    }

    @Override
    public Object getSharedData(String key) {
        return PreferenceManager.getDefaultSharedPreferences(this).getAll().get(key);
    }

    @Override
    public Object getSharedData(String key, Object def) {
        Object ret = PreferenceManager.getDefaultSharedPreferences(this).getAll().get(key);
        if (ret != null)
            return ret;
        return def;
    }

    @Override
    public boolean setSharedData(String key, Object value) {
        SharedPreferences.Editor edit = PreferenceManager.getDefaultSharedPreferences(this).edit();
        if (value == null)
            edit.remove(key);
        else if (value instanceof String)
            edit.putString(key, value.toString());
        else if (value instanceof Long)
            edit.putLong(key, (Long) value);
        else if (value instanceof Integer)
            edit.putInt(key, (Integer) value);
        else if (value instanceof Float)
            edit.putFloat(key, (Float) value);
        else if (value instanceof Set)
            edit.putStringSet(key, (Set<String>) value);
        else if (value instanceof Boolean)
            edit.putBoolean(key, (Boolean) value);
        else
            return false;
        return edit.commit();
    }

    @Override
    public void regGc(LuaGcable obj) {

    }

    public void newActivity(String path, boolean newDocument) throws FileNotFoundException {
        newActivity(1, path, null, newDocument);
    }

    public void newActivity(String path, Object[] arg, boolean newDocument) throws FileNotFoundException {
        newActivity(1, path, arg, newDocument);
    }

    public void newActivity(int req, String path, boolean newDocument) throws FileNotFoundException {
        newActivity(req, path, null, newDocument);
    }

    public void newActivity(String path) throws FileNotFoundException {
        newActivity(1, path, new Object[0]);
    }

    public void newActivity(String path, Object[] arg) throws FileNotFoundException {
        newActivity(1, path, arg);
    }

    public void newActivity(int req, String path) throws FileNotFoundException {
        newActivity(req, path, new Object[0]);
    }

    public void newActivity(int req, String path, Object[] arg) throws FileNotFoundException {
        newActivity(req, path, arg, false);
    }

    public void newActivity(int req, String path, Object[] arg, boolean newDocument) throws FileNotFoundException {
        //Log.i("luaj", "newActivity: "+path+ Arrays.toString(arg));
        Intent intent = new Intent(this, widget.class);
        if (newDocument)
            intent = new Intent(this, widgetX.class);

        intent.putExtra(NAME, path);
        if (path.charAt(0) != '/' && luaDir != null)
            path = luaDir + "/" + path;
        File f = new File(path);
        if (f.isDirectory() && new File(path + "/main.lua").exists())
            path += "/main.lua";
        else if ((f.isDirectory() || !f.exists()) && !path.endsWith(".lua"))
            path += ".lua";
        if (!new File(path).exists())
            throw new FileNotFoundException(path);

        if (newDocument) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
                intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
            }
        }

        intent.setData(Uri.parse("file://" + path));

        if (arg != null)
            intent.putExtra(ARG, arg);
        if (newDocument)
            startActivity(intent);
        else
            startActivityForResult(intent, req);
        //overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }

    public void newActivity(String path, int in, int out, boolean newDocument) throws FileNotFoundException {
        newActivity(1, path, in, out, null, newDocument);
    }

    public void newActivity(String path, int in, int out, Object[] arg, boolean newDocument) throws FileNotFoundException {
        newActivity(1, path, in, out, arg, newDocument);
    }

    public void newActivity(int req, String path, int in, int out, boolean newDocument) throws FileNotFoundException {
        newActivity(req, path, in, out, null, newDocument);
    }

    public void newActivity(String path, int in, int out) throws FileNotFoundException {
        newActivity(1, path, in, out, new Object[0]);
    }

    public void newActivity(String path, int in, int out, Object[] arg) throws FileNotFoundException {
        newActivity(1, path, in, out, arg);
    }

    public void newActivity(int req, String path, int in, int out) throws FileNotFoundException {
        newActivity(req, path, in, out, new Object[0]);
    }

    public void newActivity(int req, String path, int in, int out, Object[] arg) throws FileNotFoundException {
        newActivity(req, path, in, out, arg, false);
    }

    public void newActivity(int req, String path, int in, int out, Object[] arg, boolean newDocument) throws FileNotFoundException {
        Intent intent = new Intent(this, widget.class);
        if (newDocument)
            intent = new Intent(this, widgetX.class);
        intent.putExtra(NAME, path);
        if (path.charAt(0) != '/' && luaDir != null)
            path = luaDir + "/" + path;
        File f = new File(path);
        if (f.isDirectory() && new File(path + "/main.lua").exists())
            path += "/main.lua";
        else if ((f.isDirectory() || !f.exists()) && !path.endsWith(".lua"))
            path += ".lua";
        if (!new File(path).exists())
            throw new FileNotFoundException(path);

        intent.setData(Uri.parse("file://" + path));

        if (newDocument) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
                intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
            }
        }


        if (arg != null)
            intent.putExtra(ARG, arg);
        if (newDocument)
            startActivity(intent);
        else
            startActivityForResult(intent, req);
        overridePendingTransition(in, out);

    }

    public void finish(boolean finishTask) {
        if (!finishTask) {
            super.finish();
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Intent intent = getIntent();
            if (intent != null && (intent.getFlags() & Intent.FLAG_ACTIVITY_NEW_DOCUMENT) != 0)
                finishAndRemoveTask();
            else
                super.finish();
        } else {
            super.finish();
        }
    }

}


