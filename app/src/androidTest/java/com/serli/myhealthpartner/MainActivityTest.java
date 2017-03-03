package com.serli.myhealthpartner;

import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.support.test.espresso.action.ViewActions;
import android.support.test.espresso.intent.Intents;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.View;
import android.widget.NumberPicker;

import junit.framework.Assert;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static android.support.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withSpinnerText;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

@RunWith(AndroidJUnit4.class)
public class MainActivityTest {

  /*  @Rule
    public ActivityTestRule<MainActivity> mainActivityTestRule = new IntentsTestRule<>(MainActivity.class);

    @Test
    public void setTime() {
        onView(withId(R.id.second_picker))
                .perform(click());
        for (int i = 0; i < 60; ++i) {
            onView(allOf(withId(R.id.second_picker), isAssignableFrom(NumberPicker.class)))
                    .perform(setNumber(i));
            onView(allOf(withId(R.id.second_picker), isAssignableFrom(NumberPicker.class)))
                    .check(matches(hasNumber(i)));

            onView(allOf(withId(R.id.minute_picker), isAssignableFrom(NumberPicker.class)))
                    .perform(setNumber(i));
            onView(allOf(withId(R.id.minute_picker), isAssignableFrom(NumberPicker.class)))
                    .check(matches(hasNumber(i)));
        }
        ViewActions.closeSoftKeyboard();
    }

    @Test
    public void setActivity() {
        String[] sportActivity = InstrumentationRegistry.getTargetContext().getResources().getStringArray(R.array.sport_activity);

        onView(withId(R.id.activity_spinner))
                .check(matches(withSpinnerText(containsString(sportActivity[0]))));
        for (int i = 1; i < sportActivity.length; ++i) {
            onView(withId(R.id.activity_spinner))
                    .perform(click());
            onData(allOf(is(instanceOf(String.class)), is(sportActivity[i])))
                    .perform(click());
            onView(withId(R.id.activity_spinner))
                    .check(matches(withSpinnerText(containsString(sportActivity[i]))));
        }
    }

    @Test
    public void clickStart() {
        onView(withId(R.id.button_start_stop))
                .check(matches(withText(R.string.button_start)));
        onView(allOf(withId(R.id.second_picker), isAssignableFrom(NumberPicker.class)))
                .perform(setNumber(1));
        onView(withId(R.id.button_start_stop))
                .perform(click());
        onView(withId(R.id.button_start_stop))
                .check(matches(withText(R.string.button_stop)));
        Assert.assertTrue(AccelerometerService.isRunning());

        onView(withId(R.id.button_start_stop))
                .perform(click());
        onView(withId(R.id.button_start_stop))
                .check(matches(withText(R.string.button_start)));
        Assert.assertFalse(AccelerometerService.isRunning());
    }

    @Test
    public void openProfile() {
        openActionBarOverflowOrOptionsMenu(InstrumentationRegistry.getInstrumentation().getTargetContext());
        onView(withText(R.string.action_profile))
                .perform(click());
        Intents.intended(hasComponent("com.serli.myhealthpartner.ProfileActivity"));
    }

    @Test
    public void displayData() {
        onView(withId(R.id.data_list_layout))
                .check(matches(not(isDisplayed())));
        openActionBarOverflowOrOptionsMenu(InstrumentationRegistry.getInstrumentation().getTargetContext());
        onView(withText(R.string.action_show_datas))
                .perform(click());
        onView(withId(R.id.data_list_layout))
                .check(matches(isDisplayed()));
    }

    public static ViewAction setNumber(final int number) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return ViewMatchers.isAssignableFrom(NumberPicker.class);
            }

            @Override
            public String getDescription() {
                return "Set the passed number into the NumberPicker";
            }

            @Override
            public void perform(UiController uiController, View view) {
                NumberPicker np = (NumberPicker) view;
                np.setValue(number);
            }
        };
    }

    public static Matcher<View> hasNumber(final int number) {
        return new BaseMatcher<View>() {
            @Override
            public boolean matches(Object item) {
                return ((NumberPicker) item).getValue() == number;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("getValue should return ").appendValue(number);
            }

            @Override
            public void describeMismatch(Object item, Description description) {
                description.appendText(" was ").appendValue(((NumberPicker) item).getValue());
            }
        };
    }
*/
}
