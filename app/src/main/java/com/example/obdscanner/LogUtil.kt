package com.example.obdscanner

import android.content.Context
import android.provider.Settings
import com.example.obdscanner.model.dto.LogData
import com.google.firebase.Firebase
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.database
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun logData(context : Context, tag : String, msg : String){
    val database: DatabaseReference= Firebase.database.reference
    val deviceID  = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    val timeStamp : String = SimpleDateFormat("dd-MM-yy HH:mm:ss", Locale.US).format(Date())
    database.child("devices")
        .child(deviceID).child(timeStamp).setValue(
            LogData(deviceID, tag,
            msg, timeStamp))
}