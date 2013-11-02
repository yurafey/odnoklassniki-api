package com.github.mastersobg.odkl.model;

public class PageableResponse<T> {

    private final T data;
    private final String anchor;

    public PageableResponse(T data, String anchor) {
        this.data = data;
        this.anchor = anchor;
    }

    public T getData() {
        return data;
    }

    public String getAnchor() {
        return anchor;
    }
}
