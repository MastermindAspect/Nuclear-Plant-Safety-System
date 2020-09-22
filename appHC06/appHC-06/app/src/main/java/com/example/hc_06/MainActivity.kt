package com.example.hc_06

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Adapter
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    lateinit var m_bluetoothAdapter: BluetoothAdapter
    lateinit var pairedDevices: Set<BluetoothDevice>
    val REQUEST_BLUETOOTH_ENABLE = 1

    companion object{
        val EXTRA_ADRESS: String = "Device_adress"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        m_bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if(m_bluetoothAdapter == null){
            Toast.makeText(this, "Device don't support bluetooth", Toast.LENGTH_SHORT).show()
            return
        }
        if(!m_bluetoothAdapter.isEnabled){
            val enableBluetoothIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBluetoothIntent, REQUEST_BLUETOOTH_ENABLE)
        }

        refresh.setOnClickListener{
             pairedDeviceList()

        }

    }

    private fun pairedDeviceList(){
        pairedDevices = m_bluetoothAdapter.bondedDevices
        val list: ArrayList<BluetoothDevice> = ArrayList()

        if(!pairedDevices.isEmpty()){
            for (device: BluetoothDevice in pairedDevices){
                list.add(device)
                Log.i("device", ""+device)
            }
        }else {
            Toast.makeText(this, "no devices found", Toast.LENGTH_SHORT).show()
        }
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, list)
        select_device_list.adapter = adapter
        select_device_list.onItemClickListener = AdapterView.OnItemClickListener{
            _,_, position, _ ->
            val device: BluetoothDevice = list[position]
            val address: String = device.address
            val intent = Intent(this, controlDevice::class.java)
            intent.putExtra(EXTRA_ADRESS, address)
            startActivity(intent)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == REQUEST_BLUETOOTH_ENABLE){
            if(resultCode == Activity.RESULT_OK){
                if(m_bluetoothAdapter.isEnabled){
                    Toast.makeText(this, "bluetooth enable", Toast.LENGTH_SHORT).show()
                }else{
                    Toast.makeText(this, "bluetooth disabled", Toast.LENGTH_SHORT).show()
                }


            }
            else if(resultCode == Activity.RESULT_CANCELED){
                Toast.makeText(this, "canceled",Toast.LENGTH_SHORT).show()
            }
        }

    }
}