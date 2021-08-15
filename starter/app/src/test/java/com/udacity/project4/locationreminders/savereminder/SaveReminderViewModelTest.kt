package com.udacity.project4.locationreminders.savereminder

import android.app.Application
import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.android.architecture.blueprints.todoapp.MainCoroutineRule
import com.example.android.architecture.blueprints.todoapp.getOrAwaitValue
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config

@Config(sdk = [Build.VERSION_CODES.O_MR1])
@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {

    private lateinit var dataSource: FakeDataSource

    private lateinit var viewModel: SaveReminderViewModel

    private lateinit var context: Application

    private lateinit var reminder: ReminderDataItem

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @ExperimentalCoroutinesApi
    @get:Rule
    private var mainCoroutineScopeRule = MainCoroutineRule()

    @Before
    fun init() {
        stopKoin()
        reminder = ReminderDataItem(
            "TEST_TITLE1",
            "TEST_DESCRIPTION1",
            "TEST_LOCATION1",
            60.0,
            25.0
        )
        context = ApplicationProvider.getApplicationContext()
        dataSource = FakeDataSource()
        viewModel = SaveReminderViewModel(context, dataSource)
    }

    @Test
    fun validateValidReminder() {
        val valid = viewModel.validateEnteredData(reminder)
        assertThat(valid, `is`(true))
    }

    @Test
    fun validateInvalidReminderTitle() {
        reminder.title = null
        val valid = viewModel.validateEnteredData(reminder)
        assertThat(valid, `is`(false))
        assertThat(viewModel.showSnackBarInt.getOrAwaitValue(), `is`(R.string.err_enter_title))
    }

    @Test
    fun validateInvalidReminderLocation() {
        reminder.location = null
        val valid = viewModel.validateEnteredData(reminder)
        assertThat(valid, `is`(false))
        assertThat(viewModel.showSnackBarInt.getOrAwaitValue(), `is`(R.string.err_select_location))
    }

    @Test
    fun saveReminder() = runBlockingTest {
        viewModel.saveReminder(reminder)
        val showToastValue = viewModel.showToast.getOrAwaitValue()
        assertEquals(showToastValue, context.resources.getString(R.string.reminder_saved))
    }

    @Test
    fun checkLoadingWhenSavingReminder() {
        viewModel.saveReminder(reminder)
        mainCoroutineScopeRule.resumeDispatcher()
        val loading = viewModel.showLoading.getOrAwaitValue()
        assertThat(loading, `is`(false))
    }

}