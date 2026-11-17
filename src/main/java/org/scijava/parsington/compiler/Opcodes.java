/*
 * #%L
 * Parsington: the SciJava mathematical expression parser.
 * %%
 * Copyright (C) 2015 - 2026 Parsington developers.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

package org.scijava.parsington.compiler;

/**
 * Instruction set for {@link CompiledExpression} programs.
 * <p>
 * Programs operate on a stack of {@code long} values. Booleans are stored as 0
 * or 1, and doubles as their raw IEEE 754 bits. An instruction prefixed with
 * {@code L} operates on longs, {@code D} on doubles and {@code B} on booleans.
 * Comparisons push a boolean. Instructions marked with an operand are followed
 * in the program by a single {@code int} argument.
 * </p>
 *
 * @author Curtis Rueden
 */
final class Opcodes {

	// -- stack and variables --

	/** Pushes a constant. Operand: constant index. */
	static final int CONST = 0;
	/** Pushes a variable's value. Operand: variable slot. */
	static final int LOAD = 1;
	/** Stores the top value into a variable, leaving it on the stack. Operand: variable slot. */
	static final int STORE = 2;
	/** Discards the top value. */
	static final int POP = 3;
	/** Converts the top value from long to double. */
	static final int L2D = 4;

	// -- arithmetic --

	static final int LADD = 5;
	static final int LSUB = 6;
	static final int LMUL = 7;
	static final int LDIV = 8;
	static final int LMOD = 9;
	static final int LNEG = 10;
	static final int DADD = 11;
	static final int DSUB = 12;
	static final int DMUL = 13;
	static final int DDIV = 14;
	static final int DMOD = 15;
	static final int DNEG = 16;
	static final int DPOW = 17;

	// -- bitwise --

	static final int LSHL = 18;
	static final int LSHR = 19;
	static final int LUSHR = 20;
	static final int LAND = 21;
	static final int LOR = 22;
	static final int LCOMPL = 23;
	static final int BNOT = 24;

	// -- comparison --

	static final int LLT = 25;
	static final int LGT = 26;
	static final int LLE = 27;
	static final int LGE = 28;
	static final int LEQ = 29;
	static final int LNE = 30;
	static final int DLT = 31;
	static final int DGT = 32;
	static final int DLE = 33;
	static final int DGE = 34;
	static final int DEQ = 35;
	static final int DNE = 36;

	// -- control flow --

	/** Jumps unconditionally. Operand: target address. */
	static final int JMP = 37;
	/** Pops a boolean, jumping if it is false. Operand: target address. */
	static final int JZ = 38;
	/** Pops a boolean, jumping if it is true. Operand: target address. */
	static final int JNZ = 39;
	/** Ends the program, yielding the top value. */
	static final int RETURN = 40;

	private Opcodes() {
		// NB: Prevent instantiation of utility class.
	}

	/** Gets whether the given instruction is followed by an operand. */
	static boolean hasOperand(final int op) {
		switch (op) {
			case CONST: case LOAD: case STORE: case JMP: case JZ: case JNZ:
				return true;
			default:
				return false;
		}
	}

	/** Gets the change in stack depth caused by the given instruction. */
	static int stackDelta(final int op) {
		switch (op) {
			case CONST: case LOAD:
				return 1;
			case STORE: case L2D: case LNEG: case DNEG: case LCOMPL: case BNOT:
			case JMP: case RETURN:
				return 0;
			default:
				// NB: POP, JZ, JNZ, and all binary operations.
				return -1;
		}
	}
}
