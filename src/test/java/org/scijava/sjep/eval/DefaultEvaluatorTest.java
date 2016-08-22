/*
 * #%L
 * SciJava mathematical expression parser.
 * %%
 * Copyright (C) 2015 - 2016 Board of Regents of the University of
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

package org.scijava.sjep.eval;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.scijava.sjep.AbstractTest;
import org.scijava.sjep.Operators;
import org.scijava.sjep.SyntaxTree;
import org.scijava.sjep.Variable;

/** Tests {@link DefaultEvaluator}. */
public class DefaultEvaluatorTest extends AbstractTest {

	private DefaultEvaluator e;

	@Before
	public void setUp() {
		e = new DefaultEvaluator();
	}

	/** Tests {@link DefaultEvaluator#evaluate(String)}. */
	@Test
	public void testEvaluate() {
		assertEquals(26, e.evaluate("(2*3)+(4*5)"));
	}

	/** Tests strict mode; see {@link Evaluator#setStrict(boolean)}. */
	@Test(expected = IllegalArgumentException.class)
	public void testStrict() {
		e.evaluate("foo=bar");
	}

	/** Tests non-strict mode; see {@link Evaluator#setStrict(boolean)}. */
	@Test
	public void testNonStrict() {
		e.setStrict(false);
		e.evaluate("foo=bar");
		final Object bar = e.get(new Variable("foo"));
		assertTrue(bar instanceof Unresolved);
		assertEquals("bar", ((Unresolved) bar).getToken());
	}

	/**
	 * Tests that {@link DefaultEvaluator#evaluate(String)} handles the built-in
	 * {@code postfix} and {@code tree} functions as expected.
	 */
	@Test
	public void testBuiltIns() {
		final Object[] o = {1, 2, 3, Operators.MUL, Operators.ADD};
		final List<Object> postfix = Arrays.asList(o);
		assertEquals(postfix, e.evaluate("postfix('1+2*3')"));
		final SyntaxTree tree = new SyntaxTree(new LinkedList<Object>(postfix));
		assertEquals(tree, e.evaluate("tree('1+2*3')"));
	}

	// -- function --

	/** Tests {@link DefaultEvaluator#function(Object, Object)}. */
	@Test
	public void testFunction() {
		// test list access
		final Variable v = new Variable("v");
		e.set(v, Arrays.asList("a", "b", "c"));
		assertEquals("a", e.function(v, Arrays.asList(0)));
		assertEquals("b", e.function(v, Arrays.asList(1)));
		assertEquals("c", e.function(v, Arrays.asList(2)));

		assertNull(e.function(o(0), o(1)));
	}

	// -- dot --

	/** Tests {@link DefaultEvaluator#dot(Object, Object)}. */
	@Test
	public void testDot() {
		assertNull(e.dot(o(0), o(1)));
	}

	// -- groups --

	/** Tests {@link DefaultEvaluator#parens(Object[])}. */
	@Test
	public void testParens() {
		final Object[] o = {1, 2, 3};
		assertEquals(Arrays.asList(o), e.parens(o));

		// test empty parentheses
		assertEquals(Collections.emptyList(), e.parens(new Object[0]));

		// test collapse of single elements
		assertEquals(4, e.parens(new Object[] {4}));
	}

	/** Tests {@link DefaultEvaluator#brackets(Object[])}. */
	@Test
	public void testBrackets() {
		final Object[] o = {1, 2, 3};
		assertEquals(Arrays.asList(o), e.brackets(o));

		// test empty brackets
		assertEquals(Collections.emptyList(), e.brackets(new Object[0]));
	}

	/** Tests {@link DefaultEvaluator#braces(Object[])}. */
	@Test
	public void testBraces() {
		final Object[] o = {1, 2, 3};
		assertEquals(Arrays.asList(o), e.braces(o));

		// test empty braces
		assertEquals(Collections.emptyList(), e.braces(new Object[0]));
	}

	// -- transpose, power --

	/** Tests {@link DefaultEvaluator#transpose(Object)}. */
	@Test
	public void testTranspose() {
		assertNull(e.transpose(o(0)));
	}

	/** Tests {@link DefaultEvaluator#dotTranspose(Object)}. */
	@Test
	public void testDotTranspose() {
		assertNull(e.dotTranspose(o(0)));
	}

	/** Tests {@link DefaultEvaluator#pow(Object, Object)}. */
	@Test
	public void testPow() {
		assertNumber(15.625d, e.pow(o(2.5d), o(3d)));
		assertNumber(bi(15625), e.pow(o(bi(5)), o(6)));
		assertNumber(bd(15.625), e.pow(o(bd(2.5d)), o(3)));
	}

	/** Tests {@link DefaultEvaluator#dotPow(Object, Object)}. */
	@Test
	public void testDotPow() {
		assertNull(e.dotPow(o(0), o(0)));
	}

	// -- postfix --

	/** Tests {@link DefaultEvaluator#postInc(Object)}. */
	@Test
	public void testPostInc() {
		final Variable v = new Variable("v");
		e.set(v, 2); assertEquals(2, e.postInc(v)); assertEquals(3, e.get(v));
		e.set(v, 4L); assertEquals(4L, e.postInc(v)); assertEquals(5L, e.get(v));
		e.set(v, 6f); assertEquals(6f, e.postInc(v)); assertEquals(7f, e.get(v));
		e.set(v, 8d); assertEquals(8d, e.postInc(v)); assertEquals(9d, e.get(v));
		e.set(v, bi(10)); assertEquals(bi(10), e.postInc(v)); assertEquals(bi(11), e.get(v));
		e.set(v, bd(12)); assertEquals(bd(12), e.postInc(v)); assertEquals(bd(13), e.get(v));
	}

	/** Tests {@link DefaultEvaluator#postDec(Object)}. */
	@Test
	public void testPostDec() {
		final Variable v = new Variable("v");
		e.set(v, 2); assertEquals(2, e.postDec(v)); assertEquals(1, e.get(v));
		e.set(v, 4L); assertEquals(4L, e.postDec(v)); assertEquals(3L, e.get(v));
		e.set(v, 6f); assertEquals(6f, e.postDec(v)); assertEquals(5f, e.get(v));
		e.set(v, 8d); assertEquals(8d, e.postDec(v)); assertEquals(7d, e.get(v));
		e.set(v, bi(10)); assertEquals(bi(10), e.postDec(v)); assertEquals(bi(9), e.get(v));
		e.set(v, bd(12)); assertEquals(bd(12), e.postDec(v)); assertEquals(bd(11), e.get(v));
	}

	// -- unary --

	/** Tests {@link DefaultEvaluator#preInc(Object)}. */
	@Test
	public void testPreInc() {
		final Variable v = new Variable("v");
		e.set(v, 2); assertEquals(3, e.preInc(v)); assertEquals(3, e.get(v));
		e.set(v, 4L); assertEquals(5L, e.preInc(v)); assertEquals(5L, e.get(v));
		e.set(v, 6f); assertEquals(7f, e.preInc(v)); assertEquals(7f, e.get(v));
		e.set(v, 8d); assertEquals(9d, e.preInc(v)); assertEquals(9d, e.get(v));
		e.set(v, bi(10)); assertEquals(bi(11), e.preInc(v)); assertEquals(bi(11), e.get(v));
		e.set(v, bd(12)); assertEquals(bd(13), e.preInc(v)); assertEquals(bd(13), e.get(v));
	}

	/** Tests {@link DefaultEvaluator#preDec(Object)}. */
	@Test
	public void testPreDec() {
		final Variable v = new Variable("v");
		e.set(v, 2); assertEquals(1, e.preDec(v)); assertEquals(1, e.get(v));
		e.set(v, 4L); assertEquals(3L, e.preDec(v)); assertEquals(3L, e.get(v));
		e.set(v, 6f); assertEquals(5f, e.preDec(v)); assertEquals(5f, e.get(v));
		e.set(v, 8d); assertEquals(7d, e.preDec(v)); assertEquals(7d, e.get(v));
		e.set(v, bi(10)); assertEquals(bi(9), e.preDec(v)); assertEquals(bi(9), e.get(v));
		e.set(v, bd(12)); assertEquals(bd(11), e.preDec(v)); assertEquals(bd(11), e.get(v));
	}

	/** Tests {@link DefaultEvaluator#pos(Object)}. */
	@Test
	public void testPos() {
		assertNumber(7, e.pos(o(7)));
		assertNumber(7L, e.pos(o(7L)));
		assertNumber(7.8d, e.pos(o(7.8d)));
		assertNumber(7.8f, e.pos(o(7.8f)));
		assertSame(BigInteger.ZERO, e.pos(BigInteger.ZERO));
		assertSame(BigInteger.ONE, e.pos(BigInteger.ONE));
		assertSame(BigInteger.TEN, e.pos(BigInteger.TEN));
		assertSame(BigDecimal.ZERO, e.pos(BigDecimal.ZERO));
		assertSame(BigDecimal.ONE, e.pos(BigDecimal.ONE));
		assertSame(BigDecimal.TEN, e.pos(BigDecimal.TEN));
	}

	/** Tests {@link DefaultEvaluator#neg(Object)}. */
	@Test
	public void testNeg() {
		assertNumber(7, e.neg(o(-7)));
		assertNumber(7L, e.neg(o(-7L)));
		assertNumber(7.8f, e.neg(o(-7.8f)));
		assertNumber(7.8d, e.neg(o(-7.8d)));
		assertNumber(bi(7), e.neg(o(bi(-7))));
		assertNumber(bd(7.8), e.neg(o(bd(-7.8))));
		assertNumber(-7, e.neg(o(7)));
		assertNumber(-7L, e.neg(o(7L)));
		assertNumber(-7.8f, e.neg(o(7.8f)));
		assertNumber(-7.8d, e.neg(o(7.8d)));
		assertNumber(bi(-7), e.neg(o(bi(7))));
		assertNumber(bd(-7.8), e.neg(o(bd(7.8))));
	}

	/** Tests {@link DefaultEvaluator#complement(Object)}. */
	@Test
	public void testComplement() {
		assertNumber(0x35014541, e.complement(o(0xcafebabe)));
		assertNumber(0x2152350141104541L, e.complement(o(0xdeadcafebeefbabeL)));
	}

	/** Tests {@link DefaultEvaluator#not(Object)}. */
	@Test
	public void testNot() {
		assertSame(false, e.not(o(true)));
		assertSame(true, e.not(o(false)));
	}

	// -- multiplicative --

	/** Tests {@link DefaultEvaluator#mul(Object, Object)}. */
	@Test
	public void testMul() {
		assertNumber(24, e.mul(o(4), o(6)));
		assertNumber(24L, e.mul(o(4L), o(6L)));
		assertNumber(8.75f, e.mul(o(2.5f), o(3.5f)));
		assertNumber(8.75d, e.mul(o(2.5d), o(3.5d)));
		assertNumber(bi(24), e.mul(o(bi(4)), o(bi(6))));
		assertNumber(bd(8.75), e.mul(o(bd(2.5)), o(bd(3.5))));
	}

	/** Tests {@link DefaultEvaluator#div(Object, Object)}. */
	@Test
	public void testDiv() {
		assertNumber(4, e.div(o(27), o(6)));
		assertNumber(4L, e.div(o(27L), o(6L)));
		assertNumber(2.5f, e.div(o(8.75f), o(3.5f)));
		assertNumber(2.5d, e.div(o(8.75d), o(3.5d)));
		assertNumber(bi(4), e.div(o(bi(27)), o(bi(6))));
		assertNumber(bd(2.5), e.div(o(bd(8.75)), o(bd(3.5))));
	}

	/** Tests {@link DefaultEvaluator#mod(Object, Object)}. */
	@Test
	public void testMod() {
		assertNumber(3, e.mod(o(27), o(6)));
		assertNumber(3L, e.mod(o(27L), o(6L)));
		assertNumber(1.75f, e.mod(o(8.75f), o(3.5f)));
		assertNumber(1.75d, e.mod(o(8.75d), o(3.5d)));
		assertNumber(bi(3), e.mod(o(bi(27)), o(bi(6))));
		assertNumber(bd(1.75), e.mod(o(bd(8.75)), o(bd(3.5))));
	}

	/** Tests {@link DefaultEvaluator#rightDiv(Object, Object)}. */
	@Test
	public void testRightDiv() {
		assertNull(e.rightDiv(o(0), o(0)));
	}

	/** Tests {@link DefaultEvaluator#dotDiv(Object, Object)}. */
	@Test
	public void testDotDiv() {
		assertNull(e.dotDiv(o(0), o(0)));
	}

	/** Tests {@link DefaultEvaluator#dotRightDiv(Object, Object)}. */
	@Test
	public void testDotRightDiv() {
		assertNull(e.dotRightDiv(o(0), o(0)));
	}

	// -- additive --

	/** Tests {@link DefaultEvaluator#add(Object, Object)}. */
	@Test
	public void testAdd() {
		assertEquals("Hello, world", e.add(o("Hello,"), o(" world")));
		assertNumber(10, e.add(o(4), o(6)));
		assertNumber(10L, e.add(o(4L), o(6L)));
		assertNumber(3.6f, e.add(o(1.5f), o(2.1f)));
		assertNumber(3.6d, e.add(o(1.5d), o(2.1d)));
		assertNumber(bi(10), e.add(o(bi(4)), o(bi(6))));
		assertNumber(bd(3.6), e.add(o(bd(1.5)), o(bd(2.1))));
	}

	/** Tests {@link DefaultEvaluator#sub(Object, Object)}. */
	@Test
	public void testSub() {
		assertNumber(4, e.sub(o(10), o(6)));
		assertNumber(4L, e.sub(o(10L), o(6L)));
		assertNumber(1.5f, e.sub(o(3.6f), o(2.1f)));
		assertNumber(1.5d, e.sub(o(3.6d), o(2.1d)));
		assertNumber(bi(4), e.sub(o(bi(10)), o(bi(6))));
		assertNumber(bd(1.5), e.sub(o(bd(3.6)), o(bd(2.1))));
	}

	// -- shift --

	/** Tests {@link DefaultEvaluator#leftShift(Object, Object)}. */
	@Test
	public void testLeftShift() {
		assertNumber(0xafebabe0, e.leftShift(o(0xcafebabe), o(4)));
		assertNumber(0xdcafebeefbabe000L, e.leftShift(o(0xdeadcafebeefbabeL), o(12)));
		assertNumber(bi(7296), e.leftShift(o(bi(57)), o(7)));
	}

	/** Tests {@link DefaultEvaluator#rightShift(Object, Object)}. */
	@Test
	public void testRightShift() {
		assertNumber(0xfcafebab, e.rightShift(o(0xcafebabe), o(4)));
		assertNumber(0xfffdeadcafebeefbL, e.rightShift(o(0xdeadcafebeefbabeL), o(12)));
		assertNumber(bi(278), e.rightShift(o(bi(8920)), o(5)));
	}

	/** Tests {@link DefaultEvaluator#unsignedRightShift(Object, Object)}. */
	@Test
	public void testUnsignedRightShift() {
		assertNumber(0x0cafebab, e.unsignedRightShift(o(0xcafebabe), o(4)));
		assertNumber(0x000deadcafebeefbL, e.unsignedRightShift(o(0xdeadcafebeefbabeL), o(12)));
	}

	// -- colon --

	/** Tests {@link DefaultEvaluator#colon(Object, Object)}. */
	@Test
	public void testColon() {
		assertNull(e.colon(o(0), o(0)));
	}

	// -- relational --

	/** Tests {@link DefaultEvaluator#lessThan(Object, Object)}. */
	@Test
	public void testLessThan() {
		assertSame(true, e.lessThan(o(false), o(true)));
		assertSame(false, e.lessThan(o(false), o(false)));
		assertSame(false, e.lessThan(o(true), o(false)));

		assertSame(true, e.lessThan(o("hello"), o("world")));
		assertSame(false, e.lessThan(o("hello"), o("hello")));
		assertSame(false, e.lessThan(o("young"), o("world")));

		assertSame(true, e.lessThan(o(2), o(3)));
		assertSame(false, e.lessThan(o(2), o(2)));
		assertSame(false, e.lessThan(o(2), o(1)));

		assertSame(true, e.lessThan(o(5L), o(6L)));
		assertSame(false, e.lessThan(o(5L), o(5L)));
		assertSame(false, e.lessThan(o(5L), o(4L)));

		assertSame(true, e.lessThan(o(8f), o(9f)));
		assertSame(false, e.lessThan(o(8f), o(8f)));
		assertSame(false, e.lessThan(o(8f), o(7f)));

		assertSame(true, e.lessThan(o(11d), o(12d)));
		assertSame(false, e.lessThan(o(11d), o(11d)));
		assertSame(false, e.lessThan(o(11d), o(10d)));

		assertSame(true, e.lessThan(o(bi(14)), o(bi(15))));
		assertSame(false, e.lessThan(o(bi(14)), o(bi(14))));
		assertSame(false, e.lessThan(o(bi(14)), o(bi(13))));

		assertSame(true, e.lessThan(o(bd(17)), o(bd(18))));
		assertSame(false, e.lessThan(o(bd(17)), o(bd(17))));
		assertSame(false, e.lessThan(o(bd(17)), o(bd(16))));
	}

	/** Tests {@link DefaultEvaluator#greaterThan(Object, Object)}. */
	@Test
	public void testGreaterThan() {
		assertSame(false, e.greaterThan(o(false), o(true)));
		assertSame(false, e.greaterThan(o(false), o(false)));
		assertSame(true, e.greaterThan(o(true), o(false)));

		assertSame(false, e.greaterThan(o("hello"), o("world")));
		assertSame(false, e.greaterThan(o("hello"), o("hello")));
		assertSame(true, e.greaterThan(o("young"), o("world")));

		assertSame(false, e.greaterThan(o(2), o(3)));
		assertSame(false, e.greaterThan(o(2), o(2)));
		assertSame(true, e.greaterThan(o(2), o(1)));

		assertSame(false, e.greaterThan(o(5L), o(6L)));
		assertSame(false, e.greaterThan(o(5L), o(5L)));
		assertSame(true, e.greaterThan(o(5L), o(4L)));

		assertSame(false, e.greaterThan(o(8f), o(9f)));
		assertSame(false, e.greaterThan(o(8f), o(8f)));
		assertSame(true, e.greaterThan(o(8f), o(7f)));

		assertSame(false, e.greaterThan(o(11d), o(12d)));
		assertSame(false, e.greaterThan(o(11d), o(11d)));
		assertSame(true, e.greaterThan(o(11d), o(10d)));

		assertSame(false, e.greaterThan(o(bi(14)), o(bi(15))));
		assertSame(false, e.greaterThan(o(bi(14)), o(bi(14))));
		assertSame(true, e.greaterThan(o(bi(14)), o(bi(13))));

		assertSame(false, e.greaterThan(o(bd(17)), o(bd(18))));
		assertSame(false, e.greaterThan(o(bd(17)), o(bd(17))));
		assertSame(true, e.greaterThan(o(bd(17)), o(bd(16))));
	}

	/** Tests {@link DefaultEvaluator#lessThanOrEqual(Object, Object)}. */
	@Test
	public void testLessThanOrEqual() {
		assertSame(true, e.lessThanOrEqual(o(false), o(true)));
		assertSame(true, e.lessThanOrEqual(o(false), o(false)));
		assertSame(false, e.lessThanOrEqual(o(true), o(false)));

		assertSame(true, e.lessThanOrEqual(o("hello"), o("world")));
		assertSame(true, e.lessThanOrEqual(o("hello"), o("hello")));
		assertSame(false, e.lessThanOrEqual(o("young"), o("world")));

		assertSame(true, e.lessThanOrEqual(o(2), o(3)));
		assertSame(true, e.lessThanOrEqual(o(2), o(2)));
		assertSame(false, e.lessThanOrEqual(o(2), o(1)));

		assertSame(true, e.lessThanOrEqual(o(5L), o(6L)));
		assertSame(true, e.lessThanOrEqual(o(5L), o(5L)));
		assertSame(false, e.lessThanOrEqual(o(5L), o(4L)));

		assertSame(true, e.lessThanOrEqual(o(8f), o(9f)));
		assertSame(true, e.lessThanOrEqual(o(8f), o(8f)));
		assertSame(false, e.lessThanOrEqual(o(8f), o(7f)));

		assertSame(true, e.lessThanOrEqual(o(11d), o(12d)));
		assertSame(true, e.lessThanOrEqual(o(11d), o(11d)));
		assertSame(false, e.lessThanOrEqual(o(11d), o(10d)));

		assertSame(true, e.lessThanOrEqual(o(bi(14)), o(bi(15))));
		assertSame(true, e.lessThanOrEqual(o(bi(14)), o(bi(14))));
		assertSame(false, e.lessThanOrEqual(o(bi(14)), o(bi(13))));

		assertSame(true, e.lessThanOrEqual(o(bd(17)), o(bd(18))));
		assertSame(true, e.lessThanOrEqual(o(bd(17)), o(bd(17))));
		assertSame(false, e.lessThanOrEqual(o(bd(17)), o(bd(16))));
	}

	/** Tests {@link DefaultEvaluator#greaterThanOrEqual(Object, Object)}. */
	@Test
	public void testGreaterThanOrEqual() {
		assertSame(false, e.greaterThanOrEqual(o(false), o(true)));
		assertSame(true, e.greaterThanOrEqual(o(false), o(false)));
		assertSame(true, e.greaterThanOrEqual(o(true), o(false)));

		assertSame(false, e.greaterThanOrEqual(o("hello"), o("world")));
		assertSame(true, e.greaterThanOrEqual(o("hello"), o("hello")));
		assertSame(true, e.greaterThanOrEqual(o("young"), o("world")));

		assertSame(false, e.greaterThanOrEqual(o(2), o(3)));
		assertSame(true, e.greaterThanOrEqual(o(2), o(2)));
		assertSame(true, e.greaterThanOrEqual(o(2), o(1)));

		assertSame(false, e.greaterThanOrEqual(o(5L), o(6L)));
		assertSame(true, e.greaterThanOrEqual(o(5L), o(5L)));
		assertSame(true, e.greaterThanOrEqual(o(5L), o(4L)));

		assertSame(false, e.greaterThanOrEqual(o(8f), o(9f)));
		assertSame(true, e.greaterThanOrEqual(o(8f), o(8f)));
		assertSame(true, e.greaterThanOrEqual(o(8f), o(7f)));

		assertSame(false, e.greaterThanOrEqual(o(11d), o(12d)));
		assertSame(true, e.greaterThanOrEqual(o(11d), o(11d)));
		assertSame(true, e.greaterThanOrEqual(o(11d), o(10d)));

		assertSame(false, e.greaterThanOrEqual(o(bi(14)), o(bi(15))));
		assertSame(true, e.greaterThanOrEqual(o(bi(14)), o(bi(14))));
		assertSame(true, e.greaterThanOrEqual(o(bi(14)), o(bi(13))));

		assertSame(false, e.greaterThanOrEqual(o(bd(17)), o(bd(18))));
		assertSame(true, e.greaterThanOrEqual(o(bd(17)), o(bd(17))));
		assertSame(true, e.greaterThanOrEqual(o(bd(17)), o(bd(16))));
	}

	/** Tests {@link DefaultEvaluator#instanceOf(Object, Object)}. */
	@Test
	public void testInstanceOf() {
		assertNull(e.instanceOf(o(0), o(0)));
	}

	// -- equality --

	/** Tests {@link DefaultEvaluator#equal(Object, Object)}. */
	@Test
	public void testEqual() {
		assertSame(false, e.equal(o(false), o(true)));
		assertSame(true, e.equal(o(false), o(false)));
		assertSame(false, e.equal(o(true), o(false)));

		assertSame(false, e.equal(o("hello"), o("world")));
		assertSame(true, e.equal(o("hello"), o("hello")));
		assertSame(false, e.equal(o("young"), o("world")));

		assertSame(false, e.equal(o(2), o(3)));
		assertSame(true, e.equal(o(2), o(2)));
		assertSame(false, e.equal(o(2), o(1)));

		assertSame(false, e.equal(o(5L), o(6L)));
		assertSame(true, e.equal(o(5L), o(5L)));
		assertSame(false, e.equal(o(5L), o(4L)));

		assertSame(false, e.equal(o(8f), o(9f)));
		assertSame(true, e.equal(o(8f), o(8f)));
		assertSame(false, e.equal(o(8f), o(7f)));

		assertSame(false, e.equal(o(11d), o(12d)));
		assertSame(true, e.equal(o(11d), o(11d)));
		assertSame(false, e.equal(o(11d), o(10d)));

		assertSame(false, e.equal(o(bi(14)), o(bi(15))));
		assertSame(true, e.equal(o(bi(14)), o(bi(14))));
		assertSame(false, e.equal(o(bi(14)), o(bi(13))));

		assertSame(false, e.equal(o(bd(17)), o(bd(18))));
		assertSame(true, e.equal(o(bd(17)), o(bd(17))));
		assertSame(false, e.equal(o(bd(17)), o(bd(16))));
	}

	/** Tests {@link DefaultEvaluator#notEqual(Object, Object)}. */
	@Test
	public void testNotEqual() {
		assertSame(true, e.notEqual(o(false), o(true)));
		assertSame(false, e.notEqual(o(false), o(false)));
		assertSame(true, e.notEqual(o(true), o(false)));

		assertSame(true, e.notEqual(o("hello"), o("world")));
		assertSame(false, e.notEqual(o("hello"), o("hello")));
		assertSame(true, e.notEqual(o("young"), o("world")));

		assertSame(true, e.notEqual(o(2), o(3)));
		assertSame(false, e.notEqual(o(2), o(2)));
		assertSame(true, e.notEqual(o(2), o(1)));

		assertSame(true, e.notEqual(o(5L), o(6L)));
		assertSame(false, e.notEqual(o(5L), o(5L)));
		assertSame(true, e.notEqual(o(5L), o(4L)));

		assertSame(true, e.notEqual(o(8f), o(9f)));
		assertSame(false, e.notEqual(o(8f), o(8f)));
		assertSame(true, e.notEqual(o(8f), o(7f)));

		assertSame(true, e.notEqual(o(11d), o(12d)));
		assertSame(false, e.notEqual(o(11d), o(11d)));
		assertSame(true, e.notEqual(o(11d), o(10d)));

		assertSame(true, e.notEqual(o(bi(14)), o(bi(15))));
		assertSame(false, e.notEqual(o(bi(14)), o(bi(14))));
		assertSame(true, e.notEqual(o(bi(14)), o(bi(13))));

		assertSame(true, e.notEqual(o(bd(17)), o(bd(18))));
		assertSame(false, e.notEqual(o(bd(17)), o(bd(17))));
		assertSame(true, e.notEqual(o(bd(17)), o(bd(16))));
	}

	// -- bitwise --

	/** Tests {@link DefaultEvaluator#bitwiseAnd(Object, Object)}. */
	@Test
	public void testBitwiseAnd() {
		assertNumber(0xcaacbaae, e.bitwiseAnd(o(0xcafebabe), o(0xdeadbeef)));
		assertNumber(0L, e.bitwiseAnd(o(0x0d0e0a0d0c0a0f0eL), o(0xb0e0e0f0b0a0b0e0L)));
		assertNumber(bi(0xcaacbaae), e.bitwiseAnd(o(bi(0xcafebabe)), o(bi(0xdeadbeef))));
	}

	/** Tests {@link DefaultEvaluator#bitwiseOr(Object, Object)}. */
	@Test
	public void testBitwiseOr() {
		assertNumber(0xdeffbeff, e.bitwiseOr(o(0xcafebabe), o(0xdeadbeef)));
		assertNumber(0xbdeeeafdbcaabfeeL, e.bitwiseOr(o(0x0d0e0a0d0c0a0f0eL), o(0xb0e0e0f0b0a0b0e0L)));
		assertNumber(bi(0xdeffbeff), e.bitwiseOr(o(bi(0xcafebabe)), o(bi(0xdeadbeef))));
	}

	// -- logical --

	/** Tests {@link DefaultEvaluator#logicalAnd(Object, Object)}. */
	@Test
	public void testLogicalAnd() {
		assertSame(false, e.logicalAnd(o(false), o(false)));
		assertSame(false, e.logicalAnd(o(false), o(true)));
		assertSame(false, e.logicalAnd(o(true), o(false)));
		assertSame(true, e.logicalAnd(o(true), o(true)));
	}

	/** Tests {@link DefaultEvaluator#logicalOr(Object, Object)}. */
	@Test
	public void testLogicalOr() {
		assertSame(false, e.logicalOr(o(false), o(false)));
		assertSame(true, e.logicalOr(o(false), o(true)));
		assertSame(true, e.logicalOr(o(true), o(false)));
		assertSame(true, e.logicalOr(o(true), o(true)));
	}

	// -- assignment --

	/** Tests {@link DefaultEvaluator#assign(Object, Object)}. */
	@Test
	public void testAssign() {
		final Variable v = new Variable("v");
		assertAssigned(true, v, e.assign(v, true));
		assertAssigned("hello", v, e.assign(v, "hello"));
		assertAssigned(1, v, e.assign(v, 1));
		assertAssigned(2L, v, e.assign(v, 2L));
		assertAssigned(3f, v, e.assign(v, 3f));
		assertAssigned(4d, v, e.assign(v, 4d));
		assertAssigned(bi(5), v, e.assign(v, bi(5)));
		assertAssigned(bd(6), v, e.assign(v, bd(6)));
	}

	/** Tests {@link DefaultEvaluator#powAssign(Object, Object)}. */
	@Test
	public void testPowAssign() {
		final Variable v = new Variable("v");
		e.set(v, 2.5d); assertAssigned(15.625d, v, e.powAssign(v, 3));
		e.set(v, bi(5)); assertAssigned(bi(15625), v, e.powAssign(v, 6));
		e.set(v, bd(2.5)); assertAssigned(bd(15.625), v, e.powAssign(v, 3));
	}

	/** Tests {@link DefaultEvaluator#dotPowAssign(Object, Object)}. */
	@Test
	public void testDotPowAssign() {
		// NB: Nothing to test; dotPow is unimplemented.
	}

	/** Tests {@link DefaultEvaluator#mulAssign(Object, Object)}. */
	@Test
	public void testMulAssign() {
		final Variable v = new Variable("v");
		e.set(v, 4); assertAssigned(24, v, e.mulAssign(v, 6));
		e.set(v, 4L); assertAssigned(24L, v, e.mulAssign(v, 6L));
		e.set(v, 2.5f); assertAssigned(8.75f, v, e.mulAssign(v, 3.5f));
		e.set(v, 2.5d); assertAssigned(8.75d, v, e.mulAssign(v, 3.5d));
		e.set(v, bi(4)); assertAssigned(bi(24), v, e.mulAssign(v, bi(6)));
		e.set(v, bd(2.5)); assertAssigned(bd(8.75), v, e.mulAssign(v, bd(3.5)));
	}

	/** Tests {@link DefaultEvaluator#divAssign(Object, Object)}. */
	@Test
	public void testDivAssign() {
		final Variable v = new Variable("v");
		e.set(v, 27); assertAssigned(4, v, e.divAssign(v, 6));
		e.set(v, 27L); assertAssigned(4L, v, e.divAssign(v, 6L));
		e.set(v, 8.75f); assertAssigned(2.5f, v, e.divAssign(v, 3.5f));
		e.set(v, 8.75d); assertAssigned(2.5d, v, e.divAssign(v, 3.5d));
		e.set(v, bi(27)); assertAssigned(bi(4), v, e.divAssign(v, bi(6)));
		e.set(v, bd(8.75)); assertAssigned(bd(2.5), v, e.divAssign(v, bd(3.5)));
	}

	/** Tests {@link DefaultEvaluator#modAssign(Object, Object)}. */
	@Test
	public void testModAssign() {
		final Variable v = new Variable("v");
		e.set(v, 27); assertAssigned(3, v, e.modAssign(v, 6));
		e.set(v, 27L); assertAssigned(3L, v, e.modAssign(v, 6L));
		e.set(v, 8.75f); assertAssigned(1.75f, v, e.modAssign(v, 3.5f));
		e.set(v, 8.75d); assertAssigned(1.75d, v, e.modAssign(v, 3.5d));
		e.set(v, bi(27)); assertAssigned(bi(3), v, e.modAssign(v, bi(6)));
		e.set(v, bd(8.75)); assertAssigned(bd(1.75), v, e.modAssign(v, bd(3.5)));
	}

	/** Tests {@link DefaultEvaluator#rightDivAssign(Object, Object)}. */
	@Test
	public void testRightDivAssign() {
		// NB: Nothing to test; rightDiv is unimplemented.
	}

	/** Tests {@link DefaultEvaluator#dotDivAssign(Object, Object)}. */
	@Test
	public void testDotDivAssign() {
		// NB: Nothing to test; dotDiv is unimplemented.
	}

	/** Tests {@link DefaultEvaluator#dotRightDivAssign(Object, Object)}. */
	@Test
	public void testDotRightDivAssign() {
		// NB: Nothing to test; dotRightDiv is unimplemented.
	}

	/** Tests {@link DefaultEvaluator#addAssign(Object, Object)}. */
	@Test
	public void testAddAssign() {
		final Variable v = new Variable("v");
		e.set(v, "Hello,");
		assertAssigned("Hello, world", v, e.addAssign(v, " world"));
		e.set(v, 4); assertAssigned(10, v, e.addAssign(v, 6));
		e.set(v, 4L); assertAssigned(10L, v, e.addAssign(v, 6L));
		e.set(v, 1.5f); assertAssigned(3.6f, v, e.addAssign(v, 2.1f));
		e.set(v, 1.5d); assertAssigned(3.6d, v, e.addAssign(v, 2.1d));
		e.set(v, bi(4)); assertAssigned(bi(10), v, e.addAssign(v, bi(6)));
		e.set(v, bd(1.5)); assertAssigned(bd(3.6), v, e.addAssign(v, bd(2.1)));
	}

	/** Tests {@link DefaultEvaluator#subAssign(Object, Object)}. */
	@Test
	public void testSubAssign() {
		final Variable v = new Variable("v");
		e.set(v, 10); assertAssigned(4, v, e.subAssign(v, 6));
		e.set(v, 10L); assertAssigned(4L, v, e.subAssign(v, 6L));
		e.set(v, 3.6f); assertAssigned(1.5f, v, e.subAssign(v, 2.1f));
		e.set(v, 3.6d); assertAssigned(1.5d, v, e.subAssign(v, 2.1d));
		e.set(v, bi(10)); assertAssigned(bi(4), v, e.subAssign(v, bi(6)));
		e.set(v, bd(3.6)); assertAssigned(bd(1.5), v, e.subAssign(v, bd(2.1)));
	}

	/** Tests {@link DefaultEvaluator#andAssign(Object, Object)}. */
	@Test
	public void testAndAssign() {
		final Variable v = new Variable("v");
		e.set(v, 0xcafebabe); assertAssigned(0xcaacbaae, v, e.andAssign(v, 0xdeadbeef));
		e.set(v, 0x0d0e0a0d0c0a0f0eL); assertAssigned(0L, v, e.andAssign(v, 0xb0e0e0f0b0a0b0e0L));
		e.set(v, bi(0xcafebabeL)); assertAssigned(bi(0xcaacbaaeL), v, e.andAssign(v, bi(0xdeadbeefL)));
	}

	/** Tests {@link DefaultEvaluator#orAssign(Object, Object)}. */
	@Test
	public void testOrAssign() {
		final Variable v = new Variable("v");
		e.set(v, 0xcafebabe); assertAssigned(0xdeffbeff, v, e.orAssign(v, 0xdeadbeef));
		e.set(v, 0x0d0e0a0d0c0a0f0eL); assertAssigned(0xbdeeeafdbcaabfeeL, v, e.orAssign(v, 0xb0e0e0f0b0a0b0e0L));
		e.set(v, bi(0xcafebabeL)); assertAssigned(bi(0xdeffbeffL), v, e.orAssign(v, bi(0xdeadbeefL)));
	}

	/** Tests {@link DefaultEvaluator#leftShiftAssign(Object, Object)}. */
	@Test
	public void testLeftShiftAssign() {
		final Variable v = new Variable("v");
		e.set(v, 0xcafebabe); assertAssigned(0xafebabe0, v, e.leftShiftAssign(v, 4));
		e.set(v, 0xdeadcafebeefbabeL); assertAssigned(0xdcafebeefbabe000L, v, e.leftShiftAssign(v, 12));
		e.set(v, bi(57)); assertAssigned(bi(7296), v, e.leftShiftAssign(v, 7));
	}

	/** Tests {@link DefaultEvaluator#rightShiftAssign(Object, Object)}. */
	@Test
	public void testRightShiftAssign() {
		final Variable v = new Variable("v");
		e.set(v, 0xcafebabe); assertAssigned(0xfcafebab, v, e.rightShiftAssign(v, 4));
		e.set(v, 0xdeadcafebeefbabeL); assertAssigned(0xfffdeadcafebeefbL, v, e.rightShiftAssign(v, 12));
		e.set(v, bi(8920)); assertAssigned(bi(278), v, e.rightShiftAssign(v, 5));
	}

	/** Tests {@link DefaultEvaluator#unsignedRightShiftAssign(Object, Object)}. */
	@Test
	public void testUnsignedRightShiftAssign() {
		final Variable v = new Variable("v");
		e.set(v, 0xcafebabe); assertAssigned(0x0cafebab, v, e.unsignedRightShiftAssign(v, 4));
		e.set(v, 0xdeadcafebeefbabeL); assertAssigned(0x000deadcafebeefbL, v, e.unsignedRightShiftAssign(v, 12));
	}

	// -- Helper methods --

	/** Widens the given object to an {@link Object}. */
	private Object o(final Object o) { return o; }

	private BigInteger bi(final Object o) { return new BigInteger(o.toString()); }
	private BigDecimal bd(final Object o) { return new BigDecimal(o.toString()); }

	private void assertAssigned(final Object expected, final Variable v,
		final Object result)
	{
		assertSame(v, result);
		assertEquals(expected, e.get(v));
	}

}
