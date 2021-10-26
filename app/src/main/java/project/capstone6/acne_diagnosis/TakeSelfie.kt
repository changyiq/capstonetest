package project.capstone6.acne_diagnosis

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Base64
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.android.volley.AuthFailureError
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import org.json.JSONObject
import project.capstone6.acne_diagnosis.databinding.ActivityTakeSelfieBinding
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.*


private const val FILE_NAME = "selfie"

class TakeSelfie : AppCompatActivity() {

    //val TAG = "TakeSelfie"
    private lateinit var binding2: ActivityTakeSelfieBinding
    private lateinit var btnTakeSelfie: Button
    private lateinit var btnDiagnosis: Button
    private lateinit var imageView: ImageView
    private lateinit var photoFile: File
    private lateinit var fileProvider: Uri
    private lateinit var takenImage: Bitmap

    private lateinit var subDir: String
    private lateinit var fullDir: String


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

        //clear subDir
        subDir =""

        //click the button to invoke an intent to take a selfie
        btnTakeSelfie.setOnClickListener() {
            val takeSelfieIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            photoFile = getPhotoFile(FILE_NAME)

            fileProvider =
                FileProvider.getUriForFile(this, "project.capstone6.fileprovider", photoFile)
            takeSelfieIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider)

            if (takeSelfieIntent.resolveActivity(this.packageManager) != null) {
                startActivityForResult(takeSelfieIntent, REQUEST_FROM_CAMERA)
            } else {
                Toast.makeText(this, "Unable to open camera", Toast.LENGTH_LONG).show()
            }
        }
        btnDiagnosis.setOnClickListener {

            if (subDir != "" && subDir != null) {
                // call intent to go to result page
                val intent = Intent(this, Result::class.java)

                //pass fulldirectory information to Result page
                //Toast.makeText(this," Save Subdir as " + subDir,Toast.LENGTH_LONG).show()
                intent.putExtra(EXTRA_FULLDIRECTORY, fullDir)
                intent.putExtra(EXTRA_SUBDIRECTORY, subDir)
                startActivity(intent)

                //upload image to API by Volley
                postImageByVolley(takenImage)

                //upload image to API by HttpURLConnection
                //postImageByHttpURLConnection(takenImage)

                //pass path message to API
                //postPathByVolley(fullDir)

            }else {

                // Tell user to wait
                Toast.makeText(this,"Please take selfie for skin, or check for your history analysis.",Toast.LENGTH_LONG).show()
            }        }
    }

    //to create a file for the selfie
    private fun getPhotoFile(fileName: String): File {
        val storageDirectory = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(fileName, ".jpg", storageDirectory)
    }

    //to Retrieve the selfie， display it in an ImageView and upload into Firebase cloud
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_FROM_CAMERA && resultCode == Activity.RESULT_OK) {

            //getting image from the file stored the selfie
            takenImage = BitmapFactory.decodeFile(photoFile.absolutePath)
            imageView.setImageBitmap(takenImage)

            //uploadImage(this, fileProvider)
            subDir = FirebaseStorageManager().uploadImage(this, fileProvider)
            fullDir = "gs://acne-diagnosis-6a653.appspot.com/" + subDir
            Toast.makeText(this, "Selfie upload to Firebase", Toast.LENGTH_SHORT).show()

            // add image into realtime database
            // Write a message to the database
            val database = FirebaseDatabase.getInstance()
            val myRef = database.getReference("Users")

            val user = FirebaseAuth.getInstance().currentUser
            val uid = user?.uid

            // Get column from the table
            // get the user nd add the data
            myRef.child(uid.toString()).get().addOnSuccessListener {
                if (it.child("image").exists()) {
                    myRef.child(uid.toString()).child("image").setValue(fullDir)
                }else if (!it.child("image").exists()){
                    myRef.child(uid.toString()).child("image").setValue(fullDir)
                    //myRef.child(uid.toString()).child("result").setValue(SymptomEnum.AD)
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    fun postPathByVolley(fullDirectory: String) {

        //val url1: String = "http://localhost:44374/api/Image"
        val url1: String = "https://reqres.in/api/dir"

        // Post parameters, Form fields and values
        val params = HashMap<String, String>()
        params["fullDirectory"] = fullDirectory
        val jsonObject = JSONObject(params as Map<*, *>)

        // Volley post request with parameters
        val request1 = JsonObjectRequest(
            Request.Method.POST, url1, jsonObject,
            Response.Listener { response ->
                // Process the json
                try {
                    //textView.text = "Response: $response"
                    Toast.makeText(
                        this,
                        "Response: \nPath are posted to API. \n$response",
                        Toast.LENGTH_LONG
                    ).show()
                } catch (e: Exception) {
                    //textView.text = "Exception: $e"
                    Toast.makeText(this, "Exception: $e", Toast.LENGTH_LONG).show()
                }

            }, Response.ErrorListener {
                // Error in request
                //textView.text = "Volley error: $it"
                Toast.makeText(this, "Volley error: $it", Toast.LENGTH_LONG).show()
            })


        // Volley request policy, only one time request to avoid duplicate transaction
        request1.retryPolicy = DefaultRetryPolicy(
            DefaultRetryPolicy.DEFAULT_TIMEOUT_MS,
            // 0 means no retry
            0, // DefaultRetryPolicy.DEFAULT_MAX_RETRIES = 2
            1f // DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )

        // Add the volley post request to the request queue
        VolleySingleton.getInstance(this).addToRequestQueue(request1)

    }

    /*
    fun postImageByHttpURLConnection(bitmap: Bitmap) {
        try {
            val url = URL("http://localhost:44374/api/Image")
            val c: HttpURLConnection = url.openConnection() as HttpURLConnection
            c.setDoInput(true)
            c.setRequestMethod("POST")
            c.setDoOutput(true)
            c.connect()

            val output: OutputStream = c.getOutputStream()
            bitmap.compress(CompressFormat.JPEG, 50, output)
            output.close()

            val result = Scanner(c.getInputStream())
            val response: String = result.nextLine()
            Log.e("ImageUploader", "Error uploading image: $response")

            result.close()
        } catch (e: IOException) {
            Log.e("ImageUploader", "Error uploading image", e)
        }
    }
    */


    fun postImageByVolley(image: Bitmap) {

        val url2: String = "http://localhost:44374/api/Image"

        //converting image to base64 string
        val baos = ByteArrayOutputStream()
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val imageBytes = baos.toByteArray()
        val imageString = Base64.encodeToString(imageBytes, Base64.DEFAULT)

        //sending image to server
        val request2: StringRequest = object : StringRequest(
            Method.POST, url2,
            Response.Listener { s ->
                if (s == "true") {
                    Toast.makeText(this@TakeSelfie, "Uploaded Image Successful", Toast.LENGTH_LONG)
                        .show()
                } else {
                    Toast.makeText(this@TakeSelfie, "Some error occurred!", Toast.LENGTH_LONG)
                        .show()
                }
            },
            Response.ErrorListener { volleyError ->
                Toast.makeText(
                    this@TakeSelfie,
                    "Some error occurred -> $volleyError",
                    Toast.LENGTH_LONG
                ).show()
            }) {
            //adding parameters to send
            @Throws(AuthFailureError::class)
            override fun getParams(): Map<String, String>? {
                val parameters: MutableMap<String, String> = HashMap()
                parameters["image"] = imageString
                return parameters
            }
        }

        // Add the volley post request to the request queue
        VolleySingleton.getInstance(this).addToRequestQueue(request2)
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
        //val currentUser = firebaseAuth!!.currentUser
        firebaseAuth!!.signOut()
        LoginManager.getInstance().logOut()
        finish()
    }
}