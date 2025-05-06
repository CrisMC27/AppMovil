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
    private lateinit var crearCasoButton: Button
    private lateinit var misCasosButton: Button
    private lateinit var editarPerfilButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()

        usernameTextView = findViewById(R.id.usernameTextView)
        crearCasoButton = findViewById(R.id.crearCasoButton)
        misCasosButton = findViewById(R.id.misCasosButton)
        editarPerfilButton = findViewById(R.id.editarPerfilButton)

        val currentUser: FirebaseUser? = auth.currentUser
        if (currentUser != null) {
            val db = FirebaseFirestore.getInstance()
            val userRef = db.collection("users").document(currentUser.uid)

            userRef.get().addOnSuccessListener { document ->
                if (document.exists()) {
                    val nombre = document.getString("nombre") ?: ""
                    val apellidos = document.getString("apellidos") ?: ""
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

        crearCasoButton.setOnClickListener {
            startActivity(Intent(this, CrearCasoActivity::class.java))
        }

        misCasosButton.setOnClickListener {
            startActivity(Intent(this, MisCasosActivity::class.java))
        }

        editarPerfilButton.setOnClickListener {
            startActivity(Intent(this, EditarPerfilActivity::class.java))
        }
    }
}
