package com.example.android.capstone;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.support.annotation.NonNull;
import android.widget.RemoteViews;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.AppWidgetTarget;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import static com.example.android.capstone.utils.Constants.*;

/**
 * Implementation of App Widget functionality.
 */
public class PetsWidget extends AppWidgetProvider {

    private static FirebaseDatabase mFirebaseDatabase;
    private static DatabaseReference mPetsDatabaseReference;
    private static Query query;
    private static ValueEventListener mEventListener;

    static void updateAppWidget(final Context context, final AppWidgetManager appWidgetManager,
                                final int appWidgetId) {

        //Initialize Firebase
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mPetsDatabaseReference = mFirebaseDatabase.getReference().child(PETS);
        query = mPetsDatabaseReference.orderByChild(TIMESTAMP).limitToLast(1);
        if (mEventListener == null) {
            mEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists())
                        for (DataSnapshot data : dataSnapshot.getChildren()) {
                            Pet pet = data.getValue(Pet.class);

                            CharSequence widgetText = pet.getName();
                            // Construct the RemoteViews object
                            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.pets_widget);
                            views.setTextViewText(R.id.pet_widget_name, widgetText);

                        AppWidgetTarget appWidgetTarget = new AppWidgetTarget(context, R.id.pet_widget_image, views, appWidgetId);

                        Glide
                                .with(context.getApplicationContext())
                                .asBitmap()
                                .load(pet.getImageUrl())
                                .into(appWidgetTarget);

                            // Instruct the widget manager to update the widget
                            appWidgetManager.updateAppWidget(appWidgetId, views);
                        }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            };
        }
        query.addValueEventListener(mEventListener);
    }


    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
        if (mEventListener != null) {
            query.removeEventListener(mEventListener);
            mEventListener = null;
        }
    }
}

