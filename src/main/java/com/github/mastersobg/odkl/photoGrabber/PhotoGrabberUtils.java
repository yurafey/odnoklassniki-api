package com.github.mastersobg.odkl.photoGrabber;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

/**
 * Created by yuraf_000 on 09.10.2014.
 */
public class PhotoGrabberUtils {
    private String threadId = null;
    private String targetId = null;

    public PhotoGrabberUtils(String threadId,String userId) {
        this.threadId = threadId;
        this.targetId = userId;
    }

    public void logger(String message) {
        if (PhotoGrabberConfig.LOGS) {
            int separatorIndex = message.indexOf(' ');
            String logMode = message.substring(0, separatorIndex);
            message = message.substring(separatorIndex+1);
            String thread = threadId.substring(threadId.lastIndexOf('-')+1);
            switch (logMode) {
                case "e":
                    System.out.println(String.format("[ERR][%s][uid%s] %s",thread,targetId,message));
                    break;
                case "m":
                    System.out.println(String.format("[MSG][%s][uid%s] %s",thread,targetId,message));
                    break;
                case "bt":
                    System.out.println(String.format("[MSG][%s][building_friends_tree] %s",thread,message));
                    break;
                case "f":
                    System.out.println(String.format("[MSG][%s][checking] %s",thread,message));
                    break;
                default:
                    System.out.println(String.format("[MSG][%s] %s",thread,message));
            }
        }
    }
    public boolean checkUser(String userId){
        File jsonFile = new File(PhotoGrabberConfig.PHOTOS_DIR + userId + "/" + userId + PhotoGrabberConfig.JSON_EXT);
        if (!jsonFile.exists()) {
            try {
                File userDir = new File(PhotoGrabberConfig.PHOTOS_DIR + userId);
                if (userDir.exists()) {
                    FileUtils.deleteDirectory(userDir);
                    logger("m Found directory without json. Reloading user.");
                }
            } catch (IOException e) {
                logger(String.format("e IO Exception on checking user %s directory.",userId));
            }
            return false;
        }
        return true;
    }
}
