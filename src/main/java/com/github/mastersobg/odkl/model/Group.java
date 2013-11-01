package com.github.mastersobg.odkl.model;

import com.github.mastersobg.odkl.util.JsonUtil;
import org.json.simple.JSONObject;

/**
 * @author Ivan Gorbachev <gorbachev.ivan@gmail.com>
 */
public class Group {

    public enum UserStatus {
        ACTIVE,
        BLOCKED,
        ADMIN,
        MODERATOR,
        PASSIVE,
        UNKNOWN
    }

    private final Long id;
    private final String name;
    private final String description;
    private final String shortName;
    private final String avatar;
    private final Boolean shopVisibleAdmin;
    private final Boolean shopVisiblePublic;

    public Group(JSONObject json) {
        id = JsonUtil.getLong(json, "uid");
        name = JsonUtil.getString(json, "name");
        description = JsonUtil.getString(json, "description");
        shortName = JsonUtil.getString(json, "shortname");
        avatar = JsonUtil.getString(json, "picAvatar");
        shopVisibleAdmin = JsonUtil.getBoolean(json, "shop_visible_admin");
        shopVisiblePublic = JsonUtil.getBoolean(json, "shop_visible_public");
    }

    public String toString() {
        return "[" +
                "id=" + id + "," +
                "name=" + name + "," +
                "description=" + description + "," +
                "shortName=" + shortName + "," +
                "avatar=" + avatar + "," +
                "shopVisibleAdmin=" + shopVisibleAdmin + "," +
                "shopVisiblePublic=" + shopVisiblePublic +
                "]";
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getShortName() {
        return shortName;
    }

    public String getAvatar() {
        return avatar;
    }

    public Boolean getShopVisibleAdmin() {
        return shopVisibleAdmin;
    }

    public Boolean getShopVisiblePublic() {
        return shopVisiblePublic;
    }
}
