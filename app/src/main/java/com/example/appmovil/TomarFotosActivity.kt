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
    private val fotoUris = mutableListOf<Uri>()
    private lateinit var adapter: FotosAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var mensajeInicial: TextView
    private lateinit var abrirCamaraButton: Button
    private lateinit var descripcionEditText: EditText  // NUEVO

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tomar_fotos)

        val nombreUsuario = intent.getStringExtra("nombreUsuario") ?: "Usuario"
        findViewById<TextView>(R.id.usernameTextView).text = nombreUsuario

        mensajeInicial = findViewById(R.id.mensajeInicial)
        recyclerView = findViewById(R.id.recyclerViewFotos)
        abrirCamaraButton = findViewById(R.id.btnAbrirCamara)
        descripcionEditText = findViewById(R.id.editTextDescripcion)  // NUEVO

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
            val descripcion = descripcionEditText.text.toString()
            val paths = fotoUris.map { it.path ?: "" }

            val resultIntent = Intent()
            resultIntent.putStringArrayListExtra("fotoPaths", ArrayList(paths))
            resultIntent.putExtra("descripcionCaso", descripcion)

            setResult(RESULT_OK, resultIntent)
            finish()
        }
    }

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
            val photoURI = Uri.fromFile(File(currentPhotoPath))
            fotoUris.add(photoURI)
            val imageBitmap = BitmapFactory.decodeFile(currentPhotoPath)
            fotos.add(imageBitmap)
            adapter.notifyItemInserted(fotos.size - 1)
            mensajeInicial.text = ""
        }
    }
}

