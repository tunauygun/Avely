package com.tuygun.shoppinglist

import android.util.Log
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

//import com.google.firebase.database.IgnoreExtraProperties

//@IgnoreExtraProperties
data class User(
    val uid: String? = null,
    val name: String? = null,
    val email: String? = null,
    val listId: String? = null
) {
    // Null default values create a no-argument default constructor, which is needed
    // for deserialization from a DataSnapshot.
}

//@IgnoreExtraProperties
data class AppList(
    val listId: String? = null,
    val creatorId: String? = null,
    val items: MutableList<ListItem>? = null,
    val allowsAccess: Boolean = false,
    val usersWithAccess: MutableList<String>? = null,
) {
    // Null default values create a no-argument default constructor, which is needed
    // for deserialization from a DataSnapshot.
}

data class ListItem(
    val item: String? = null,
    val checked: Boolean = false){
}

class DatabaseOperation{
    companion object {
        private lateinit var userDatabase: DatabaseReference
        private lateinit var listDatabase: DatabaseReference
        private lateinit var auth: FirebaseAuth

        fun updateListItemCheckedStatus(itemName :String, isChecked :Boolean){
            Log.i("DatabaseOperations", itemName)
            Log.i("DatabaseOperations", isChecked.toString())
            Log.i("DatabaseOperations", "----------------")

            auth = FirebaseAuth.getInstance()
            userDatabase = FirebaseDatabase.getInstance("https://shoppinglist-190a1-default-rtdb.firebaseio.com/").getReference("users")
            listDatabase = FirebaseDatabase.getInstance("https://shoppinglist-190a1-default-rtdb.firebaseio.com/").getReference("lists")

            userDatabase.child(auth.currentUser!!.uid).get().addOnSuccessListener {
                val listId = it.child("listId").value.toString()
                listDatabase.child(listId).child("items").child(itemName).setValue(isChecked).addOnSuccessListener {
                }
            }
        }

        fun deleteItem(itemName: String){
            auth = FirebaseAuth.getInstance()
            userDatabase = FirebaseDatabase.getInstance("https://shoppinglist-190a1-default-rtdb.firebaseio.com/").getReference("users")
            listDatabase = FirebaseDatabase.getInstance("https://shoppinglist-190a1-default-rtdb.firebaseio.com/").getReference("lists")

            userDatabase.child(auth.currentUser!!.uid).get().addOnSuccessListener {
                val listId = it.child("listId").value.toString()
                listDatabase.child(listId).child("items").child(itemName).setValue(null).addOnSuccessListener {
                }
            }
        }

        fun deleteList(fragment: Fragment){
            auth = FirebaseAuth.getInstance()
            userDatabase = FirebaseDatabase.getInstance("https://shoppinglist-190a1-default-rtdb.firebaseio.com/").getReference("users")
            listDatabase = FirebaseDatabase.getInstance("https://shoppinglist-190a1-default-rtdb.firebaseio.com/").getReference("lists")

            userDatabase.child(auth.currentUser!!.uid).get().addOnSuccessListener {
                val listId = it.child("listId").value.toString()
                listDatabase.child(listId).setValue(null).addOnSuccessListener {
                    userDatabase.child(auth.currentUser!!.uid).child("listId").setValue(null).addOnSuccessListener {
                        fragment.requireView().findNavController().navigate(R.id.action_listFragment_to_findListFragment)
                        Toast.makeText(fragment.activity, "Deleted Successfully", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        fun doesListExist(listId :String): Boolean {
            var doesExist = false;
            listDatabase = FirebaseDatabase.getInstance("https://shoppinglist-190a1-default-rtdb.firebaseio.com/").getReference("lists")
            listDatabase.child(listId).child("listId").get().addOnSuccessListener {
                if(it.value != null){
                    doesExist = true
                }
            }
            return doesExist

        }
    }
}




/*
USERS
   UserA
      Uid
      Name
      Email
      Listid
LISTS
   ListA
      ListID
      Creater
      Items
      allowsAccess
      userWithAccess
 */