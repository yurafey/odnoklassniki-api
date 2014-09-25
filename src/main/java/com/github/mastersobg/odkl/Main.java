package com.github.mastersobg.odkl;

import com.github.mastersobg.odkl.auth.ApiConfig;
import org.json.simple.JSONArray;

/**
 * Created by yuraf_000 on 25.09.2014.
 */
public class Main {

    public static void main(String[] args){
        OdklApi api = new OdklApi(ApiConfig.APP_ID, ApiConfig.APP_PUBLIC_KEY, ApiConfig.APP_SECRET_KEY,ApiConfig.ACCESS_TOKEN , ApiConfig.REFRESH_TOKEN);
        PhotoGrabber gr = new PhotoGrabber(api);
        //574881296141 - Martin (2 friends)
        //559653274945 - Rudolf (4 friends)
        //gr.recursiveGrab("559653274945", 3);
        JSONArray j = gr.getTags("563026980282");

    }
}
