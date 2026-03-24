package com.example.fuelapp

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.fuelapp.ui.fragments.AddFuelLogFragment
import android.view.View
import com.example.fuelapp.ui.fragments.FuelLogFragment
import com.example.fuelapp.ui.fragments.VehicleListFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private val tag = "mainactivity"

    private lateinit var bottomNav: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(tag, "onCreate")
        setContentView(R.layout.activity_main)

        bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigationView)

        bottomNav.selectedItemId = R.id.nav_dashboard

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, VehicleListFragment())
            .commit()

        bottomNav.setOnItemSelectedListener { item ->
            when(item.itemId) {
                R.id.nav_add_log -> { switchFragment(AddFuelLogFragment()); false }
                R.id.nav_dashboard -> { switchFragment(VehicleListFragment()); true }
                R.id.nav_fuel_logs -> { switchFragment(FuelLogFragment()); true }
                else -> false
            }
        }
    }

    override fun onStart() { super.onStart(); Log.d(tag, "onStart") }
    override fun onResume() { super.onResume(); Log.d(tag, "onResume") }
    override fun onPause() { super.onPause(); Log.d(tag, "onPause") }
    override fun onStop() { super.onStop(); Log.d(tag, "onStop") }
    override fun onDestroy() { super.onDestroy(); Log.d(tag, "onDestroy") }

    fun switchFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    fun hideBottomNav() {
        bottomNav.visibility = View.GONE
    }

    fun showBottomNav() {
        bottomNav.visibility = View.VISIBLE
    }
}