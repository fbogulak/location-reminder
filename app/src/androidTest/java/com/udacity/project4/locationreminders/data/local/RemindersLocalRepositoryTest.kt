package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result.Success
import com.udacity.project4.locationreminders.data.dto.Result.Error
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert
import org.hamcrest.MatcherAssert.assertThat
import org.junit.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {

    private lateinit var localRepository: RemindersLocalRepository
    private lateinit var database: RemindersDatabase

    // Executes each reminder synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()
    
    @Before
    fun setup() {
        // Using an in-memory database for testing, because it doesn't survive killing the process.
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()
        
        localRepository = RemindersLocalRepository(
            database.reminderDao(),
            Dispatchers.Main
        )
    }

    @After
    fun cleanUp() {
        database.close()
    }
    
    // runBlocking is used here because of https://github.com/Kotlin/kotlinx.coroutines/issues/1204
    // TODO: Replace with runBlockingTest once issue is resolved
    @Test
    fun saveReminder_retrievesReminder() = runBlocking {
        // GIVEN - A new reminder saved in the database.
        val newReminder = ReminderDTO("title", "description", "location", 1.0, 1.1)
        localRepository.saveReminder(newReminder)

        // WHEN  - Reminder retrieved by ID.
        val result = localRepository.getReminder(newReminder.id)

        // THEN - Same reminder is returned.
        assertThat(result is Success, `is`(true))
        result as Success
        assertThat(result.data.title, `is`("title"))
        assertThat(result.data.description, `is`("description"))
        assertThat(result.data.location, `is`("location"))
        assertThat(result.data.latitude, `is`(1.0))
        assertThat(result.data.longitude, `is`(1.1))
    }

    // runBlocking is used here because of https://github.com/Kotlin/kotlinx.coroutines/issues/1204
    // TODO: Replace with runBlockingTest once issue is resolved
    @Test
    fun saveReminders_retrieveReminders() = runBlocking {
        // Given - save reminders
        val reminders = listOf(
            ReminderDTO("title1", "description1", "location1", 1.0, 1.1),
            ReminderDTO("title2", "description2", "location2", 2.0, 2.2),
            ReminderDTO("title3", "description3", "location3", 3.0, 3.3)
        )
        for (reminder in reminders)
            localRepository.saveReminder(reminder)

        // When - get reminders from repository
        val result = localRepository.getReminders()

        // Then - retrieved reminders are equals to the original
        assertThat(result is Success, `is`(true))
        result as Success
        assertThat(result.data, `is`(reminders))
    }

    // runBlocking is used here because of https://github.com/Kotlin/kotlinx.coroutines/issues/1204
    // TODO: Replace with runBlockingTest once issue is resolved
    @Test
    fun getReminder_shouldReturnError() = runBlocking {
        // Given new reminder saved in repository
        val newReminder = ReminderDTO("title", "description", "location", 1.0, 1.1)
        localRepository.saveReminder(newReminder)

        // When delete all reminders from repository
        localRepository.deleteAllReminders()

        // Then error is returned when getting reminder by id
        val result = localRepository.getReminder(newReminder.id)
        assertThat(result is Error, `is`(true))
    }
}