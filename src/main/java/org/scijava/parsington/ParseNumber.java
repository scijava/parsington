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

import java.math.BigDecimal;
import java.math.BigInteger;

import static org.scijava.parsington.ParseNumberResults.*;
import static org.scijava.parsington.ParseNumberResults.NumberType.*;


/**
 * Utility methods for parsing numeric literals from strings. These methods largely
 * conform to the Java specification's ideas of what constitutes a numeric literal.
 * Based on a regular expression implementation.
 * Initial source from Google Gemini on July 23, 2026 query
 *   "java fast method to identify integers, doubles, hex and octal numbers in a string"
 * and existing code in Literals
 *
 * @author Curtis Rueden, Jared Davis
 */

public class ParseNumber {

	/**
	 * Internal parsing state: after the digits of {@link ParseNumberResults#DEC_INTEGER_PART}
	 * have ended, deciding whether a decimal point, exponent or suffix follows.
	 * NB: Not a stored result group&mdash;purely a switch value local to
	 * {@link #extractDecimalOrOctalNumber}.
	 */
	private static final int DEC_STATE_AFTER_INTEGER_PART = 8;

	/**
	 * Parses a numeric literal of any known type.
	 * <p>
	 * This parsing mechanism is intended to be as close as possible to the
	 * numeric literals supported by the Java programming language itself.
	 * </p>
	 *
	 * @param s The string from which the numeric literal should be parsed.
	 * @return The parsed numeric value, of a type consistent with Java's support
	 *         for numeric primitives&mdash;or for values outside the normal range
	 *         of Java primitives, {@link BigInteger} or {@link BigDecimal} as
	 *         appropriate. Returns null if a numeric literal is not detected.
	 */
	public static Number parseAllNumbers(String s) {
		return parseAllNumbers(s, new Position());
	}

	/**
	 * Parses a numeric literal of any known type.
	 * <p>
	 * This parsing mechanism is intended to be as close as possible to the
	 * numeric literals supported by the Java programming language itself.
	 * </p>
	 *
	 * @param s The string from which the numeric literal should be parsed.
	 * @param pos The offset from which the literal should be parsed. If parsing
	 *          is successful, the position will be advanced to the next index
	 *          after the parsed literal.
	 * @return The parsed numeric value, of a type consistent with Java's support
	 *         for numeric primitives&mdash;or for values outside the normal range
	 *         of Java primitives, {@link BigInteger} or {@link BigDecimal} as
	 *         appropriate. Returns null if a numeric literal is not detected.
	 */

	public static Number parseAllNumbers(String s, Position pos) {
		ParseNumberResults result = ParseNumber.identifyNumber(s, pos.get());
		if (result == null) return null;
		ParseNumber.processNumber(s, result);
		if (result.getNumber() != null) {
			pos.inc(result.getLength());
		}
		return result.getNumber();
	}


	// identify

	// return null if not a number
	private static ParseNumberResults identifyNumber(String s, int startingPosition) {
		int len = s.length();
		if (startingPosition >= len) return null;
		final char first = s.charAt(startingPosition);
		boolean hasSign=false;
		// quick fail if first char not in not 0-9 or a +- or a .
		if (first < '0' || first > '9') {
			if (first == '-' || first == '+') {
				hasSign = true;
				if (len == 1) {
					// fail on just a sign
					return null;
				}
			} else if (first != '.' ){
				return null; // first is not [-+0-9\.]
			}
		}

		ParseNumberResults results = new ParseNumberResults();
		results.setBegin(startingPosition);
		int start = startingPosition;
		if (hasSign) {
			results.setSignIndex(startingPosition);
			start++;
		}


		if (start + 2 < len && s.charAt(start) == '0') {
			final char afterZero = s.charAt(start + 1);
			if (afterZero == 'x' || afterZero == 'X') {
				extractHexNumber(s, start + 2, len, results);
				return results;
			}
			if (afterZero == 'b' || afterZero == 'B') {
				extractBinaryNumber(s, start + 2, len, results);
				return results;
			}
		}

		extractDecimalOrOctalNumber(s, start, len, results);
		return results;
	}

	// extract

/**
 * Attempts to parse a decimal or octal literal (integer or otherwise; e.g., {@code 1234567890},
 * {@code 1234.0987} or {@code 1.2e34} or {@code 01234}).
 *
 *
 * @param s The string from which the numeric literal should be parsed.
 * @param start The index of the string to start the parsing after the optional sign.
 * @param end The index of the last char to parse.
 * @param results contains the parsing details calculated in this method.
 *
 *
 * <p>Based on regular expression (but not exact) (([-+]?[0-9]+(\.[0-9]*)?([Ee][-+]?[0-9]+)?)([DdFfLl])?).*
 * group 1 ({@link ParseNumberResults#GROUP_ALL}) = entire matching string. NB: re has .* postfix but this method does not include .* in this group.
 * group 2 ({@link ParseNumberResults#DEC_VALUE}) is from start of string including optional groups 3 and 4
 * group 3 ({@link ParseNumberResults#DEC_FRACTION}) is \. then [0-9]*
 * group 4 ({@link ParseNumberResults#DEC_EXPONENT}) is [Ee][-+]?[0-9]+
 * group 5 ({@link ParseNumberResults#DEC_SUFFIX}) is [DdFfLl]?
 * group 6 ({@link ParseNumberResults#DEC_SIGN}) is not in re. [-+]? at start of string
 * group 7 ({@link ParseNumberResults#DEC_INTEGER_PART}) is not in re. Digits after sign, before group 3. re has [0-9]+ but changed to [0-9]* to match java numeric parsing
 * group 8 ({@link #DEC_STATE_AFTER_INTEGER_PART}) is not in re. Leads to group 3 or 4 or 5 or end
 *
 * <p>Octal encoding detection
 * If we have (no group 3) and (no group 4) then do a scan for an octal value in group 2
 *  Octal IFF
 *    group 7 starts with 0; does not contain 8 or 9; is longer than 1
 *    group 5 == empty or Ll
 */

	private static void extractDecimalOrOctalNumber(String s, int start, int end, ParseNumberResults results) {
		char c;

		results.setSignGroup(DEC_SIGN);

		boolean havedot = false;
		boolean haveSign = false;
		boolean haveFour = false;

		int group = results.setBeginGroup(DEC_INTEGER_PART, start);

		bigWhile:
		while (start <= end) {
			if (start == end) {
				results.setEndGroup(group, start);
				break;
			}
			c = s.charAt(start);
			switch (group) {
				case DEC_INTEGER_PART: // [0-9]*  digits after sign and before decimal point
					if (isDecimalDigit(c)) {
						start++;
					} else {
						results.setEndGroup(group, start);
						group = DEC_STATE_AFTER_INTEGER_PART;
					}
					break;

				case DEC_STATE_AFTER_INTEGER_PART: // transition to group 3, 4, 5 or end
					if (c == '.') {
						if (havedot) { // only one . allowed
							results.setNumberType(NOT_A_NUMBER);
							break bigWhile;
						}
						havedot = true;
						group = results.setBeginGroup(DEC_FRACTION, start++);
						break;
					}
					if (isE(c)) {
						if (haveFour) { // one group 4 allowed
							results.setNumberType(NOT_A_NUMBER);
							break bigWhile;
						}
						haveFour = true;
						group = results.setBeginGroup(DEC_EXPONENT, start++);
						break;
					}
					if (isADoubleOrFloatSuffix(c) || isL(c)) {
						results.setGroup(DEC_SUFFIX, start, ++start);
						results.setNumberType(isL(c) ? INTEGER : DOUBLE);
						// break bigWhile fall through below
					}
					break bigWhile;
				case DEC_FRACTION: // [0-9] 0..n times
					if (isDecimalDigit(c)) {
						start++;
					} else {
						results.setEndGroup(group, start);
						group = DEC_STATE_AFTER_INTEGER_PART;
					}
					break;
				case DEC_EXPONENT: //  just [-+]?[0-9]+ of group 4. [Ee] done in the transition state
					if (isASign(c)) {
						if (haveSign) { // only one sign allowed
							results.setNumberType(NOT_A_NUMBER);
							break bigWhile;
						}
						haveSign = true;
						start++;
					} else if (isDecimalDigit(c)) {
						start++;
					} else {
						results.setEndGroup(group, start);
						group = DEC_STATE_AFTER_INTEGER_PART; // back to transition state for group 5
					}
					break;
			} // switch group
		} // bigWhile
		results.setEnd(start);

		int integerPartLength = results.getGroupLength(DEC_INTEGER_PART);
		int fractionLength = results.getGroupLength(DEC_FRACTION);

		if ((integerPartLength == 0) && (fractionLength < 2)) {
			results.setNumberType(NOT_A_NUMBER); // need at least 1 digit in number after decimal
		} else {
			if (results.hasGroupBegin(DEC_EXPONENT)) { // we got an E
				int minLength = (haveSign) ? 3 : 2;
				if (results.getGroupLength(DEC_EXPONENT) < minLength)
					results.setNumberType(NOT_A_NUMBER); // need at least 1 digit in exponent
			}

			if (results.getNumberType() == null) {
				if ((results.hasGroupBegin(DEC_EXPONENT)) || (fractionLength > 0)) {
					results.setNumberType(DOUBLE);
				} else {
					results.setNumberType(INTEGER);
				}
			}

			if (results.getNumberType() != NOT_A_NUMBER) {
				// DEC_VALUE length = GROUP_ALL length - DEC_SUFFIX length
				results.copyGroup(DEC_VALUE, GROUP_ALL);
				if (results.hasGroupBegin(DEC_SUFFIX)) {
					results.offsetEndGroup(DEC_VALUE, -1);
				}
			}

			// octal encoding ?
			if (results.getNumberType() == INTEGER) {
				if (s.charAt(results.getBeginGroup(DEC_INTEGER_PART)) == '0') {
					boolean octal = (integerPartLength > 1);
					if (octal) {
						for (int i = results.getBeginGroup(DEC_INTEGER_PART) + 1; i < results.getEndGroup(DEC_INTEGER_PART); i++) {
							if (!isOctalDigit(s.charAt(i))) {
								octal = false;
								break;
							}
						}
					}
					if (octal) {
						if (results.hasGroupBegin(DEC_SUFFIX)) {
							if (!isL(s.charAt(results.getBeginGroup(DEC_SUFFIX)))) {
								octal = false;
							}
						}
					}
					if (octal) {
						results.setNumberType(OCTAL);
					}
				}
			}
		}
	}



	/**
	 * Parses a binary literal (e.g., {@code 0b010101000011}).
	 *
	 * @param s The string from which the numeric literal should be parsed.
	 * @param start The index of the string to start the parsing after the sign and 0[Bb] prefix.
	 * @param end The index of the last char to parse.
	 * @param results contains the parsing details calculated in this method.
	 *
	 * <p>based on Regular Expression (([-+]?)0[Bb]([01]+)([Ll]?)).*
	 * group 1 = entire matching string. NB: re has .* postfix but this method does not include .* in this group.
	 * group 2 ({@link ParseNumberResults#BIN_SIGN}) [-+]?  This is processed by the caller and placed into ParseNumberResults ndxSign.
	 * group none 0[Bb] must exist. This is processed by the caller.
	 * group 3 ({@link ParseNumberResults#BIN_DIGITS}) [01]+
	 * group 4 ({@link ParseNumberResults#BIN_SUFFIX}) [lL]?
	 */

	private static void extractBinaryNumber(String s, int start, int end, ParseNumberResults results) {
		char c;
		results.setNumberType(BINARY);
		results.setSignGroup(BIN_SIGN);
		int group = results.setBeginGroup(BIN_DIGITS, start);

		bigWhile:
		while (start <= end) {
			if (start == end) {
				results.setEndGroup(group, start);
				break;
			}
			c = s.charAt(start);
			switch (group) {
				case BIN_DIGITS: // [01]+
					if (isBinaryDigit(c)) {
						start++;
					} else {
						results.setEndGroup(group, start);
						group = BIN_SUFFIX;
					}
					break;
				case BIN_SUFFIX: // [Ll]?
					if (isL(c)) {
						results.setGroup(group, start, ++start);
					}
					// ok - we are done
					break bigWhile;
			} // switch group
		} // while bigWhile
		results.setEnd(start);
		// need at least 1 digit in group 3
		if (results.getGroupLength(BIN_DIGITS) == 0)
			results.setNumberType(NOT_A_NUMBER);
	}

	/**
	 * Parses a hexadecimal literal. Both hexadecimal integer (e.g.,
	 * {@code 0xfedcba9876543210}) and hexadecimal floating point (e.g.,
	 * {@code 0xfedcba.98765432p10f}) are supported.
	 *
	 * @param s The string from which the numeric literal should be parsed.
	 * @param start The index of the string to start the parsing after the sign and 0[Xx] prefix.
	 * @param end The index of the last char to parse.
	 * @param results contains the parsing details calculated in this method.
	 *
	 *
	 * <p>based on Regular Expression (([-+]?)0[Xx]([0-9a-fA-F]+)([Ll]|(\\.[0-9a-fA-F]*)?[Pp]([-+]?)([0-9]+)([DdFf]|)|)).*
	 * group 1 = entire matching string. NB: re has .* postfix but this method does not include .* in this group.
	 * group 2 ({@link ParseNumberResults#HEX_SIGN}) [-+]? This is processed by the caller and placed into ParseNumberResults ndxSign.
	 * group none 0[Xx]  This is processed by the caller.
	 * group 3 ({@link ParseNumberResults#HEX_DIGITS}) [0-9a-fA-F]+ repeat.
	 * group 4 ({@link ParseNumberResults#HEX_SUFFIX_OR_EXPONENT}) includes groups 5, 6, 7 and 8 or [lL]?
	 * group 5 ({@link ParseNumberResults#HEX_FRACTION}) (\\.[0-9a-fA-F]*)? Entire group is optional
	 * group 6 ({@link ParseNumberResults#HEX_EXPONENT_SIGN}) requires a [Pp] prefix which is not part of the group. [-+]?
	 * group 7 ({@link ParseNumberResults#HEX_EXPONENT_DIGITS}) [0-9]+
	 * group 8 ({@link ParseNumberResults#HEX_FLOAT_SUFFIX}) [DdFf]?
	 * group 9 ({@link ParseNumberResults#HEX_P_MARKER}), not in re, is [Pp] which is required before groups 6, 7.
	 */

	private static void extractHexNumber(String s, int start, int end, ParseNumberResults results) {
		char c;
		results.setNumberType(HEXADECIMAL);
		results.setSignGroup(HEX_SIGN);
		int group = results.setBeginGroup(HEX_DIGITS, start);
		bigWhile:
		while (start <= end) {
			if (start == end) {
				results.setEndGroup(group, start);
				break;
			}
			c = s.charAt(start); // start may not change when group changes
			switch (group) {
				case HEX_DIGITS: // [0-9a-fA-F]+
					if (isHexDigit(c)) {
						start++;
					} else {
						group = results.endGroupBeginGroup(group, HEX_SUFFIX_OR_EXPONENT, start);
					}
					break;
				case HEX_SUFFIX_OR_EXPONENT: // [Ll]? group end is adjusted after switch to include groups 5-8.
					if (isL(c)) {
						results.setEndGroup(group, ++start);
						break bigWhile; // end of re. No need to continue parsing chars
					}
					if (c == '.') {
						group = results.setBeginGroup(HEX_FRACTION, start++);
						break;
					}
					if (isP(c)) {
						start++;
						group = HEX_P_MARKER;
						break;
					}
					// we are done, there is no group 4
					results.resetGroup(group);
					break bigWhile;
				case HEX_FRACTION: // \.[0-9a-fA-F]* 0..n
					if (isHexDigit(c)) {
						start++;
					} else {
						results.setEndGroup(group, start);
						if (isP(c)) {
							group = results.setGroup(HEX_P_MARKER, start, ++start);
						}
					}
					break;
				case HEX_P_MARKER: // start -1 == p or P
					if (isASign(c)) {
						results.setGroup(HEX_EXPONENT_SIGN, start, ++start);
					}
					group = HEX_EXPONENT_DIGITS;
					break;
				case HEX_EXPONENT_DIGITS: // [0-9]+ enforce at least 1 char in this group
					if (isDecimalDigit(c)) {
						results.setGroupBeginIfNotSet(group, start);
						start++;
					} else {
						if (results.groupIsEmpty(group)) {
							// need at least 1 digit to be valid
							results.setNumberType(NOT_A_NUMBER);
							break bigWhile;
						} else {
							results.setEndGroup(group, start);
							group = HEX_FLOAT_SUFFIX;
						}
					}
					break;
				case HEX_FLOAT_SUFFIX: // [DdFf]?
					if (isADoubleOrFloatSuffix(c)) {
						results.setGroup(HEX_FLOAT_SUFFIX, start, ++start);
					}
					break bigWhile;
			} // switch  group
		} // while bigWhile

		results.setEnd(start);

		// group 4 end = max of groups 4,5,6,7,8
		results.setEndGroupMaxOfRange(HEX_SUFFIX_OR_EXPONENT, HEX_SUFFIX_OR_EXPONENT, HEX_FLOAT_SUFFIX);

		// P (in group 9) requires at least one entry in group 7
		if (results.hasGroupBegin(HEX_P_MARKER) && results.groupIsEmpty(HEX_EXPONENT_DIGITS))
			results.setNumberType(NOT_A_NUMBER);
		// group 3 must have at least 1 digit
		if (results.getGroupLength(HEX_DIGITS) == 0)
			results.setNumberType(NOT_A_NUMBER);
	}

	// process extracted results

	private static void processNumber(String in, ParseNumberResults results) {
		switch (results.getNumberType()) {
			case INTEGER:
			case DOUBLE:
				results.setNumber(processDecimal(in, results));
				break;
			case HEXADECIMAL:
				results.setNumber(processHex(in, results));
				break;
			case BINARY:
				results.setNumber(processBinary(in, results));
				break;
			case OCTAL:
				results.setNumber(processOctal(in, results));
				break;
		}
	}

	private static Number processHex(final String s, ParseNumberResults results) {
		final String sign = results.getGroup(s, HEX_SIGN); // + or - or ""
		final String integer = results.getGroup(s, HEX_DIGITS); // hex digits before decimal point
		final String suffix = results.getGroup(s, HEX_SUFFIX_OR_EXPONENT);  // L or floating point expression
		final boolean forceLong = "L".equalsIgnoreCase(suffix);

		final Number result;
		if (forceLong || suffix.isEmpty()) {
			// Integer notation.
			final String number = sign + integer;
			result = parseIntegerToNumber(number, forceLong, 16);
		} else {
			// Floating point notation.
			final String token = results.getGroup(s, GROUP_ALL);       // entire matched literal
			final String expSuffix = results.getGroup(s, HEX_FLOAT_SUFFIX);   // f or d or nothing
			final boolean forceFloat = "F".equalsIgnoreCase(expSuffix);
			final boolean forceDouble = "D".equalsIgnoreCase(expSuffix);
			// NB: The BigDecimal code does not understand floating point
			// hex strings, so the following invocation will never produce
			// a larger-than-double-precision floating point BigDecimal.
			// It's a convenient way to support float and double precision,
			// but for BigDecimal support, we would need to process the
			// matched groups above, converting hex to base 10 first.
			result = parseDecimalToNumber(token, forceFloat, forceDouble);
		}
		return result;//verifyResult(result, m, pos);
	}

	private static Number processBinary(String s, ParseNumberResults results) {
		final String sign = results.getGroup(s, BIN_SIGN);
		final String number = sign + results.getGroup(s, BIN_DIGITS);

		final boolean forceLong = !results.getGroup(s, BIN_SUFFIX).isEmpty();
		return parseIntegerToNumber(number, forceLong, 2);
	}

	// NB: octal literals share the decimal group layout (DEC_VALUE, DEC_SUFFIX)
	private static Number processOctal(String s, ParseNumberResults results) {
		final String number = results.getGroup(s, DEC_VALUE);
		final String expSuffix = results.getGroup(s, DEC_SUFFIX);   // Ll nothing
		final boolean forceLong = "L".equalsIgnoreCase(expSuffix);
		return parseIntegerToNumber(number, forceLong, 8);
	}

	private static Number processDecimal(String s, ParseNumberResults results) {
		final String numberStr = results.getGroup(s, DEC_VALUE);
		final String force = results.getGroup(s, DEC_SUFFIX);
		final boolean forceLong = "l".equalsIgnoreCase(force);
		final boolean forceFloat = "f".equalsIgnoreCase(force);
		final boolean forceDouble = "d".equalsIgnoreCase(force);
		Number result = null;
		if (results.getNumberType() == INTEGER) {
			// No decimal point and no exponent part. So this *might* be an integer!
			result = parseIntegerToNumber(numberStr, forceLong, 10);
		}
		if (result == null && !forceLong) {
			result = parseDecimalToNumber(numberStr, forceFloat, forceDouble);
		}
		return result;
	}


	// parse resulting Strings and flags to Number

	/**
	 * Parses an Integer literal (e.g., {@code 1234}).
	 * Attempts to minimize swallowed Exceptions by parsing as a long first.
	 *
	 * @param number The string from which the numeric literal should be parsed.
	 * @param forceLong boolean flag to require a long Number be returned
	 * @param base radix for conversion
	 * @return The parsed numeric value&mdash;an {@link Integer} if sufficiently
	 *         small, or a {@link Long} if needed or if the {@code L} suffix is
	 *         given; or a {@link BigInteger} if the value is too large even for
	 *         {@code long} or null.
	 */

	private static Number parseIntegerToNumber(final String number,
											   final boolean forceLong, final int base) {

		// parse to long, convert to int if in range and not forced to a long
		long result ;
		try {
			result = Long.parseLong(number, base);
			if (forceLong || result < Integer.MIN_VALUE || result > Integer.MAX_VALUE) {
				return result;
			}
			return (int) result; // NB: range check above to avoid silent loss
		}  catch (NumberFormatException e) {
			// will not fit in long or bad text
			if (!forceLong) {
				try {
					return new BigInteger(number, base);
				} catch (final NumberFormatException exc) {
					// do nothing
				}
			}
		}
		return null;
	}


	/**
	 * Parses a Decimal literal (e.g., {@code 1234.1}).
	 *
	 * @param number The string from which the numeric literal should be parsed.
	 * @param forceFloat boolean flag to require a float Number be returned
	 * @param forceDouble boolean flag to require a double Number be returned
	 * @return The parsed numeric value&mdash;an {@link Double}, {@link Float} or {@link BigDecimal} or null
	 */
	private static Number parseDecimalToNumber(final String number,
											   final boolean forceFloat, final boolean forceDouble) {
		if (forceFloat) {
			// Try to fit it into a float.
			try {
				return Float.parseFloat(number);
			} catch (final NumberFormatException exc) {
				// NB: No action needed.
			}
		} else {
			// Try to fit it into a double.
			try {
				return Double.parseDouble(number);
			} catch (final NumberFormatException exc) {
				// NB: No action needed.
			}
		}

		if (!forceDouble && !forceFloat) {
			// Try to treat it as a BigDecimal.
			try {
				return new BigDecimal(number);
			} catch (final NumberFormatException exc) {
				// NB: No action needed.
			}
		}

		return null;
	}

	// Helpers

	private static boolean isHexDigit(char c) {
		return ((c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F'));
	}

	private static boolean isOctalDigit(char c) {
		return (c >= '0' && c <= '7');
	}

	private static boolean isL(char c) {
		return (c == 'L' || c == 'l');
	}

	private static boolean isP(char c) {
		return (c == 'P' || c == 'p');
	}

	private static boolean isE(char c) {
		return (c == 'E' || c == 'e');
	}

	private static boolean isDecimalDigit(char c) {
		return (c >= '0' && c <= '9');
	}

	private static boolean isBinaryDigit(char c) {
		return (c == '0' || c == '1');
	}

	private static boolean isASign(char c) {
		return (c == '+' || c == '-');
	}

	private static boolean isADoubleOrFloatSuffix(char c) {
		return (c == 'D' || c == 'd' || c == 'F' || c == 'f');
	}



}
