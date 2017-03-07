package com.serli.myhealthpartner;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.action.ViewActions;
import android.support.test.espresso.contrib.PickerActions;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.View;
import android.widget.DatePicker;

import junit.framework.Assert;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static android.support.test.espresso.matcher.ViewMatchers.withHint;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withSpinnerText;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

@RunWith(AndroidJUnit4.class)
public class ProfileActivityTest {

    @Rule
    public ActivityTestRule<ProfileActivity> profileActivityTestRule = new ActivityTestRule<>(ProfileActivity.class);

    @Test
    public void setGender() {
        Context context = InstrumentationRegistry.getTargetContext();
//
//        onView(withId(R.id.spinner_gender))
//                .check(matches(withSpinnerText(containsString(context.getResources().getStringArray(R.array.gender)[0]))));
//        onView(withId(R.id.spinner_gender))
//                .perform(click());
//        onData(allOf(is(instanceOf(String.class)), is(context.getResources().getStringArray(R.array.gender)[1])))
//                .perform(click());
//        onView(withId(R.id.spinner_gender))
//                .check(matches(withSpinnerText(containsString(context.getResources().getStringArray(R.array.gender)[1]))));
    }

    @Test
    public void setHeight() {
        onView(withId(R.id.editText_height))
                .check(matches(withHint(R.string.hint_height)));
        onView(withId(R.id.editText_height))
                .perform(replaceText("150"), ViewActions.closeSoftKeyboard());
        onView(withId(R.id.editText_height))
                .check(matches(withText("150")));
    }

    @Test
    public void setWeight() {
        onView(withId(R.id.editText_weight))
                .check(matches(withHint(R.string.hint_weight)));
        onView(withId(R.id.editText_weight))
                .perform(replaceText("50"), ViewActions.closeSoftKeyboard());
        onView(withId(R.id.editText_weight))
                .check(matches(withText("50")));
    }

    @Test
    public void setBirthday() {
        int year = 1990;
        int month = 11;
        int day = 15;
//        onView(withId(R.id.datePicker_birthday))
//                .perform(click());
//        onView(isAssignableFrom(DatePicker.class))
//                .perform(PickerActions.setDate(year, month + 1, day));
//        onView(isAssignableFrom(DatePicker.class))
//                .check(matches(hasDate(year, month, day)));
    }

    @Test
    public void clickValidate() {
//        onView(withId(R.id.button_validate))
//                .perform(click());
//        Assert.assertTrue(profileActivityTestRule.getActivity().isFinishing());
    }

    public static Matcher<View> hasDate(final int year, final int month, final int day) {
        return new BaseMatcher<View>() {
            @Override
            public boolean matches(Object item) {
                DatePicker datePicker = (DatePicker) item;
                return datePicker.getYear() == year && datePicker.getMonth() == month && datePicker.getDayOfMonth() == day;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("Date should be ").appendValue(year).appendText("/").appendValue(month).appendText("/").appendValue(day);
            }

            @Override
            public void describeMismatch(Object item, Description description) {
                DatePicker datePicker = (DatePicker) item;
                description.appendText(" was ").appendValue(datePicker.getYear()).appendText("/").appendValue(datePicker.getMonth()).appendText("/").appendValue(datePicker.getDayOfMonth());
            }
        };
    }

}
