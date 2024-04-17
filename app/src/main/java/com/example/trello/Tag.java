package com.example.trello;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

public class Tag implements Parcelable {
    private String color;

    protected Tag(Parcel in) {
        color = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(color);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Tag> CREATOR = new Creator<Tag>() {
        @Override
        public Tag createFromParcel(Parcel in) {
            return new Tag(in);
        }

        @Override
        public Tag[] newArray(int size) {
            return new Tag[size];
        }
    };

    public Tag(String color){
        this.color = color;
    }

    public Tag(Tag tag){
        this.color = tag.color;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public JSONObject toJSON() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("tag", color);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }
}
