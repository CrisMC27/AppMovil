package com.example.appmovil

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*
import android.net.Uri

class CrearCasoActivity : AppCompatActivity() {

    private lateinit var contactarAseguradoraButton: Button
    private lateinit var contactarEmergenciasButton: Button
    private lateinit var tomarFotosButton: Button
    private lateinit var generarDocumentoButton: Button
    private lateinit var guardarCasoButton: Button
    private lateinit var salirSinGuardarButton: Button
    private lateinit var compartirButton: Button  // Botón para compartir

    private var llamadaAseguradoraRealizada = false
    private var horaLlamadaAseguradora: String? = null

    private var llamadaEmergenciasRealizada = false
    private var horaLlamadaEmergencias: String? = null

    private val fotos = mutableListOf<Bitmap>() // Lista de fotos tomadas
    private lateinit var nombreUsuario: String
    private lateinit var descripcion: String  // Para almacenar la descripción del caso

    private var pdfFilePath: String? = null // Variable para almacenar la ruta del archivo PDF

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crear_caso)

        nombreUsuario = intent.getStringExtra("nombreUsuario") ?: "Usuario"

        contactarAseguradoraButton = findViewById(R.id.btnContactarAseguradora)
        contactarEmergenciasButton = findViewById(R.id.btnContactarEmergencias)
        tomarFotosButton = findViewById(R.id.btnTomarFotos)
        generarDocumentoButton = findViewById(R.id.btnGenerarDocumento)
        guardarCasoButton = findViewById(R.id.btnGuardarCaso)
        salirSinGuardarButton = findViewById(R.id.btnSalirSinGuardar)
        compartirButton = findViewById(R.id.btnCompartir)  // Inicializa el botón de compartir

        compartirButton.isEnabled = false // El botón de compartir estará deshabilitado al principio

        contactarAseguradoraButton.setOnClickListener {
            llamadaAseguradoraRealizada = true
            horaLlamadaAseguradora = obtenerHoraActual()
            val intent = Intent(this, AseguradorasActivity::class.java)
            intent.putExtra("nombreUsuario", nombreUsuario)
            startActivity(intent)
        }

        contactarEmergenciasButton.setOnClickListener {
            llamadaEmergenciasRealizada = true
            horaLlamadaEmergencias = obtenerHoraActual()
            Toast.makeText(this, "Llamando a emergencias...", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, EmergenciaActivity::class.java)
            startActivity(intent)
        }

        tomarFotosButton.setOnClickListener {
            val intent = Intent(this, TomarFotosActivity::class.java)
            startActivityForResult(intent, 101)
        }

        generarDocumentoButton.setOnClickListener {
            if (fotos.isEmpty()) {
                Toast.makeText(this, "No hay fotos para generar el documento", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (llamadaAseguradoraRealizada && horaLlamadaAseguradora != null &&
                llamadaEmergenciasRealizada && horaLlamadaEmergencias != null) {

                Toast.makeText(this, "Generando documento PDF...", Toast.LENGTH_SHORT).show()

                val pdfHelper = PdfHelper()  // Asegúrate de haber importado correctamente PdfHelper
                val pdfFile = pdfHelper.generarPDFConFotos(
                    fotos,
                    llamadaAseguradoraRealizada,
                    horaLlamadaAseguradora,
                    llamadaEmergenciasRealizada,
                    horaLlamadaEmergencias,
                    this,
                    nombreUsuario
                )

                // Guardamos la ruta del archivo PDF generado
                pdfFilePath = pdfFile.absolutePath

                // Habilitar el botón de compartir después de generar el PDF
                compartirButton.isEnabled = true
            } else {
                Toast.makeText(this, "Faltan datos para generar el documento", Toast.LENGTH_SHORT).show()
            }
        }

        compartirButton.setOnClickListener {
            // Asegúrate de que pdfFilePath no sea nulo antes de intentar compartirlo
            pdfFilePath?.let { path ->
                val fileUri = Uri.parse("file://$path") // Asegúrate de que pdfFilePath es la ruta correcta
                val intent = Intent(Intent.ACTION_SEND)
                intent.type = "application/pdf"
                intent.putExtra(Intent.EXTRA_STREAM, fileUri)
                startActivity(Intent.createChooser(intent, "Compartir documento"))
            } ?: run {
                Toast.makeText(this, "No se ha generado el documento aún", Toast.LENGTH_SHORT).show()
            }
        }

        guardarCasoButton.setOnClickListener {
            Toast.makeText(this, "Caso guardado", Toast.LENGTH_SHORT).show()
            finish()
        }

        salirSinGuardarButton.setOnClickListener {
            Toast.makeText(this, "Saliendo sin guardar", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    // Este método recibe los resultados de la actividad TomarFotosActivity
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 101 && resultCode == RESULT_OK) {
            val paths = data?.getStringArrayListExtra("fotoPaths") ?: return
            descripcion = data?.getStringExtra("descripcion") ?: ""  // Obtener la descripción

            for (path in paths) {
                val bitmap = BitmapFactory.decodeFile(path)
                fotos.add(bitmap)  // Agregar la foto a la lista de fotos
            }

            Toast.makeText(this, "Fotos recibidas: ${fotos.size}\nDescripción: $descripcion", Toast.LENGTH_LONG).show()
        }
    }

    private fun obtenerHoraActual(): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        return sdf.format(Date())
    }
}
