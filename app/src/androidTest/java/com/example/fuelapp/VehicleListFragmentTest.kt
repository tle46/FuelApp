package com.example.fuelapp

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.ViewModelProvider
import androidx.test.espresso.Espresso.onData
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

import com.example.fuelapp.model.Vehicle
import com.example.fuelapp.ui.fragments.VehicleListFragment
import com.example.fuelapp.viewmodel.VehicleListViewModel
import junit.framework.TestCase.assertEquals
import org.hamcrest.CoreMatchers.anything
import org.junit.Before

@RunWith(AndroidJUnit4::class)
class VehicleListFragmentTest {
    private lateinit var viewModel: VehicleListViewModel

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setup() {
        viewModel = VehicleListViewModel()
    }

    @Test
    fun logoutButton_handlesClick() {
        launchFragmentInContainer<VehicleListFragment>(
            themeResId = R.style.Theme_FuelApp
        )

        onView(withId(R.id.logoutButton))
            .perform(click())
    }

    @Test
    fun initialState_hidesVehicleForm() {
        launchFragmentInContainer<VehicleListFragment>(
            themeResId = R.style.Theme_FuelApp
        )

        onView(withId(R.id.etVehicleName))
            .check(matches(withEffectiveVisibility(Visibility.GONE)))

        onView(withId(R.id.etYear))
            .check(matches(withEffectiveVisibility(Visibility.GONE)))

        onView(withId(R.id.btnEditVehicle))
            .check(matches(withEffectiveVisibility(Visibility.GONE)))
    }

    /**
     * Selecting a vehicle updates the form fields
     */
    @Test
    fun selectingVehicle_populatesFormFields() {

        val scenario = launchFragmentInContainer<VehicleListFragment>(
            themeResId = R.style.Theme_FuelApp
        )

        scenario.onFragment { fragment ->
            val viewModel = ViewModelProvider(fragment.requireActivity())
                .get(VehicleListViewModel::class.java)

            val vehicle = Vehicle(
                name = "My Car",
                year = 2018,
                make = "Honda",
                model = "Civic"
            )

            viewModel.addVehicle(vehicle)
            viewModel.selectVehicle(vehicle)
        }

        // Check vehicle name
        onView(withId(R.id.etVehicleName))
            .check(matches(withText("My Car")))

        onView(withId(R.id.etYear))
            .check(matches(withText("2018")))

        onView(withId(R.id.etMake))
            .check(matches(withText("Honda")))

        onView(withId(R.id.etModel))
            .check(matches(withText("Civic")))
    }
}