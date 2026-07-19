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

package org.scijava.parsington.bench;

import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.scijava.parsington.Literals;

/**
 * Benchmarks single-token literal parsing&mdash;the hot path highlighted in
 * <a href="https://github.com/scijava/parsington/issues/24">issue #24</a>,
 * where decimal literals were disproportionately expensive because
 * {@code parseDecimal} first attempted {@code parseInteger} and paid for a
 * thrown-and-swallowed {@code NumberFormatException} on every non-integer.
 * <p>
 * This isolates {@link Literals#parseLiteral} on a single token, per literal
 * type. The {@code INT} vs {@code DOUBLE} contrast is the most sensitive
 * detector of regressions or fixes on the issue #24 path. Literal-heavy list
 * parsing through the full parser (integer-heavy vs decimal-heavy vs mixed) is
 * covered by {@link ParsingBenchmark} via the {@code *_LIST} corpus entries.
 * </p>
 *
 * @author Curtis Rueden
 */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class LiteralsBenchmark {

	/** A single literal token spanning the supported literal types. */
	public enum Token {
			INT("12345"),
			DOUBLE("12345.678"),
			SCIENTIFIC("1.2345e6"),
			LONG("12345678900L"),
			FLOAT("1.5f"),
			HEX("0x1F2A3B"),
			BINARY("0b101011001"),
			BIG_INTEGER("9987654321234567899"),
			STRING("'hello world'"),
			BOOLEAN("true");

		private final String text;

		Token(final String text) {
			this.text = text;
		}

		public String text() {
			return text;
		}
	}

	@Param
	public Token token;

	private String tokenText;

	@Setup
	public void setUp() {
		tokenText = token.text();
	}

	@Benchmark
	public Object parseSingleLiteral() {
		return Literals.parseLiteral(tokenText);
	}
}
