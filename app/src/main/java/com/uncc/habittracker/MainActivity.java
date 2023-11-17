package com.uncc.habittracker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.accounts.Account;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.uncc.habittracker.data.model.Event;

public class MainActivity extends AppCompatActivity implements LoginFragment.LoginListener, SignUpFragment.SignUpListener,



        HabitsFragment.HabitsListener, CreateHabitFragment.CreateHabitListener, SettingsFragment.SettingsListener, EventsFragment.EventsListener, CreateEventsFragment.CreateEventListener, AccountFragment.AccountListener, EditAccount.EditListener, UpdatePasswordFragment.UpdatePassword, ApproveVerification.ApproveVerificationListener , EditEventFragment.EditFragmentListener {

        private Menu menuList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MaterialToolbar toolbar = findViewById(R.id.topAppBar); // get the reference of Toolbar
        setSupportActionBar(toolbar); // Setting/replace toolbar as the ActionBar

        this.menuList = toolbar.getMenu();

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(navListener);

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.rootView, new LoginFragment())
                    .commit();

            // Since we are signing in set bottom navigation to invisible and hide menu
            bottomNav.setVisibility(View.INVISIBLE);
            hideMenu();
        }
        else {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.rootView, new HabitsFragment())
                    .commit();

            // Auth successful, set bottom navigation to visible, pre-select the Dashboard, and show
            // menu
            bottomNav.setVisibility(View.VISIBLE);
            bottomNav.setSelectedItemId(R.id.dashboard);
            showMenu();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.top_app_bar, menu);
        return true;
    }

    public boolean onPrepareOptionsMenu(Menu menu) {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            hideMenu();
        }
        else {
            showMenu();
        }

        return true;
    }

    private void hideMenu() {
        menuList.findItem(R.id.profile).setVisible(false);
        menuList.findItem(R.id.people).setVisible(false);
        menuList.findItem(R.id.sign_out).setVisible(false);
    }

    private void showMenu()  {
        menuList.findItem(R.id.profile).setVisible(true);
        //menuList.findItem(R.id.people).setVisible(true);
        menuList.findItem(R.id.sign_out).setVisible(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // By using switch we can easily get the selected fragment by using its id.
        Fragment selectedFragment = null;
        int itemId = item.getItemId();

        if (itemId == R.id.profile) {
            // TODO: Update this to point to the Profile/Account fragment
            selectedFragment = new AccountFragment();
        }
        else if (itemId == R.id.people) {
            // TODO: Update this to point to the People/Discovery fragment
            //selectedFragment = new FollowingFragment();
        }
        else if (itemId == R.id.sign_out) {
            logout();
        }

        if (selectedFragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.rootView, selectedFragment)
                    .commit();
        }

        return super.onOptionsItemSelected(item);
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

        // Since we are signing in set bottom navigation to invisible and hide menu
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setVisibility(View.INVISIBLE);
        hideMenu();
    }

    @Override
    public void authSuccessful() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.rootView, new HabitsFragment())
                .commit();

        // Set bottom navigation to visible, pre-select the Dashboard, and show menu
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setVisibility(View.VISIBLE);
        bottomNav.setSelectedItemId(R.id.dashboard);
        showMenu();
    }

    @Override
    public void createNewHabit() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.rootView, new CreateHabitFragment())
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void account() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.rootView, new AccountFragment())
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void editAccount() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.rootView, new EditAccount())
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void logout() {
        FirebaseAuth.getInstance().signOut();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.rootView, new LoginFragment())
                .commit();

        // Since we are signing out set bottom navigation to invisible and hide menu
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setVisibility(View.INVISIBLE);
        hideMenu();
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
    public void errorState() {
        login();
    }

    @Override
    public void openAdminApproval() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.rootView, new ApproveVerification())
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

    public void editEvent(Event event) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.rootView, new EditEventFragment(event))
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void goBackToHome() {
        getSupportFragmentManager().popBackStack();
    }

    @Override
    public void cancelEdit() {
        getSupportFragmentManager().popBackStack();
    }

    @Override
    public void afterEditOpen(Event event) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.rootView, new ViewEventFragment(event))
                .addToBackStack(null)
                .commit();
    }


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