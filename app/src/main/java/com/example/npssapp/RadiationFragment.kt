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
        var reactorRadiation:Int = 30
        var roomCoefficient = arrayOf(0.1,0.5,1.6)
        var roomIndex = 0
        private var protectiveCoefficient : Int = 1
        private const val maxExposure : Int = 500000
        var isWearingHazmat : Boolean = false
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
        protectiveCoefficient = if (isWearingHazmat) 5
        else 1
        val estimatedTimeRemain = maxExposure / this.radiationPerSecond()
        return estimatedTimeRemain.toLong()
    }

    fun radiationPerSecond(): Double {
        return reactorRadiation * roomCoefficient[roomIndex] / protectiveCoefficient
    }

    fun startTimeCounter() {
        if (estimatedTimeRemaining() <= 0L){
            estimatedTimeRemainingText.text = "Leave the area!"
        }
        else {
            val countTime = estimatedTimeRemainingText
            var secondsPassed : Long = 0
            val q = LongArray(3)
            var tempEstimated : Long = 0
            object : CountDownTimer((estimatedTimeRemaining()*1000)-secondsPassed*1000, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    var counter = estimatedTimeRemaining() - secondsPassed
                    if (tempEstimated != estimatedTimeRemaining()){
                        q[0] = counter/2 + counter/4
                        q[1] = counter/2
                        q[2] = counter/2 - counter/4
                        tempEstimated = estimatedTimeRemaining()
                    }
                    if (!timerRunning) {
                        countTime.text = " "
                        this.cancel()
                    }

                    countTime.text = "Reaching maximum exposure in: $counter"
                    counter--
                    secondsPassed++
                    try {
                        MainActivity.mBluetoothContext!!.sendCommand("t:${counter.toInt()}")
                    }
                    catch (e: KotlinNullPointerException){
                        Toast.makeText(activity, "Error! Could not send message over Bluetooth!", Toast.LENGTH_SHORT).show()
                    }

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
            }
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
            }
            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
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