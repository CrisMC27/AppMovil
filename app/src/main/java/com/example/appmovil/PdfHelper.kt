package com.example.appmovil

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
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
        nombreUsuario: String
    ): File {
        val documento = Document(PageSize.A4)
        val pdfFile = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "documento_caso.pdf")
        val outputStream = FileOutputStream(pdfFile)
        PdfWriter.getInstance(documento, outputStream)
        documento.open()

        documento.add(Paragraph("Reporte de Siniestro"))
        documento.add(Paragraph("Fecha de creación: ${System.currentTimeMillis()}"))
        documento.add(Paragraph("Llamada a aseguradora: ${if (llamadaAseguradora) "Sí" else "No"}"))
        horaLlamadaAseguradora?.let {
            documento.add(Paragraph("Hora de llamada a aseguradora: $it"))
        }
        documento.add(Paragraph("Llamada a emergencias: ${if (llamadaEmergencias) "Sí" else "No"}"))
        horaLlamadaEmergencias?.let {
            documento.add(Paragraph("Hora de llamada a emergencias: $it"))
        }

        for (foto in fotos) {
            val byteArrayOutputStream = ByteArrayOutputStream()
            foto.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
            val byteArray = byteArrayOutputStream.toByteArray()
            val image: Image = Image.getInstance(byteArray)
            image.scaleToFit(500f, 500f)
            documento.add(image)
        }

        documento.close()

        // Subir a Firebase Storage
        val storage: FirebaseStorage = FirebaseStorage.getInstance()
        val storageReference: StorageReference = storage.reference
        val pdfRef: StorageReference = storageReference.child("documentos/${pdfFile.name}")

        pdfRef.putFile(Uri.fromFile(pdfFile))
            .addOnSuccessListener {
                pdfRef.downloadUrl.addOnSuccessListener { uri ->
                    // Guardar en Firestore
                    val firestore = FirebaseFirestore.getInstance()
                    val casoData = hashMapOf(
                        "usuario" to nombreUsuario,
                        "fecha" to SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date()),
                        "llamadaAseguradora" to llamadaAseguradora,
                        "horaLlamadaAseguradora" to horaLlamadaAseguradora,
                        "llamadaEmergencias" to llamadaEmergencias,
                        "horaLlamadaEmergencias" to horaLlamadaEmergencias,
                        "urlDocumentoPDF" to uri.toString()
                    )

                    firestore.collection("casos").add(casoData)
                        .addOnSuccessListener {
                            println("Caso registrado exitosamente")
                        }
                        .addOnFailureListener { e ->
                            println("Error al registrar el caso: ${e.message}")
                        }
                }
            }
            .addOnFailureListener {
                println("Error al subir el archivo: ${it.message}")
            }

        return pdfFile // Este retorno debería ser un archivo File válido
    }
}