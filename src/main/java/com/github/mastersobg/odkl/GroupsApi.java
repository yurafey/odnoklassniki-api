package com.github.mastersobg.odkl;

import com.github.mastersobg.odkl.model.Group;
import com.github.mastersobg.odkl.model.PageableResponse;
import com.github.mastersobg.odkl.model.Pagination;
import com.github.mastersobg.odkl.util.JsonUtil;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.*;

/**
 * @author Ivan Gorbachev <gorbachev.ivan@gmail.com>
 */
public class GroupsApi {

    private final OdklApi api;

    public GroupsApi(OdklApi api) {
        this.api = api;
    }

    public List<Group> getInfo(List<Long> uids) {
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

    public PageableResponse<Long> getMembers(Long uid, Pagination pagination) {
        if (uid == null) {
            throw new IllegalArgumentException("uid is null");
        }

        if (pagination == null) {
            throw new IllegalArgumentException("pagination is null");
        }

        OdklRequest request = api
                .createApiRequest("group", "getMembers")
                .addParam("uid", uid.toString())
                .addParams(pagination.asParamsMap());

        JSONObject json = JsonUtil.parseObject(api.sendRequest(request));
        JSONArray array = JsonUtil.getArray(json, "members");

        List<Long> list = new ArrayList<Long>();
        if (array != null) {
            for (Object o : array) {
                JSONObject jsonObject = (JSONObject) o;
                list.add(JsonUtil.getLong(jsonObject, "userId"));
            }
        }
        return JsonUtil.getPageableResponse(json, list);
    }

    public Map<Long, Group.UserStatus> getUserGroupsByIds(Long groupId, List<Long> uids) {
        if (groupId == null) {
            throw new IllegalArgumentException("groupId is null");
        }
        if (uids == null) {
            throw new IllegalArgumentException("uids are null");
        }

        OdklRequest request = api
                .createApiRequest("group", "getUserGroupsByIds")
                .addParam("group_id", groupId.toString())
                .addParam("uids", join(uids));

        JSONArray array = JsonUtil.parseArray(api.sendRequest(request));
        Map<Long, Group.UserStatus> result = new HashMap<Long, Group.UserStatus>();
        if (array != null) {
            for (Object o : array) {
                JSONObject jsonObject = (JSONObject) o;
                Long userId = JsonUtil.getLong(jsonObject, "userId");
                Group.UserStatus status = Group.UserStatus.valueOf(JsonUtil.getString(jsonObject, "status"));
                result.put(userId, status);
            }
        }
        return result;
    }

    public PageableResponse<Long> getUserGroupsV2(Pagination pagination) {
        if (pagination == null) {
            throw new IllegalArgumentException("pagination is null");
        }

        OdklRequest request = api
                .createApiRequest("group", "getUserGroupsV2")
                .addParams(pagination.asParamsMap());

        JSONObject json = JsonUtil.parseObject(api.sendRequest(request));
        JSONArray array = JsonUtil.getArray(json, "groups");

        List<Long> list = new ArrayList<Long>();
        if (array != null) {
            for (Object o : array) {
                JSONObject jsonObject = (JSONObject) o;
                list.add(JsonUtil.getLong(jsonObject, "groupId"));
            }
        }

        return JsonUtil.getPageableResponse(json, list);
    }

    private static <Long> String join(List<Long> uids) {
        if (uids.isEmpty()) {
            return "";
        }
        Iterator<Long> it = uids.iterator();
        StringBuilder result = new StringBuilder(it.next().toString());
        while (it.hasNext()) {
            result.append(",").append(it.next().toString());
        }
        return result.toString();
    }
}
