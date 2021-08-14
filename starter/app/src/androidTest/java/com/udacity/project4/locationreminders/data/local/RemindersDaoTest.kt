package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {

    private lateinit var database: RemindersDatabase

    private lateinit var reminder: ReminderDTO

    private lateinit var reminderList: MutableList<ReminderDTO>

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun initDb() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).build()
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
    fun closeDb() = database.close()

    @Test
    fun saveReminderAndGetById() = runBlockingTest {
        // GIVEN - save a reminder
        database.reminderDao().saveReminder(reminder)

        // WHEN - Get the reminder by id from the database
        val loaded = database.reminderDao().getReminderById(reminder.id)

        // THEN - The loaded data contains the expected values
        assertThat<ReminderDTO>(loaded as ReminderDTO, notNullValue())
        assertThat(loaded.id, `is`(reminder.id))
        assertThat(loaded.title, `is`(reminder.title))
        assertThat(loaded.description, `is`(reminder.description))
        assertThat(loaded.location, `is`(reminder.location))
        assertThat(loaded.latitude, `is`(reminder.latitude))
        assertThat(loaded.longitude, `is`(reminder.longitude))
    }

    @Test
    fun getReminders() = runBlockingTest {
        // GIVEN - save a reminder
        reminderList.forEach {
            database.reminderDao().saveReminder(it)
        }

        // WHEN - Get reminders from the database
        val reminders = database.reminderDao().getReminders()

        // THEN - The loaded data contains the expected values
        assertThat(reminders.size, `is`(reminderList.size))

        reminders.forEachIndexed { index, reminder ->
            assertThat(reminder.id, `is`(reminderList[index].id))
            assertThat(reminder.title, `is`(reminderList[index].title))
            assertThat(reminder.description, `is`(reminderList[index].description))
            assertThat(reminder.location, `is`(reminderList[index].location))
            assertThat(reminder.latitude, `is`(reminderList[index].latitude))
            assertThat(reminder.longitude, `is`(reminderList[index].longitude))
        }
    }

    @Test
    fun deleteReminders() = runBlockingTest {
        // GIVEN - save a reminder
        reminderList.forEach {
            database.reminderDao().saveReminder(it)
        }

        // WHEN - Delete reminders from the database
        database.reminderDao().deleteAllReminders()
        val reminders = database.reminderDao().getReminders()

        // THEN - The loaded data contains the expected values
        assertThat(reminders.size, `is`(0))
    }

}