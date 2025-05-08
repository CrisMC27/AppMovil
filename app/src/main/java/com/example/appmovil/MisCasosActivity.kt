package com.example.appmovil

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MisCasosActivity : AppCompatActivity() {

    private lateinit var casosLayout: LinearLayout
    private lateinit var noCasosTextView: TextView
    private lateinit var userNameTextView: TextView
    private lateinit var btnRegresar: Button

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mis_casos)

        casosLayout = findViewById(R.id.casosLayout)
        noCasosTextView = findViewById(R.id.noCasosTextView)
        userNameTextView = findViewById(R.id.userNameTextView)
        btnRegresar = findViewById(R.id.btnRegresar)

        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userRef = db.collection("users").document(currentUser.uid)
            userRef.get().addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val nombre = doc.getString("nombre") ?: ""
                    val apellidos = doc.getString("apellidos") ?: ""
                    userNameTextView.text = "$nombre $apellidos"
                }
            }

            db.collection("casos")
                .whereEqualTo("usuarioId", currentUser.uid)
                .get()
                .addOnSuccessListener { result ->
                    if (!result.isEmpty) {
                        for (document in result) {
                            val nombreCaso = document.getString("nombreCaso") ?: "Caso sin título"
                            val casoId = document.id

                            val casoView = LinearLayout(this)
                            casoView.orientation = LinearLayout.HORIZONTAL
                            casoView.setPadding(8, 8, 8, 8)

                            val casoText = TextView(this)
                            casoText.text = nombreCaso
                            casoText.textSize = 18f
                            casoText.setTextColor(resources.getColor(android.R.color.black))
                            casoText.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)

                            val ojoButton = ImageButton(this)
                            ojoButton.setImageResource(android.R.drawable.ic_menu_view)
                            ojoButton.setBackgroundColor(0x00000000)
                            ojoButton.setOnClickListener {
                                //val intent = Intent(this, VerDocumentoActivity::class.java)
                                intent.putExtra("CASO_ID", casoId)
                                startActivity(intent)
                            }

                            casoView.addView(casoText)
                            casoView.addView(ojoButton)

                            casosLayout.addView(casoView)
                        }
                    } else {
                        noCasosTextView.visibility = View.VISIBLE
                    }
                }
        }

        btnRegresar.setOnClickListener {
            finish()
        }
    }
}
