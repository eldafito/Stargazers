package com.gianessi.stargazers.activities;


import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.gianessi.stargazers.R;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.not;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class SearchUsersActivityTest {


    private static final int NETWORK_DELAY_MILLIS = 5000;
    private static final String VERIFIED_USERNAME = "eldafito";
    private static final String CONFUSIN_STRING = "fsdkfslkdfjsd";


    @Rule
    public ActivityTestRule<UsersListActivity> mActivityRule = new ActivityTestRule<>(
            UsersListActivity.class);


    @Test
    public void changeText_sameActivity() {
        onView(withId(R.id.search))
                .perform(typeText(VERIFIED_USERNAME),closeSoftKeyboard());

        // Wait API response
        try {
            Thread.sleep(NETWORK_DELAY_MILLIS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Check there's at least one user
        onView(withId(R.id.users_empty_placeholder)).check(matches(not(isDisplayed())));

        onView(withId(R.id.search)).perform(typeText(CONFUSIN_STRING),closeSoftKeyboard());

        try {
            Thread.sleep(NETWORK_DELAY_MILLIS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        onView(withId(R.id.users_empty_placeholder)).check(matches(isDisplayed()));

    }
}
