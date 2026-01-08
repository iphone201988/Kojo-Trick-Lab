package com.tech.kojo.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.tech.kojo.R
import com.tech.kojo.ui.dashboard.DashBoardActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.util.Random

class MyFirebaseMessagingServiceClass : FirebaseMessagingService() {
    var title = ""
    var body = ""
    override fun onNewToken(token: String) {
        Log.d("token", "Refreshed token: $token")
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        //  ImageUtils.checkNotification = 1
        if (remoteMessage.notification != null) {
            showNotification(remoteMessage.notification)
            Log.d("dgffgfgdfgggff", "onMessageReceived: ")
        } else if (remoteMessage.notification != null) {
            //  checkNotification(remoteMessage.notification?.body)
            showNotification(remoteMessage.notification)
            Log.d("dgffgfgdfgggff", "onMessageReceived: ")
        } else {
            Log.d("MyFirebase", "none_data")
        }
    }

    private fun showNotification(message: RemoteMessage.Notification?) {
        val intent = Intent(applicationContext, DashBoardActivity::class.java)
        val channelId = "notification_channel"
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val notifyPendingIntent = PendingIntent.getActivity(
            this,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val builder: NotificationCompat.Builder =
            NotificationCompat.Builder(applicationContext, channelId).setChannelId(channelId)
                .setSmallIcon(R.drawable.iv_notification).setContentText(message?.body)
                .setContentTitle(message?.title).setAutoCancel(true)
                .setVibrate(longArrayOf(1000, 1000, 1000, 1000, 1000)).setOnlyAlertOnce(false)
                .setContentIntent(notifyPendingIntent)
        val a = generateRandom()
        val notificationManager =
            getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                channelId, "web_app", NotificationManager.IMPORTANCE_HIGH
            )
            notificationChannel.setShowBadge(true)
            notificationManager.createNotificationChannel(notificationChannel)
        }
        notificationManager.notify(a, builder.build())
    }

    private fun generateRandom(): Int {
        val random = Random()
        return random.nextInt(9999 - 1000) + 1000

    }


}