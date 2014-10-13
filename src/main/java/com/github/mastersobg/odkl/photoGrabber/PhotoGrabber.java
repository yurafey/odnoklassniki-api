package com.github.mastersobg.odkl.photoGrabber;

import com.github.mastersobg.odkl.OdklApi;
import com.github.mastersobg.odkl.OdklRequest;
import com.github.mastersobg.odkl.exception.OdklApiException;
import com.github.mastersobg.odkl.exception.OdklApiRuntimeException;
import com.github.mastersobg.odkl.util.JsonUtil;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class PhotoGrabber {
    private final static int THREADS_NUM = PhotoGrabberConfig.THREADS_NUM;
    private final OdklApi api;
    private PhotoGrabberUtils utils = new PhotoGrabberUtils("-main", "");
    private final static int REQUEST_NUM = PhotoGrabberConfig.REQUEST_NUM;

    public PhotoGrabber(OdklApi api) {
        this.api = api;
    }

    private LinkedHashSet<String> getFriendsSet(String targetId, Integer friendsDepthLevel) {
        List<String> friendsList = getFriendsList(targetId, friendsDepthLevel);
        LinkedHashSet<String> friendsSet = new LinkedHashSet<>();
        friendsSet.addAll(friendsList);
        friendsSet.add(targetId);
        return friendsSet;
    }

    private List<String> getFriendsList(String targetId, Integer friendsDepthLevel) {
        try {
            List<String> friendsList = api.friends().getFriends(targetId);
            if ((friendsDepthLevel > 0) && (friendsList != null)) {
                int size = friendsList.size();
                for (int i = 0; i < size; i++) {
                    List<String> tmp = getFriendsList(friendsList.get(i), friendsDepthLevel - 1);
                    if (tmp != null) {
                        friendsList.addAll(tmp);
                    }
                }
            }
            if (friendsDepthLevel==0) {
                return checkUsersPrivacy(friendsList);
            }
            return friendsList;
        } catch (OdklApiException a) {
            utils.logger(String.format("bt User %s privacy error.", targetId));
            return null;
        }
    }

    public List<String> getUsersPrivacyInfo(List<String> usersList) {
        StringBuilder sb = new StringBuilder();
        for (String userId : usersList) sb.append(userId + ",");
        sb.deleteCharAt(sb.length() - 1);
        OdklRequest request = api.createApiRequest("users", "getInfo")
                .addParam("uids", sb.toString())
                .addParam("fields", "private");
        try {
            JSONArray response = JsonUtil.parseArray(api.sendRequest(request));
            if (!response.isEmpty()) {
                List<String> resList = new ArrayList<>();
                for (int i = 0; i < response.size(); i++) {
                    JSONObject obj = (JSONObject) response.get(i);
                    String prvt = JsonUtil.getString(obj, "private");
                    if (prvt.equals("false")) {
                        resList.add(JsonUtil.getString(obj, "uid"));
                    }
                }
                return resList;
            }
        } catch (OdklApiException a) {
            utils.logger(String.format("e Can't get profile privacy information."));
        } catch (OdklApiRuntimeException e) {
            utils.logger(String.format("e Can't get profile privacy information. Runtime error. Connection failed."));
        }
        return null;
    }

    public List<String> checkUsersPrivacy(List<String> usersList) {
        List<String> checkedList = new ArrayList<>();
        usersList = new ArrayList<String>(new LinkedHashSet<String>(usersList));
        int userListSize = usersList.size();
        if (userListSize <= REQUEST_NUM) {
            List<String> tmp = getUsersPrivacyInfo(usersList);
            if (tmp != null) {
                return tmp;
            }
        } else {
            int loops = usersList.size() / REQUEST_NUM;
            boolean integrally = ((usersList.size()) % REQUEST_NUM == 0);
            int start = 0;
            int last = REQUEST_NUM;
            List<String> tmp = new ArrayList<String>();
            for (int x = 0; x < loops; x++) {
                tmp = getUsersPrivacyInfo(usersList.subList(start, last));
                if (tmp != null) {
                    checkedList.addAll(tmp);
                }
                start += REQUEST_NUM;
                last += REQUEST_NUM;
            }
            if (!integrally) {
                List<String> lastList = usersList.subList((REQUEST_NUM * loops), usersList.size());
                tmp = getUsersPrivacyInfo(lastList);
                if (tmp != null) {
                    checkedList.addAll(tmp);
                }
            }
            if (!checkedList.isEmpty()) {
                return checkedList;
            }
        }
        return null;
    }

    private void checkAndFixAllUsers(LinkedHashSet<String> friendsSet) {
        utils.logger("f Starting grabbed users check.");
        Iterator<String> itr = friendsSet.iterator();
        LinkedHashSet<String> crashedUsersId = new LinkedHashSet<>();
        while (itr.hasNext()) {
            String userId = itr.next();
            if (!utils.checkUser(userId)) {
                crashedUsersId.add(userId);
            }
        }
        if (!crashedUsersId.isEmpty()) {
            Iterator<String> crashedItr = crashedUsersId.iterator();
            ExecutorService service = Executors.newFixedThreadPool(THREADS_NUM);
            while (crashedItr.hasNext()) {
                service.execute(new PhotoGrabberThread(api, crashedItr.next()));
            }
            service.shutdown();
            try {
                service.awaitTermination(99999, TimeUnit.DAYS);
            } catch (InterruptedException e) {
                //
            }
        }
        utils.logger("f Finished grabbed users check.");
    }

    public void recursivePhotoGrabWithMetadata(String targetId, Integer friendsDepthLevel) {
        utils.logger(String.format("bt Getting friends tree from user %s with depth level %s", targetId, friendsDepthLevel));
        LinkedHashSet<String> friendsSet = getFriendsSet(targetId, friendsDepthLevel);
        Iterator<String> itr = friendsSet.iterator();
        utils.logger(String.format("bt Found %s users to load.", friendsSet.size()));
        ExecutorService service = Executors.newFixedThreadPool(THREADS_NUM);
        while (itr.hasNext()) {
            service.execute(new PhotoGrabberThread(api, itr.next()));
        }
        service.shutdown();
        try {
            if (service.awaitTermination(99999, TimeUnit.DAYS)) {
                checkAndFixAllUsers(friendsSet);
            }
        } catch (InterruptedException e) {
            //
        }
        utils.logger("m Finished grabbing.");
    }
}
