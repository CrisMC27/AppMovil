package com.example.appmovil

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class AseguradorasActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_aseguradoras)

        // Mostrar el nombre del usuario en el TextView
        val nombreUsuario = intent.getStringExtra("nombreUsuario") ?: "Usuario"
        val usernameTextView = findViewById<TextView>(R.id.usernameTextView)
        usernameTextView.text = nombreUsuario

        // Botones de aseguradoras
        val aseguradoras = mapOf(
            R.id.btnAxa to "Axa Colpatria",
            R.id.btnMapfre to "Mapfre",
            R.id.btnAllianz to "Allianz",
            R.id.btnBolivar to "Seguros Bolívar",
            R.id.btnMundial to "Seguros Mundial",
            R.id.btnSura to "Sura"
        )

        aseguradoras.forEach { (buttonId, nombre) ->
            findViewById<Button>(buttonId).setOnClickListener {
                //val intent = Intent(this, PreguntaImagenesActivity::class.java)
                intent.putExtra("aseguradora", nombre)
                intent.putExtra("nombreUsuario", nombreUsuario) // Si deseas seguir pasándolo
                startActivity(intent)
            }
        }

        // Botón regresar
        val btnRegresar = findViewById<Button>(R.id.backButton)
        btnRegresar.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }
    }
}
