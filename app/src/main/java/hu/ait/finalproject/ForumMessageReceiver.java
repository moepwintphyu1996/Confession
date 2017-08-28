package hu.ait.finalproject;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;


public class ForumMessageReceiver extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        String from = remoteMessage.getFrom();
        String payload = "";
        if (remoteMessage.getData().size() > 0) {
            payload += remoteMessage.getData();
        }
        String body = remoteMessage.getNotification().getBody();

        Log.d(getString(R.string.Log), from+"\n"+payload+"\n"+body);
    }
}