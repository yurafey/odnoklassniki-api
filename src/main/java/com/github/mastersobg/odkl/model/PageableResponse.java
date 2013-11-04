package com.github.mastersobg.odkl.model;

import java.util.Collection;

public class PageableResponse<T> {

    private final Collection<T> data;
    private final String anchor;
    private final boolean hasMore;

    public PageableResponse(Collection<T> data, String anchor, boolean hasMore) {
        this.data = data;
        this.anchor = anchor;
        this.hasMore = hasMore;
    }

    public Collection<T> getData() {
        return data;
    }

    public String getAnchor() {
        return anchor;
    }

    public boolean hasMore() {
        return hasMore;
    }
}
