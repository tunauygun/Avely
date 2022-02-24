package com.tuygun.shoppinglist

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.tuygun.shoppinglist.databinding.ActivityRegisterBinding
import java.util.regex.Pattern

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var userDatabase: DatabaseReference
    val EMAIL_ADDRESS_PATTERN = Pattern.compile(
        "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
                "\\@" +
                "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
                "(" +
                "\\." +
                "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
                ")+"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_register)

        userDatabase = FirebaseDatabase.getInstance("https://shoppinglist-190a1-default-rtdb.firebaseio.com/").getReference("users")

        auth = FirebaseAuth.getInstance()

        binding.registerButton.setOnClickListener {
            val name = binding.nameEditTest.text.toString()
            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()

            if(name!="" && email!="" && password!=""){
                if(isValidEmail(email)) {
                    signUpUser(name, email, password)
                }else{
                    Toast.makeText(baseContext, "Invalid Email Address!",
                        Toast.LENGTH_SHORT).show()
                }
            }

        }
    }

    private fun createUserInDatabase(name: String) {
        val user = auth.currentUser

        userDatabase.child(auth.currentUser!!.uid).get().addOnSuccessListener {
            if (it.value == null){
                val databaseUser = User(uid = user?.uid, name = name, email = user?.email, listId = null)
                userDatabase.child(user!!.uid).setValue(databaseUser).addOnFailureListener {
                    Log.i("RegisterActivity", "FAILED TO WRITE DATA! FAILED TO WRITE DATA! FAILED TO WRITE DATA!")
                    Log.i("RegisterActivity", it.toString())

                }.addOnSuccessListener {
                    Log.i("RegisterActivity", "SUCCESSFULLY WROTE DATA! SUCCESSFULLY WROTE DATA! SUCCESSFULLY WROTE DATA!")
                    Log.i("RegisterActivity",
                        databaseUser.toString())
                    val mainIntent = Intent(this, LoginActivity::class.java)
                    startActivity(mainIntent)
                    finish()
                }
            }else{
                Log.i("RegisterActivity", "User is already registered in database")
            }
        }.addOnFailureListener {
            Log.e("RegisterActivity", "Error getting user data", it)
        }
    }

    private fun signUpUser(name: String, email: String, password: String){
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("RegisterActivity", "createUserWithEmail:success")
                    Toast.makeText(baseContext, "Registration successful.",
                        Toast.LENGTH_SHORT).show()
                    createUserInDatabase(name)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("RegisterActivity", "createUserWithEmail:failure", task.exception)
                    Toast.makeText(baseContext, "Registration failed.",
                        Toast.LENGTH_SHORT).show()
                }
            }
    }


    private fun isValidEmail(str: String): Boolean{
        return EMAIL_ADDRESS_PATTERN.matcher(str).matches()
    }


}

