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

import static org.scijava.parsington.compiler.Opcodes.CONST;
import static org.scijava.parsington.compiler.Opcodes.L2D;
import static org.scijava.parsington.compiler.Opcodes.LOAD;
import static org.scijava.parsington.compiler.Opcodes.RETURN;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import org.scijava.parsington.Function;
import org.scijava.parsington.Group;
import org.scijava.parsington.Operator;
import org.scijava.parsington.Operators;
import org.scijava.parsington.SyntaxTree;
import org.scijava.parsington.Tokens;
import org.scijava.parsington.Variable;

/**
 * State of a single compilation: variable declarations, inferred types, and
 * the instructions emitted so far.
 *
 * @author Curtis Rueden
 */
final class Compilation {

	private final Map<Operator, Rule> rules;

	private final Map<SyntaxTree, Type> types = new IdentityHashMap<>();
	private final Map<String, Integer> slots = new HashMap<>();
	private final List<String> varNames = new ArrayList<>();
	private final List<Type> varTypes = new ArrayList<>();

	private int[] code = new int[32];
	private int pc;
	private long[] constants = new long[8];
	private int constantCount;
	private int depth, maxDepth;

	Compilation(final Map<Operator, Rule> rules, final Map<String, Type> vars) {
		this.rules = rules;
		for (final Map.Entry<String, Type> var : vars.entrySet()) {
			declare(var.getKey(), var.getValue());
		}
	}

	/** Compiles the given tree into a finished program. */
	CompiledExpression compile(final SyntaxTree tree) {
		final Type type = type(tree);
		emit(tree, type);
		op(RETURN);
		return new CompiledExpression(Arrays.copyOf(code, pc), //
			Arrays.copyOf(constants, constantCount), maxDepth, //
			varNames.toArray(new String[0]), varTypes.toArray(new Type[0]), type);
	}

	// -- Typing --

	/** Gets the result type of the given tree, inferring it if needed. */
	Type type(final SyntaxTree tree) {
		Type type = types.get(tree);
		if (type == null) {
			type = inferType(tree);
			types.put(tree, type);
		}
		return type;
	}

	/** Gets the result type of the given tree, which must be numeric. */
	Type numericType(final SyntaxTree tree) {
		final Type type = type(tree);
		if (!type.isNumeric()) throw unsupported(tree, type);
		return type;
	}

	/**
	 * Gets the common numeric type of the given trees: {@link Type#DOUBLE} if
	 * either is a double, or {@link Type#LONG} otherwise.
	 */
	Type numericType(final SyntaxTree a, final SyntaxTree b) {
		final Type ta = numericType(a), tb = numericType(b);
		return ta == Type.DOUBLE || tb == Type.DOUBLE ? Type.DOUBLE : Type.LONG;
	}

	/** Gets whether a value of type {@code from} can be stored as type {@code to}. */
	static boolean isAssignable(final Type from, final Type to) {
		return from == to || from == Type.LONG && to == Type.DOUBLE;
	}

	/** Creates an exception describing an operand of an unsupported type. */
	static IllegalArgumentException unsupported(final SyntaxTree tree,
		final Type type)
	{
		return new IllegalArgumentException("Unsupported " + type + " operand: " +
			tree.token());
	}

	// -- Variables --

	/** Gets the name of the variable at the root of the given tree. */
	static String variableName(final SyntaxTree tree) {
		if (!Tokens.isVariable(tree.token())) {
			throw new IllegalArgumentException("Not a variable: " + tree.token());
		}
		return ((Variable) tree.token()).getToken();
	}

	/** Gets the slot of the given variable, or -1 if it is undeclared. */
	int slot(final String name) {
		final Integer slot = slots.get(name);
		return slot == null ? -1 : slot;
	}

	/** Gets the declared type of the given variable. */
	Type variableType(final String name) {
		final int slot = slot(name);
		if (slot < 0) throw new IllegalArgumentException("Unknown variable: " + name);
		return varTypes.get(slot);
	}

	/** Declares a new variable of the given type, returning its slot. */
	int declare(final String name, final Type type) {
		if (type == null) {
			throw new IllegalArgumentException("No type for variable: " + name);
		}
		final int slot = varNames.size();
		slots.put(name, slot);
		varNames.add(name);
		varTypes.add(type);
		return slot;
	}

	// -- Code generation --

	/** Emits instructions which leave the tree's result on the stack as the given type. */
	void emit(final SyntaxTree tree, final Type as) {
		final Type type = type(tree);
		final Object token = tree.token();
		if (isParens(tree)) {
			emit(tree.child(0), as);
			return;
		}
		if (Tokens.isVariable(token)) {
			op(LOAD, slot(((Variable) token).getToken()));
		}
		else if (token instanceof Operator) {
			rules.get(token).emit(this, tree, type);
		}
		else {
			// NB: A literal, which can be converted at compile time.
			if (!isAssignable(type, as)) convert(type, as); // NB: Throws.
			op(CONST, constant(as, token));
			return;
		}
		convert(type, as);
	}

	/** Emits an instruction converting the top value from one type to another. */
	void convert(final Type from, final Type to) {
		if (from == to) return;
		if (from == Type.LONG && to == Type.DOUBLE) op(L2D);
		else throw new IllegalArgumentException("Cannot convert " + from +
			" to " + to);
	}

	/** Emits an instruction without an operand. */
	void op(final int op) {
		ensureCapacity(1);
		code[pc++] = op;
		adjustDepth(Opcodes.stackDelta(op));
	}

	/** Emits an instruction with an operand. */
	void op(final int op, final int operand) {
		ensureCapacity(2);
		code[pc++] = op;
		code[pc++] = operand;
		adjustDepth(Opcodes.stackDelta(op));
	}

	/**
	 * Emits a jump instruction whose target is not yet known.
	 *
	 * @return The location of the target, to be passed to {@link #land}.
	 */
	int jump(final int op) {
		op(op, -1);
		return pc - 1;
	}

	/** Sets the target of a previously emitted jump to the current location. */
	void land(final int jump) {
		code[jump] = pc;
	}

	/**
	 * Adds a value to the constant pool.
	 *
	 * @param type The type as which the value should be stored.
	 * @param value The value, a {@link Boolean} or {@link Number}.
	 * @return The index of the constant, for use with {@link Opcodes#CONST}.
	 */
	int constant(final Type type, final Object value) {
		final long bits;
		if (value instanceof Boolean) bits = (Boolean) value ? 1 : 0;
		else if (type == Type.DOUBLE) {
			bits = Double.doubleToRawLongBits(((Number) value).doubleValue());
		}
		else bits = ((Number) value).longValue();
		for (int i = 0; i < constantCount; i++) {
			if (constants[i] == bits) return i;
		}
		if (constantCount == constants.length) {
			constants = Arrays.copyOf(constants, 2 * constants.length);
		}
		constants[constantCount] = bits;
		return constantCount++;
	}

	/** Gets the current stack depth. */
	int depth() {
		return depth;
	}

	/**
	 * Sets the current stack depth. Needed when two branches of code both
	 * leave a value on the stack, but only one of them will execute.
	 */
	void depth(final int depth) {
		this.depth = depth;
	}

	// -- Helper methods --

	private Type inferType(final SyntaxTree tree) {
		final Object token = tree.token();
		if (isParens(tree)) return type(tree.child(0));
		if (Tokens.isVariable(token)) {
			return variableType(((Variable) token).getToken());
		}
		if (token instanceof Function) {
			throw new IllegalArgumentException("Functions are not supported");
		}
		if (token instanceof Group) {
			throw new IllegalArgumentException("Unsupported group: " + token);
		}
		if (token instanceof Operator) {
			final Rule rule = rules.get(token);
			if (rule == null) {
				throw new IllegalArgumentException("Unsupported operator: " + token);
			}
			return rule.type(this, tree);
		}
		final Type type = Type.of(token);
		if (type == null) {
			throw new IllegalArgumentException("Unsupported literal: " + token);
		}
		return type;
	}

	private static boolean isParens(final SyntaxTree tree) {
		return Tokens.isMatchingGroup(tree.token(), Operators.PARENS) &&
			tree.count() == 1;
	}

	private void ensureCapacity(final int count) {
		if (pc + count > code.length) code = Arrays.copyOf(code, 2 * code.length);
	}

	private void adjustDepth(final int delta) {
		depth += delta;
		if (depth > maxDepth) maxDepth = depth;
	}
}
