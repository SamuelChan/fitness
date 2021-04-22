package com.tvdi.fitness

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin
import com.amplifyframework.core.Amplify
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.tvdi.fitness.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private val requiredPermissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACTIVITY_RECOGNITION,
    )

    val locationPermissionGranted: Boolean by lazy {
        checkLocationPermissions().also {
            if (!it) this.runOnUiThread { requestFitnessPermissions() }
        }
    }

    val recognitionPermissionGranted: Boolean by lazy {
        checkRecognitionPermissions().also {
            if (!it) this.runOnUiThread { requestFitnessPermissions() }
        }
    }

    private lateinit var binding: ActivityMainBinding

    fun requestFitnessPermissions() = requestPermissions.launch(requiredPermissions)

    private val requestPermissions = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        result.entries.forEach { entry ->
            when (entry.key) {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACTIVITY_RECOGNITION -> {
                    Log.d(
                        this@MainActivity.javaClass.name,
                        "${entry.key} permission is granted"
                    )
                }
                else -> Log.e(
                    this@MainActivity.javaClass.name,
                    "This permission ${entry.key} is not required."
                )
            }
        }
    }

    private fun checkLocationPermissions() = try {
        applicationContext?.run {
            ActivityCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(
                        applicationContext,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
        } ?: false
    } catch (e: Exception) {
        false
    }

    private fun checkRecognitionPermissions() = try {
        applicationContext?.run {
            ActivityCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.ACTIVITY_RECOGNITION
            ) == PackageManager.PERMISSION_GRANTED
        } ?: false
    } catch (e: Exception) {
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Amplify.addPlugin(AWSCognitoAuthPlugin())
        Amplify.configure(applicationContext)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        navView.setupWithNavController(navController)
    }
}