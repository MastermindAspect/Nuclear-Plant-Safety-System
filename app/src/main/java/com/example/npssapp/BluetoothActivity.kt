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
import kotlinx.coroutines.NonCancellable.start
import java.io.IOException
import java.util.*

/*mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
if(!mBluetoothAdapter.isEnabled) {
    val enableBluetoothIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
    startActivityForResult(enableBluetoothIntent, REQUEST_ENABLE_BLUETOOTH)
}
override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    if (requestCode == REQUEST_ENABLE_BLUETOOTH) {
        if (resultCode == Activity.RESULT_OK) {
            if (mBluetoothAdapter.isEnabled) {
                Toast.makeText(headContext, "Bluetooth has been enabled", Toast.LENGTH_SHORT).show()

            } else {
                Toast.makeText(headContext, "Bluetooth has been disabled", Toast.LENGTH_SHORT).show()

            }
        } else if (resultCode == Activity.RESULT_CANCELED) {
            Toast.makeText(headContext, "Bluetooth enabling has been canceled", Toast.LENGTH_SHORT).show()
        }
    }
}
 */
class Bluetooth(c:Context) : Thread() {

    companion object {
        var mBluetoothSocket : BluetoothSocket? = null
        lateinit var mProgress : ProgressDialog
        lateinit var mBluetoothAdapter : BluetoothAdapter
        var mIsConnected : Boolean = false
        val mAddress : String ="MAC_ADRESS"
        var mUUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    }
    private val headContext = c

    lateinit var mBluetoothAdapter: BluetoothAdapter
    private val REQUEST_ENABLE_BLUETOOTH = 1

    override fun run() {
        ConnectToDevice(headContext).execute()
        while(!Thread.currentThread().isInterrupted && mBluetoothSocket!=null){
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

    private class ConnectToDevice(c: Context) : AsyncTask<Void, Void, String>() {
        private var connectSuccess: Boolean = true
        private val context: Context = c

        override fun onPreExecute() {
            super.onPreExecute()
            mProgress = ProgressDialog.show(context, "Connecting...", "please wait")
        }

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
