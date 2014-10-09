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
                default:
                    System.out.println(String.format("[MSG][%s][uid%s] %s",thread,targetId,message));
            }
        }
    }
    public boolean checkUser(String userId){
        File imageFile = new File(PhotoGrabberConfig.PHOTOS_DIR + userId + "/" + userId + PhotoGrabberConfig.JSON_EXT);
        if (!imageFile.exists()) {
            try {
                FileUtils.deleteDirectory(new File(PhotoGrabberConfig.PHOTOS_DIR + userId));
            } catch (IOException e) {
                //do nothing
            }
            return false;
        }
        return true;
    }
}
