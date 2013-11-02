package com.github.mastersobg.odkl.model;

public class Pagination {

    public enum Direction {
        FORWARD,
        BACKWARD
    }

    private final String anchor;
    private final Direction direction;
    private final int count;

    public Pagination(String anchor) {
        this(anchor, Direction.FORWARD, 10);
    }

    public Pagination(String anchor, Direction direction, int count) {
        if (anchor == null) {
            throw new IllegalArgumentException("anchor is null");
        }
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
}
