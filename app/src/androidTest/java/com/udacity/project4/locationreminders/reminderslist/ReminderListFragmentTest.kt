package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import android.os.Bundle
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.FakeAndroidTestDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify


@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class ReminderListFragmentTest : AutoCloseKoinTest() {

    // Use a fake data source to be injected into the viewmodel
    private lateinit var dataSource: FakeAndroidTestDataSource

    @Before
    fun init() {
        stopKoin()//stop the original app koin

        val appContext = getApplicationContext<Application>()
        dataSource = FakeAndroidTestDataSource()

        val myModule = module {
            viewModel {
                RemindersListViewModel(
                    appContext,
                    dataSource
                )
            }
        }
        //declare a new koin module
        startKoin {
            modules(listOf(myModule))
        }
    }

    @Test
    fun clickAddReminderFAB_navigateToSaveReminderFragment() {
        // GIVEN - On the home screen
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        val navController = mock(NavController::class.java)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        // WHEN - Click on the add reminder FAB
        onView(withId(R.id.addReminderFAB)).perform(click())

        // THEN - Verify that we navigate to the save reminder screen
        verify(navController).navigate(
            ReminderListFragmentDirections.toSaveReminder()
        )
    }

    @Test
    fun oneActiveReminder_displayedInUi() = runBlockingTest {
        // GIVEN - Add reminder to the data source
        dataSource.saveReminder(ReminderDTO("title1", "description1", "location1", 1.0, 1.1))

        // WHEN - Reminder list fragment launched to display reminders
        launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)

        // THEN - Reminder title, description and location are both displayed on the screen and correct
        onView(withId(R.id.title)).check(matches(isDisplayed()))
        onView(withId(R.id.title)).check(matches(withText("title1")))
        onView(withId(R.id.description)).check(matches(isDisplayed()))
        onView(withId(R.id.description)).check(matches(withText("description1")))
        onView(withId(R.id.location)).check(matches(isDisplayed()))
        onView(withId(R.id.location)).check(matches(withText("location1")))
    }

    @Test
    fun errorLoadingReminders_snackbarDisplayedInUi(){
        // Given error loading reminders from DB
        dataSource.setReturnError(true)

        // When on reminder list screen
        launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)

        // Then snackbar with error message is displayed
        onView(withId(com.google.android.material.R.id.snackbar_text)).check(matches(isDisplayed()))

    }

}