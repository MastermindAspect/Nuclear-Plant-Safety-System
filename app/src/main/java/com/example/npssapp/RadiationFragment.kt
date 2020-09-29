package com.example.npssapp

import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.postDelayed
import androidx.fragment.app.Fragment
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_radiation.*

class RadiationFragment : Fragment() {
    companion object{
        private var reactorRadiation:Int = 99000
        private var roomCoefficient : Double = 1.6
        private var protectiveCoefficient : Int = 5
        private const val maxExposure : Int = 500000
        private var totalRadiationExposure: Double = 0.0
        var timerRunning : Boolean = false
        private var runnable : Runnable = Runnable {}
        val handler = Handler()
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        runnable = Runnable{
            listenForClockInEntry(MainActivity.currentUId)
            handler.postDelayed(runnable,2000)
        }
        handler.postDelayed(runnable,0)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_radiation, container, false)
    }


    fun estimatedTimeRemaining() : Long {
        val estimatedTimeRemain = maxExposure / this.radiationPerSecond()
        return estimatedTimeRemain.toLong()
    }

    fun radiationPerSecond(): Double {
        return reactorRadiation * roomCoefficient / protectiveCoefficient
    }

    fun startTimeCounter() {
        if (estimatedTimeRemaining() <= 0L){
            estimatedTimeRemainingText.text = "Leave the area!"
        }
        else {
            val countTime = estimatedTimeRemainingText
            var counter = estimatedTimeRemaining()
            val q = LongArray(3)
            q[0] = counter/2 + counter/4
            q[1] = counter/2
            q[2] = counter/2 - counter/4
            object : CountDownTimer(estimatedTimeRemaining()*1000, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    if (!timerRunning) {
                        this.cancel()
                    }
                    countTime.text = "Reaching maximum exposure in: $counter"
                    counter--
                    if (counter in q){
                        try {
                            MainActivity.notificationHandler!!.sendRadiationNotification()
                            MainActivity.notificationHandler!!.wakePhoneScreen()
                            try {
                                MainActivity.mBluetoothContext!!.sendCommand("n")
                            }
                            catch (e: KotlinNullPointerException){
                                Toast.makeText(activity, "Error! Could not send message over Bluetooth!", Toast.LENGTH_SHORT).show()
                            }
                        }
                        catch (e: KotlinNullPointerException){
                            Toast.makeText(activity, "Error! Could not send notification!", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                override fun onFinish() {
                    try {
                        MainActivity.notificationHandler!!.sendRadiationNotification()
                        MainActivity.notificationHandler!!.wakePhoneScreen()
                        try {
                            MainActivity.mBluetoothContext!!.sendCommand("r")
                            countTime.text = "Leave the area, NOW!"
                        }
                        catch (e: KotlinNullPointerException){
                            Toast.makeText(activity, "Error! Could not send message over Bluetooth!", Toast.LENGTH_SHORT).show()
                        }
                    }
                    catch (e: KotlinNullPointerException){
                        Toast.makeText(activity, "Error! Could not send notification!", Toast.LENGTH_SHORT).show()
                    }
                }
            }.start()
        }
    }
    fun listenForClockInEntry(uId: String) {
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
                if (newChildNodeUid == uId && !timerRunning) {
                    timerRunning = true
                    startTimeCounter()
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


}