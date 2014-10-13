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

/**
 * Created by yuraf_000 on 08.10.2014.
 */
public class PhotoGrabberThread extends Thread {
    private final static String PHOTOS_DIR = PhotoGrabberConfig.PHOTOS_DIR;
    private final static String PHOTOS_EXT = PhotoGrabberConfig.PHOTOS_EXT;
    private final OdklApi api;
    private final String targetId;
    private String threadId;
    private PhotoGrabberUtils utils;


    public PhotoGrabberThread(OdklApi api, String targetId) {
        this.api = api;
        this.targetId = targetId;
    }

    public void run() {
        grabUserPhotosWithMetadata(targetId);
    }

    private void grabUserPhotosWithMetadata(String targetId) {
        this.threadId = Thread.currentThread().getName();
        utils = new PhotoGrabberUtils(threadId, targetId);
        if (!utils.checkUser(targetId)) {
            try {
                utils.logger("m Starting grab.");
                JsonUser userJson = new JsonUser(targetId);
                JSONArray friendsArray = new JSONArray();
                List<String> friendsList = api.friends().getFriends(targetId);
                if (friendsList != null) {
                    friendsArray.addAll(friendsList);
                    userJson.setFriends(friendsArray);
                }
                utils.logger("m Grabbing marked photos from user friends.");
                userJson.addMetainfo(grabUserMarkedPhotos(targetId)); //grab marked photos from friends albums

                utils.logger("m Grabbing photos from personal album.");
                userJson.addMetainfo(grabUserPhotos(targetId)); //grab user photos from personal album

                utils.logger("m Grabbing marked photos from all user albums.");
                userJson.addMetainfo(grabUserMarkedPhotosFromPersonalAlbums(targetId)); // grab user marked photos from all his albums (except his personal album)

                if (!userJson.isEmpty()) {
                    userJson.writeJson();
                    utils.logger("m Finished grab.");
                }

            } catch (OdklApiException a) {
                utils.logger("e Privacy error.");
            }
        }
//        else {
//            utils.logger("m User copy found.");
//        }
    }

    private Map<String, Object> grabUserPhotos(String userId) {
        OdklRequest request = api
                .createApiRequest("photos", "getPhotos")
                .addParam("fid", userId)
                .addParam("count", "100");
        Map<String, Object> result = recursiveGrabber(request, userId, null);
        if ((int) result.get("photosNum") != 0) {
            result.put("albumsNum", 1);
            return result;
        }
        return null;
    }

    private Map<String, Object> grabUserMarkedPhotos(String userId) {
        OdklRequest request = api
                .createApiRequest("photos", "getPhotos")
                .addParam("fid", userId)
                .addParam("aid", "tags")
                .addParam("count", "100");
        return recursiveGrabber(request, userId, null);
    }

    private Map<String, Object> appendMap(Map<String, Object> targetMap, Map<String, Object> tempMap) {
        int tempPhotosNum = (int) targetMap.get("photosNum");
        targetMap.remove("photosNum");
        targetMap.put("photosNum", (int) tempMap.get("photosNum") + tempPhotosNum);
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
            utils.logger("e Runtime error while loading profile. Connection failed.");
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
                JSONArray currentTags = getMarksWithUserFromPhoto(userId, photoId);
                if (currentTags != null) {
                    Map<String, Object> additionMap = new HashMap<String, Object>();
                    additionMap.put("photoOwner", photoOwnerId);
                    additionMap.put("tags", currentTags);
                    photoIdOwnerIdTagsMap.put(photoId, additionMap);
                }
                loadImage(imgUrl, userId, photoId);
            }
        }
        photoIdOwnerIdTagsMap.put("photosNum", photosNum);
        return photoIdOwnerIdTagsMap;
    }

//    private JSONObject getUserMarkFromPhoto(String userId, String photoId) {
//        JSONArray marksArray = getAllMarksFromPhoto(photoId);
//        if (marksArray != null && marksArray.size() != 0) {
//            for (int i = 0; i < marksArray.size(); i++) {
//                JSONObject tag = (JSONObject) marksArray.get(i);
//                if (tag.get("id").equals(userId)) {
//                    tag.put("marksNum", marksArray.size());
//                    return tag;
//                }
//            }
//        }
//        return null;
//    }

    private JSONArray getMarksWithUserFromPhoto(String userId, String photoId) {
        JSONArray marksArray = getAllMarksFromPhoto(photoId);
        boolean toReturn = false;
        if (marksArray != null) {
            int marksArraySize = marksArray.size();
            if (marksArraySize>0) {
                List<Integer> toRemove = new ArrayList<>();
                for (int i = 0; i < marksArraySize; i++) {
                    JSONObject tag = (JSONObject) marksArray.get(i);
                    if (((String) tag.get("id")).contains("-")) {
                        toRemove.add(i);
                        continue;
                    } else if (tag.get("user_id").equals(userId)) {
                        toReturn = true;
                    }
                    tag.remove("index");
                    tag.remove("id");
                    marksArray.set(i, tag);
                }
                if (toReturn) {
                    if (toRemove.size() > 0) {
                        for (int i:toRemove) marksArray.remove(i);
                    }
                    return marksArray;
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
            utils.logger(String.format("e Runtime error while loading marks from photo %s. Connection failed.", photoId));
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
            utils.logger("e Runtime error while loading user albums list. Connection failed.");
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
                        JSONArray tags = getMarksWithUserFromPhoto(userId, photoId);
                        if (tags != null) {
                            Map<String, Object> additionMap = new HashMap<String, Object>();
                            additionMap.put("photoOwner", userId);
                            additionMap.put("tags", tags);
                            photoIdOwnerIdTagsMap.put(photoId, additionMap);
                            utils.logger(String.format("m Found user mark on photo %s from album %s.", photoId, albumId));
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
            utils.logger("e Runtime error while loading photo list of the album. Connection failed.");
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
                utils.logger(String.format("e Image %s photoId %s load error.", imgUrl, photoId));
            }
        } else {
            utils.logger(String.format("m Photo %s already exists.", photoId));
            return false;
        }
        return true;
    }
}
