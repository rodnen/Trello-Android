package com.example.trello;
import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

public class List implements Parcelable {
    private final String id;
    private String name;
    private final Date created;
    private boolean removable;
    private Vector<Task> tasks;

    public List(String id, String name, boolean removable) {
        this.id = id;
        this.name = name;
        this.created = new Date();
        this.removable = removable;
        this.tasks = new Vector<Task>();
    }

    public List(String id, String name, Date created, boolean removable, Vector<Task> tasks) {
        this.id = id;
        this.name = name;
        this.created = created;
        this.removable = removable;
        this.tasks = tasks;
    }


    protected List(Parcel in) {
        id = in.readString();
        name = in.readString();
        created = new Date(in.readLong());
        removable = in.readInt() == 1;
        tasks = new Vector<Task>();
        in.readList(tasks, Task.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeLong(created.getTime());
        dest.writeInt(removable ? 1 : 0);
        dest.writeList(tasks);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<List> CREATOR = new Creator<List>() {
        @Override
        public List createFromParcel(Parcel in) {
            return new List(in);
        }

        @Override
        public List[] newArray(int size) {
            return new List[size];
        }
    };

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Date getCreationDate() {
        return created;
    }

    public String getFormatDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
        String formattedDate = dateFormat.format(created);
        return formattedDate;
    }

    public boolean isRemovable() {
        return removable;
    }

    public Vector<Task> getTasks() {
        return tasks;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void appendTask(Task task) {
        this.tasks.add(task);
    }

    public void removeTask(String id) {
        for (int i = 0; i < tasks.size(); i++) {
            if (tasks.elementAt(i).getId().compareTo(id) == 0) {
                tasks.remove(i);
            }
        }
    }

    public JSONObject toJSON() {
        JSONArray tasksArray = new JSONArray();
        for (Task task : tasks) {
            tasksArray.put(task.toJSON());
        }

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("id", id);
            jsonObject.put("name", name);
            jsonObject.put("created", created);
            jsonObject.put("removable", removable);
            jsonObject.put("tasks", tasksArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    public Task getTaskById(String id) {
        for (int i = 0; i < tasks.size(); i++) {
            if (tasks.get(i).getId().equals(id)) {
                return tasks.get(i);
            }
        }

        return  null;
    }
}

