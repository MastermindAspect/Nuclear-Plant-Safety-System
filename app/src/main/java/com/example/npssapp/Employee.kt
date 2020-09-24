package com.example.npssapp

import android.util.Log
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import com.google.firebase.iid.FirebaseInstanceId


@IgnoreExtraProperties
data class Employee(val uid: String = "", val title: String = "", val clockIn : String = "", val registrationToken : String = FirebaseInstanceId.getInstance().id, val clockInOut : HashMap<String,String> = HashMap<String,String>())

fun createNewEmployee(uId: String,title: String): Array<String> {
    val database = Firebase.database.reference
    val employee = Employee(uId, title)
    var outcome : Array<String> = arrayOf<String>()
    val titles : Array<String> = arrayOf("Nuclear technician", "Power plant manager", "Console operator")
    if (!titles.contains(title)){
        outcome += "No title with that name."
        return outcome
    }
    database.child("employees").child(employee.uid.toString()).setValue(employee)
        .addOnFailureListener {
            outcome += "Could not write to database!"
        }
    return outcome
}

fun clockInEmployee(uId: String, title: String = "") {
    val database = Firebase.database.reference.child("online").child(uId.toString())
    var employeeTitle = title
    getEmployee(uId) {
        val currentDateTime = LocalDateTime.now()
        if (it == null) createNewEmployee(uId, "Nuclear technician")
        else employeeTitle = it.title
        val map = mutableMapOf<String, Any>()
        map["uid"] = uId
        map["title"] = employeeTitle
        map["clockIn"] = currentDateTime.format(DateTimeFormatter.ISO_DATE)
        database.setValue(map)
    }
}

fun isClockedIn(uId: String, callback: (result: Boolean) -> Unit){
    val databaseOnlineRef = Firebase.database.reference.child("online")
    databaseOnlineRef.addListenerForSingleValueEvent(object: ValueEventListener{
        override fun onCancelled(error: DatabaseError) {
        }
        override fun onDataChange(snapshot: DataSnapshot) {
            val employeeDetails = snapshot.children
            employeeDetails.forEach {
                val childEmployee = it.getValue(Employee::class.java)
                if (childEmployee != null) {
                    val result = childEmployee.uid == uId
                    return callback(result)
                }
            }
            return callback(false)
        }
    })
}

fun clockOutEmployee(uId: String){
    val databaseOnlineRef = Firebase.database.reference.child("online")
    val databaseEmployeeRef = Firebase.database.reference.child("employees").child(uId.toString()).child("clockInOut")

    databaseOnlineRef.addListenerForSingleValueEvent(object: ValueEventListener{
        override fun onCancelled(error: DatabaseError) {
        }
        override fun onDataChange(snapshot: DataSnapshot) {
            val employeeDetails = snapshot.children
            employeeDetails.forEach {
                val childEmployee = it.getValue(Employee::class.java)
                if (childEmployee != null) {
                    if (childEmployee.uid == uId){
                        val currentDateTime = LocalDateTime.now()
                        val map = childEmployee.clockInOut
                        map[childEmployee.clockIn] = currentDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                        databaseEmployeeRef.setValue(map)
                        it.ref.removeValue()
                    }
                }
            }
        }
    })
}

fun getEmployee(uId: String, callback: (result: Employee?) -> Unit){
    val database = Firebase.database.reference
    val employees = database.child("employees")
    employees.addListenerForSingleValueEvent(object: ValueEventListener{
        override fun onCancelled(error: DatabaseError) {
        }
        override fun onDataChange(snapshot: DataSnapshot) {
            val employeeDetails = snapshot.children
            employeeDetails.forEach {

                val childEmployee = it.getValue(Employee::class.java)
                if (childEmployee != null) {
                    if (childEmployee.uid == uId){
                        callback.invoke(childEmployee)
                    }
                }
            }
            callback.invoke(null)
        }
    })
}

fun deleteEmployee(uId:String) : Boolean{
    val database = Firebase.database.reference
    val employees = database.child("employees")
    var outcome : Boolean = false
    employees.addListenerForSingleValueEvent(object: ValueEventListener{
        override fun onCancelled(error: DatabaseError) {
        }
        override fun onDataChange(snapshot: DataSnapshot) {
            val employeeDetails = snapshot.children
            employeeDetails.forEach {
                val employee = it.getValue(Employee::class.java)
                if (employee != null) {
                    if (employee.uid == uId){
                        it.ref.removeValue()
                            .addOnSuccessListener {
                                outcome = true
                            }
                            .addOnFailureListener {
                                outcome = false
                            }
                    }
                }
            }
        }
    })
    return outcome
}