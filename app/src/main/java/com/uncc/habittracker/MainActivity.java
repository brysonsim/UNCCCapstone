package com.uncc.habittracker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.view.View;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.uncc.habittracker.data.model.Event;

public class MainActivity extends AppCompatActivity implements LoginFragment.LoginListener, SignUpFragment.SignUpListener,
        HabitsFragment.HabitsListener, CreateHabitFragment.CreateHabitListener, SettingsFragment.SettingsListener, EventsFragment.EventsListener, CreateEventsFragment.CreateEventListener, UpdatePasswordFragment.UpdatePassword {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(navListener);

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.rootView, new LoginFragment())
                    .commit();

            // Since we are signing in set bottom navigation to invisible
            bottomNav.setVisibility(View.INVISIBLE);
        }
        else {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.rootView, new HabitsFragment())
                    .commit();

            // Auth successful, set bottom navigation to visible and pre-select the Dashboard
            bottomNav.setVisibility(View.VISIBLE);
            bottomNav.setSelectedItemId(R.id.dashboard);
        }
    }

    private final BottomNavigationView.OnItemSelectedListener navListener = item -> {
        // By using switch we can easily get the selected fragment by using its id.
        Fragment selectedFragment = null;
        int itemId = item.getItemId();

        if (itemId == R.id.dashboard) {
            selectedFragment = new DashboardFragment();
        }
        else if (itemId == R.id.following) {
            selectedFragment = new DiscoveryFragment();
        }
        else if (itemId == R.id.habits) {
            selectedFragment = new HabitsFragment();
        }
        else if (itemId == R.id.events) {
            selectedFragment = new EventsFragment();
        }
        else if (itemId == R.id.settings) {
            selectedFragment = new SettingsFragment();
        }

        if (selectedFragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.rootView, selectedFragment)
                    .commit();
        }

        return true;
    };

    @Override
    public void createNewAccount() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.rootView, new SignUpFragment())
                .commit();
    }

    @Override
    public void login() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.rootView, new LoginFragment())
                .commit();

        // Since we are signing in set bottom navigation to invisible
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setVisibility(View.INVISIBLE);
    }

    @Override
    public void authSuccessful() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.rootView, new HabitsFragment())
                .commit();

        // Set bottom navigation to visible and pre-select the Dashboard
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setVisibility(View.VISIBLE);
        bottomNav.setSelectedItemId(R.id.dashboard);
    }

    @Override
    public void createNewHabit() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.rootView, new CreateHabitFragment())
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void logout() {
        FirebaseAuth.getInstance().signOut();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.rootView, new LoginFragment())
                .commit();

        // Since we are signing out set bottom navigation to invisible
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setVisibility(View.INVISIBLE);
    }

    @Override
    public void updateEmail() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.rootView, new UpdateEmailFragment())
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void updatePassword() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.rootView, new UpdatePasswordFragment())
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void cancelCreateHabit() {
        getSupportFragmentManager().popBackStack();
    }

    @Override
    public void doneCreateHabit() {
        getSupportFragmentManager().popBackStack();
    }

    @Override
    public void createNewEvent() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.rootView, new CreateEventsFragment())
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void cancelEventCreation() {getSupportFragmentManager().popBackStack();    }

    @Override
    public void submitEventCreation() {getSupportFragmentManager().popBackStack();}

    @Override
    public void viewEvent(Event event) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.rootView, new ViewEventFragment(event))
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void logoutUpdatePass() {
        FirebaseAuth.getInstance().signOut();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.rootView, new LoginFragment())
                .commit();

        // Since we are signing out set bottom navigation to invisible
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setVisibility(View.INVISIBLE);
    }
}