package com.github.mastersobg.odkl.photoGrabber;

import org.apache.commons.io.FileUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * Created by yuraf_000 on 27.09.2014.
 */
public class JsonUser {
    private String uid = new String();
    private JSONArray friends = new JSONArray();
    private int photosNum = 0;
    private int albumsNum = 0;
    private JSONArray markedPhotos = new JSONArray();
    private final static String PHOTOS_DIR = PhotoGrabberConfig.PHOTOS_DIR;
    private final static String JSON_EXT = PhotoGrabberConfig.JSON_EXT;

    public JsonUser(String userId) {
        uid = userId;
    }

    public void setFriends(JSONArray friends) {
        if (friends != null) this.friends = friends;
    }

    public void addPhotosNum(int photosNum) {
        this.photosNum += photosNum;
    }

    public void addAlbumsNum(int albumsNum) {
        this.albumsNum += albumsNum;
    }

    public void addMarkedPhoto(String photoId, String photoOwner, int marksNum, Long x, Long y) {
        JSONObject addition = new JSONObject();
        addition.put("photoId", photoId);
        addition.put("photoOwner", photoOwner);
        addition.put("marksNum", marksNum);
        addition.put("x", x);
        addition.put("y", y);
        markedPhotos.add(addition);
    }

    public void addMetainfo(Map<String, Object> addition) {
        if (addition != null && addition.size() != 0) {
            for (String key : addition.keySet()) {
                switch (key) {
                    case "albumsNum":
                        addAlbumsNum((int) addition.get(key));
                        break;
                    case "photosNum":
                        addPhotosNum((int) addition.get(key));
                        break;
                    default:
                        Map<String, Object> metaMap = (Map<String, Object>) addition.get(key);
                        JSONObject userMarkFromPhoto = (JSONObject) metaMap.get("tag");
                        addMarkedPhoto(key, (String) metaMap.get("photoOwner"), (int) userMarkFromPhoto.get("marksNum"), (Long) userMarkFromPhoto.get("x"), (Long) userMarkFromPhoto.get("y"));
                }
            }
        }
    }

    public void writeJson() {
        JSONObject writeResult = new JSONObject();
        writeResult.put("uid", uid);
        writeResult.put("friends", friends);
        writeResult.put("albumsNum", albumsNum);
        writeResult.put("photosNum", photosNum);
        writeResult.put("markedPhotos", markedPhotos);
        File jsonFile = new File(PHOTOS_DIR + uid + "/" + uid + JSON_EXT);
        try {
            FileUtils.writeStringToFile(jsonFile, writeResult.toJSONString());
        } catch (IOException e) {
            System.out.println(String.format("[ERR][uid%s] Json write error. IOException.",uid));
        }
    }

    public boolean isEmpty() {
        if (friends.isEmpty() && markedPhotos.isEmpty()) return true;
        return false;
    }
}
