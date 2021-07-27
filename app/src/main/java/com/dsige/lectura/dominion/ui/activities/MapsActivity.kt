package com.dsige.lectura.dominion.ui.activities

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.RelativeLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.dsige.lectura.dominion.R
import com.dsige.lectura.dominion.helper.FetchUrl
import com.dsige.lectura.dominion.helper.TaskLoadedCallback

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, LocationListener, TaskLoadedCallback {

    lateinit var camera: CameraPosition
    private lateinit var mMap: GoogleMap
    private var mapView: View? = null
    private lateinit var place1: MarkerOptions
    private lateinit var place2: MarkerOptions
    lateinit var locationManager: LocationManager

    private var isFirstTime: Boolean = true
    private var minDistanceChangeForUpdates: Int = 10
    private var minTimeBwUpdates: Int = 5000

    private var latitud: String = ""
    private var longitud: String = ""
    private var title: String = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        val b = intent.extras
        if (b != null) {
            latitud = b.getString("latitud")!!
            longitud = b.getString("longitud")!!
            title = b.getString("title")!!
            val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
            mapFragment.getMapAsync(this)
        }
    }

    override fun onResume() {
        super.onResume()
        if (!this.isGPSEnabled()) {
            showInfoAlert()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        val permisos = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            mMap = googleMap

            val sydney = LatLng(latitud.toDouble(), longitud.toDouble())
            mMap.addMarker(MarkerOptions().position(sydney).title(title))
            mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
            mMap.isMyLocationEnabled = true

            if (mapView?.findViewById<View>(Integer.parseInt("1")) != null) {
                val locationButton =
                    (mapView!!.findViewById<View>(Integer.parseInt("1")).parent as View).findViewById<View>(
                        Integer.parseInt("2")
                    )
                val layoutParams = locationButton.layoutParams as RelativeLayout.LayoutParams
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0)
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE)
                layoutParams.setMargins(0, 0, 30, 30)
            }

            locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                minTimeBwUpdates.toLong(),
                minDistanceChangeForUpdates.toFloat(),
                this
            )
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                minTimeBwUpdates.toLong(),
                minDistanceChangeForUpdates.toFloat(),
                this
            )
        } else {
            ActivityCompat.requestPermissions(this, permisos, 1)
        }
    }

    private fun zoomToLocation(location: Location) {
        camera = CameraPosition.Builder()
            .target(LatLng(location.latitude, location.longitude))
            .zoom(12f)  // limite 21
            //.bearing(165) // 0 - 365°
            .tilt(30f)        // limit 90
            .build()
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(camera))
    }

    private fun Context.isGPSEnabled() =
        (getSystemService(Context.LOCATION_SERVICE) as LocationManager).isProviderEnabled(
            LocationManager.GPS_PROVIDER
        )

    private fun showInfoAlert() {
        val builder = AlertDialog.Builder(ContextThemeWrapper(this, R.style.AppTheme))
        builder.setTitle("GPS Signal")
        builder.setMessage("Necesitas tener habilitado la señal de GPS. Te gustaria habilitar la señal de GPS ahora ?.")
        builder.setPositiveButton("Aceptar") { dialog, _ ->
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(intent)
            dialog.dismiss()
        }
        val dialog = builder.create()
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
    }

    override fun onTaskDone(vararg values: Any) {
        val polyline = mMap.addPolyline(values[0] as PolylineOptions)
        polyline.isClickable = true
    }

    private fun getUrl(origin: LatLng, dest: LatLng): String {
        val strOrigin = "origin=" + origin.latitude + "," + origin.longitude
        val strDest = "destination=" + dest.latitude + "," + dest.longitude
        val mode = "mode=driving"
        val parameters = "$strOrigin&$strDest&$mode"
        val output = "json"
        return "https://maps.googleapis.com/maps/api/directions/" +
                "$output?" +
                "$parameters&key=" +
                getString(R.string.google_maps_key)
    }

    override fun onLocationChanged(location: Location) {
        if (isFirstTime) {
            zoomToLocation(location)
            place1 =
                MarkerOptions().position(LatLng(location.latitude, location.longitude)).title("YO")
            place2 = MarkerOptions().position(LatLng(latitud.toDouble(), longitud.toDouble()))
                .title(title)
            FetchUrl(getUrl(place1.position, place2.position), "driving", this)
            isFirstTime = false
        }
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
    override fun onProviderEnabled(provider: String) {}
    override fun onProviderDisabled(provider: String) {}
}