package com.example.android.capstone.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;

import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.android.capstone.Pet;
import com.example.android.capstone.utils.NetworkUtils;
import com.example.android.capstone.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static com.example.android.capstone.utils.Constants.*;

public class AddPetFragment extends Fragment {

    private static final String NAME_FIELD = "NAME_FIELD";
    private static final String IMAGE_FIELD = "IMAGE_FIELD";
    private static final String EMAIL_FIELD = "EMAIL_FIELD";
    private static final String PHONE_FIELD = "PHONE_FIELD";
    private static final String DROPOFF_FIELD = "DROPOFF_FIELD";
    private static final String PICKUP_FIELD = "PICKUP_FIELD";
    private static final String BREED_FIELD = "BREED_FIELD";
    private Context mContext;
    public static final int RC_SIGN_IN = 1;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mPetsDatabaseReference;
    private FirebaseStorage mFirebaseStorage;
    private StorageReference mPetPhotosStorageReference;
    private String userName;
    private String userNameUid;
    private String locationName;
    private Double locationLongitude;
    private Double locationLatitude;

    @BindView(R.id.pet_detail_name)
    EditText name;
    @BindView(R.id.pet_detail_breed)
    EditText breed;
    @BindView(R.id.pet_detail_start_date)
    TextView startDate;
    @BindView(R.id.pet_detail_end_date)
    TextView endDate;
    @BindView(R.id.pet_detail_phone)
    EditText phone;
    @BindView(R.id.pet_detail_email)
    EditText email;
    @BindView(R.id.pet_detail_location)
    TextView locationTV;
    @BindView(R.id.pet_detail_owner)
    TextView owner;
    @BindView(R.id.pet_detail_image)
    ImageButton image;
    @BindView(R.id.empty_view)
    ImageView emptyView;
    @BindView(R.id.linear_layout_add_pet)
    LinearLayout linearLayout;

    public Uri selectedImageUri;
    private boolean randomImage;
    View currentView;

    private Unbinder unbinder;

    public AddPetFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userName = getArguments().getString(USER_NAME);
        userNameUid = getArguments().getString(USER_ID);
        locationName = getArguments().getString(LOCATION_NAME);
        locationLongitude = getArguments().getDouble(LOCATION_LONGITUDE);
        locationLatitude = getArguments().getDouble(LOCATION_LATITUDE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_pet, container, false);
        mContext = container.getContext();
        unbinder = ButterKnife.bind(this, view);

        if (savedInstanceState != null)
            onRestoreInstanceState(savedInstanceState);

        checkInternetConnection(mContext, this, emptyView, linearLayout);

        //Initialize Firebase
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mPetsDatabaseReference = mFirebaseDatabase.getReference().child(PETS);
        mFirebaseStorage = FirebaseStorage.getInstance();
        mPetPhotosStorageReference = mFirebaseStorage.getReference().child(PET_PHOTOS);

        owner.setText(userName);
        locationTV.setText(locationName);

        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(mContext)
                        .setTitle(R.string.choose_image)
                        .setMessage(R.string.what_image)
                        .setPositiveButton(R.string.random, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // Continue with delete operation
                                randomImage = true;
                                AsyncTaskDogApi asyncTaskDogApi = new AsyncTaskDogApi();
                                asyncTaskDogApi.execute("https://dog.ceo/api/breeds/image/random");
                            }
                        })
                        .setNegativeButton(R.string.from_device, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                randomImage = false;
                                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                                intent.setType("image/jpeg");
                                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                                startActivityForResult(Intent.createChooser(intent, getString(R.string.complete_action_using)), RC_PHOTO_PICKER);
                            }
                        })
                        .setIcon(R.drawable.question_icon)
                        .show();
            }
        });
        startDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePicker(mContext, startDate);
            }
        });
        endDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePicker(mContext, endDate);
            }
        });

        Button saveButton = view.findViewById(R.id.pet_detail_save);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (allFieldsCompleted())
                    savePetWithImage();
            }
        });

        currentView = view;
        return view;
    }

    private boolean allFieldsCompleted() {
        boolean ok = true;
        if (TextUtils.isEmpty(name.getText())) {
            name.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.warning, 0);
            ok = false;
        } else
            name.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0);
        if (TextUtils.isEmpty(breed.getText())) {
            breed.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.warning, 0);
            ok = false;
        } else
            breed.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0);
        if (TextUtils.isEmpty(startDate.getText())) {
            startDate.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.date_icon, 0, R.drawable.warning, 0);
            ok = false;
        } else
            startDate.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.date_icon, 0, 0, 0);
        if (TextUtils.isEmpty(endDate.getText())) {
            endDate.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.date_icon, 0, R.drawable.warning, 0);
            ok = false;
        } else
            endDate.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.date_icon, 0, 0, 0);
        if (TextUtils.isEmpty(phone.getText())) {
            phone.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.warning, 0);
            ok = false;
        } else
            phone.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0);
        if (TextUtils.isEmpty(email.getText())) {
            email.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.warning, 0);
            ok = false;
        } else
            email.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0);

        if (!ok)
            Toast.makeText(mContext, R.string.please_enter_required, Toast.LENGTH_SHORT).show();
        return ok;
    }

    private void savePetWithImage() {
        if (randomImage)
            saveToDB(selectedImageUri);
        else {
            final StorageReference photoRef = mPetPhotosStorageReference.child(selectedImageUri.getLastPathSegment());
            photoRef.putFile(selectedImageUri)
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(mContext, R.string.failure_to_get_photo, Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnSuccessListener(getActivity(), new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //When the image has successfully uploaded, get its download URL
                            photoRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    saveToDB(uri);
                                }
                            });
                        }
                    });
        }
    }

    private void saveToDB(Uri uri) {
        String id = mPetsDatabaseReference.push().getKey();
        Pet newPet = new Pet(id, name.getText().toString(), breed.getText().toString(),
                locationTV.getText().toString(), locationLongitude, locationLatitude,
                startDate.getText().toString(), endDate.getText().toString(),
                phone.getText().toString(), email.getText().toString(),
                userName, userNameUid, uri.toString(), String.valueOf(Calendar.getInstance().getTimeInMillis()));
        mPetsDatabaseReference.child(id).setValue(newPet);
        Toast.makeText(mContext, R.string.pet_saved, Toast.LENGTH_SHORT).show();
        currentView.findViewById(R.id.linear_layout_add_pet).setVisibility(View.GONE);
        currentView.findViewById(R.id.save_layout).setVisibility(View.VISIBLE);
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                // Sign-in succeeded, set up the UI
                Toast.makeText(mContext, R.string.signed_in, Toast.LENGTH_SHORT).show();
            } else if (resultCode == RESULT_CANCELED) {
                // Sign in was canceled by the user, finish the activity
                Toast.makeText(mContext, R.string.signed_in_canceled, Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == RC_PHOTO_PICKER && resultCode == RESULT_OK) {
            selectedImageUri = data.getData();
            Glide.with(mContext)
                    .load(selectedImageUri)
                    .into(image);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString(NAME_FIELD, name.getText().toString());
        outState.putString(BREED_FIELD, breed.getText().toString());
        outState.putString(PICKUP_FIELD, startDate.getText().toString());
        outState.putString(DROPOFF_FIELD, endDate.getText().toString());
        outState.putString(PHONE_FIELD, phone.getText().toString());
        outState.putString(EMAIL_FIELD, email.getText().toString());
        if (selectedImageUri != null)
            outState.putString(IMAGE_FIELD, selectedImageUri.toString());

        super.onSaveInstanceState(outState);
    }

    public void onRestoreInstanceState(@NonNull Bundle bundle) {
        name.setText(bundle.getString(NAME_FIELD));
        breed.setText(bundle.getString(BREED_FIELD));
        startDate.setText(bundle.getString(PICKUP_FIELD));
        endDate.setText(bundle.getString(DROPOFF_FIELD));
        phone.setText(bundle.getString(PHONE_FIELD));
        email.setText(bundle.getString(EMAIL_FIELD));
        String imageUrl = bundle.getString(IMAGE_FIELD);
        if (!TextUtils.isEmpty(imageUrl)) {
            selectedImageUri = Uri.parse(imageUrl);
            Glide.with(mContext)
                    .load(selectedImageUri)
                    .into(image);
        }

        super.onSaveInstanceState(bundle);
    }

    public class AsyncTaskDogApi extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... strings) {
            String imageURL = "";
            try {
                imageURL = NetworkUtils.fetchImageUrl(strings[0]);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return imageURL;
        }

        @Override
        protected void onPostExecute(String imageUrl) {
            super.onPostExecute(imageUrl);
            selectedImageUri = Uri.parse(imageUrl);
            if (!TextUtils.isEmpty(imageUrl)) {
                Glide.with(mContext)
                        .load(selectedImageUri)
                        .into(image);
            }
        }
    }

}
