package com.github.mastersobg.odkl.photoGrabber;

import com.github.mastersobg.odkl.OdklApi;
import com.github.mastersobg.odkl.OdklRequest;
import com.github.mastersobg.odkl.exception.OdklApiException;
import com.github.mastersobg.odkl.exception.OdklApiRuntimeException;
import com.github.mastersobg.odkl.util.JsonUtil;
import org.apache.commons.io.FileUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PhotoGrabber {
    private final static String PHOTOS_DIR = PhotoGrabberConfig.PHOTOS_DIR;
    private final static String PHOTOS_EXT = PhotoGrabberConfig.PHOTOS_EXT;
    private final static boolean LOGS = PhotoGrabberConfig.LOGS;
    private final OdklApi api;

    public PhotoGrabber(OdklApi api) {
        this.api = api;
    }

    public void recursivePhotoGrabWithMetadata(String targetId, Integer friendsDepthLevel) {
        List<String> friendsList = grabUserPhotosWithMetadata(targetId);
        //recursive part
        if (friendsDepthLevel != 0 && friendsList!=null) {
            if (friendsList!=null) {
                for (int i = 0; i < friendsList.size(); i++) {
                    recursivePhotoGrabWithMetadata(friendsList.get(i), friendsDepthLevel - 1);
                }
            }
        }
    }

    public List<String> grabUserPhotosWithMetadata(String targetId) {
        if (LOGS) System.out.println("[MSG] Starting user " + targetId + " grab.");
        try {
            JsonUser userJson = new JsonUser(targetId);
            JSONArray friendsArray = new JSONArray();
            List<String> friendsList = api.friends().getFriends(targetId);
            if (friendsList!=null) friendsArray.addAll(friendsList);
            userJson.setFriends(friendsArray);
            userJson.addMarkedPhoto(getMetainfoJsonArray(grabUserMarkedPhotos(targetId))); //grab marked photos from friends albums
            userJson.addMarkedPhoto(getMetainfoJsonArray(grabUserPhotos(targetId))); //grab user photos from personal album
            userJson.addMarkedPhoto(getMetainfoJsonArray(grabUserMarkedPhotosFromPersonalAlbums(targetId))); // grab user marked photos from all his albums (except his personal album)
            if(!userJson.isEmpty()) {
                userJson.writeJson();
                if (LOGS) System.out.println("[MSG] Finished user " + targetId + " grab.");
            }
            return friendsList;
        } catch (OdklApiException a) {
            if (LOGS) System.out.println("[ERR] User " + targetId + " privacy error.");
        }
        return null;
    }

    private JSONArray getMetainfoJsonArray(Map<String, Map<String, Object>> metainfoMap) {
        if (metainfoMap!=null) {
            JSONArray resultArray = new JSONArray();
            for (String photoId : metainfoMap.keySet()) {
                Map<String, Object> currentOwnerIdAndTagsMap = metainfoMap.get(photoId);
                JSONObject currentTag = new JSONObject();
                currentTag.put("photoId", photoId);
                currentTag.put("photoOwner", currentOwnerIdAndTagsMap.get("ownerId"));
                currentTag.put("x", ((JSONObject) (currentOwnerIdAndTagsMap.get("tag"))).get("x"));
                currentTag.put("y", ((JSONObject) (currentOwnerIdAndTagsMap.get("tag"))).get("y"));
                resultArray.add(currentTag);
            }
            return resultArray;
        }
        return null;
    }

    private Map<String,Map<String,Object>> grabUserPhotos(String userId) {
        OdklRequest request = api
                .createApiRequest("photos", "getPhotos")
                .addParam("fid", userId)
                .addParam("count","100");
        return recursiveGrabber(request,userId,null);
    }

    private Map<String,Map<String,Object>> grabUserMarkedPhotos(String userId) {
        OdklRequest request = api
                .createApiRequest("photos", "getPhotos")
                .addParam("fid", userId)
                .addParam("aid","tags")
                .addParam("count","100");
        return recursiveGrabber(request,userId,null);
    }

    private Map<String, Map<String, Object>> recursiveGrabber(OdklRequest request, String userId, Map<String, Map<String, Object>> fullMap) {
        try {
            String response = api.sendRequest(request);
            JSONObject responseJson = JsonUtil.parseObject(response);
            Map<String, Map<String, Object>> grabberTemp = grabber(userId, responseJson);
            if (grabberTemp != null) {
                if (fullMap == null) fullMap = grabberTemp;
                else {
                    fullMap.putAll(grabberTemp);
                }
            }
            if ((Boolean) responseJson.get("hasMore")) {
                String anchor = (String) responseJson.get("anchor");
                return recursiveGrabber(request.addParam("anchor", anchor), userId, fullMap);
            }
            return fullMap;
        } catch (OdklApiRuntimeException e) {
            if (LOGS) System.out.println("[ERR] Runtime error. Can't load user profile. Connection failed.");
        }
        return null;
    }

    private Map<String,Map<String,Object>> grabber(String userId, JSONObject responseJson) {
        JSONArray photos = JsonUtil.getArray(responseJson, "photos");
        Map<String,Map<String,Object>> photoIdOwnerIdTagsMap = new HashMap<String, Map<String, Object>>();
        for (int i = 0; i < photos.size(); i++) {
            JSONObject photo = (JSONObject) photos.get(i);
            String photoOwnerId = JsonUtil.getString(photo,"user_id");
            String imgUrl = JsonUtil.getString(photo, "pic640x480");
            String photoId = (String) photo.get("id");
            JSONObject currentTag = getUserMarkFromPhoto(userId, photoId);
            if (currentTag!=null) {
                Map<String,Object> additionMap = new HashMap<String, Object>();
                additionMap.put("ownerId",photoOwnerId);
                additionMap.put("tag",currentTag);
                photoIdOwnerIdTagsMap.put(photoId, additionMap);
            }
            loadImage(imgUrl, userId, photoId);
        }
        return photoIdOwnerIdTagsMap.isEmpty()?null:photoIdOwnerIdTagsMap;
    }

    private JSONObject getUserMarkFromPhoto(String userId, String photoId) {
        JSONArray marksArray = getAllMarksFromPhoto(photoId);
        if (marksArray!=null){
            for (int i = 0; i < marksArray.size(); i++) {
                JSONObject tag = (JSONObject) marksArray.get(i);
                if (tag.get("id").equals(userId)) {
                    tag.put("photo_id",photoId);
                    return tag;
                }
            }
        }
        return null;
    }

    private JSONArray getAllMarksFromPhoto(String photoId) {
        try {
            OdklRequest request = api
                    .createApiRequest("photos", "getTags")
                    .addParam("photo_id", photoId);
            String response = api.sendRequest(request);
            JSONObject responseJson = JsonUtil.parseObject(response);
            JSONArray marksArray = JsonUtil.getArray(responseJson, "photo_tags");
            return marksArray.isEmpty()?null:marksArray;
        } catch (OdklApiRuntimeException e){
            if (LOGS) System.out.println("[ERR] Runtime error. Can't load marks from photo. Connection failed.");
        }
        return null;
    }

    private List<String> getUserAlbumIds(String userId) {
        try {
            OdklRequest request = api
                    .createApiRequest("photos", "getAlbums")
                    .addParam("fid", userId)
                    .addParam("count", "100");
            JSONObject responseJson = JsonUtil.parseObject(api.sendRequest(request));
            JSONArray albumArray = JsonUtil.getArray(responseJson, "albums");
            List<String> albumIds = new ArrayList<String>();
            for (int i = 0; i < albumArray.size(); i++) {
                JSONObject album = (JSONObject) albumArray.get(i);
                albumIds.add(JsonUtil.getString(album, "aid"));
            }
            return albumIds;
        } catch (OdklApiRuntimeException e) {
            if (LOGS) System.out.println("[ERR] Runtime error. Can't get user album id's. Connection failed.");
        }
        return null;
    }

    private Map<String,Map<String,Object>> grabUserMarkedPhotosFromPersonalAlbums(String userId) {
        Map<String,Map<String,Object>> photoIdOwnerIdTagsMap = new HashMap<String, Map<String, Object>>();
        List<String> albumList = getUserAlbumIds(userId);
        if (albumList!=null){
            for (int albumIterator=0; albumIterator < albumList.size(); albumIterator++){
                List<String> photoIdList = getAlbumPhotoIdList(userId, albumList.get(albumIterator));
                if(photoIdList!=null) {
                    for (int i = 0; i < photoIdList.size(); i++) {
                        String photoId = photoIdList.get(i);
                        JSONObject tag = getUserMarkFromPhoto(userId, photoId);
                        if (tag != null) {
                            Map<String, Object> additionMap = new HashMap<String, Object>();
                            additionMap.put("ownerId", userId);
                            additionMap.put("tag", tag);
                            photoIdOwnerIdTagsMap.put(photoId, additionMap);
                            loadImage(getPhotoUrl(photoId), userId, photoId);
                        }
                    }
                }
            }
        }
        return photoIdOwnerIdTagsMap.isEmpty()?null:photoIdOwnerIdTagsMap;
    }

    private String getPhotoUrl(String photoId) {
        OdklRequest request = api
                .createApiRequest("photos", "getPhotoInfo")
                .addParam("photo_id", photoId);
        JSONObject responseJson = JsonUtil.parseObject(api.sendRequest(request));
        return JsonUtil.getString(((JSONObject)responseJson.get("photo")), "pic640x480");
    }

    private List<String> getAlbumPhotoIdList(String userId, String albumId) {
        OdklRequest request = api
                .createApiRequest("photos", "getPhotos")
                .addParam("fid", userId)
                .addParam("aid",albumId)
                .addParam("count","100");
        return recursiveGetPhotoList(request,userId,null);
    }

    private List<String> recursiveGetPhotoList(OdklRequest request, String userId, List<String> fullList) {
        try {
            JSONObject responseJson = JsonUtil.parseObject(api.sendRequest(request));
            if (fullList == null) fullList = grabList(responseJson);
            else {
                fullList.addAll(grabList(responseJson));
            }
            if ((Boolean) responseJson.get("hasMore")) {
                String anchor = (String) responseJson.get("anchor");
                return recursiveGetPhotoList(request.addParam("anchor", anchor), userId, fullList);
            }
            return fullList;
        } catch (OdklApiRuntimeException e) {
            if (LOGS) System.out.println("[ERR] Runtime error. Can't get user photo list. Connection failed.");
        }
        return null;
    }

    private List<String> grabList(JSONObject responseJson){
        List<String> fullList = new ArrayList<String>();
        JSONArray photos = JsonUtil.getArray(responseJson, "photos");
        for (int i = 0; i < photos.size(); i++) {
            JSONObject photo = (JSONObject) photos.get(i);
            fullList.add((String) photo.get("id"));
        }
        return fullList;
    }

    private boolean loadImage(String imgUrl, String userId, String photoId) {
        File imageFile = new File(PHOTOS_DIR + userId + "/" + photoId + PHOTOS_EXT);
        if (!imageFile.exists()) {
            try {
                FileUtils.copyURLToFile(new URL(imgUrl), imageFile);
            } catch (IOException e) {
                if (LOGS) System.out.println("[ERR] Image "+imgUrl+" photoId "+photoId+" load error.");
            }
        } else {
            return false;
        }
        return true;
    }
}
