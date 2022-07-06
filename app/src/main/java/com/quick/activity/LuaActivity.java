package com.quick.activity;

import static android.os.Environment.getExternalStorageDirectory;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.myopicmobile.textwarrior.android.OnSelectionChangedListener;
import com.myopicmobile.textwarrior.common.AutoIndent;
import com.myopicmobile.textwarrior.common.PackageUtil;
import com.quick.BuildConfig;
import com.quick.common.LocaleComparator;
import com.quick.common.LuaBitmapDrawable;
import com.quick.content.FileProvider;
import com.quick.core.lib.file;
import com.quick.core.lib.json;
import com.quick.core.widget;
import com.quick.utils.JsonUtil;
import com.quick.utils.LuaUtil;
import com.quick.weiget.luaeditor.LuaEditor;

import org.json.JSONObject;
import org.luaj.Globals;
import org.luaj.LuaClosure;
import org.luaj.LuaError;
import org.luaj.LuaValue;
import org.luaj.Varargs;
import org.luaj.compiler.DumpState;
import org.luaj.lib.ResourceFinder;
import org.luaj.lib.jse.JsePlatform;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Created by nirenr on 2019/10/12.
 */

public class LuaActivity extends Activity implements ResourceFinder {
    private static String mHelp;
    private LuaEditor edit;
    private String path;
    private ArrayList<String> permissions;
    private File mDir;
    private String mName;
    private DisplayMetrics dm;
    private ArrayList<JsonUtil.HistoryData> history = new ArrayList<>();
    private String mHistoryPath;
    private TextView mTitle;
    private File mRootDir;
    private Globals mGlobals;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            InputStream f = getAssets().open("main.lua");
            if (f != null) {
                startActivity(new Intent(this, widget.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                finish();
                return;
            }
        } catch (Exception e) {
            if (BuildConfig.DEBUG)
                e.printStackTrace();
        }

        dm = getResources().getDisplayMetrics();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        initDir();
        mHistoryPath = new File(mDir, "history.json").getAbsolutePath();
        File f = new File(mDir, "android.json");
        history = JsonUtil.loadHistoryData(mHistoryPath);

        if (f.exists())
            PackageUtil.load(this, f.getAbsolutePath());
        else
            PackageUtil.load(this);
        edit = new LuaEditor(this);
        loadConfig();
        mGlobals = JsePlatform.standardGlobals();
        mGlobals.finder = this;
        mGlobals.load(new json());
        mGlobals.load(new file());
        LuaValue key = LuaValue.NIL;
        Varargs next;
        ArrayList<String> ks = new ArrayList<>();
        while (!(next = mGlobals.next(key)).isnil(1)) {
            key = next.arg1();
            LuaValue val = next.arg(2);
            if (val.istable()) {
                Varargs n;
                LuaValue k = LuaValue.NIL;
                ArrayList<String> vs = new ArrayList<>();
                while (!(n = val.next(k)).isnil(1)) {
                    k = n.arg1();
                    vs.add(k.tojstring());
                }
                String[] ss = new String[vs.size()];
                vs.toArray(ss);
                edit.addPackage(key.tojstring(), ss);
            }
            ks.add(key.tojstring());
        }
        Method[] ms = widget.class.getMethods();
        ArrayList<String> vs = new ArrayList<>();
        for (Method m : ms) {
            String n = m.getName();
            if(!vs.contains(n))
                vs.add(n);
        }
        String[] ss = new String[vs.size()];
        vs.toArray(ss);
        edit.addPackage("ui", ss);

        ss = new String[ks.size()];
        ks.toArray(ss);
        edit.addNames(ss);
        LinearLayout hList = new LinearLayout(this);
        String[] btn = {"(", ")", "[", "]", "{", "}", "\"", "=", ":", ".", ",", "_", "+", "-", "*", "/", "\\", "%", "#", "^", "$", "?", "&", "|", "<", ">", "~", ";", "'"};
        for (String s : btn) {
            final TextView view = new TextView(this);
            view.setText(s);
            view.setGravity(Gravity.CENTER);
            view.setPadding(0, 0, 0, 0);
            view.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
            view.setLayoutParams(new ViewGroup.LayoutParams(dp(36), dp(32)));
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    edit.paste(view.getText().toString());
                    final ColorStateList tc = view.getTextColors();
                    view.setBackgroundColor(tc.getDefaultColor());
                    view.setTextColor(0);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            view.setBackgroundColor(0);
                            view.setTextColor(tc);
                        }
                    }, 100);
                }
            });
            hList.addView(view);
        }

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        //edit.setNonPrintingCharVisibility(true);
        //标题栏
        LinearLayout mLayout = new LinearLayout(this);
        mLayout.setOrientation(LinearLayout.VERTICAL);
        mTitle = new TextView(this);
        //mTitle.setGravity(Gravity.CENTER);
        mTitle.setEllipsize(TextUtils.TruncateAt.START);
        mTitle.setSingleLine(true);
        //mLayout.addView();
        mLayout.addView(mTitle, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        //菜单栏
        LinearLayout mList = new LinearLayout(this);
        mList.addView(createButton("撤消", 1));
        mList.addView(createButton("重做", 2));
        View sBtn = createButton("搜索", 7);
        mList.addView(sBtn);
        mList.addView(createButton("最近", 12));
        mList.addView(createButton("打开", 4));
        mList.addView(createButton("保存", 6));
        mList.addView(createButton("格式化", 3));
        mList.addView(createButton("导入分析", 10));
        mList.addView(createButton("新建文件", 5));
        mList.addView(createButton("新建工程", 11));
        mList.addView(createButton("打包", 13));
        mList.addView(createButton("日志", 8));
        mList.addView(createButton("帮助", 9));
        HorizontalScrollView mScroll = new HorizontalScrollView(this);
        mScroll.addView(mList);
        mLayout.addView(mScroll, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        layout.addView(mLayout, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));


        final LinearLayout sList = new LinearLayout(this);
        final EditText sEdit = new androidx.appcompat.widget.AppCompatEditText(this) {
            @Override
            protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
                super.onTextChanged(text, start, lengthBefore, lengthAfter);
               // edit.findNext(text.toString());
            }
        };
        sList.addView(sEdit, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        sList.addView(createButton("▼", 28));
        sList.addView(createButton("▲", 29));
        sList.setVisibility(View.GONE);
        layout.addView(sList, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        sBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sList.getVisibility() == View.GONE) {
                    sList.setVisibility(View.VISIBLE);
                    sEdit.setText(edit.getSelectedText());
                } else {
                    sList.setVisibility(View.GONE);
                }
            }
        });
        layout.addView(edit, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 1));

        //底部工具栏
        LinearLayout hLayout = new LinearLayout(this);
        hLayout.addView(createButton("运行", 0));

        //剪切板栏
        final LinearLayout cList = new LinearLayout(this);
        cList.addView(createButton("全选", 20));
        cList.addView(createButton("剪切", 21));
        cList.addView(createButton("复制", 22));
        cList.addView(createButton("粘贴", 23));
        cList.addView(createButton("◀", 24));
        cList.addView(createButton("▶", 26));
        cList.addView(createButton("▲", 25));
        cList.addView(createButton("▼", 27));
        cList.setVisibility(View.GONE);
        HorizontalScrollView cScroll = new HorizontalScrollView(this);
        cScroll.addView(cList);
        hLayout.addView(cScroll);
        edit.setOnSelectionChangedListener(new OnSelectionChangedListener() {
            @Override
            public void onSelectionChanged(boolean active, int selStart, int selEnd) {
                if (active)
                    cList.setVisibility(View.VISIBLE);
                else
                    cList.setVisibility(View.GONE);
            }
        });

        //符号栏
        HorizontalScrollView hScroll = new HorizontalScrollView(this);
        hScroll.addView(hList);
        hLayout.addView(hScroll, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        layout.addView(hLayout, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        // layout.addView(hScroll, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        setContentView(layout);
        if (Build.VERSION.SDK_INT >= 23) {
            try {
                permissions = new ArrayList<String>();
                String[] ps2 = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_PERMISSIONS).requestedPermissions;
                for (String p : ps2) {
                    try {
                        checkPermission(p);
                    } catch (Exception e) {
                        if (BuildConfig.DEBUG)
                            e.printStackTrace();
                    }
                }
                if (!permissions.isEmpty()) {
                    String[] ps = new String[permissions.size()];
                    permissions.toArray(ps);
                    //requestPermissions(ps, 0);
                    return;
                }
            } catch (Exception e) {
                if (BuildConfig.DEBUG)
                    e.printStackTrace();
            }
        }
        if (history.size() > 0 && readHistory(0))
            return;
        readFile();
    }

    private View createButton(String s, int id) {
        final TextView view = new TextView(this);
        view.setId(id);
        view.setText(s);
        view.setGravity(Gravity.CENTER);
        view.setPadding(dp(8), 0, dp(8), 0);
        view.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, dp(32)));
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onOptionsItemSelected(v);
                final ColorStateList tc = view.getTextColors();
                view.setBackgroundColor(tc.getDefaultColor());
                view.setTextColor(0);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        view.setBackgroundColor(0);
                        view.setTextColor(tc);
                    }
                }, 100);
            }
        });
        return view;
    }

    @SuppressLint("WrongConstant")
    private int dp(float n) {
        // TODO: Implement this method
        return (int) TypedValue.applyDimension(1, n, dm);
    }

    @Override
    protected void onStop() {
        super.onStop();
        save();
    }

    private void save() {
        try {
            if (edit.isEdited()) {
                edit.save();
            }
            JsonUtil.HistoryData c = null;
            for (JsonUtil.HistoryData data : history) {
                if (data.getPath().equals(path)) {
                    c = data;
                    break;
                }
            }
            if (c != null)
                history.remove(c);
            history.add(0, new JsonUtil.HistoryData(path, edit.getCaretPosition()));
            JsonUtil.saveHistoryData(mHistoryPath, history);
        } catch (IOException e) {
            if (BuildConfig.DEBUG)
                e.printStackTrace();
        }
    }

    private void loadConfig() {
        try {
            File path = new File(mDir, "config.json");
            if (!path.exists())
                return;
            InputStream stream = new FileInputStream(path);
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
            StringBuilder stringBuilder = new StringBuilder(8196);
            String input;
            while ((input = reader.readLine()) != null) {
                stringBuilder.append(input);
            }
            stream.close();
            JSONObject colors = new JSONObject(stringBuilder.toString());
            if (colors.has("dark")) {
                if (colors.optBoolean("dark")) {
                    setTheme(android.R.style.Theme_DeviceDefault_NoActionBar);
                    edit.setDark(true);
                } else {
                    setTheme(android.R.style.Theme_DeviceDefault_Light_NoActionBar);
                    edit.setDark(false);
                }
            }

            if (colors.has("wrap"))
                edit.setWordWrap(colors.optBoolean("wrap"));

            if (colors.has("text"))
                edit.setTextColor(Color.parseColor(colors.optString("text")));
            if (colors.has("highlight"))
                edit.setTextHighlightColor(Color.parseColor(colors.optString("Highlight")));
            if (colors.has("background")) {
                String bg = colors.optString("background");
                if (new File(mDir, bg).exists())
                    edit.setBackground(new LuaBitmapDrawable(this, new File(mDir, bg).getAbsolutePath()));
                else
                    edit.setBackgroundColor(Color.parseColor(bg));
            }

            if (colors.has("line"))
                edit.setLineColor(Color.parseColor(colors.optString("line")));
            if (colors.has("keyword"))
                edit.setKeywordColor(Color.parseColor(colors.optString("keyword")));
            if (colors.has("package"))
                edit.setBasewordColor(Color.parseColor(colors.optString("package")));
            if (colors.has("number"))
                edit.setUserwordColor(Color.parseColor(colors.optString("number")));
            if (colors.has("global"))
                edit.setGlobalColor(Color.parseColor(colors.optString("global")));
            if (colors.has("local"))
                edit.setLocalColor(Color.parseColor(colors.optString("local")));
            if (colors.has("upval"))
                edit.setUpvalColor(Color.parseColor(colors.optString("upval")));
            if (colors.has("comment"))
                edit.setCommentColor(Color.parseColor(colors.optString("comment")));
            if (colors.has("string"))
                edit.setStringColor(Color.parseColor(colors.optString("string")));
            if (colors.has("panel")) {
                colors = colors.getJSONObject("panel");
                if (colors.has("background")) {
                    String bg = colors.optString("background");
                    if (new File(mDir, bg).exists())
                        edit.setPanelBackground(new LuaBitmapDrawable(this, new File(mDir, bg).getAbsolutePath()));
                    else
                        edit.setPanelBackgroundColor(Color.parseColor(bg));
                }
                //    edit.setPanelBackgroundColor(Color.parseColor(colors.optString("background")));
                if (colors.has("text"))
                    edit.setPanelTextColor(Color.parseColor(colors.optString("text")));
            } /*else {
                edit.setPanelTextColor(edit.getColorScheme().getColor(ColorScheme.Colorable.FOREGROUND));
                edit.setPanelBackgroundColor(edit.getColorScheme().getColor(ColorScheme.Colorable.BACKGROUND));
            }*/

        } catch (Exception e) {
            if (BuildConfig.DEBUG)
                e.printStackTrace();
        }
    }


    private void initDir() {
        mDir = new File(getExternalStorageDirectory(), "LuaJ");
        mRootDir = mDir;
        if (!mDir.exists())
            mDir.mkdirs();
    }

    private void checkPermission(String permission) {
        if (checkCallingOrSelfPermission(permission)
                != PackageManager.PERMISSION_GRANTED) {
            permissions.add(permission);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        readFile();
    }

    /*
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        readFile();
    }
     */

    private void readFile() {
        try {
            path = new File(mDir, "main.lua").getAbsolutePath();
            edit.open(path);
            mName = "main.lua";
            //getActionBar().setSubtitle("main.lua");
            mTitle.setText(path);
        } catch (IOException e) {
            Toast.makeText(this, "打开出错：" + e.toString(), Toast.LENGTH_SHORT).show();
            if (BuildConfig.DEBUG)
                e.printStackTrace();
        }
    }

    private boolean readHistory(int i) {
        try {
            path = history.get(i).getPath();
            edit.open(path);
            mName = new File(path).getName();
            mDir = new File(path).getParentFile();
            edit.setSelection(history.get(i).getIdx());
            //getActionBar().setSubtitle(mName);
            mTitle.setText(path);
            return true;
        } catch (IOException e) {
            if (BuildConfig.DEBUG)
                e.printStackTrace();
            return false;
        }
    }

    private void readFile(String p) {
        try {
            path = new File(mDir, p).getAbsolutePath();
            edit.open(path);
            mName = p;
            for (JsonUtil.HistoryData data : history) {
                if (data.getPath().equals(path))
                    edit.setSelection(data.getIdx());
            }
            //getActionBar().setSubtitle(p);
            mTitle.setText(path);
            save();
        } catch (IOException e) {
            Toast.makeText(this, "打开出错：" + e.toString(), Toast.LENGTH_SHORT).show();
            if (BuildConfig.DEBUG)
                e.printStackTrace();
        }
    }

    public void onOptionsItemSelected(View item) {
        switch (item.getId()) {
            case 0:
                try {
                    edit.save();
                    startActivity(new Intent(this, widget.class).setData(Uri.fromFile(mDir)));
                } catch (IOException e) {
                    Toast.makeText(this, "保存出错：" + e.toString(), Toast.LENGTH_SHORT).show();
                    if (BuildConfig.DEBUG)
                        e.printStackTrace();
                }
                break;
            case 1:
                edit.undo();
                break;
            case 2:
                edit.redo();
                break;
            case 3:
                edit.format();
                break;
            case 7:
                edit.search();
                break;
            case 12:
                String[] hs = new String[history.size()];
                for (int i = 0; i < history.size(); i++) {
                    hs[i] = history.get(i).getPath();
                }
                new AlertDialog.Builder(this).setTitle("最近打开")
                        .setItems(hs, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                save();
                                if (readHistory(which)) {
                                    history.remove(which);
                                    save();
                                } else {
                                    history.remove(which);
                                }
                            }
                        })
                        .setNegativeButton("取消", null)
                        .create()
                        .show();
                break;
            case 8:
                String[] logs = new String[widget.logs.size()];
                widget.logs.toArray(logs);
                new AlertDialog.Builder(this).setTitle("日志")
                        .setItems(logs, null)
                        .setPositiveButton("确定", null)
                        .create()
                        .show();
                break;
            case 6:
                try {
                    edit.save();
                    Toast.makeText(this, "已保存", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    Toast.makeText(this, "保存出错：" + e.toString(), Toast.LENGTH_SHORT).show();
                    if (BuildConfig.DEBUG)
                        e.printStackTrace();
                }
                break;
            case 4:
                save();
                openFile(mDir);
                break;
            case 5:
                save();
				/*
                new EditDialog(this, "输入文件名", "", new EditDialog.EditDialogCallback() {
                    @Override
                    public void onCallback(String text) {
                        if (TextUtils.isEmpty(text))
                            return;
                        if (!text.contains("."))
                            text = text + ".lua";
                        readFile(text);
                    }
                }).show();
				*/
                break;
            case 11:
                save();
               /* new EditDialog(this, "输入工程名", "", new EditDialog.EditDialogCallback() {
                    @Override
                    public void onCallback(String text) {
                        if (TextUtils.isEmpty(text))
                            return;
                        File d = new File(mDir, text);
                        if (!d.exists() && !d.mkdirs()) {
                            Toast.makeText(LuaActivity.this, "创建出错", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        mDir = d;
                        readFile("main.lua");
                    }
                }).show();
				*/
                break;
            case 9:
                new AlertDialog.Builder(this)
                        .setTitle("LuaJ++")
                        .setMessage(load())
                        .setPositiveButton("确定", null)
                        .create()
                        .show();
                break;
            case 10:
                final String[] cls = AutoIndent.fix(edit.getText());
                Arrays.sort(cls, new LocaleComparator());
                final boolean[] ss = new boolean[cls.length];
                final StringBuilder buf = new StringBuilder();
                new AlertDialog.Builder(this)
                        .setTitle("LuaJ++")
                        .setMultiChoiceItems(cls, ss, new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                                ss[which] = isChecked;
                            }
                        })
                        .setPositiveButton("复制", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                for (int i = 0; i < cls.length; i++) {
                                    if (ss[i])
                                        buf.append("import \"").append(cls[i]).append("\"\n");
                                }
                                ClipboardManager clp = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                                clp.setText(buf.toString());
                            }
                        })
                        .setNegativeButton("取消", null)
                        .create()
                        .show();
                break;
            case 13:
                bin();
                break;
            case 20:
                edit.selectAll();
                break;
            case 21:
                edit.cut();
                break;
            case 22:
                edit.copy();
                break;
            case 23:
                edit.paste();
                break;
            case 24:
                edit.moveCaretLeft();
                break;
            case 25:
                edit.moveCaretUp();
                break;
            case 26:
                edit.moveCaretRight();
                break;
            case 27:
                edit.moveCaretDown();
                break;
            case 28:
                edit.findNext();
                break;
            case 29:
                edit.findBack();
                break;

        }
    }

    private void openFile(final File dir) {
        File[] ls = dir.listFiles();
        ArrayList<String> ds = new ArrayList<>();
        ArrayList<String> fs = new ArrayList<>();
        for (File l : ls) {
            if (l.isDirectory())
                ds.add(l.getName());
            else
                fs.add(l.getName());
        }
        Collections.sort(ds, new LocaleComparator());
        Collections.sort(fs, new LocaleComparator());
        ds.add(0, "..");
        ds.addAll(fs);
        final String[] list = new String[ds.size()];
        ds.toArray(list);
        final AlertDialog dlg = new AlertDialog.Builder(this)
                .setTitle("打开 " + dir.getAbsolutePath())
                .setItems(list, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.i("luaj", "onClick: " + list[which]);
                        if (which == 0) {
                            if (dir.getParentFile() != null)
                                openFile(dir.getParentFile());
                        } else if (new File(dir, list[which]).isDirectory()) {
                            openFile(new File(dir, list[which]));
                        } else {
                            LuaActivity.this.mDir = dir;
                            readFile(list[which]);
                        }
                    }
                })
                .setNegativeButton("取消", null)
                .create();
        dlg.show();
        dlg.getListView().setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                dlg.dismiss();
                new AlertDialog.Builder(LuaActivity.this)
                        .setItems(new String[]{
                                "删除",
                                "重命名",
                                "取消"
                        }, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case 0:
                                        new File(dir, list[position]).delete();
                                        if (list[position].equals(mName))
                                            readFile();
                                        break;
                                    case 1:
                                       /* new EditDialog(LuaActivity.this, "输入文件名", list[position], new EditDialog.EditDialogCallback() {
                                            @Override
                                            public void onCallback(String text) {
                                                if (TextUtils.isEmpty(text))
                                                    return;
                                                new File(dir, list[position]).renameTo(new File(dir, text));
                                                if (list[position].equals(mName))
                                                    readFile(list[position]);
                                            }
                                        }).show();
										*/
                                        break;
                                }
                                dialog.dismiss();
                            }
                        })
                        .create()
                        .show();

                return true;
            }
        });
    }

    public String load() {
        if (mHelp != null)
            return mHelp;
        try {
            InputStream stream=getAssets().open("help.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
            StringBuilder stringBuilder = new StringBuilder(8196);
            String input;
            while ((input = reader.readLine()) != null) {
                stringBuilder.append(input).append("\n");
            }
            stream.close();
            mHelp = stringBuilder.toString();
        } catch (Exception e) {
            if (BuildConfig.DEBUG)
                e.printStackTrace();
        }
        //Log.i("luaj", "load filter: " + packages);
        return mHelp;
    }

    public void bin() {
        GridLayout layout = new GridLayout(this);
        layout.setColumnCount(2);
        TextView n = new TextView(this);
        n.setText("应用名");
        layout.addView(n);
        EditText ne = new EditText(this);
        ne.setText("demo");
        layout.addView(ne, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        TextView p = new TextView(this);
        p.setText("包名");
        layout.addView(p);
        EditText pe = new EditText(this);
        pe.setText("com.luaj.demo");
        layout.addView(pe, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        TextView v = new TextView(this);
        v.setText("版本号");
        layout.addView(v);
        EditText ve = new EditText(this);
        ve.setText("1.0");
        layout.addView(ve, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        new AlertDialog.Builder(this)
                .setTitle("打包")
                .setView(layout)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //bin(ne.getText().toString(), pe.getText().toString(), ve.getText().toString());
						Toast.makeText(LuaActivity.this,"暂不支持打包",Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("取消", null)
                .create()
                .show();
    }


    private String getType(File file) {
        int lastDot = file.getName().lastIndexOf(46);
        if (lastDot >= 0) {
            String extension = file.getName().substring(lastDot + 1);
            String mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
            if (mime != null) {
                return mime;
            }
        }
        return "application/octet-stream";
    }

    public Uri getUriForFile(File path) {
        return FileProvider.getUriForFile(this, getPackageName(), path);
    }

    public void installApk(String path) {
        Intent share = new Intent(Intent.ACTION_VIEW);
        File file = new File(path);
        share.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        share.setDataAndType(getUriForFile(file), getType(file));
        share.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(share);
    }


    private void addZip(ZipOutputStream zip, File dir, String root) {
        Log.i("luaj", "addZip: " + root + ";" + dir);
        if(dir.getName().startsWith("."))
            return;
        String name = root + "/" + dir.getName();
        if (name.endsWith(".apk"))
            return;
        if (dir.isDirectory()) {
            File[] fs = dir.listFiles();
            for (File f : fs) {
                addZip(zip, f, name);
            }
        } else {
            try {
                zip.putNextEntry(new ZipEntry(name));
            } catch (IOException e) {
                throw new LuaError(e);
            }
            if(name.endsWith(".lua")) {
                LuaValue args = mGlobals.loadfile(dir.getAbsolutePath());
                LuaValue f = args.checkfunction(1);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                try {
                    DumpState.dump(((LuaClosure) f).p, baos, true);
                    zip.write(baos.toByteArray());
                    zip.flush();
                } catch (IOException e) {
                    throw new LuaError(e);
                }
            } else {
                try {
                    byte[] b = LuaUtil.readAll(dir.getAbsolutePath());
                    zip.write(b, 0, b.length);
                    zip.flush();
                } catch (IOException e) {
                    throw new LuaError(e);
                }
            }
        }
    }

    @Override
    public InputStream findResource(String name) {
        try {
            if (new File(name).exists())
                return new FileInputStream(name);
        } catch (Exception e) {
            try {
                return new FileInputStream(new File(mDir, name));
            } catch (Exception e2) {
                return null;
            }
        }
        return null;
    }

    @Override
    public String findFile(String filename) {
        if(filename.startsWith("/"))
            return filename;
        return new File(mDir, filename).getAbsolutePath();
    }
}
