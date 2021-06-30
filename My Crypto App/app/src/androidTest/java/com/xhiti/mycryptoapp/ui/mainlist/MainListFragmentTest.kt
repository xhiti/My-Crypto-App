package com.baruckis.kriptofolio.ui.mainlist

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4
import com.baruckis.kriptofolio.R
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class MainListFragmentTest {

    @Rule
    @JvmField
    val mainActivityTestRule: ActivityTestRule<MainActivity> = ActivityTestRule(MainActivity::class.java)

    @Test
    @Throws(Exception::class)
    fun clickAddFab_opensAddCryptoUi() {
        onView(withId(R.id.fab)).perform(click())
        onView(withId(R.id.coordinator_add_search)).check(matches(isDisplayed()))
    }

}