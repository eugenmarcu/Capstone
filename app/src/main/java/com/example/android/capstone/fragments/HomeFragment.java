package com.example.android.capstone.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.example.android.capstone.activities.DetailsActivity;
import com.example.android.capstone.Pet;
import com.example.android.capstone.PetAdapter;
import com.example.android.capstone.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static com.example.android.capstone.utils.Constants.*;

public class HomeFragment extends Fragment implements PetAdapter.ItemClickListener {

    private Context mContext;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mPetsDatabaseReference;
    private Query query;
    private ValueEventListener mEventListener;

    private ArrayList<Pet> petList;
    @BindView(R.id.list_rv)
    RecyclerView petRecyclerView;
    private PetAdapter petAdapter;
    @BindView(R.id.empty_view)
    ImageView emptyView;
    @BindView(R.id.progress_bar)
    ProgressBar progressBar;
    private StaggeredGridLayoutManager layoutManager;

    private Unbinder unbinder;

    public HomeFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        query = mPetsDatabaseReference.orderByChild(TIMESTAMP).limitToLast(MAX_PETS);

        int numberOfColumns = getColumnsNumByOrientation(mContext);
        if (savedInstanceState == null) petList = new ArrayList<>();
        else petList = savedInstanceState.getParcelableArrayList(PET_LIST);
        layoutManager = new StaggeredGridLayoutManager(numberOfColumns, 1);
        petRecyclerView.setLayoutManager(layoutManager);
        petAdapter = new PetAdapter(mContext, petList);
        petAdapter.setClickListener(this);
        petRecyclerView.setAdapter(petAdapter);

        if (savedInstanceState != null) {
            final StaggeredGridLayoutManager.SavedState mLayoutManagerState = savedInstanceState.getParcelable(LAYOUT_MANAGER_SATE);
            /*
             * It looks like the RecyclerView keeps going back to initial state
             * because the data in Adapter still being populated when we call the onRestoreInstanceState
             * so we need a little delay
             */
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    petRecyclerView.getLayoutManager().onRestoreInstanceState(mLayoutManagerState);
                }
            }, 300);
        } else {
            attachDatabaseReadListener();
            progressBar.setVisibility(View.VISIBLE);
        }

        return view;
    }

    @Override
    public void onItemClick(View view, int position) {
        Intent intent = new Intent(mContext, DetailsActivity.class);
        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                getActivity(), view, getString(R.string.transition_name));
        Pet currentPet = petAdapter.getItem(position);
        intent.putExtra(CURRENT_PET, currentPet);
        startActivity(intent, options.toBundle());
    }

    private void attachDatabaseReadListener() {
        if (mEventListener == null) {
            mEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (progressBar != null)
                        progressBar.setVisibility(View.GONE);
                    if (dataSnapshot.exists()) {
                        for (DataSnapshot data : dataSnapshot.getChildren()) {
                            Pet pet = data.getValue(Pet.class);
                            petAdapter.add(pet);
                            Collections.reverse(petList);
                        }

                        if (petAdapter.getItemCount() > 0 && emptyView != null)
                            emptyView.setVisibility(View.GONE);
                        else if (emptyView != null) emptyView.setVisibility(View.VISIBLE);
                    } else if (emptyView != null) emptyView.setVisibility(View.VISIBLE);

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            };
        }

        query.addValueEventListener(mEventListener);
    }

    private void detachDatabaseReadListener() {
        if (mEventListener != null) {
            query.removeEventListener(mEventListener);
            mEventListener = null;
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(LAYOUT_MANAGER_SATE, layoutManager.onSaveInstanceState());
        outState.putParcelableArrayList(PET_LIST, petList);
    }

    @Override
    public void onPause() {
        super.onPause();
        detachDatabaseReadListener();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
