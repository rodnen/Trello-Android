package com.example.trello;
import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

public class Task implements Parcelable {
    private final String id;
    private String name;
    private String description;
    private final Date created;
    private Date deadline;
    private Vector<Tag> tags;

    public Task(String id, String name, String description, Date deadline, Vector<Tag> tags){
        this.id = id;
        this.name = name;
        this.description = description;
        this.created = new Date();
        this.deadline = deadline;
        this.tags = tags;
    }

    public Task(String id, String name, String description, Date created, Date deadline, Vector<Tag> tags){
        this.id = id;
        this.name = name;
        this.description = description;
        this.created = created;
        this.deadline = deadline;
        this.tags = tags;
    }

    protected Task(Parcel in) {
        id = in.readString();
        name = in.readString();
        description = in.readString();
        created = new Date(in.readLong());
        tags = new Vector<Tag>();
        in.readList(tags, Tag.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(description);
        dest.writeLong(created.getTime());
        dest.writeList(tags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Task> CREATOR = new Creator<Task>() {
        @Override
        public Task createFromParcel(Parcel in) {
            return new Task(in);
        }

        @Override
        public Task[] newArray(int size) {
            return new Task[size];
        }
    };

    public String getId(){
        return id;
    }

    public String getName(){
        return name;
    }

    public String getDescription(){
        return description;
    }

    public Date getCreationDate(){
        return created;
    }

    public Date getDeadline(){ return deadline;}

    public String getFormatDate(){
        return FormateDate.formatDate(created);
    }

    public String getFormatDeadline(){
        return FormateDate.formatDate(deadline);
    }

    public Vector<Tag> getTags(){
        return tags;
    }

    public void setName(String name){
        this.name = name;
    }

    public void setDescription(String description){
        this.description = description;
    }

    public void setDeadline(Date date){
        this.deadline = date;
    }

    public void setTags(Vector<Tag> tags){
        this.tags = tags;
    }

    public void appendTag(Tag tag){
        tags.add(tag);
    }

    public void removeTag(String color){
        for(int i = 0; i < tags.size(); i++){
            if(tags.elementAt(i).getColor().toLowerCase().compareTo(color.toLowerCase()) == 0){
                tags.remove(i);
            }
        }
    }

    public JSONObject toJSON() {
        JSONArray tagsArray = new JSONArray();
        for(Tag tag : tags){
            tagsArray.put(tag.toJSON());
        }

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("id", id);
            jsonObject.put("name", name);
            jsonObject.put("description", description);
            jsonObject.put("created", created);
            jsonObject.put("deadline", deadline);
            jsonObject.put("tags", tagsArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    public boolean containsTag(String color){
        for (Tag tag : tags){
            if(tag.getColor().toLowerCase().equals(color.toLowerCase())){
                return true;
            }
        }
        return false;
    }

    public boolean containsTag(Tag t){
        for (Tag tag : tags){
            if(tag.equals(t)){
                return true;
            }
        }
        return false;
    }
}
