package com.tuygun.shoppinglist

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.tuygun.shoppinglist.databinding.FragmentAddListItemBinding

class AddListItemFragment : Fragment() {
    private lateinit var userDatabase: DatabaseReference
    private lateinit var listDatabase: DatabaseReference
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,savedInstanceState: Bundle?): View? {
        val binding = DataBindingUtil.inflate<FragmentAddListItemBinding>(inflater, R.layout.fragment_add_list_item, container, false)

        auth = FirebaseAuth.getInstance()
        userDatabase = FirebaseDatabase.getInstance("https://shoppinglist-190a1-default-rtdb.firebaseio.com/").getReference("users")
        listDatabase = FirebaseDatabase.getInstance("https://shoppinglist-190a1-default-rtdb.firebaseio.com/").getReference("lists")

        binding.addItemButton.setOnClickListener {
            val newItemText = binding.editTextTextPersonName3.text.toString()
            if (newItemText.isNotBlank()){
                val newItem = ListItem(newItemText, false)

                userDatabase.child(auth.currentUser!!.uid).get().addOnSuccessListener {
                    val listId = it.child("listId").value.toString()
                    listDatabase.child(listId).child("items").child(newItemText).setValue(false).addOnSuccessListener {
                        Toast.makeText(this.context, "New item is created successfully!.", Toast.LENGTH_LONG).show()
                        requireView().findNavController().navigate(R.id.action_addListItemFragment_to_listFragment)
                    }
                }
            }
        }

        return binding.root
    }

}