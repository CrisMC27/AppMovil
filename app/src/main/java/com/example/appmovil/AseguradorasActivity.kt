package com.example.appmovil

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class AseguradorasActivity : AppCompatActivity() {

    private val REQUEST_CALL_PERMISSION = 1
    private var pendingPhoneNumber: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_aseguradoras)

        val nombreUsuario = intent.getStringExtra("nombreUsuario") ?: "Usuario"
        val usernameTextView = findViewById<TextView>(R.id.usernameTextView)
        usernameTextView.text = nombreUsuario

        val aseguradoras = mapOf(
            R.id.btnAxa to "tel:018000515999",
            R.id.btnMapfre to "tel:018000519991",
            R.id.btnAllianz to "tel:6013077080",
            R.id.btnBolivar to "tel:6013122122",
            R.id.btnMundial to "tel:6017444788",
            R.id.btnSura to "tel:018000519494"
        )

        aseguradoras.forEach { (buttonId, telefono) ->
            findViewById<Button>(buttonId).setOnClickListener {
                makePhoneCall(telefono)
            }
        }

        val btnRegresar = findViewById<Button>(R.id.backButton)
        btnRegresar.setOnClickListener {
            finish()
        }
    }

    private fun makePhoneCall(phoneNumber: String) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            pendingPhoneNumber = phoneNumber
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CALL_PHONE), REQUEST_CALL_PERMISSION)
        } else {
            startCall(phoneNumber)
        }
    }

    private fun startCall(phoneNumber: String) {
        val intent = Intent(Intent.ACTION_CALL)
        intent.data = Uri.parse(phoneNumber)
        startActivity(intent)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_CALL_PERMISSION && grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            pendingPhoneNumber?.let {
                startCall(it)
            }
        }
    }
}
