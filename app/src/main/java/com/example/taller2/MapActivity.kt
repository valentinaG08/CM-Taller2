package com.example.taller2

import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import android.Manifest

import androidx.core.app.ActivityCompat
import com.example.taller2.databinding.ActivityMapBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import org.osmdroid.api.IMapController
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.library.BuildConfig
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker

class MapActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMapBinding
    private lateinit var mapController: IMapController
    private lateinit var fusedLocationClient: FusedLocationProviderClient

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
                    // Crear un GeoPoint con la ubicación del usuario
                    val userGeoPoint = GeoPoint(latitude, longitude)
                    // Centrar el mapa en la ubicación del usuario
                    mapController.setZoom(18.0)
                    mapController.setCenter(userGeoPoint)
                    // Aquí puedes hacer lo que necesites con el GeoPoint, como mostrarlo en el mapa
                    val userMarker = createMarker(userGeoPoint, "Tu ubicación", null, 0)
                    binding.osmMapPlace.overlays.add(userMarker)
                }
            }
    }

    private fun createMarker(p: GeoPoint, title: String?, desc: String?, iconID: Int): Marker {
        val marker = Marker(binding.osmMapPlace)
        title?.let { marker.title = it }
        desc?.let { marker.subDescription = it }
        if (iconID != 0) {
            val myIcon = resources.getDrawable(iconID, this.theme)
            marker.icon = myIcon
        }
        marker.position = p
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        return marker
    }

    companion object {
        private const val REQUEST_LOCATION_PERMISSION = 1
    }
}
