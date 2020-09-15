package com.example.npssapp

import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase

val database = Firebase.database.reference

@IgnoreExtraProperties
data class Employee(
    var uId: Int,
    var title: String
)

fun createNewEmployee(uId: Int,title: String): Array<String> {
    val employee = Employee(uId, title)
    var outcome : Array<String> = arrayOf<String>()
    val titles : Array<String> = arrayOf("Nuclear technician", "Power plant manager", "Console operator")
    if (!titles.contains(title)){
        outcome += "No title with that name."
        return outcome
    }
    database.child("users").setValue(employee)
        .addOnFailureListener {
            outcome += "Could not write to database!"
        }
    return outcome
}

fun deleteEmployee(uId:Int) : Boolean{
    val employees = database.child("users")
    var outcome : Boolean = false
    employees.addListenerForSingleValueEvent(object: ValueEventListener{
        override fun onCancelled(error: DatabaseError) {
        }
        override fun onDataChange(snapshot: DataSnapshot) {
            val employeeDetails = snapshot.children
            employeeDetails.forEach {
                val employee = it.getValue<Employee>()
                if (employee != null) {
                    if (employee.uId == uId){
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