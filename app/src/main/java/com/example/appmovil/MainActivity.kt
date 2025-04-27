package com.example.appmovil

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var usernameTextView: TextView
    private lateinit var btnAseguradora: Button
    private lateinit var btnEmergencia: Button
    private lateinit var editarPerfilButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()

        usernameTextView = findViewById(R.id.usernameTextView)
        btnAseguradora = findViewById(R.id.btnAseguradora)
        btnEmergencia = findViewById(R.id.btnEmergencia)
        editarPerfilButton = findViewById(R.id.editarPerfilButton)

        val currentUser: FirebaseUser? = auth.currentUser
        if (currentUser != null) {
            // Obtener la referencia a Firestore
            val db = FirebaseFirestore.getInstance()

            // Obtener los datos del usuario desde Firestore
            val userRef = db.collection("users").document(currentUser.uid)

            userRef.get().addOnSuccessListener { document ->
                if (document.exists()) {
                    // Recuperar nombre y apellidos del usuario
                    val nombre = document.getString("nombre") ?: ""
                    val apellidos = document.getString("apellidos") ?: ""

                    // Actualizar el TextView con el nombre y apellido
                    usernameTextView.text = "$nombre $apellidos"
                } else {
                    usernameTextView.text = "Usuario no encontrado"
                }
            }.addOnFailureListener {
                usernameTextView.text = "Error al cargar datos"
            }
        } else {
            usernameTextView.text = "Usuario no identificado"
        }

        editarPerfilButton.setOnClickListener {
            val intent = Intent(this, EditarPerfilActivity::class.java)
            startActivity(intent)
        }

        btnAseguradora.setOnClickListener {
            val intent = Intent(this, AseguradorasActivity::class.java)
            val displayName = currentUser?.displayName
            val email = currentUser?.email
            val nombreUsuario = displayName ?: email ?: "Usuario"
            intent.putExtra("nombreUsuario", nombreUsuario)
            startActivity(intent)
        }

        // btnEmergencia.setOnClickListener {
        //     val intent = Intent(this, EmergenciaActivity::class.java)
        //     startActivity(intent)
        // }
    }
}
