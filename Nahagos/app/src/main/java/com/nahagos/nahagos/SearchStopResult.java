package com.nahagos.nahagos;

import android.util.Pair;

public class SearchStopResult extends Pair<Integer, String> {
    /**
     * Constructor for a Pair.
     *
     * @param first  the first object in the Pair
     * @param second the second object in the pair
     */
    public SearchStopResult(Integer first, String second) {
        super(first, second);
    }

    @Override
    public String toString() {
        return second;
    }
}
