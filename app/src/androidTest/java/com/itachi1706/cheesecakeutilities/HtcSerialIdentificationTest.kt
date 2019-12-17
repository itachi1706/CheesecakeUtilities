package com.itachi1706.cheesecakeutilities


import android.view.View
import android.view.ViewGroup
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers
import org.hamcrest.Matchers.allOf
import org.hamcrest.TypeSafeMatcher
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class HtcSerialIdentificationTest {

    @Rule
    @JvmField
    var mActivityTestRule = ActivityTestRule(MainMenuActivity::class.java)

    @Before
    fun unlockScreen() {
        TestHelper.wakeUpDevice(mActivityTestRule)
    }

    @Test
    fun htcSerialIdentificationTest() {
        val mainMenu = onView(allOf(withId(R.id.text1), withText("HTC Serial Number Identification"), childAtPosition(allOf(withId(R.id.main_menu_recycler_view),
                childAtPosition(withClassName(Matchers.`is`("android.widget.LinearLayout")), 0)), 1), isDisplayed()))
        mainMenu.perform(click())

        val inputEditText = onView(allOf(withId(R.id.htc_sn_serialField), isDisplayed()))
        inputEditText.perform(click())
        inputEditText.perform(replaceText("SH1111111111"), closeSoftKeyboard())

        val searchButton = onView(allOf(withId(R.id.btn_htc_sn_search), withText("Search"), isDisplayed()))
        searchButton.perform(click())

        val serialEditText = onView(allOf(withId(R.id.htc_sn_serialField), childAtPosition(childAtPosition(withId(R.id.til_htc_sn_serialField), 0), 0), isDisplayed()))
        serialEditText.check(matches(withText("SH1111111111")))

        val resultTextView = onView(allOf(withId(R.id.tv_htc_sn_result), childAtPosition(childAtPosition(withId(android.R.id.content), 0), 3), isDisplayed()))
        resultTextView.check(matches(withText("Result for S/N SH1111111111\nManufactured At: Shanghai, China\nHtcSerialNumberDates of Manufacture: 1 January 2011")))
    }

    private fun childAtPosition(
            parentMatcher: Matcher<View>, position: Int): Matcher<View> {

        return object : TypeSafeMatcher<View>() {
            override fun describeTo(description: Description) {
                description.appendText("Child at position $position in parent ")
                parentMatcher.describeTo(description)
            }

            public override fun matchesSafely(view: View): Boolean {
                val parent = view.parent
                return (parent is ViewGroup && parentMatcher.matches(parent)
                        && view == parent.getChildAt(position))
            }
        }
    }
}
