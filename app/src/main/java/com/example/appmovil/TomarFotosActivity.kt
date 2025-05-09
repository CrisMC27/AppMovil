package com.example.appmovil

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class TomarFotosActivity : AppCompatActivity() {

    private val REQUEST_IMAGE_CAPTURE = 1
    private val fotos = mutableListOf<Bitmap>()
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
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as? Bitmap
            imageBitmap?.let {
                fotos.add(it)
                adapter.notifyItemInserted(fotos.size - 1)
                mensajeInicial.text = ""  // Oculta el mensaje cuando hay imágenes
            }
        }
    }
}
