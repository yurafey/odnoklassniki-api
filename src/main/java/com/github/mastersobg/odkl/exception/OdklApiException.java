package com.github.mastersobg.odkl.exception;

import com.github.mastersobg.odkl.util.JsonUtil;
import org.json.simple.JSONObject;

/**
 * @author Ivan Gorbachev <gorbachev.ivan@gmail.com>
 */
public class OdklApiException extends RuntimeException {

    private final Integer errorCode;
    private final String errorData;
    private final String errorMsg;

    public OdklApiException(JSONObject json, String msg) {
        super(msg);
        errorCode = JsonUtil.getInt(json, "error_code");
        errorData = JsonUtil.getString(json, "error_data");
        errorMsg = JsonUtil.getString(json, "error_msg");
    }

    public Integer getErrorCode() {
        return errorCode;
    }

    public String getErrorData() {
        return errorData;
    }

    public String getErrorMsg() {
        return errorMsg;
    }
}
