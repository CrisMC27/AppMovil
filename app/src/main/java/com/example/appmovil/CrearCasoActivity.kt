package com.example.appmovil

import PdfHelper
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.File
import java.io.FileInputStream
import java.text.SimpleDateFormat
import java.util.*

class CrearCasoActivity : AppCompatActivity() {

    private lateinit var contactarAseguradoraButton: Button
    private lateinit var contactarEmergenciasButton: Button
    private lateinit var tomarFotosButton: Button
    private lateinit var generarDocumentoButton: Button
    private lateinit var guardarCasoButton: Button
    private lateinit var salirSinGuardarButton: Button
    private lateinit var compartirButton: Button

    private var llamadaAseguradoraRealizada = false
    private var horaLlamadaAseguradora: String? = null

    private var llamadaEmergenciasRealizada = false
    private var horaLlamadaEmergencias: String? = null

    private val fotos = mutableListOf<Bitmap>()
    private lateinit var nombreUsuario: String
    private lateinit var descripcion: String

    private var pdfFilePath: String? = null

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
        compartirButton = findViewById(R.id.btnCompartir)

        compartirButton.isEnabled = false

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
                llamadaEmergenciasRealizada && horaLlamadaEmergencias != null
            ) {
                Toast.makeText(this, "Generando documento PDF...", Toast.LENGTH_SHORT).show()

                val pdfHelper = PdfHelper()
                val pdfFile = pdfHelper.generarPDFConFotos(
                    fotos,
                    llamadaAseguradoraRealizada,
                    horaLlamadaAseguradora,
                    llamadaEmergenciasRealizada,
                    horaLlamadaEmergencias,
                    this,
                    nombreUsuario,
                    descripcion
                )

                pdfFilePath = pdfFile.absolutePath
                compartirButton.isEnabled = true
            } else {
                Toast.makeText(this, "Faltan datos para generar el documento", Toast.LENGTH_SHORT).show()
            }
        }

        compartirButton.setOnClickListener {
            pdfFilePath?.let { path ->
                val file = File(path)

                val fileUri: Uri = FileProvider.getUriForFile(
                    this,
                    "${applicationContext.packageName}.provider",
                    file
                )

                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/pdf"
                    putExtra(Intent.EXTRA_STREAM, fileUri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                startActivity(Intent.createChooser(shareIntent, "Compartir documento PDF"))
            } ?: run {
                Toast.makeText(this, "No se ha generado el documento aún", Toast.LENGTH_SHORT).show()
            }
        }

        guardarCasoButton.setOnClickListener {
            if (pdfFilePath == null) {
                Toast.makeText(this, "Genera primero el documento PDF", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Guardar el PDF en almacenamiento local (Descargas)
            guardarPDFEnDescargas()

            // Subir el PDF a Firebase Storage y registrar en Firestore
            subirPDFAFirebase(pdfFilePath!!)
        }

        salirSinGuardarButton.setOnClickListener {
            Toast.makeText(this, "Saliendo sin guardar", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 101 && resultCode == RESULT_OK) {
            val paths = data?.getStringArrayListExtra("fotoPaths") ?: return
            descripcion = data.getStringExtra("descripcionCaso") ?: ""

            for (path in paths) {
                val bitmap = BitmapFactory.decodeFile(path)
                fotos.add(bitmap)
            }

            Toast.makeText(this, "Fotos recibidas: ${fotos.size}\nDescripción: $descripcion", Toast.LENGTH_LONG).show()
        }
    }

    private fun obtenerHoraActual(): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        return sdf.format(Date())
    }

    private fun guardarPDFEnDescargas() {
        val inputFile = File(pdfFilePath!!)
        val fileName = inputFile.name
        val resolver = contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
        }

        val collection: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10+ (API 29+)
            contentValues.put(MediaStore.Downloads.IS_PENDING, 1)
            MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            // Android 9 y anteriores: usar MediaStore.Files con la carpeta de descargas manual
            MediaStore.Files.getContentUri("external")
        }

        val uri = resolver.insert(collection, contentValues)

        if (uri != null) {
            resolver.openOutputStream(uri).use { outputStream ->
                FileInputStream(inputFile).use { inputStream ->
                    inputStream.copyTo(outputStream!!)
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(MediaStore.Downloads.IS_PENDING, 0)
                resolver.update(uri, contentValues, null, null)
            }

            Toast.makeText(this, "Documento guardado en Descargas", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, "Error al guardar en Descargas", Toast.LENGTH_SHORT).show()
        }
    }

    private fun subirPDFAFirebase(pdfFilePath: String) {
        val inputFile = File(pdfFilePath)
        val fileName = inputFile.name

        // Obtener el UID del usuario autenticado
        val userUid = FirebaseAuth.getInstance().currentUser?.uid ?: "unknown_user"

        if (userUid == "unknown_user") {
            Toast.makeText(this, "No hay usuario autenticado", Toast.LENGTH_SHORT).show()
            return
        }

        // Referencia a Firebase Storage
        val storageRef = FirebaseStorage.getInstance().reference
        val pdfRef = storageRef.child("documentos/$userUid/$fileName")

        // Subir el archivo a Firebase Storage
        pdfRef.putFile(Uri.fromFile(inputFile))
            .addOnSuccessListener {
                // Obtener la URL de descarga del archivo
                pdfRef.downloadUrl.addOnSuccessListener { uri ->
                    // Guardar la URL en Firestore
                    guardarCasoEnFirestore(uri.toString(), userUid)
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error al subir el PDF: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun guardarCasoEnFirestore(pdfUrl: String, userUid: String) {
        val firestore = FirebaseFirestore.getInstance()

        val casoData = hashMapOf(
            "usuario" to nombreUsuario,
            "uid" to userUid,
            "fecha" to obtenerHoraActual(),
            "llamadaAseguradora" to llamadaAseguradoraRealizada,
            "horaLlamadaAseguradora" to horaLlamadaAseguradora,
            "llamadaEmergencias" to llamadaEmergenciasRealizada,
            "horaLlamadaEmergencias" to horaLlamadaEmergencias,
            "descripcion" to descripcion,
            "urlDocumentoPDF" to pdfUrl
        )

        // Guardar el documento en la colección "casos"
        firestore.collection("casos")
            .add(casoData)
            .addOnSuccessListener {
                Toast.makeText(this, "Caso guardado exitosamente", Toast.LENGTH_SHORT).show()
                finish()  // Puedes finalizar la actividad si deseas
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error al guardar el caso: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
