/*******************************************************************************
 * Copyright (c) 2011 Luaj.org. All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 ******************************************************************************/
package org.luaj.lib.jse;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.luaj.LuaError;
import org.luaj.LuaValue;
import org.luaj.Varargs;

/**
 * LuaValue that represents a Java class.
 * <p>
 * Will respond to get() and set() by returning field values, or java methods.
 * <p>
 * This class is not used directly.
 * It is returned by calls to {@link CoerceJavaToLua#coerce(Object)}
 * when a Class is supplied.
 *
 * @see CoerceJavaToLua
 * @see CoerceLuaToJava
 */
class JavaClass extends JavaInstance implements CoerceJavaToLua.Coercion {
    final HashMap<LuaValue, Integer> typeCache = new HashMap<>();
    final HashMap<LuaValue, Integer> setTypeCache = new HashMap<>();
    final HashMap<LuaValue, LuaValue> finalValueCache = new HashMap<>();
    final HashMap<LuaValue, LuaValue> getterCache = new HashMap<>();
    final HashMap<LuaValue, LuaValue> setterCache = new HashMap<>();
    static final HashMap<LuaValue, LuaValue> classMethods = new HashMap<>();

    static final Map classes = Collections.synchronizedMap(new HashMap());

    static final LuaValue NEW = valueOf("new");

    Map fields;
    Map methods;
    Map innerclasses;

    static JavaClass forClass(Class c) {
        JavaClass j = (JavaClass) classes.get(c);
        if (j == null)
            classes.put(c, j = new JavaClass(c));
        return j;
    }

    static JavaClass forName(String c) throws ClassNotFoundException {
        JavaClass j = (JavaClass) classes.get(c);
        if (j == null)
            classes.put(c, j = forClass(Class.forName(c)));
        return j;
    }

    static {
        Method[] ms = Class.class.getMethods();
        for (Method m : ms) {
            classMethods.put(LuaValue.valueOf(m.getName()),JavaMethod.forMethod(m));
        }
    }

    JavaClass(Class c) {
        super(c);
        this.jclass = this;
    }

    @Override
    public LuaValue call() {
        LuaValue m = get(NEW);
        return m.call();
    }

    @Override
    public LuaValue call(LuaValue arg) {
         Class obj = (Class) touserdata();
        if(obj.isPrimitive()){
            return new JavaInstance(CoerceLuaToJava.coerce(arg,obj));
        }
        if(arg.istable()){
            if (obj.isInterface())
                return LuajavaLib.createProxy(obj, arg);
            if(Map.class.isAssignableFrom(obj))
                return CoerceJavaToLua.coerce(new CoerceLuaToJava.MapCoercion(obj).coerce(arg));
            if(List.class.isAssignableFrom(obj))
                return CoerceJavaToLua.coerce(new CoerceLuaToJava.ListCoercion(obj).coerce(arg));
            if((obj.getModifiers() & Modifier.ABSTRACT) != 0){
                try {
                    return LuajavaLib.override(obj,arg).call();
                } catch (Exception e) {
                    throw new LuaError(e);
                }
            }
            try{
                LuaValue m = get(NEW);
                return m.call(arg);
            } catch (Exception e) {
                return CoerceJavaToLua.coerce(new CoerceLuaToJava.ArrayCoercion(obj).coerce(arg));
            }
        }
        LuaValue m = get(NEW);
        return m.call(arg);
    }

    @Override
    public LuaValue call(LuaValue arg1, LuaValue arg2) {
        LuaValue m = get(NEW);
        return m.call(arg1, arg2);
    }

    @Override
    public LuaValue call(LuaValue arg1, LuaValue arg2, LuaValue arg3) {
        LuaValue m = get(NEW);
        return m.call(arg1, arg2, arg3);
    }

    @Override
    public Varargs invoke(Varargs args) {
        if(args.narg()==1){
            Class obj = (Class) touserdata();
            LuaValue arg = args.arg1();
            if(obj.isPrimitive()){
                return new JavaInstance(CoerceLuaToJava.coerce(arg,obj));
            }
            if(arg.istable()){
                if (obj.isInterface())
                    return LuajavaLib.createProxy(obj, arg);
                if(Map.class.isAssignableFrom(obj))
                    return CoerceJavaToLua.coerce(new CoerceLuaToJava.MapCoercion(obj).coerce(arg));
                if(List.class.isAssignableFrom(obj))
                    return CoerceJavaToLua.coerce(new CoerceLuaToJava.ListCoercion(obj).coerce(arg));
                if((obj.getModifiers() & Modifier.ABSTRACT) != 0){
                    try {
                        return LuajavaLib.override(obj,arg).call();
                    } catch (Exception e) {
                        throw new LuaError(e);
                    }
                }
                try{
                    LuaValue m = get(NEW);
                    return m.invoke(args);
                } catch (Exception e) {
                    return CoerceJavaToLua.coerce(new CoerceLuaToJava.ArrayCoercion(obj).coerce(arg));
                }
            }
        }
        LuaValue m = get(NEW);
        return m.invoke(args);
    }

    public LuaValue coerce(Object javaValue) {
        return this;
    }

    Field getField(LuaValue key) {
        if (fields == null) {
            Map m = new HashMap();
            Field[] f = ((Class) m_instance).getFields();
            for (int i = 0; i < f.length; i++) {
                Field fi = f[i];
                if (Modifier.isPublic(fi.getModifiers())) {
                    m.put(LuaValue.valueOf(fi.getName()), fi);
                    try {
                        if (!fi.isAccessible())
                            fi.setAccessible(true);
                    } catch (SecurityException s) {
                    }
                }
            }
            fields = m;
        }
        return (Field) fields.get(key);
    }

    LuaValue getMethod(LuaValue key) {
        if (methods == null) {
            Map namedlists = new HashMap();
            Method[] m = ((Class) m_instance).getMethods();
            for (int i = 0; i < m.length; i++) {
                Method mi = m[i];
                if (Modifier.isPublic(mi.getModifiers())) {
                    String name = mi.getName();
                    List list = (List) namedlists.get(name);
                    if (list == null)
                        namedlists.put(name, list = new ArrayList());
                    list.add(JavaMethod.forMethod(mi));
                }
            }
            Map map = new HashMap();
            Constructor[] c = ((Class) m_instance).getConstructors();
            List list = new ArrayList();
            for (int i = 0; i < c.length; i++)
                if (Modifier.isPublic(c[i].getModifiers()))
                    list.add(JavaConstructor.forConstructor(c[i]));
            switch (list.size()) {
                case 0:
                    break;
                case 1:
                    map.put(NEW, list.get(0));
                    break;
                default:
                    map.put(NEW, JavaConstructor.forConstructors((JavaConstructor[]) list.toArray(new JavaConstructor[list.size()])));
                    break;
            }
            map.putAll(classMethods);
            for (Iterator it = namedlists.entrySet().iterator(); it.hasNext(); ) {
                Entry e = (Entry) it.next();
                String name = (String) e.getKey();
                List methods = (List) e.getValue();
                map.put(LuaValue.valueOf(name),
                        methods.size() == 1 ?
                                methods.get(0) :
                                JavaMethod.forMethods((JavaMethod[]) methods.toArray(new JavaMethod[methods.size()])));
            }
            methods = map;
        }
        return (LuaValue) methods.get(key);
    }

    Class getInnerClass(LuaValue key) {
        if (innerclasses == null) {
            Map m = new HashMap();
            Class[] c = ((Class) m_instance).getClasses();
            for (int i = 0; i < c.length; i++) {
                Class ci = c[i];
                String name = ci.getName();
                String stub = name.substring(Math.max(name.lastIndexOf('$'), name.lastIndexOf('.')) + 1);
                m.put(LuaValue.valueOf(stub), ci);
            }
            c = ((Class) m_instance).getDeclaredClasses();
            for (int i = 0; i < c.length; i++) {
                Class ci = c[i];
                if(!Modifier.isPublic(ci.getModifiers()))
                    continue;
                String name = ci.getName();
                String stub = name.substring(Math.max(name.lastIndexOf('$'), name.lastIndexOf('.')) + 1);
                m.put(LuaValue.valueOf(stub), ci);
            }
            innerclasses = m;
        }
        return (Class) innerclasses.get(key);
    }

    public LuaValue getConstructor() {
        return getMethod(NEW);
    }

    @Override
    public LuaValue get(LuaValue key) {
        if (key.isnumber())
            return CoerceJavaToLua.coerce(Array.newInstance((Class<?>) touserdata(), key.toint()));
        if(key.tojstring().equals("override")){
            return new LuajavaLib.override(this);
        }
        return super.get(key);
    }

}
