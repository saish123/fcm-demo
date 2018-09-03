package com.sawant.fcm;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String GROUP_KEY_SAWANT = "GROUP_SAWANT";

    @Override
    public void onNewToken(String s) {
        Log.e("NEW_TOKEN", s);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        /**
         * Notification payload
         */
        String body = remoteMessage.getNotification().getBody();
        String data = remoteMessage.getNotification().getTitle();
        String imageUrl = remoteMessage.getNotification().getIcon();


        Map<String, String> params = remoteMessage.getData();
        JSONObject object = new JSONObject(params);
        Log.e("JSON_OBJECT", object.toString());

        String NOTIFICATION_CHANNEL_ID = "My_channel";

        long pattern[] = {0, 1000, 500, 1000};

        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "Your Notifications",
                    NotificationManager.IMPORTANCE_HIGH);

            notificationChannel.setDescription("");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setVibrationPattern(pattern);
            notificationChannel.enableVibration(true);
            mNotificationManager.createNotificationChannel(notificationChannel);
        }

        // to diaplay notification in DND Mode
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = mNotificationManager.getNotificationChannel(NOTIFICATION_CHANNEL_ID);
            channel.canBypassDnd();
        }

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);

        String imageUri = null;
        if (remoteMessage.getData().get("image") != null) {
            imageUri = remoteMessage.getData().get("image");
        } else {
            imageUri = imageUrl;
        }

        String message = remoteMessage.getData().get("message");


        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Bitmap remote_picture = getBitmapfromUrl(imageUri);


        try {
            remote_picture = BitmapFactory.decodeStream((InputStream) new URL(imageUri).getContent());
        } catch (IOException e) {
            e.printStackTrace();
        }

        notificationBuilder.setAutoCancel(true)
                .setColor(ContextCompat.getColor(this, R.color.colorAccent))
                .setContentTitle(getString(R.string.app_name))
                .setContentText(message)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setLargeIcon(remote_picture)
                .setGroup(GROUP_KEY_SAWANT)
                .setGroupSummary(true)
                .setStyle(getStyleForNotification(message))
/*                .setStyle(new NotificationCompat.BigPictureStyle()
                        .bigPicture(remote_picture))*//*Notification with Image*/
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);


        Random random = new Random();
        int randomInt = random.nextInt(12345);

        SharedPreferences sharedPreferences = getSharedPreferences("NotificationData", 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(String.valueOf(new Random(randomInt)), message);
        editor.apply();

        mNotificationManager.notify(12345, notificationBuilder.build());
    }

    /**
     *
     */

    private NotificationCompat.InboxStyle getStyleForNotification(String messageBody) {
        NotificationCompat.InboxStyle inbox = new NotificationCompat.InboxStyle();
        SharedPreferences sharedPref = getSharedPreferences("NotificationData", 0);
        Map<String, String> notificationMessages = (Map<String, String>) sharedPref.getAll();
        Map<String, String> myNewHashMap = new HashMap<>();
        for (Map.Entry<String, String> entry : notificationMessages.entrySet()) {
            myNewHashMap.put(entry.getKey(), entry.getValue());
        }
        inbox.addLine(messageBody);
        for (Map.Entry<String, String> message : myNewHashMap.entrySet()) {
            inbox.addLine(message.getValue());
        }
        inbox.setBigContentTitle(this.getResources().getString(R.string.app_name))
                .setSummaryText("Tap to open");
        return inbox;
    }


    /**
     * End
     */


    /*
     *To get a Bitmap image from the URL received
     * */
    public Bitmap getBitmapfromUrl(String imageUrl) {
        try {
            URL url = new URL(imageUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap bitmap = BitmapFactory.decodeStream(input);
            return bitmap;

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;

        }
    }
}