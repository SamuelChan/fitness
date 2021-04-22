package com.tvdi.fitness.ui.map

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.maps.android.SphericalUtil
import com.tvdi.fitness.MainActivity
import com.tvdi.fitness.R
import com.tvdi.fitness.databinding.FragmentMapsBinding
import com.tvdi.fitness.ui.user.UserViewModel
import java.text.SimpleDateFormat
import java.util.*

class MapsFragment : Fragment() {

    companion object {
        const val DEFAULT_INTERVAL_MS: Long = 0 * 1000
        const val DEFAULT_ZOOM_LEVEL: Float = 15F
        val DISTANCE_GOALS = arrayOf("1000", "2000", "3000")
    }

    private val model: UserViewModel by activityViewModels()

    private lateinit var locationClient: FusedLocationProviderClient
    private lateinit var stepDetectSensor: Sensor
    private lateinit var mapInstance: GoogleMap
    private lateinit var goalInput: AutoCompleteTextView
    private var _binding: FragmentMapsBinding? = null
    private var lastLocation: Location? = null
    private var lastDismissDate: String = ""
    private var perceiving = false
    private lateinit var sensorManager: SensorManager

    private val stepSensorListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            if (event?.sensor?.type == Sensor.TYPE_STEP_DETECTOR)
                if (event.values[0] == 1.0F)
                    model.setStepCount(1)
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    private val locationCallback: LocationCallback =
        object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                if (locationResult.locations.size == 0 ||
                    !perceiving
                )
                    return
                lastLocation = lastLocation ?: locationResult.locations.first().also {
                    locationResult.locations.remove(it)
                }
                val calculatedDistance = locationResult.locations.fold(
                    0.0,
                    { total: Double, point: Location ->
                        val partial = SphericalUtil.computeDistanceBetween(
                            LatLng(point.latitude, point.longitude),
                            LatLng(lastLocation!!.latitude, lastLocation!!.longitude)
                        )
                        lastLocation = point
                        total + partial
                    })
                if (calculatedDistance >= 0.0) {
                    model.setDistance(calculatedDistance.toLong())
                    mapInstance.moveCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            LatLng(lastLocation!!.latitude, lastLocation!!.longitude),
                            DEFAULT_ZOOM_LEVEL
                        )
                    )
                    val goal = goalInput.text.toString().toDoubleOrNull() ?: 0.0
                    val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        .format(Calendar.getInstance().time)
                    if (goal > 0.0 && calculatedDistance >= goal &&
                        !currentDate.equals(lastDismissDate, true)
                    )
                        showAchievementDialog()
                }
            }
        }

    fun showAchievementDialog() {
        try {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(resources.getString(R.string.achievement_title))
                .setMessage(resources.getString(R.string.achievement_message))
                .setPositiveButton(resources.getString(R.string.achievement_button_text)) { _, _ ->
                    lastDismissDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        .format(Calendar.getInstance().time)
                }.show()
        } catch (exception: IllegalStateException) {
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        locationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        sensorManager = activity?.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepDetectSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
    }

    private val mapAsyncCallback = OnMapReadyCallback { map ->
        if (map == null) return@OnMapReadyCallback
        else mapInstance = map
        val activity = (requireActivity() as MainActivity)
        if (!activity.locationPermissionGranted)
            activity.requestFitnessPermissions()
        else
            onMapUpdate(map)
    }

    private fun onMapUpdate(map: GoogleMap) {
        try {
            map.isMyLocationEnabled = true
            map.uiSettings.isMyLocationButtonEnabled = true
            locationClient.requestLocationUpdates(
                LocationRequest.create()
                    .setInterval(DEFAULT_INTERVAL_MS)
                    .setFastestInterval(2 * DEFAULT_INTERVAL_MS),
                locationCallback, Looper.myLooper()!!
            )
        } catch (securityException: SecurityException) {
            Log.e(
                javaClass.name,
                "onMapUpdate, exception=${securityException.message}"
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_maps, container, false
        )
        _binding?.userViewModel = model
        _binding?.lifecycleOwner = viewLifecycleOwner
        _binding?.floatingActionButton?.setOnClickListener {
            if (!perceiving) {
                perceiving = true
                (it as FloatingActionButton).setImageResource(
                    R.drawable.ic_baseline_stop_circle_24
                )
                if ((requireActivity() as MainActivity).recognitionPermissionGranted)
                    sensorManager.registerListener(
                        stepSensorListener, stepDetectSensor, SensorManager.SENSOR_DELAY_FASTEST
                    )
            } else {
                perceiving = false
                (it as FloatingActionButton).setImageResource(
                    R.drawable.ic_baseline_not_started_24
                )
                sensorManager.unregisterListener(stepSensorListener, stepDetectSensor)
            }
        }
        goalInput = _binding?.goalDistanceInput as AutoCompleteTextView
        goalInput.setAdapter(
            ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                DISTANCE_GOALS
            )
        )
        return _binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(mapAsyncCallback)
    }
}