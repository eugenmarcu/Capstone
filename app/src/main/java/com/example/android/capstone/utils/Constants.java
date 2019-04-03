package com.example.android.capstone.utils;

import android.app.DatePickerDialog;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.content.res.Configuration;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.capstone.R;

import java.util.Calendar;

public final class Constants {
    public final static String USER_NAME = "USER_NAME";
    public final static String USER_ID = "USER_ID";
    public final static String LOCATION_NAME = "LOCATION_NAME";
    public final static String LOCATION_LONGITUDE = "LOCATION_LONGITUDE";
    public final static String LOCATION_LATITUDE = "LOCATION_LATITUDE";
    public final static String CURRENT_PET = "CURRENT_PET";
    public final static String PET_LIST = "PET_LIST";
    public final static String LAYOUT_MANAGER_SATE = "LAYOUT_MANAGER_STATE";
    public final static int EDIT_REQUEST_CODE = 300;
    public static final int RC_PHOTO_PICKER = 2;
    public static final int MAX_PETS = 20;
    public static final int REQUEST_CODE_ASK_PERMISSIONS = 400;
    public static final int WIFI_DATA = 2001;
    public static final int MOBILE_DATA = 2002;
    public static final String PETS = "pets";
    public static final String PET_PHOTOS = "pet_photos";
    public static final String TIMESTAMP = "timestamp";
    public static final String OWNER_UID = "ownerUid";

    public static final void showDatePicker(Context context, final TextView textView) {
        // Get Current Date
        final Calendar c = Calendar.getInstance();
        int mYear = c.get(Calendar.YEAR);
        int mMonth = c.get(Calendar.MONTH);
        int mDay = c.get(Calendar.DAY_OF_MONTH);


        DatePickerDialog datePickerDialog = new DatePickerDialog(context,
                new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year,
                                          int monthOfYear, int dayOfMonth) {

                        textView.setText(dayOfMonth + "/" + (monthOfYear + 1) + "/" + year);

                    }
                }, mYear, mMonth, mDay);
        datePickerDialog.show();
    }

    public static final void checkInternetConnection(final Context context, LifecycleOwner owner, final ImageView emptyView, final View view) {
        ConnectionLiveData connectionLiveData = new ConnectionLiveData(context);
        connectionLiveData.observe(owner, new Observer<ConnectionLiveData.ConnectionModel>() {
            @Override
            public void onChanged(@Nullable ConnectionLiveData.ConnectionModel connection) {
                if (connection.getIsConnected()) {
                    emptyView.setImageResource(R.drawable.empty);
                    emptyView.setVisibility(View.GONE);
                    view.setVisibility(View.VISIBLE);
                    switch (connection.getType()) {
                        case WIFI_DATA:
                            //Toast.makeText(context, "Wifi turned ON",         Toast.LENGTH_SHORT).show();
                            break;
                        case MOBILE_DATA:
                            //Toast.makeText(context, String.format("Mobile data turned ON"), Toast.LENGTH_SHORT).show();
                            break;
                    }
                } else {
                    //Toast.makeText(context, String.format("Connection turned OFF"), Toast.LENGTH_SHORT).show();
                    emptyView.setImageResource(R.drawable.no_internet);
                    emptyView.setVisibility(View.VISIBLE);
                    view.setVisibility(View.GONE);
                }
            }
        });
    }

    public static final int getColumnsNumByOrientation(Context context) {
        if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
            return 2;
        return 3;
    }
}
