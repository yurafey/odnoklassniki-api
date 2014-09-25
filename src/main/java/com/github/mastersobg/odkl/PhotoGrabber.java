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
            grabPhotos(String.valueOf(targetId));
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

    public boolean grabPhotos(String userId) {
        OdklRequest request = api
                .createApiRequest("photos", "getPhotos")
                .addParam("fid", userId);

        String response = api.sendRequest(request);

        JSONObject responseJson = JsonUtil.parseObject(response);
        JSONArray photos = JsonUtil.getArray(responseJson, "photos");
        for (int i = 0; i < photos.size(); i++) {
            JSONObject photo = (JSONObject) photos.get(i);
            String link = JsonUtil.getString(photo, "pic640x480");
            String photoId = (String) photo.get("id");

            File imageFile = new File(PHOTOS_DIR + userId + "/" + photoId + PHOTOS_EXT);
            if (!imageFile.exists()) {
                try {
                    FileUtils.copyURLToFile(new URL(link), imageFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return true;
    }

}
