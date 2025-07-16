package com.example.week10

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.week10.databinding.ActivityMainBinding
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    private lateinit var binding: ActivityMainBinding
    private lateinit var googleSignInClient: GoogleSignInClient

    private val tracking_nb_google_sing_up = 9876

    private lateinit var callbackManager: CallbackManager

    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        updateUI(currentUser)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //both
        auth = FirebaseAuth.getInstance()

       //facebook
        callbackManager = CallbackManager.Factory.create()

        //google
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.defaul_web_client_id)).build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.buttonSingUpGoogle.setOnClickListener {
            Toast.makeText(this, "Clicked Button", Toast.LENGTH_SHORT).show()
            val singInIntent = googleSignInClient.signInIntent
            startActivityForResult(singInIntent, tracking_nb_google_sing_up)

        }

        binding.buttonLogOUt.setOnClickListener {
            // Sign out from Google
            googleSignInClient.signOut().addOnCompleteListener {
                // Now user is fully signed out
                Toast.makeText(this, "Singout successeful", Toast.LENGTH_SHORT).show()

                googleSignInClient.revokeAccess().addOnCompleteListener {
                    // Access revoked
                    Toast.makeText(this, "Access revoked", Toast.LENGTH_SHORT).show()
                }
            }


        }

        binding.buttonSingInFacebook.setPermissions("email", "public_profile")
        binding.buttonSingInFacebook.registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: LoginResult) {
                Log.d(TAG, "facebook:onSuccess:$loginResult")
                handleFacebookAccessToken(loginResult.accessToken)
            }

            override fun onCancel() {
                Log.d(TAG, "facebook:onCancel")
            }

            override fun onError(error: FacebookException) {
                Log.d(TAG, "facebook:onError", error)
            }
        })

        binding.buttonSingupPasswordBased.setOnClickListener {
            val email=binding.editTextText.text.toString()
            val password=binding.editTextTextPassword.text.toString()
            var isValid = true

            if (email.isEmpty()) {
                binding.editTextText.error = "Login is required"
                isValid = false
            }

            if (password.isEmpty()) {
                binding.editTextTextPassword.error = "Password is required"
                isValid = false
            }

            if (isValid) {
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success")
                            val user = auth.currentUser
                            updateUI(user)
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.exception)
                            Toast.makeText(
                                baseContext,
                                "Authentication failed:${task.exception?.message}",
                                Toast.LENGTH_SHORT,
                            ).show()
                            updateUI(null)
                        }
                    }
            }
        }

        binding.buttonPasswordBasedLogin.setOnClickListener {
            val email=binding.editTextText.text.toString().trim()
            val password=binding.editTextTextPassword.text.toString().trim()


            var isValid = true

            if (email.isEmpty()) {
                binding.editTextText.error = "Login is required"
                isValid = false
            }

            if (password.isEmpty()) {
                binding.editTextTextPassword.error = "Password is required"
                isValid = false
            }

            if (isValid) {
                // Proceed with authentication logic


                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            val user = auth.currentUser
                            updateUI(user)
                        } else {
                            updateUI(null, task.exception)
                        }
                    }
            }
        }
    }


    private fun handleFacebookAccessToken(token: AccessToken) {
        Log.d(TAG, "handleFacebookAccessToken:$token")

        val credential = FacebookAuthProvider.getCredential(token.token)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    updateUI(user)
                } else {
                    if (task.exception is FirebaseAuthUserCollisionException) {
                        Toast.makeText(this, "Account already exists with this email but another provider. Please login using Google or Email/Password.", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(this, "Authentication failed: ${task.exception?.localizedMessage}", Toast.LENGTH_SHORT).show()
                    }
                    updateUI(null)
                }
            }
    }
    companion object {
        private const val TAG = "EmailPassword"
    }

    private fun updateUI(user: FirebaseUser?,e:Exception?=null) {
        if (user==null&&e!=null){
            Toast.makeText(this, "Singin Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }else{
            Toast.makeText(this, "Sucess ${user?.uid} ", Toast.LENGTH_SHORT).show()
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager.onActivityResult(requestCode, resultCode, data)
        if (requestCode == tracking_nb_google_sing_up) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            if (task.isSuccessful) {
                val account = task.result
                val idToken = account.idToken
                val user = auth.currentUser
              //  updateUI(user)
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                Toast.makeText(this, "Google Sing In Successful :${user?.uid} ", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Google Sing In Failed", Toast.LENGTH_SHORT).show()
            }
        }

    }
}