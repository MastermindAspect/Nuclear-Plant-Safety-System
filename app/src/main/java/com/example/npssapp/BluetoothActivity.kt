package com.example.npssapp

import android.os.Bundle
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import android.app.Activity
import android.app.ProgressDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.util.Log
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.npssapp.MainActivity.Companion.mProgress
import kotlinx.coroutines.NonCancellable.start
import kotlinx.coroutines.runBlocking
import java.io.IOException
import java.util.*

class Bluetooth() : Thread() {

    companion object {
        var mBluetoothSocket : BluetoothSocket? = null
        lateinit var mBluetoothAdapter : BluetoothAdapter
        var mIsConnected : Boolean = false
        const val mAddress : String ="98:D3:41:F9:76:43"
        val mUUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    }

    override fun run() {
        runBlocking {
            ConnectToDevice().execute()
        }
        while(!Thread.currentThread().isInterrupted && mBluetoothSocket!=null && mIsConnected){
            Log.d("Connect", "We are connected to bluetooth device.")
            retrieveData(mBluetoothSocket!!)// retrieve data from module then do necessary thing
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
            isClockedIn(uId.toInt()) {
                if (it == true) {
                    clockOutEmployee(uId.toInt())
                    sendCommand("Success on logging out!")
                }
                else {
                    clockInEmployee(uId.toInt())
                    sendCommand("Success on logging in!")
                }
            }
        } catch (e: IOException) {
            Log.e("client", "Cannot read data", e)
        }
    }

    private fun disconnect(){
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
            } catch (e: IOException) {
                connectSuccess = false
                e.printStackTrace()
            }
            return null
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            if (!connectSuccess) {
                Log.i("data", "couldn't connect")
            } else {
                mIsConnected = true
            }
            mProgress.dismiss()
        }
    }
}
