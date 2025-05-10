package com.example.appmovil

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.*
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
    private val fotoUris = mutableListOf<Uri>() // Lista de URIs para almacenar las fotos
    private lateinit var adapter: FotosAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var mensajeInicial: TextView
    private lateinit var abrirCamaraButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tomar_fotos)

        val nombreUsuario = intent.getStringExtra("nombreUsuario") ?: "Usuario"
        findViewById<TextView>(R.id.usernameTextView).text = nombreUsuario

        mensajeInicial = findViewById(R.id.mensajeInicial)
        recyclerView = findViewById(R.id.recyclerViewFotos)
        abrirCamaraButton = findViewById(R.id.btnAbrirCamara)

        adapter = FotosAdapter(fotos)
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.adapter = adapter

        abrirCamaraButton.setOnClickListener {
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (takePictureIntent.resolveActivity(packageManager) != null) {
                val photoFile = createImageFile()
                val photoURI: Uri = FileProvider.getUriForFile(
                    this,
                    "${applicationContext.packageName}.provider",
                    photoFile
                )
                currentPhotoPath = photoFile.absolutePath
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
            }
        }

        val regresarButton: Button = findViewById(R.id.btnRegresar)
        regresarButton.setOnClickListener {
            finish() // Esto cerrará la actividad y regresará a la anterior.
        }
    }

    // Esta función crea un archivo de imagen único
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File = File(getExternalFilesDir(null), "Pictures")
        if (!storageDir.exists()) {
            storageDir.mkdirs()
        }
        return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir).apply {
            currentPhotoPath = absolutePath
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            val photoURI = Uri.parse(currentPhotoPath) // Obtener la URI
            fotoUris.add(photoURI) // Guardar la URI
            val imageBitmap = BitmapFactory.decodeFile(currentPhotoPath) // Decodificar el archivo en un Bitmap
            fotos.add(imageBitmap) // Agregar el Bitmap a la lista para mostrar en la galería
            adapter.notifyItemInserted(fotos.size - 1)
            mensajeInicial.text = ""  // Ocultar el mensaje cuando haya imágenes
        }
    }

}
