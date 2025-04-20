package com.example.appmovil

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var usernameTextView: TextView
    private lateinit var btnAseguradora: Button
    private lateinit var btnEmergencia: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()

        usernameTextView = findViewById(R.id.usernameTextView)
        btnAseguradora = findViewById(R.id.btnAseguradora)
        btnEmergencia = findViewById(R.id.btnEmergencia)

        // Obtener el usuario actual
        val currentUser: FirebaseUser? = auth.currentUser
        if (currentUser != null) {
            val displayName = currentUser.displayName
            val email = currentUser.email
            usernameTextView.text = displayName ?: email ?: "Usuario"
        } else {
            usernameTextView.text = "Usuario no identificado"
        }

        // Ir a pantalla para editar perfil al hacer clic en el nombre
        usernameTextView.setOnClickListener {
            val editarPerfilButton = findViewById<Button>(R.id.editarPerfilButton)
            editarPerfilButton.setOnClickListener {
                val intent = Intent(this, EditarPerfilActivity::class.java)
                startActivity(intent)
            }
        }

        // Navegar a la pantalla de aseguradoras
        btnAseguradora.setOnClickListener {
            val intent = Intent(this, AseguradorasActivity::class.java)

            val displayName = currentUser?.displayName
            val email = currentUser?.email
            val nombreUsuario = displayName ?: email ?: "Usuario"

            intent.putExtra("nombreUsuario", nombreUsuario)
            startActivity(intent)
        }

        // Navegar a la pantalla de servicios de emergencia
        // btnEmergencia.setOnClickListener {
        //     val intent = Intent(this, EmergenciaActivity::class.java)
        //     startActivity(intent)
        // }
    }
}
