package com.example.android.capstone;

import android.os.Parcel;
import android.os.Parcelable;

public class Pet implements Parcelable {

    private String id;
    private String name;
    private String breed;
    private String location;
    private Double locationLongitude;
    private Double locationLatitude;
    private String startDate;
    private String endDate;
    private String phone;
    private String email;
    private String owner;
    private String imageUrl;
    private String ownerUid;
    private String timestamp;


    public Pet() {
    }


    public Pet(String id, String name, String breed, String location, Double locationLongitude, Double locationLatitude, String startDate, String endDate, String phone, String email, String owner, String ownerUid, String imageUrl, String timestamp) {
        this.id = id;
        this.name = name;
        this.breed = breed;
        this.location = location;
        this.locationLongitude = locationLongitude;
        this.locationLatitude = locationLatitude;
        this.startDate = startDate;
        this.endDate = endDate;
        this.phone = phone;
        this.email = email;
        this.owner = owner;
        this.ownerUid = ownerUid;
        this.imageUrl = imageUrl;
        this.timestamp = timestamp;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setBreed(String breed) {
        this.breed = breed;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setLocationLongitude(Double locationLongitude) {
        this.locationLongitude = locationLongitude;
    }

    public void setLocationLatitude(Double locationLatitude) {
        this.locationLatitude = locationLatitude;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setOwnerUid(String ownerUid) {
        this.ownerUid = ownerUid;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getBreed() {
        return breed;
    }

    public String getLocation() {
        return location;
    }

    public Double getLocationLongitude() {
        return locationLongitude;
    }

    public Double getLocationLatitude() {
        return locationLatitude;
    }

    public String getOwnerUid() {
        return ownerUid;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getOwner() {
        return owner;
    }

    public static final Creator CREATOR = new Creator() {
        public Pet createFromParcel(Parcel in) {
            return new Pet(in);
        }

        public Pet[] newArray(int size) {
            return new Pet[size];
        }
    };

    public Pet(Parcel in) {
        this.id = in.readString();
        this.name = in.readString();
        this.breed = in.readString();
        this.location = in.readString();
        this.locationLongitude = in.readDouble();
        this.locationLatitude = in.readDouble();
        this.startDate = in.readString();
        this.endDate = in.readString();
        this.phone = in.readString();
        this.email = in.readString();
        this.owner = in.readString();
        this.ownerUid = in.readString();
        this.imageUrl = in.readString();
        this.timestamp = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.id);
        parcel.writeString(this.name);
        parcel.writeString(this.breed);
        parcel.writeString(this.location);
        parcel.writeDouble(this.locationLongitude);
        parcel.writeDouble(this.locationLatitude);
        parcel.writeString(this.startDate);
        parcel.writeString(this.endDate);
        parcel.writeString(this.phone);
        parcel.writeString(this.email);
        parcel.writeString(this.owner);
        parcel.writeString(this.ownerUid);
        parcel.writeString(this.imageUrl);
        parcel.writeString(this.timestamp);
    }
}
