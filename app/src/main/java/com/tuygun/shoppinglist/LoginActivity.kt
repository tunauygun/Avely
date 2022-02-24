package com.tuygun.shoppinglist

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.tuygun.shoppinglist.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    companion object{
        private const val RC_SIGN_IN = 1001
        private const val TAG = "LoginActivity"
    }

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var userDatabase: DatabaseReference


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)

        userDatabase = FirebaseDatabase.getInstance("https://shoppinglist-190a1-default-rtdb.firebaseio.com/").getReference("users")
        Log.i(TAG,"USER DATABASE KEY! USER DATABASE KEY! USER DATABASE KEY! USER DATABASE KEY! USER DATABASE KEY!")
        Log.i(TAG, userDatabase.database.reference.toString())
        //userDatabase.child("users").child("userId").setValue("user")
        //userDatabase = Firebase.database.getReference("users")
        auth = FirebaseAuth.getInstance()
        //auth.signOut()
        val user = auth.currentUser
        if(user != null){
            val mainIntent = Intent(this, MainActivity::class.java)
            startActivity(mainIntent)
            finish()
        }

        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        googleSignInClient.signOut()

        binding.googleSignInButton.setOnClickListener {
            signInWithGoogle()
        }

        binding.signInButton.setOnClickListener {
            signInWithEmail()
        }

        binding.signUpText.setOnClickListener {
            val registerIntent = Intent(this, RegisterActivity::class.java)
            startActivity(registerIntent)
        }

    }

    private fun signInWithEmail() {
        val email = binding.emailSignInEditText.text.toString()
        val password = binding.passwordSignInEditText.text.toString()

        if(email!="" && password!=""){
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithEmail:success")
                        val user = auth.currentUser
                        val mainIntent = Intent(this, MainActivity::class.java)
                        startActivity(mainIntent)
                        finish()
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithEmail:failure", task.exception)
                        Toast.makeText(baseContext, "Authentication failed.",
                            Toast.LENGTH_SHORT).show()
                    }
                }

        }
    }

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.id)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e)
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")

                    val user = auth.currentUser


                    userDatabase.child(auth.currentUser!!.uid).get().addOnSuccessListener {
                        if (it.value == null){
                            val databaseUser = User(uid = user?.uid, name = user?.displayName, email = user?.email, listId = null)
                            userDatabase.child(user!!.uid).setValue(databaseUser).addOnFailureListener {
                                Log.i("LoginActivity", "FAILED TO WRITE DATA! FAILED TO WRITE DATA! FAILDE TO WRITE DATA!")
                                Log.i("LoginActivity", it.toString())
                            }.addOnSuccessListener {
                                Log.i("LoginActivity", "SUCCESSFULLY WROTE DATA! SUCCESSFULLY WROTE DATA! SUCCESSFULLY WROTE DATA!")
                                Log.i("LoginActivity", databaseUser.toString())
                            }
                        }else{
                            Log.i("LoginActivity", "User is already registered in database")
                        }
                    }.addOnFailureListener {
                        Log.e(TAG, "Error getting user data", it)
                    }


                    val mainIntent = Intent(this, MainActivity::class.java)
                    startActivity(mainIntent)
                    finish()
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                }
            }
    }
}