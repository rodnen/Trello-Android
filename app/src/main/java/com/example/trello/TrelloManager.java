package com.example.trello;

import android.content.Context;
import android.content.SharedPreferences;
import android.icu.text.SimpleDateFormat;
import android.util.Log;

import androidx.fragment.app.FragmentActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.Date;
import java.util.Locale;
import java.util.Vector;

public class TrelloManager {
    public static void saveData(FragmentActivity activity, Vector<Board> boards) {
        JSONArray boardsArray = new JSONArray();
        for (Board board : boards) {
            boardsArray.put(board.toJSON());
        }
        String jsonString = boardsArray.toString();

        SharedPreferences sharedPreferences = activity.getSharedPreferences("trello_preferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("json_data", jsonString);
        editor.apply();
    }

    public static void saveData(FragmentActivity activity, Board board) {
        SharedPreferences sharedPreferences = activity.getSharedPreferences("trello_preferences", Context.MODE_PRIVATE);
        String jsonData = sharedPreferences.getString("json_data", "");

        try {
            JSONArray jsonArray = new JSONArray(jsonData);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String boardId = jsonObject.getString("id");

                if (boardId.equals(board.getId())) {
                    jsonArray.put(i, board.toJSON());

                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("json_data", jsonArray.toString());
                    editor.apply();
                    return;
                }
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static boolean loadData(FragmentActivity activity, Vector<Board> boards) {
        SharedPreferences sharedPreferences = activity.getSharedPreferences("trello_preferences", Context.MODE_PRIVATE);
        String jsonData = sharedPreferences.getString("json_data", "");

        try {
            JSONArray jsonArray = new JSONArray(jsonData);
            //Log.d("JSON", jsonArray.get(0).toString());
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String boardId = jsonObject.getString("id");
                String boardName = jsonObject.getString("name");
                String boardCreatedString = jsonObject.getString("created");
                Date boardCreatedDate = new SimpleDateFormat("EEE MMM dd HH:mm:ss 'GMT'Z yyyy", Locale.ENGLISH).parse(boardCreatedString);

                JSONArray boardTagsJsonArray = jsonObject.getJSONArray("tags");
                Vector<Tag> boardTags = new Vector<Tag>();

                for (int j = 0; j < boardTagsJsonArray.length(); j++) {
                    JSONObject tagJson = boardTagsJsonArray.getJSONObject(j);
                    String color = tagJson.getString("tag");
                    boardTags.add(new Tag(color));
                }

                JSONArray listsJsonArray = jsonObject.getJSONArray("lists");
                Vector<List> lists = new Vector<List>();
                for (int j = 0; j < listsJsonArray.length(); j++) {
                    JSONObject listJson = listsJsonArray.getJSONObject(j);
                    String listId = listJson.getString("id");
                    String listName = listJson.getString("name");
                    String listCreatedString = listJson.getString("created");
                    Date listCreatedDate = new SimpleDateFormat("EEE MMM dd HH:mm:ss 'GMT'Z yyyy", Locale.ENGLISH).parse(listCreatedString);
                    boolean removable = listJson.getBoolean("removable");

                    JSONArray tasksJsonArray = listJson.getJSONArray("tasks");
                    Vector<Task> tasks = new Vector<Task>();

                    for (int k = 0; k < tasksJsonArray.length(); k++) {
                        JSONObject taskJson = tasksJsonArray.getJSONObject(k);
                        String taskId = taskJson.getString("id");
                        String taskName = taskJson.getString("name");
                        String taskDescription = taskJson.getString("description");
                        String taskCreatedString = taskJson.getString("created");
                        String taskDeadlineString = taskJson.getString("deadline");
                        Date taskCreatedDate = new SimpleDateFormat("EEE MMM dd HH:mm:ss 'GMT'Z yyyy", Locale.ENGLISH).parse(taskCreatedString);
                        Date taskDeadlineDate = new SimpleDateFormat("EEE MMM dd HH:mm:ss 'GMT'Z yyyy", Locale.ENGLISH).parse(taskDeadlineString);

                        JSONArray tagsJsonArray = taskJson.getJSONArray("tags");
                        Vector<Tag> tags = new Vector<Tag>();

                        for (int l = 0; l < tagsJsonArray.length(); l++) {
                            JSONObject tagJson = tagsJsonArray.getJSONObject(l);
                            String color = tagJson.getString("tag");
                            tags.add(new Tag(color));
                        }

                        tasks.add(new Task(taskId, taskName, taskDescription, taskCreatedDate, taskDeadlineDate, tags));
                    }

                    lists.add(new List(listId, listName, listCreatedDate, removable, tasks));
                }

                boards.add(new Board(boardId, boardName, boardCreatedDate, lists, boardTags));
            }

            return true;

        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }

        catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
    }

}