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
import com.google.gson.Gson
import org.json.JSONObject
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
        if (remoteMessage.data!=null){
            checkNotification(remoteMessage.data)
        }
    }

    private fun checkNotification(messageBody: MutableMap<String, String>) {
        try {
            val gson = Gson()
            val obj = JSONObject(messageBody as Map<String, Any>)
            val data: NotificationPayload =
                gson.fromJson(obj.toString(), NotificationPayload::class.java)
            showNotification(data)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    private fun showNotification(data: NotificationPayload) {
        val intent = Intent(applicationContext, DashBoardActivity::class.java)
        intent.putExtra("NOTIFICATION_DATA", "$data")
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
                .setSmallIcon(R.drawable.iv_notification).setContentText(data.body)
                .setContentTitle(data.title).setAutoCancel(true)
                .setVibrate(longArrayOf(1000, 1000, 1000, 1000, 1000)).setOnlyAlertOnce(false)
                .setContentIntent(notifyPendingIntent)
        val a = generateRandom()
        val notificationManager =
            getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                channelId, "Notifications", NotificationManager.IMPORTANCE_HIGH
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