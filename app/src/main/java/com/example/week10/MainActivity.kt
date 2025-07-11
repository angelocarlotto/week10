package com.example.week10

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.week10.databinding.ActivityMainBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var auth: FirebaseAuth
    private val tracking_nb_google_sing_up = 9876

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
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

        binding.buttonSingupFacebook.setOnClickListener {

        }

        binding.buttonFaceBookLogout.setOnClickListener {

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == tracking_nb_google_sing_up) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            if (task.isSuccessful) {
                val account = task.result
                val idToken = account.idToken

                val credential = GoogleAuthProvider.getCredential(idToken, null)
                Toast.makeText(this, "Google Sing In Successful ", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Google Sing In Failed", Toast.LENGTH_SHORT).show()
            }
        }

    }
}