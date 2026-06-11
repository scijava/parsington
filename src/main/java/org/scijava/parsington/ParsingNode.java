package org.scijava.parsington;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * A simple deterministic finite automaton for parsing characters
 *
 * @author Jared Davis
 */


public class ParsingNode<T> {
    // when payload is non-null, this node is equivalent to an accept state
    List<T> payload = null;
    // transitions to the next state, in value order
    List<ParsingNode<T>> next = new ArrayList<>();
    // starting ParsingNode value is not used
    char value = 0;

    /**
     * Get the next matching state.
     * @param v The character to find in the next list.
     * @return next ParsingNode that matches v or null when v is not found.
     */
    ParsingNode<T> hasValueNext(char v) {
        int ndx = getIndex(v);
        if (ndx >= 0) {
            return next.get(ndx);
        } else {
            return null;
        }
    }

    /**
     * Get the current value
     *
     * @return value of this ParsingNode
     */
    char getValue() {
        return value;
    }


    // Use an int comparator to keep the next list in char order.
    Comparator<ParsingNode<T>> vComparator = Comparator.comparingInt(ParsingNode::getValue);

    /**
     * Get index value of next list for char v.
     * Performs a binarySearch of list next looking for v.
     *
     * @param v The character to index in the next list
     * @return The index of v in the next list, if it is contained in the next list;
     *         otherwise, (-(insertion point) - 1).
     */
    int getIndex(char v) {
        ParsingNode<T> pn = new ParsingNode<T>();
        pn.value = v;
        return Collections.binarySearch(next, pn, vComparator);
    }

    /**
     * Add a new next ParsingNode.
     *
     * @param v The character to add.
     * @param action when non-null this defines an accept state.
     * @return The ParsingNode that was added.
     */

    ParsingNode<T> addNextValue(char v, T action) {
        int ndx = getIndex(v);
        ParsingNode<T> n;
        if (ndx >= 0) {
            n = next.get(ndx);
        } else {
            n = new ParsingNode<T>();
            n.value = v;
            next.add(-ndx - 1, n);
        }
        if (action != null) {
            if (n.payload == null) {
                n.payload = new ArrayList<>(6);
            }
            n.payload.add(action);
        }
        return n;
    }
}
