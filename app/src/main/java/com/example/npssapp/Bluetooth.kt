package com.example.npssapp

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.os.AsyncTask
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import com.example.npssapp.MainActivity.Companion.mProgress
import java.io.IOException
import java.lang.Integer.max
import java.lang.Integer.min
import java.lang.NumberFormatException
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException


class Bluetooth(context: Context) : Thread() {

    companion object {
        var mBluetoothSocket : BluetoothSocket? = null
        lateinit var mBluetoothAdapter : BluetoothAdapter
        var mIsConnected : Boolean = false
        const val mAddress : String ="98:D3:41:F9:76:43"
        val mUUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        var c : Context? = null
    }

    init {
        c = context
    }

    fun getConnected() : Boolean{
        return mIsConnected
    }

    override fun run() {
        try {
            ConnectToDevice().execute().get(8000, TimeUnit.MILLISECONDS)
        }
        catch (e: TimeoutException){
            backgroundToast(c, "Bluetooth connection timed out!")
            mProgress.dismiss()
        }
        while (!currentThread().isInterrupted && mBluetoothSocket != null && mIsConnected) {
            retrieveData(mBluetoothSocket!!)
            isClockedIn("asd123") {
                Log.d("TESTTT", it.toString())
            }
        }

    }
    fun sendCommand(input: String){
        if (mBluetoothSocket != null) {
            try{
                mBluetoothSocket!!.outputStream.write(input.toByteArray())
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        mBluetoothSocket!!.outputStream.flush()
    }
    private fun retrieveData(socket: BluetoothSocket){
        val inputStream = socket.inputStream
        try {
            val available = inputStream.available()
            var bytes = ByteArray(available)
            inputStream.read(bytes, 0, available)
            val message = String(bytes)
            val arr = message.split(":").toTypedArray()
            if (message.length > 2){
                Log.d("Oscar", "${arr[0]} ${arr[1]}")
                when (arr[0]){
                    "u" -> {
                        if (arr[1].length == 8) {
                            isClockedIn(arr[1]) {
                                if (it) {
                                    clockOutEmployee(arr[1])
                                    sendCommand("o")
                                } else {
                                    clockInEmployee(arr[1])
                                    MainActivity.currentUId = arr[1]
                                    MainActivity.notificationHandler = WarningNotificationHandler(
                                        arr[1],
                                        c!!
                                    )
                                    sendCommand("i")
                                }
                            }
                        }
                    }
                    "r" -> {
                        // Log.d("Oscar", "${arr[1]}")
                        try{
                            RadiationFragment.reactorRadiation = min(100, max(1, arr[1].toInt()))
                        }
                        catch (e: NumberFormatException){
                            Log.e("Error", "Could not format string!")
                        }
                    }
                    "s" -> {
                        // Log.d("Oscar", "${arr[1]}")
                        RadiationFragment.isWearingHazmat = arr[1] == "true"
                    }
                    "y" -> {
                        try{
                            RadiationFragment.roomIndex++
                            if(RadiationFragment.roomIndex > 2){
                                RadiationFragment.roomIndex = 0
                            }
                            sendCommand("${RadiationFragment.roomIndex+1}")
                        }
                        catch (e: NumberFormatException){
                            Log.e("Error", "Could not format string!")
                        }
                    }
                }
            }
        } catch (e: IOException) {
            Log.e("client", "Cannot read data", e)
        }
    }

    fun disconnect(){
        if (mBluetoothSocket != null) {
            try {
                mBluetoothSocket!!.close()
                mBluetoothSocket!!.inputStream.close()
                mBluetoothSocket!!.outputStream.close()
                mBluetoothSocket = null
                MainActivity.mBluetoothContext = null
                MainActivity.currentUId = ""
                mIsConnected = false
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private class ConnectToDevice() : AsyncTask<Void, Void, String>() {
        private var connectSuccess: Boolean = true

        override fun doInBackground(vararg p0: Void?): String? {

            try {
                if (mBluetoothSocket == null || !mIsConnected) {
                    mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
                    val device: BluetoothDevice = mBluetoothAdapter.getRemoteDevice(mAddress)
                    mBluetoothSocket = device.createInsecureRfcommSocketToServiceRecord(mUUID)
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery()
                    mBluetoothSocket!!.connect()
                }
            }
            catch (e: IOException) {
                connectSuccess = false
                e.printStackTrace()
            }
            return null
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            if (!connectSuccess) {
                Log.d("Connect", "couldn't connect")
                backgroundToast(c, "Could not connect to device!")
            } else {
                mIsConnected = true
                Log.d("Connect", "We are connected to bluetooth device.")
                backgroundToast(c, "Connected!")

            }
            mProgress.dismiss()
        }
    }

}

fun backgroundToast(
    context: Context?,
    msg: String?
) {
    if (context != null && msg != null) {
        Handler(Looper.getMainLooper()).post(Runnable {
            Toast.makeText(
                context,
                msg,
                Toast.LENGTH_SHORT
            ).show()
        })
    }
}