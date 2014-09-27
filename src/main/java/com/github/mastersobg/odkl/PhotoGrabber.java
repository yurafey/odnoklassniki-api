package com.github.mastersobg.odkl;

import com.github.mastersobg.odkl.exception.OdklApiException;
import com.github.mastersobg.odkl.util.JsonUtil;
import org.apache.commons.io.FileUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
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

    public void recursivePhotoGrab(String targetId, Integer friendsDepthLevel) {
        try {
            grabUserPhotos(targetId);
            if (friendsDepthLevel != 0) {
                List<String> friendsList = friends.getFriends(targetId);
                for (int i = 0; i < friendsList.size(); i++) {
                    recursivePhotoGrab(friendsList.get(i), friendsDepthLevel - 1);
                }
                friendsList = null;
            }
        } catch (OdklApiException a) {
            System.out.println("User " + targetId + " privacy error");
        }

    }

    public void recursivePhotoGrabWithMetadata(String targetId, Integer friendsDepthLevel) {
        try {
            grabUserPhotosWithMetadata(targetId);
            //recursive part
            if (friendsDepthLevel != 0) {
                List<String> friendsList = friends.getFriends(targetId);
                for (int i = 0; i < friendsList.size(); i++) {
                    recursivePhotoGrabWithMetadata(friendsList.get(i), friendsDepthLevel - 1);
                }
                friendsList = null;
            }
        } catch (OdklApiException a) {
            System.out.println("User " + targetId + " privacy error");
        }

    }

    public void grabUserPhotosWithMetadata (String targetId){
        JsonUser userJson = new JsonUser(targetId);

        JSONArray friendsArray = new JSONArray();           //friends retrieving
        friendsArray.addAll(friends.getFriends(targetId));  //
        userJson.setFriends(friendsArray);                  //

        HashMap<String,String> photoIdOwnerIdMap = grabUserMarkedPhotos(targetId);
        for (String photoId: photoIdOwnerIdMap.keySet()){
            JSONObject userMarkFromPhoto = getUserMarkFromPhoto(targetId, photoId);
            if (userMarkFromPhoto == null ) System.out.println("NULL POINTER EXCEPTION");
            else {
                System.out.println(userMarkFromPhoto.toString());
                userJson.addMarkedPhoto(photoId, photoIdOwnerIdMap.get(photoId), (Long) userMarkFromPhoto.get("x"), (Long) userMarkFromPhoto.get("y"));
            }
        }
        grabUserPhotos(targetId);
        userJson.writeJson();
    }


    public JSONObject getUserMarkFromPhoto(String userId, String photoId) {
        JSONArray marksArray = getAllMarksFromPhoto(photoId);
        for (int i = 0; i < marksArray.size(); i++) {
            JSONObject tag = (JSONObject) marksArray.get(i);
            if (((String) tag.get("id")).equals(userId)) {
                return tag;
            }
        }
        return null;
    }

    public JSONArray getAllMarksFromPhoto(String photoId){
        OdklRequest request = api
                .createApiRequest("photos", "getTags")
                .addParam("photo_id", photoId);
        String response = api.sendRequest(request);
        JSONObject responseJson = JsonUtil.parseObject(response);
        JSONArray marksArray = JsonUtil.getArray(responseJson,"photo_tags");
//        for (int i = 0; i < marksArray.size(); i++){
//            JSONObject tag = (JSONObject)marksArray.get(i);
//            System.out.println(tag.get("id") + " "+ tag.get("x")+ " "+ tag.get("y"));
//
//        }
        return marksArray;

    }

    public HashMap<String,String> grabUserPhotos(String userId) {
        OdklRequest request = api
                .createApiRequest("photos", "getPhotos")
                .addParam("fid", userId);

        return grabber(userId, api.sendRequest(request));
    }

    public HashMap<String,String> grabUserMarkedPhotos(String userId){
        OdklRequest request = api
                .createApiRequest("photos", "getPhotos")
                .addParam("fid", userId)
                .addParam("aid","tags");
        return grabber(userId, api.sendRequest(request));
    }



    private HashMap<String,String> grabber (String userId, String response){
        JSONObject responseJson = JsonUtil.parseObject(response);
        JSONArray photos = JsonUtil.getArray(responseJson, "photos");
        HashMap <String,String> PhotoIdOwnerMap = new HashMap();
        for (int i = 0; i < photos.size(); i++) {
            JSONObject photo = (JSONObject) photos.get(i);
            String photoOwnerId = JsonUtil.getString(photo,"user_id");
            String imgUrl = JsonUtil.getString(photo, "pic640x480");
            String photoId = (String) photo.get("id");
            PhotoIdOwnerMap.put(photoId, photoOwnerId);
            boolean fileExists = imgLoader(imgUrl,userId,photoId);
        }
        return PhotoIdOwnerMap;
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
