package org.scijava.parsington.parser;

import org.scijava.parsington.Literals;
import org.scijava.parsington.ParserChain;
import org.scijava.parsington.Position;

public class StringParser extends AbstractParser {

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

        final char quote = pos.ch(s);
        if (quote != '"' && quote != '\'') return parserChain.doParser(s, pos);
        int index = pos.get() + 1;

        boolean escaped = false;
        final StringBuilder sb = new StringBuilder();
        while (true) {
            if (index >= s.length()) pos.die("Unclosed string literal");
            final char c = s.charAt(index);

            if (escaped) {
                escaped = false;
                if (Literals.isOctal(c)) { // octal sequence
                    String octal = "" + c;
                    final char c1 = pos.ch(s, index + 1);
                    if (Literals.isOctal(c1)) {
                        octal += c1;
                        if (c >= '0' && c <= '3') {
                            final char c2 = pos.ch(s, index + 2);
                            if (Literals.isOctal(c2)) octal += c2;
                        }
                    }
                    sb.append((char) Integer.parseInt(octal, 8));
                    index += octal.length();
                    continue;
                }
                switch (c) {
                    case 'b': // backspace
                        sb.append('\b');
                        break;
                    case 't': // tab
                        sb.append('\t');
                        break;
                    case 'n': // linefeed
                        sb.append('\n');
                        break;
                    case 'f': // form feed
                        sb.append('\f');
                        break;
                    case 'r': // carriage return
                        sb.append('\r');
                        break;
                    case '"': // double quote
                        sb.append('"');
                        break;
                    case '\\': // backslash
                        sb.append('\\');
                        break;
                    case 'u': // unicode sequence
                        final char u1 = Literals.hex(s, pos, index + 1);
                        final char u2 = Literals.hex(s, pos, index + 2);
                        final char u3 = Literals.hex(s, pos, index + 3);
                        final char u4 = Literals.hex(s, pos, index + 4);
                        sb.append((char) Integer.parseInt("" + u1 + u2 + u3 + u4, 16));
                        index += 4;
                        break;
                    default: // invalid escape
                        pos.die("Invalid escape sequence");
                }
            } else if (c == '\\' && quote == '"') escaped = true;
            else if (c == quote) break;
            else sb.append(c);
            index++;
        }
        pos.set(index + 1);
        return sb.toString();
    }
}
