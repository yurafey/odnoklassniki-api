package com.github.mastersobg.odkl;

import com.github.mastersobg.odkl.model.Group;
import com.github.mastersobg.odkl.model.PageableResponse;
import com.github.mastersobg.odkl.model.Pagination;
import com.github.mastersobg.odkl.util.JsonUtil;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.*;

/**
 * <pp>Groups</pp> API methods
 *
 * @author Ivan Gorbachev <gorbachev.ivan@gmail.com>
 */
public class GroupsApi {

    private final OdklApi api;

    GroupsApi(OdklApi api) {
        this.api = api;
    }


    /**
     * Retrieves information about specified list of groups.
     *
     * @param uids ids of groups
     * @return list of {@link Group} for every specified ID
     * @throws com.github.mastersobg.odkl.exception.OdklApiException
     *          if got API error
     * @throws com.github.mastersobg.odkl.exception.OdklApiRuntimeException
     *          if unexpected runtime error occurred
     * @see <a href="http://apiok.ru/wiki/display/api/group.getInfo">http://apiok.ru/wiki/display/api/group.getInfo</a>
     */
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

    /**
     * Retrieves the list of the members of specified group
     *
     * @param uid        group id
     * @param pagination pagination parameters
     * @return {@code PageableResponse} with ids of members of specified group
     * @throws com.github.mastersobg.odkl.exception.OdklApiException
     *          if got API error
     * @throws com.github.mastersobg.odkl.exception.OdklApiRuntimeException
     *          if unexpected runtime error occurred
     * @see <a href="http://apiok.ru/wiki/display/api/group.getMembers">http://apiok.ru/wiki/display/api/group.getMembers</a>
     */
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
        for (Object o : array) {
            JSONObject jsonObject = (JSONObject) o;
            list.add(JsonUtil.getLong(jsonObject, "userId"));
        }
        return JsonUtil.getPageableResponse(json, list);
    }

    /**
     * Retrieves information about users' membership in the specified group.
     *
     * @param groupId group id
     * @param uids    user IDs
     * @return map where a key is a user id and a value is user membership status
     * @throws com.github.mastersobg.odkl.exception.OdklApiException
     *          if got API error
     * @throws com.github.mastersobg.odkl.exception.OdklApiRuntimeException
     *          if unexpected runtime error occurred
     * @see <a href="http://apiok.ru/wiki/display/api/group.getUserGroupsByIds">http://apiok.ru/wiki/display/api/group.getUserGroupsByIds</a>
     */
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
        for (Object o : array) {
            JSONObject jsonObject = (JSONObject) o;
            Long userId = JsonUtil.getLong(jsonObject, "userId");
            Group.UserStatus status = Group.UserStatus.valueOf(JsonUtil.getString(jsonObject, "status"));
            result.put(userId, status);
        }
        return result;
    }

    /**
     * Retrieves list of the user's groups.
     *
     * @param pagination pagination parameters
     * @return {@code PageableResponse} with ids of groups the current user is member of.
     * @throws com.github.mastersobg.odkl.exception.OdklApiException
     *          if got API error
     * @throws com.github.mastersobg.odkl.exception.OdklApiRuntimeException
     *          if unexpected runtime error occurred
     * @see <a href="http://apiok.ru/wiki/display/api/group.getUserGroupsV2">http://apiok.ru/wiki/display/api/group.getUserGroupsV2</a>

     */
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
        for (Object o : array) {
            JSONObject jsonObject = (JSONObject) o;
            list.add(JsonUtil.getLong(jsonObject, "groupId"));
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
