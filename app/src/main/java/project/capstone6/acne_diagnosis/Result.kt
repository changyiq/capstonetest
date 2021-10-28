package project.capstone6.acne_diagnosis

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.widget.*
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import project.capstone6.acne_diagnosis.databinding.ActivityResultBinding
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.*

class Result : AppCompatActivity() {

    // properties declaration
    private lateinit var binding3: ActivityResultBinding
    private lateinit var btnAgain: Button
    private lateinit var btnExit: Button
    private lateinit var skinProblem: TextView
    private lateinit var hybirdLink1: TextView
    private lateinit var hybirdLink2: TextView
    private lateinit var memeImageView: ImageView
    private lateinit var resultText: TextView
    private lateinit var database: Firebase
    private lateinit var ref: DatabaseReference
    private lateinit var picPath: String
    private lateinit var symptom: String
    private lateinit var linkList: List<String>
    private lateinit var receivedImage: ByteArray
    private lateinit var resultFromResponse: String

    var firebaseAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding3 = ActivityResultBinding.inflate(LayoutInflater.from(this))
        setContentView(binding3.root)

        // initializing
        btnAgain = binding3.btnAgain
        btnExit = binding3.btnExit
        hybirdLink1 = binding3.medicalRtv1
        hybirdLink2 = binding3.medicalRtv2
        symptom = ""
        linkList = listOf()
        skinProblem = binding3.skinProblem
        resultFromResponse = ""

        //Get intent obj
        val intent = getIntent()
        picPath = intent.getStringExtra(TakeSelfie.EXTRA_FULLDIRECTORY).toString()
        receivedImage = intent.getByteArrayExtra("ImageFile")!!
        Log.e("picPath in result---------------", picPath)

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

        // pass image to analyze
        getResultFromVolley(receivedImage)
        getUser()
        loadResult()

    }

    @SuppressLint("SetTextI18n")
    private fun getUser(){
        val user = FirebaseAuth.getInstance().currentUser
        val email = user?.email.toString()

        binding3.tv2.text = "Hi $email"
    }

    @SuppressLint("SetTextI18n")
    private fun loadResult() {
        // Write a message to the database
        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference("Users")

        // get current logged in user
        val user = FirebaseAuth.getInstance().currentUser
        val uid = user?.uid

        // Retrieving result value from textView which is from api
        resultFromResponse = skinProblem.text.toString()

        // code to get the response from api and filter the keyword of the symptom and provide user
        // medical resources
        for(sym in SymptomEnum.values()){
            if (resultFromResponse.contains(sym.symptom)){
                symptom = sym.symptom
            }else{
                //Toast.makeText(this, "Cannot be diagnosed", Toast.LENGTH_SHORT).show()
            }
        }

        //Get column from the table
        // if exists, then fetch the data and update the UI
        myRef.child(uid.toString()).get().addOnSuccessListener {
            if (it.exists()) {
                if (it.child("image").exists()) {
                   //symptom = SymptomEnum.AR.symptom
                    myRef.child(uid.toString()).child("result").setValue(symptom)
                    if (getWebsite(symptom).isNotEmpty()) {
                        hybirdLink1.text = getWebsite(symptom)[0]
                        hybirdLink2.text = getWebsite(symptom)[1]
                        // pass the url info based on the clicked link
                        val intent = Intent(this, Website::class.java)
                        hybirdLink1.setOnClickListener() {
                            intent.putExtra("URL", hybirdLink1.text.toString())
                            startActivity(intent)
                        }
                        hybirdLink2.setOnClickListener() {
                            intent.putExtra("URL", hybirdLink2.text.toString())
                            startActivity(intent)
                        }
                    } else{
                        // if no symptom found, then no medical resources provided, set it to ...
                        hybirdLink1.isClickable = false
                        hybirdLink2.isClickable = false
                        hybirdLink1.text = "..."
                        hybirdLink2.text = "..."
                    }

                }else {
                    skinProblem.text = "You have not made any analysis"
                    Toast.makeText(this, "You have not made any analysis", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Invalid user", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // get website links based on the symptom using enum classes
    private fun getWebsite(sym: String): List<String> {
        when(sym){
            SymptomEnum.AD.symptom -> linkList = listOf(MedicalResourcesEnum.AD.website)
            SymptomEnum.AM.symptom -> linkList = listOf(MedicalResourcesEnum.AM.website)
            SymptomEnum.AR.symptom -> linkList = listOf(MedicalResourcesEnum.AR1.website, MedicalResourcesEnum.AR2.website)
            SymptomEnum.BD.symptom -> linkList = listOf(MedicalResourcesEnum.BD.website)
            SymptomEnum.CI.symptom -> linkList = listOf(MedicalResourcesEnum.CI.website)
            SymptomEnum.EC.symptom -> linkList = listOf(MedicalResourcesEnum.EC.website)
            SymptomEnum.EDE.symptom -> linkList = listOf(MedicalResourcesEnum.EDE.website)
            SymptomEnum.HAIR.symptom -> linkList = listOf(MedicalResourcesEnum.HAIR.website)
            SymptomEnum.HPV.symptom -> linkList = listOf(MedicalResourcesEnum.HPV.website)
            SymptomEnum.PI.symptom -> linkList = listOf(MedicalResourcesEnum.PI.website)
            SymptomEnum.CTD.symptom -> linkList = listOf(MedicalResourcesEnum.CTD.website)
            SymptomEnum.MM.symptom -> linkList = listOf(MedicalResourcesEnum.MM1.website, MedicalResourcesEnum.MM2.website)
            SymptomEnum.NAIL.symptom -> linkList = listOf(MedicalResourcesEnum.NAIL.website)
            SymptomEnum.CD.symptom -> linkList = listOf(MedicalResourcesEnum.CD.website)
            SymptomEnum.PSO.symptom -> linkList = listOf(MedicalResourcesEnum.PSO.website)
            SymptomEnum.SLD.symptom -> linkList = listOf(MedicalResourcesEnum.SLD.website)
            SymptomEnum.SK.symptom -> linkList = listOf(MedicalResourcesEnum.SK.website)
            SymptomEnum.SD.symptom -> linkList = listOf(MedicalResourcesEnum.SD.website)
            SymptomEnum.TRC.symptom -> linkList = listOf(MedicalResourcesEnum.TRC.website)
            SymptomEnum.UH.symptom -> linkList = listOf(MedicalResourcesEnum.UH.website)
            SymptomEnum.VP.symptom -> linkList = listOf(MedicalResourcesEnum.VP.website)
            SymptomEnum.VT.symptom -> linkList = listOf(MedicalResourcesEnum.VT.website)
            SymptomEnum.WM.symptom -> linkList = listOf(MedicalResourcesEnum.WM.website)
        }
        return  linkList
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

    // sending request to get the result response from api
    fun getResultFromVolley(image: ByteArray) {
        val url2: String = "https://10.0.2.2:5001/api/Image"

        // converting to image encoded string
        val imageString = Base64.encodeToString(image, Base64.DEFAULT)

        //fetching image result from server
        val request2: StringRequest = object : StringRequest(
            Method.POST, url2,
            Response.Listener { response ->
                // Process the json
                try {
                    // pass value on UI textView from received result
                    skinProblem.text = response.toString()
                    Toast.makeText(
                        this,
                        "Response: \n$response",
                        Toast.LENGTH_LONG
                    ).show()
                } catch (e: Exception) {
                    Toast.makeText(this, "Exception: $e", Toast.LENGTH_LONG).show()
                }

            },Response.ErrorListener { volleyError ->
                Toast.makeText(
                    this,
                    "Some error occurred -> $volleyError",
                    Toast.LENGTH_LONG
                ).show()
                Log.e("Volley Error-----------", "${volleyError.cause}")
                Log.e("Volley Error-----------", "${volleyError.message}")

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
