package project.capstone6.acne_diagnosis

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import project.capstone6.acne_diagnosis.databinding.ActivityTakeSelfieBinding

class TakeSelfie : AppCompatActivity() {

    private lateinit var binding2: ActivityTakeSelfieBinding
    private lateinit var btnTakeSelfie : Button
    private lateinit var btnDiagnosis : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding2 = ActivityTakeSelfieBinding.inflate(LayoutInflater.from(this))
        setContentView(binding2.root)

        btnTakeSelfie = binding2.btnTakeSelfie
        btnDiagnosis = binding2.btnDiagnosis

        btnDiagnosis.setOnClickListener {

            val intent2 = Intent(this, Result::class.java)
            startActivity(intent2)
        }
    }
}