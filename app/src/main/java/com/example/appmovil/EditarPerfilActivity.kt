package com.example.appmovil

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class EditarPerfilActivity : AppCompatActivity() {

    private lateinit var nombreEditText: EditText
    private lateinit var apellidosEditText: EditText
    private lateinit var correoEditText: EditText
    private lateinit var edadEditText: EditText
    private lateinit var sexoSpinner: Spinner
    private lateinit var guardarSalirButton: Button
    private lateinit var regresarButton: Button
    private lateinit var logoImageView: ImageView

    private var originalNombre: String = ""
    private var originalApellidos: String = ""
    private var originalCorreo: String = ""
    private var originalEdad: String = ""
    private var originalSexo: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editar_perfil)

        // Inicializar vistas
        nombreEditText = findViewById(R.id.nombreEditText)
        apellidosEditText = findViewById(R.id.apellidosEditText)
        correoEditText = findViewById(R.id.correoEditText)
        edadEditText = findViewById(R.id.edadEditText)
        sexoSpinner = findViewById(R.id.sexoSpinner)
        guardarSalirButton = findViewById(R.id.guardarSalirButton)
        regresarButton = findViewById(R.id.regresarButton)
        logoImageView = findViewById(R.id.logoImageView)

        // Spinner de sexo
        ArrayAdapter.createFromResource(
            this,
            R.array.sexos_array, // asegúrate de tener este array en strings.xml
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            sexoSpinner.adapter = adapter
        }

        // Obtener usuario actual
        val usuario = FirebaseAuth.getInstance().currentUser
        val uid = usuario?.uid

        if (uid != null) {
            val db = FirebaseFirestore.getInstance()
            // Cambié la colección de 'usuarios' a 'users'
            val usuarioRef = db.collection("users").document(uid)

            usuarioRef.get().addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val nombre = doc.getString("nombre") ?: ""
                    val apellidos = doc.getString("apellidos") ?: ""
                    val correo = doc.getString("correo") ?: ""
                    val edad = doc.getString("edad") ?: ""
                    val sexo = doc.getString("sexo") ?: ""

                    nombreEditText.setText(nombre)
                    apellidosEditText.setText(apellidos)
                    correoEditText.setText(correo)
                    edadEditText.setText(edad)
                    val sexoIndex = (sexoSpinner.adapter as ArrayAdapter<String>).getPosition(sexo)
                    sexoSpinner.setSelection(sexoIndex)

                    // Guardar originales
                    originalNombre = nombre
                    originalApellidos = apellidos
                    originalCorreo = correo
                    originalEdad = edad
                    originalSexo = sexo
                }
            }
        }

        // Inicialmente deshabilitar el botón "Guardar y salir"
        guardarSalirButton.isEnabled = false

        // Detectar cambios en los campos
        val watcher = object : android.text.TextWatcher {
            override fun afterTextChanged(s: android.text.Editable?) {
                verificarCambios()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }

        nombreEditText.addTextChangedListener(watcher)
        apellidosEditText.addTextChangedListener(watcher)
        correoEditText.addTextChangedListener(watcher)
        edadEditText.addTextChangedListener(watcher)
        sexoSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: android.view.View?,
                position: Int,
                id: Long
            ) {
                verificarCambios()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Botón "Guardar y salir"
        guardarSalirButton.setOnClickListener {
            if (uid != null) {
                val db = FirebaseFirestore.getInstance()
                // Cambié la colección de 'usuarios' a 'users'
                val usuarioRef = db.collection("users").document(uid)

                val nuevosDatos = mapOf(
                    "nombre" to nombreEditText.text.toString(),
                    "apellidos" to apellidosEditText.text.toString(),
                    "correo" to correoEditText.text.toString(),
                    "edad" to edadEditText.text.toString(),
                    "sexo" to sexoSpinner.selectedItem.toString()
                )

                usuarioRef.get().addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        // Si existe el documento, lo actualizamos
                        usuarioRef.update(nuevosDatos)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Cambios guardados", Toast.LENGTH_SHORT).show()
                                finish()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Error al guardar: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                    } else {
                        // Si no existe, mostramos un mensaje
                        Toast.makeText(this, "No se encontró información del perfil", Toast.LENGTH_LONG).show()
                    }
                }.addOnFailureListener { e ->
                    Toast.makeText(this, "Error al consultar perfil: ${e.message}", Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
            }
        }

        // Botón "Regresar"
        regresarButton.setOnClickListener {
            finish()
        }
    }

    // Función que verifica si los datos en los campos han cambiado
    private fun verificarCambios() {
        val nuevoNombre = nombreEditText.text.toString()
        val nuevosApellidos = apellidosEditText.text.toString()
        val nuevoCorreo = correoEditText.text.toString()
        val nuevaEdad = edadEditText.text.toString()
        val nuevoSexo = sexoSpinner.selectedItem.toString()

        // Habilitar el botón "Guardar y salir" si hubo cambios
        guardarSalirButton.isEnabled = (
                nuevoNombre != originalNombre ||
                        nuevosApellidos != originalApellidos ||
                        nuevoCorreo != originalCorreo ||
                        nuevaEdad != originalEdad ||
                        nuevoSexo != originalSexo
                )
    }
}