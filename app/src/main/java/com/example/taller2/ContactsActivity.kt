package com.example.taller2

import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.PackageManager
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat


class ContactsActivity : AppCompatActivity() {

    private lateinit var contactsText : TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contacts)

        contactsText = findViewById(R.id.TextContacts)

        requestPermission(this,
            android.Manifest.permission.READ_CONTACTS,
            "App",
            DataPermission.MY_PERMISSION_REQUEST_READ_CONTACTS
        )
    }

    private fun requestPermission(context: Activity, permission: String, justify: String, idCode: Int) {
        when {
            ContextCompat.checkSelfPermission(
                context,
                permission
            ) == PackageManager.PERMISSION_GRANTED -> {
                contactsText.text = "PERMISSION GRANTED"
                contactsText.setTextColor(Color.GREEN)
            }

            ActivityCompat.shouldShowRequestPermissionRationale(
                context,
                permission
            ) -> {
                // Explicar por qué necesitamos el permiso
                Toast.makeText(this, "Permiso negado previamente.", Toast.LENGTH_SHORT).show()
                requestPermissions(
                    arrayOf(permission),
                    idCode
                )
            }

            else -> {
                Toast.makeText(this, "La aplicación necesita el permiso.", Toast.LENGTH_SHORT).show()
                requestPermissions(
                    arrayOf(permission),
                    idCode
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            DataPermission.MY_PERMISSION_REQUEST_READ_CONTACTS -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Gracias", Toast.LENGTH_SHORT).show()
                    contactsText.text = "PERMISSION GRANTED"
                    contactsText.setTextColor(Color.GREEN)
                } else {
                    Toast.makeText(this, "La aplicación necesita el permiso.", Toast.LENGTH_SHORT).show()
                    contactsText.text = "PERMISSION DENIED"
                    contactsText.setTextColor(Color.RED)
                }

                return
            }
            else -> {
                // Ignorar todos los demas permisos
            }
        }
    }
}