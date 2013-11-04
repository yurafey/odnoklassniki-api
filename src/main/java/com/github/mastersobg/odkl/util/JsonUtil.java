package com.github.mastersobg.odkl.util;

import com.github.mastersobg.odkl.exception.OdklApiRuntimeException;
import com.github.mastersobg.odkl.model.PageableResponse;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

/**
 * @author Ivan Gorbachev <gorbachev.ivan@gmail.com>
 */
public class JsonUtil {

    public static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    public static JSONObject parseObject(String json) {
        try {
            return (JSONObject) new JSONParser().parse(json);
        } catch (ParseException e) {
            throw new OdklApiRuntimeException(e);
        }
    }

    public static JSONArray parseArray(String json) {
        try {
            Object object = new JSONParser().parse(json);
            if (object == null) {
                return new JSONArray();
            } else {
                return (JSONArray) object;
            }
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
            return new JSONArray();
        }
        return (JSONArray) o;
    }

    public static <T, C extends Collection<T>> PageableResponse<T> getPageableResponse(JSONObject json, C data) {
        String anchor = getString(json, "anchor");
        if (anchor == null) {
            throw new OdklApiRuntimeException("anchor not found");
        }
        return new PageableResponse<T> (data, anchor, data.size() > 0);
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
