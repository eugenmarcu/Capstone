package com.example.android.capstone.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.android.capstone.Pet;
import com.example.android.capstone.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static com.example.android.capstone.utils.Constants.*;


public class EditActivity extends AppCompatActivity {

    private Pet currentPet;
    private boolean petChanged = false;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mPetsDatabaseReference;
    private FirebaseStorage mFirebaseStorage;
    private StorageReference mPetPhotosStorageReference;

    private Uri selectedImageUri;

    @BindView(R.id.pet_detail_name)
    TextView name;
    @BindView(R.id.pet_detail_image)
    ImageView image;
    @BindView(R.id.pet_detail_breed)
    TextView breed;
    @BindView(R.id.pet_detail_start_date)
    TextView startDate;
    @BindView(R.id.pet_detail_end_date)
    TextView endDate;
    @BindView(R.id.pet_detail_owner)
    TextView owner;
    @BindView(R.id.pet_detail_location)
    TextView locationTV;
    @BindView(R.id.pet_detail_phone)
    TextView phone;
    @BindView(R.id.pet_detail_email)
    TextView email;

    private Unbinder unbinder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        unbinder = ButterKnife.bind(this);

        Intent intent = getIntent();
        if (intent.hasExtra(CURRENT_PET)) {
            currentPet = intent.getParcelableExtra(CURRENT_PET);
        }

        Toolbar toolbar = findViewById(R.id.details_toolbar);
        toolbar.setTitle(R.string.edit);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Glide.with(this)
                .load(currentPet.getImageUrl())
                .into(image);

        name.setText(currentPet.getName());
        name.setOnTouchListener(mTouchListener);
        breed.setText(currentPet.getBreed());
        breed.setOnTouchListener(mTouchListener);
        startDate.setText(currentPet.getStartDate());
        startDate.setOnTouchListener(mTouchListener);
        endDate.setText(currentPet.getEndDate());
        endDate.setOnTouchListener(mTouchListener);
        phone.setText(currentPet.getPhone());
        phone.setOnTouchListener(mTouchListener);
        email.setText(currentPet.getEmail());
        email.setOnTouchListener(mTouchListener);
        locationTV.setText(currentPet.getLocation());
        locationTV.setOnTouchListener(mTouchListener);
        owner.setText(currentPet.getOwner());
        owner.setOnTouchListener(mTouchListener);

        //Initialize Firebase
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mPetsDatabaseReference = mFirebaseDatabase.getReference().child(PETS);
        mFirebaseStorage = FirebaseStorage.getInstance();
        mPetPhotosStorageReference = mFirebaseStorage.getReference().child(PET_PHOTOS);

        startDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePicker(EditActivity.this, startDate);
            }
        });
        endDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePicker(EditActivity.this, endDate);
            }
        });

        Button saveBtn = findViewById(R.id.pet_detail_save);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                savePetWithImage();
                sendActivityResult();
            }
        });

        Button deleteBtn = findViewById(R.id.pet_detail_delete);
        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(EditActivity.this)
                        .setTitle(R.string.delete_pet)
                        .setMessage(R.string.are_you_sure_delete)

                        // Specifying a listener allows you to take an action before dismissing the dialog.
                        // The dialog is automatically dismissed when a dialog button is clicked.
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // Continue with delete operation
                                mPetsDatabaseReference.child(currentPet.getId()).removeValue();
                                sendActivityResult();
                            }
                        })

                        // A null listener allows the button to dismiss the dialog and take no further action.
                        .setNegativeButton(R.string.no, null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        });
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_PHOTO_PICKER && resultCode == RESULT_OK) {
            selectedImageUri = data.getData();
            Glide.with(this)
                    .load(selectedImageUri)
                    .into(image);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // If the pet hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!petChanged) {
                    finish();
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                finish();
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void sendActivityResult() {
        Intent returnIntent = new Intent();
        returnIntent.putExtra("result", EDIT_REQUEST_CODE);
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }

    private Pet getEditedPet() {
        currentPet.setName(name.getText().toString());
        currentPet.setBreed(breed.getText().toString());
        currentPet.setStartDate(startDate.getText().toString());
        currentPet.setEndDate(endDate.getText().toString());
        currentPet.setPhone(phone.getText().toString());
        currentPet.setEmail(email.getText().toString());
        if (selectedImageUri != null)
            currentPet.setImageUrl(selectedImageUri.toString());
        return currentPet;
    }

    private void savePetWithImage() {
        if (selectedImageUri == null) {
            currentPet = getEditedPet();
            mPetsDatabaseReference.child(currentPet.getId()).setValue(currentPet);
            Toast.makeText(EditActivity.this, R.string.pet_saved, Toast.LENGTH_SHORT).show();
        } else {
            final StorageReference photoRef = mPetPhotosStorageReference.child(selectedImageUri.getLastPathSegment());
            photoRef.putFile(selectedImageUri)
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(EditActivity.this, R.string.failure_to_get_photo, Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //When the image has successfully uploaded, get its download URL
                            photoRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    currentPet = getEditedPet();
                                    currentPet.setImageUrl(uri.toString());
                                    mPetsDatabaseReference.child(currentPet.getId()).setValue(currentPet);
                                    Toast.makeText(EditActivity.this, R.string.pet_saved, Toast.LENGTH_SHORT).show();

                                }
                            });
                        }
                    });
        }
    }

    // OnTouchListener that listens for any user touches on a View, implying that they are modifying
    // the view, and we change the  boolean to true.
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            petChanged = true;
            return false;
        }
    };

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this);
        builder.setMessage(R.string.discard_changes);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the item.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        android.support.v7.app.AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }
}
