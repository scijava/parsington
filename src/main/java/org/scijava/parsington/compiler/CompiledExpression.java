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

import static org.scijava.parsington.compiler.Opcodes.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * An expression compiled by an {@link ExpressionCompiler}, ready to be
 * evaluated repeatedly with different variable values.
 * <p>
 * Variables can be set by name, or more quickly by slot index via the typed
 * setters (e.g. {@link #setDouble(int, double)}). Variables which are never
 * set have the value {@code 0} or {@code false}.
 * </p>
 * <p>
 * Instances are not thread-safe, since they hold their variables' values and
 * an evaluation stack.
 * </p>
 *
 * @author Curtis Rueden
 */
public final class CompiledExpression {

	private final int[] code;
	private final long[] constants;
	private final long[] stack;
	private final String[] names;
	private final Type[] types;
	private final long[] vars;
	private final Type type;

	CompiledExpression(final int[] code, final long[] constants,
		final int maxDepth, final String[] names, final Type[] types,
		final Type type)
	{
		this.code = code;
		this.constants = constants;
		this.stack = new long[maxDepth];
		this.names = names;
		this.types = types;
		this.vars = new long[names.length];
		this.type = type;
	}

	// -- CompiledExpression methods --

	/** Gets the type of the expression's result. */
	public Type type() {
		return type;
	}

	/** Gets the names of the expression's variables, in slot order. */
	public List<String> variables() {
		return Collections.unmodifiableList(Arrays.asList(names));
	}

	/** Gets the slot of the given variable, or -1 if there is no such variable. */
	public int slot(final String name) {
		for (int i = 0; i < names.length; i++) {
			if (names[i].equals(name)) return i;
		}
		return -1;
	}

	/** Gets the type of the variable in the given slot. */
	public Type type(final int slot) {
		return types[slot];
	}

	/** Gets the value of the given variable. */
	public Object get(final String name) {
		return get(checkSlot(name));
	}

	/** Gets the value of the variable in the given slot. */
	public Object get(final int slot) {
		return box(vars[slot], types[slot]);
	}

	/**
	 * Sets the value of the given variable.
	 *
	 * @param name The name of the variable.
	 * @param value The value, which must be a {@link Boolean} for a
	 *          {@link Type#BOOLEAN} variable, an integer {@link Number} for a
	 *          {@link Type#LONG} variable, or an integer or floating point
	 *          {@link Number} for a {@link Type#DOUBLE} variable.
	 * @throws IllegalArgumentException If there is no such variable, or the
	 *           value is incompatible with the variable's type.
	 */
	public void set(final String name, final Object value) {
		set(checkSlot(name), value);
	}

	/** Sets the value of the variable in the given slot. */
	public void set(final int slot, final Object value) {
		final Type valueType = Type.of(value);
		if (valueType == null || !Compilation.isAssignable(valueType, types[slot])) {
			throw incompatible(slot, value);
		}
		switch (types[slot]) {
			case BOOLEAN:
				setBoolean(slot, (Boolean) value);
				break;
			case LONG:
				setLong(slot, ((Number) value).longValue());
				break;
			default: // DOUBLE
				setDouble(slot, ((Number) value).doubleValue());
		}
	}

	/** Sets the value of the {@link Type#BOOLEAN} variable in the given slot. */
	public void setBoolean(final int slot, final boolean value) {
		if (types[slot] != Type.BOOLEAN) throw incompatible(slot, value);
		vars[slot] = value ? 1 : 0;
	}

	/**
	 * Sets the value of the {@link Type#LONG} or {@link Type#DOUBLE} variable in
	 * the given slot.
	 */
	public void setLong(final int slot, final long value) {
		final Type t = types[slot];
		if (t == Type.LONG) vars[slot] = value;
		else if (t == Type.DOUBLE) vars[slot] = bits(value);
		else throw incompatible(slot, value);
	}

	/** Sets the value of the {@link Type#DOUBLE} variable in the given slot. */
	public void setDouble(final int slot, final double value) {
		if (types[slot] != Type.DOUBLE) throw incompatible(slot, value);
		vars[slot] = bits(value);
	}

	/**
	 * Evaluates the expression.
	 *
	 * @return The result, as a {@link Boolean}, {@link Long} or {@link Double}
	 *         according to the expression's {@link #type()}.
	 */
	public Object evaluate() {
		return box(run(), type);
	}

	/** Evaluates the expression, which must be of type {@link Type#BOOLEAN}. */
	public boolean evaluateBoolean() {
		checkType(Type.BOOLEAN);
		return run() != 0;
	}

	/** Evaluates the expression, which must be of type {@link Type#LONG}. */
	public long evaluateLong() {
		checkType(Type.LONG);
		return run();
	}

	/**
	 * Evaluates the expression, which must be of type {@link Type#LONG} or
	 * {@link Type#DOUBLE}.
	 */
	public double evaluateDouble() {
		if (type == Type.LONG) return run();
		checkType(Type.DOUBLE);
		return dbl(run());
	}

	// -- Helper methods --

	/** Executes the program, returning the result's raw bits. */
	private long run() {
		final int[] code = this.code;
		final long[] s = stack, k = constants, v = vars;
		int pc = 0, sp = 0;
		while (true) {
			switch (code[pc++]) {
				case CONST: s[sp++] = k[code[pc++]]; break;
				case LOAD: s[sp++] = v[code[pc++]]; break;
				case STORE: v[code[pc++]] = s[sp - 1]; break;
				case POP: sp--; break;
				case L2D: s[sp - 1] = bits((double) s[sp - 1]); break;

				case LADD: sp--; s[sp - 1] += s[sp]; break;
				case LSUB: sp--; s[sp - 1] -= s[sp]; break;
				case LMUL: sp--; s[sp - 1] *= s[sp]; break;
				case LDIV: sp--; s[sp - 1] /= s[sp]; break;
				case LMOD: sp--; s[sp - 1] %= s[sp]; break;
				case LNEG: s[sp - 1] = -s[sp - 1]; break;
				case DADD: sp--; s[sp - 1] = bits(dbl(s[sp - 1]) + dbl(s[sp])); break;
				case DSUB: sp--; s[sp - 1] = bits(dbl(s[sp - 1]) - dbl(s[sp])); break;
				case DMUL: sp--; s[sp - 1] = bits(dbl(s[sp - 1]) * dbl(s[sp])); break;
				case DDIV: sp--; s[sp - 1] = bits(dbl(s[sp - 1]) / dbl(s[sp])); break;
				case DMOD: sp--; s[sp - 1] = bits(dbl(s[sp - 1]) % dbl(s[sp])); break;
				case DNEG: s[sp - 1] = bits(-dbl(s[sp - 1])); break;
				case DPOW: sp--; s[sp - 1] = bits(Math.pow(dbl(s[sp - 1]), dbl(s[sp]))); break;

				case LSHL: sp--; s[sp - 1] <<= s[sp]; break;
				case LSHR: sp--; s[sp - 1] >>= s[sp]; break;
				case LUSHR: sp--; s[sp - 1] >>>= s[sp]; break;
				case LAND: sp--; s[sp - 1] &= s[sp]; break;
				case LOR: sp--; s[sp - 1] |= s[sp]; break;
				case LCOMPL: s[sp - 1] = ~s[sp - 1]; break;
				case BNOT: s[sp - 1] ^= 1; break;

				case LLT: sp--; s[sp - 1] = s[sp - 1] < s[sp] ? 1 : 0; break;
				case LGT: sp--; s[sp - 1] = s[sp - 1] > s[sp] ? 1 : 0; break;
				case LLE: sp--; s[sp - 1] = s[sp - 1] <= s[sp] ? 1 : 0; break;
				case LGE: sp--; s[sp - 1] = s[sp - 1] >= s[sp] ? 1 : 0; break;
				case LEQ: sp--; s[sp - 1] = s[sp - 1] == s[sp] ? 1 : 0; break;
				case LNE: sp--; s[sp - 1] = s[sp - 1] != s[sp] ? 1 : 0; break;
				case DLT: sp--; s[sp - 1] = dbl(s[sp - 1]) < dbl(s[sp]) ? 1 : 0; break;
				case DGT: sp--; s[sp - 1] = dbl(s[sp - 1]) > dbl(s[sp]) ? 1 : 0; break;
				case DLE: sp--; s[sp - 1] = dbl(s[sp - 1]) <= dbl(s[sp]) ? 1 : 0; break;
				case DGE: sp--; s[sp - 1] = dbl(s[sp - 1]) >= dbl(s[sp]) ? 1 : 0; break;
				case DEQ: sp--; s[sp - 1] = dbl(s[sp - 1]) == dbl(s[sp]) ? 1 : 0; break;
				case DNE: sp--; s[sp - 1] = dbl(s[sp - 1]) != dbl(s[sp]) ? 1 : 0; break;

				case JMP: pc = code[pc]; break;
				case JZ: pc = s[--sp] == 0 ? code[pc] : pc + 1; break;
				case JNZ: pc = s[--sp] != 0 ? code[pc] : pc + 1; break;
				case RETURN: return s[sp - 1];

				default:
					throw new IllegalStateException("Invalid instruction: " +
						code[pc - 1]);
			}
		}
	}

	private static long bits(final double value) {
		return Double.doubleToRawLongBits(value);
	}

	private static double dbl(final long bits) {
		return Double.longBitsToDouble(bits);
	}

	private static Object box(final long value, final Type type) {
		switch (type) {
			case BOOLEAN: return value != 0;
			case LONG: return value;
			default: return dbl(value);
		}
	}

	private int checkSlot(final String name) {
		final int slot = slot(name);
		if (slot < 0) throw new IllegalArgumentException("Unknown variable: " + name);
		return slot;
	}

	private void checkType(final Type expected) {
		if (type != expected) {
			throw new IllegalStateException("Expression is of type " + type);
		}
	}

	private IllegalArgumentException incompatible(final int slot,
		final Object value)
	{
		return new IllegalArgumentException("Cannot assign " + value + " to " +
			types[slot] + " variable " + names[slot]);
	}
}
