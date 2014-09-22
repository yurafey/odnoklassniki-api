package com.github.mastersobg.odkl;

import com.github.mastersobg.odkl.OdklApi;
import com.github.mastersobg.odkl.OdklRequest;
import com.github.mastersobg.odkl.exception.OdklApiRuntimeException;
import com.github.mastersobg.odkl.model.Photo;
import com.github.mastersobg.odkl.util.JsonUtil;
import org.apache.commons.io.FileUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Ivan Gorbachev <gorbachev.ivan@gmail.com>
 */
public class PhotosApi {

    private final OdklApi api;

    PhotosApi(OdklApi api) {
        this.api = api;
    }

    public Photo getPhotoInfo(Long photoId) {
        if (photoId == null) {
            throw new IllegalArgumentException("photoId is null");
        }

        OdklRequest request = api
                .createApiRequest("photos", "getPhotoInfo")
                .addParam("photo_id", photoId.toString());

        String response = api.sendRequest(request);
        return new Photo(JsonUtil.parseObject(response));
    }

    // TODO авторизацию LIKE делаю, но error: 10 возвращается
    public boolean likeAlbum(Long albumId) {
        if (albumId == null) {
            throw new IllegalArgumentException("albumId is null");
        }

        OdklRequest request = api
                .createApiRequest("photos", "addAlbumLike")
                .addParam("aid", albumId.toString());

        return Boolean.valueOf(api.sendRequest(request));
    }

    public boolean likeGroupAlbum(Long albumId, String groupId) {
        throw new OdklApiRuntimeException("not implemented");
    }
}
