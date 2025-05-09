package com.example.appmovil

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class EmergenciaActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicia la app de llamadas con el número 123
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:123")
        }
        startActivity(intent)

        finish()
    }
}