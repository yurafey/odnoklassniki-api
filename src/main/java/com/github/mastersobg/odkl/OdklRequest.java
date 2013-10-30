package com.github.mastersobg.odkl;

import com.github.mastersobg.odkl.exception.OdklApiRuntimeException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Ivan Gorbachev <gorbachev.ivan@gmail.com>
 */
class OdklRequest {

    public static final String API_URL = "http://api.odnoklassniki.ru/fb.do";
    public static final String OAUTH_URL = "http://api.odnoklassniki.ru/oauth/token.do";
    public static final String LOGIN_URL = "http://www.odnoklassniki.ru/oauth/authorize";

    private final List<Param> params = new ArrayList<Param>();
    private boolean apiCall;

    private String accessToken;
    private String clientSecretKey;

    private String url = API_URL;

    private boolean sendPost;

    OdklRequest addParam(String key, String value) {
        params.add(new Param(key, value));
        return this;
    }

    OdklRequest setGroupAndMethod(String group, String method) {
        apiCall = true;
        params.add(new Param("method", group + "." + method));
        return this;
    }

    OdklRequest setSendPost(boolean sendPost) {
        this.sendPost = sendPost;
        return this;
    }

    OdklRequest setUrl(String url) {
        this.url = url;
        return this;
    }

    OdklRequest setAccessToken(String accessToken) {
        this.accessToken = accessToken;
        return this;
    }

    OdklRequest setClientSecretKey(String clientSecretKey) {
        this.clientSecretKey = clientSecretKey;
        return this;
    }

    String sendRequest() {
        try {
            URLConnection connection = new URL(buildUrl()).openConnection();
            if (sendPost) {
                connection.addRequestProperty("Content-type", "application/x-www-form-urlencoded");
                connection.setDoOutput(true);
                OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
                writer.write(buildParamsString());
                writer.close();
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String response = readAll(reader);
            reader.close();
            return response;
        } catch (IOException e) {
            throw new OdklApiRuntimeException(e);
        }
    }

    protected String readAll(BufferedReader reader) throws IOException {
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        reader.close();
        return sb.toString();
    }

    String buildUrl() {
        if (sendPost) {
            return url;
        } else {
            String ret = url;
            return ret + "?" + buildParamsString();
        }
    }

    private String buildParamsString() {
        final String delimiter = "&";
        String params = joinParams(delimiter);
        String token = accessToken;
        if (apiCall) {
            params += delimiter + "access_token=" + token;
            params += delimiter + "sig=" + calculateSig(token);
        }
        return params;
    }

    private String calculateSig(String accessToken) {
        Collections.sort(params);
        return md5(joinParams("") + md5(accessToken + clientSecretKey));
    }

    private String joinParams(String delimiter) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < params.size(); ++i) {
            if (i > 0) {
                sb.append(delimiter);
            }
            sb.append(params.get(i).asString());
        }
        return sb.toString();
    }

    private String md5(String s) {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new OdklApiRuntimeException(e);
        }
        byte []data = md.digest(s.getBytes());
        return byteArrayToHexString(data);
    }

    private String byteArrayToHexString(byte []data) {
        StringBuilder sb = new StringBuilder();
        for (int value : data) {
            value &= 0xFF;
            sb.append(digitToChar(value / 0x10));
            sb.append(digitToChar(value % 0x10));
        }
        return sb.toString();
    }

    private char digitToChar(int digit) {
        if (digit < 10) {
            return (char) (digit + '0');
        } else {
            return (char) (digit - 10 + 'a');
        }
    }

    private class Param implements Comparable<Param> {

        private final String key;
        private final String value;

        private Param(String key, String value) {
            this.key = key;
            this.value = value;
        }

        private String asString() {
            return key + "=" + value;
        }

        @Override
        public int compareTo(Param o) {
            return key.compareTo(o.key);
        }
    }
}
