package com.github.mastersobg.odkl;

import com.github.mastersobg.odkl.exception.OdklApiException;
import com.github.mastersobg.odkl.exception.OdklApiRuntimeException;
import com.github.mastersobg.odkl.util.JsonUtil;
import org.json.simple.JSONObject;

/**
 * @author Ivan Gorbachev <gorbachev.ivan@gmail.com>
 */
public class OdklApi {

    private static final String API_ERROR_STRING = "error_code";
    private static final String TOKEN_EXPIRED_ERROR = "\"error_code\":102";
    private static final long EXPIRE_TIME = 1800000;
    private static final int MAX_REFRESH_COUNT = 2;

    private final String clientId;
    private final String clientPublicKey;
    private final String clientSecretKey;
    private final String refreshToken;

    private volatile boolean autoRefreshToken = true;
    private volatile String accessToken;
    private volatile long lastUpdateTime = 0;

    public OdklApi(String clientId, String clientPublicKey, String clientSecretKey,
                   String accessToken, String refreshToken) {
        this.clientId = clientId;
        this.clientPublicKey = clientPublicKey;
        this.clientSecretKey = clientSecretKey;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    public void setAutoRefreshToken(boolean autoRefreshToken) {
        this.autoRefreshToken = autoRefreshToken;
    }

    public UsersApi users() {
        return new UsersApi(this);
    }

    public PhotosApi photos() {
        return new PhotosApi(this);
    }

    public EventsApi events() {
        return new EventsApi(this);
    }

    public FriendsApi friends() {
        return new FriendsApi(this);
    }

    public GroupsApi groups() {
        return new GroupsApi(this);
    }

    public OdklRequest createApiRequest(String group, String method) {
        return createRequest()
                .setAccessToken(accessToken)
                .setClientSecretKey(clientSecretKey)
                .setGroupAndMethod(group, method)
                .addParam("application_key", clientPublicKey);
    }

    OdklRequest createRequest() {
        return new OdklRequest();
    }

    public String sendRequest(OdklRequest request) {
        String response = request.sendRequest();
        for (int it = 0; it < MAX_REFRESH_COUNT && response.contains(TOKEN_EXPIRED_ERROR); ++it) {
            refreshToken();
            request.setAccessToken(accessToken);
            response = request.sendRequest();
        }

        if (response.contains(API_ERROR_STRING)) {
            throw new OdklApiException(JsonUtil.parseObject(response), response);
        }
        return response;
    }

    String getLoginUrl(String redirectUri, String scope) {
        return createRequest()
                .setUrl(OdklRequest.LOGIN_URL)
                .addParam("client_id", clientId)
                .addParam("scope", scope)
                .addParam("response_type", "code")
                .addParam("redirect_uri", redirectUri)
                .buildUrl();
    }

    String authorizeApp(String redirectUri, String code) {
        OdklRequest request = createRequest()
                .setUrl(OdklRequest.OAUTH_URL)
                .setSendPost(true)
                .addParam("code", code)
                .addParam("redirect_uri", redirectUri)
                .addParam("grant_type", "authorization_code")
                .addParam("client_id", clientId)
                .addParam("client_secret", clientSecretKey);
        return request.sendRequest();
    }

    private synchronized void refreshToken() {
        if (System.currentTimeMillis() - EXPIRE_TIME < lastUpdateTime) {
            return ;
        }
        String response = refreshToken(refreshToken);
        if (response.contains("error")) {
            throw new OdklApiRuntimeException(response);
        }
        JSONObject json = JsonUtil.parseObject(response);
        accessToken = JsonUtil.getString(json, "access_token");
        lastUpdateTime = System.currentTimeMillis();
    }

    String refreshToken(String refreshToken) {
        OdklRequest request = createRequest()
                .setUrl(OdklRequest.OAUTH_URL)
                .setSendPost(true)
                .addParam("refresh_token", refreshToken)
                .addParam("grant_type", "refresh_token")
                .addParam("client_id", clientId)
                .addParam("client_secret", clientSecretKey);
        return request.sendRequest();
    }

}
