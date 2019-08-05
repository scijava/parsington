/*
 * #%L
 * Parsington: the SciJava mathematical expression parser.
 * %%
 * Copyright (C) 2015 - 2019 Board of Regents of the University of
 * Wisconsin-Madison.
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

import org.scijava.parsington.ExpressionParser;
import org.scijava.parsington.Literals;
import org.scijava.parsington.Operators;
import org.scijava.parsington.Tokens;
import org.scijava.parsington.Variable;

/**
 * An expression evaluator for most {@link Operators standard operators} with
 * common built-in types (i.e.: {@link Boolean}s, {@link String}s and
 * {@link Number}s). Simulates the ternary {@code ?:} operator including
 * short-circuiting.
 * <h2>Caveats</h2>
 * <p>
 * This class is a big bag of case logic for various operators and types.
 * Looking at it, you might think: "It sure would be nice to modularize this,
 * with each operation in its own class, with properly declared types, and
 * called dynamically at runtime as appropriate."
 * </p>
 * <p>
 * "Great idea!" I would reply. Then I would suggest you have a look at the
 * <a href="https://github.com/scijava/scijava-ops">SciJava Ops</a> and
 * <a href="https://github.com/imagej/imagej-ops">ImageJ Ops</a> projects, which
 * do exactly that in an extensible way.
 * </p>
 * <p>
 * Or maybe you are thinking: "This can't possibly work as well as awesome
 * JVM-based scripting languages like <a href="http://jython.org/">Jython</a>
 * and <a href="http://groovy.codehaus.org/">Groovy</a>..."
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
 * @see org.scijava.parsington.Main The main class, to give it a spin.
 */
public class DefaultTreeEvaluator extends AbstractStandardTreeEvaluator {

	public DefaultTreeEvaluator() {
		super();
	}

	public DefaultTreeEvaluator(final ExpressionParser parser) {
		super(parser);
	}

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
		return null;
	}

	// -- dot --

	@Override
	public Object dot(final Object a, final Object b) {
		// NB: Unimplemented.
		return null;
	}

	// -- groups --

	@Override
	public Object parens(final Object[] args) {
		if (args.length == 1) return args[0];
		return Arrays.asList(args);
	}

	@Override
	public Object brackets(final Object[] args) {
		return Arrays.asList(args);
	}

	@Override
	public Object braces(final Object[] args) {
		return Arrays.asList(args);
	}

	// -- transpose, power --

	@Override
	public Object transpose(final Object a) {
		// NB: Unimplemented.
		return null;
	}

	@Override
	public Object dotTranspose(final Object a) {
		// NB: Unimplemented.
		return null;
	}

	@Override
	public Object pow(final Object a, final Object b) {
		if (isD(a) && isD(b)) return pow(d(a), d(b));
		if (isBI(a) && isI(b)) return pow(bi(a), i(b));
		if (isBD(a) && isI(b)) return pow(bd(a), i(b));
		return null;
	}
	public double pow(final double a, final double b) { return Math.pow(a, b); }
	public BigInteger pow(final BigInteger a, final int b) { return a.pow(b); }
	public BigDecimal pow(final BigDecimal a, final int b) { return a.pow(b); }

	@Override
	public Object dotPow(final Object a, final Object b) {
		// NB: Unimplemented.
		return null;
	}

	// -- unary --

	@Override
	public Object pos(final Object a) {
		if (isI(a)) return pos(i(a));
		if (isL(a)) return pos(l(a));
		if (isF(a)) return pos(f(a));
		if (isD(a)) return pos(d(a));
		return value(a);
	}
	public int pos(final int num) { return +num; }
	public long pos(final long num) { return +num; }
	public float pos(final float num) { return +num; }
	public double pos(final double num) { return +num; }

	@Override
	public Object neg(final Object a) {
		if (isI(a)) return neg(i(a));
		if (isL(a)) return neg(l(a));
		if (isF(a)) return neg(f(a));
		if (isD(a)) return neg(d(a));
		if (isBI(a)) return neg(bi(a));
		if (isBD(a)) return neg(bd(a));
		return sub(0, a);
	}
	public int neg(final int num) { return -num; }
	public long neg(final long num) { return -num; }
	public float neg(final float num) { return -num; }
	public double neg(final double num) { return -num; }
	public BigInteger neg(final BigInteger num) { return num.negate(); }
	public BigDecimal neg(final BigDecimal num) { return num.negate(); }

	@Override
	public Object complement(final Object a) {
		if (isI(a)) return complement(i(a));
		if (isL(a)) return complement(l(a));
		return null;
	}
	public int complement(final int a) { return ~a; }
	public long complement(final long a) { return ~a; }

	@Override
	public Object not(final Object a) { return not(bool(a)); }
	public boolean not(final boolean a) { return !a; }

	// -- multiplicative --

	@Override
	public Object mul(final Object a, final Object b) {
		if (isI(a) && isI(b)) return mul(i(a), i(b));
		if (isL(a) && isL(b)) return mul(l(a), l(b));
		if (isF(a) && isF(b)) return mul(f(a), f(b));
		if (isD(a) && isD(b)) return mul(d(a), d(b));
		if (isBI(a) && isBI(b)) return mul(bi(a), bi(b));
		if (isBD(a) && isBD(b)) return mul(bd(a), bd(b));
		return null;
	}
	public int mul(final int a, final int b) { return a * b; }
	public long mul(final long a, final long b) { return a * b; }
	public float mul(final float a, final float b) { return a * b; }
	public double mul(final double a, final double b) { return a * b; }
	public BigInteger mul(final BigInteger a, final BigInteger b) {
		return a.multiply(b);
	}
	public BigDecimal mul(final BigDecimal a, final BigDecimal b) {
		return a.multiply(b);
	}

	@Override
	public Object div(final Object a, final Object b) {
		if (isI(a) && isI(b)) return div(i(a), i(b));
		if (isL(a) && isL(b)) return div(l(a), l(b));
		if (isF(a) && isF(b)) return div(f(a), f(b));
		if (isD(a) && isD(b)) return div(d(a), d(b));
		if (isBI(a) && isBI(b)) return div(bi(a), bi(b));
		if (isBD(a) && isBD(b)) return div(bd(a), bd(b));
		return null;
	}
	public int div(final int a, final int b) { return a / b; }
	public long div(final long a, final long b) { return a / b; }
	public float div(final float a, final float b) { return a / b; }
	public double div(final double a, final double b) { return a / b; }
	public BigInteger div(final BigInteger a, final BigInteger b) {
		return a.divide(b);
	}
	public BigDecimal div(final BigDecimal a, final BigDecimal b) {
		return a.divide(b);
	}

	@Override
	public Object mod(final Object a, final Object b) {
		if (isI(a) && isI(b)) return mod(i(a), i(b));
		if (isL(a) && isL(b)) return mod(l(a), l(b));
		if (isF(a) && isF(b)) return mod(f(a), f(b));
		if (isD(a) && isD(b)) return mod(d(a), d(b));
		if (isBI(a) && isBI(b)) return mod(bi(a), bi(b));
		if (isBD(a) && isBD(b)) return mod(bd(a), bd(b));
		return null;
	}
	public int mod(final int a, final int b) { return a % b; }
	public long mod(final long a, final long b) { return a % b; }
	public float mod(final float a, final float b) { return a % b; }
	public double mod(final double a, final double b) { return a % b; }
	public BigInteger mod(final BigInteger a, final BigInteger b) {
		return a.remainder(b);
	}
	public BigDecimal mod(final BigDecimal a, final BigDecimal b) {
		return a.remainder(b);
	}

	@Override
	public Object rightDiv(final Object a, final Object b) {
		// NB: Unimplemented.
		return null;
	}

	@Override
	public Object dotMul(Object a, Object b) {
		// NB: Unimplemented.
		return null;
	}

	@Override
	public Object dotDiv(final Object a, final Object b) {
		// NB: Unimplemented.
		return null;
	}

	@Override
	public Object dotRightDiv(final Object a, final Object b) {
		// NB: Unimplemented.
		return null;
	}

	// -- additive --

	@Override
	public Object add(final Object a, final Object b) {
		if (isStr(a)) return add(str(a), str(b));
		if (isI(a) && isI(b)) return add(i(a), i(b));
		if (isL(a) && isL(b)) return add(l(a), l(b));
		if (isF(a) && isF(b)) return add(f(a), f(b));
		if (isD(a) && isD(b)) return add(d(a), d(b));
		if (isBI(a) && isBI(b)) return add(bi(a), bi(b));
		if (isBD(a) && isBD(b)) return add(bd(a), bd(b));
		return null;
	}
	public String add(final String a, final String b) { return a + b; }
	public int add(final int a, final int b) { return a + b; }
	public long add(final long a, final long b) { return a + b; }
	public float add(final float a, final float b) { return a + b; }
	public double add(final double a, final double b) { return a + b; }
	public BigInteger add(final BigInteger a, final BigInteger b) {
		return a.add(b);
	}
	public BigDecimal add(final BigDecimal a, final BigDecimal b) {
		return a.add(b);
	}

	@Override
	public Object sub(final Object a, final Object b) {
		if (isI(a) && isI(b)) return sub(i(a), i(b));
		if (isL(a) && isL(b)) return sub(l(a), l(b));
		if (isF(a) && isF(b)) return sub(f(a), f(b));
		if (isD(a) && isD(b)) return sub(d(a), d(b));
		if (isBI(a) && isBI(b)) return sub(bi(a), bi(b));
		if (isBD(a) && isBD(b)) return sub(bd(a), bd(b));
		return null;
	}
	public int sub(final int a, final int b) { return a - b; }
	public long sub(final long a, final long b) { return a - b; }
	public float sub(final float a, final float b) { return a - b; }
	public double sub(final double a, final double b) { return a - b; }
	public BigInteger sub(final BigInteger a, final BigInteger b) {
		return a.subtract(b);
	}
	public BigDecimal sub(final BigDecimal a, final BigDecimal b) {
		return a.subtract(b);
	}

	// -- shift --

	@Override
	public Object leftShift(final Object a, final Object b) {
		if (isI(a) && isI(b)) return leftShift(i(a), i(b));
		if (isL(a) && isL(b)) return leftShift(l(a), l(b));
		if (isBI(a) && isI(b)) return leftShift(bi(a), i(b));
		return null;
	}
	public int leftShift(final int a, final int b) { return a << b; }
	public long leftShift(final long a, final long b) { return a << b; }
	public BigInteger leftShift(final BigInteger a, final int b) {
		return a.shiftLeft(b);
	}

	@Override
	public Object rightShift(final Object a, final Object b) {
		if (isI(a) && isI(b)) return rightShift(i(a), i(b));
		if (isL(a) && isL(b)) return rightShift(l(a), l(b));
		if (isBI(a) && isI(b)) return rightShift(bi(a), i(b));
		return null;
	}
	public int rightShift(final int a, final int b) { return a >> b; }
	public long rightShift(final long a, final long b) { return a >> b; }
	public BigInteger rightShift(final BigInteger a, final int b) {
		return a.shiftRight(b);
	}

	@Override
	public Object unsignedRightShift(final Object a, final Object b) {
		if (isI(a) && isI(b)) return unsignedRightShift(i(a), i(b));
		if (isL(a) && isL(b)) return unsignedRightShift(l(a), l(b));
		return null;
	}
	public int unsignedRightShift(final int a, final int b) { return a >>> b; }
	public long unsignedRightShift(final long a, final long b) { return a >>> b; }

	// -- relational --

	@Override
	public Object lessThan(final Object a, final Object b) {
		if (isBool(a) && isBool(b)) return lessThan(bool(a), bool(b));
		if (isStr(a) && isStr(b)) return lessThan(str(a), str(b));
		if (isI(a) && isI(b)) return lessThan(i(a), i(b));
		if (isL(a) && isL(b)) return lessThan(l(a), l(b));
		if (isF(a) && isF(b)) return lessThan(f(a), f(b));
		if (isD(a) && isD(b)) return lessThan(d(a), d(b));
		if (isBI(a) && isBI(b)) return lessThan(bi(a), bi(b));
		if (isBD(a) && isBD(b)) return lessThan(bd(a), bd(b));
		return null;
	}
	public <T> boolean lessThan(final Comparable<T> a, final T b) {
		return a.compareTo(b) < 0;
	}

	@Override
	public Object greaterThan(final Object a, final Object b) {
		if (isBool(a) && isBool(b)) return greaterThan(bool(a), bool(b));
		if (isStr(a) && isStr(b)) return greaterThan(str(a), str(b));
		if (isI(a) && isI(b)) return greaterThan(i(a), i(b));
		if (isL(a) && isL(b)) return greaterThan(l(a), l(b));
		if (isF(a) && isF(b)) return greaterThan(f(a), f(b));
		if (isD(a) && isD(b)) return greaterThan(d(a), d(b));
		if (isBI(a) && isBI(b)) return greaterThan(bi(a), bi(b));
		if (isBD(a) && isBD(b)) return greaterThan(bd(a), bd(b));
		return null;
	}
	public <T> boolean greaterThan(final Comparable<T> a, final T b) {
		return a.compareTo(b) > 0;
	}

	@Override
	public Object lessThanOrEqual(final Object a, final Object b) {
		if (isBool(a) && isBool(b)) return lessThanOrEqual(bool(a), bool(b));
		if (isStr(a) && isStr(b)) return lessThanOrEqual(str(a), str(b));
		if (isI(a) && isI(b)) return lessThanOrEqual(i(a), i(b));
		if (isL(a) && isL(b)) return lessThanOrEqual(l(a), l(b));
		if (isF(a) && isF(b)) return lessThanOrEqual(f(a), f(b));
		if (isD(a) && isD(b)) return lessThanOrEqual(d(a), d(b));
		if (isBI(a) && isBI(b)) return lessThanOrEqual(bi(a), bi(b));
		if (isBD(a) && isBD(b)) return lessThanOrEqual(bd(a), bd(b));
		return null;
	}
	public <T> boolean lessThanOrEqual(final Comparable<T> a, final T b) {
		return a.compareTo(b) <= 0;
	}

	@Override
	public Object greaterThanOrEqual(final Object a, final Object b) {
		if (isBool(a) && isBool(b)) return greaterThanOrEqual(bool(a), bool(b));
		if (isStr(a) && isStr(b)) return greaterThanOrEqual(str(a), str(b));
		if (isI(a) && isI(b)) return greaterThanOrEqual(i(a), i(b));
		if (isL(a) && isL(b)) return greaterThanOrEqual(l(a), l(b));
		if (isF(a) && isF(b)) return greaterThanOrEqual(f(a), f(b));
		if (isD(a) && isD(b)) return greaterThanOrEqual(d(a), d(b));
		if (isBI(a) && isBI(b)) return greaterThanOrEqual(bi(a), bi(b));
		if (isBD(a) && isBD(b)) return greaterThanOrEqual(bd(a), bd(b));
		return null;
	}
	public <T> boolean greaterThanOrEqual(final Comparable<T> a, final T b) {
		return a.compareTo(b) >= 0;
	}

	@Override
	public Object instanceOf(final Object a, final Object b) {
		// NB: Unimplemented.
		return null;
	}

	// -- equality --

	@Override
	public Object equal(final Object a, final Object b) {
		return value(a).equals(value(b));
	}

	@Override
	public Object notEqual(final Object a, final Object b) {
		return !value(a).equals(value(b));
	}

	// -- bitwise --

	@Override
	public Object bitwiseAnd(final Object a, final Object b) {
		if (isI(a) && isI(b)) return bitwiseAnd(i(a), i(b));
		if (isL(a) && isL(b)) return bitwiseAnd(l(a), l(b));
		if (isBI(a) && isBI(b)) return bitwiseAnd(bi(a), bi(b));
		return null;
	}
	public int bitwiseAnd(final int a, final int b) { return a & b; }
	public long bitwiseAnd(final long a, final long b) { return a & b; }
	public BigInteger bitwiseAnd(final BigInteger a, final BigInteger b) {
		return a.and(b);
	}

	@Override
	public Object bitwiseOr(final Object a, final Object b) {
		if (isI(a) && isI(b)) return bitwiseOr(i(a), i(b));
		if (isL(a) && isL(b)) return bitwiseOr(l(a), l(b));
		if (isBI(a) && isBI(b)) return bitwiseOr(bi(a), bi(b));
		return null;
	}
	public int bitwiseOr(final int a, final int b) { return a | b; }
	public long bitwiseOr(final long a, final long b) { return a | b; }
	public BigInteger bitwiseOr(final BigInteger a, final BigInteger b) {
		return a.or(b);
	}

	// -- logical --

	@Override
	public Object logicalAnd(final Object a, final Object b) {
		if (isBool(a) && isBool(b)) return logicalAnd(bool(a), bool(b));
		return null;
	}
	public boolean logicalAnd(final boolean a, final boolean b) { return a && b; }

	@Override
	public Object logicalOr(final Object a, final Object b) {
		if (isBool(a) && isBool(b)) return logicalOr(bool(a), bool(b));
		return null;
	}
	public boolean logicalOr(final boolean a, final boolean b) { return a || b; }

	// -- Helper methods - type matching --

	private boolean is(final Object o, final Class<?> c) {
		return c.isInstance(value(o));
	}

	private boolean isBool(final Object o) { return is(o, Boolean.class); }
	private boolean isStr(final Object o) { return is(o, String.class); }
	private boolean isB(final Object o) { return is(o, Byte.class); }
	private boolean isS(final Object o) { return is(o, Short.class) || isB(o); }
	private boolean isI(final Object o) { return is(o, Integer.class) || isS(o); }
	private boolean isL(final Object o) { return is(o, Long.class) || isI(o); }
	// NB: Java allows assignment of all integer primitive types to float!
	private boolean isF(final Object o) { return is(o, Float.class) || isL(o); }
	private boolean isD(final Object o) { return is(o, Double.class) || isF(o); }
	private boolean isBI(final Object o) { return is(o, BigInteger.class) || isL(o); }
	private boolean isBD(final Object o) { return is(o, BigDecimal.class) || isBI(o) || isD(o); }

	// -- Helper methods - type coercion --

	/** Casts the given token to the specified class, or null if incompatible. */
	private <T> T cast(final Object token, final Class<T> type) {
		if (type.isInstance(token)) {
			@SuppressWarnings("unchecked")
			final T result = (T) token;
			return result;
		}
		return null;
	}

	/** Coerces the given token to a boolean. */
	private boolean bool(final Object token) {
		final Boolean b = cast(value(token), Boolean.class);
		return b != null ? b : Boolean.valueOf(token.toString());
	}

	/** Coerces the given token to a string. */
	private String str(final Object token) {
		final String s = cast(value(token), String.class);
		return s != null ? s : token.toString();
	}

	/** Coerces the given token to a number. */
	private Number num(final Object token) {
		final Number n = cast(value(token), Number.class);
		return n != null ? n : Literals.parseNumber(token.toString());
	}

	private int i(final Object o) { return num(o).intValue(); }
	private long l(final Object o) { return num(o).longValue(); }
	private float f(final Object o) { return num(o).floatValue(); }
	private double d(final Object o) { return num(o).doubleValue(); }
	private BigInteger bi(final Object o) {
		final BigInteger bi = cast(o, BigInteger.class);
		return bi != null ? bi : new BigInteger("" + value(o));
	}
	private BigDecimal bd(final Object o) {
		final BigDecimal bd = cast(o, BigDecimal.class);
		return bd != null ? bd : new BigDecimal("" + value(o));
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

}
