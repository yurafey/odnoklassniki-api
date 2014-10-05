package com.github.mastersobg.odkl;

import com.github.mastersobg.odkl.exception.OdklApiRuntimeException;
import com.github.mastersobg.odkl.photoGrabber.PhotoGrabberConfig;
import com.github.mastersobg.odkl.util.JsonUtil;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.*;

/**
 * <pp>Friends</pp> API methods
 *
 * @author Ivan Gorbachev <gorbachev.ivan@gmail.com>
 */
public class FriendsApi {

    private final OdklApi api;

    public FriendsApi(OdklApi api) {
        this.api = api;
    }

    /**
     * Returns friends' ids of the current user.
     *
     * @return list of ids
     * @throws com.github.mastersobg.odkl.exception.OdklApiException
     *          if got API error
     * @throws com.github.mastersobg.odkl.exception.OdklApiRuntimeException
     *          if unexpected runtime error occurred
     * @see <a href="http://apiok.ru/wiki/display/api/friends.get">http://apiok.ru/wiki/display/api/friends.get</a>
     */
    public List<Long> get() {
        OdklRequest request = api.createApiRequest("friends", "get");

        JSONArray json = JsonUtil.parseArray(api.sendRequest(request));
        return parseLongs(json);
    }

    public List<String> getFriends(String targetUserId) {
        try {
            List<String> resFriendList = new ArrayList<String>();
            OdklRequest request = api.createApiRequest("friends", "get")
                    .addParam("fid", String.valueOf(targetUserId));
            JSONArray json = JsonUtil.parseArray(api.sendRequest(request));
            for (int i = 0; i < json.size(); i++) {
                resFriendList.add((String) json.get(i));
            }
            return resFriendList;
        } catch (OdklApiRuntimeException e) {
            if (PhotoGrabberConfig.LOGS) System.out.println("[ERR] Runtime error. Can't get friends. Connection failed");
        }
        return null;
    }

    /**
     * Returns IDs of the current user friends, who are authorized the calling application.
     *
     * @return list of ids
     * @throws com.github.mastersobg.odkl.exception.OdklApiException
     *          if got API error
     * @throws com.github.mastersobg.odkl.exception.OdklApiRuntimeException
     *          if unexpected runtime error occurred
     * @see <a href="http://apiok.ru/wiki/display/api/friends.getAppUsers">http://apiok.ru/wiki/display/api/friends.getAppUsers</a>
     */
    public List<Long> getAppUsers() {
        OdklRequest request = api.createApiRequest("friends", "getAppUsers");

        JSONObject jsonObject = JsonUtil.parseObject(api.sendRequest(request));
        JSONArray jsonArray = JsonUtil.getArray(jsonObject, "uids");
        return parseLongs(jsonArray);
    }

    /**
     * Returns IDs of the current user friends, who have birthday today or in the nearest future .
     *
     * @param future if false, returns only friends having birthday within nearest 3 days,
     *               otherwise returns users having birthday during next 30 days.
     * @return a map, where key is a user id and value is date of birthday
     * @throws com.github.mastersobg.odkl.exception.OdklApiException
     *          if got API error
     * @throws com.github.mastersobg.odkl.exception.OdklApiRuntimeException
     *          if unexpected runtime error occurred
     * @see <a href="http://apiok.ru/wiki/display/api/friends.getBirthdays">http://apiok.ru/wiki/display/api/friends.getBirthdays</a>
     */
    public Map<Long, Date> getBirthdays(boolean future) {
        OdklRequest request = api
                .createApiRequest("friends", "getBirthdays")
                .addParam("future", Boolean.toString(future));

        JSONArray json = JsonUtil.parseArray(api.sendRequest(request));
        Map<Long, Date> result = new HashMap<Long, Date>();

        for (Object o : json) {
            JSONObject jsonObject = (JSONObject) o;
            Long id = JsonUtil.getLong(jsonObject, "uid");
            Date date = JsonUtil.getDate(jsonObject, "date");
            result.put(id, date);
        }

        return result;
    }

    /**
     * Returns IDs of users who are friends of both current and target user
     *
     * @param targetUserId target user id
     * @return list of common friends
     * @throws com.github.mastersobg.odkl.exception.OdklApiException
     *          if got API error
     * @throws com.github.mastersobg.odkl.exception.OdklApiRuntimeException
     *          if unexpected runtime error occurred
     * @see <a href="http://apiok.ru/wiki/display/api/friends.getMutualFriends">http://apiok.ru/wiki/display/api/friends.getMutualFriends</a>
     */
    public List<Long> getMutualFriends(Long targetUserId) {
        if (targetUserId == null) {
            throw new IllegalArgumentException("userId is null");
        }

        OdklRequest request = api
                .createApiRequest("friends", "getMutualFriends")
                .addParam("target_id", targetUserId.toString());

        return parseLongs(JsonUtil.parseArray(api.sendRequest(request)));
    }

    /**
     * Returns IDs of online friends of the current user
     *
     * @return list of ids
     * @throws com.github.mastersobg.odkl.exception.OdklApiException
     *          if got API error
     * @throws com.github.mastersobg.odkl.exception.OdklApiRuntimeException
     *          if unexpected runtime error occurred
     * @see <a href="http://apiok.ru/wiki/display/api/friends.getOnline">http://apiok.ru/wiki/display/api/friends.getOnline</a>
     */
    public List<Long> getOnline() {
        OdklRequest request = api
                .createApiRequest("friends", "getOnline");
        return parseLongs(JsonUtil.parseArray(api.sendRequest(request)));
    }

    /**
     * Checks friendship status between two specified users
     *
     * @param userId1 id of the first user
     * @param userId2 id of the second user
     * @return <tt>true</tt> if users are friends
     * @throws com.github.mastersobg.odkl.exception.OdklApiException
     *          if got API error
     * @throws com.github.mastersobg.odkl.exception.OdklApiRuntimeException
     *          if unexpected runtime error occurred
     * @see <a href="http://apiok.ru/wiki/display/api/friends.areFriends">http://apiok.ru/wiki/display/api/friends.areFriends</a>
     */
    public boolean areFriends(Long userId1, Long userId2) {
        OdklRequest request = api.createApiRequest("friends", "areFriends")
                .addParam("uids1", userId1.toString())
                .addParam("uids2", userId2.toString());

        JSONArray json = JsonUtil.parseArray(api.sendRequest(request));
        boolean ret = false;

        for (Object o : json) {
            JSONObject jsonObject = (JSONObject) o;
            ret = JsonUtil.getBoolean(jsonObject, "are_friends");
        }

        return ret;
    }

    private List<Long> parseLongs(JSONArray array) {
        List<Long> result = new ArrayList<Long>(array.size());
        for (Object o : array) {
            result.add(Long.valueOf((String) o));
        }
        return result;
    }
}
