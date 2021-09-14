package org.scijava.parsington.parser;

import org.scijava.parsington.Literals;
import org.scijava.parsington.ParserChain;
import org.scijava.parsington.Position;

/**
 * Parses a boolean literal (i.e., true and false).
 *
 */
public class BooleanParser extends AbstractParser {

    /**
     * Parses a boolean literal (i.e., true and false).
     *
     * @param s   The string from which the boolean literal should be parsed.
     * @param pos The offset from which the literal should be parsed. If parsing
     *            is successful, the position will be advanced to the next index
     *            after the parsed literal.
     * @return The parsed boolean value&mdash;either {@link Boolean#TRUE} or
     * {@link Boolean#FALSE}&mdash; or null if the string does not begin
     * with a boolean literal.
     */
    public Object doParse(CharSequence s, Position pos, ParserChain parserChain) {

        if (Literals.isWord(s, pos, "true")) {
            pos.inc(4);
            return Boolean.TRUE;
        }
        if (Literals.isWord(s, pos, "false")) {
            pos.inc(5);
            return Boolean.FALSE;
        }
        return parserChain.doParser(s, pos);
    }

}
