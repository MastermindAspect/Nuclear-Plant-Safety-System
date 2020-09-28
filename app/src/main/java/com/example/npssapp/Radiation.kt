package com.example.npssapp

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_radiation.*
import java.time.LocalDateTime

class Radiation : AppCompatActivity() {

    companion object{
        var reactorRadiation:Int = 30
        var roomCoefficient : Double = 1.6
        var protectiveCoefficient : Int = 5
        private val maxExposure : Int = 500000
        private var totalRadiationExposure: Double = 0.0
        private var estimatedTimeLeft : Long = 0

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_radiation)
        MainActivity.currentUId?.let { listenForClockInEntry(it) }

    }

    private fun radiationExposure(){
        while(totalRadiationExposure < maxExposure) {
            Thread.sleep(1000)
            totalRadiationExposure += radiationPerSecond()
            Log.i("Radiation status",
                "User radiation exposure is currently: $totalRadiationExposure"
            )
            estimatedTimeRemaining.text = estimatedTimeRemaining().toString()
        }
        MainActivity.notificationHandler!!.sendRadiationNotification()
        MainActivity.notificationHandler!!.wakePhoneScreen()
    }

    private fun estimatedTimeRemaining() : LocalDateTime {
        estimatedTimeLeft = (maxExposure / totalRadiationExposure).toLong()
        val currentDateTime = LocalDateTime.now()
        return currentDateTime.plusSeconds(estimatedTimeLeft)
    }

    private fun radiationPerSecond(): Double {
        return reactorRadiation * roomCoefficient / protectiveCoefficient
    }
    private fun listenForClockInEntry(uId: String) {
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
                if (newChildNodeUid == uId) {
                    radiationExposure()
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
    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            R.id.connect_safety_console -> {
                if (!Bluetooth.mIsConnected){
                    MainActivity.mProgress = ProgressDialog.show(this, "Connecting...", "please wait")
                    MainActivity.mBluetoothContext = Bluetooth(this)
                    MainActivity.mBluetoothContext!!.start()
                } else {
                    MainActivity.mBluetoothContext?.disconnect()
                    Toast.makeText(this, "Disconnected from Bluetooth!", Toast.LENGTH_SHORT).show()
                }
            }
            R.id.home -> {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
            R.id.radiation -> {
                val intent = Intent(this, Radiation::class.java)
                startActivity(intent)
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}