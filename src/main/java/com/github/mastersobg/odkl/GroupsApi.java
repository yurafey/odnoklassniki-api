package com.github.mastersobg.odkl;

import com.github.mastersobg.odkl.OdklApi;
import com.github.mastersobg.odkl.OdklRequest;
import com.github.mastersobg.odkl.model.Group;
import com.github.mastersobg.odkl.util.JsonUtil;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Ivan Gorbachev <gorbachev.ivan@gmail.com>
 */
public class GroupsApi {

    private final OdklApi api;

    public GroupsApi(OdklApi api) {
        this.api = api;
    }

    public List<Group> getInfo(List<String> uids) {
        if (uids == null) {
            throw new IllegalArgumentException("uids are null");
        }
        OdklRequest request = api
                .createApiRequest("group", "getInfo")
                .addParam("fields", "uid,name,description,shortname,pic_avatar,shop_visible_admin,shop_visible_public")
                .addParam("uids", join(uids));

        JSONArray array = JsonUtil.parseArray(api.sendRequest(request));
        List<Group> groups = new ArrayList<Group>(array.size());
        for (Object o : array) {
            groups.add(new Group((JSONObject) o));
        }
        return groups;
    }

    public List<Long> getMembers(Long uid) {
        return null;
    }

    private static String join(List<String> uids) {
        if (uids.isEmpty()) {
            return "";
        }
        Iterator<String> it = uids.iterator();
        StringBuilder result = new StringBuilder(it.next());
        while(it.hasNext()) {
            result.append(",").append(it.next());
        }
        return result.toString();
    }
}
