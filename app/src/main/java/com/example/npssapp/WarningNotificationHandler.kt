package com.example.npssapp

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.io.Serializable
import java.util.*
import kotlin.concurrent.schedule


data class WarningNotificationHandler(val uid: Int, val context: Context) : Serializable {

    fun listenForClockInEntry(uId: Int) {
        val databaseOnlineRef = Firebase.database.reference.child("online")

        databaseOnlineRef.addChildEventListener(object : ChildEventListener {
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                TODO("Not yet implemented")
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                TODO("Not yet implemented")
            }
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {

                if (snapshot.child("uid").value.toString() == uId.toString()) {
                    val timer = Timer("schedule", true)

                    //TODO("Delay until radiation limit is almost reached (in milliseconds)")
                    timer.schedule(3000) {
                        sendRadiationNotification()
                        wakePhoneScreen()
                    }
                }
            }
            override fun onChildRemoved(snapshot: DataSnapshot) {
                Log.i(
                    "Child removed",
                    "'Online' child node removed with uid: " + snapshot.child("uid").value.toString()
                )
            }
        })
    }

    fun sendRadiationNotification() {
        //intent which determines where user ends up when pressing notification
        var intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, intent, 0)

        var builder = NotificationCompat.Builder(context, 5.toString())
            .setSmallIcon(R.drawable.ic_stat_warning)
            .setContentTitle("Radiation limit almost reached.")
            .setContentText("WARNING: You are about to reach the radiation limit.")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            // notificationId is a unique int for each notification that you must define
            notify(123, builder.build())
        }
    }

    fun wakePhoneScreen() {
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val screenOn =
            if (Build.VERSION.SDK_INT >= 20) {
                pm.isInteractive
            } else {
                pm.isScreenOn
            }

            if (!screenOn) {
                val wl = pm.newWakeLock(
                    PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
                    "app:radiationWarning"
                )
                wl.acquire(5000)
            }
    }
}