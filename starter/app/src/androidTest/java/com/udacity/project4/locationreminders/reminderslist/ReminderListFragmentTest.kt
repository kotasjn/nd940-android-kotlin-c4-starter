package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import android.os.Bundle
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.monitorFragment
import com.udacity.project4.utils.EspressoIdlingResource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class ReminderListFragmentTest : AutoCloseKoinTest() {

    private lateinit var repository: ReminderDataSource

    private lateinit var appContext: Application

    private val dataBindingIdlingResource = DataBindingIdlingResource()

    private lateinit var reminder: ReminderDTO

    private lateinit var reminderList: MutableList<ReminderDTO>

    @Before
    fun init() {
        stopKoin()//stop the original app koin
        appContext = getApplicationContext()
        val myModule = module {
            viewModel {
                RemindersListViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single { RemindersLocalRepository(get()) as ReminderDataSource }
            single { LocalDB.createRemindersDao(appContext) }
        }
        //declare a new koin module
        startKoin {
            modules(listOf(myModule))
        }
        //Get our real repository
        repository = get()

        //clear the data to start fresh
        runBlocking {
            repository.deleteAllReminders()
        }
    }

    @Before
    fun initTestVariables() {
        reminder = ReminderDTO(
            "TEST_TITLE",
            "TEST_DESCRIPTION",
            "TEST_LOCATION",
            50.0,
            15.0
        )

        reminderList = mutableListOf(
            reminder,
            ReminderDTO(
                "TEST_TITLE2",
                "TEST_DESCRIPTION2",
                "TEST_LOCATION2",
                60.0,
                25.0
            ),
            ReminderDTO(
                "TEST_TITLE3",
                "TEST_DESCRIPTION3",
                "TEST_LOCATION3",
                70.0,
                35.0
            )
        )
    }

    @Before
    fun registerIdlingResource() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().register(dataBindingIdlingResource)
    }

    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().unregister(dataBindingIdlingResource)
    }

    @Test
    fun clickAddReminderFabButtonAndNavigateToSaveReminderFragment() {
        // GIVEN - On the list screen
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        val navigationMock = mock(NavController::class.java)
        dataBindingIdlingResource.monitorFragment(scenario)

        scenario.onFragment { fragment ->
            Navigation.setViewNavController(fragment.view!!, navigationMock)
        }

        // WHEN - Click on the fab button
        onView(withId(R.id.addReminderFAB)).perform(click())

        // THEN - navigate to fragment
        verify(navigationMock).navigate(ReminderListFragmentDirections.toSaveReminder())
    }

    @Test
    fun checkWhetherNoDataIsShown() {
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        dataBindingIdlingResource.monitorFragment(scenario)
        onView(withId(R.id.noDataTextView)).check(matches(withText(appContext.getString(R.string.no_data))))
    }

    @Test
    fun checkIfReminderListIsDisplayed() {
        // GIVEN - save a reminder
        runBlocking {
            reminderList.forEach {
                repository.saveReminder(it)
            }
        }

        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        dataBindingIdlingResource.monitorFragment(scenario)

        // THEN - List data are displayed
        onView(withId(R.id.reminderssRecyclerView)).check(matches(isDisplayed()))
        onView(withText("TEST_TITLE")).check(matches(isDisplayed()))
        onView(withText("TEST_TITLE2")).check(matches(isDisplayed()))
        onView(withText("TEST_TITLE3")).check(matches(isDisplayed()))
    }

}