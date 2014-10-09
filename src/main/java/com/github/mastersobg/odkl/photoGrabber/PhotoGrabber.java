package com.github.mastersobg.odkl.photoGrabber;

import com.github.mastersobg.odkl.OdklApi;
import com.github.mastersobg.odkl.exception.OdklApiException;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class PhotoGrabber {
    private final static int THREADS_NUM = PhotoGrabberConfig.THREADS_NUM;
    private final OdklApi api;
    private PhotoGrabberUtils utils = new PhotoGrabberUtils("-main", "");

    public PhotoGrabber(OdklApi api) {
        this.api = api;
    }

    private LinkedHashSet<String> getFriendsSet(String targetId, Integer friendsDepthLevel) {
        List<String> friendsList = getFriendsList(targetId, friendsDepthLevel);
        LinkedHashSet<String> friendsSet = new LinkedHashSet<>();
        friendsSet.addAll(friendsList);
        friendsSet.add(targetId);
        return friendsSet;
    }

    private List<String> getFriendsList(String targetId, Integer friendsDepthLevel) {
        try {
            List<String> friendsList = api.friends().getFriends(targetId);
            if ((friendsDepthLevel > 0) && (friendsList != null)) {
                int size = friendsList.size();
                for (int i = 0; i < size; i++) {
                    List<String> tmp = getFriendsList(friendsList.get(i), friendsDepthLevel - 1);
                    if (tmp != null) {
                        friendsList.addAll(tmp);
                    }
                }
            }
            return friendsList;
        } catch (OdklApiException a) {
            utils.logger(String.format("e User %s privacy error.", targetId));
            return null;
        }
    }

    private void checkAndFixAllUsers(LinkedHashSet<String> friendsSet) {
        utils.logger("m Starting grabbed users check.");
        Iterator<String> itr = friendsSet.iterator();
        LinkedHashSet<String> crashedUsersId = new LinkedHashSet<>();
        while (itr.hasNext()) {
            String userId = itr.next();
            if (!utils.checkUser(userId)) {
                crashedUsersId.add(userId);
            }
        }
        if (!crashedUsersId.isEmpty()) {
            Iterator<String> crashedItr = crashedUsersId.iterator();
            ExecutorService service = Executors.newFixedThreadPool(THREADS_NUM);
            while (crashedItr.hasNext()) {
                service.execute(new PhotoGrabberThread(api, crashedItr.next()));
            }
            service.shutdown();
            try {
                service.awaitTermination(99999, TimeUnit.DAYS);
            } catch (InterruptedException e) {
                //
            }
        }
        utils.logger("m Finished grabbed users check.");
    }

    public void recursivePhotoGrabWithMetadata(String targetId, Integer friendsDepthLevel) {
        utils.logger(String.format("m Getting friends tree from user %s with depth level %s", targetId, friendsDepthLevel));
        LinkedHashSet<String> friendsSet = getFriendsSet(targetId, friendsDepthLevel);
        Iterator<String> itr = friendsSet.iterator();
        utils.logger(String.format("m Found %s users to load.", friendsSet.size()));
        ExecutorService service = Executors.newFixedThreadPool(THREADS_NUM);
        while (itr.hasNext()) {
            service.execute(new PhotoGrabberThread(api, itr.next()));
        }
        service.shutdown();
        try {
            if (service.awaitTermination(99999, TimeUnit.DAYS)) {
                checkAndFixAllUsers(friendsSet);
            }
        } catch (InterruptedException e) {
            //
        }
        utils.logger("m Finished grabbing.");
    }
}
