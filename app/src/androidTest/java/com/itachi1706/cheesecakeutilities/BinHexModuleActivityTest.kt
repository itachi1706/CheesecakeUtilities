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
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.allOf
import org.hamcrest.TypeSafeMatcher
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@Ignore("Fails on CI. We will disable and rewrite all the tests in the future")
@RunWith(AndroidJUnit4::class)
class BinHexModuleActivityTest {

    @Rule
    @JvmField
    var mActivityTestRule = ActivityTestRule(MainMenuActivity::class.java)

    @Before
    fun unlockScreen() {
        TestHelper.wakeUpDevice(mActivityTestRule)
    }

    @Test
    fun binHexModuleActivityTest() {
        val mainMenu = onView(allOf(withId(R.id.text1), withText("String to Binary/Hex Converter"), childAtPosition(allOf(withId(R.id.main_menu_recycler_view),
                childAtPosition(withClassName(`is`("android.widget.LinearLayout")), 0)), 0), isDisplayed()))
        mainMenu.perform(click())

        // Test Encode
        val inputEditText = onView(allOf(withId(R.id.input_binhex_string), childAtPosition(childAtPosition(withId(R.id.svBinHex), 0), 2)))
        inputEditText.perform(scrollTo(), replaceText("Test"), closeSoftKeyboard())

        val translateButton = onView(allOf(withId(R.id.btn_binhex_translate), withText("TRANSLATE!"), childAtPosition(childAtPosition(withId(R.id.svBinHex), 0), 5)))
        translateButton.perform(scrollTo(), click())

        val outputEditText = onView(allOf(withId(R.id.tv_binhex_results), childAtPosition(childAtPosition(withId(R.id.svBinHex), 0), 7), isDisplayed()))
        outputEditText.check(matches(withText("01010100011001010111001101110100")))

        val clearButton = onView(allOf(withId(R.id.btn_binhex_clear), withText("CLEAR"), childAtPosition(allOf(withId(R.id.tr_binhex_action),
                childAtPosition(withId(R.id.table_binhex_action), 0)), 2)))
        clearButton.perform(scrollTo(), click())

        // Test Decode
        val decodeOption = onView(allOf(withId(R.id.rb_binhex_decode), withText("Decode"), childAtPosition(allOf(withId(R.id.radio_binhex_action),
                childAtPosition(withId(R.id.tr_binhex_01), 2)), 1), isDisplayed()))
        decodeOption.perform(click())
        inputEditText.perform(scrollTo(), click())
        inputEditText.perform(scrollTo(), replaceText("01000001"), closeSoftKeyboard())
        translateButton.perform(scrollTo(), click())
        outputEditText.check(matches(withText("A")))
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
                return parent is ViewGroup && parentMatcher.matches(parent)
                        && view == parent.getChildAt(position)
            }
        }
    }
}
