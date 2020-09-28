package com.example.npssapp

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import com.example.npssapp.MainActivity.Companion.mProgress
import com.google.android.material.internal.ContextUtils.getActivity
import java.io.IOException
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

    override fun run() {
        try {
            ConnectToDevice().execute().get(8000, TimeUnit.MILLISECONDS)
        }
        catch(e: TimeoutException){
            backgroundToast(c,"Bluetooth connection timed out!")
            mProgress.dismiss()
        }
        while ((!currentThread().isInterrupted && mBluetoothSocket != null && mIsConnected)) {
            if (mBluetoothSocket != null){
                retrieveData(mBluetoothSocket!!)
            }
            isClockedIn("asd123") {
                Log.d("TESTTT", it.toString())
            }
        }

    }
    private fun sendCommand(input: String){
        if (mBluetoothSocket != null) {
            try{
                mBluetoothSocket!!.outputStream.write(input.toByteArray())
            } catch(e: IOException) {
                e.printStackTrace()
            }
        }
    }
    private fun retrieveData(socket: BluetoothSocket){
        val inputStream = socket.inputStream
        try {
            val available = inputStream.available()
            val bytes = ByteArray(available)
            inputStream.read(bytes, 0, available)
            val uId = String(bytes)
            if (uId.length >= 8 ) {
                isClockedIn(uId) {
                    if (it) {
                        clockOutEmployee(uId)
                        sendCommand("Success on logging out!")
                    }
                    else {
                        clockInEmployee(uId)
                        MainActivity.currentUId = uId
                        MainActivity.notificationHandler = WarningNotificationHandler(uId,c!!)
                        sendCommand("Success on logging in!")
                    }
                }
                inputStream.reset()
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
                mBluetoothSocket = null
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
                backgroundToast(c,"Could not connect to device!")
            } else {
                Log.d("Connect", "We are connected to bluetooth device.")
                backgroundToast(c,"Connected!")
                mIsConnected = true
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