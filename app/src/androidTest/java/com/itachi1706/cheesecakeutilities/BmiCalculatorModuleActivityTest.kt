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
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
@Ignore("Disable and pending rewrite")
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
        val mainMenu = onView(allOf(withId(R.id.text1), withText("BMI Calculator"), childAtPosition(allOf(withId(R.id.main_menu_recycler_view),
                childAtPosition(withClassName(Matchers.`is`("android.widget.LinearLayout")), 0)), 6), isDisplayed()))
        mainMenu.perform(click())

        val calculateButton = onView(allOf(withId(R.id.btnCalculate), withText("Calculate BMI"), isDisplayed()))
        val resultView = onView(allOf(withId(R.id.tvResults), isDisplayed()))

        // Test Normal
        updateHeightWeight("179", "80")
        calculateButton.perform(click())
        resultView.check(matches(withText("BMI Value: 24.97\nStatus: Normal")))

        // Test Overweight
        updateHeightWeight(null, "81")
        calculateButton.perform(click())
        resultView.check(matches(withText("BMI Value: 25.28\nStatus: Overweight")))

        // Test Underweight (55) - 17.17
        updateHeightWeight(null, "55")
        calculateButton.perform(click())
        resultView.check(matches(withText("BMI Value: 17.17\nStatus: Underweight")))

        // Test Moderately Underweight (52) - 16.23
        updateHeightWeight(null, "52")
        calculateButton.perform(click())
        resultView.check(matches(withText("BMI Value: 16.23\nStatus: Moderately Underweight")))

        // Test Severely Underweight (51) - 15.92
        updateHeightWeight(null, "51")
        calculateButton.perform(click())
        resultView.check(matches(withText("BMI Value: 15.92\nStatus: Severely Underweight")))

        // Test Class I Obese (112) - 34.96
        updateHeightWeight(null, "112")
        calculateButton.perform(click())
        resultView.check(matches(withText("BMI Value: 34.96\nStatus: Class I Obese")))

        // Test Class II Obese (113) - 35.27
        updateHeightWeight(null, "113")
        calculateButton.perform(click())
        resultView.check(matches(withText("BMI Value: 35.27\nStatus: Class II Obese")))

        // Test Class III Obese (150) - 46.82
        updateHeightWeight(null, "150")
        calculateButton.perform(click())
        resultView.check(matches(withText("BMI Value: 46.82\nStatus: Class III Obese")))
    }

    private fun updateHeightWeight(height: String?, weight: String?) {
        val heightEditText = onView(allOf(withId(R.id.etHeight), isDisplayed()))
        val weightEditText = onView(allOf(withId(R.id.etWeight), isDisplayed()))
        if (height != null) {
            heightEditText.perform(click())
            heightEditText.perform(replaceText(height), closeSoftKeyboard())
        }
        if (weight != null) {
            weightEditText.perform(click())
            weightEditText.perform(replaceText(weight), closeSoftKeyboard())
        }
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
