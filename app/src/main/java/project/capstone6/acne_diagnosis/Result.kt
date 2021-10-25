package project.capstone6.acne_diagnosis

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import com.facebook.AccessTokenTracker
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuth.AuthStateListener
import com.google.firebase.auth.FirebaseAuth.getInstance
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import project.capstone6.acne_diagnosis.databinding.ActivityResultBinding

class Result : AppCompatActivity() {

    private lateinit var binding3: ActivityResultBinding
    private lateinit var btnAgain: Button
    private lateinit var btnExit: Button
    private lateinit var tvResult: TextView
    private lateinit var pBarSeverity: ProgressBar
    private lateinit var memeImageView: ImageView
    private lateinit var resultText: TextView
    private lateinit var database: Firebase
    private lateinit var ref: DatabaseReference
    private lateinit var picPath: String

    var firebaseAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding3 = ActivityResultBinding.inflate(LayoutInflater.from(this))
        setContentView(binding3.root)


        btnAgain = binding3.btnAgain
        btnExit = binding3.btnExit
        //tvResult = binding3.tvResult

        //Get fulldirectory
        val intent = getIntent()
        picPath = intent.getStringExtra(TakeSelfie.EXTRA_FULLDIRECTORY).toString()

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

        pBarSeverity = binding3.severityLevel
        pBarSeverity.max = 12
        // level 2-4-8-12

        getUser()
        loadResult()

    }

    private fun getUser(){
        val user = FirebaseAuth.getInstance().currentUser
        val email = user?.email.toString()
        val uid = user?.tenantId

        binding3.tv2.text = "Hi $email"
    }

    private fun loadResult() {
        // Write a message to the database
        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference("Users")

        // get current logged in user
        val user = FirebaseAuth.getInstance().currentUser
        val uid = user?.uid

        //Get column from the table
        // if exists, then fetch the data and update the UI
        myRef.child(uid.toString()).get().addOnSuccessListener {
            if (it.exists()) {
                if (it.child("image").exists()) {
                    val diagResul = it.child("result").value
                    val level = it.child("level").value
                    val imageUri = it.child("image").value

                    binding3.skinProblem.text = diagResul.toString() + "\n"+ imageUri.toString()
                    pBarSeverity.progress = level.toString().toInt()
                }else {
                    Toast.makeText(this, "You have not made any analysis", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Invalid user", Toast.LENGTH_SHORT).show()
            }
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
