package project.capstone6.acne_diagnosis

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.Toast
import com.facebook.AccessToken
import com.facebook.AccessTokenTracker
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuth.AuthStateListener
import com.google.firebase.auth.FirebaseAuth.getInstance
import com.google.firebase.auth.FirebaseUser
import project.capstone6.acne_diagnosis.databinding.ActivityResultBinding

class Result : AppCompatActivity() {

    private lateinit var binding3: ActivityResultBinding
    private lateinit var btnAgain: Button
    private lateinit var btnExit: Button

    var authStateListener: AuthStateListener? = null
    var accessTokenTracker: AccessTokenTracker? = null
    var firebaseAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding3 = ActivityResultBinding.inflate(LayoutInflater.from(this))
        setContentView(binding3.root)


        btnAgain = binding3.btnAgain
        btnExit = binding3.btnExit

        btnAgain.setOnClickListener {

            val intent3 = Intent(this, TakeSelfie::class.java)
            startActivity(intent3)
        }


        // Initialise Firebase
        firebaseAuth = FirebaseAuth.getInstance()
        val currentUser = firebaseAuth!!.currentUser

        btnExit.setOnClickListener {
            logOut()
        }

    }

    fun logOut() {

        startActivity(Intent(applicationContext, LoginActivity::class.java))

        // get google sign in status
        GoogleSignIn.getClient(
            this,
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
        )
            .signOut()
            .addOnSuccessListener { startActivity(Intent(this, LoginActivity::class.java)) }
            .addOnFailureListener {
                Toast.makeText(
                    this,
                    "Sign out failed",
                    Toast.LENGTH_SHORT
                ).show()
            }
        finish()

        // Facebook login status check
        val currentUser = firebaseAuth!!.currentUser
        firebaseAuth!!.signOut()
        LoginManager.getInstance().logOut()
        finish()


    }


}
