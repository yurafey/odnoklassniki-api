package com.github.mastersobg.odkl.model;

import com.github.mastersobg.odkl.util.JsonUtil;
import org.json.simple.JSONObject;

import java.util.Date;

/**
 * @author Ivan Gorbachev <gorbachev.ivan@gmail.com>
 */
public class User {

    private final String uid;
    private final Date birthday;
    private final Integer age;
    private final String firstname;
    private final String lastame;
    private final String name;
    private final Boolean hasEmail;
    private final String male;
    private final String profilePicture50x50;
    private final String profilePicture128x128;

    public User(JSONObject json) {
        uid = JsonUtil.getString(json, "uid");
        age = JsonUtil.getInt(json, "age");
        firstname = JsonUtil.getString(json, "first_name");
        lastame = JsonUtil.getString(json, "last_name");
        name = JsonUtil.getString(json, "name");
        hasEmail = JsonUtil.getBoolean(json, "has_email");
        male = JsonUtil.getString(json, "gender");
        profilePicture50x50 = JsonUtil.getString(json, "pic_1");
        profilePicture128x128 = JsonUtil.getString(json, "pic_2");
        birthday = JsonUtil.getDate(json, "birthday");
    }

    public String getUid() {
        return uid;
    }

    public Date getBirthday() {
        return birthday;
    }

    public int getAge() {
        return age;
    }

    public String getFirstname() {
        return firstname;
    }

    public String getLastame() {
        return lastame;
    }

    public String getName() {
        return name;
    }

    public boolean isHasEmail() {
        return hasEmail;
    }

    public String getMale() {
        return male;
    }

    public String getProfilePicture50x50() {
        return profilePicture50x50;
    }

    public String getProfilePicture128x128() {
        return profilePicture128x128;
    }

    public String toString() {
        return "[" +
                "uid=" + uid + "," +
                "birthday=" + (birthday != null ? JsonUtil.DATE_FORMAT.format(birthday) : "null") + "," +
                "age=" + asString(age) + "," +
                "firstname=" + firstname + "," +
                "lastname=" + lastame + "," +
                "name=" + name + "," +
                "hasEmail=" + asString(hasEmail) + "," +
                "male=" + male + "," +
                "profilePicture50x50=" + profilePicture50x50 + "," +
                "profilePicture128x128=" + profilePicture128x128 +
                "]";
    }

    private String asString(Object o) {
        return o == null ? "null" : o.toString();
    }
}
