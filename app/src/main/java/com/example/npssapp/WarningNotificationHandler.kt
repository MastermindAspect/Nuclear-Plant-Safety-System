package com.example.npssapp

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
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
                val newChildNodeUid = snapshot.child("uid").value.toString()

                if (newChildNodeUid == uId.toString()) {
                    var totalRadiationExposure = 0.0

                    while(totalRadiationExposure < 500000) {
                        Thread.sleep(1_000)

                        //TODO("Get current values of these three variables for every loop")
                        var reactorRadiation = 30 //default value 30, can be change between 1-100 on console
                        var roomCoefficient = 1.6 //changes depending on what room user is in
                        var protectiveCoefficient = 5 //changes depending on what clothing/equipment user is wearing

                        totalRadiationExposure += radiationPerSecond(reactorRadiation, roomCoefficient, protectiveCoefficient)
                        Log.i("Radiation status",
                            "User radiation exposure is currently: $totalRadiationExposure"
                        )
                    }
                    sendRadiationNotification()
                    wakePhoneScreen()
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

    fun radiationPerSecond(reactorRadiation: Int, roomCoefficient: Double, protectiveCoefficient: Int): Double {
        return reactorRadiation * roomCoefficient / protectiveCoefficient
    }

    fun sendRadiationNotification() {
        //intent which determines where user ends up when pressing notification
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, intent, 0)

        val builder = NotificationCompat.Builder(context, 5.toString())
            .setSmallIcon(R.drawable.ic_stat_warning)
            .setContentTitle("Radiation limit reached.")
            .setContentText("WARNING: You have reached the radiation limit.")
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
        val screenOn = pm.isInteractive

        if (!screenOn) {
                val wl = pm.newWakeLock(
                    PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
                    "app:radiationWarning"
                )
                wl.acquire(5000)
            }
    }
}