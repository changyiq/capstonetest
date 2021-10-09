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
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.FileProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.storage.FirebaseStorage
import project.capstone6.acne_diagnosis.databinding.ActivityTakeSelfieBinding
import java.io.File
import java.util.*

private const val FILE_NAME ="selfie"
class TakeSelfie : AppCompatActivity() {

    private lateinit var binding2: ActivityTakeSelfieBinding
    private lateinit var btnTakeSelfie : Button
    private lateinit var btnDiagnosis : Button
    private lateinit var imageView: ImageView
    private lateinit var ImageUri: Uri
    private lateinit var photoFile: File
    private lateinit var fileProvider: Uri

    private var subDir: String =""

    companion object {
        const val REQUEST_FROM_CAMERA = 1001
        const val EXTRA_SUBDIRECTORY = "SavedSubdirectory"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding2 = ActivityTakeSelfieBinding.inflate(LayoutInflater.from(this))
        setContentView(binding2.root)

        btnTakeSelfie = binding2.btnTakeSelfie
        btnDiagnosis = binding2.btnDiagnosis
        imageView = binding2.imageView

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
                startActivity(intent)

                //pass subdirectory information to Result page
                intent.putExtra(EXTRA_SUBDIRECTORY,subDir)
                Toast.makeText(this,"Subdir is " + subDir,Toast.LENGTH_LONG).show()
                //trigger and pass subdirectory to REST API

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
        } else{
            super.onActivityResult(requestCode, resultCode, data)
        }
    }
}