package com.example.android.capstone;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

public class PetAdapter extends RecyclerView.Adapter<PetAdapter.PetViewHolder> {
    private List<Pet> petList;
    private Context mContext;
    private ItemClickListener mClickListener;

    /**
     * ReviewAdapter constructor that will take the reviewList to display within context
     *
     * @param context the context within will be displayed the reviewsList
     * @param petList the list of pets that will be displayed
     */
    public PetAdapter(Context context, List<Pet> petList) {
        this.mContext = context;
        this.petList = petList;
    }


    @NonNull
    @Override
    public PetAdapter.PetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.pet_list_item, parent, false);
        return new PetViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull PetAdapter.PetViewHolder holder, int position) {
        Pet currentPet = petList.get(position);

        //get the holder that should be updated for each pet details
        Glide.with(this.mContext)
                .load(currentPet.getImageUrl())
                .into(holder.image);
        holder.name.setText(currentPet.getName());
        holder.location.setText(currentPet.getLocation());
        holder.date.setText(currentPet.getStartDate());

    }

    @Override
    public int getItemCount() {
        return petList.size();
    }

    // convenience method for getting data at click position
    public Pet getItem(int id) {
        return petList.get(id);
    }

    public void addAll(List<Pet> petList) {
        this.petList.clear();
        this.petList.addAll(petList);
        notifyDataSetChanged();
    }

    public void add(Pet pet){
        this.petList.add(pet);
        notifyDataSetChanged();
    }
    public void clearAll() {
        this.petList.clear();
        notifyDataSetChanged();
    }

    // allows clicks events to be caught
    public void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }

    public class PetViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public ImageView image;
        public TextView name;
        public TextView location;
        public TextView date;

        //ViewHolder's constructor
        public PetViewHolder(View itemView) {
            super(itemView);

            //Find Views that will display each item
            image = itemView.findViewById(R.id.pet_item_image);
            name = itemView.findViewById(R.id.pet_item_name);
            location = itemView.findViewById(R.id.pet_item_location);
            date = itemView.findViewById(R.id.pet_item_date);
            itemView.setOnClickListener(this);

        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }
}
