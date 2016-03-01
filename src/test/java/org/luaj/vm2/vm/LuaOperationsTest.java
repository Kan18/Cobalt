/**
 * ****************************************************************************
 * Copyright (c) 2009 Luaj.org. All rights reserved.
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
 * ****************************************************************************
 */
package org.luaj.vm2.vm;

import org.junit.Test;
import org.luaj.vm2.*;
import org.luaj.vm2.compiler.LuaC;
import org.luaj.vm2.lib.ZeroArgFunction;
import org.luaj.vm2.lib.jse.JsePlatform;
import org.luaj.vm2.vm.TypeTest.MyData;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;

import static org.junit.Assert.*;
import static org.luaj.vm2.Constants.*;
import static org.luaj.vm2.Factory.*;

public class LuaOperationsTest {
	private final int sampleint = 77;
	private final long samplelong = 123400000000L;
	private final double sampledouble = 55.25;
	private final String samplestringstring = "abcdef";
	private final String samplestringint = String.valueOf(sampleint);
	private final String samplestringlong = String.valueOf(samplelong);
	private final String samplestringdouble = String.valueOf(sampledouble);
	private final Object sampleobject = new Object();
	private final MyData sampledata = new MyData();

	private final LuaValue somenil = NIL;
	private final LuaValue sometrue = TRUE;
	private final LuaValue somefalse = FALSE;
	private final LuaValue zero = ZERO;
	private final LuaValue intint = valueOf(sampleint);
	private final LuaValue longdouble = valueOf(samplelong);
	private final LuaValue doubledouble = valueOf(sampledouble);
	private final LuaValue stringstring = valueOf(samplestringstring);
	private final LuaValue stringint = valueOf(samplestringint);
	private final LuaValue stringlong = valueOf(samplestringlong);
	private final LuaValue stringdouble = valueOf(samplestringdouble);
	private final LuaTable table = listOf(new LuaValue[]{valueOf("aaa"), valueOf("bbb")});
	private final LuaValue somefunc = new ZeroArgFunction(table) {
		@Override
		public LuaValue call(LuaState state) {
			return NONE;
		}
	};
	private final LuaState state = LuaThread.getRunning().luaState;
	private final LuaThread thread = new LuaThread(LuaThread.getRunning().luaState, somefunc, table);
	private final Prototype proto = new Prototype();
	private final LuaClosure someclosure = new LuaClosure(proto, table);
	private final LuaUserdata userdataobj = userdataOf(sampleobject);
	private final LuaUserdata userdatacls = userdataOf(sampledata);

	private void throwsLuaError(String methodName, Object obj) {
		try {
			try {
				LuaValue.class.getMethod(methodName, LuaState.class).invoke(obj, state);
			} catch (NoSuchMethodException e) {
				LuaValue.class.getMethod(methodName).invoke(obj);
			}
			fail("failed to throw LuaError as required");
		} catch (InvocationTargetException e) {
			if (!(e.getTargetException() instanceof LuaError)) {
				fail("not a LuaError: " + e.getTargetException());
			}
		} catch (Exception e) {
			fail("bad exception: " + e);
		}
	}

	private void throwsLuaError(String methodName, Object obj, Object arg) {
		try {
			try {
				LuaValue.class.getMethod(methodName, LuaState.class, LuaValue.class).invoke(obj, state, arg);
			} catch (NoSuchMethodException e) {
				LuaValue.class.getMethod(methodName, LuaValue.class).invoke(obj, arg);
			}
			fail("failed to throw LuaError as required");
		} catch (InvocationTargetException e) {
			if (!(e.getTargetException() instanceof LuaError)) {
				fail("not a LuaError: " + e.getTargetException());
			}
		} catch (Exception e) {
			fail("bad exception: " + e);
		}
	}

	@Test
	public void testLen() {
		LuaState state = LuaThread.getRunning().luaState;

		throwsLuaError("len", somenil);
		throwsLuaError("len", sometrue);
		throwsLuaError("len", somefalse);
		throwsLuaError("len", zero);
		throwsLuaError("len", intint);
		throwsLuaError("len", longdouble);
		throwsLuaError("len", doubledouble);
		assertEquals(LuaInteger.valueOf(samplestringstring.length()), stringstring.len(state));
		assertEquals(LuaInteger.valueOf(samplestringint.length()), stringint.len(state));
		assertEquals(LuaInteger.valueOf(samplestringlong.length()), stringlong.len(state));
		assertEquals(LuaInteger.valueOf(samplestringdouble.length()), stringdouble.len(state));
		assertEquals(LuaInteger.valueOf(2), table.len(state));
		throwsLuaError("len", somefunc);
		throwsLuaError("len", thread);
		throwsLuaError("len", someclosure);
		throwsLuaError("len", userdataobj);
		throwsLuaError("len", userdatacls);
	}

	@Test
	public void testLength() {
		LuaState state = LuaThread.getRunning().luaState;

		throwsLuaError("length", somenil);
		throwsLuaError("length", sometrue);
		throwsLuaError("length", somefalse);
		throwsLuaError("length", zero);
		throwsLuaError("length", intint);
		throwsLuaError("length", longdouble);
		throwsLuaError("length", doubledouble);
		assertEquals(samplestringstring.length(), stringstring.length(state));
		assertEquals(samplestringint.length(), stringint.length(state));
		assertEquals(samplestringlong.length(), stringlong.length(state));
		assertEquals(samplestringdouble.length(), stringdouble.length(state));
		assertEquals(2, table.length(state));
		throwsLuaError("length", somefunc);
		throwsLuaError("length", thread);
		throwsLuaError("length", someclosure);
		throwsLuaError("length", userdataobj);
		throwsLuaError("length", userdatacls);
	}

	@Test
	public void testGetfenv() {
		throwsLuaError("getfenv", somenil);
		throwsLuaError("getfenv", sometrue);
		throwsLuaError("getfenv", somefalse);
		throwsLuaError("getfenv", zero);
		throwsLuaError("getfenv", intint);
		throwsLuaError("getfenv", longdouble);
		throwsLuaError("getfenv", doubledouble);
		throwsLuaError("getfenv", stringstring);
		throwsLuaError("getfenv", stringint);
		throwsLuaError("getfenv", stringlong);
		throwsLuaError("getfenv", stringdouble);
		throwsLuaError("getfenv", table);
		assertTrue(table == thread.getfenv());
		assertTrue(table == someclosure.getfenv());
		assertTrue(table == somefunc.getfenv());
		throwsLuaError("getfenv", userdataobj);
		throwsLuaError("getfenv", userdatacls);
	}

	@Test
	public void testSetfenv() {
		LuaTable table2 = listOf(new LuaValue[]{
			valueOf("ccc"),
			valueOf("ddd")});
		throwsLuaError("setfenv", somenil, table2);
		throwsLuaError("setfenv", sometrue, table2);
		throwsLuaError("setfenv", somefalse, table2);
		throwsLuaError("setfenv", zero, table2);
		throwsLuaError("setfenv", intint, table2);
		throwsLuaError("setfenv", longdouble, table2);
		throwsLuaError("setfenv", doubledouble, table2);
		throwsLuaError("setfenv", stringstring, table2);
		throwsLuaError("setfenv", stringint, table2);
		throwsLuaError("setfenv", stringlong, table2);
		throwsLuaError("setfenv", stringdouble, table2);
		throwsLuaError("setfenv", table, table2);
		thread.setfenv(table2);
		assertTrue(table2 == thread.getfenv());
		assertTrue(table == someclosure.getfenv());
		assertTrue(table == somefunc.getfenv());
		someclosure.setfenv(table2);
		assertTrue(table2 == someclosure.getfenv());
		assertTrue(table == somefunc.getfenv());
		somefunc.setfenv(table2);
		assertTrue(table2 == somefunc.getfenv());
		throwsLuaError("setfenv", userdataobj, table2);
		throwsLuaError("setfenv", userdatacls, table2);
	}

	public Prototype createPrototype(String script, String name) {
		LuaState state = LuaThread.getRunning().luaState;

		try {
			JsePlatform.standardGlobals(state);
			InputStream is = new ByteArrayInputStream(script.getBytes("UTF-8"));
			return LuaC.compile(is, name);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail(e.toString());
			return null;
		}
	}

	@Test
	public void testFunctionClosureThreadEnv() {
		LuaState state = LuaThread.getRunning().luaState;

		// set up suitable environments for execution
		LuaValue aaa = valueOf("aaa");
		LuaValue eee = valueOf("eee");
		LuaTable _G = JsePlatform.standardGlobals(state);
		LuaTable newenv = tableOf(new LuaValue[]{
			valueOf("a"), valueOf("aaa"),
			valueOf("b"), valueOf("bbb"),});
		LuaTable mt = tableOf(new LuaValue[]{INDEX, _G});
		newenv.setMetatable(state, mt);
		_G.set(state, "a", aaa);
		newenv.set(state, "a", eee);

		// function tests
		{
			LuaFunction f = new ZeroArgFunction(_G) {
				@Override
				public LuaValue call(LuaState state) {
					return env.get(state, "a");
				}
			};
			assertEquals(aaa, f.call(state));
			f.setfenv(newenv);
			assertEquals(newenv, f.getfenv());
			assertEquals(eee, f.call(state));
		}

		// closure tests
		{
			Prototype p = createPrototype("return a\n", "closuretester");
			LuaClosure c = new LuaClosure(p, _G);
			assertEquals(aaa, c.call(state));
			c.setfenv(newenv);
			assertEquals(newenv, c.getfenv());
			assertEquals(eee, c.call(state));
		}

		// thread tests, functions created in threads inherit the thread's environment initially
		// those closures created not in any other function get the thread's enviroment
		Prototype p2 = createPrototype("return loadstring('return a')", "threadtester");
		{
			LuaThread t = new LuaThread(state, new LuaClosure(p2, _G), _G);
			Varargs v = t.resume(NONE);
			assertEquals(TRUE, v.arg(1));
			LuaValue f = v.arg(2);
			assertEquals(TFUNCTION, f.type());
			assertEquals(aaa, f.call(state));
			assertEquals(_G, f.getfenv());
		}
		{
			// change the thread environment after creation!
			LuaThread t = new LuaThread(state, new LuaClosure(p2, _G), _G);
			t.setfenv(newenv);
			Varargs v = t.resume(NONE);
			assertEquals(TRUE, v.arg(1));
			LuaValue f = v.arg(2);
			assertEquals(TFUNCTION, f.type());
			assertEquals(eee, f.call(state));
			assertEquals(newenv, f.getfenv());
		}
		{
			// let the closure have a different environment from the thread
			Prototype p3 = createPrototype("return function() return a end", "envtester");
			LuaThread t = new LuaThread(state, new LuaClosure(p3, newenv), _G);
			Varargs v = t.resume(NONE);
			assertEquals(TRUE, v.arg(1));
			LuaValue f = v.arg(2);
			assertEquals(TFUNCTION, f.type());
			assertEquals(eee, f.call(state));
			assertEquals(newenv, f.getfenv());
		}
	}
}
