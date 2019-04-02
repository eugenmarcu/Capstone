package com.example.android.capstone.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.android.capstone.Pet;
import com.example.android.capstone.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static com.example.android.capstone.utils.Constants.*;

public class DetailsActivity extends AppCompatActivity {

    private Pet currentPet;
    @BindView(R.id.pet_detail_image)
    ImageView petImage;
    @BindView(R.id.pet_detail_name_layout)
    View petNameLayout;
    @BindView(R.id.pet_detail_breed)
    TextView petBreed;
    @BindView(R.id.pet_detail_start_date)
    TextView petPickUp;
    @BindView(R.id.pet_detail_end_date)
    TextView petDropOff;
    @BindView(R.id.pet_detail_owner)
    TextView petOwner;
    @BindView(R.id.pet_detail_location)
    TextView petLocation;
    @BindView(R.id.pet_detail_message)
    EditText message;

    private Unbinder unbinder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        unbinder = ButterKnife.bind(this);

        Intent intent = getIntent();
        if (intent.hasExtra(CURRENT_PET)) {
            currentPet = intent.getParcelableExtra(CURRENT_PET);
        }

        Toolbar toolbar = findViewById(R.id.details_toolbar);
        toolbar.setTitle(currentPet.getName());
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Glide.with(this)
                .load(currentPet.getImageUrl())
                .into(petImage);
        petNameLayout.setVisibility(View.GONE);
        petBreed.setText(currentPet.getBreed());
        petBreed.setEnabled(false);
        petPickUp.setText(currentPet.getStartDate());
        petPickUp.setEnabled(false);
        petDropOff.setText(currentPet.getEndDate());
        petDropOff.setEnabled(false);
        petOwner.setText(currentPet.getOwner());
        petLocation.setText(currentPet.getLocation());

        ImageButton call = findViewById(R.id.pet_detail_call_btn);
        call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(DetailsActivity.this,
                        Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.CALL_PHONE},
                            REQUEST_CODE_ASK_PERMISSIONS);
                } else {
                    Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + currentPet.getPhone()));
                    startActivity(intent);
                }
            }
        });

        Button emailBtn = findViewById(R.id.pet_detail_send_message_btn);
        emailBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Activity activity = DetailsActivity.this;

                ShareCompat.IntentBuilder.from(activity)
                        .setType("message/rfc822")
                        .addEmailTo(currentPet.getEmail())
                        .setSubject(getString(R.string.pet_my_pet))
                        .setText(message.getText())
                        .setChooserTitle(getString(R.string.send_email))
                        .startChooser();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, R.string.call_permission_granted, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, R.string.call_permission_not_granted, Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}
