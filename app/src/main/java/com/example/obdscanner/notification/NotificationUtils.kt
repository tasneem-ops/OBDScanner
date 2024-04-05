package com.example.obdscanner.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.obdscanner.MainActivity
import com.example.obdscanner.R
import com.example.obdscanner.ble.BluetoothLeService

const val CHANNEL_ID = "CHANNEL_ID"
const val NOTIFICATION_ID = 84686
const val DATA_NOTIFICATION_ID = 8752

private fun createNotificationChannel(context: Context) {
    // Create the NotificationChannel, but only on API 26+ because
    // the NotificationChannel class is new and not in the support library
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val name = context.getString(R.string.channel_name)
        val description: String = context.getString(R.string.channel_description)
        val importance : Int = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(CHANNEL_ID, name, importance)
        channel.description = description
        // Register the channel with the system; you can't change the importance
        // or other notification behaviors after this
        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }
}
fun getForegroundNotification(context: Context) : NotificationCompat.Builder{
    val intent = Intent(context, MainActivity::class.java)
    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
    val pendingIntent = PendingIntent.getActivity(context, 0, intent,
        PendingIntent.FLAG_MUTABLE)
    val serviceIntent = Intent(context, BluetoothLeService::class.java)
    serviceIntent.setAction("STOP")
    val stopIntent = PendingIntent.getService(context, 1, serviceIntent,
        PendingIntent.FLAG_IMMUTABLE)

    val notificationManager = context.getSystemService(NotificationManager::class.java)
    // some code goes here
    val builder: NotificationCompat.Builder =
        NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.connection)
            .setContentTitle("OBD Scanner")
            .setContentText("Connecting to OBD")
            .setOngoing(true)
            .setAutoCancel(false)
            .addAction(R.drawable.connection, "Stop", stopIntent)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
    createNotificationChannel(context)
    return  builder
}
fun sendDataNotification(context: Context, data : String) : Notification {
    val intent = Intent(context, MainActivity::class.java)
    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
    val pendingIntent = PendingIntent.getActivity(context, 0, intent,
        PendingIntent.FLAG_IMMUTABLE)

    val notificationManager = context.getSystemService(NotificationManager::class.java)
    // some code goes here
    val builder: NotificationCompat.Builder =
        NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.connection)
            .setContentTitle("OBD Scanner")
            .setContentText("Data Received From OBD: $data")
            .setAutoCancel(false)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
    createNotificationChannel(context)
    notificationManager.notify(DATA_NOTIFICATION_ID, builder.build())
    return  builder.build()
}
