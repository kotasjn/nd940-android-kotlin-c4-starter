package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.android.architecture.blueprints.todoapp.MainCoroutineRule
import com.example.android.architecture.blueprints.todoapp.getOrAwaitValue
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config

@Config(sdk = [Build.VERSION_CODES.O_MR1])
@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {

    private lateinit var dataSource: FakeDataSource

    private lateinit var viewModel: RemindersListViewModel

    private lateinit var context: Application

    private lateinit var reminderList: MutableList<ReminderDTO>

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Before
    fun init() {
        stopKoin()
        reminderList = mutableListOf(
            ReminderDTO(
                "TEST_TITLE1",
                "TEST_DESCRIPTION1",
                "TEST_LOCATION1",
                60.0,
                25.0
            ),
            ReminderDTO(
                "TEST_TITLE2",
                "TEST_DESCRIPTION2",
                "TEST_LOCATION2",
                70.0,
                35.0
            )
        )
        context = ApplicationProvider.getApplicationContext()
        dataSource = FakeDataSource(reminderList)
        viewModel = RemindersListViewModel(context, dataSource)
    }

    @Test
    fun loadRemindersNotEmpty() {
        viewModel.loadReminders()
        val remindersList = viewModel.remindersList.getOrAwaitValue()
        Assert.assertThat(remindersList.isNotEmpty(), Matchers.`is`(true))
    }

    @Test
    fun showNoDataIsInvalid() {
        viewModel.loadReminders()
        val showNoData = viewModel.showNoData.getOrAwaitValue()
        Assert.assertThat(showNoData, Matchers.`is`(false))
    }

    @Test
    fun loadRemindersExpectSuccess() = runBlockingTest {
        dataSource.returnError = false
        viewModel.loadReminders()
        MatcherAssert.assertThat(
            viewModel.showNoData.getOrAwaitValue(),
            CoreMatchers.`is`(false)
        )
    }

    @Test
    fun loadRemindersExpectError() = runBlockingTest {
        dataSource.returnError = true
        viewModel.loadReminders()
        MatcherAssert.assertThat(
            viewModel.showNoData.getOrAwaitValue(),
            CoreMatchers.`is`(true)
        )
    }

}