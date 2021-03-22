package com.udacity.project4.locationreminders.reminderslist

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Transformations
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.koin.dsl.koinApplication
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.Is.`is`
import org.mockito.BDDMockito
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
@Config(sdk = [Build.VERSION_CODES.P])
class RemindersListViewModelTest {

    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    // Use a fake data source to be injected into the viewmodel
    private lateinit var dataSource: FakeDataSource

    // Subject under test
    private lateinit var viewModel: RemindersListViewModel

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private val reminder1 = ReminderDTO("title1", "description1", "location1", 1.0, 1.1)
    private val reminder2 = ReminderDTO("title2", "description2", "location2", 2.0, 2.2)
    private val reminder3 = ReminderDTO("title3", "description3", "location3", 3.0, 3.3)

    private val reminders = listOf(reminder1, reminder2, reminder3)

    @Before
    fun setupViewModel() {
        stopKoin()
        dataSource = FakeDataSource(reminders.toMutableList())
        viewModel =
            RemindersListViewModel(ApplicationProvider.getApplicationContext(), dataSource)
    }

    @Test
    fun loadReminders_loadingData() {

        // When loading reminders from data source
        viewModel.loadReminders()

        // Then remindersList updates
        val value = viewModel.remindersList.getOrAwaitValue()
        assertThat(value.toReminderDTOs(), `is`(reminders))
    }

    @Test
    fun loadReminders_shouldReturnError() {
        // Given data source returning error
        dataSource.setReturnError(true)

        // When loading reminders from data source
        viewModel.loadReminders()

        // Then error snackbar is shown
        assertThat(viewModel.showSnackBar.getOrAwaitValue().isNullOrEmpty(), `is`(false))
    }

    @Test
    fun loadReminders_checkLoading() {
        // Pause dispatcher so you can verify initial values.
        mainCoroutineRule.pauseDispatcher()

        // When loading starts
        viewModel.loadReminders()

        // Then progress indicator is shown.
        assertThat(viewModel.showLoading.getOrAwaitValue(), `is`(true))

        // When execute pending coroutines actions
        mainCoroutineRule.resumeDispatcher()

        // Then progress indicator is hidden.
        assertThat(viewModel.showLoading.getOrAwaitValue(), `is`(false))
    }

    @Test
    fun invalidateShowNoData_noData_showNoData() = mainCoroutineRule.runBlockingTest{
        // Given empty data source
        dataSource.deleteAllReminders()

        // When load reminders
        viewModel.loadReminders()

        // Then no data indicator is shown
        assertThat(viewModel.showNoData.getOrAwaitValue(), `is`(true))
    }

    @Test
    fun invalidateShowNoData_someData_hideNoData() {
        // When load reminders
        viewModel.loadReminders()

        // Then no data indicator is hidden
        assertThat(viewModel.showNoData.getOrAwaitValue(), `is`(false))
    }

    private fun List<ReminderDataItem>.toReminderDTOs(): List<ReminderDTO> {
        val result = mutableListOf<ReminderDTO>()
        forEach {
            result.add(
                ReminderDTO(
                    it.title,
                    it.description,
                    it.location,
                    it.latitude,
                    it.longitude,
                    it.id
                )
            )
        }
        return result.toList()
    }
}