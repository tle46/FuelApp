package com.example.fuelapp.ui.dashboard

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import com.example.fuelapp.R

class DashboardFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("DashboardFragment", "onCreate")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("DashboardFragment", "onCreateView")
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d("DashboardFragment", "onDestroyView")
    }
}