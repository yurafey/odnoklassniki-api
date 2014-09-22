package com.github.mastersobg.odkl;

import com.github.mastersobg.odkl.util.JsonUtil;
import org.apache.commons.io.FileUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.URL;

public class PhotoGrabber {
    private final static String PHOTOS_DIR = "/Users/den/base/";
    private final static String PHOTOS_EXT = ".jpg";

    private final OdklApi api;

    public PhotoGrabber(OdklApi api) {
        this.api = api;
    }

    public void grab(String userId, int level) {
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
