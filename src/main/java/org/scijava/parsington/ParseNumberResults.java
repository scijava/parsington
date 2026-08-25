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

package org.scijava.parsington;

public class ParseNumberResults {

	private static final int EMPTY = -1;

	/** Index of the group holding the entire matched literal, in every extraction context. */
	static final int GROUP_ALL = 1;

	// -- Decimal/octal literal parsing groups (see ParseNumber#extractDecimalOrOctalNumber) --
	static final int DEC_VALUE = 2;        // number text, with suffix stripped
	static final int DEC_FRACTION = 3;     // digits after the decimal point
	static final int DEC_EXPONENT = 4;     // digits (with optional sign) after E/e
	static final int DEC_SUFFIX = 5;       // D/F/L suffix character
	static final int DEC_SIGN = 6;         // leading +/- sign
	static final int DEC_INTEGER_PART = 7; // digits before the decimal point

	// -- Binary literal parsing groups (see ParseNumber#extractBinaryNumber) --
	static final int BIN_SIGN = 2;   // leading +/- sign
	static final int BIN_DIGITS = 3; // [01]+
	static final int BIN_SUFFIX = 4; // L suffix

	// -- Hexadecimal literal parsing groups (see ParseNumber#extractHexNumber) --
	static final int HEX_SIGN = 2;               // leading +/- sign
	static final int HEX_DIGITS = 3;             // [0-9a-fA-F]+
	static final int HEX_SUFFIX_OR_EXPONENT = 4; // L suffix, or (end-adjusted) the whole floating-point extension
	static final int HEX_FRACTION = 5;           // digits after the decimal point
	static final int HEX_EXPONENT_SIGN = 6;      // +/- following P/p
	static final int HEX_EXPONENT_DIGITS = 7;    // digits following P/p
	static final int HEX_FLOAT_SUFFIX = 8;       // D/F suffix
	static final int HEX_P_MARKER = 9;           // records the position of the P/p exponent marker

	private final int[] beginGroup = {EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY};
	private final int[] endGroup = {EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY};
	private int signNdx = EMPTY;

	private NumberType numberType;
	private Number number = null;

	void setSignIndex(int signNdx) {
		this.signNdx = signNdx;
	}

	void setSignGroup(int signGroup) {
		if (signNdx != EMPTY) {
			beginGroup[signGroup] = signNdx;
			endGroup[signGroup] = signNdx + 1;
		}
	}

	String getGroup(String in, int group) {
		if ((beginGroup[group] != EMPTY) && (endGroup[group] != EMPTY)) {
			return in.substring(beginGroup[group], endGroup[group]);
		} else {
			return "";
		}
	}

	int getLength() {
		return getGroupLength(GROUP_ALL);
	}

	Number getNumber() {
		return number;
	}

	void setNumber(Number number) {
		this.number = number;
	}

	void setBegin(int ndx) {
		setBeginGroup(GROUP_ALL, ndx);
	}

	void setEnd(int ndx) {
		setEndGroup(GROUP_ALL, ndx);
	}

	int setBeginGroup(int group, int ndx) {
		beginGroup[group] = ndx;
		return group;
	}

	int getBeginGroup(int group) {
		return beginGroup[group];
	}

	void setEndGroup(int group, int ndx) {
		endGroup[group] = ndx;
	}

	int getEndGroup(int group) {
		return endGroup[group];
	}

	int offsetBeginGroup(int group, int offset) {
		return beginGroup[group] += offset;
	}

	void offsetEndGroup(int group, int offset) {
		endGroup[group] += offset;
	}

	int getGroupLength(int group) {
		if ((beginGroup[group] != EMPTY) && (endGroup[group] != EMPTY)) {
			return endGroup[group] - beginGroup[group];
		}
		return 0;
	}

	boolean hasGroupBegin(int group) {
		return beginGroup[group] != EMPTY;
	}

	boolean groupIsEmpty(int group) {
		return beginGroup[group] == EMPTY;
	}

	void resetGroup(int group) {
		beginGroup[group] = EMPTY;
		endGroup[group] = EMPTY;
	}

	int setGroup(int group, int start, int stop) {
		beginGroup[group] = start;
		endGroup[group] = stop;
		return group;
	}

	// NB destination first
	void copyGroup(int dest, int source) {
		beginGroup[dest] = beginGroup[source];
		endGroup[dest] = endGroup[source];
	}

	void setEndGroupMaxOfRange(int destination, int sourceStart, int sourceStop) {
		int maxEnd = endGroup[sourceStart];
		for (int g = sourceStart + 1; g <= sourceStop; g++) {
			if (endGroup[g] > maxEnd) {
				maxEnd = endGroup[g];
			}
		}
		endGroup[destination] = maxEnd;
	}

	void setGroupBeginIfNotSet(int group, int start) {
		if (beginGroup[group] == EMPTY) {
			beginGroup[group] = start;
		}
	}

	int endGroupBeginGroup(int groupEnd, int groupBegin, int startEnd) {
		endGroup[groupEnd] = startEnd;
		beginGroup[groupBegin] = startEnd;
		return groupBegin;
	}

	public NumberType getNumberType() {
		return numberType;
	}

	public void setNumberType(NumberType numberType) {
		this.numberType = numberType;
	}

	public enum NumberType {
		NOT_A_NUMBER, INTEGER, // or LONG or BIGINT
		DOUBLE, // or FLOAT
		HEXADECIMAL, OCTAL, // INTEGER or LONG
		BINARY, // INTEGER, DOUBLE, FLOAT
	}

}
