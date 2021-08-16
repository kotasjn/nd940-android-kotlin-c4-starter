package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {

    private lateinit var repository: RemindersLocalRepository

    private lateinit var database: RemindersDatabase

    private lateinit var reminder: ReminderDTO

    private lateinit var reminderList: MutableList<ReminderDTO>

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun init() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).allowMainThreadQueries().build()

        repository = RemindersLocalRepository(
            database.reminderDao(),
            Dispatchers.Main
        )
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

    @After
    fun cleanUp() {
        database.close()
    }

    @Test
    fun saveReminderAndGetById() = runBlocking {
        // GIVEN - save a reminder
        repository.saveReminder(reminder)

        // WHEN - Get the reminder by id from the database
        val reminderResult = repository.getReminder(reminder.id)

        // THEN - The loaded reminder contains the expected values
        reminderResult as Result.Success
        assertThat(reminderResult.data.id, `is`(reminder.id))
        assertThat(reminderResult.data.title, `is`(reminder.title))
        assertThat(reminderResult.data.description, `is`(reminder.description))
        assertThat(reminderResult.data.location, `is`(reminder.location))
        assertThat(reminderResult.data.latitude, `is`(reminder.latitude))
        assertThat(reminderResult.data.longitude, `is`(reminder.longitude))
    }

    @Test
    fun getNonExistingReminder() = runBlocking {
        // WHEN - Get the reminder by id from the database
        val reminderResult = repository.getReminder(reminder.id)

        // THEN - The loaded reminder contains the expected values
        reminderResult as Result.Error
        assertThat(reminderResult.message, `is`("Reminder not found!"))
    }


    @Test
    fun getReminders() = runBlocking {
        // GIVEN - save a reminder
        reminderList.forEach {
            repository.saveReminder(it)
        }

        // WHEN - Get the reminder by id from the database
        val reminderListResult = repository.getReminders()

        // THEN - The loaded reminders correspond to test reminders

        assert(reminderListResult is Result.Success)

        reminderListResult as Result.Success
        assertThat(reminderListResult.data.size, `is`(reminderList.size))

        reminderListResult.data.forEachIndexed { index, reminder ->
            assertThat(reminder.id, `is`(reminderList[index].id))
            assertThat(reminder.title, `is`(reminderList[index].title))
            assertThat(reminder.description, `is`(reminderList[index].description))
            assertThat(reminder.location, `is`(reminderList[index].location))
            assertThat(reminder.latitude, `is`(reminderList[index].latitude))
            assertThat(reminder.longitude, `is`(reminderList[index].longitude))
        }
    }

    @Test
    fun deleteReminders() = runBlocking {
        // GIVEN - save a reminder
        reminderList.forEach {
            repository.saveReminder(it)
        }

        // WHEN - Delete reminders from the database
        repository.deleteAllReminders()

        // THEN - Check database is empty
        val reminderListResult = repository.getReminders()

        assert(reminderListResult is Result.Success)

        reminderListResult as Result.Success
        assertThat(reminderListResult.data.size, `is`(0))
    }
}