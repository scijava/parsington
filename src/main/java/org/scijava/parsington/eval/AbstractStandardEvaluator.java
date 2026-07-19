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

package org.scijava.parsington.eval;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.scijava.parsington.ExpressionParser;
import org.scijava.parsington.Literals;
import org.scijava.parsington.Tokens;
import org.scijava.parsington.Variable;

/**
 * Base class for {@link StandardEvaluator} implementations on <em>common
 * built-in types</em>: {@link Boolean}s, {@link String}s and {@link Number}s.
 * <p>
 * This class is a big bag of case logic for various operators and types.
 * Looking at it, you might think: "It sure would be nice to modularize this,
 * with each operation in its own class, with properly declared types, and
 * called dynamically at runtime as appropriate."
 * </p>
 * <p>
 * "Great idea!" I would reply. Then I would suggest you have a look at the
 * <a href="https://github.com/scijava/scijava">SciJava Ops</a> and
 * <a href="https://github.com/imagej/imagej-ops">ImageJ Ops</a> projects, which
 * do exactly that in an extensible way.
 * </p>
 * <p>
 * Or maybe you are thinking: "This can't possibly work as well as awesome
 * JVM-based scripting languages like
 * <a href="https://www.jython.org/">Jython</a> and
 * <a href="https://groovy.apache.org/">Groovy</a>..."
 * </p>
 * <p>
 * To which I would reply: "You are absolutely right! This class is mostly just
 * a demonstration of an extensible, working evaluator built using the
 * {@link org.scijava.parsington.eval} package. If your use case is only
 * concerned with feature-rich evaluation of standard types, then building on
 * top of a scripting language might make more sense."
 * </p>
 *
 * @author Curtis Rueden
 */
public abstract class AbstractStandardEvaluator extends AbstractEvaluator
	implements StandardEvaluator
{

	public AbstractStandardEvaluator() {
		super();
	}

	public AbstractStandardEvaluator(final ExpressionParser parser) {
		super(parser);
	}

	// -- StandardEvaluator methods --

	// Naming convention:
	//
	// - A variable named a or b represents a Parsington object, which
	//   might be a Parsington Variable, or might already be a value of
	//   a supported type such as numeric or boolean primitive or
	//   boxed object, or other object type such as String or List.
	//
	// - A variable named v or av or bv always represents a *value*,
	//   i.e. the result of a value(Object) call on a Parsington object.

	// -- function --

	@Override
	public Object function(final Object a, final Object b) {
		final Object element = listElement(a, b);
		if (element != null) return element;

		if (Tokens.isVariable(a)) {
			final String name = ((Variable) a).getToken();
			final Object result = callFunction(name, b);
			if (result != null) return result;
		}

		// NB: Unknown function type.
		throw new UnsupportedOperationException("function " + a + " was not found");
	}

	// -- dot --

	@Override
	public Object dot(final Object a, final Object b) {
		return throwUnimplementedOp("dot");
	}

	// -- groups --

	@Override
	public Object parens(final Object... args) {
		if (args.length == 1) return args[0];
		return Arrays.asList(args);
	}

	@Override
	public Object brackets(final Object... args) {
		return Arrays.asList(args);
	}

	@Override
	public Object braces(final Object... args) {
		return Arrays.asList(args);
	}

	// -- transpose, power --

	@Override
	public Object transpose(final Object a) {
		return throwUnimplementedOp("transpose");
	}

	@Override
	public Object dotTranspose(final Object a) {
		return throwUnimplementedOp("dotTranspose");
	}

	@Override
	public Object pow(final Object a, final Object b) {
		final Object av = value(a), bv = value(b);
		if (isD(av) && isD(bv)) return pow(d(av), d(bv));
		if (isBI(av) && isI(bv)) return pow(bi(av), i(bv));
		if (isBD(av) && isI(bv)) return pow(bd(av), i(bv));
		return throwUnsupportedTypes("pow", av, bv);
	}
	public double pow(final double av, final double bv) { return Math.pow(av, bv); }
	public BigInteger pow(final BigInteger av, final int bv) { return av.pow(bv); }
	public BigDecimal pow(final BigDecimal av, final int bv) { return av.pow(bv); }

	@Override
	public Object dotPow(final Object a, final Object b) {
		return throwUnimplementedOp("dotPow");
	}

	// -- unary --

	@Override
	public Object pos(final Object a) {
		final Object av = value(a);
		if (isI(av)) return pos(i(av));
		if (isL(av)) return pos(l(av));
		if (isF(av)) return pos(f(av));
		if (isD(av)) return pos(d(av));
		return value(a);
	}
	public int pos(final int num) { return +num; }
	public long pos(final long num) { return +num; }
	public float pos(final float num) { return +num; }
	public double pos(final double num) { return +num; }

	@Override
	public Object neg(final Object a) {
		final Object av = value(a);
		if (isI(av)) return neg(i(av));
		if (isL(av)) return neg(l(av));
		if (isF(av)) return neg(f(av));
		if (isD(av)) return neg(d(av));
		if (isBI(av)) return neg(bi(av));
		if (isBD(av)) return neg(bd(av));
		return sub(0, a); // Non-numeric type: punt to subtraction.
	}
	public int neg(final int num) { return -num; }
	public long neg(final long num) { return -num; }
	public float neg(final float num) { return -num; }
	public double neg(final double num) { return -num; }
	public BigInteger neg(final BigInteger num) { return num.negate(); }
	public BigDecimal neg(final BigDecimal num) { return num.negate(); }

	@Override
	public Object complement(final Object a) {
		final Object av = value(a);
		if (isI(av)) return complement(i(av));
		if (isL(av)) return complement(l(av));
		return throwUnsupportedTypes("complement", av);
	}
	public int complement(final int a) { return ~a; }
	public long complement(final long a) { return ~a; }

	@Override
	public Object not(final Object a) { return not(bool(value(a))); }
	public boolean not(final boolean a) { return !a; }

	// -- multiplicative --

	@Override
	public Object mul(final Object a, final Object b) {
		final Object av = value(a), bv = value(b);
		if (isI(av) && isI(bv)) return mul(i(av), i(bv));
		if (isL(av) && isL(bv)) return mul(l(av), l(bv));
		if (isF(av) && isF(bv)) return mul(f(av), f(bv));
		if (isD(av) && isD(bv)) return mul(d(av), d(bv));
		if (isBI(av) && isBI(bv)) return mul(bi(av), bi(bv));
		if (isBD(av) && isBD(bv)) return mul(bd(av), bd(bv));
		return throwUnsupportedTypes("mul", av, bv);
	}
	public int mul(final int av, final int bv) { return av * bv; }
	public long mul(final long av, final long bv) { return av * bv; }
	public float mul(final float av, final float bv) { return av * bv; }
	public double mul(final double av, final double bv) { return av * bv; }
	public BigInteger mul(final BigInteger av, final BigInteger bv) {
		return av.multiply(bv);
	}
	public BigDecimal mul(final BigDecimal av, final BigDecimal bv) {
		return av.multiply(bv);
	}

	@Override
	public Object div(final Object a, final Object b) {
		final Object av = value(a), bv = value(b);
		if (isI(av) && isI(bv)) return div(i(av), i(bv));
		if (isL(av) && isL(bv)) return div(l(av), l(bv));
		if (isF(av) && isF(bv)) return div(f(av), f(bv));
		if (isD(av) && isD(bv)) return div(d(av), d(bv));
		if (isBI(av) && isBI(bv)) return div(bi(av), bi(bv));
		if (isBD(av) && isBD(bv)) return div(bd(av), bd(bv));
		return throwUnsupportedTypes("div", av, bv);
	}
	public int div(final int av, final int bv) { return av / bv; }
	public long div(final long av, final long bv) { return av / bv; }
	public float div(final float av, final float bv) { return av / bv; }
	public double div(final double av, final double bv) { return av / bv; }
	public BigInteger div(final BigInteger av, final BigInteger bv) {
		return av.divide(bv);
	}
	public BigDecimal div(final BigDecimal av, final BigDecimal bv) {
		return av.divide(bv);
	}

	@Override
	public Object mod(final Object a, final Object b) {
		final Object av = value(a), bv = value(b);
		if (isI(av) && isI(bv)) return mod(i(av), i(bv));
		if (isL(av) && isL(bv)) return mod(l(av), l(bv));
		if (isF(av) && isF(bv)) return mod(f(av), f(bv));
		if (isD(av) && isD(bv)) return mod(d(av), d(bv));
		if (isBI(av) && isBI(bv)) return mod(bi(av), bi(bv));
		if (isBD(av) && isBD(bv)) return mod(bd(av), bd(bv));
		return throwUnsupportedTypes("mod", av, bv);
	}
	public int mod(final int av, final int bv) { return av % bv; }
	public long mod(final long av, final long bv) { return av % bv; }
	public float mod(final float av, final float bv) { return av % bv; }
	public double mod(final double av, final double bv) { return av % bv; }
	public BigInteger mod(final BigInteger av, final BigInteger bv) {
		return av.remainder(bv);
	}
	public BigDecimal mod(final BigDecimal av, final BigDecimal bv) {
		return av.remainder(bv);
	}

	@Override
	public Object rightDiv(final Object a, final Object b) {
		return throwUnimplementedOp("rightDiv");
	}

	@Override
	public Object dotMul(Object a, Object b) {
		return throwUnimplementedOp("dotMul");
	}

	@Override
	public Object dotDiv(final Object a, final Object b) {
		return throwUnimplementedOp("dotDiv");
	}

	@Override
	public Object dotRightDiv(final Object a, final Object b) {
		return throwUnimplementedOp("dotRightDiv");
	}

	// -- additive --

	@Override
	public Object add(final Object a, final Object b) {
		final Object av = value(a), bv = value(b);
		if (isStr(av)) return add(str(av), str(bv));
		if (isI(av) && isI(bv)) return add(i(av), i(bv));
		if (isL(av) && isL(bv)) return add(l(av), l(bv));
		if (isF(av) && isF(bv)) return add(f(av), f(bv));
		if (isD(av) && isD(bv)) return add(d(av), d(bv));
		if (isBI(av) && isBI(bv)) return add(bi(av), bi(bv));
		if (isBD(av) && isBD(bv)) return add(bd(av), bd(bv));
		return throwUnsupportedTypes("add", av, bv);
	}
	public String add(final String av, final String bv) { return av + bv; }
	public int add(final int av, final int bv) { return av + bv; }
	public long add(final long av, final long bv) { return av + bv; }
	public float add(final float av, final float bv) { return av + bv; }
	public double add(final double av, final double bv) { return av + bv; }
	public BigInteger add(final BigInteger av, final BigInteger bv) {
		return av.add(bv);
	}
	public BigDecimal add(final BigDecimal av, final BigDecimal bv) {
		return av.add(bv);
	}

	@Override
	public Object sub(final Object a, final Object b) {
		final Object av = value(a), bv = value(b);
		if (isI(av) && isI(bv)) return sub(i(av), i(bv));
		if (isL(av) && isL(bv)) return sub(l(av), l(bv));
		if (isF(av) && isF(bv)) return sub(f(av), f(bv));
		if (isD(av) && isD(bv)) return sub(d(av), d(bv));
		if (isBI(av) && isBI(bv)) return sub(bi(av), bi(bv));
		if (isBD(av) && isBD(bv)) return sub(bd(av), bd(bv));
		return throwUnsupportedTypes("sub", av, bv);
	}
	public int sub(final int av, final int bv) { return av - bv; }
	public long sub(final long av, final long bv) { return av - bv; }
	public float sub(final float av, final float bv) { return av - bv; }
	public double sub(final double av, final double bv) { return av - bv; }
	public BigInteger sub(final BigInteger av, final BigInteger bv) {
		return av.subtract(bv);
	}
	public BigDecimal sub(final BigDecimal av, final BigDecimal bv) {
		return av.subtract(bv);
	}

	// -- shift --

	@Override
	public Object leftShift(final Object a, final Object b) {
		final Object av = value(a), bv = value(b);
		if (isI(av) && isI(bv)) return leftShift(i(av), i(bv));
		if (isL(av) && isL(bv)) return leftShift(l(av), l(bv));
		if (isBI(av) && isI(bv)) return leftShift(bi(av), i(bv));
		return throwUnsupportedTypes("leftShift", av, bv);
	}
	public int leftShift(final int av, final int bv) { return av << bv; }
	public long leftShift(final long av, final long bv) { return av << bv; }
	public BigInteger leftShift(final BigInteger av, final int bv) {
		return av.shiftLeft(bv);
	}

	@Override
	public Object rightShift(final Object a, final Object b) {
		final Object av = value(a), bv = value(b);
		if (isI(av) && isI(bv)) return rightShift(i(av), i(bv));
		if (isL(av) && isL(bv)) return rightShift(l(av), l(bv));
		if (isBI(av) && isI(bv)) return rightShift(bi(av), i(bv));
		return throwUnsupportedTypes("rightShift", av, bv);
	}
	public int rightShift(final int av, final int bv) { return av >> bv; }
	public long rightShift(final long av, final long bv) { return av >> bv; }
	public BigInteger rightShift(final BigInteger av, final int bv) {
		return av.shiftRight(bv);
	}

	@Override
	public Object unsignedRightShift(final Object a, final Object b) {
		final Object av = value(a), bv = value(b);
		if (isI(av) && isI(bv)) return unsignedRightShift(i(av), i(bv));
		if (isL(av) && isL(bv)) return unsignedRightShift(l(av), l(bv));
		return throwUnsupportedTypes("unsignedRightShift", av, bv);
	}
	public int unsignedRightShift(final int av, final int bv) { return av >>> bv; }
	public long unsignedRightShift(final long av, final long bv) { return av >>> bv; }

	// -- relational --

	@Override
	public Object lessThan(final Object a, final Object b) {
		final Object av = value(a), bv = value(b);
		if (isBool(av) && isBool(bv)) return lessThan(bool(av), bool(bv));
		if (isStr(av) && isStr(bv)) return lessThan(str(av), str(bv));
		if (isI(av) && isI(bv)) return lessThan(i(av), i(bv));
		if (isL(av) && isL(bv)) return lessThan(l(av), l(bv));
		if (isF(av) && isF(bv)) return lessThan(f(av), f(bv));
		if (isD(av) && isD(bv)) return lessThan(d(av), d(bv));
		if (isBI(av) && isBI(bv)) return lessThan(bi(av), bi(bv));
		if (isBD(av) && isBD(bv)) return lessThan(bd(av), bd(bv));
		return throwUnsupportedTypes("lessThan", av, bv);
	}
	public <T> boolean lessThan(final Comparable<T> av, final T bv) {
		return av.compareTo(bv) < 0;
	}

	@Override
	public Object greaterThan(final Object a, final Object b) {
		final Object av = value(a), bv = value(b);
		if (isBool(av) && isBool(bv)) return greaterThan(bool(av), bool(bv));
		if (isStr(av) && isStr(bv)) return greaterThan(str(av), str(bv));
		if (isI(av) && isI(bv)) return greaterThan(i(av), i(bv));
		if (isL(av) && isL(bv)) return greaterThan(l(av), l(bv));
		if (isF(av) && isF(bv)) return greaterThan(f(av), f(bv));
		if (isD(av) && isD(bv)) return greaterThan(d(av), d(bv));
		if (isBI(av) && isBI(bv)) return greaterThan(bi(av), bi(bv));
		if (isBD(av) && isBD(bv)) return greaterThan(bd(av), bd(bv));
		return throwUnsupportedTypes("greaterThan", av, bv);
	}
	public <T> boolean greaterThan(final Comparable<T> av, final T bv) {
		return av.compareTo(bv) > 0;
	}

	@Override
	public Object lessThanOrEqual(final Object a, final Object b) {
		final Object av = value(a), bv = value(b);
		if (isBool(av) && isBool(bv)) return lessThanOrEqual(bool(av), bool(bv));
		if (isStr(av) && isStr(bv)) return lessThanOrEqual(str(av), str(bv));
		if (isI(av) && isI(bv)) return lessThanOrEqual(i(av), i(bv));
		if (isL(av) && isL(bv)) return lessThanOrEqual(l(av), l(bv));
		if (isF(av) && isF(bv)) return lessThanOrEqual(f(av), f(bv));
		if (isD(av) && isD(bv)) return lessThanOrEqual(d(av), d(bv));
		if (isBI(av) && isBI(bv)) return lessThanOrEqual(bi(av), bi(bv));
		if (isBD(av) && isBD(bv)) return lessThanOrEqual(bd(av), bd(bv));
		return throwUnsupportedTypes("lessThanOrEqual", av, bv);
	}
	public <T> boolean lessThanOrEqual(final Comparable<T> av, final T bv) {
		return av.compareTo(bv) <= 0;
	}

	@Override
	public Object greaterThanOrEqual(final Object a, final Object b) {
		final Object av = value(a), bv = value(b);
		if (isBool(av) && isBool(bv)) return greaterThanOrEqual(bool(av), bool(bv));
		if (isStr(av) && isStr(bv)) return greaterThanOrEqual(str(av), str(bv));
		if (isI(av) && isI(bv)) return greaterThanOrEqual(i(av), i(bv));
		if (isL(av) && isL(bv)) return greaterThanOrEqual(l(av), l(bv));
		if (isF(av) && isF(bv)) return greaterThanOrEqual(f(av), f(bv));
		if (isD(av) && isD(bv)) return greaterThanOrEqual(d(av), d(bv));
		if (isBI(av) && isBI(bv)) return greaterThanOrEqual(bi(av), bi(bv));
		if (isBD(av) && isBD(bv)) return greaterThanOrEqual(bd(av), bd(bv));
		return throwUnsupportedTypes("greaterThanOrEqual", av, bv);
	}
	public <T> boolean greaterThanOrEqual(final Comparable<T> av, final T bv) {
		return av.compareTo(bv) >= 0;
	}

	@Override
	public Object instanceOf(final Object av, final Object bv) {
		return throwUnimplementedOp("instanceOf");
	}

	// -- equality --

	@Override
	public Object equal(final Object a, final Object b) {
		return Objects.equals(value(a), value(b));
	}

	@Override
	public Object notEqual(final Object a, final Object b) {
		return !Objects.equals(value(a), value(b));
	}

	// -- bitwise --

	@Override
	public Object bitwiseAnd(final Object a, final Object b) {
		final Object av = value(a), bv = value(b);
		if (isI(av) && isI(bv)) return bitwiseAnd(i(av), i(bv));
		if (isL(av) && isL(bv)) return bitwiseAnd(l(av), l(bv));
		if (isBI(av) && isBI(bv)) return bitwiseAnd(bi(av), bi(bv));
		return throwUnsupportedTypes("bitwiseAnd", av, bv);
	}
	public int bitwiseAnd(final int av, final int bv) { return av & bv; }
	public long bitwiseAnd(final long av, final long bv) { return av & bv; }
	public BigInteger bitwiseAnd(final BigInteger av, final BigInteger bv) {
		return av.and(bv);
	}

	@Override
	public Object bitwiseOr(final Object a, final Object b) {
		final Object av = value(a), bv = value(b);
		if (isI(av) && isI(bv)) return bitwiseOr(i(av), i(bv));
		if (isL(av) && isL(bv)) return bitwiseOr(l(av), l(bv));
		if (isBI(av) && isBI(bv)) return bitwiseOr(bi(av), bi(bv));
		return throwUnsupportedTypes("bitwiseOr", av, bv);
	}
	public int bitwiseOr(final int av, final int bv) { return av | bv; }
	public long bitwiseOr(final long av, final long bv) { return av | bv; }
	public BigInteger bitwiseOr(final BigInteger av, final BigInteger bv) {
		return av.or(bv);
	}

	// -- logical --

	@Override
	public Object logicalAnd(final Object a, final Object b) {
		final Object av = value(a), bv = value(b);
		if (isBool(av) && isBool(bv)) return logicalAnd(bool(av), bool(bv));
		return throwUnsupportedTypes("logicalAnd", av, bv);
	}
	public boolean logicalAnd(final boolean av, final boolean bv) { return av && bv; }

	@Override
	public Object logicalOr(final Object a, final Object b) {
		final Object av = value(a), bv = value(b);
		if (isBool(av) && isBool(bv)) return logicalOr(bool(av), bool(bv));
		return throwUnsupportedTypes("logicalOr", av, bv);
	}
	public boolean logicalOr(final boolean av, final boolean bv) { return av || bv; }

	// -- ternary --

	@Override
	public Object question(final Object a, final Object b) {
		return throwUnimplementedOp("question");
	}

	@Override
	public Object colon(Object a, Object b) {
		return throwUnimplementedOp("colon");
	}

	// -- Helper methods - type matching --

	private boolean is(final Object v, final Class<?> c) {
		return c.isInstance(v);
	}

	private boolean isBool(final Object v) { return is(v, Boolean.class); }
	private boolean isStr(final Object v) { return is(v, String.class); }
	private boolean isB(final Object v) { return is(v, Byte.class); }
	private boolean isS(final Object v) { return is(v, Short.class) || isB(v); }
	private boolean isI(final Object v) { return is(v, Integer.class) || isS(v); }
	private boolean isL(final Object v) { return is(v, Long.class) || isI(v); }
	// NB: Java allows assignment of all integer primitive types to float!
	private boolean isF(final Object v) { return is(v, Float.class) || isL(v); }
	private boolean isD(final Object v) { return is(v, Double.class) || isF(v); }
	private boolean isBI(final Object v) { return is(v, BigInteger.class) || isL(v); }
	private boolean isBD(final Object v) { return is(v, BigDecimal.class) || isBI(v) || isD(v); }

	// -- Helper methods - type coercion --

	/** Casts the given value to the specified class, or null if incompatible. */
	private <T> T cast(final Object v, final Class<T> type) {
		if (type.isInstance(v)) {
			@SuppressWarnings("unchecked")
			final T result = (T) v;
			return result;
		}
		return null;
	}

	/** Coerces the given value to a boolean. */
	private boolean bool(final Object v) {
		final Boolean b = cast(v, Boolean.class);
		return b != null ? b : Boolean.valueOf(v.toString());
	}

	/** Coerces the given value to a string. */
	private String str(final Object v) {
		final String s = cast(v, String.class);
		return s != null ? s : v.toString();
	}

	/** Coerces the given value to a number. */
	private Number num(final Object v) {
		final Number n = cast(v, Number.class);
		return n != null ? n : Literals.parseNumber(v.toString());
	}

	private int i(final Object v) { return num(v).intValue(); }
	private long l(final Object v) { return num(v).longValue(); }
	private float f(final Object v) { return num(v).floatValue(); }
	private double d(final Object v) { return num(v).doubleValue(); }
	private BigInteger bi(final Object v) {
		final BigInteger bi = cast(v, BigInteger.class);
		return bi != null ? bi : new BigInteger("" + v);
	}
	private BigDecimal bd(final Object v) {
		final BigDecimal bd = cast(v, BigDecimal.class);
		return bd != null ? bd : new BigDecimal("" + v);
	}

	private Object listElement(final Object a, final Object b) {
		final Object value;
		try {
			value = value(a);
		}
		catch (final IllegalArgumentException exc) {
			return null;
		}
		if (!(value instanceof List)) return null;
		final List<?> list = (List<?>) value;
		if (!(b instanceof List)) return null;
		final List<?> indices = (List<?>) b;
		if (indices.size() != 1) return null; // not a 1-D access
		return list.get(i(indices.get(0)));
	}

	/** Executes built-in functions. */
	private Object callFunction(final String name, final Object b) {
		if (name.equals("postfix") && b instanceof String) {
			return getParser().parsePostfix((String) b);
		}

		if (name.equals("tree") && b instanceof String) {
			return getParser().parseTree((String) b);
		}

		return null;
	}

	// -- Helper methods - exception formatting --

	private Object throwUnimplementedOp(final String op) {
		throw new UnsupportedOperationException("Operation " + op + " is not implemented");
	}

	private Object throwUnsupportedTypes(String op, Object a) {
		throw new UnsupportedOperationException("Invalid parameter type for unary operation " + op + ": " + typeAndValue(a));
	}

	private Object throwUnsupportedTypes(String op, Object a, Object
		b) {
		throw new UnsupportedOperationException("Invalid parameter types for binary operation " + op + ": A = " + typeAndValue(a) + ", B = " + typeAndValue(b));
	}

	private String typeAndValue(Object o) {
		return o == null ? "<null>" : o.getClass().getSimpleName() + " [" + o + "]";
	}

}
