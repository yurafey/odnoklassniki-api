package com.github.mastersobg.odkl.util;

import com.github.mastersobg.odkl.exception.OdklApiRuntimeException;
import com.github.mastersobg.odkl.model.PageableResponse;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Ivan Gorbachev <gorbachev.ivan@gmail.com>
 */
public class JsonUtil {

    public static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    private static final JSONParser parser = new JSONParser();

    public static JSONObject parseObject(String json) {
        try {
            return (JSONObject) parser.parse(json);
        } catch (ParseException e) {
            throw new OdklApiRuntimeException(e);
        }
    }

    public static JSONArray parseArray(String json) {
        try {
            return (JSONArray) parser.parse(json);
        } catch (ParseException e) {
            throw new OdklApiRuntimeException(e);
        }
    }

    public static String getString(JSONObject json, String key) {
        Object o = json.get(key);
        if (o == null) {
            return null;
        }
        return o.toString();
    }

    public static Integer getInt(JSONObject json, String key) {
        Object o = json.get(key);
        if (o == null) {
            return null;
        }
        return Integer.valueOf(o.toString());
    }

    public static Boolean getBoolean(JSONObject json, String key) {
        Object o = json.get(key);
        if (o == null) {
            return null;
        }
        return Boolean.valueOf(o.toString());
    }

    public static Double getDouble(JSONObject json, String key) {
        Object o = json.get(key);
        if (o == null) {
            return null;
        }
        return Double.valueOf(o.toString());
    }

    public static Long getLong(JSONObject json, String key) {
        Object o = json.get(key);
        if (o == null) {
            return null;
        }
        return Long.valueOf(o.toString());
    }

    public static JSONObject getObject(JSONObject json, String key) {
        Object o = json.get(key);
        if (o == null) {
            return null;
        }
        return (JSONObject) o;
    }

    public static JSONArray getArray(JSONObject json, String key) {
        Object o = json.get(key);
        if (o == null) {
            return null;
        }
        return (JSONArray) o;
    }

    public static <T> PageableResponse<T> getPageableResponse(JSONObject json, T data) {
        String anchor = getString(json, "anchor");
        if (anchor == null) {
            throw new OdklApiRuntimeException("anchor not found");
        }
        return new PageableResponse<T>(data, anchor);
    }

    public static Date getDate(JSONObject json, String key) {
        Object o = json.get(key);
        if (o == null) {
            return null;
        }
        try {
            return DATE_FORMAT.parse(o.toString());
        } catch (java.text.ParseException e) {
            throw new OdklApiRuntimeException(e);
        }
    }
}
