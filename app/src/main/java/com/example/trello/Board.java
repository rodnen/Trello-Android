package com.example.trello;
import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

public class Board implements Parcelable {
    private final String id;
    private String name;
    private final Date created;
    private Vector<List> lists;
    private Vector<Tag> tags;

    public Board(String id, String name){
        this.id = id;
        this.name = name;
        this.created = new Date();
        lists = new Vector<List>();
        tags = new Vector<Tag>();

        lists.add(new List(UniqueCodeGenerator.generateUniqueCode(), "Треба зробити", false));
        lists.add(new List(UniqueCodeGenerator.generateUniqueCode(), "Робиться", false));
        lists.add(new List(UniqueCodeGenerator.generateUniqueCode(), "Готово", false));

        createTags();
    }

    public Board(String id, String name, Date created, Vector<List> lists, Vector<Tag> tags){
        this.id = id;
        this.name = name;
        this.created = created;
        this.lists = lists;
        this.tags = tags;
    }

    protected Board(Parcel in) {
        id = in.readString();
        name = in.readString();
        created = new Date(in.readLong());
        lists = new Vector<List>();
        in.readList(lists, List.class.getClassLoader());
        tags = new Vector<Tag>();
        in.readList(tags, Tag.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeLong(created.getTime());
        dest.writeList(lists);
        dest.writeList(tags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Board> CREATOR = new Creator<Board>() {
        @Override
        public Board createFromParcel(Parcel in) {
            return new Board(in);
        }

        @Override
        public Board[] newArray(int size) {
            return new Board[size];
        }
    };

    public void createTags(){
        tags.add(new Tag("#EE334E"));
        tags.add(new Tag("#F4B54C"));
        tags.add(new Tag("#279BDB"));
        tags.add(new Tag("#00A651"));
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Date getCreated() {
        return created;
    }

    public String getFormatDate() {
       return FormateDate.formatDate(created);
    }


    public Vector<List> getLists() {
        return lists;
    }

    public Vector<Tag> getTags() {
        return tags;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void appendList(List list) {
        this.lists.add(list);
    }

    public void appendTag(Tag tag){
        this.tags.add(tag);
    }

    public void removeList(String id){
        for(int i = 0; i < lists.size(); i++){
            if(lists.elementAt(i).getId().compareTo(id) == 0){
                lists.remove(i);
            }
        }
    }

    public void removeTag(String color){
        for(int i = 0; i < tags.size(); i++){
            if(tags.elementAt(i).getColor().toLowerCase().compareTo(color.toLowerCase()) == 0){
                tags.remove(i);
                for (List list : lists){
                    for (Task task : list.getTasks()){
                        for(int j = 0; j < task.getTags().size(); j++){
                            if(task.getTags().get(j).getColor().toLowerCase().equals(color.toLowerCase())){
                                task.getTags().remove(j);
                            }
                        }
                    }
                }
            }
        }
    }

    public int getTaskCount(){
        int counter = 0;
        for (int i = 0; i < lists.size(); i++){
            List list = lists.get(i);
            for(int j = 0; j < list.getTasks().size(); j++){
                counter++;
            }
        }
        return counter;
    }

    public JSONObject toJSON() {
        JSONArray listsArray = new JSONArray();
        JSONArray tagsArray = new JSONArray();
        for(List list : lists){
            listsArray.put(list.toJSON());
        }
        for(Tag tag : tags){
            tagsArray.put(tag.toJSON());
        }

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("id", id);
            jsonObject.put("name", name);
            jsonObject.put("created", created);
            jsonObject.put("lists", listsArray);
            jsonObject.put("tags", tagsArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    public List getListById(String id){
        for(List list : lists){
            if(list.getId().equals(id)){
                return list;
            }
        }
        return null;
    }

    public int getListIndexById(String id){
        for(int i = 0; i < lists.size(); i++){
            if(lists.get(i).getId().equals(id)){
                return i;
            }
        }
        return 0;
    }

    public Task getTaskById(String id){
        for(List list : lists){
            for(Task task : list.getTasks()){
                if(task.getId().equals(id)){
                    return task;
                }
            }
        }
        return null;
    }

    public List getListByTaskId(String id){
        for(List list : lists){
            for (Task task : list.getTasks()){
                if(task.getId().equals(id)){
                    return list;
                }
            }
        }

        return null;
    }
}
