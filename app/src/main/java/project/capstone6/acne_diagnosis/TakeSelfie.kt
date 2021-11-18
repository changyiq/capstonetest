package project.capstone6.acne_diagnosis

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.android.volley.AuthFailureError
import com.android.volley.Response
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import project.capstone6.acne_diagnosis.databinding.ActivityTakeSelfieBinding
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.*
import android.annotation.SuppressLint
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.*

import com.android.volley.toolbox.Volley
import project.capstone6.acne_diagnosis.infrastructure.*

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
    private lateinit var responseFromApi: String

    var firebaseAuth: FirebaseAuth? = null

    // embedded obj to pass around
    companion object {
        const val REQUEST_FROM_CAMERA = 1001
        const val EXTRA_FULLDIRECTORY = "SavedFulldirectory"
        const val EXTRA_SUBDIRECTORY = "SavedSubdirectory"
        const val RESPONSE_BY_API = "response"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding2 = ActivityTakeSelfieBinding.inflate(LayoutInflater.from(this))
        setContentView(binding2.root)

        btnTakeSelfie = binding2.btnTakeSelfie
        btnDiagnosis = binding2.btnDiagnosis
        imageView = binding2.imageView

        responseFromApi = ""

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

                handleSSLHandshake()

                // call intent to go to result page
                val intent = Intent(this, Result::class.java)

                //upload image to API by Volley

                postImageByVolley(takenImage)
                Log.w("Response in takeSelfie btnClick-----------", responseFromApi)
                intent.putExtra(RESPONSE_BY_API, responseFromApi)

                // pass image
                val baos = ByteArrayOutputStream()
                takenImage.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                val imageBytes = baos.toByteArray()
                intent.putExtra("ImageFile", imageBytes)

                //pass fulldirectory information to Result page
                intent.putExtra(EXTRA_FULLDIRECTORY, fullDir)
                intent.putExtra(EXTRA_SUBDIRECTORY, subDir)

                startActivity(intent)
            }else {

                // Tell user to wait
                Toast.makeText(this,"Please take selfie for skin, or check for your history analysis.",Toast.LENGTH_LONG).show()
            }        }
    }

    //to create a file for the selfie
    private fun getPhotoFile(fileName: String): File {
        val storageDirectory = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(fileName, ".png", storageDirectory)
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

    /**
     * Enables https connections
     */
    @SuppressLint("TrulyRandom")
    fun handleSSLHandshake() {
        try {
            val trustAllCerts: Array<TrustManager> =
                arrayOf<TrustManager>(object : X509TrustManager {
                    val acceptedIssuers: Array<Any?>?
                        get() = arrayOfNulls(0)

                    override fun checkClientTrusted(certs: Array<X509Certificate?>?, authType: String?) {}
                    override fun checkServerTrusted(certs: Array<X509Certificate?>?, authType: String?) {}
                    override fun getAcceptedIssuers(): Array<X509Certificate> {
                        TODO("Not yet implemented")
                    }
                })
            val sc: SSLContext = SSLContext.getInstance("SSL")
            sc.init(null, trustAllCerts, SecureRandom())
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory())
            HttpsURLConnection.setDefaultHostnameVerifier(object : HostnameVerifier {
                override fun verify(arg0: String?, arg1: SSLSession?): Boolean {
                    return true
                }
            })
        } catch (ignored: java.lang.Exception) {
        }
    }

    // send http post request to communicate with api and get the response with its header
    fun postImageByVolley(image: Bitmap) {
        val url2: String = "https://10.0.2.2:44374/api/Image"

        //converting image to bytes/base64 string
        val baos = ByteArrayOutputStream()
        image.compress(Bitmap.CompressFormat.PNG, 100, baos)
        val imageBytes = baos.toByteArray() // get data from drawabale

        //sending image to server
        val request2: VolleyMultipartRequest = object : VolleyMultipartRequest(
            Method.POST, url2,
              Response.Listener { response ->
                // Process the json
                try {
                    //textView.text = "Response: $response"
                    responseFromApi = response.toString()

                    // debugging
                    Log.e("Response in takeSelfie-----------", responseFromApi)
                    /**
                     * WAITING FOR CONFIGURE
                     */
                    Toast.makeText(
                        this,
                        "Response: \nPath are posted to API. \n${responseFromApi} \n Acne and Rosacea Photos",
                        Toast.LENGTH_LONG
                    ).show()
                } catch (e: Exception) {
                    Toast.makeText(this, "Exception: $e", Toast.LENGTH_LONG).show()
                }

            },Response.ErrorListener { volleyError ->
                Toast.makeText(
                    this@TakeSelfie,
                    "Some error occurred -> $volleyError",
                    Toast.LENGTH_LONG
                ).show()
                // debugging
                Log.e("Volley Error-----------", "${volleyError.cause}")
                Log.e("Volley Error-----------", "${volleyError.message}")

            }) {

            // setup parameters
            protected open fun getByteData(): MutableMap<String, DataPart> {
                val params: MutableMap<String, DataPart> = HashMap()
                val imageName = System.currentTimeMillis()
                params["image"] = DataPart("$imageName.png", imageBytes)
                return params
            }

            // get the header data
            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                val headers = HashMap<String, String>()
                headers["Content-Type"] = "text/plain; charset=utf-8"
                return headers
            }
        }
        // Add the volley post request to the request queue
        Volley.newRequestQueue(this).add(request2)
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