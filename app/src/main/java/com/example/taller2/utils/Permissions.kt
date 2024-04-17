package com.example.taller2.utils

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class Permissions {

    companion object {
        const val MY_PERMISSION_REQUEST_READ_CONTACTS : Int = 0
        const val MY_PERMISSION_REQUEST_CAMERA : Int = 1

        fun requestPermission(
            context: Activity,
            permission: String,
            justify: String,
            idCode: Int,
            callback: (Boolean)->Unit
        ) {
            when {
                ContextCompat.checkSelfPermission(
                    context,
                    permission
                ) == PackageManager.PERMISSION_GRANTED -> {
                    callback(true)
                }

                ActivityCompat.shouldShowRequestPermissionRationale(
                    context,
                    permission
                ) -> {
                    // Explicar por qué necesitamos el permiso
                    Toast.makeText(context, "Permiso negado previamente.", Toast.LENGTH_SHORT).show()
                    ActivityCompat.requestPermissions(
                        context,
                        arrayOf(permission),
                        idCode
                    )
                }

                else -> {
                    Toast.makeText(context, "La aplicación necesita el permiso.", Toast.LENGTH_SHORT).show()
                    ActivityCompat.requestPermissions(
                        context,
                        arrayOf(permission),
                        idCode
                    )
                }
            }
        }

        fun catchPermissionsResult(
            requestCode: Int,
            grantResults: IntArray,
            context: Context,
            callback: (Boolean, Int) -> Unit
        ) {
            when (requestCode) {
                MY_PERMISSION_REQUEST_READ_CONTACTS -> {
                    if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(context, "Gracias", Toast.LENGTH_SHORT).show()
                        callback(true, MY_PERMISSION_REQUEST_READ_CONTACTS)
                    } else {
                        Toast.makeText(context, "La aplicación necesita el permiso.", Toast.LENGTH_SHORT).show()
                        callback(false, -1)
                    }
                }

                MY_PERMISSION_REQUEST_CAMERA -> {
                    if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(context, "Gracias", Toast.LENGTH_SHORT).show()
                        callback(true, MY_PERMISSION_REQUEST_CAMERA)
                    } else {
                        Toast.makeText(context, "La aplicación necesita el permiso.", Toast.LENGTH_SHORT).show()
                        callback(false, -1)
                    }
                }

                else -> {
                    // Ignorar todos los demas permisos
                }
            }
        }
    }


}