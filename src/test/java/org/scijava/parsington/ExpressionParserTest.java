/*
 * #%L
 * Parsington: the SciJava mathematical expression parser.
 * %%
 * Copyright (C) 2015 - 2023 Board of Regents of the University of
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

package org.scijava.parsington;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.LinkedList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.scijava.parsington.Operator.Associativity;

/**
 * Tests {@link ExpressionParser}.
 *
 * @author Curtis Rueden
 */
public class ExpressionParserTest extends AbstractTest {

	@Test
	public void testStrings() {
		final ExpressionParser parser = new ExpressionParser();
		final LinkedList<Object> queue = parser.parsePostfix("'hello'*\"world\"");
		// "hello" "world" *
		assertNotNull(queue);
		assertEquals(3, queue.size());
		assertString("hello", queue.pop());
		assertString("world", queue.pop());
		assertSame(Operators.MUL, queue.pop());
	}

	@Test
	public void testNumbers() {
		final ExpressionParser parser = new ExpressionParser();
		final LinkedList<Object> queue = parser.parsePostfix("1.1+2L+3f+4-+5d-6");
		// 1.1 2L + 3f + 4 + +5d - 6 -
		assertNotNull(queue);
		assertEquals(11, queue.size());
		assertNumber(1.1, queue.pop());
		assertNumber(2L, queue.pop());
		assertSame(Operators.ADD, queue.pop());
		assertNumber(3f, queue.pop());
		assertSame(Operators.ADD, queue.pop());
		assertNumber(4, queue.pop());
		assertSame(Operators.ADD, queue.pop());
		assertNumber(+5d, queue.pop());
		assertSame(Operators.SUB, queue.pop());
		assertNumber(6, queue.pop());
		assertSame(Operators.SUB, queue.pop());
	}

	@Test
	public void testVariables() {
		final ExpressionParser parser = new ExpressionParser();
		final LinkedList<Object> queue = parser.parsePostfix("x+y-_z");
		assertNotNull(queue);
		assertEquals(5, queue.size());
		assertVariable("x", queue.pop());
		assertVariable("y", queue.pop());
		assertSame(Operators.ADD, queue.pop());
		assertVariable("_z", queue.pop());
		assertSame(Operators.SUB, queue.pop());
	}

	/** Tests each individual standard operator. */
	@Test
	public void testOperatorsIndividual() {
		final ExpressionParser p = new ExpressionParser();
		assertBinary(p, "a", Operators.DOT, "b", "a.b");
		assertUnary(p, "a", Operators.TRANSPOSE, "a'");
		assertUnary(p, "a", Operators.DOT_TRANSPOSE, "a.'");
		assertBinary(p, "a", Operators.POW, "b", "a^b");
		assertBinary(p, "a", Operators.DOT_POW, "b", "a.^b");
		assertUnary(p, "a", Operators.PRE_INC, "++a");
		assertUnary(p, "a", Operators.POST_INC, "a++");
		assertUnary(p, "a", Operators.PRE_DEC, "--a");
		assertUnary(p, "a", Operators.POST_DEC, "a--");
		assertUnary(p, "a", Operators.POS, "+a");
		assertUnary(p, "a", Operators.NEG, "-a");
		assertUnary(p, "a", Operators.COMPLEMENT, "~a");
		assertUnary(p, "a", Operators.NOT, "!a");
		assertBinary(p, "a", Operators.MUL, "b", "a*b");
		assertBinary(p, "a", Operators.DIV, "b", "a/b");
		assertBinary(p, "a", Operators.MOD, "b", "a%b");
		assertBinary(p, "a", Operators.ADD, "b", "a+b");
		assertBinary(p, "a", Operators.SUB, "b", "a-b");
		assertBinary(p, "a", Operators.LEFT_SHIFT, "b", "a<<b");
		assertBinary(p, "a", Operators.RIGHT_SHIFT, "b", "a>>b");
		assertBinary(p, "a", Operators.UNSIGNED_RIGHT_SHIFT, "b", "a>>>b");
		assertBinary(p, "a", Operators.COLON, "b", "a:b");
		assertBinary(p, "a", Operators.LESS_THAN, "b", "a<b");
		assertBinary(p, "a", Operators.GREATER_THAN, "b", "a>b");
		assertBinary(p, "a", Operators.LESS_THAN_OR_EQUAL, "b", "a<=b");
		assertBinary(p, "a", Operators.GREATER_THAN_OR_EQUAL, "b", "a>=b");
		assertBinary(p, "a", Operators.INSTANCEOF, "b", "a instanceof b");
		assertBinary(p, "a", Operators.EQUAL, "b", "a==b");
		assertBinary(p, "a", Operators.NOT_EQUAL, "b", "a!=b");
		assertBinary(p, "a", Operators.BITWISE_AND, "b", "a&b");
		assertBinary(p, "a", Operators.BITWISE_OR, "b", "a|b");
		assertBinary(p, "a", Operators.LOGICAL_AND, "b", "a&&b");
		assertBinary(p, "a", Operators.LOGICAL_OR, "b", "a||b");
		assertBinary(p, "a", Operators.ASSIGN, "b", "a=b");
		assertBinary(p, "a", Operators.POW_ASSIGN, "b", "a^=b");
		assertBinary(p, "a", Operators.DOT_POW_ASSIGN, "b", "a.^=b");
		assertBinary(p, "a", Operators.MUL_ASSIGN, "b", "a*=b");
		assertBinary(p, "a", Operators.DIV_ASSIGN, "b", "a/=b");
		assertBinary(p, "a", Operators.MOD_ASSIGN, "b", "a%=b");
		assertBinary(p, "a", Operators.RIGHT_DIV_ASSIGN, "b", "a\\=b");
		assertBinary(p, "a", Operators.DOT_DIV_ASSIGN, "b", "a./=b");
		assertBinary(p, "a", Operators.DOT_RIGHT_DIV_ASSIGN, "b", "a.\\=b");
		assertBinary(p, "a", Operators.ADD_ASSIGN, "b", "a+=b");
		assertBinary(p, "a", Operators.SUB_ASSIGN, "b", "a-=b");
		assertBinary(p, "a", Operators.AND_ASSIGN, "b", "a&=b");
		assertBinary(p, "a", Operators.OR_ASSIGN, "b", "a|=b");
		assertBinary(p, "a", Operators.LEFT_SHIFT_ASSIGN, "b", "a<<=b");
		assertBinary(p, "a", Operators.RIGHT_SHIFT_ASSIGN, "b", "a>>=b");
		assertBinary(p, "a", Operators.UNSIGNED_RIGHT_SHIFT_ASSIGN, "b", "a>>>=b");
	}

	/** Tests all the numeric operators in a single expression. */
	@Test
	public void testMathOperators() {
		final String expression =
			"(a|=b)|(c&=d)&(e>>>=f)>>>(g>>=h)>>(i<<=j)<<(k-=l)-(m+=n)"
				+ "+(o.\\=p).\\(q./=r)./(s\\=t)\\(u%=v)%(w/=x)/(y*=z)"
				+ "*(aa.^=bb).^(cc^=dd)^f(~ee--,-ff++,+--gg',++hh.')";
		final ExpressionParser parser = new ExpressionParser();
		final LinkedList<Object> queue = parser.parsePostfix(expression);
		// a b |= (1) c d &= (1) e f >>>= (1) g h >>= (1) >>> i j <<= (1) >> k l
		// -= (1) m n += (1) - o p .\= (1) q r ./= (1) .\ s t \= (1) ./ u v %=
		// (1) \ w x /= (1) % y z *= (1) / aa bb .^= (1) cc dd ^= (1) f ee -- ~
		// ff ++ - gg ' -- + hh .' ++ (4) <Fn> ^ .^ * + << & |
		assertNotNull(queue);
		assertEquals(91, queue.size());
		assertVariable("a", queue.pop());
		assertVariable("b", queue.pop());
		assertSame(Operators.OR_ASSIGN, queue.pop());
		assertGroup(Operators.PARENS, 1, queue.pop());
		assertVariable("c", queue.pop());
		assertVariable("d", queue.pop());
		assertSame(Operators.AND_ASSIGN, queue.pop());
		assertGroup(Operators.PARENS, 1, queue.pop());
		assertVariable("e", queue.pop());
		assertVariable("f", queue.pop());
		assertSame(Operators.UNSIGNED_RIGHT_SHIFT_ASSIGN, queue.pop());
		assertGroup(Operators.PARENS, 1, queue.pop());
		assertVariable("g", queue.pop());
		assertVariable("h", queue.pop());
		assertSame(Operators.RIGHT_SHIFT_ASSIGN, queue.pop());
		assertGroup(Operators.PARENS, 1, queue.pop());
		assertSame(Operators.UNSIGNED_RIGHT_SHIFT, queue.pop());
		assertVariable("i", queue.pop());
		assertVariable("j", queue.pop());
		assertSame(Operators.LEFT_SHIFT_ASSIGN, queue.pop());
		assertGroup(Operators.PARENS, 1, queue.pop());
		assertSame(Operators.RIGHT_SHIFT, queue.pop());
		assertVariable("k", queue.pop());
		assertVariable("l", queue.pop());
		assertSame(Operators.SUB_ASSIGN, queue.pop());
		assertGroup(Operators.PARENS, 1, queue.pop());
		assertVariable("m", queue.pop());
		assertVariable("n", queue.pop());
		assertSame(Operators.ADD_ASSIGN, queue.pop());
		assertGroup(Operators.PARENS, 1, queue.pop());
		assertSame(Operators.SUB, queue.pop());
		assertVariable("o", queue.pop());
		assertVariable("p", queue.pop());
		assertSame(Operators.DOT_RIGHT_DIV_ASSIGN, queue.pop());
		assertGroup(Operators.PARENS, 1, queue.pop());
		assertVariable("q", queue.pop());
		assertVariable("r", queue.pop());
		assertSame(Operators.DOT_DIV_ASSIGN, queue.pop());
		assertGroup(Operators.PARENS, 1, queue.pop());
		assertSame(Operators.DOT_RIGHT_DIV, queue.pop());
		assertVariable("s", queue.pop());
		assertVariable("t", queue.pop());
		assertSame(Operators.RIGHT_DIV_ASSIGN, queue.pop());
		assertGroup(Operators.PARENS, 1, queue.pop());
		assertSame(Operators.DOT_DIV, queue.pop());
		assertVariable("u", queue.pop());
		assertVariable("v", queue.pop());
		assertSame(Operators.MOD_ASSIGN, queue.pop());
		assertGroup(Operators.PARENS, 1, queue.pop());
		assertSame(Operators.RIGHT_DIV, queue.pop());
		assertVariable("w", queue.pop());
		assertVariable("x", queue.pop());
		assertSame(Operators.DIV_ASSIGN, queue.pop());
		assertGroup(Operators.PARENS, 1, queue.pop());
		assertSame(Operators.MOD, queue.pop());
		assertVariable("y", queue.pop());
		assertVariable("z", queue.pop());
		assertSame(Operators.MUL_ASSIGN, queue.pop());
		assertGroup(Operators.PARENS, 1, queue.pop());
		assertSame(Operators.DIV, queue.pop());
		assertVariable("aa", queue.pop());
		assertVariable("bb", queue.pop());
		assertSame(Operators.DOT_POW_ASSIGN, queue.pop());
		assertGroup(Operators.PARENS, 1, queue.pop());
		assertVariable("cc", queue.pop());
		assertVariable("dd", queue.pop());
		assertSame(Operators.POW_ASSIGN, queue.pop());
		assertGroup(Operators.PARENS, 1, queue.pop());
		assertVariable("f", queue.pop());
		assertVariable("ee", queue.pop());
		assertSame(Operators.POST_DEC, queue.pop());
		assertSame(Operators.COMPLEMENT, queue.pop());
		assertVariable("ff", queue.pop());
		assertSame(Operators.POST_INC, queue.pop());
		assertSame(Operators.NEG, queue.pop());
		assertVariable("gg", queue.pop());
		assertSame(Operators.TRANSPOSE, queue.pop());
		assertSame(Operators.PRE_DEC, queue.pop());
		assertSame(Operators.POS, queue.pop());
		assertVariable("hh", queue.pop());
		assertSame(Operators.DOT_TRANSPOSE, queue.pop());
		assertSame(Operators.PRE_INC, queue.pop());
		assertGroup(Operators.PARENS, 4, queue.pop());
		assertFunction(queue.pop());
		assertSame(Operators.POW, queue.pop());
		assertSame(Operators.DOT_POW, queue.pop());
		assertSame(Operators.MUL, queue.pop());
		assertSame(Operators.ADD, queue.pop());
		assertSame(Operators.LEFT_SHIFT, queue.pop());
		assertSame(Operators.BITWISE_AND, queue.pop());
		assertSame(Operators.BITWISE_OR, queue.pop());
	}

	/** Tests all the boolean operators in a single expression. */
	@Test
	public void testLogicOperators() {
		final String expression =
			"a<b || c>d && e<=f || g>=h && i==j || k!=l && m instanceof n || !o";
		final ExpressionParser parser = new ExpressionParser();
		final LinkedList<Object> queue = parser.parsePostfix(expression);
		// a b |= c d &= e f >>>= g h >>= >>> i j <<= >> k l -= m n += - o p .\=
		// q r ./= .\ s t \= ./ u v %= \ w x /= % y z *= / aa bb .^= cc dd ^= ee
		// -- ~ ff ++ - gg ' -- + hh .' ++ f ^ .^ * + << & |
		assertNotNull(queue);
		assertEquals(30, queue.size());
		assertVariable("a", queue.pop());
		assertVariable("b", queue.pop());
		assertSame(Operators.LESS_THAN, queue.pop());
		assertVariable("c", queue.pop());
		assertVariable("d", queue.pop());
		assertSame(Operators.GREATER_THAN, queue.pop());
		assertVariable("e", queue.pop());
		assertVariable("f", queue.pop());
		assertSame(Operators.LESS_THAN_OR_EQUAL, queue.pop());
		assertSame(Operators.LOGICAL_AND, queue.pop());
		assertSame(Operators.LOGICAL_OR, queue.pop());
		assertVariable("g", queue.pop());
		assertVariable("h", queue.pop());
		assertSame(Operators.GREATER_THAN_OR_EQUAL, queue.pop());
		assertVariable("i", queue.pop());
		assertVariable("j", queue.pop());
		assertSame(Operators.EQUAL, queue.pop());
		assertSame(Operators.LOGICAL_AND, queue.pop());
		assertSame(Operators.LOGICAL_OR, queue.pop());
		assertVariable("k", queue.pop());
		assertVariable("l", queue.pop());
		assertSame(Operators.NOT_EQUAL, queue.pop());
		assertVariable("m", queue.pop());
		assertVariable("n", queue.pop());
		assertSame(Operators.INSTANCEOF, queue.pop());
		assertSame(Operators.LOGICAL_AND, queue.pop());
		assertSame(Operators.LOGICAL_OR, queue.pop());
		assertVariable("o", queue.pop());
		assertSame(Operators.NOT, queue.pop());
		assertSame(Operators.LOGICAL_OR, queue.pop());
	}

	@Test
	public void testUnaryOperators1() {
		final ExpressionParser parser = new ExpressionParser();
		final LinkedList<Object> queue = parser.parsePostfix("-+a");
		// a + -
		assertNotNull(queue);
		assertEquals(3, queue.size());
		assertVariable("a", queue.pop());
		assertSame(Operators.POS, queue.pop());
		assertSame(Operators.NEG, queue.pop());
	}

	@Test
	public void testUnaryOperators2() {
		final ExpressionParser parser = new ExpressionParser();
		final LinkedList<Object> queue = parser.parsePostfix("a+-b");
		// a b - +
		assertNotNull(queue);
		assertEquals(4, queue.size());
		assertVariable("a", queue.pop());
		assertVariable("b", queue.pop());
		assertSame(Operators.NEG, queue.pop());
		assertSame(Operators.ADD, queue.pop());
	}

	@Test
	public void testUnaryOperators3() {
		final ExpressionParser parser = new ExpressionParser();
		final LinkedList<Object> queue = parser.parsePostfix("a-+-b");
		// a b - + -
		assertNotNull(queue);
		assertEquals(5, queue.size());
		assertVariable("a", queue.pop());
		assertVariable("b", queue.pop());
		assertSame(Operators.NEG, queue.pop());
		assertSame(Operators.POS, queue.pop());
		assertSame(Operators.SUB, queue.pop());
	}

	@Test
	public void testUnaryOperators4() {
		final ExpressionParser parser = new ExpressionParser();
		final LinkedList<Object> queue = parser.parsePostfix("-+a-+-b");
		// a + - b - + -
		assertNotNull(queue);
		assertEquals(7, queue.size());
		assertVariable("a", queue.pop());
		assertSame(Operators.POS, queue.pop());
		assertSame(Operators.NEG, queue.pop());
		assertVariable("b", queue.pop());
		assertSame(Operators.NEG, queue.pop());
		assertSame(Operators.POS, queue.pop());
		assertSame(Operators.SUB, queue.pop());
	}

	@Test
	public void testNullaryGroup() {
		final ExpressionParser parser = new ExpressionParser();
		final LinkedList<Object> queue = parser.parsePostfix("()");
		// (0)
		assertNotNull(queue);
		assertEquals(1, queue.size());
		assertGroup(Operators.PARENS, 0, queue.pop());
	}

	@Test
	public void testUnaryGroup() {
		final ExpressionParser parser = new ExpressionParser();
		final LinkedList<Object> queue = parser.parsePostfix("(a)");
		// a (1)
		assertNotNull(queue);
		assertEquals(2, queue.size());
		assertVariable("a", queue.pop());
		assertGroup(Operators.PARENS, 1, queue.pop());
	}

	@Test
	public void testNestedGroups() {
		final ExpressionParser parser = new ExpressionParser();
		final LinkedList<Object> queue = parser.parsePostfix("(())");
		// (0) (1)
		assertNotNull(queue);
		assertEquals(2, queue.size());
		assertGroup(Operators.PARENS, 0, queue.pop());
		assertGroup(Operators.PARENS, 1, queue.pop());
	}

	@Test
	public void testNullaryFunction() {
		final ExpressionParser parser = new ExpressionParser();
		final LinkedList<Object> queue = parser.parsePostfix("f()");
		// f (0) <Fn>
		assertNotNull(queue);
		assertEquals(3, queue.size());
		assertVariable("f", queue.pop());
		assertGroup(Operators.PARENS, 0, queue.pop());
		assertFunction(queue.pop());
	}

	@Test
	public void testUnaryFunction() {
		final ExpressionParser parser = new ExpressionParser();
		final LinkedList<Object> queue = parser.parsePostfix("f(a)");
		// f a (1) <Fn>
		assertNotNull(queue);
		assertVariable("f", queue.pop());
		assertVariable("a", queue.pop());
		assertGroup(Operators.PARENS, 1, queue.pop());
		assertFunction(queue.pop());
	}

	@Test
	public void testBinaryFunction() {
		final ExpressionParser parser = new ExpressionParser();
		final LinkedList<Object> queue = parser.parsePostfix("f(a,b)");
		// f a b (2) <Fn>
		assertNotNull(queue);
		assertEquals(5, queue.size());
		assertVariable("f", queue.pop());
		assertVariable("a", queue.pop());
		assertVariable("b", queue.pop());
		assertGroup(Operators.PARENS, 2, queue.pop());
		assertFunction(queue.pop());
	}

	@Test
	public void testTernaryFunction() {
		final ExpressionParser parser = new ExpressionParser();
		final LinkedList<Object> queue = parser.parsePostfix("f(a,b,c)");
		// f a b c (3) <Fn>
		assertNotNull(queue);
		assertEquals(6, queue.size());
		assertVariable("f", queue.pop());
		assertVariable("a", queue.pop());
		assertVariable("b", queue.pop());
		assertVariable("c", queue.pop());
		assertGroup(Operators.PARENS, 3, queue.pop());
		assertFunction(queue.pop());
	}

	@Test
	public void testNestedFunctions() {
		final ExpressionParser parser = new ExpressionParser();
		final LinkedList<Object> queue =
			parser.parsePostfix("f(g(),a,h(b),i(c,d))");
		// f g (0) <Fn> a h b (1) <Fn> i c d (2) <Fn> (4) <Fn>
		assertNotNull(queue);
		assertEquals(16, queue.size());
		assertVariable("f", queue.pop());
		assertVariable("g", queue.pop());
		assertGroup(Operators.PARENS, 0, queue.pop());
		assertFunction(queue.pop());
		assertVariable("a", queue.pop());
		assertVariable("h", queue.pop());
		assertVariable("b", queue.pop());
		assertGroup(Operators.PARENS, 1, queue.pop());
		assertFunction(queue.pop());
		assertVariable("i", queue.pop());
		assertVariable("c", queue.pop());
		assertVariable("d", queue.pop());
		assertGroup(Operators.PARENS, 2, queue.pop());
		assertFunction(queue.pop());
		assertGroup(Operators.PARENS, 4, queue.pop());
		assertFunction(queue.pop());
	}

	@Test
	public void testFunctionParens1() {
		final ExpressionParser parser = new ExpressionParser();
		final LinkedList<Object> queue = parser.parsePostfix("a.b(c)");
		// a b . c (1) <Fn>
		assertNotNull(queue);
		assertEquals(6, queue.size());
		assertVariable("a", queue.pop());
		assertVariable("b", queue.pop());
		assertSame(Operators.DOT, queue.pop());
		assertVariable("c", queue.pop());
		assertGroup(Operators.PARENS, 1, queue.pop());
		assertFunction(queue.pop());
	}

	@Test
	public void testFunctionParens2() {
		final ExpressionParser parser = new ExpressionParser();
		final LinkedList<Object> queue = parser.parsePostfix("a(b).c");
		// a b (1) <Fn> c .
		assertNotNull(queue);
		assertEquals(6, queue.size());
		assertVariable("a", queue.pop());
		assertVariable("b", queue.pop());
		assertGroup(Operators.PARENS, 1, queue.pop());
		assertFunction(queue.pop());
		assertVariable("c", queue.pop());
		assertSame(Operators.DOT, queue.pop());
	}

	@Test
	public void testFunctionParens3() {
		final ExpressionParser parser = new ExpressionParser();
		final LinkedList<Object> queue = parser.parsePostfix("a.b(c).d");
		// a b . c (1) <Fn> d .
		assertNotNull(queue);
		assertEquals(8, queue.size());
		assertVariable("a", queue.pop());
		assertVariable("b", queue.pop());
		assertSame(Operators.DOT, queue.pop());
		assertVariable("c", queue.pop());
		assertGroup(Operators.PARENS, 1, queue.pop());
		assertFunction(queue.pop());
		assertVariable("d", queue.pop());
		assertSame(Operators.DOT, queue.pop());
	}

	@Test
	public void testFunctionParens4() {
		final List<Operator> operators = Operators.standardList();
		final Operator atOp = new Operator("@", 2, Associativity.LEFT, 100);
		operators.add(atOp);
		final ExpressionParser parser = new ExpressionParser(operators);
		final LinkedList<Object> queue = parser.parsePostfix("a@b(c)@d");
		// a b @ c (1) d @ <Fn>
		assertNotNull(queue);
		assertEquals(8, queue.size());
		assertVariable("a", queue.pop());
		assertVariable("b", queue.pop());
		assertSame(atOp, queue.pop());
		assertVariable("c", queue.pop());
		assertGroup(Operators.PARENS, 1, queue.pop());
		assertVariable("d", queue.pop());
		assertSame(atOp, queue.pop());
		assertFunction(queue.pop());
	}

	@Test
	public void testFunctionBrackets1() {
		final ExpressionParser parser = new ExpressionParser();
		final LinkedList<Object> queue = parser.parsePostfix("a.b[c]");
		// a b . c [1] <Fn>
		assertNotNull(queue);
		assertEquals(6, queue.size());
		assertVariable("a", queue.pop());
		assertVariable("b", queue.pop());
		assertSame(Operators.DOT, queue.pop());
		assertVariable("c", queue.pop());
		assertGroup(Operators.BRACKETS, 1, queue.pop());
		assertFunction(queue.pop());
	}

	@Test
	public void testFunctionBrackets2() {
		final ExpressionParser parser = new ExpressionParser();
		final LinkedList<Object> queue = parser.parsePostfix("a[b].c");
		// a b [1] <Fn> c .
		assertNotNull(queue);
		assertEquals(6, queue.size());
		assertVariable("a", queue.pop());
		assertVariable("b", queue.pop());
		assertGroup(Operators.BRACKETS, 1, queue.pop());
		assertFunction(queue.pop());
		assertVariable("c", queue.pop());
		assertSame(Operators.DOT, queue.pop());
	}

	@Test
	public void testFunctionBrackets3() {
		final ExpressionParser parser = new ExpressionParser();
		final LinkedList<Object> queue = parser.parsePostfix("a.b[c].d");
		// a b . c [1] <Fn> d .
		assertNotNull(queue);
		assertEquals(8, queue.size());
		assertVariable("a", queue.pop());
		assertVariable("b", queue.pop());
		assertSame(Operators.DOT, queue.pop());
		assertVariable("c", queue.pop());
		assertGroup(Operators.BRACKETS, 1, queue.pop());
		assertFunction(queue.pop());
		assertVariable("d", queue.pop());
		assertSame(Operators.DOT, queue.pop());
	}

	@Test
	public void testFunctionBrackets4() {
		final List<Operator> operators = Operators.standardList();
		final Operator atOp = new Operator("@", 2, Associativity.LEFT, 100);
		operators.add(atOp);
		final ExpressionParser parser = new ExpressionParser(operators);
		final LinkedList<Object> queue = parser.parsePostfix("a@b[c]@d");
		// a b @ c [1] d @ <Fn>
		assertNotNull(queue);
		assertEquals(8, queue.size());
		assertVariable("a", queue.pop());
		assertVariable("b", queue.pop());
		assertSame(atOp, queue.pop());
		assertVariable("c", queue.pop());
		assertGroup(Operators.BRACKETS, 1, queue.pop());
		assertVariable("d", queue.pop());
		assertSame(atOp, queue.pop());
		assertFunction(queue.pop());
	}

	@Test
	public void testFunctionBraces1() {
		final ExpressionParser parser = new ExpressionParser();
		final LinkedList<Object> queue = parser.parsePostfix("a.b{c}");
		// a b . c {1} <Fn>
		assertNotNull(queue);
		assertEquals(6, queue.size());
		assertVariable("a", queue.pop());
		assertVariable("b", queue.pop());
		assertSame(Operators.DOT, queue.pop());
		assertVariable("c", queue.pop());
		assertGroup(Operators.BRACES, 1, queue.pop());
		assertFunction(queue.pop());
	}

	@Test
	public void testFunctionBraces2() {
		final ExpressionParser parser = new ExpressionParser();
		final LinkedList<Object> queue = parser.parsePostfix("a{b}.c");
		// a b {1} <Fn> c .
		assertNotNull(queue);
		assertEquals(6, queue.size());
		assertVariable("a", queue.pop());
		assertVariable("b", queue.pop());
		assertGroup(Operators.BRACES, 1, queue.pop());
		assertFunction(queue.pop());
		assertVariable("c", queue.pop());
		assertSame(Operators.DOT, queue.pop());
	}

	@Test
	public void testFunctionBraces3() {
		final ExpressionParser parser = new ExpressionParser();
		final LinkedList<Object> queue = parser.parsePostfix("a.b{c}.d");
		// a b . c {1} <Fn> d .
		assertNotNull(queue);
		assertEquals(8, queue.size());
		assertVariable("a", queue.pop());
		assertVariable("b", queue.pop());
		assertSame(Operators.DOT, queue.pop());
		assertVariable("c", queue.pop());
		assertGroup(Operators.BRACES, 1, queue.pop());
		assertFunction(queue.pop());
		assertVariable("d", queue.pop());
		assertSame(Operators.DOT, queue.pop());
	}

	@Test
	public void testFunctionBraces4() {
		final List<Operator> operators = Operators.standardList();
		final Operator atOp = new Operator("@", 2, Associativity.LEFT, 100);
		operators.add(atOp);
		final ExpressionParser parser = new ExpressionParser(operators);
		final LinkedList<Object> queue = parser.parsePostfix("a@b{c}@d");
		// a b @ c {1} d @ <Fn>
		assertNotNull(queue);
		assertEquals(8, queue.size());
		assertVariable("a", queue.pop());
		assertVariable("b", queue.pop());
		assertSame(atOp, queue.pop());
		assertVariable("c", queue.pop());
		assertGroup(Operators.BRACES, 1, queue.pop());
		assertVariable("d", queue.pop());
		assertSame(atOp, queue.pop());
		assertFunction(queue.pop());
	}

	@Test
	public void testMixedFunctions() {
		final ExpressionParser parser = new ExpressionParser();
		final LinkedList<Object> queue = parser.parsePostfix("a[b(c), d{3:4}, 2]");
		// a b c (1) <Fn> d 3 4 : {1} <Fn> 2 [3] <Fn>
		assertNotNull(queue);
		assertEquals(14, queue.size());
		assertVariable("a", queue.pop());
		assertVariable("b", queue.pop());
		assertVariable("c", queue.pop());
		assertGroup(Operators.PARENS, 1, queue.pop());
		assertFunction(queue.pop());
		assertVariable("d", queue.pop());
		assertNumber(3, queue.pop());
		assertNumber(4, queue.pop());
		assertSame(Operators.COLON, queue.pop());
		assertGroup(Operators.BRACES, 1, queue.pop());
		assertFunction(queue.pop());
		assertNumber(2, queue.pop());
		assertGroup(Operators.BRACKETS, 3, queue.pop());
		assertFunction(queue.pop());
	}

	@Test
	public void testParameterSyntax() {
		final ExpressionParser parser = new ExpressionParser();
		final LinkedList<Object> queue = parser.parsePostfix(
			"(choices={'quick brown fox', 'lazy dog'}, persist=false, value=5)");
		// choices 'quick brown fox' 'lazy dog' {2} = persist false = value 5 = (3)
		assertNotNull(queue);
		assertVariable("choices", queue.pop());
		assertString("quick brown fox", queue.pop());
		assertString("lazy dog", queue.pop());
		assertGroup(Operators.BRACES, 2, queue.pop());
		assertSame(Operators.ASSIGN, queue.pop());
		assertVariable("persist", queue.pop());
		assertSame(false, queue.pop());
		assertSame(Operators.ASSIGN, queue.pop());
		assertVariable("value", queue.pop());
		assertNumber(5, queue.pop());
		assertSame(Operators.ASSIGN, queue.pop());
		assertGroup(Operators.PARENS, 3, queue.pop());
	}

	@Test
	public void testNonFunctionGroup() {
		final ExpressionParser parser = new ExpressionParser();
		final LinkedList<Object> queue = parser.parsePostfix("f+(g)");
		// f g (1) +
		assertNotNull(queue);
		assertEquals(4, queue.size());
		assertVariable("f", queue.pop());
		assertVariable("g", queue.pop());
		assertGroup(Operators.PARENS, 1, queue.pop());
		assertSame(Operators.ADD, queue.pop());
	}

	@Test
	public void testOperatorPrecedence() {
		final ExpressionParser parser = new ExpressionParser();
		final LinkedList<Object> queue = parser.parsePostfix("a+b*c^d.^e'");
		// a b c d e .^ ^ ' * +
		assertNotNull(queue);
		assertEquals(10, queue.size());
		assertVariable("a", queue.pop());
		assertVariable("b", queue.pop());
		assertVariable("c", queue.pop());
		assertVariable("d", queue.pop());
		assertVariable("e", queue.pop());
		assertSame(Operators.DOT_POW, queue.pop());
		assertSame(Operators.POW, queue.pop());
		assertSame(Operators.TRANSPOSE, queue.pop());
		assertSame(Operators.MUL, queue.pop());
		assertSame(Operators.ADD, queue.pop());
	}

	@Test
	public void testOperatorAssociativity() {
		final ExpressionParser parser = new ExpressionParser();
		final LinkedList<Object> queue = parser.parsePostfix("(a/b/c)+(a^b^c)");
		// a b / c / (1) a b c ^ ^ (1) +
		assertNotNull(queue);
		assertEquals(13, queue.size());
		assertVariable("a", queue.pop());
		assertVariable("b", queue.pop());
		assertSame(Operators.DIV, queue.pop());
		assertVariable("c", queue.pop());
		assertSame(Operators.DIV, queue.pop());
		assertGroup(Operators.PARENS, 1, queue.pop());
		assertVariable("a", queue.pop());
		assertVariable("b", queue.pop());
		assertVariable("c", queue.pop());
		assertSame(Operators.POW, queue.pop());
		assertSame(Operators.POW, queue.pop());
		assertGroup(Operators.PARENS, 1, queue.pop());
		assertSame(Operators.ADD, queue.pop());
	}

	/** Tests multiple semicolon-separated statements. */
	@Test
	public void testMultipleStatements() {
		final ExpressionParser parser = new ExpressionParser();
		final LinkedList<Object> queue =
			parser.parsePostfix("a=1 ; b=2 ; c=3");
		// a 1 = b 2 = c 3 =
		assertNotNull(queue);
		assertEquals(9, queue.size());
		assertVariable("a", queue.pop());
		assertNumber(1, queue.pop());
		assertSame(Operators.ASSIGN, queue.pop());
		assertVariable("b", queue.pop());
		assertNumber(2, queue.pop());
		assertSame(Operators.ASSIGN, queue.pop());
		assertVariable("c", queue.pop());
		assertNumber(3, queue.pop());
		assertSame(Operators.ASSIGN, queue.pop());
	}

	/** Tests custom separator. */
	@Test
	public void testCustomElementSeparator() {
		final ExpressionParser parser = new ExpressionParser( //
			Operators.standardList(), "@@", ";");
		final LinkedList<Object> queue =
			parser.parsePostfix("f(2 @@ 3 @@ 4) + (5 @@ 6)");
		// f 2 3 4 (3) <Fn> 5 6 (2) +
		assertNotNull(queue);
		assertEquals(10, queue.size());
		assertVariable("f", queue.pop());
		assertNumber(2, queue.pop());
		assertNumber(3, queue.pop());
		assertNumber(4, queue.pop());
		assertGroup(Operators.PARENS, 3, queue.pop());
		assertFunction(queue.pop());
		assertNumber(5, queue.pop());
		assertNumber(6, queue.pop());
		assertGroup(Operators.PARENS, 2, queue.pop());
		assertSame(Operators.ADD, queue.pop());
	}

	/** Tests multiple statements with custom separator. */
	@Test
	public void testCustomGroupSeparator() {
		final ExpressionParser parser = new ExpressionParser( //
			Operators.standardList(), ",", "@@@");
		final LinkedList<Object> queue =
			parser.parsePostfix("x=1 @@@ x += 3 @@@ y = x^2");
		// x 1 = x 3 += y x 2 ^ =
		assertNotNull(queue);
		assertEquals(11, queue.size());
		assertVariable("x", queue.pop());
		assertNumber(1, queue.pop());
		assertSame(Operators.ASSIGN, queue.pop());
		assertVariable("x", queue.pop());
		assertNumber(3, queue.pop());
		assertSame(Operators.ADD_ASSIGN, queue.pop());
		assertVariable("y", queue.pop());
		assertVariable("x", queue.pop());
		assertNumber(2, queue.pop());
		assertSame(Operators.POW, queue.pop());
		assertSame(Operators.ASSIGN, queue.pop());
	}

	/** Tests customized {@link ParseOperation} function. */
	@Test
	public void testCustomParseOperation() {
		final ExpressionParser parser = new ExpressionParser( //
			(p, expression) -> new ParseOperation(p, expression)
			{

				@Override
				protected Object parseLiteral() {
					// The number five is the best number!
					return super.parseLiteral() == null ? null : 5;
				}
			});
		final LinkedList<Object> queue =
			parser.parsePostfix("('hello', 1 + 2 * 3)");
		// 5 5 5 5 * + (2)
		assertNotNull(queue);
		assertEquals(7, queue.size());
		assertNumber(5, queue.pop());
		assertNumber(5, queue.pop());
		assertNumber(5, queue.pop());
		assertNumber(5, queue.pop());
		assertSame(Operators.MUL, queue.pop());
		assertSame(Operators.ADD, queue.pop());
		assertGroup(Operators.PARENS, 2, queue.pop());
	}

	/** A more complex test of {@link ExpressionParser#parsePostfix}. */
	@Test
	public void testParsePostfix() {
		final ExpressionParser parser = new ExpressionParser();
		final LinkedList<Object> queue =
			parser.parsePostfix("a*b-c*a/func(quick,brown,fox)^foo^bar");
		// a b * c a * func quick brown fox (3) <Fn> foo bar ^ ^ / -
		assertNotNull(queue);
		assertEquals(18, queue.size());
		assertVariable("a", queue.pop());
		assertVariable("b", queue.pop());
		assertSame(Operators.MUL, queue.pop());
		assertVariable("c", queue.pop());
		assertVariable("a", queue.pop());
		assertSame(Operators.MUL, queue.pop());
		assertVariable("func", queue.pop());
		assertVariable("quick", queue.pop());
		assertVariable("brown", queue.pop());
		assertVariable("fox", queue.pop());
		assertGroup(Operators.PARENS, 3, queue.pop());
		assertFunction(queue.pop());
		assertVariable("foo", queue.pop());
		assertVariable("bar", queue.pop());
		assertSame(Operators.POW, queue.pop());
		assertSame(Operators.POW, queue.pop());
		assertSame(Operators.DIV, queue.pop());
		assertSame(Operators.SUB, queue.pop());
	}

	/** Tests empty expressions. */
	@Test
	public void testEmpty() {
		final ExpressionParser parser = new ExpressionParser();
		assertTrue(parser.parsePostfix("").isEmpty());
	}

	/** Tests expression ending with a unary minus. */
	@Test
	public void testTrailingUnaryMinus() {
		final ExpressionParser parser = new ExpressionParser();
		final LinkedList<Object> queue = parser.parsePostfix("-");
		assertNotNull(queue);
		assertEquals(1, queue.size());
		assertSame(Operators.NEG, queue.pop());
	}

	/** Tests expression ending with a binary minus. */
	@Test
	public void testTrailingBinaryMinus() {
		final ExpressionParser parser = new ExpressionParser();
		final LinkedList<Object> queue = parser.parsePostfix("1-");
		assertNotNull(queue);
		assertEquals(2, queue.size());
		assertSame(1, queue.pop());
		assertSame(Operators.SUB, queue.pop());
	}

	/** Tests expressions with extra whitespace. */
	@Test
	public void testWhitespace() {
		final ExpressionParser parser = new ExpressionParser();

		assertTrue(parser.parsePostfix(" ").isEmpty());
		assertTrue(parser.parsePostfix("  \t \n  \r").isEmpty());

		final LinkedList<Object> queue = parser.parsePostfix(" 3 ");
		assertNotNull(queue);
		assertEquals(1, queue.size());
		assertSame(3, queue.pop());
	}

	/** Tests that parsing an invalid expression fails appropriately. */
	@Test
	public void testInvalid() {
		final ExpressionParser parser = new ExpressionParser();
		assertInvalid(parser, "a a", "Invalid character at index 2");
		assertInvalid(parser, "func(,)", "Invalid character at index 5");
		assertInvalid(parser, "foo,bar",
			"Misplaced separator or mismatched groups at index 4");
		assertInvalid(parser, "(", "Mismatched groups at index 1");
		assertInvalid(parser, ")", "Mismatched group terminator ')' at index 1");
		assertInvalid(parser, ")(", "Mismatched group terminator ')' at index 1");
		assertInvalid(parser, "(()", "Mismatched groups at index 3");
	}

	// -- Helper methods --

	private void assertUnary(final ExpressionParser parser, final String var,
		final Operator op, final String expression)
	{
		final LinkedList<Object> queue = parser.parsePostfix(expression);
		assertNotNull(queue);
		assertEquals(2, queue.size());
		assertVariable(var, queue.pop());
		assertSame(op, queue.pop());
	}

	private void assertBinary(final ExpressionParser parser, final String var1,
		final Operator op, final String var2, final String expression)
	{
		final LinkedList<Object> queue = parser.parsePostfix(expression);
		assertNotNull(queue);
		assertEquals(3, queue.size());
		assertVariable(var1, queue.pop());
		assertVariable(var2, queue.pop());
		assertSame(op, queue.pop());
	}

	private void assertInvalid(final ExpressionParser parser,
		final String expression, final String message)
	{
		try {
			parser.parsePostfix(expression);
			fail("Expected IllegalArgumentException");
		}
		catch (final IllegalArgumentException exc) {
			assertEquals(message, exc.getMessage());
		}
	}

}
