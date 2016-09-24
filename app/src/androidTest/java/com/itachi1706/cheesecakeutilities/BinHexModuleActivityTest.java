package com.itachi1706.cheesecakeutilities;


import android.support.test.espresso.ViewInteraction;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class BinHexModuleActivityTest {

    @Rule
    public ActivityTestRule<MainMenuActivity> mActivityTestRule = new ActivityTestRule<>(MainMenuActivity.class);

    @Test
    public void binHexModuleActivityTest() {
        ViewInteraction recyclerView = onView(
                allOf(withId(R.id.main_menu_recycler_view), isDisplayed()));
        recyclerView.perform(actionOnItemAtPosition(0, click()));

        ViewInteraction appCompatEditText = onView(
                withId(R.id.input_binhex_string));
        appCompatEditText.perform(scrollTo(), click());

        ViewInteraction appCompatEditText2 = onView(
                withId(R.id.input_binhex_string));
        appCompatEditText2.perform(scrollTo(), replaceText("Test"), closeSoftKeyboard());

        ViewInteraction appCompatButton = onView(
                allOf(withId(R.id.btn_binhex_translate), withText("TRANSLATE!")));
        appCompatButton.perform(scrollTo(), click());

        ViewInteraction editText = onView(
                allOf(withId(R.id.tv_binhex_results),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.svBinHex),
                                        0),
                                7),
                        isDisplayed()));
        editText.check(matches(withText("01010100011001010111001101110100")));

        ViewInteraction editText2 = onView(
                allOf(withId(R.id.input_binhex_string),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.svBinHex),
                                        0),
                                2),
                        isDisplayed()));
        editText2.check(matches(withText("Test")));

        ViewInteraction appCompatButton2 = onView(
                allOf(withId(R.id.btn_binhex_clear), withText("CLEAR"),
                        withParent(allOf(withId(R.id.tr_binhex_action),
                                withParent(withId(R.id.table_binhex_action))))));
        appCompatButton2.perform(scrollTo(), click());

        ViewInteraction appCompatEditText3 = onView(
                withId(R.id.input_binhex_string));
        appCompatEditText3.perform(scrollTo(), click());

        ViewInteraction appCompatEditText4 = onView(
                withId(R.id.input_binhex_string));
        appCompatEditText4.perform(scrollTo(), replaceText("01000001"), closeSoftKeyboard());

        ViewInteraction appCompatRadioButton = onView(
                allOf(withId(R.id.rb_binhex_decode), withText("Decode"),
                        withParent(allOf(withId(R.id.radio_binhex_action),
                                withParent(withId(R.id.tr_binhex_01)))),
                        isDisplayed()));
        appCompatRadioButton.perform(click());

        ViewInteraction appCompatButton3 = onView(
                allOf(withId(R.id.btn_binhex_translate), withText("TRANSLATE!")));
        appCompatButton3.perform(scrollTo(), click());

        ViewInteraction editText3 = onView(
                allOf(withId(R.id.input_binhex_string),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.svBinHex),
                                        0),
                                2),
                        isDisplayed()));
        editText3.check(matches(withText("01000001")));

        ViewInteraction editText4 = onView(
                allOf(withId(R.id.tv_binhex_results),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.svBinHex),
                                        0),
                                7),
                        isDisplayed()));
        editText4.check(matches(withText("A")));

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
