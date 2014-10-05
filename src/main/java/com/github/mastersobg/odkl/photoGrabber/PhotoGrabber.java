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
        if (!checkUser(targetId)){
            try {
                if (LOGS) System.out.println(String.format("[MSG][uid%s] Starting grab.",targetId));
                JsonUser userJson = new JsonUser(targetId);
                JSONArray friendsArray = new JSONArray();
                List<String> friendsList = api.friends().getFriends(targetId);
                if (friendsList != null) {
                    friendsArray.addAll(friendsList);
                    userJson.setFriends(friendsArray);
                }
                if (LOGS) System.out.println(String.format("[MSG][uid%s] Grabbing photos from person album.",targetId));
                userJson.addMetainfo(grabUserMarkedPhotos(targetId)); //grab marked photos from friends albums
                if (LOGS) System.out.println(String.format("[MSG][uid%s] Grabbing marked photos from user friends.",targetId));
                userJson.addMetainfo(grabUserPhotos(targetId)); //grab user photos from personal album
                if (LOGS) System.out.println(String.format("[MSG][uid%s] Grabbing marked photos from all user albums.",targetId));
                userJson.addMetainfo(grabUserMarkedPhotosFromPersonalAlbums(targetId)); // grab user marked photos from all his albums (except his personal album)
                if(!userJson.isEmpty()) {
                    userJson.writeJson();
                    if (LOGS) System.out.println(String.format("[MSG][uid%s] Finished grab.",targetId));
                }
                return friendsList;
            } catch (OdklApiException a) {
                if (LOGS)  System.out.println(String.format("[ERR][uid%s] Privacy error.",targetId));
            }
        } else {
            List<String> friendsList = api.friends().getFriends(targetId);
            if (friendsList != null) {
                return friendsList;
            }
        }
        return null;
    }

    private boolean checkUser(String userId){
        File imageFile = new File(PHOTOS_DIR + userId);
        if (imageFile.exists()) {
            return true;
        }
        else return false;
    }

    private Map<String,Object> grabUserPhotos(String userId) {
        OdklRequest request = api
                .createApiRequest("photos", "getPhotos")
                .addParam("fid", userId)
                .addParam("count","100");
        Map<String,Object> result = recursiveGrabber(request,userId,null);
        if ((int)result.get("photosNum")!=0) {
            result.put("albumsNum",1);
        }
        return result;
    }

    private Map<String,Object> grabUserMarkedPhotos(String userId) {
        OdklRequest request = api
                .createApiRequest("photos", "getPhotos")
                .addParam("fid", userId)
                .addParam("aid","tags")
                .addParam("count","100");
        return recursiveGrabber(request,userId,null);
    }

    private Map<String, Object> appendMap(Map<String, Object> targetMap, Map<String, Object> tempMap){
        int tempPhotosNum = (int) targetMap.get("photosNum");
        targetMap.remove("photosNum");
        targetMap.put("photosNum",(int)tempMap.get("photosNum")+tempPhotosNum);
        tempMap.remove("photosNum");
        targetMap.putAll(targetMap);
        return targetMap;
    }

    private Map<String, Object> recursiveGrabber(OdklRequest request, String userId, Map<String, Object> fullMap) {
        try {
            String response = api.sendRequest(request);
            JSONObject responseJson = JsonUtil.parseObject(response);
            Map<String, Object> grabberTemp = grabber(userId, responseJson);
            if (grabberTemp != null) {
                if (fullMap == null) {
                    fullMap = grabberTemp;
                } else {
                    fullMap = appendMap(fullMap, grabberTemp);
                }
            }
            if ((Boolean) responseJson.get("hasMore")) {
                String anchor = (String) responseJson.get("anchor");
                return recursiveGrabber(request.addParam("anchor", anchor), userId, fullMap);
            }
            return fullMap;
        } catch (OdklApiRuntimeException e) {
            if (LOGS) {
                System.out.println(String.format("[ERR][uid%s] Runtime error while loading profile. Connection failed.", userId));
            }
        }
        return null;
    }

    private Map<String, Object> grabber(String userId, JSONObject responseJson) {
        JSONArray photos = JsonUtil.getArray(responseJson, "photos");
        Map<String, Object> photoIdOwnerIdTagsMap = new HashMap<String, Object>();
        int photosNum = 0;
        if (photos != null && photos.size() != 0) {
            for (int i = 0; i < photos.size(); i++) {
                photosNum++;
                JSONObject photo = (JSONObject) photos.get(i);
                String photoOwnerId = JsonUtil.getString(photo, "user_id");
                String imgUrl = JsonUtil.getString(photo, "pic640x480");
                String photoId = (String) photo.get("id");
                JSONObject currentTag = getUserMarkFromPhoto(userId, photoId);
                if (currentTag != null) {
                    Map<String, Object> additionMap = new HashMap<String, Object>();
                    additionMap.put("photoOwner", photoOwnerId);
                    additionMap.put("tag", currentTag);
                    photoIdOwnerIdTagsMap.put(photoId, additionMap);
                }
                loadImage(imgUrl, userId, photoId);
            }
        }
        photoIdOwnerIdTagsMap.put("photosNum", photosNum);
        return photoIdOwnerIdTagsMap;
    }

    private JSONObject getUserMarkFromPhoto(String userId, String photoId) {
        JSONArray marksArray = getAllMarksFromPhoto(photoId);
        if (marksArray != null && marksArray.size() != 0) {
            for (int i = 0; i < marksArray.size(); i++) {
                JSONObject tag = (JSONObject) marksArray.get(i);
                if (tag.get("id").equals(userId)) {
                    tag.put("marksNum", marksArray.size());
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
            return marksArray.isEmpty() ? null : marksArray;
        } catch (OdklApiRuntimeException e) {
            if (LOGS) {
                System.out.println(String.format("[ERR][] Runtime error while loading marks from photo %s. Connection failed.", photoId));
            }
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
            if (LOGS) {
                System.out.println(String.format("[ERR][uid%s] Runtime error while loading user albums list. Connection failed.", userId));
            }
        }
        return null;
    }

    private Map<String, Object> grabUserMarkedPhotosFromPersonalAlbums(String userId) {
        Map<String, Object> photoIdOwnerIdTagsMap = new HashMap<String, Object>();
        List<String> albumList = getUserAlbumIds(userId);
        if (albumList != null) {
            int albumsNum = 0;
            int photosNum = 0;
            for (int albumIterator = 0; albumIterator < albumList.size(); albumIterator++) {
                String albumId = albumList.get(albumIterator);
                List<String> photoIdList = getAlbumPhotoIdList(userId, albumId);
                if (photoIdList != null && photoIdList.size() != 0) {
                    photosNum += photoIdList.size();
                    albumsNum++;
                    for (int i = 0; i < photoIdList.size(); i++) {
                        String photoId = photoIdList.get(i);
                        JSONObject tag = getUserMarkFromPhoto(userId, photoId);
                        if (tag != null) {
                            Map<String, Object> additionMap = new HashMap<String, Object>();
                            additionMap.put("photoOwner", userId);
                            additionMap.put("tag", tag);
                            photoIdOwnerIdTagsMap.put(photoId, additionMap);
                            if (LOGS) {
                                System.out.println(String.format("[MSG][uid%s] Found user mark on photo %s from album %s.", userId, photoId, albumId));
                            }
                            loadImage(getPhotoUrl(photoId), userId, photoId);
                        }
                    }
                }
            }
            photoIdOwnerIdTagsMap.put("albumsNum", albumsNum);
            photoIdOwnerIdTagsMap.put("photosNum", photosNum);
        }
        return photoIdOwnerIdTagsMap;
    }

    private String getPhotoUrl(String photoId) {
        OdklRequest request = api
                .createApiRequest("photos", "getPhotoInfo")
                .addParam("photo_id", photoId);
        JSONObject responseJson = JsonUtil.parseObject(api.sendRequest(request));
        return JsonUtil.getString(((JSONObject) responseJson.get("photo")), "pic640x480");
    }

    private List<String> getAlbumPhotoIdList(String userId, String albumId) {
        OdklRequest request = api
                .createApiRequest("photos", "getPhotos")
                .addParam("fid", userId)
                .addParam("aid", albumId)
                .addParam("count", "100");
        return recursiveGetPhotoList(request, userId, null);
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
            if (LOGS) {
                System.out.println(String.format("[ERR][uid%s] Runtime error while loading photo list of the album. Connection failed.", userId));
            }
        }
        return null;
    }

    private List<String> grabList(JSONObject responseJson) {
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
                if (LOGS) {
                    System.out.println(String.format("[ERR][] Image %s photoId %s load error.", imgUrl, photoId));
                }
            }
        } else {
            if (LOGS) {
                System.out.println(String.format("[MSG][] Photo %s already exists.", photoId));
            }
            return false;
        }
        return true;
    }
}
