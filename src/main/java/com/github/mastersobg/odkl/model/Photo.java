package com.github.mastersobg.odkl.model;

import com.github.mastersobg.odkl.util.JsonUtil;
import com.github.mastersobg.odkl.exception.OdklApiRuntimeException;
import org.json.simple.JSONObject;

/**
 * @author Ivan Gorbachev <gorbachev.ivan@gmail.com>
 */
public class Photo {

    private final Long id;
    private final Long albumId;
    private final String pic50x50;
    private final String pic128x128;
    private final String pic640x480;
    private final Integer commentsCount;
    private final Integer likesCount;
    private final Boolean likeIt;
    private final Long userId;
    private final Integer marksCount;
    private final Integer bonusMarksCount;
    private final String avgMark;
    private final String viewerMark;

    public Photo(JSONObject json) {
        json = JsonUtil.getObject(json, "photo");
        if (json == null) {
            throw new OdklApiRuntimeException("photo is null");
        }
        id = JsonUtil.getLong(json, "id");
        albumId = JsonUtil.getLong(json, "album_id");
        pic50x50 = JsonUtil.getString(json, "pic50x50");
        pic128x128 = JsonUtil.getString(json, "pic128x128");
        pic640x480 = JsonUtil.getString(json, "pic640x480");
        commentsCount = JsonUtil.getInt(json, "comments_count");
        likesCount = JsonUtil.getInt(json, "like_count");
        likeIt = JsonUtil.getBoolean(json, "liked_it");
        userId = JsonUtil.getLong(json, "user_id");
        marksCount = JsonUtil.getInt(json, "mark_count");
        bonusMarksCount = JsonUtil.getInt(json, "mark_bonus_count");
        avgMark = JsonUtil.getString(json, "mark_avg");
        viewerMark = JsonUtil.getString(json, "viewer_mark");
    }

    public Long getId() {
        return id;
    }

    public Long getAlbumId() {
        return albumId;
    }

    public String getPic50x50() {
        return pic50x50;
    }

    public String getPic128x128() {
        return pic128x128;
    }

    public String getPic640x480() {
        return pic640x480;
    }

    public Integer getCommentsCount() {
        return commentsCount;
    }

    public Integer getLikesCount() {
        return likesCount;
    }

    public Boolean getLikeIt() {
        return likeIt;
    }

    public Long getUserId() {
        return userId;
    }

    public Integer getMarksCount() {
        return marksCount;
    }

    public Integer getBonusMarksCount() {
        return bonusMarksCount;
    }

    public String getAvgMark() {
        return avgMark;
    }

    public String getViewerMark() {
        return viewerMark;
    }

    @Override
    public String toString() {
        return "[" +
                "id=" + id + "," +
                "albumId=" + albumId + "," +
                "pic50x50=" + pic50x50 + "," +
                "pic128x128=" + pic128x128 + "," +
                "pic640x480=" + pic640x480 + "," +
                "commentsCount=" + commentsCount + "," +
                "likesCount=" + likesCount + "," +
                "likeIt=" + likeIt + "," +
                "userId=" + userId + "," +
                "marksCount=" + marksCount + "," +
                "bonusMarksCount=" + bonusMarksCount + "," +
                "avgMark=" + avgMark + "," +
                "viewerMark=" + viewerMark + "," +
                "]";
    }

}
