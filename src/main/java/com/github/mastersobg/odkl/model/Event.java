package com.github.mastersobg.odkl.model;

import com.github.mastersobg.odkl.util.JsonUtil;
import org.json.simple.JSONObject;

/**
 * @author Ivan Gorbachev <gorbachev.ivan@gmail.com>
 */
public class Event {

    private final Long userId;
    private final String type;
    private final Integer count;
    private final Long lastId;
    private final Integer message;
    private final Integer icon;
    private final Integer likesCount;
    private final Integer repliesCount;

    public Event(JSONObject json) {
        userId = JsonUtil.getLong(json, "uid");
        type = JsonUtil.getString(json, "type");
        count = JsonUtil.getInt(json, "number");
        lastId = JsonUtil.getLong(json, "lastId");
        message = JsonUtil.getInt(json, "message");
        icon = JsonUtil.getInt(json, "icon");
        likesCount = JsonUtil.getInt(json, "likes_count");
        repliesCount = JsonUtil.getInt(json, "replies_count");
    }

    @Override
    public String toString() {
        return "[" +
                "userId=" + userId + "," +
                "type=" + type + "," +
                "count=" + count + "," +
                "lastId=" + lastId + "," +
                "message=" + message + "," +
                "icon=" + icon + "," +
                "likesCount=" + likesCount + "," +
                "repliesCount=" + repliesCount +
                "]";
    }

    public Long getUserId() {
        return userId;
    }

    public String getType() {
        return type;
    }

    public Integer getCount() {
        return count;
    }

    public Long getLastId() {
        return lastId;
    }

    public Integer getMessage() {
        return message;
    }

    public Integer getIcon() {
        return icon;
    }

    public Integer getLikesCount() {
        return likesCount;
    }

    public Integer getRepliesCount() {
        return repliesCount;
    }
}
