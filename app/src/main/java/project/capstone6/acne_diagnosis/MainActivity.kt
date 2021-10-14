package project.capstone6.acne_diagnosis

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class MainActivity : AppCompatActivity() {

    var firebaseAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val myNewsList = generateNewsList(4)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.adapter = MyRecyclerView(myNewsList)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)

    }

    private fun generateNewsList(size: Int): List<NewsFeeds> {

        val list = ArrayList<NewsFeeds>()

        for (i in 0 until size) {
            val drawable = when (i % 3) {
                0 -> R.drawable.img1
                1 -> R.drawable.img2
                else -> R.drawable.img3
            }

            val newsFeed = when (i % 3) {
                0 -> getString(R.string.feed1)
                1 -> getString(R.string.feed2)
                else -> getString(R.string.feed3)
            }

            val item = NewsFeeds(drawable, newsFeed)

            list += item
        }

        return list
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