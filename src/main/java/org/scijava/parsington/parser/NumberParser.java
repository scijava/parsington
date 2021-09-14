package org.scijava.parsington.parser;

import org.scijava.parsington.Literals;
import org.scijava.parsington.ParserChain;
import org.scijava.parsington.Position;

public class NumberParser extends AbstractParser {

    /**
     * Parses a string literal which is enclosed in single or double quotes.
     * <p>
     * For literals in double quotes, this parsing mechanism is intended to be as
     * close as possible to the numeric literals supported by the Java programming
     * language itself. Literals in single quotes are completely verbatim, with no
     * escaping performed.
     * </p>
     *
     * @param s   The string from which the string literal should be parsed.
     * @param pos The offset from which the literal should be parsed. If parsing
     *            is successful, the position will be advanced to the next index
     *            after the parsed literal.
     * @return The parsed string value, unescaped according to Java conventions.
     * Returns null if the string does not begin with a single or double
     * quote.
     */
    @Override
    public Object doParse(CharSequence s, Position pos, ParserChain parserChain) {
        final Number hex = Literals.parseHex(s, pos);
        if (hex != null) return hex;

        final Number binary = Literals.parseBinary(s, pos);
        if (binary != null) return binary;

        final Number octal = Literals.parseOctal(s, pos);
        if (octal != null) return octal;

        final Number decimal = Literals.parseDecimal(s, pos);
        if (decimal != null) return decimal;

        return parserChain.doParser(s,pos);
    }


}
