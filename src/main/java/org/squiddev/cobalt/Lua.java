/*
 * The MIT License (MIT)
 *
 * Original Source: Copyright (c) 2009-2011 Luaj.org. All rights reserved.
 * Modifications: Copyright (c) 2015-2020 SquidDev
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.squiddev.cobalt;

/**
 * Constants for lua limits and opcodes.
 * <p>
 * This is a direct translation of C lua distribution header file constants for bytecode creation and processing.
 */
public class Lua {
	/**
	 * Use return values from previous op
	 */
	public static final int LUA_MULTRET = -1;

	/**
	 * Masks for new-style vararg
	 */
	public static final int VARARG_HASARG = 1;
	public static final int VARARG_ISVARARG = 2;
	public static final int VARARG_NEEDSARG = 4;

	/*===========================================================================
	We assume that instructions are unsigned numbers.
	All instructions have an opcode in the first 6 bits.
	Instructions can have the following fields:
		`A' : 8 bits
		`B' : 9 bits
		`C' : 9 bits
		`Bx' : 18 bits (`B' and `C' together)
		`sBx' : signed Bx

	A signed argument is represented in excess K; that is, the number
	value is the unsigned value minus K. K is exactly the maximum value
	for that argument (so that -max is represented by 0, and +max is
	represented by 2*max), which is half the maximum for the corresponding
	unsigned argument.
	===========================================================================*/

	// basic instruction format
	public static final int iABC = 0;
	public static final int iABx = 1;
	public static final int iAsBx = 2;

	// Size and position of opcode arguments.
	public static final int SIZE_C = 9;
	public static final int SIZE_B = 9;
	public static final int SIZE_Bx = SIZE_C + SIZE_B;
	public static final int SIZE_A = 8;

	public static final int SIZE_OP = 6;

	public static final int POS_OP = 0;
	public static final int POS_A = POS_OP + SIZE_OP;
	public static final int POS_C = POS_A + SIZE_A;
	public static final int POS_B = POS_C + SIZE_C;
	public static final int POS_Bx = POS_C;

	// Limits for opcode arguments.
	public static final int MAX_OP = (1 << Lua.SIZE_OP) - 1;
	public static final int MAXARG_A = (1 << Lua.SIZE_A) - 1;
	public static final int MAXARG_B = (1 << Lua.SIZE_B) - 1;
	public static final int MAXARG_C = (1 << Lua.SIZE_C) - 1;
	public static final int MAXARG_Bx = (1 << Lua.SIZE_Bx) - 1;
	public static final int MAXARG_sBx = MAXARG_Bx >> 1; // sBx' is signed

	public static final int MASK_OP = ((1 << Lua.SIZE_OP) - 1) << Lua.POS_OP;
	public static final int MASK_A = ((1 << Lua.SIZE_A) - 1) << Lua.POS_A;
	public static final int MASK_B = ((1 << Lua.SIZE_B) - 1) << Lua.POS_B;
	public static final int MASK_C = ((1 << Lua.SIZE_C) - 1) << Lua.POS_C;
	public static final int MASK_Bx = ((1 << Lua.SIZE_Bx) - 1) << Lua.POS_Bx;

	// Utilities for reading instructions.

	public static int GET_OPCODE(int i) {
		return (i >> POS_OP) & MAX_OP;
	}

	public static int GETARG_A(int i) {
		return (i >> POS_A) & MAXARG_A;
	}

	public static int GETARG_B(int i) {
		return (i >> POS_B) & MAXARG_B;
	}

	public static int GETARG_C(int i) {
		return (i >> POS_C) & MAXARG_C;
	}

	public static int GETARG_Bx(int i) {
		return (i >> POS_Bx) & MAXARG_Bx;
	}

	public static int GETARG_sBx(int i) {
		return ((i >> POS_Bx) & MAXARG_Bx) - MAXARG_sBx;
	}


	/**
	 * This bit 1 means constant (0 means register)
	 */
	private static final int BITRK = (1 << (SIZE_B - 1));
	public static final int MAXINDEXRK = (BITRK - 1);

	/**
	 * Test whether value is a constant
	 *
	 * @param x The part of the opcode
	 * @return If the opcode part is a constant
	 */
	public static boolean ISK(int x) {
		return (x & BITRK) != 0;
	}

	/**
	 * Gets the index of the constant
	 *
	 * @param r The part of the opcode
	 * @return The constant index
	 */
	public static int INDEXK(int r) {
		return r & ~BITRK;
	}

	/**
	 * code a constant index as a RK value
	 *
	 * @param x The constant index
	 * @return The part of the opcode
	 */
	public static int RKASK(int x) {
		return x | BITRK;
	}

	/**
	 * Invalid register that fits in 8 bits
	 */
	public static final int NO_REG = Lua.MAXARG_A;

	/*
	 ** R(x) - register
	 ** Kst(x) - constant (in constant table)
	 ** RK(x) == if ISK(x) then Kst(INDEXK(x)) else R(x)
	 */
	public static final int OP_MOVE = 0;      // A B     R(A) := R(B)
	public static final int OP_LOADK = 1;     // A Bx    R(A) := Kst(Bx)
	public static final int OP_LOADBOOL = 2;  // A B C   R(A) := (Bool)B; if (C) pc++
	public static final int OP_LOADNIL = 3;   // A B     R(A) := ... := R(B) := nil
	public static final int OP_GETUPVAL = 4;  // A B     R(A) := UpValue[B]

	public static final int OP_GETGLOBAL = 5; // A Bx    R(A) := Gbl[Kst(Bx)]
	public static final int OP_GETTABLE = 6;  // A B C   R(A) := R(B)[RK(C)]

	public static final int OP_SETGLOBAL = 7; // A Bx    Gbl[Kst(Bx)] := R(A)
	public static final int OP_SETUPVAL = 8;  // A B     UpValue[B] := R(A)
	public static final int OP_SETTABLE = 9;  // A B C   R(A)[RK(B)] := RK(C)

	public static final int OP_NEWTABLE = 10; // A B C   R(A) := {} (size = B,C)

	public static final int OP_SELF = 11;     // A B C   R(A+1) := R(B); R(A) := R(B)[RK(C)]

	public static final int OP_ADD = 12;      // A B C   R(A) := RK(B) + RK(C)
	public static final int OP_SUB = 13;      // A B C   R(A) := RK(B) - RK(C)
	public static final int OP_MUL = 14;      // A B C   R(A) := RK(B) * RK(C)
	public static final int OP_DIV = 15;      // A B C   R(A) := RK(B) / RK(C)
	public static final int OP_MOD = 16;      // A B C   R(A) := RK(B) % RK(C)
	public static final int OP_POW = 17;      // A B C   R(A) := RK(B) ^ RK(C)
	public static final int OP_UNM = 18;      // A B     R(A) := -R(B)
	public static final int OP_NOT = 19;      // A B     R(A) := not R(B)
	public static final int OP_LEN = 20;      // A B     R(A) := length of R(B)

	public static final int OP_CONCAT = 21;   // A B C   R(A) := R(B).. ... ..R(C)

	public static final int OP_JMP = 22;      // sBx     pc+=sBx

	public static final int OP_EQ = 23;       // A B C   if ((RK(B) == RK(C)) ~= A) then pc++
	public static final int OP_LT = 24;       // A B C   if ((RK(B) <  RK(C)) ~= A) then pc++
	public static final int OP_LE = 25;       // A B C   if ((RK(B) <= RK(C)) ~= A) then pc++

	public static final int OP_TEST = 26;     // A C     if not (R(A) <=> C) then pc++                   *
	public static final int OP_TESTSET = 27;  // A B C   if (R(B) <=> C) then R(A) := R(B) else pc++     *

	public static final int OP_CALL = 28;     // A B C   R(A), ... ,R(A+C-2) := R(A)(R(A+1), ... ,R(A+B-1))
	public static final int OP_TAILCALL = 29; // A B C   return R(A)(R(A+1), ... ,R(A+B-1))
	public static final int OP_RETURN = 30;   // A B     return R(A), ... ,R(A+B-2)      (see note)

	public static final int OP_FORLOOP = 31;  // A sBx   R(A)+=R(A+2; if R(A) <?= R(A+1) then { pc+=sBx; R(A+3)=R(A) }*/
	public static final int OP_FORPREP = 32;  // A sBx   R(A)-=R(A+2); pc+=sBx

	public static final int OP_TFORLOOP = 33; // A C     R(A+3), ... ,R(A+2+C) := R(A)(R(A+1), R(A+2)); if R(A+3) ~= nil then R(A+2)=R(A+3) else pc++   */
	public static final int OP_SETLIST = 34;  // A B C   R(A)[(C-1)*FPF+i] := R(A+i), 1 <= i <= B

	public static final int OP_CLOSE = 35;    // A       close all variables in the stack up to (>=) R(A)
	public static final int OP_CLOSURE = 36;  // A Bx    R(A) := closure(KPROTO[Bx], R(A), ... ,R(A+n))

	public static final int OP_VARARG = 37;   // A B     R(A), R(A+1), ..., R(A+B-1) = vararg
	public static final int NUM_OPCODES = OP_VARARG + 1;

	/*===========================================================================
	  Notes:
	  (*) In OP_CALL, if (B == 0) then B = top. C is the number of returns - 1,
	      and can be 0: OP_CALL then sets `top' to last_result+1, so
	      next open instruction (OP_CALL, OP_RETURN, OP_SETLIST) may use `top'.

	  (*) In OP_VARARG, if (B == 0) then use actual number of varargs and
	      set top (like in OP_CALL with C == 0).

	  (*) In OP_RETURN, if (B == 0) then return up to `top'

	  (*) In OP_SETLIST, if (B == 0) then B = `top';
	      if (C == 0) then next `instruction' is real C

	  (*) For comparisons, A specifies what condition the test should accept
	      (true or false).

	  (*) All `skips' (pc++) assume that next instruction is a jump
	===========================================================================*/


	/*
	 ** masks for instruction properties. The format is:
	 ** bits 0-1: op mode
	 ** bits 2-3: C arg mode
	 ** bits 4-5: B arg mode
	 ** bit 6: instruction set register A
	 ** bit 7: operator is a test
	 */

	public static final int OpArgN = 0;  /* argument is not used */
	public static final int OpArgU = 1;  /* argument is used */
	public static final int OpArgR = 2;  /* argument is a register or a jump offset */
	public static final int OpArgK = 3;  /* argument is a constant or register/constant */

	private static int opmode(int t, int a, int b, int c, int m) {
		return (t << 7) | (a << 6) | (b << 4) | (c << 2) | m;
	}

	public static final int[] opmodes = {
		opmode(0, 1, OpArgR, OpArgN, iABC),  // OP_MOVE
		opmode(0, 1, OpArgK, OpArgN, iABx),  // OP_LOADK
		opmode(0, 1, OpArgU, OpArgU, iABC),  // OP_LOADBOOL
		opmode(0, 1, OpArgR, OpArgN, iABC),  // OP_LOADNIL
		opmode(0, 1, OpArgU, OpArgN, iABC),  // OP_GETUPVAL
		opmode(0, 1, OpArgK, OpArgN, iABx),  // OP_GETGLOBAL
		opmode(0, 1, OpArgR, OpArgK, iABC),  // OP_GETTABLE
		opmode(0, 0, OpArgK, OpArgN, iABx),  // OP_SETGLOBAL
		opmode(0, 0, OpArgU, OpArgN, iABC),  // OP_SETUPVAL
		opmode(0, 0, OpArgK, OpArgK, iABC),  // OP_SETTABLE
		opmode(0, 1, OpArgU, OpArgU, iABC),  // OP_NEWTABLE
		opmode(0, 1, OpArgR, OpArgK, iABC),  // OP_SELF
		opmode(0, 1, OpArgK, OpArgK, iABC),  // OP_ADD
		opmode(0, 1, OpArgK, OpArgK, iABC),  // OP_SUB
		opmode(0, 1, OpArgK, OpArgK, iABC),  // OP_MUL
		opmode(0, 1, OpArgK, OpArgK, iABC),  // OP_DIV
		opmode(0, 1, OpArgK, OpArgK, iABC),  // OP_MOD
		opmode(0, 1, OpArgK, OpArgK, iABC),  // OP_POW
		opmode(0, 1, OpArgR, OpArgN, iABC),  // OP_UNM
		opmode(0, 1, OpArgR, OpArgN, iABC),  // OP_NOT
		opmode(0, 1, OpArgR, OpArgN, iABC),  // OP_LEN
		opmode(0, 1, OpArgR, OpArgR, iABC),  // OP_CONCAT
		opmode(0, 0, OpArgR, OpArgN, iAsBx), // OP_JMP
		opmode(1, 0, OpArgK, OpArgK, iABC),  // OP_EQ
		opmode(1, 0, OpArgK, OpArgK, iABC),  // OP_LT
		opmode(1, 0, OpArgK, OpArgK, iABC),  // OP_LE
		opmode(1, 1, OpArgR, OpArgU, iABC),  // OP_TEST
		opmode(1, 1, OpArgR, OpArgU, iABC),  // OP_TESTSET
		opmode(0, 1, OpArgU, OpArgU, iABC),  // OP_CALL
		opmode(0, 1, OpArgU, OpArgU, iABC),  // OP_TAILCALL
		opmode(0, 0, OpArgU, OpArgN, iABC),  // OP_RETURN
		opmode(0, 1, OpArgR, OpArgN, iAsBx), // OP_FORLOOP
		opmode(0, 1, OpArgR, OpArgN, iAsBx), // OP_FORPREP
		opmode(1, 0, OpArgN, OpArgU, iABC),  // OP_TFORLOOP
		opmode(0, 0, OpArgU, OpArgU, iABC),  // OP_SETLIST
		opmode(0, 0, OpArgN, OpArgN, iABC),  // OP_CLOSE
		opmode(0, 1, OpArgU, OpArgN, iABx),  // OP_CLOSURE
		opmode(0, 1, OpArgU, OpArgN, iABC),  // OP_VARARG
	};

	public static int getOpMode(int m) {
		return opmodes[m] & 3;
	}

	public static int getBMode(int m) {
		return (opmodes[m] >> 4) & 3;
	}

	public static int getCMode(int m) {
		return (opmodes[m] >> 2) & 3;
	}

	public static boolean testAMode(int m) {
		return 0 != (opmodes[m] & (1 << 6));
	}

	public static boolean testTMode(int m) {
		return 0 != (opmodes[m] & (1 << 7));
	}

	/**
	 * Number of list items to accumulate before a {@link #OP_SETLIST} instruction
	 */
	public static final int LFIELDS_PER_FLUSH = 50;

	private static final String[] opcodeNames = {
		"MOVE",
		"LOADK",
		"LOADBOOL",
		"LOADNIL",
		"GETUPVAL",
		"GETGLOBAL",
		"GETTABLE",
		"SETGLOBAL",
		"SETUPVAL",
		"SETTABLE",
		"NEWTABLE",
		"SELF",
		"ADD",
		"SUB",
		"MUL",
		"DIV",
		"MOD",
		"POW",
		"UNM",
		"NOT",
		"LEN",
		"CONCAT",
		"JMP",
		"EQ",
		"LT",
		"LE",
		"TEST",
		"TESTSET",
		"CALL",
		"TAILCALL",
		"RETURN",
		"FORLOOP",
		"FORPREP",
		"TFORLOOP",
		"SETLIST",
		"CLOSE",
		"CLOSURE",
		"VARARG",
	};

	public static String getOpName(int opcode) {
		return opcodeNames[opcode];
	}
}
