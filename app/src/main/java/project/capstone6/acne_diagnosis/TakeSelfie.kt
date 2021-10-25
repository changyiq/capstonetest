package project.capstone6.acne_diagnosis

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.Image
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.os.Environment.getExternalStoragePublicDirectory
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.FileProvider
import com.android.volley.*
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.storage.FirebaseStorage
import org.json.JSONException
import org.json.JSONObject
import project.capstone6.acne_diagnosis.databinding.ActivityTakeSelfieBinding
import java.io.File
import java.nio.charset.Charset
import java.util.*

private const val FILE_NAME ="selfie"
class TakeSelfie : AppCompatActivity() {

    val TAG ="TakeSelfie"

    private lateinit var binding2: ActivityTakeSelfieBinding
    private lateinit var btnTakeSelfie : Button
    private lateinit var btnDiagnosis : Button
    private lateinit var imageView: ImageView
    private lateinit var ImageUri: Uri
    private lateinit var photoFile: File
    private lateinit var fileProvider: Uri

    private lateinit var subDir: String
    private lateinit var fullDir: String

    var volleyRequestQueue: RequestQueue? = null
    val url: String = "http://localhost:44374/api/Dir"
    //val url: String = "https://reqres.in/api/dir"

    var dialog: ProgressDialog? = null

    var firebaseAuth: FirebaseAuth? = null

    companion object {
        const val REQUEST_FROM_CAMERA = 1001
        const val EXTRA_FULLDIRECTORY = "SavedFulldirectory"
        const val EXTRA_SUBDIRECTORY = "SavedSubdirectory"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding2 = ActivityTakeSelfieBinding.inflate(LayoutInflater.from(this))
        setContentView(binding2.root)

        btnTakeSelfie = binding2.btnTakeSelfie
        btnDiagnosis = binding2.btnDiagnosis
        imageView = binding2.imageView

        // Initialise Firebase
        firebaseAuth = FirebaseAuth.getInstance()
        val currentUser = firebaseAuth!!.currentUser

        //click the button to invoke an intent to take a selfie
        btnTakeSelfie.setOnClickListener(){
            val takeSelfieIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
           photoFile = getPhotoFile(FILE_NAME)

           fileProvider = FileProvider.getUriForFile(this,"project.capstone6.fileprovider", photoFile)
            takeSelfieIntent.putExtra(MediaStore.EXTRA_OUTPUT,fileProvider)

            if(takeSelfieIntent.resolveActivity(this.packageManager) != null){
                startActivityForResult(takeSelfieIntent, REQUEST_FROM_CAMERA)
            }else{
                Toast.makeText(this,"Unable to open camera",Toast.LENGTH_LONG).show()
            }
        }
        btnDiagnosis.setOnClickListener {

            if(subDir != "" && subDir != null){
                // call intent to go to result page
                val intent = Intent(this, Result::class.java)

                //pass fulldirectory information to Result page
                //Toast.makeText(this," Save Subdir as " + subDir,Toast.LENGTH_LONG).show()
                intent.putExtra(EXTRA_FULLDIRECTORY,fullDir)
                intent.putExtra(EXTRA_SUBDIRECTORY,subDir)
                startActivity(intent)

                //pass directory to API
                postVolley(fullDir,subDir)

                //clear subDir
                subDir =""
            }
        }
    }

    //to create a file for the selfie
    private fun getPhotoFile(fileName:String):File{
        val storageDirectory = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(fileName,".jpg",storageDirectory)
    }

    //to Retrieve the selfie， display it in an ImageView and upload into Firebase cloud
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode == REQUEST_FROM_CAMERA && resultCode == Activity.RESULT_OK){

            val takenImage = BitmapFactory.decodeFile(photoFile.absolutePath)
            imageView.setImageBitmap(takenImage)
            Toast.makeText(this, "image saved", Toast.LENGTH_SHORT).show()

            //uploadImage(this, fileProvider)
            subDir = FirebaseStorageManager().uploadImage(this, fileProvider)
            fullDir = "gs://acne-diagnosis-6a653.appspot.com/" + subDir
        } else{
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    fun postVolley(fullDirectory: String, subDirectory: String) {

        // Post parameters
        // Form fields and values
        val params = HashMap<String,String>()
        params["fullDirectory"] = fullDirectory
        params["subDirectory"] = subDirectory
        val jsonObject = JSONObject(params as Map<*, *>)

        // Volley post request with parameters
        val request = JsonObjectRequest(
            Request.Method.POST,url,jsonObject,
            Response.Listener { response ->
                // Process the json
                try {
                    //textView.text = "Response: $response"
                    Toast.makeText(this, "Response: \nMessage be posted to API. \n$response", Toast.LENGTH_LONG).show()
                }catch (e:Exception){
                    //textView.text = "Exception: $e"
                    Toast.makeText(this, "Exception: $e", Toast.LENGTH_LONG).show()
                }

            }, Response.ErrorListener{
                // Error in request
                //textView.text = "Volley error: $it"
                Toast.makeText(this, "Volley error: $it", Toast.LENGTH_LONG).show()
            })


        // Volley request policy, only one time request to avoid duplicate transaction
        request.retryPolicy = DefaultRetryPolicy(
            DefaultRetryPolicy.DEFAULT_TIMEOUT_MS,
            // 0 means no retry
            0, // DefaultRetryPolicy.DEFAULT_MAX_RETRIES = 2
            1f // DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )

        // Add the volley post request to the request queue
        VolleySingleton.getInstance(this).addToRequestQueue(request)

    }

    //process menu
    override fun onCreateOptionsMenu(menu: Menu): Boolean {

        val inflater = menuInflater
        inflater.inflate(R.menu.takeselfiemenu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_result -> {
            val intent = Intent(this, Result::class.java)
            startActivity(intent)
            true
        }
        R.id.action_homepage -> {
            val intent = Intent(this, MainActivity::class.java)
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