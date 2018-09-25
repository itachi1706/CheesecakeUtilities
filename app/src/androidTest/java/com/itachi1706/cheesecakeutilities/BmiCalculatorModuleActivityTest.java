package com.itachi1706.cheesecakeutilities;


import androidx.test.espresso.ViewInteraction;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.WindowManager;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class BmiCalculatorModuleActivityTest {

    @Rule
    public ActivityTestRule<MainMenuActivity> mActivityTestRule = new ActivityTestRule<>(MainMenuActivity.class);

    @Before
    public void unlockScreen() {
        final MainMenuActivity activity = mActivityTestRule.getActivity();
        Runnable wakeUpDevice = () -> activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        activity.runOnUiThread(wakeUpDevice);
    }

    @Test
    public void bmiCalculatorModuleActivityTest() {
        ViewInteraction recyclerView = onView(
                allOf(withId(R.id.main_menu_recycler_view), isDisplayed()));
        recyclerView.perform(actionOnItemAtPosition(7, click()));

        ViewInteraction textInputEditText = onView(
                allOf(withId(R.id.etHeight), isDisplayed()));
        textInputEditText.perform(click());

        ViewInteraction textInputEditText2 = onView(
                allOf(withId(R.id.etHeight), isDisplayed()));
        textInputEditText2.perform(replaceText("179"), closeSoftKeyboard());

        ViewInteraction textInputEditText3 = onView(
                allOf(withId(R.id.etWeight), isDisplayed()));
        textInputEditText3.perform(replaceText("80"), closeSoftKeyboard());

        ViewInteraction appCompatButton = onView(
                allOf(withId(R.id.btnCalculate), withText("Calculate BMI"), isDisplayed()));
        appCompatButton.perform(click());

        ViewInteraction textView = onView(
                allOf(withId(R.id.tvResults),
                        isDisplayed()));
        textView.check(matches(withText("BMI Value: 24.97\nStatus: Normal")));

        ViewInteraction textInputEditText4 = onView(
                allOf(withId(R.id.etWeight), withText("80"), isDisplayed()));
        textInputEditText4.perform(click());

        ViewInteraction textInputEditText5 = onView(
                allOf(withId(R.id.etWeight), isDisplayed()));
        textInputEditText5.perform(replaceText("90"), closeSoftKeyboard());

        ViewInteraction appCompatButton2 = onView(
                allOf(withId(R.id.btnCalculate), withText("Calculate BMI"), isDisplayed()));
        appCompatButton2.perform(click());

        ViewInteraction textView2 = onView(
                allOf(withId(R.id.tvResults),
                        isDisplayed()));
        textView2.check(matches(withText("BMI Value: 28.09\nStatus: Overweight")));

    }

    private static Matcher<View> childAtPosition(
            final Matcher<View> parentMatcher, final int position) {

        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("Child at position " + position + " in parent ");
                parentMatcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                ViewParent parent = view.getParent();
                return parent instanceof ViewGroup && parentMatcher.matches(parent)
                        && view.equals(((ViewGroup) parent).getChildAt(position));
            }
        };
    }
}
