package com.example.appmovil

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class CrearCasoActivity : AppCompatActivity() {

    private lateinit var contactarAseguradoraButton: Button
    private lateinit var contactarEmergenciasButton: Button
    private lateinit var tomarFotosButton: Button
    private lateinit var generarDocumentoButton: Button
    private lateinit var guardarCasoButton: Button
    private lateinit var salirSinGuardarButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crear_caso)

        contactarAseguradoraButton = findViewById(R.id.btnContactarAseguradora)
        contactarEmergenciasButton = findViewById(R.id.btnContactarEmergencias)
        tomarFotosButton = findViewById(R.id.btnTomarFotos)
        generarDocumentoButton = findViewById(R.id.btnGenerarDocumento)
        guardarCasoButton = findViewById(R.id.btnGuardarCaso)
        salirSinGuardarButton = findViewById(R.id.btnSalirSinGuardar)

        contactarAseguradoraButton.setOnClickListener {
            val intent = Intent(this, AseguradorasActivity::class.java)
            intent.putExtra("nombreUsuario", "UsuarioEjemplo")
            startActivity(intent)
        }

        contactarEmergenciasButton.setOnClickListener {
            Toast.makeText(this, "Llamando a emergencias...", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, EmergenciaActivity::class.java)
            startActivity(intent)
        }

        tomarFotosButton.setOnClickListener {
            Toast.makeText(this, "Abriendo cámara...", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, TomarFotosActivity::class.java)
            startActivity(intent)
        }


        generarDocumentoButton.setOnClickListener {
            Toast.makeText(this, "Generando documento PDF...", Toast.LENGTH_SHORT).show()
            // Lógica para generar el documento
        }

        guardarCasoButton.setOnClickListener {
            Toast.makeText(this, "Caso guardado", Toast.LENGTH_SHORT).show()
            // Guardar información del caso
            finish()
        }

        salirSinGuardarButton.setOnClickListener {
            Toast.makeText(this, "Saliendo sin guardar", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
