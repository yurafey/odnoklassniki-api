package com.github.mastersobg.odkl;

import com.github.mastersobg.odkl.OdklApi;
import com.github.mastersobg.odkl.OdklRequest;
import com.github.mastersobg.odkl.model.User;
import com.github.mastersobg.odkl.util.JsonUtil;

/**
 * @author Ivan Gorbachev <gorbachev.ivan@gmail.com>
 */
public class UsersApi {

    private final OdklApi api;

    public UsersApi(OdklApi api) {
        this.api = api;
    }

    public User getCurrentUser() {
        OdklRequest request = api.createApiRequest("users", "getCurrentUser");
        return new User(JsonUtil.parseObject(api.sendRequest(request)));
    }

    public String getSettings() {
        OdklRequest request = api.createApiRequest("users", "getSettings");
        return api.sendRequest(request);
    }
}
