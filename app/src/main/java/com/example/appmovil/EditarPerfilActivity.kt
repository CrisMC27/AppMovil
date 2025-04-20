package com.example.appmovil

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

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

        // Obtener usuario actual
        val usuario = FirebaseAuth.getInstance().currentUser

        originalNombre = usuario?.displayName ?: ""
        originalCorreo = usuario?.email ?: ""
        originalApellidos = ""  // Este valor lo puedes obtener si se almacena en Firebase
        originalEdad = ""
        originalSexo = ""

        // Mostrar datos iniciales
        nombreEditText.setText(originalNombre)
        correoEditText.setText(originalCorreo)

        // Inicialmente deshabilitar el botón "Guardar y salir"
        guardarSalirButton.isEnabled = false

        // Escuchar cambios en los campos
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
                parent: AdapterView<*>,
                view: android.view.View?,
                position: Int,
                id: Long
            ) {
                verificarCambios()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // Botón "Guardar y salir"
        guardarSalirButton.setOnClickListener {
            // Aquí se simula guardar los cambios
            Toast.makeText(this, "Cambios guardados (simulado)", Toast.LENGTH_SHORT).show()
            finish()
        }

        // Botón "Regresar"
        regresarButton.setOnClickListener {
            finish()
        }

        // Spinner de sexo
        ArrayAdapter.createFromResource(
            this,
            R.array.sexos_array, // asegúrate de tener este array en strings.xml
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            sexoSpinner.adapter = adapter
        }
    }

    private fun verificarCambios() {
        val nuevoNombre = nombreEditText.text.toString()
        val nuevosApellidos = apellidosEditText.text.toString()
        val nuevoCorreo = correoEditText.text.toString()
        val nuevaEdad = edadEditText.text.toString()
        val nuevoSexo = sexoSpinner.selectedItem.toString()

        guardarSalirButton.isEnabled = (
                nuevoNombre != originalNombre ||
                        nuevosApellidos != originalApellidos ||
                        nuevoCorreo != originalCorreo ||
                        nuevaEdad != originalEdad ||
                        nuevoSexo != originalSexo
                )
    }
}

