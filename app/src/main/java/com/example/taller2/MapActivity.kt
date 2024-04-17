package com.example.taller2

import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import android.Manifest
import android.widget.Toast

import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.taller2.databinding.ActivityMapBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import org.osmdroid.api.IMapController
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.library.BuildConfig
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import android.os.Environment
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class MapActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMapBinding
    private lateinit var mapController: IMapController
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var lastKnownLocation: Location? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Configuration.getInstance().userAgentValue = applicationContext.packageName

        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicialización del cliente de ubicación
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Configuración del mapa
        binding.osmMapPlace.setTileSource(TileSourceFactory.MAPNIK)
        binding.osmMapPlace.setMultiTouchControls(true)
        mapController = binding.osmMapPlace.controller

        // Obtener la ubicación del usuario y centrar el mapa en esa ubicación
        getUserLocation()
    }

    private fun getUserLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Si no se tienen los permisos necesarios, solicitarlos al usuario
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                REQUEST_LOCATION_PERMISSION
            )
            return
        }

        // Se tienen los permisos, obtener la ubicación actual del usuario
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                // Got last known location. In some rare situations this can be null.
                if (location != null) {
                    // Se obtiene la ubicación actual del usuario
                    val latitude = location.latitude
                    val longitude = location.longitude

                    // Si hay una ubicación previa, comprobar el movimiento
                    lastKnownLocation?.let { lastLocation ->
                        val distanceMoved = location.distanceTo(lastLocation)
                        if (distanceMoved > 30) {
                            // Registrar el movimiento en el archivo JSON
                            registerMovement(location)
                        }
                    }

                    // Crear un GeoPoint con la ubicación del usuario
                    val userGeoPoint = GeoPoint(latitude, longitude)
                    // Centrar el mapa en la ubicación del usuario
                    mapController.setCenter(userGeoPoint)
                    // Actualizar la última ubicación conocida
                    lastKnownLocation = location
                }
            }
    }

    private fun registerMovement(location: Location) {
        // Obtener la fecha y la hora actual
        val currentDateTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

        // Crear un objeto JSON con los datos de ubicación y tiempo
        val locationData = JSONObject()
        try {
            locationData.put("latitude", location.latitude)
            locationData.put("longitude", location.longitude)
            locationData.put("datetime", currentDateTime)
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        // Obtener el directorio de almacenamiento externo
        val externalStorageDir = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)

        // Crear un archivo JSON en el directorio de documentos
        val file = File(externalStorageDir, "movement_records.json")
        try {
            // Leer el contenido del archivo JSON actual
            val jsonArray = if (file.exists()) {
                val jsonString = file.readText()
                JSONArray(jsonString)
            } else {
                JSONArray()
            }

            // Agregar el nuevo registro al arreglo JSON
            jsonArray.put(locationData)

            // Escribir el nuevo contenido al archivo JSON
            val fileWriter = FileWriter(file)
            fileWriter.write(jsonArray.toString())
            fileWriter.close()

            // Mostrar un mensaje de registro exitoso
            Toast.makeText(this, "Registro de movimiento exitoso", Toast.LENGTH_SHORT).show()
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, "Error al registrar movimiento", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            0 -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Gracias", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Permiso negado", Toast.LENGTH_SHORT).show()
                }
                return
            }
            1 -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permiso de localizacion", Toast.LENGTH_SHORT).show()
                    getUserLocation()
                } else {
                    Toast.makeText(this, "Funcionalidades reducidas", Toast.LENGTH_SHORT).show()
                }
            }
            else -> {
                // Ignorar todos los demas permisos
            }
        }
    }

    companion object {
        private const val REQUEST_LOCATION_PERMISSION = 1
    }
}
