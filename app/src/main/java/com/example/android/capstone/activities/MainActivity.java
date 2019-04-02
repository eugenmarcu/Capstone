package com.example.android.capstone.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.example.android.capstone.R;
import com.example.android.capstone.fragments.AddPetFragment;
import com.example.android.capstone.fragments.HomeFragment;
import com.example.android.capstone.fragments.MyPetsFragment;
import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;


import static com.example.android.capstone.utils.Constants.*;

public class MainActivity extends AppCompatActivity {

    public static final int RC_SIGN_IN = 1;

    //firebase
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    private String signedInUserName;
    private String signedInUserUid;
    private String locationName;
    private Double locationLongitude;
    private Double locationLatitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //get current Location;
        getCurrentLocation();

        //Initialize Firebase
        mFirebaseAuth = FirebaseAuth.getInstance();
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    signedInUserName = user.getDisplayName();
                    signedInUserUid = user.getUid();
                    user.getUid();
                } else {
                    // User is signed out
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setIsSmartLockEnabled(false)
                                    .setAvailableProviders(
                                            Arrays.asList(new AuthUI.IdpConfig.EmailBuilder().build(),
                                                    new AuthUI.IdpConfig.GoogleBuilder().build()))
                                    .setLogo(R.drawable.pets)
                                    .setTheme(R.style.AuthenticationTheme)
                                    .build(),
                            RC_SIGN_IN);
                }

            }
        };

        if (savedInstanceState != null) {
            signedInUserName = savedInstanceState.getString(USER_NAME);
            signedInUserUid = savedInstanceState.getString(USER_ID);
        }
        else {
            startHomeFragment();
        }

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_nav_view);
        bottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        item.setChecked(true);
                        FragmentManager fragmentManager = getSupportFragmentManager();

                        if (signedInUserUid != null) {
                            Bundle args = new Bundle();
                            args.putString(USER_NAME, signedInUserName);
                            args.putString(USER_ID, signedInUserUid);
                            args.putString(LOCATION_NAME, locationName);
                            args.putDouble(LOCATION_LONGITUDE, locationLongitude);
                            args.putDouble(LOCATION_LATITUDE, locationLatitude);
                            Fragment currentFragment;
                            switch (item.getItemId()) {
                                case R.id.action_home:
                                    currentFragment = new HomeFragment();
                                    currentFragment.setArguments(args);
                                    fragmentManager.beginTransaction()
                                            .replace(R.id.content_main_frame, new HomeFragment())
                                            .commit();
                                    break;
                                case R.id.action_add_pet:
                                    currentFragment = new AddPetFragment();
                                    currentFragment.setArguments(args);
                                    fragmentManager.beginTransaction()
                                            .replace(R.id.content_main_frame, currentFragment)
                                            .commit();
                                    break;
                                case R.id.action_my_pets:
                                    currentFragment = new MyPetsFragment();
                                    currentFragment.setArguments(args);
                                    fragmentManager.beginTransaction()
                                            .replace(R.id.content_main_frame, currentFragment)
                                            .commit();
                                    break;
                            }
                        }
                        return false;
                    }
                });
    }

    private void startHomeFragment(){
        Fragment fragment = new HomeFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.content_main_frame, fragment)
                .commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }


    @Override
    protected void onPause() {
        super.onPause();
        if (mAuthStateListener != null) {
            mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                // Sign-in succeeded, set up the UI
                Toast.makeText(this, getString(R.string.signed_in), Toast.LENGTH_SHORT).show();
            } else if (resultCode == RESULT_CANCELED) {
                // Sign in was canceled by the user, finish the activity
                Toast.makeText(this, getString(R.string.signed_in_canceled), Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sign_out_menu:
                AuthUI.getInstance().signOut(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private String getCurrentLocationName(Double lat, Double lon) {
        String cityName = "";
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses;
        try {
            addresses = geocoder.getFromLocation(lat, lon, 10);
            if (addresses.size() > 0) {
                for (Address add : addresses) {
                    if (add.getLocality() != null && add.getLocality().length() > 0) {
                        cityName = add.getLocality();
                        break;
                    }

                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return cityName;
    }

    private void getCurrentLocation() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1000);
        } else {
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            try {
                locationName = getCurrentLocationName(location.getLatitude(), location.getLongitude());
                locationLatitude = location.getLatitude();
                locationLongitude = location.getLongitude();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, R.string.location_not_found, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1000: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                    Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    try {
                        locationName = getCurrentLocationName(location.getLatitude(), location.getLongitude());
                        locationLatitude = location.getLatitude();
                        locationLongitude = location.getLongitude();
                        startHomeFragment();
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, getString(R.string.location_not_found), Toast.LENGTH_SHORT).show();
                    }

                } else {
                    final EditText edittext = new EditText(this);
                    new AlertDialog.Builder(this)
                    .setTitle(R.string.please_enter_location)
                    .setView(edittext)
                    .setPositiveButton(R.string.set, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            locationName = edittext.getText().toString();
                            //put random coordinates
                            locationLatitude = 44.0d;
                            locationLongitude = 25.0d;
                            startHomeFragment();
                        }}).show();
                }
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(USER_NAME, signedInUserName);
        outState.putString(USER_ID, signedInUserUid);
        super.onSaveInstanceState(outState);
    }

    //remove keyboard on touch
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int) event.getRawX(), (int) event.getRawY())) {
                    v.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent(event);
    }
}
