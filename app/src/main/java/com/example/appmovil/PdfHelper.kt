import android.content.Context
import android.graphics.Bitmap
import android.os.Environment
import android.widget.Toast
import com.itextpdf.text.*
import com.itextpdf.text.pdf.PdfWriter
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import com.itextpdf.text.pdf.PdfPTable
import com.example.appmovil.R


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

        // Logo desde drawable
        try {
            val drawable = context.getDrawable(R.drawable.sinbog)
            val bitmap = (drawable as android.graphics.drawable.BitmapDrawable).bitmap
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            val image = Image.getInstance(stream.toByteArray())
            image.scaleToFit(60f, 60f)
            image.alignment = Image.ALIGN_LEFT
            documento.add(image)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Encabezado
        val encabezado = Paragraph("Documento generado por SinBog", Font(Font.FontFamily.HELVETICA, 12f, Font.BOLD))
        documento.add(encabezado)
        documento.add(Paragraph(" "))

        // Título
        val titulo = Paragraph("Reporte de Siniestro", Font(Font.FontFamily.HELVETICA, 20f, Font.BOLD))
        titulo.alignment = Element.ALIGN_CENTER
        documento.add(titulo)
        documento.add(Paragraph(" "))

        // Fecha
        val fecha = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date())
        documento.add(Paragraph("Fecha de creación: $fecha"))
        documento.add(Paragraph(" "))

        // Tabla con información
        val tabla = PdfPTable(2)
        tabla.widthPercentage = 100f
        tabla.setWidths(floatArrayOf(1f, 2f))

        fun agregarFila(titulo: String, valor: String?) {
            tabla.addCell(Phrase(titulo, Font(Font.FontFamily.HELVETICA, 12f, Font.BOLD)))
            tabla.addCell(Phrase(valor ?: "No disponible", Font(Font.FontFamily.HELVETICA, 12f)))
        }

        agregarFila("Usuario", nombreUsuario)
        agregarFila("Llamada a aseguradora", if (llamadaAseguradora) "Sí" else "No")
        agregarFila("Hora llamada aseguradora", horaLlamadaAseguradora)
        agregarFila("Llamada a emergencias", if (llamadaEmergencias) "Sí" else "No")
        agregarFila("Hora llamada emergencias", horaLlamadaEmergencias)
        agregarFila("Descripción", descripcion)

        documento.add(tabla)
        documento.add(Paragraph(" "))

        // Fotos
        val fotosTitulo = Paragraph("Fotos del caso:", Font(Font.FontFamily.HELVETICA, 14f, Font.BOLD))
        documento.add(fotosTitulo)
        documento.add(Paragraph(" "))

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

        Toast.makeText(context, "PDF guardado en: ${pdfFile.absolutePath}", Toast.LENGTH_LONG).show()
        return pdfFile
    }

}
