package com.example.taller2

import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import android.Manifest
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
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
import android.view.inputmethod.EditorInfo
import com.example.taller2.utils.Permissions
import com.example.taller2.utils.data.MyLocation
import com.example.taller2.utils.jsonwriters.JSONUtil
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.location.Priority
import com.google.android.gms.location.SettingsClient
import com.google.android.gms.tasks.Task
import org.json.JSONArray
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.TilesOverlay
import java.io.IOException
import java.util.Date
import kotlin.math.roundToInt

class MapActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMapBinding
    private lateinit var mapController: IMapController
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var lastKnownLocation: Location? = null
    private lateinit var mLocationRequest: LocationRequest
    private lateinit var mLocationCallback: LocationCallback

    private var localizaciones : JSONArray = JSONArray()
    private var locationMarker : Marker? = null
    private var longPressedMarker: Marker? = null

    private lateinit var sensorManager: SensorManager
    private lateinit var lightSensor : Sensor
    private lateinit var lightSensorListener: SensorEventListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Configuration.getInstance().userAgentValue = applicationContext.packageName

        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Lunminosidad

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)!!


        lightSensorListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (binding.osmMapPlace != null) {
                    if (event?.values!![0] < 5000) {
                        Log.i("MAPA OSCURO", "ENABLED")
                        binding.osmMapPlace.overlayManager.tilesOverlay.setColorFilter(TilesOverlay.INVERT_COLORS)
                    } else {
                        Log.i("MAPA CLARO", "ENABLED")
                        binding.osmMapPlace.overlayManager.tilesOverlay.setColorFilter(TilesOverlay.INVERT_COLORS)
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

        }


        // Inicialización del cliente de ubicación
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Configuración del mapa
        binding.osmMapPlace.setTileSource(TileSourceFactory.MAPNIK)
        binding.osmMapPlace.setMultiTouchControls(true)
        mapController = binding.osmMapPlace.controller

        // Obtener la ubicación del usuario y centrar el mapa en esa ubicación
        getUserLocation()

        mLocationRequest = createLocationRequest()

        mLocationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                val location = p0.lastLocation
                Log.i("UBICACION", location.toString())

                if (location != null) {
                    lastKnownLocation.let {
                        if (lastKnownLocation?.distanceTo(location)!! > 30) {
                            writeLocation(location)
                            createMarkerUser(location.latitude, location.longitude, "Usted esta aqui", "Su ubicacion")
                        }
                    }
                }
            }
        }

        checkLocationSettings()

        binding.osmMapPlace.overlays.add(createOverlayEvents())

        binding.buscador.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                val addressString = binding.buscador.text.toString()
                if (addressString.isNotEmpty()) {
                    val mGeocoder = Geocoder(baseContext)
                    try {
                        val addresses = mGeocoder.getFromLocationName(addressString, 1)
                        if (!addresses.isNullOrEmpty()) {
                            val pos = addresses[0]
                            createMarkerUser(pos.latitude, pos.longitude, pos.featureName, pos.countryName)
                        } else {
                            Toast.makeText(baseContext, "No existe esa ubicacion", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: IOException) {
                        Log.i("Jum", "Jum")
                    }
                }
            }

            return@setOnEditorActionListener true
        }
    }

    private fun createOverlayEvents(): MapEventsOverlay {
        val overlayEventos = MapEventsOverlay(object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
                return false
            }
            override fun longPressHelper(p: GeoPoint): Boolean {
                longPressOnMap(p)
                return true
            }
        })
        return overlayEventos
    }

    private fun longPressOnMap(p: GeoPoint) {

        val geoCoder = Geocoder(baseContext)

        if (lastKnownLocation != null) {
            Toast.makeText(baseContext, "La distancia es de ${distance(lastKnownLocation!!.latitude, lastKnownLocation!!.longitude, p.latitude, p.longitude)}", Toast.LENGTH_SHORT).show()
        }

        val results = geoCoder.getFromLocation(p.latitude, p.longitude, 1)

        longPressedMarker = createMarkerUser(p.latitude, p.longitude, results!![0].featureName, results[0].countryName)
    }


    private fun createMarkerUser(lat: Double, lng: Double, title: String, desc: String): Marker {
        locationMarker?.let { binding.osmMapPlace.overlays.remove(it) }
        mapController.setZoom(18.0)
        mapController.setCenter(GeoPoint(lat, lng))
        val marker = Marker(binding.osmMapPlace)
        marker.title = title
        marker.position = GeoPoint(lat, lng)
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        marker.subDescription = desc
        marker.textLabelForegroundColor = androidx.appcompat.R.color.material_blue_grey_800
        binding.osmMapPlace.overlays.add(marker)
        locationMarker = marker
        return marker
    }

    private fun writeLocation(loc: Location) {
        localizaciones = JSONUtil.readJsonFromFile("locations.json", baseContext.getExternalFilesDir(null)!!)

        Log.i("JSON", localizaciones.toString())

        localizaciones.put(
            MyLocation(Date(System.currentTimeMillis()), loc.latitude, loc.longitude)
                .toJSON()
        )
        JSONUtil.writeJSON("locations.json", localizaciones ,baseContext.getExternalFilesDir(null)!!)
    }
    private fun checkLocationSettings() {
        val builder = LocationSettingsRequest.Builder().addLocationRequest(mLocationRequest)
        val client : SettingsClient = LocationServices.getSettingsClient(this)
        val task : Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener {
            Log.i("LOCATION", "IS ON")
            startLocationUpdates()
        }
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
                Permissions.REQUEST_LOCATION_PERMISSION
            )

            return
        }

        // Se tienen los permisos, obtener la ubicación actual del usuario
        fusedLocationClient.lastLocation
            .addOnSuccessListener(this) { location: Location? ->
                // Got last known location. In some rare situations this can be null.

                Log.i("LOCATION", location.toString())
                if (location != null) {

                    val latitude = location.latitude
                    val longitude = location.longitude


                    val userGeoPoint = GeoPoint(latitude, longitude)

                    createMarkerUser(userGeoPoint.latitude, userGeoPoint.longitude, "Usted esta aqui", "Su Ubicacion")

                    // Actualizar la última ubicación conocida
                    lastKnownLocation = location
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
            Permissions.REQUEST_LOCATION_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getUserLocation()
                }
            }
        }

    }

    private fun createLocationRequest() : LocationRequest {
        return LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000).apply {
            setMinUpdateIntervalMillis(5000)
        }.build()
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null)
        }
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(mLocationCallback)
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

    private fun distance(lat1: Double, long1: Double, lat2: Double, long2: Double): Double {
        val RADIUS_OF_EARTH_KM : Double = 6371.01
        val latDistance = Math.toRadians(lat1 - lat2)
        val lngDistance = Math.toRadians(long1 - long2)
        val a = (Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lngDistance / 2) * Math.sin(lngDistance / 2))
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        val result = RADIUS_OF_EARTH_KM * c
        return (result * 100.0).roundToInt() / 100.0
    }

}
