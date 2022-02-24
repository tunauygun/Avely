package com.tuygun.shoppinglist

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.tuygun.shoppinglist.databinding.FragmentFindListBinding

class FindListFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var userDatabase: DatabaseReference
    private lateinit var listDatabase: DatabaseReference

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,savedInstanceState: Bundle?): View? {
        val binding = DataBindingUtil.inflate<FragmentFindListBinding>(inflater, R.layout.fragment_find_list, container, false)
        auth = FirebaseAuth.getInstance()
        userDatabase = FirebaseDatabase.getInstance("https://shoppinglist-190a1-default-rtdb.firebaseio.com/").getReference("users")
        listDatabase = FirebaseDatabase.getInstance("https://shoppinglist-190a1-default-rtdb.firebaseio.com/").getReference("lists")

        // get user from database. set username text. check if user has list.
        userDatabase.child(auth.currentUser!!.uid).get().addOnSuccessListener {
            var userName = it.child("name").value.toString()
            userName = userName.split(" ")[0]
            binding.welcomeText.text = "Welcome $userName!"
            val listId = it.child("listId").value.toString()
            Log.i("FindListFragment", "listId: $listId")
            if (listId=="null"){
                // User does not have a list yet.
            } else{
                requireView().findNavController().navigate(R.id.action_findListFragment_to_listFragment)
            }
        }.addOnFailureListener {
            Log.e("FindListFragment", "Error getting user data", it)
        }

        binding.createNewListButton.setOnClickListener {
            // Generate a reference to a new location and add some data using push()
            val pushedListRef: DatabaseReference = listDatabase.push()
            // Get the unique ID generated by a push()
            val listId = pushedListRef.key

            val newList = AppList(listId = listId, creatorId = auth.uid, allowsAccess = false)
            listDatabase.child(listId!!).setValue(newList).addOnFailureListener {
            }.addOnSuccessListener {
                listDatabase.child(listId!!).child("usersWithAccess").child(auth.uid!!).setValue(true).addOnSuccessListener {
                    userDatabase.child(auth.currentUser!!.uid).child("listId").setValue(listId).addOnFailureListener {
                    }.addOnSuccessListener {
                        Toast.makeText(this.context, "New list is created successfully!.", Toast.LENGTH_LONG).show()
                        requireView().findNavController().navigate(R.id.action_findListFragment_to_listFragment)
                    }
                }
            }
        }

        binding.enterAccessCodeImageView.setOnClickListener {
            //val x = DatabaseOperation.doesListExist(binding.accessCodeEditText.text.toString())
            //Toast.makeText(this.context, binding.accessCodeEditText.text.toString() + " " + x.toString(), Toast.LENGTH_LONG).show()
            listDatabase.child(binding.accessCodeEditText.text.toString()).child("listId").get().addOnSuccessListener {
                var a = it.value.toString().equals(binding.accessCodeEditText.text.toString())
                Toast.makeText(this.context, a.toString(), Toast.LENGTH_SHORT).show()
                if (a){
                    userDatabase.child(auth.currentUser!!.uid).child("listId").setValue(binding.accessCodeEditText.text.toString()).addOnFailureListener {
                    }.addOnSuccessListener {
                        listDatabase.child(binding.accessCodeEditText.text.toString()).child("usersWithWriteAccess").child(auth.currentUser!!.uid).setValue(false).addOnSuccessListener {
                            requireView().findNavController().navigate(R.id.action_findListFragment_to_listFragment)
                        }
                    }
                }
            }
        }

        return binding.root
    }

}