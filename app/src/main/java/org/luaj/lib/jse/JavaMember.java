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

import org.luaj.Varargs;
import org.luaj.lib.VarArgFunction;
import org.luaj.lib.jse.CoerceLuaToJava.Coercion;

import java.lang.reflect.Array;

/**
 * Java method or constructor.
 * <p>
 * Primarily handles argument coercion for parameter lists including scoring of compatibility and 
 * java varargs handling.
 * <p>
 * This class is not used directly.  
 * It is an abstract base class for {@link JavaConstructor} and {@link JavaMethod}.
 * @see JavaConstructor
 * @see JavaMethod
 * @see CoerceJavaToLua
 * @see CoerceLuaToJava
 */
abstract
class JavaMember extends VarArgFunction {
	
	static final int METHOD_MODIFIERS_VARARGS = 0x80;

	final Coercion[] fixedargs;
	final Coercion varargs;
	private Coercion vararg2;
	private Class varclass;

	protected JavaMember(Class[] params, int modifiers) {
		boolean isvarargs = ((modifiers & METHOD_MODIFIERS_VARARGS) != 0);
		fixedargs = new Coercion[isvarargs? params.length-1: params.length];
		for ( int i=0; i<fixedargs.length; i++ )
			fixedargs[i] = CoerceLuaToJava.getCoercion( params[i] );
		varargs = isvarargs? CoerceLuaToJava.getCoercion( params[params.length-1] ): null;
		if(isvarargs){
			varclass=params[params.length-1].getComponentType();
			vararg2 = CoerceLuaToJava.getCoercion( varclass );
		}
	}
	
	int score(Varargs args) {
		int n = args.narg();
		int s = n>fixedargs.length? CoerceLuaToJava.SCORE_WRONG_TYPE * (n-fixedargs.length): 0;
		for ( int j=0; j<fixedargs.length; j++ ) {
			s += fixedargs[j].score( args.arg(j+1) );
		}
		if ( varargs != null ){
			if(n==fixedargs.length+1)
				s += Math.min(varargs.score( args.arg(fixedargs.length+1)), vararg2.score( args.arg(fixedargs.length+1)));
            else
			for ( int k=fixedargs.length; k<n; k++ )
				s += vararg2.score( args.arg(k+1));
		}
		return s;
	}
	
	protected Object[] convertArgs(Varargs args) {
		Object[] a;
		if ( varargs == null ) {
			a = new Object[fixedargs.length];
			for ( int i=0; i<a.length; i++ )
				a[i] = fixedargs[i].coerce( args.arg(i+1) );
		} else {
			int n = args.narg();
			a = new Object[fixedargs.length+1];
			for ( int i=0; i<fixedargs.length; i++ )
				a[i] = fixedargs[i].coerce( args.arg(i+1));
			if(n==fixedargs.length+1&&varargs.score(args.arg(fixedargs.length+1))<0x100){
				a[fixedargs.length] = varargs.coerce( args.arg(fixedargs.length+1) );
			} else {
				int m = Math.max(args.narg()-fixedargs.length,0);
				Object v = Array.newInstance(varclass, m);
				for ( int i=fixedargs.length; i<n; i++ )
					Array.set(v,i-fixedargs.length, vararg2.coerce( args.arg(i+1)));
				a[fixedargs.length]=v;
			}

			/*int n = Math.max(fixedargs.length,args.narg());
			a = new Object[n];
			int m = Math.max(args.narg()-fixedargs.length,0);
			Object v = Array.newInstance(varclass, m);
			for ( int i=0; i<fixedargs.length; i++ )
				a[i] = fixedargs[i].coerce( args.arg(i+1));
			for ( int i=fixedargs.length; i<n; i++ )
				//a[i] = varargs.coerce( args.arg(i+1) );
			    Array.set(v,i-fixedargs.length, varargs.coerce( args.arg(i+1)));
			a[fixedargs.length]=v;*/
		}
		return a;
	}
}
