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
 * The value types supported by the {@link ExpressionCompiler}.
 * <p>
 * All integer values are represented as {@code long}, and all floating point
 * values as {@code double}. When the two are mixed, {@code long} is promoted to
 * {@code double}, as in Java.
 * </p>
 *
 * @author Curtis Rueden
 */
public enum Type {

	BOOLEAN, LONG, DOUBLE;

	/** Gets whether this type is numeric (i.e., {@link #LONG} or {@link #DOUBLE}). */
	public boolean isNumeric() {
		return this != BOOLEAN;
	}

	/**
	 * Gets the type corresponding to the given value.
	 *
	 * @param value The value whose type should be determined.
	 * @return {@link #BOOLEAN} for a {@link Boolean}; {@link #LONG} for a
	 *         {@link Byte}, {@link Short}, {@link Integer} or {@link Long};
	 *         {@link #DOUBLE} for a {@link Float} or {@link Double}; or null if
	 *         the value is of any other type.
	 */
	public static Type of(final Object value) {
		if (value instanceof Boolean) return BOOLEAN;
		if (value instanceof Long || value instanceof Integer ||
			value instanceof Short || value instanceof Byte) return LONG;
		if (value instanceof Double || value instanceof Float) return DOUBLE;
		return null;
	}
}
