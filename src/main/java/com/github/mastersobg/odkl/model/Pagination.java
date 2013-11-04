package com.github.mastersobg.odkl.model;

import java.util.HashMap;
import java.util.Map;

public class Pagination {

    private static final int DEFAULT_COUNT = 10;

    public enum Direction {
        FORWARD,
        BACKWARD
    }

    private final String anchor;
    private final Direction direction;
    private final int count;

    public Pagination() {
        this(null, Direction.FORWARD, DEFAULT_COUNT);
    }

    public Pagination(String anchor, int count) {
        this(anchor, Direction.FORWARD, count);
    }

    public Pagination(String anchor) {
        this(anchor, Direction.FORWARD, DEFAULT_COUNT);
    }

    public Pagination(int count) {
        this(null, Direction.FORWARD, DEFAULT_COUNT);
    }

    public Pagination(String anchor, Direction direction, int count) {
        if (direction == null) {
            throw new IllegalArgumentException("direction is null");
        }
        if (count <= 0) {
            throw new IllegalArgumentException("count is not positive [" + count + "]");
        }

        this.anchor = anchor;
        this.direction = direction;
        this.count = count;
    }

    public String getAnchor() {
        return anchor;
    }

    public Direction getDirection() {
        return direction;
    }

    public int getCount() {
        return count;
    }

    public Map<String, String> asParamsMap() {
        Map<String, String> params = new HashMap<String, String>();

        if (anchor != null) {
            params.put("anchor", anchor);
        }
        params.put("direction", direction.toString().toLowerCase());
        params.put("count", Integer.toString(count));

        return params;
    }
}
