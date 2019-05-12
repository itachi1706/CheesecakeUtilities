package com.itachi1706.cheesecakeutilities


import androidx.test.espresso.ViewInteraction
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import android.view.View
import android.view.ViewGroup
import android.view.ViewParent
import android.view.WindowManager
import androidx.recyclerview.widget.RecyclerView

import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import org.hamcrest.Matchers
import org.hamcrest.Matchers.allOf

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
        val appCompatTextView = onView(allOf(withId(R.id.text1), withText("HTC Serial Number Identification"), childAtPosition(allOf(withId(R.id.main_menu_recycler_view),
                childAtPosition(ViewMatchers.withClassName(Matchers.`is`("android.widget.LinearLayout")), 0)), 2), isDisplayed()))
        appCompatTextView.perform(click())

        val textInputEditText = onView(
                allOf(withId(R.id.htc_sn_serialField), isDisplayed()))
        textInputEditText.perform(click())

        val textInputEditText2 = onView(
                allOf(withId(R.id.htc_sn_serialField), isDisplayed()))
        textInputEditText2.perform(replaceText("SH1111111111"), closeSoftKeyboard())

        val appCompatButton = onView(
                allOf(withId(R.id.btn_htc_sn_search), withText("Search"), isDisplayed()))
        appCompatButton.perform(click())

        val editText = onView(
                allOf(withId(R.id.htc_sn_serialField),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.til_htc_sn_serialField),
                                        0),
                                0),
                        isDisplayed()))
        editText.check(matches(withText("SH1111111111")))

        val textView = onView(
                allOf(withId(R.id.tv_htc_sn_result),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                3),
                        isDisplayed()))
        textView.check(matches(withText("Result for S/N SH1111111111\nManufactured At: Shanghai, China\nHtcSerialNumberDates of Manufacture: 1 January 2011")))
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
