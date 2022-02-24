package com.tuygun.shoppinglist

import android.content.Intent
import android.opengl.Visibility
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.tuygun.shoppinglist.databinding.FragmentListBinding
import java.util.ArrayList

class ListFragment : Fragment() {

    private lateinit var viewModel: ListViewModel
    private lateinit var userDatabase: DatabaseReference
    private lateinit var listDatabase: DatabaseReference
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = DataBindingUtil.inflate<FragmentListBinding>(inflater, R.layout.fragment_list, container, false)
        viewModel = ViewModelProviders.of(this).get(ListViewModel::class.java)
        auth = FirebaseAuth.getInstance()
        userDatabase = FirebaseDatabase.getInstance("https://shoppinglist-190a1-default-rtdb.firebaseio.com/").getReference("users")
        listDatabase = FirebaseDatabase.getInstance("https://shoppinglist-190a1-default-rtdb.firebaseio.com/").getReference("lists")

        val adapter = ListAdapter()
        binding.listItemsRecyclerView.adapter = adapter

//        userDatabase.child(auth.currentUser!!.uid).child("listId").get().addOnSuccessListener {
//            val userList = it.value.toString()
//            listDatabase.child(userList).child("items").get().addOnSuccessListener { list ->
//                var listAsHashMap = list.value as HashMap<String, Boolean>
//                val listOfItems = mutableListOf<List<String>>()
//                for(i in 0 until listAsHashMap.size){
//                    listOfItems.add(listOf(listAsHashMap.keys.elementAt(i), listAsHashMap.values.elementAt(i).toString()))
//                }
//                adapter.data = listOfItems
//            }
//        }

        binding.floatingActionButton.setOnClickListener {
            it.findNavController().navigate(R.id.action_listFragment_to_addListItemFragment)
        }



        val listListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if(dataSnapshot.value == null){
                    binding.noItemNoticeImageView.visibility = View.VISIBLE
                    binding.noItemTextView.visibility = View.VISIBLE
                    binding.noItemTextView2.visibility = View.VISIBLE
                    binding.addButtonArrowImageView.visibility = View.VISIBLE
                    binding.listItemsRecyclerView.visibility = View.GONE
                } else{
                    binding.noItemNoticeImageView.visibility = View.GONE
                    binding.noItemTextView.visibility = View.GONE
                    binding.noItemTextView2.visibility = View.GONE
                    binding.addButtonArrowImageView.visibility = View.GONE
                    binding.listItemsRecyclerView.visibility = View.VISIBLE
                    val listAsHashMap = dataSnapshot.value as HashMap<String, Boolean>
                    val listOfItems = mutableListOf<List<String>>()
                    for(i in 0 until listAsHashMap.size){
                        listOfItems.add(listOf(listAsHashMap.keys.elementAt(i), listAsHashMap.values.elementAt(i).toString()))
                    }
                    adapter.data = listOfItems
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w("ListFragment", "loadPost:onCancelled", databaseError.toException())
            }
        }

        userDatabase.child(auth.currentUser!!.uid).child("listId").get().addOnSuccessListener {
            val userList = it.value.toString()
            listDatabase.child(userList).child("items").addValueEventListener(listListener)
        }

        binding.menuButtonImageView.setOnClickListener {
            //showPopup(binding.menuButtonImageView)
            displayPopup(binding.menuButtonImageView)
        }



        return binding.root
    }

    private fun displayPopup(button: View) {
        val popupMenu: PopupMenu = PopupMenu(this.requireContext(),button)
        popupMenu.menuInflater.inflate(R.menu.fragment_list_menu,popupMenu.menu)
        popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
            when(item.itemId) {
                R.id.shareMenuItem -> {
                    userDatabase.child(auth.currentUser!!.uid).child("listId").get().addOnSuccessListener {
                        var listId = it.value.toString()
                        val sendIntent: Intent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, listId)
                            type = "text/plain"
                        }

                        val shareIntent = Intent.createChooser(sendIntent, null)
                        startActivity(shareIntent)
                    }
                }
                R.id.deleteMenuItem -> {
                    DatabaseOperation.deleteList(this)
                }
                R.id.signOutMenuItem -> {
                    auth.signOut()
                    val mainIntent = Intent(this.activity, LoginActivity::class.java)
                    startActivity(mainIntent)
                    this.activity?.finish()
                }
            }
            true
        })
        popupMenu.show()
    }

}