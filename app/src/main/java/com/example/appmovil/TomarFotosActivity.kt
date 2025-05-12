package com.example.appmovil

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class TomarFotosActivity : AppCompatActivity() {

    private val REQUEST_IMAGE_CAPTURE = 1
    private val REQUEST_CAMERA_PERMISSION = 100
    private lateinit var currentPhotoPath: String
    private val fotos = mutableListOf<Uri>()
    private lateinit var adapter: FotosAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tomar_fotos)

        val mensajeInicial = findViewById<TextView>(R.id.mensajeInicial)
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewFotos)
        val abrirCamaraButton = findViewById<Button>(R.id.btnAbrirCamara)
        val regresarButton = findViewById<Button>(R.id.btnRegresar)
        val descripcionEditText = findViewById<EditText>(R.id.editTextDescripcion)

        adapter = FotosAdapter(fotos.map { uri -> BitmapFactory.decodeFile(uri.path) }.toMutableList())
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.adapter = adapter

        abrirCamaraButton.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA_PERMISSION)
            } else {
                abrirCamara()
            }
        }

        regresarButton.setOnClickListener {
            val descripcion = descripcionEditText.text.toString()
            val paths = ArrayList(fotos.map { it.path ?: "" })
            val intent = Intent().apply {
                putStringArrayListExtra("fotoPaths", paths)
                putExtra("descripcionCaso", descripcion)
            }
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
    }

    private fun abrirCamara() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            val photoFile = createImageFile()
            val photoURI = FileProvider.getUriForFile(
                this,
                "${applicationContext.packageName}.provider",
                photoFile
            )
            currentPhotoPath = photoFile.absolutePath
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        } else {
            Toast.makeText(this, "No se pudo abrir la cámara", Toast.LENGTH_SHORT).show()
        }
    }

    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = File(getExternalFilesDir(null), "Pictures")
        if (!storageDir.exists()) storageDir.mkdirs()
        return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                abrirCamara()
            } else {
                Toast.makeText(this, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            val photoURI = Uri.fromFile(File(currentPhotoPath))
            fotos.add(photoURI)
            adapter.fotos.add(BitmapFactory.decodeFile(currentPhotoPath))
            adapter.notifyItemInserted(fotos.size - 1)
            findViewById<TextView>(R.id.mensajeInicial).text = ""
        }
    }
}
