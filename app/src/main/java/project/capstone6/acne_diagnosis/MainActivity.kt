package project.capstone6.acne_diagnosis

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import project.capstone6.acne_diagnosis.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding1: ActivityMainBinding
    private lateinit var btnLogin: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding1 = ActivityMainBinding.inflate(LayoutInflater.from(this))
        setContentView(binding1.root)

        btnLogin = binding1.btnLogin

        btnLogin.setOnClickListener {

            val intent1 = Intent(this,TakeSelfie::class.java)
            startActivity(intent1)
        }
    }


}