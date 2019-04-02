package com.example.android.capstone.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.example.android.capstone.Pet;
import com.example.android.capstone.PetAdapter;
import com.example.android.capstone.R;
import com.example.android.capstone.activities.EditActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static com.example.android.capstone.utils.Constants.*;

public class MyPetsFragment extends Fragment implements PetAdapter.ItemClickListener {

    private Context mContext;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mPetsDatabaseReference;
    private ValueEventListener valueEventListener;
    private Query query;

    private List<Pet> petList;
    @BindView(R.id.list_rv)
    RecyclerView petRecyclerView;
    private PetAdapter petAdapter;
    @BindView(R.id.empty_view)
    ImageView emptyView;
    @BindView(R.id.progress_bar)
    ProgressBar progressBar;

    private String userName;
    private String userUid;

    private Unbinder unbinder;

    public MyPetsFragment() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userName = getArguments().getString(USER_NAME);
        userUid = getArguments().getString(USER_ID);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        mContext = container.getContext();
        unbinder = ButterKnife.bind(this, view);

        checkInternetConnection(mContext, this, emptyView, petRecyclerView);

        //Initialize Firebase
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mPetsDatabaseReference = mFirebaseDatabase.getReference().child(PETS);
        //query the data by user name
        queryDataByUserName();

        int numberOfColumns = getColumnsNumByOrientation(mContext);
        petList = new ArrayList<>();

        petRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(numberOfColumns, 1));
        petAdapter = new PetAdapter(mContext, petList);
        petAdapter.setClickListener(this);
        petRecyclerView.setAdapter(petAdapter);

        progressBar.setVisibility(View.VISIBLE);

        return view;
    }

    private void queryDataByUserName() {
        if (valueEventListener != null) {
            query.removeEventListener(valueEventListener);
        }
        query = mPetsDatabaseReference.orderByChild(OWNER_UID).equalTo(userUid);
        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (progressBar != null)
                    progressBar.setVisibility(View.GONE);
                if (dataSnapshot.exists()) {
                    for (DataSnapshot data : dataSnapshot.getChildren()) {
                        Pet pet = data.getValue(Pet.class);
                        petAdapter.add(pet);
                    }

                    if (petAdapter.getItemCount() > 0 && emptyView != null)
                        emptyView.setVisibility(View.GONE);
                    else if (emptyView != null) emptyView.setVisibility(View.VISIBLE);
                } else if (emptyView != null) emptyView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        query.addListenerForSingleValueEvent(valueEventListener);

    }

    @Override
    public void onItemClick(View view, int position) {
        Intent intent = new Intent(mContext, EditActivity.class);
        Pet currentPet = petAdapter.getItem(position);
        intent.putExtra(CURRENT_PET, currentPet);
        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                getActivity(), view, getString(R.string.transition_name));
        intent.putExtra(CURRENT_PET, currentPet);
        startActivityForResult(intent, EDIT_REQUEST_CODE, options.toBundle());
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == EDIT_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            petAdapter.clearAll();
            queryDataByUserName();
        }
    }
}
