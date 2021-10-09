package project.capstone6.acne_diagnosis

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import project.capstone6.acne_diagnosis.databinding.ActivityResultBinding

class Result : AppCompatActivity() {

    private lateinit var binding3:ActivityResultBinding
    private lateinit var btnAgain:Button
    private lateinit var btnExit:Button
    private lateinit var tvResult: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding3 = ActivityResultBinding.inflate(LayoutInflater.from(this))
        setContentView(binding3.root)

        btnAgain = binding3.btnAgain
        btnExit = binding3.btnExit
        tvResult = binding3.tvResult

        //Get subdirectory
        val intent = getIntent()
        val subDir= intent.getStringArrayExtra(TakeSelfie.EXTRA_SUBDIRECTORY)
        Toast.makeText(this,"Subdir is " + subDir, Toast.LENGTH_LONG).show()
        tvResult.text = "Diagnosing..."

        btnAgain.setOnClickListener {

            val intent3 = Intent(this,TakeSelfie::class.java)
            startActivity(intent3)
        }

        btnExit.setOnClickListener {

        }
    }
}