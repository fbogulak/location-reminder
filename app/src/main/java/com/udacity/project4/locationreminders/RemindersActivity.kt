package com.udacity.project4.locationreminders

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import com.firebase.ui.auth.AuthUI
import com.udacity.project4.R
import com.udacity.project4.locationreminders.reminderslist.ReminderListFragmentDirections
import kotlinx.android.synthetic.main.activity_reminders.*

class RemindersActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reminders)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.logout -> {
                AuthUI.getInstance()
                    .signOut(this)
                    .addOnCompleteListener {
                        (nav_host_fragment as NavHostFragment).navController.navigate(
                            ReminderListFragmentDirections.actionReminderListFragmentToAuthenticationFragment()
                        )
                    }
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
