package com.github.mastersobg.odkl.photoGrabber;

import com.github.mastersobg.odkl.OdklApi;

import java.util.List;

public class PhotoGrabber {
    private final static String PHOTOS_DIR = PhotoGrabberConfig.PHOTOS_DIR;
    private final static String PHOTOS_EXT = PhotoGrabberConfig.PHOTOS_EXT;
    private final static boolean LOGS = PhotoGrabberConfig.LOGS;
    private final OdklApi api;

    public PhotoGrabber(OdklApi api) {
        this.api = api;
    }

    public List<String> getFriendsList(String targetId, Integer friendsDepthLevel) {
        System.out.println("target:" + targetId + " friendsDepth:" + friendsDepthLevel);
        List<String> friendsList = api.friends().getFriends(targetId);
        if ((friendsDepthLevel > 0) && (friendsList != null)) {
            for (int i = 0; i < friendsList.size(); i++) {
                List<String> tmp = getFriendsList(friendsList.get(i), friendsDepthLevel - 1);
                if (tmp != null) {
                    friendsList.addAll(tmp);
                }
            }
        }
        return friendsList;
    }

}
