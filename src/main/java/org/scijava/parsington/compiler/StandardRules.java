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

import java.util.IdentityHashMap;
import java.util.Map;

import org.scijava.parsington.Operator;
import org.scijava.parsington.Operators;
import org.scijava.parsington.compiler.Compilation.Node;

/**
 * {@link Rule}s for the {@link Operators standard operators}.
 * <p>
 * Semantics follow Java, except that all integers are {@code long}s. Operators
 * without a sensible meaning for booleans, longs and doubles (e.g. the dot
 * operators, {@code instanceof}) are left unsupported.
 * </p>
 *
 * @author Curtis Rueden
 */
final class StandardRules {

	private StandardRules() {
		// NB: Prevent instantiation of utility class.
	}

	/** Creates a new table of rules for the standard operators. */
	static Map<Operator, Rule> create() {
		final Map<Operator, Rule> rules = new IdentityHashMap<>();

		// Arithmetic and bitwise binary operators, with compound assignments.
		final Arith pow = new Arith(Kind.POW, -1, DPOW);
		final Arith mul = new Arith(Kind.NUMERIC, LMUL, DMUL);
		final Arith div = new Arith(Kind.NUMERIC, LDIV, DDIV);
		final Arith mod = new Arith(Kind.NUMERIC, LMOD, DMOD);
		final Arith add = new Arith(Kind.NUMERIC, LADD, DADD);
		final Arith sub = new Arith(Kind.NUMERIC, LSUB, DSUB);
		final Arith shl = new Arith(Kind.INTEGER, LSHL, -1);
		final Arith shr = new Arith(Kind.INTEGER, LSHR, -1);
		final Arith ushr = new Arith(Kind.INTEGER, LUSHR, -1);
		final Arith and = new Arith(Kind.BITWISE, LAND, -1);
		final Arith or = new Arith(Kind.BITWISE, LOR, -1);
		binary(rules, Operators.POW, Operators.POW_ASSIGN, pow);
		binary(rules, Operators.MUL, Operators.MUL_ASSIGN, mul);
		binary(rules, Operators.DIV, Operators.DIV_ASSIGN, div);
		binary(rules, Operators.MOD, Operators.MOD_ASSIGN, mod);
		binary(rules, Operators.ADD, Operators.ADD_ASSIGN, add);
		binary(rules, Operators.SUB, Operators.SUB_ASSIGN, sub);
		binary(rules, Operators.LEFT_SHIFT, Operators.LEFT_SHIFT_ASSIGN, shl);
		binary(rules, Operators.RIGHT_SHIFT, Operators.RIGHT_SHIFT_ASSIGN, shr);
		binary(rules, Operators.UNSIGNED_RIGHT_SHIFT,
			Operators.UNSIGNED_RIGHT_SHIFT_ASSIGN, ushr);
		binary(rules, Operators.BITWISE_AND, Operators.AND_ASSIGN, and);
		binary(rules, Operators.BITWISE_OR, Operators.OR_ASSIGN, or);

		// Comparisons.
		rules.put(Operators.LESS_THAN, new Comparison(LLT, DLT));
		rules.put(Operators.GREATER_THAN, new Comparison(LGT, DGT));
		rules.put(Operators.LESS_THAN_OR_EQUAL, new Comparison(LLE, DLE));
		rules.put(Operators.GREATER_THAN_OR_EQUAL, new Comparison(LGE, DGE));
		rules.put(Operators.EQUAL, new Comparison(LEQ, DEQ));
		rules.put(Operators.NOT_EQUAL, new Comparison(LNE, DNE));

		// Unary operators.
		rules.put(Operators.POS, new Unary(Type.LONG, -1, Type.DOUBLE, -1));
		rules.put(Operators.NEG, new Unary(Type.LONG, LNEG, Type.DOUBLE, DNEG));
		rules.put(Operators.COMPLEMENT, new Unary(Type.LONG, LCOMPL, null, -1));
		rules.put(Operators.NOT, new Unary(Type.BOOLEAN, BNOT, null, -1));
		rules.put(Operators.PRE_INC, new Step(add, false));
		rules.put(Operators.PRE_DEC, new Step(sub, false));
		rules.put(Operators.POST_INC, new Step(add, true));
		rules.put(Operators.POST_DEC, new Step(sub, true));

		// Control flow and assignment.
		rules.put(Operators.LOGICAL_AND, new Logical(true));
		rules.put(Operators.LOGICAL_OR, new Logical(false));
		rules.put(Operators.QUESTION, new Ternary());
		rules.put(Operators.ASSIGN, new Assign());

		return rules;
	}

	// -- Helper methods --

	private static void binary(final Map<Operator, Rule> rules,
		final Operator op, final Operator assignOp, final Arith arith)
	{
		rules.put(op, new Binary(arith));
		rules.put(assignOp, new CompoundAssign(arith));
	}

	private static boolean isNumeric(final Type a, final Type b) {
		return a.isNumeric() && b.isNumeric();
	}

	private static Type promote(final Type a, final Type b) {
		return a == Type.DOUBLE || b == Type.DOUBLE ? Type.DOUBLE : Type.LONG;
	}

	// -- Helper classes --

	/** Categories of binary arithmetic, determining the supported types. */
	private enum Kind {
		/** Longs or doubles, promoting to double if mixed. */
		NUMERIC,
		/** Longs or doubles, always computed as doubles. */
		POW,
		/** Longs only. */
		INTEGER,
		/** Longs, or booleans (without short-circuiting, as in Java). */
		BITWISE
	}

	/** A binary arithmetic operation, shared by plain and compound assignment forms. */
	private static final class Arith {

		private final Kind kind;
		private final int longOp, doubleOp;

		Arith(final Kind kind, final int longOp, final int doubleOp) {
			this.kind = kind;
			this.longOp = longOp;
			this.doubleOp = doubleOp;
		}

		/**
		 * Gets the type in which the operation is computed, which is also its
		 * result type, or null if the operand types are not supported.
		 */
		Type type(final Type a, final Type b) {
			switch (kind) {
				case NUMERIC:
					return isNumeric(a, b) ? promote(a, b) : null;
				case POW:
					return isNumeric(a, b) ? Type.DOUBLE : null;
				case INTEGER:
					return a == Type.LONG && b == Type.LONG ? Type.LONG : null;
				default: // BITWISE
					return a == b && a != Type.DOUBLE ? a : null;
			}
		}

		int op(final Type type) {
			return type == Type.DOUBLE ? doubleOp : longOp;
		}
	}

	/** Rule for binary arithmetic and bitwise operators. */
	private static final class Binary implements Rule {

		private final Arith arith;

		Binary(final Arith arith) {
			this.arith = arith;
		}

		@Override
		public Type type(final Compilation c, final Node tree) {
			final Type a = c.type(tree.child(0)), b = c.type(tree.child(1));
			final Type type = arith.type(a, b);
			if (type == null) throw unsupported(tree, a, b);
			return type;
		}

		@Override
		public void emit(final Compilation c, final Node tree,
			final Type type)
		{
			c.emit(tree.child(0), type);
			c.emit(tree.child(1), type);
			c.op(arith.op(type));
		}
	}

	/** Rule for compound assignment operators, such as {@code +=}. */
	private static final class CompoundAssign implements Rule {

		private final Arith arith;

		CompoundAssign(final Arith arith) {
			this.arith = arith;
		}

		@Override
		public Type type(final Compilation c, final Node tree) {
			final Type var = c.variableType(Compilation.variableName(tree.child(0)));
			final Type b = c.type(tree.child(1));
			final Type type = arith.type(var, b);
			if (type == null) throw unsupported(tree, var, b);
			checkAssignable(tree, type, var);
			return var;
		}

		@Override
		public void emit(final Compilation c, final Node tree,
			final Type var)
		{
			final int slot = c.slot(Compilation.variableName(tree.child(0)));
			final Type type = arith.type(var, c.type(tree.child(1)));
			c.op(LOAD, slot);
			c.convert(var, type);
			c.emit(tree.child(1), type);
			c.op(arith.op(type));
			c.convert(type, var);
			c.op(STORE, slot);
		}
	}

	/** Rule for comparison operators. */
	private static final class Comparison implements Rule {

		private final int longOp, doubleOp;

		Comparison(final int longOp, final int doubleOp) {
			this.longOp = longOp;
			this.doubleOp = doubleOp;
		}

		@Override
		public Type type(final Compilation c, final Node tree) {
			final Type a = c.type(tree.child(0)), b = c.type(tree.child(1));
			// NB: Booleans compare as 0 and 1, consistent with Boolean#compareTo.
			if (a != b && !isNumeric(a, b)) throw unsupported(tree, a, b);
			return Type.BOOLEAN;
		}

		@Override
		public void emit(final Compilation c, final Node tree,
			final Type type)
		{
			final Type a = c.type(tree.child(0)), b = c.type(tree.child(1));
			final Type operands = a == Type.BOOLEAN ? Type.BOOLEAN : promote(a, b);
			c.emit(tree.child(0), operands);
			c.emit(tree.child(1), operands);
			c.op(operands == Type.DOUBLE ? doubleOp : longOp);
		}
	}

	/** Rule for unary operators. An instruction of -1 means no operation. */
	private static final class Unary implements Rule {

		private final Type type1, type2;
		private final int op1, op2;

		Unary(final Type type1, final int op1, final Type type2, final int op2) {
			this.type1 = type1;
			this.op1 = op1;
			this.type2 = type2;
			this.op2 = op2;
		}

		@Override
		public Type type(final Compilation c, final Node tree) {
			final Type type = c.type(tree.child(0));
			if (type != type1 && type != type2) {
				throw Compilation.unsupported(tree, type);
			}
			return type;
		}

		@Override
		public void emit(final Compilation c, final Node tree,
			final Type type)
		{
			c.emit(tree.child(0), type);
			final int op = type == type1 ? op1 : op2;
			if (op >= 0) c.op(op);
		}
	}

	/** Rule for increment and decrement operators. */
	private static final class Step implements Rule {

		private final Arith arith;
		private final boolean postfix;

		Step(final Arith arith, final boolean postfix) {
			this.arith = arith;
			this.postfix = postfix;
		}

		@Override
		public Type type(final Compilation c, final Node tree) {
			final Type type = c.variableType(Compilation.variableName(tree.child(0)));
			if (!type.isNumeric()) throw Compilation.unsupported(tree, type);
			return type;
		}

		@Override
		public void emit(final Compilation c, final Node tree,
			final Type type)
		{
			final int slot = c.slot(Compilation.variableName(tree.child(0)));
			if (postfix) c.op(LOAD, slot); // NB: Retain the original value.
			c.op(LOAD, slot);
			c.op(CONST, c.constant(type, 1));
			c.op(arith.op(type));
			c.op(STORE, slot);
			if (postfix) c.op(POP);
		}
	}

	/** Rule for the short-circuiting logical operators. */
	private static final class Logical implements Rule {

		private final boolean and;

		Logical(final boolean and) {
			this.and = and;
		}

		@Override
		public Type type(final Compilation c, final Node tree) {
			final Type a = c.type(tree.child(0)), b = c.type(tree.child(1));
			if (a != Type.BOOLEAN || b != Type.BOOLEAN) throw unsupported(tree, a, b);
			return Type.BOOLEAN;
		}

		@Override
		public void emit(final Compilation c, final Node tree,
			final Type type)
		{
			// a && b: if (!a) false else b
			// a || b: if (a) true else b
			c.emit(tree.child(0), Type.BOOLEAN);
			final int shortCircuit = c.jump(and ? JZ : JNZ);
			final int depth = c.depth();
			c.emit(tree.child(1), Type.BOOLEAN);
			final int end = c.jump(JMP);
			c.land(shortCircuit);
			c.depth(depth);
			c.op(CONST, c.constant(Type.BOOLEAN, and ? 0 : 1));
			c.land(end);
		}
	}

	/** Rule for the ternary operator, which parses as {@code a ? (b : c)}. */
	private static final class Ternary implements Rule {

		@Override
		public Type type(final Compilation c, final Node tree) {
			final Node branches = tree.child(1);
			if (branches.token() != Operators.COLON) {
				throw new IllegalArgumentException("Invalid ternary operator syntax");
			}
			final Type condition = c.type(tree.child(0));
			if (condition != Type.BOOLEAN) {
				throw Compilation.unsupported(tree, condition);
			}
			final Type a = c.type(branches.child(0)), b = c.type(branches.child(1));
			if (a == b) return a;
			if (isNumeric(a, b)) return promote(a, b);
			throw unsupported(tree, a, b);
		}

		@Override
		public void emit(final Compilation c, final Node tree,
			final Type type)
		{
			final Node branches = tree.child(1);
			c.emit(tree.child(0), Type.BOOLEAN);
			final int otherwise = c.jump(JZ);
			final int depth = c.depth();
			c.emit(branches.child(0), type);
			final int end = c.jump(JMP);
			c.land(otherwise);
			c.depth(depth);
			c.emit(branches.child(1), type);
			c.land(end);
		}
	}

	/**
	 * Rule for simple assignment. Assigning to an undeclared variable declares
	 * it, with the type of the assigned value.
	 */
	private static final class Assign implements Rule {

		@Override
		public Type type(final Compilation c, final Node tree) {
			final String name = Compilation.variableName(tree.child(0));
			final Type value = c.type(tree.child(1));
			if (c.slot(name) < 0) c.declare(name, value);
			final Type var = c.variableType(name);
			checkAssignable(tree, value, var);
			return var;
		}

		@Override
		public void emit(final Compilation c, final Node tree,
			final Type var)
		{
			c.emit(tree.child(1), var);
			c.op(STORE, c.slot(Compilation.variableName(tree.child(0))));
		}
	}

	private static void checkAssignable(final Node tree, final Type from,
		final Type to)
	{
		if (Compilation.isAssignable(from, to)) return;
		throw new IllegalArgumentException("Cannot assign " + from + " to " + to +
			" variable via " + tree.token());
	}

	private static IllegalArgumentException unsupported(final Node tree,
		final Type a, final Type b)
	{
		return new IllegalArgumentException("Unsupported operand types for " +
			tree.token() + ": " + a + ", " + b);
	}
}
