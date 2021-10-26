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
    private lateinit var hybirdLink1: TextView
    private lateinit var hybirdLink2: TextView
    private lateinit var memeImageView: ImageView
    private lateinit var resultText: TextView
    private lateinit var database: Firebase
    private lateinit var ref: DatabaseReference
    private lateinit var picPath: String
    private lateinit var symptom: String
    private lateinit var linkList: List<String>

    var receivedResult: String = ""

    var firebaseAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding3 = ActivityResultBinding.inflate(LayoutInflater.from(this))
        setContentView(binding3.root)


        btnAgain = binding3.btnAgain
        btnExit = binding3.btnExit
        hybirdLink1 = binding3.medicalRtv1
        hybirdLink2 = binding3.medicalRtv2
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

//        // code to get the response from api and filter the keyword of the symptom and provide user
//        // medical resources
//        receivedResult = "Acne and Rosacea photos"
//        for(sym in SymptomEnum.values()){
//            if (receivedResult.contains(sym.symptom)){
//                symptom = sym.symptom
//            }
//        }

        //Get column from the table
        // if exists, then fetch the data and update the UI
        myRef.child(uid.toString()).get().addOnSuccessListener {
            if (it.exists()) {
                if (it.child("image").exists()) {
                    symptom = SymptomEnum.AR.symptom
                    myRef.child(uid.toString()).child("result").setValue(symptom)
                    hybirdLink1.text = getWebsite(symptom)[0]
                    hybirdLink2.text = getWebsite(symptom)[1]

                    // pass the url info based on the clicked link
                    val intent = Intent(this, Website::class.java)
                    hybirdLink1.setOnClickListener(){
                        intent.putExtra("URL", hybirdLink1.text.toString())
                        startActivity(intent)
                    }
                    hybirdLink2.setOnClickListener(){
                        intent.putExtra("URL", hybirdLink2.text.toString())
                        startActivity(intent)
                    }
                    true
                }else {
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
