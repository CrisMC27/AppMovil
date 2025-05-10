package com.example.appmovil

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class TomarFotosActivity : AppCompatActivity() {

    private val REQUEST_IMAGE_CAPTURE = 1
    private lateinit var currentPhotoPath: String
    private val fotos = mutableListOf<Bitmap>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tomar_fotos)

        val btnAbrirCamara = findViewById<Button>(R.id.btnAbrirCamara)
        val btnRegresar = findViewById<Button>(R.id.btnRegresar)
        val editTextDescripcion = findViewById<EditText>(R.id.editTextDescripcion)
        val recyclerViewFotos = findViewById<RecyclerView>(R.id.recyclerViewFotos)

        // Configurar el RecyclerView
        val adapter = FotoAdapter(fotos)
        recyclerViewFotos.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerViewFotos.adapter = adapter

        btnAbrirCamara.setOnClickListener {
            dispatchTakePictureIntent()
        }

        btnRegresar.setOnClickListener {
            val descripcion = editTextDescripcion.text.toString()
            val resultIntent = Intent()
            resultIntent.putExtra("descripcion", descripcion)
            // Si deseas pasar las fotos, deberías guardarlas y pasar URIs
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }
    }

    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            val photoFile: File? = try {
                createImageFile()
            } catch (ex: Exception) {
                null
            }
            photoFile?.also {
                val photoURI: Uri = FileProvider.getUriForFile(
                    this,
                    "${applicationContext.packageName}.provider",
                    it
                )
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
            }
        }
    }

    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File = getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir).apply {
            currentPhotoPath = absolutePath
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            val imageBitmap = MediaStore.Images.Media.getBitmap(contentResolver, Uri.fromFile(File(currentPhotoPath)))
            fotos.add(imageBitmap)
            findViewById<RecyclerView>(R.id.recyclerViewFotos).adapter?.notifyDataSetChanged()
        }
    }
}
