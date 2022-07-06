package com.quick.core.lib;

import com.quick.BuildConfig;
import com.quick.utils.LuaUtil;

import org.luaj.Globals;
import org.luaj.LuaString;
import org.luaj.LuaTable;
import org.luaj.LuaValue;
import org.luaj.lib.OneArgFunction;
import org.luaj.lib.TwoArgFunction;
import org.luaj.lib.jse.LuajavaLib;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by nirenr on 2020/1/16.
 */
public class file extends TwoArgFunction {
    private Globals globals;

    public static String readAll(String path) {
        try {
            return new String(LuaUtil.readAll(path));
        } catch (IOException e) {
            if(BuildConfig.DEBUG)
			    e.printStackTrace();
        }
        return "";
    }

    public static String[] list(String path) {
        return new File(path).list();
    }

    public static boolean exists(String path) {
        return new File(path).exists();
    }

    public static boolean save(String path, LuaString text) {
        try {
            //BufferedWriter buf = new BufferedWriter(new FileWriter(path));
            FileOutputStream  buf=new FileOutputStream(path);
            buf.write(text.m_bytes,text.m_offset, text.m_length);
            buf.close();
            return true;
        } catch (Exception e) {
            if(BuildConfig.DEBUG)
			    e.printStackTrace();
        }
        return false;
    }


    public LuaValue call(LuaValue modname, LuaValue env) {
        globals = env.checkglobals();
        LuaTable file = new LuaTable();
        file.set("readall", new readall());
        file.set("list", new list());
        file.set("exists", new exists());
        file.set("save", new save());
        env.set("file", file);
        if (!env.get("package").isnil()) env.get("package").get("loaded").set("file", file);
        return NIL;
    }

    private class readall extends OneArgFunction {
        @Override
        public LuaValue call(LuaValue arg) {
            try {
                return LuaString.valueOf(LuaUtil.readAll(globals.finder.findFile(arg.tojstring())));
            } catch (Exception e) {
                return NIL;
            }
        }
    }

    private class list extends OneArgFunction {
        @Override
        public LuaValue call(LuaValue arg) {
            return LuajavaLib.asTable(list(globals.finder.findFile(arg.tojstring())));
        }
    }

    private class exists extends OneArgFunction {
        @Override
        public LuaValue call(LuaValue arg) {
            try {
                return LuaValue.valueOf(exists(globals.finder.findFile(arg.tojstring())));
            } catch (Exception e) {
                return NIL;
            }
        }
    }

    private class save extends TwoArgFunction {
        @Override
        public LuaValue call(LuaValue arg1, LuaValue arg2) {
            return LuaValue.valueOf(save(globals.finder.findFile(arg1.tojstring()), arg2.checkstring()));
        }
    }
}
