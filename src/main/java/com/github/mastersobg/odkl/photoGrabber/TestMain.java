package com.github.mastersobg.odkl.photoGrabber;

import com.github.mastersobg.odkl.OdklApi;
import com.github.mastersobg.odkl.auth.ApiConfig;

/**
 * Created by yuraf_000 on 25.09.2014.
 */
public class TestMain {

    public static void main(String[] args){
        OdklApi api = new OdklApi(ApiConfig.APP_ID, ApiConfig.APP_PUBLIC_KEY, ApiConfig.APP_SECRET_KEY,ApiConfig.ACCESS_TOKEN , ApiConfig.REFRESH_TOKEN);
        PhotoGrabber gr = new PhotoGrabber(api);
        gr.recursivePhotoGrabWithMetadata("489288866613",2);
     }
}
