package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource(
    var reminders: MutableList<ReminderDTO>? = mutableListOf(),
    var returnError: Boolean = false
) :
    ReminderDataSource {

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        if (returnError) return Result.Error("Test error")
        reminders?.let { return Result.Success(ArrayList(it)) }
        return Result.Error("Reminders not found")
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        reminders?.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        if (returnError) return Result.Error("Test error")
        reminders?.let { reminders ->
            reminders.forEach {
                if (it.id == id) return Result.Success(it)
            }
        }
        return Result.Error("Reminders not found")
    }

    override suspend fun deleteAllReminders() {
        reminders?.clear()
    }

}