import android.content.Context
import android.graphics.Bitmap
import android.os.Environment
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.itextpdf.text.*
import com.itextpdf.text.pdf.PdfWriter
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class PdfHelper {

    fun generarPDFConFotos(
        fotos: List<Bitmap>,
        llamadaAseguradora: Boolean,
        horaLlamadaAseguradora: String?,
        llamadaEmergencias: Boolean,
        horaLlamadaEmergencias: String?,
        context: Context,
        nombreUsuario: String,
        descripcion: String
    ): File {
        val documento = Document(PageSize.A4)

        val pdfFile = File(
            context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
            "documento_caso_${System.currentTimeMillis()}.pdf"
        )

        val outputStream = FileOutputStream(pdfFile)
        PdfWriter.getInstance(documento, outputStream)
        documento.open()

        // Título
        val titulo = Paragraph("Reporte de Siniestro", Font(Font.FontFamily.HELVETICA, 20f, Font.BOLD))
        titulo.alignment = Element.ALIGN_CENTER
        documento.add(titulo)
        documento.add(Paragraph(" "))

        // Fecha
        val fecha = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date())
        documento.add(Paragraph("Fecha de creación: $fecha"))

        // Datos de llamada
        documento.add(Paragraph("Llamada a aseguradora: ${if (llamadaAseguradora) "Sí" else "No"}"))
        horaLlamadaAseguradora?.let {
            documento.add(Paragraph("Hora de llamada a aseguradora: $it"))
        }

        documento.add(Paragraph("Llamada a emergencias: ${if (llamadaEmergencias) "Sí" else "No"}"))
        horaLlamadaEmergencias?.let {
            documento.add(Paragraph("Hora de llamada a emergencias: $it"))
        }

        documento.add(Paragraph(" "))

        // Descripción
        val descripcionTitulo = Paragraph("Descripción del caso:", Font(Font.FontFamily.HELVETICA, 14f, Font.BOLD))
        documento.add(descripcionTitulo)
        documento.add(Paragraph(descripcion))
        documento.add(Paragraph(" "))

        // Fotos
        for (foto in fotos) {
            val byteArrayOutputStream = ByteArrayOutputStream()
            foto.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
            val byteArray = byteArrayOutputStream.toByteArray()
            val image: Image = Image.getInstance(byteArray)
            image.scaleToFit(500f, 500f)
            documento.add(image)
            documento.add(Paragraph(" "))
        }

        documento.close()

        // Confirmación visual
        Toast.makeText(context, "PDF guardado en: ${pdfFile.absolutePath}", Toast.LENGTH_LONG).show()

        // Verificar autenticación
        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser
        val userUid = user?.uid ?: run {
            Toast.makeText(context, "No hay usuario autenticado.", Toast.LENGTH_SHORT).show()
            return pdfFile
        }

        // Subida a Firebase Storage
        val storage: FirebaseStorage = FirebaseStorage.getInstance()
        val storageReference: StorageReference = storage.reference
        val pdfRef: StorageReference = storageReference.child("documentos/$userUid/${pdfFile.name}")

        val inputStream = pdfFile.inputStream()
        pdfRef.putStream(inputStream)
            .addOnSuccessListener {
                pdfRef.downloadUrl.addOnSuccessListener { uri ->
                    // Guardar en Firestore
                    val firestore = FirebaseFirestore.getInstance()
                    val casoData = hashMapOf(
                        "usuario" to nombreUsuario,
                        "uid" to userUid,
                        "fecha" to fecha,
                        "llamadaAseguradora" to llamadaAseguradora,
                        "horaLlamadaAseguradora" to horaLlamadaAseguradora,
                        "llamadaEmergencias" to llamadaEmergencias,
                        "horaLlamadaEmergencias" to horaLlamadaEmergencias,
                        "descripcion" to descripcion,
                        "urlDocumentoPDF" to uri.toString()
                    )

                    firestore.collection("casos").add(casoData)
                        .addOnSuccessListener {
                            Toast.makeText(context, "Caso guardado correctamente en Firebase", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(context, "Error al guardar caso: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Error al subir archivo: ${it.message}", Toast.LENGTH_SHORT).show()
            }

        return pdfFile
    }
}
