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
package org.luaj.vm2.table;

import org.junit.Test;
import org.luaj.vm2.*;

import java.util.Vector;

import static org.junit.Assert.*;
import static org.luaj.vm2.Constants.NIL;

/**
 * Tests for tables used as lists.
 */
public class TableArrayTest {

	protected LuaTable new_Table() {
		return new LuaTable();
	}

	protected LuaTable new_Table(int n, int m) {
		return new LuaTable(n, m);
	}

	@Test
	public void testInOrderIntegerKeyInsertion() {
		LuaTable t = new_Table();
		LuaState state = LuaThread.getRunning().luaState;

		for (int i = 1; i <= 32; ++i) {
			t.set(state, i, LuaString.valueOf("Test Value! " + i));
		}

		// Ensure all keys are still there.
		for (int i = 1; i <= 32; ++i) {
			assertEquals("Test Value! " + i, t.get(state, i).tojstring());
		}

		// Ensure capacities make sense
		assertEquals(0, t.getHashLength());

		assertTrue(t.getArrayLength() >= 32);
		assertTrue(t.getArrayLength() <= 64);

	}

	@Test
	public void testResize() {
		LuaTable t = new_Table();
		LuaState state = LuaThread.getRunning().luaState;

		// NOTE: This order of insertion is important.
		t.set(state, 3, LuaInteger.valueOf(3));
		t.set(state, 1, LuaInteger.valueOf(1));
		t.set(state, 5, LuaInteger.valueOf(5));
		t.set(state, 4, LuaInteger.valueOf(4));
		t.set(state, 6, LuaInteger.valueOf(6));
		t.set(state, 2, LuaInteger.valueOf(2));

		for (int i = 1; i < 6; ++i) {
			assertEquals(LuaInteger.valueOf(i), t.get(state, i));
		}

		assertTrue(t.getArrayLength() >= 0 && t.getArrayLength() <= 2);
		assertTrue(t.getHashLength() >= 4);
	}

	@Test
	public void testOutOfOrderIntegerKeyInsertion() {
		LuaTable t = new_Table();
		LuaState state = LuaThread.getRunning().luaState;

		for (int i = 32; i > 0; --i) {
			t.set(state, i, LuaString.valueOf("Test Value! " + i));
		}

		// Ensure all keys are still there.
		for (int i = 1; i <= 32; ++i) {
			assertEquals("Test Value! " + i, t.get(state, i).tojstring());
		}

		// Ensure capacities make sense
		assertTrue(t.getArrayLength() >= 0);
		assertTrue(t.getArrayLength() <= 6);

		assertTrue(t.getHashLength() >= 16);
		assertTrue(t.getHashLength() <= 64);

	}

	@Test
	public void testStringAndIntegerKeys() {
		LuaTable t = new_Table();
		LuaState state = LuaThread.getRunning().luaState;

		for (int i = 0; i < 10; ++i) {
			LuaString str = LuaString.valueOf(String.valueOf(i));
			t.set(state, i, str);
			t.set(state, str, LuaInteger.valueOf(i));
		}

		assertTrue(t.getArrayLength() >= 9); // 1, 2, ..., 9
		assertTrue(t.getArrayLength() <= 18);
		assertTrue(t.getHashLength() >= 11); // 0, "0", "1", ..., "9"
		assertTrue(t.getHashLength() <= 33);

		LuaValue[] keys = t.keys();

		int intKeys = 0;
		int stringKeys = 0;

		assertEquals(20, keys.length);
		for (LuaValue k : keys) {
			if (k instanceof LuaInteger) {
				final int ik = k.toint();
				assertTrue(ik >= 0 && ik < 10);
				final int mask = 1 << ik;
				assertTrue((intKeys & mask) == 0);
				intKeys |= mask;
			} else if (k instanceof LuaString) {
				final int ik = Integer.parseInt(k.tojstring());
				assertEquals(String.valueOf(ik), k.tojstring());
				assertTrue(ik >= 0 && ik < 10);
				final int mask = 1 << ik;
				assertTrue("Key \"" + ik + "\" found more than once", (stringKeys & mask) == 0);
				stringKeys |= mask;
			} else {
				fail("Unexpected type of key found");
			}
		}

		assertEquals(0x03FF, intKeys);
		assertEquals(0x03FF, stringKeys);
	}

	@Test
	public void testBadInitialCapacity() {
		LuaState state = LuaThread.getRunning().luaState;
		LuaTable t = new_Table(0, 1);

		t.set(state, "test", LuaString.valueOf("foo"));
		t.set(state, "explode", LuaString.valueOf("explode"));
		assertEquals(2, t.keyCount());
	}

	@Test
	public void testRemove0() {
		LuaState state = LuaThread.getRunning().luaState;
		LuaTable t = new_Table(2, 0);

		t.set(state, 1, LuaString.valueOf("foo"));
		t.set(state, 2, LuaString.valueOf("bah"));
		assertNotSame(NIL, t.get(state, 1));
		assertNotSame(NIL, t.get(state, 2));
		assertEquals(NIL, t.get(state, 3));

		t.set(state, 1, NIL);
		t.set(state, 2, NIL);
		t.set(state, 3, NIL);
		assertEquals(NIL, t.get(state, 1));
		assertEquals(NIL, t.get(state, 2));
		assertEquals(NIL, t.get(state, 3));
	}

	@Test
	public void testRemove1() {
		LuaTable t = new_Table(0, 1);
		LuaState state = LuaThread.getRunning().luaState;

		assertEquals(0, t.keyCount());

		t.set(state, "test", LuaString.valueOf("foo"));
		assertEquals(1, t.keyCount());
		t.set(state, "explode", NIL);
		assertEquals(1, t.keyCount());
		t.set(state, 42, NIL);
		assertEquals(1, t.keyCount());
		t.set(state, new_Table(), NIL);
		assertEquals(1, t.keyCount());
		t.set(state, "test", NIL);
		assertEquals(0, t.keyCount());

		t.set(state, 10, LuaInteger.valueOf(5));
		t.set(state, 10, NIL);
		assertEquals(0, t.keyCount());
	}

	@Test
	public void testRemove2() {
		LuaTable t = new_Table(0, 1);
		LuaState state = LuaThread.getRunning().luaState;

		t.set(state, "test", LuaString.valueOf("foo"));
		t.set(state, "string", LuaInteger.valueOf(10));
		assertEquals(2, t.keyCount());

		t.set(state, "string", NIL);
		t.set(state, "three", LuaDouble.valueOf(3.14));
		assertEquals(2, t.keyCount());

		t.set(state, "test", NIL);
		assertEquals(1, t.keyCount());

		t.set(state, 10, LuaInteger.valueOf(5));
		assertEquals(2, t.keyCount());

		t.set(state, 10, NIL);
		assertEquals(1, t.keyCount());

		t.set(state, "three", NIL);
		assertEquals(0, t.keyCount());
	}

	@Test
	public void testInOrderlen() {
		LuaTable t = new_Table();
		LuaState state = LuaThread.getRunning().luaState;

		for (int i = 1; i <= 32; ++i) {
			LuaValue v = LuaString.valueOf("Test Value! " + i);
			t.set(state, i, v);
			assertEquals(i, t.length(state));
			assertEquals(i, t.maxn());
		}
	}

	@Test
	public void testOutOfOrderlen() {
		LuaTable t = new_Table();
		LuaState state = LuaThread.getRunning().luaState;

		for (int j = 8; j < 32; j += 8) {
			for (int i = j; i > 0; --i) {
				t.set(state, i, LuaString.valueOf("Test Value! " + i));
			}
			assertEquals(j, t.length(state));
			assertEquals(j, t.maxn());
		}
	}

	@Test
	public void testStringKeyslen() {
		LuaTable t = new_Table();
		LuaState state = LuaThread.getRunning().luaState;

		for (int i = 1; i <= 32; ++i) {
			t.set(state, "str-" + i, LuaString.valueOf("String Key Test Value! " + i));
			assertEquals(0, t.length(state));
			assertEquals(0, t.maxn());
		}
	}

	@Test
	public void testMixedKeyslen() {
		LuaTable t = new_Table();
		LuaState state = LuaThread.getRunning().luaState;

		for (int i = 1; i <= 32; ++i) {
			t.set(state, "str-" + i, LuaString.valueOf("String Key Test Value! " + i));
			t.set(state, i, LuaString.valueOf("Int Key Test Value! " + i));
			assertEquals(i, t.length(state));
			assertEquals(i, t.maxn());
		}
	}

	private static void compareLists(LuaTable t, Vector<LuaString> v) {
		int n = v.size();
		LuaState state = LuaThread.getRunning().luaState;
		assertEquals(v.size(), t.length(state));
		for (int j = 0; j < n; j++) {
			Object vj = v.elementAt(j);
			Object tj = t.get(state, j + 1).tojstring();
			vj = ((LuaString) vj).tojstring();
			assertEquals(vj, tj);
		}
	}

	public void testInsertBeginningOfList() {
		LuaTable t = new_Table();
		Vector<LuaString> v = new Vector<>();
		LuaState state = LuaThread.getRunning().luaState;

		for (int i = 1; i <= 32; ++i) {
			LuaString test = LuaString.valueOf("Test Value! " + i);
			t.insert(1, test);
			v.insertElementAt(test, 0);
			compareLists(t, v);
		}
	}

	public void testInsertEndOfList() {
		LuaTable t = new_Table();
		Vector<LuaString> v = new Vector<>();

		for (int i = 1; i <= 32; ++i) {
			LuaString test = LuaString.valueOf("Test Value! " + i);
			t.insert(0, test);
			v.insertElementAt(test, v.size());
			compareLists(t, v);
		}
	}

	public void testInsertMiddleOfList() {
		LuaTable t = new_Table();
		Vector<LuaString> v = new Vector<>();

		for (int i = 1; i <= 32; ++i) {
			LuaString test = LuaString.valueOf("Test Value! " + i);
			int m = i / 2;
			t.insert(m + 1, test);
			v.insertElementAt(test, m);
			compareLists(t, v);
		}
	}

	private static void prefillLists(LuaTable t, Vector<LuaString> v) {
		for (int i = 1; i <= 32; ++i) {
			LuaString test = LuaString.valueOf("Test Value! " + i);
			t.insert(0, test);
			v.insertElementAt(test, v.size());
		}
	}

	public void testRemoveBeginningOfList() {
		LuaTable t = new_Table();
		Vector<LuaString> v = new Vector<>();
		prefillLists(t, v);
		for (int i = 1; i <= 32; ++i) {
			t.remove(1);
			v.removeElementAt(0);
			compareLists(t, v);
		}
	}

	public void testRemoveEndOfList() {
		LuaTable t = new_Table();
		Vector<LuaString> v = new Vector<>();
		prefillLists(t, v);
		for (int i = 1; i <= 32; ++i) {
			t.remove(0);
			v.removeElementAt(v.size() - 1);
			compareLists(t, v);
		}
	}

	public void testRemoveMiddleOfList() {
		LuaTable t = new_Table();
		Vector<LuaString> v = new Vector<>();
		prefillLists(t, v);
		for (int i = 1; i <= 32; ++i) {
			int m = v.size() / 2;
			t.remove(m + 1);
			v.removeElementAt(m);
			compareLists(t, v);
		}
	}

}
