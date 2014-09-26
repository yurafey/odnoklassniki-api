package com.github.mastersobg.odkl;

import com.github.mastersobg.odkl.exception.OdklApiException;
import com.github.mastersobg.odkl.util.JsonUtil;
import org.apache.commons.io.FileUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

public class PhotoGrabber {
    private final static String PHOTOS_DIR = "C://Users/yuraf_000/okapi/";
    private final static String PHOTOS_EXT = ".jpg";
    //private Integer currentLevel = null;
    private final OdklApi api;
    private FriendsApi friends = null;
    
    
    public PhotoGrabber(OdklApi api) {
        this.api = api;
        friends = new FriendsApi(api);
    }

    public void recursiveGrab(String targetId, Integer friendsDepthLevel) {
        try {
            grabUserPhotos(targetId);
            if (friendsDepthLevel != 0) {
                List<String> friendsList = friends.getFriends(targetId);
                for (int i = 0; i < friendsList.size(); i++) {
                    recursiveGrab(friendsList.get(i), friendsDepthLevel - 1);
                }
                friendsList = null;
            }
        } catch (OdklApiException a) {
            System.out.println("User " + targetId + " privacy error");
        }

    }

    public JSONArray getMarks(String photoid){
        OdklRequest request = api
                .createApiRequest("photos", "getTags")
                .addParam("photo_id", photoid);
        String response = api.sendRequest(request);
        JSONObject responseJson = JsonUtil.parseObject(response);
        JSONArray marksArray = JsonUtil.getArray(responseJson,"photo_tags");
//        for (int i = 0; i < getMarks.size(); i++){
//            JSONObject tag = (JSONObject)getMarks.get(i);
//            System.out.println(tag.get("id") + " "+ tag.get("x")+ " "+ tag.get("y"));
//
//        }
        return marksArray;

    }

    public boolean grabUserPhotos(String userId) {
        OdklRequest request = api
                .createApiRequest("photos", "getPhotos")
                .addParam("fid", userId);
        grabber(userId, api.sendRequest(request));
        return true;
    }

    public boolean grabUserMarkedPhotos(String userId){
        OdklRequest request = api
                .createApiRequest("photos", "getPhotos")
                .addParam("fid", userId)
                .addParam("aid","tags");
        grabber(userId, api.sendRequest(request));
        return true;
    }

    private boolean grabber (String userId, String response){
        JSONObject responseJson = JsonUtil.parseObject(response);
        JSONArray photos = JsonUtil.getArray(responseJson, "photos");
        for (int i = 0; i < photos.size(); i++) {
            JSONObject photo = (JSONObject) photos.get(i);
            String photoOwnerId = JsonUtil.getString(photo,"user_id"); // not used yet - photo owner id
            System.out.println(photoOwnerId);
            String imgUrl = JsonUtil.getString(photo, "pic640x480");
            String photoId = (String) photo.get("id");
            boolean fileExists = imgLoader(imgUrl,userId,photoId);
        }
        return true;
    }

    private boolean imgLoader(String imgUrl, String userId, String photoId) {
        File imageFile = new File(PHOTOS_DIR + userId + "/" + photoId + PHOTOS_EXT);
        if (!imageFile.exists()) {
            try {
                FileUtils.copyURLToFile(new URL(imgUrl), imageFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else return false;

        return true;
    }
}
