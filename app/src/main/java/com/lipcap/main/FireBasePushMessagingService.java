package com.lipcap.main;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.lipcap.R;
import com.lipcap.ui.customer.CustomerHome;
import com.lipcap.ui.provider.ProviderHome;
import com.lipcap.utils.AppConstants;
import com.lipcap.utils.PreferenceUtil;


public class FireBasePushMessagingService extends FirebaseMessagingService {


    public void onMessageReceived(RemoteMessage remoteMessage) {
        String pushDataStr = "";

        sendNotification("Test");
//        pushDataStr = remoteMessage.getData().get("message");
//
//        /*Clear old notification*/
//        ((NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE)).cancel(0);
//        if (PreferenceUtil.getBoolPreferenceValue(getContext(), AppConstants.LOGIN_STATUS) && pushDataStr != null && !pushDataStr.isEmpty()) {
//            sendChatNotification(pushDataStr);
//        }
    }


    private void sendNotification( String messageStr) {

        if (PreferenceUtil.getBoolPreferenceValue(this, AppConstants.LOGIN_STATUS)) {
            Intent intent = new Intent(this, PreferenceUtil.getBoolPreferenceValue(this, AppConstants.CURRENT_USER_IS_PROVIDER) ? ProviderHome.class : CustomerHome.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                    PendingIntent.FLAG_ONE_SHOT);

            String channelId = getString(R.string.default_notification_channel_id);
            Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            NotificationCompat.Builder notificationBuilder =
                    new NotificationCompat.Builder(this, channelId)
                            .setSmallIcon(R.mipmap.ic_launcher) //Small Icon from drawable
                            .setContentTitle(getString(R.string.app_name))
                            .setColor(ContextCompat.getColor(this, R.color.blue))
                            .setContentText(messageStr)
                            .setAutoCancel(true)
                            .setWhen(System.currentTimeMillis())
                            .setSound(defaultSoundUri)
                            .setStyle(new NotificationCompat.BigTextStyle().bigText(messageStr))
                            .setDefaults(Notification.DEFAULT_ALL)
                            .setVibrate(new long[]{0, 100, 200, 300})
                            .setPriority(Notification.PRIORITY_HIGH)
                            .setContentIntent(pendingIntent);

            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            // Since android Oreo notification channel is needed.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(channelId,
                        getString(R.string.app_name),
                        NotificationManager.IMPORTANCE_DEFAULT);
                if (notificationManager != null)
                    notificationManager.createNotificationChannel(channel);
            }

            if (notificationManager != null)
                notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
        }
    }


    @Override
    public void onNewToken(String pushIdStr) {
        super.onNewToken(pushIdStr);
        PreferenceUtil.storeStringPreferenceValue(this,AppConstants.PUSH_DEVICE_ID,pushIdStr);
    }
}
