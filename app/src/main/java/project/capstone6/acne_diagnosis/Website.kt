package project.capstone6.acne_diagnosis

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.webkit.WebView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import project.capstone6.acne_diagnosis.databinding.ActivityWebsiteBinding

class Website : AppCompatActivity()  {

    private lateinit var binding4: ActivityWebsiteBinding
    private lateinit var web: WebView

    var firebaseAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding4 = ActivityWebsiteBinding.inflate(LayoutInflater.from(this))
        setContentView(binding4.root)

        web = binding4.web

        // get the url clicked from the result page - resources link
        val intent = getIntent()
        val url = intent.getStringExtra("URL")
        web.loadUrl(url.toString())
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {

        val inflater = menuInflater
        inflater.inflate(R.menu.menu, menu)
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_result -> {
            val intent = Intent(this, Result::class.java)
            startActivity(intent)
            true
        }
        R.id.action_analyze -> {
            val intent = Intent(this, TakeSelfie::class.java)
            startActivity(intent)
            true
        }
        R.id.action_exit -> {
            logOut()
            true
        }
        else -> {
            super.onOptionsItemSelected(item)
        }
    }
    // menu option to log out
    fun logOut() {

        startActivity(Intent(applicationContext, LoginActivity::class.java))
        firebaseAuth = FirebaseAuth.getInstance()
        //val currentUser: FirebaseUser? = firebaseAuth!!.currentUser

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
        firebaseAuth!!.signOut()
        LoginManager.getInstance().logOut()
        finish()
    }

}