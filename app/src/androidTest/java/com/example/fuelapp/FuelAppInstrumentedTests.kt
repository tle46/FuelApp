package com.example.fuelapp

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import org.hamcrest.Matchers.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class FuelAppInstrumentedTests {

    companion object {
        const val TEST_EMAIL    = "stufftest@gmail.com"
        const val TEST_PASSWORD = "password"
    }

    private lateinit var scenario: ActivityScenario<MainActivity>

    @Before
    fun signInThenLaunch() {
        val latch = java.util.concurrent.CountDownLatch(1)
        FirebaseAuth.getInstance()
            .signInWithEmailAndPassword(TEST_EMAIL, TEST_PASSWORD)
            .addOnCompleteListener { latch.countDown() }
        latch.await(10, java.util.concurrent.TimeUnit.SECONDS)
        scenario = ActivityScenario.launch(MainActivity::class.java)
    }

    @After
    fun tearDown() {
        scenario.close()
    }

    // VehicleListFragment loads and UI elements are visible
    @Test
    fun vehicleListFragment_isDisplayed() {
        onView(withId(R.id.spinnerVehicle)).check(matches(isDisplayed()))
        onView(withId(R.id.btnAddVehicle)).check(matches(isDisplayed()))
        onView(withId(R.id.txtTotalMPG)).check(matches(isDisplayed()))
        onView(withId(R.id.chartOdometerTime)).check(matches(isDisplayed()))
    }

    // Filling out the add vehicle form, saving, then selecting it from the spinner
    @Test
    fun addVehicle_savesAndAppearsInSpinner() {
        onView(withId(R.id.btnAddVehicle)).perform(click())
        onView(withId(R.id.etVehicleName)).perform(typeText("Test Car"), closeSoftKeyboard())
        onView(withId(R.id.etYear)).perform(typeText("2022"), closeSoftKeyboard())
        onView(withId(R.id.etMake)).perform(typeText("Toyota"), closeSoftKeyboard())
        onView(withId(R.id.etModel)).perform(typeText("Camry"), closeSoftKeyboard())
        onView(withId(R.id.btnSaveVehicle)).perform(click())

        onView(withId(R.id.spinnerVehicle)).perform(click())
        onData(allOf(`is`(instanceOf(String::class.java)), `is`("Test Car"))).perform(click())

        onView(withId(R.id.spinnerVehicle))
            .check(matches(withSpinnerText(containsString("Test Car"))))

        // Cleanup
        onView(withId(R.id.btnDeleteVehicle)).perform(click())
    }

    // After adding a vehicle and selecting it from the spinner, deleting it removes it
    @Test
    fun deleteVehicle_removesFromSpinner() {
        onView(withId(R.id.btnAddVehicle)).perform(click())
        onView(withId(R.id.etVehicleName)).perform(typeText("Delete Me"), closeSoftKeyboard())
        onView(withId(R.id.etYear)).perform(typeText("2020"), closeSoftKeyboard())
        onView(withId(R.id.etMake)).perform(typeText("Honda"), closeSoftKeyboard())
        onView(withId(R.id.etModel)).perform(typeText("Civic"), closeSoftKeyboard())
        onView(withId(R.id.btnSaveVehicle)).perform(click())

        onView(withId(R.id.spinnerVehicle)).perform(click())
        onData(allOf(`is`(instanceOf(String::class.java)), `is`("Delete Me"))).perform(click())

        onView(withId(R.id.btnDeleteVehicle)).perform(click())

        // Vehicle already deleted, no further cleanup needed
        onView(withId(R.id.spinnerVehicle))
            .check(matches(not(withSpinnerText(containsString("Delete Me")))))
    }

    // Filling out the add fuel log form saves a log that appears in the fuel list
    @Test
    fun addFuelLog_appearsInFuelList() {
        onView(withId(R.id.btnAddVehicle)).perform(click())
        onView(withId(R.id.etVehicleName)).perform(typeText("Fuel Test Car"), closeSoftKeyboard())
        onView(withId(R.id.etYear)).perform(typeText("2021"), closeSoftKeyboard())
        onView(withId(R.id.etMake)).perform(typeText("Ford"), closeSoftKeyboard())
        onView(withId(R.id.etModel)).perform(typeText("Focus"), closeSoftKeyboard())
        onView(withId(R.id.btnSaveVehicle)).perform(click())

        onView(withId(R.id.nav_add_log)).perform(click())
        onView(withId(R.id.etFuelPrice))
            .perform(scrollTo(), click(), typeText("3500"), closeSoftKeyboard())
        onView(withId(R.id.etFuelAmount))
            .perform(scrollTo(), click(), typeText("10000"), closeSoftKeyboard())
        onView(withId(R.id.etOdometer))
            .perform(scrollTo(), typeText("50000"), closeSoftKeyboard())
        onView(withId(R.id.btnSaveFuel)).perform(scrollTo(), click())

        onView(withId(R.id.nav_fuel_logs)).perform(click())
        onView(withId(R.id.rvFuelLogs)).check(matches(hasMinimumChildCount(1)))

        // Cleanup: delete the fuel log then the vehicle
        onView(withId(R.id.rvFuelLogs))
            .perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click()))
        onView(withId(R.id.btnDeleteFuel)).perform(scrollTo(), click())
        onView(withId(R.id.nav_dashboard)).perform(click())
        onView(withId(R.id.spinnerVehicle)).perform(click())
        onData(allOf(`is`(instanceOf(String::class.java)), `is`("Fuel Test Car"))).perform(click())
        onView(withId(R.id.btnDeleteVehicle)).perform(click())
    }

    // Tapping a fuel log and deleting it removes it from the list
    @Test
    fun deleteFuelLog_removesFromList() {
        onView(withId(R.id.btnAddVehicle)).perform(click())
        onView(withId(R.id.etVehicleName)).perform(typeText("Delete Log Car"), closeSoftKeyboard())
        onView(withId(R.id.etYear)).perform(typeText("2019"), closeSoftKeyboard())
        onView(withId(R.id.etMake)).perform(typeText("Chevy"), closeSoftKeyboard())
        onView(withId(R.id.etModel)).perform(typeText("Malibu"), closeSoftKeyboard())
        onView(withId(R.id.btnSaveVehicle)).perform(click())

        onView(withId(R.id.nav_add_log)).perform(click())
        onView(withId(R.id.etFuelPrice))
            .perform(scrollTo(), click(), typeText("3500"), closeSoftKeyboard())
        onView(withId(R.id.etFuelAmount))
            .perform(scrollTo(), click(), typeText("10000"), closeSoftKeyboard())
        onView(withId(R.id.etOdometer))
            .perform(scrollTo(), typeText("20000"), closeSoftKeyboard())
        onView(withId(R.id.btnSaveFuel)).perform(scrollTo(), click())

        onView(withId(R.id.nav_fuel_logs)).perform(click())
        val countBefore = getRecyclerViewItemCount(R.id.rvFuelLogs)

        onView(withId(R.id.rvFuelLogs))
            .perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click()))
        onView(withId(R.id.btnDeleteFuel)).perform(scrollTo(), click())

        onView(withId(R.id.rvFuelLogs))
            .check(matches(hasChildCount(countBefore - 1)))

        // Cleanup: fuel log already deleted, delete the vehicle
        onView(withId(R.id.nav_dashboard)).perform(click())
        onView(withId(R.id.spinnerVehicle)).perform(click())
        onData(allOf(`is`(instanceOf(String::class.java)), `is`("Delete Log Car"))).perform(click())
        onView(withId(R.id.btnDeleteVehicle)).perform(click())
    }

    private fun getRecyclerViewItemCount(recyclerViewId: Int): Int {
        var count = 0
        onView(withId(recyclerViewId)).check { view, _ ->
            count = (view as RecyclerView).adapter?.itemCount ?: 0
        }
        return count
    }
}