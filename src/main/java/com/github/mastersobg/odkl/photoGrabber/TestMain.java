package com.github.mastersobg.odkl.photoGrabber;

import com.github.mastersobg.odkl.OdklApi;
import com.github.mastersobg.odkl.auth.ApiConfig;

import java.util.List;

/**
 * Created by yuraf_000 on 25.09.2014.
 */
public class TestMain {

    public static void main(String[] args){
        OdklApi api = new OdklApi(ApiConfig.APP_ID, ApiConfig.APP_PUBLIC_KEY, ApiConfig.APP_SECRET_KEY,ApiConfig.ACCESS_TOKEN , ApiConfig.REFRESH_TOKEN);
        PhotoGrabber gr = new PhotoGrabber(api);
        //574881296141 - Martin (2 friends)
        //559653274945 - Rudolf (4 friends)
        //gr.grabUserPhotosWithMetadata("563733877946"); //grub one user with metadata
        //gr.recursivePhotoGrabWithMetadata("211410835183",4); // grub user and his friends with metadata
        List<String> friends = gr.getFriendsList("561861978257",1);
        System.out.println("friends size: "+friends.size());
        //System.out.println(friends);
     }
}
