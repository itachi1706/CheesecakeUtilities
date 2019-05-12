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
class BmiCalculatorModuleActivityTest {

    @Rule
    @JvmField
    var mActivityTestRule = ActivityTestRule(MainMenuActivity::class.java)

    @Before
    fun unlockScreen() {
        TestHelper.wakeUpDevice(mActivityTestRule)
    }

    @Test
    fun bmiCalculatorModuleActivityTest() {
        val appCompatTextView = onView(allOf(withId(R.id.text1), withText("BMI Calculator"), childAtPosition(allOf(withId(R.id.main_menu_recycler_view),
                childAtPosition(withClassName(Matchers.`is`("android.widget.LinearLayout")), 0)), 7), isDisplayed()))
        appCompatTextView.perform(click())

        val textInputEditText = onView(allOf(withId(R.id.etHeight), isDisplayed()))
        textInputEditText.perform(click())

        val textInputEditText2 = onView(allOf(withId(R.id.etHeight), isDisplayed()))
        textInputEditText2.perform(replaceText("179"), closeSoftKeyboard())

        val textInputEditText3 = onView(allOf(withId(R.id.etWeight), isDisplayed()))
        textInputEditText3.perform(replaceText("80"), closeSoftKeyboard())

        val appCompatButton = onView(allOf(withId(R.id.btnCalculate), withText("Calculate BMI"), isDisplayed()))
        appCompatButton.perform(click())

        val textView = onView(allOf(withId(R.id.tvResults), isDisplayed()))
        textView.check(matches(withText("BMI Value: 24.97\nStatus: Normal")))

        val textInputEditText4 = onView(allOf(withId(R.id.etWeight), withText("80"), isDisplayed()))
        textInputEditText4.perform(click())

        val textInputEditText5 = onView(allOf(withId(R.id.etWeight), isDisplayed()))
        textInputEditText5.perform(replaceText("90"), closeSoftKeyboard())

        val appCompatButton2 = onView(allOf(withId(R.id.btnCalculate), withText("Calculate BMI"), isDisplayed()))
        appCompatButton2.perform(click())

        val textView2 = onView(allOf(withId(R.id.tvResults), isDisplayed()))
        textView2.check(matches(withText("BMI Value: 28.09\nStatus: Overweight")))

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
