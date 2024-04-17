package com.example.trello;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.trello.databinding.FragmentFirstBinding;
import com.example.trello.databinding.FragmentSecondBinding;
import com.google.android.material.snackbar.Snackbar;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Set;
import java.util.Vector;

public class SecondFragment extends Fragment implements OnRectangleClickListener{

    private FragmentSecondBinding binding;
    private Board board;
    private ListView listView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSecondBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        getActivity().setTitle("Другий фрагмент");
        Bundle args = getArguments();
        if (args != null) {
            board = args.getParcelable("data");
            getActivity().setTitle(board.getName());
        }

        listView = new ListView(getContext(), board.getLists());
        listView.setOnRectangleClickListener(this);

        RelativeLayout relativeLayout = binding.relativeLayout;
        relativeLayout.addView(listView);

        binding.fabFragmentSecond.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showEditDialog("", true, false);
            }
        });

        return view;
    }

    @Override
    public void onRectangleClick(String data, boolean isLongPress) {
        Log.d("SecondFragment", "Click1");
    }

    @Override
    public void onRectangleClick(String data, int type, boolean isLongPress) {
        try {
            if (type == 1 && isLongPress) {
                showDialog(data, true);
            } else if (type == 2) {
                showEditDialog(data, false, false);
            } else if(type == 3) {
                if (isLongPress) {
                    showDialog(data, false);
                } else {
                    showEditDialog(data, false, true);
                }
            }
        }
        catch (Exception e){
            showToast("Exception");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public void showDialog(String id, boolean isList){
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        String dialogTitle = "Меню " + (isList ? ("списку " + "\"" + board.getListById(id).getName() + "\"") : ("задачі " +  "\"" + board.getTaskById(id).getName()) + "\"");
        dialogBuilder.setTitle(dialogTitle);

        final View customLayout = getLayoutInflater().inflate(R.layout.dialog_menu_layout, null);
        dialogBuilder.setView(customLayout);

        Button editBtn = customLayout.findViewById(R.id.edit_button);
        Button deleteBtn = customLayout.findViewById(R.id.delete_button);
        Button closeBtn = customLayout.findViewById(R.id.close_button);

        final AlertDialog dialog = dialogBuilder.show();

        editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                showEditDialog(id, isList, true);
            }
        });

        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int listIndex = 0;
                int taskIndex = 0;
                try{
                    if(isList){
                        listIndex = board.getListIndexById(id);
                        List list = board.getListById(id);
                        if(list == null) throw new Exception("Не вдалося видалити список");
                        if(!list.isRemovable()) throw new Exception("Початкові списки не можна видаляти");
                        if(list.getTasks().size() != 0) throw new Exception("Не можна видалити список, якщо він містить задачу");
                    }
                    else{
                        int[] indexes = findTaskIndexById(id);
                        listIndex = indexes[0];
                        taskIndex = indexes[1];
                        Task task = getTaskByIndex(listIndex, taskIndex);
                        if(task == null) throw new Exception("Не вдалося видалити задачу");
                    }

                    dialog.dismiss();
                    showYesNoDialog(isList, listIndex, taskIndex);
                }
                catch (Exception e){
                    showToast(e.getMessage());
                }
            }
        });

        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
    }

    public void showYesNoDialog(boolean isList, int listIndex, int taskIndex){
        AlertDialog.Builder deleteBuilder = new AlertDialog.Builder(getActivity());
        deleteBuilder.setTitle("Ви впевнені?");

        final View dialogLayout = getLayoutInflater().inflate(R.layout.dialog_yes_no_layout, null);
        deleteBuilder.setView(dialogLayout);

        Button yes = dialogLayout.findViewById(R.id.yes_button);
        Button no = dialogLayout.findViewById(R.id.no_button);

        final AlertDialog deleteDialog = deleteBuilder.show();

        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteDialog.dismiss();
                if(isList){
                    board.getLists().remove(listIndex);
                }
                else{
                    board.getLists().get(listIndex).getTasks().remove(taskIndex);
                }
                listView.createSquares();
                TrelloManager.saveData(getActivity(), board);
            }
        });

        no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteDialog.dismiss();
            }
        });
    }

    public void showEditDialog(String id, boolean isList,  boolean edit) {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(edit ? "Редагування" : isList ? "Новий список" : "Нова задача");

            final View customLayout = getLayoutInflater().inflate(isList ? R.layout.dialog_text_layout : R.layout.dialog_task_layout, null);
            builder.setView(customLayout);

            EditText textEdit = customLayout.findViewById(R.id.dialog_editText);
            EditText dateEdit = isList ? null : customLayout.findViewById(R.id.dialog_deadline);
            EditText descriptionEdit = isList ? null : customLayout.findViewById(R.id.dialog_description);

            Button acceptBtn = customLayout.findViewById(R.id.accept_button);
            Button cancelBtn = customLayout.findViewById(R.id.cancel_button);
            Button addTagBtn = isList ? null : customLayout.findViewById(R.id.add_tag_button);

            acceptBtn.setText(edit ? "Зберегти" : "Додати");
            String textValue = "";
            if (edit) {
                if (isList) {
                    textValue = getListByIndex(findListIndexById(id)).getName();
                } else {
                    int[] indexes = findTaskIndexById(id);
                    Task task = getTaskByIndex(indexes[0], indexes[1]);
                    textValue = task.getName();
                    dateEdit.setText(FormateDate.formatDate(task.getDeadline()));
                    descriptionEdit.setText(task.getDescription());
                }
            }

            textEdit.setText(textValue);

            if (dateEdit != null)
                dateEdit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Calendar calendar = Calendar.getInstance();
                        int year = calendar.get(Calendar.YEAR);
                        int month = calendar.get(Calendar.MONTH);
                        int day = calendar.get(Calendar.DAY_OF_MONTH);

                        DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(),
                                new DatePickerDialog.OnDateSetListener() {
                                    @Override
                                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                        Calendar calendar = Calendar.getInstance();
                                        calendar.set(year, monthOfYear, dayOfMonth);

                                        String fd = FormateDate.formatDate(calendar.getTime());
                                        dateEdit.setText(fd);
                                    }
                                }, year, month, day);

                        datePickerDialog.show();
                    }
                });

            final AlertDialog dialog = builder.show();

            Vector<Tag> tags = edit && !isList ? board.getTaskById(id).getTags() : new Vector<Tag>();

            if (edit && !isList) {
                for (Tag tag : tags) {
                    addTagToDialog(customLayout, tag.getColor());
                }
            }

            if(addTagBtn != null)
                addTagBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        try {
                            AlertDialog.Builder tagBuilder = new AlertDialog.Builder(getActivity());
                            tagBuilder.setTitle("Мітки");

                            final View tagLayout = getLayoutInflater().inflate(R.layout.dialog_tag_layout, null);
                            tagBuilder.setView(tagLayout);

                            final AlertDialog tagDialog = tagBuilder.show();

                            for (int i = 0; i < board.getTags().size(); i++) {
                                Tag tag = board.getTags().get(i);
                                String color = tag.getColor();

                                EditText editText = new EditText(getActivity());

                                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.MATCH_PARENT,
                                        LinearLayout.LayoutParams.WRAP_CONTENT
                                );
                                editText.setLayoutParams(layoutParams);

                                editText.setFocusable(false);
                                editText.setFocusableInTouchMode(false);
                                editText.setInputType(InputType.TYPE_NULL);

                                editText.setBackgroundTintMode(PorterDuff.Mode.SRC_OVER);
                                editText.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(color)));
                                editText.setBackgroundResource(R.drawable.edittext_bg);

                                float scale = getActivity().getResources().getDisplayMetrics().density;

                                int paddingInDp = 10;
                                int paddingInPixels = (int) (paddingInDp * scale + 0.5f);
                                editText.setPadding(paddingInPixels, paddingInPixels, paddingInPixels, paddingInPixels);

                                int marginInDp = 10;
                                int marginInPixels = (int) (marginInDp * scale + 0.5f);
                                layoutParams.setMargins(marginInPixels, marginInPixels, marginInPixels, marginInPixels);
                                editText.setLayoutParams(layoutParams);

                                LinearLayout linearLayout = tagLayout.findViewById(R.id.tag_layout);
                                linearLayout.addView(editText);

                                editText.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        boolean containsTag = false;
                                        for (int i = 0; i < tags.size(); i++) {
                                            if (tags.get(i).getColor().toLowerCase().equals(color.toLowerCase())) {
                                                tags.remove(i);
                                                containsTag = true;
                                            }
                                        }

                                        if (containsTag) {
                                            removeTagFromDialog(customLayout, color);
                                        } else {
                                            if (tags.size() == 2) {
                                                showToast("Не можна додати більше 2-х міток");
                                                return;
                                            }
                                            tags.add(new Tag(color));
                                            addTagToDialog(customLayout, color);
                                        }
                                    }
                                });
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                            showToast("An exception occurred");
                        }
                    }
                });

            acceptBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        if (isList) {
                            String name = textEdit.getText().toString();
                            Validator.validateName(name);
                            List list = board.getListById(id);

                            if (edit) {
                                list.setName(name);
                                listView.invalidate();
                            } else {
                                board.appendList(new List(UniqueCodeGenerator.generateUniqueCode(), name, true));
                                listView.createSquares();
                            }
                        } else {
                            String name = textEdit.getText().toString();
                            String description = descriptionEdit.getText().toString();
                            Date date = FormateDate.parseDate(dateEdit.getText().toString());

                            Validator.validateName(name);
                            Validator.validateDescription(description);
                            Validator.validateDeadline(date);

                            List list = edit ? board.getListByTaskId(id) : board.getListById(id);

                            if (edit) {
                                Task task = list.getTaskById(id);
                                if (task == null) throw new Exception("Задачі не існує");
                                task.setName(name);
                                task.setDescription(description);
                                task.setDeadline(date);
                                task.setTags(tags);
                                listView.invalidate();
                            } else {
                                list.appendTask(new Task(UniqueCodeGenerator.generateUniqueCode(), name, description, date, tags));
                                listView.createSquares();
                            }
                        }

                        TrelloManager.saveData(getActivity(), board);
                        dialog.dismiss();
                    } catch (Exception e) {
                        showToast(e.getMessage());
                    }
                }
            });

            cancelBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                }
            });
        }
        catch (Exception e){
            showToast("Exception");
        }
    }

    public int findListIndexById(String id){
        for(int i = 0; i < board.getLists().size(); i++){
            List list = board.getLists().get(i);
            if(list.getId().compareTo(id) == 0){
                return i;
            }
        }
        return -1;
    }

    public int[] findTaskIndexById(String id){
        int[] array = new int[2];
        for(int i = 0; i < board.getLists().size(); i++){
            List list = board.getLists().get(i);
            array[0] = i;
            for(int j = 0; j < list.getTasks().size(); j++){
                Task task = list.getTasks().get(j);
                if(task.getId().compareTo(id) == 0){
                    array[1] = j;
                    return array;
                }
            }
        }
        return array;
    }

    public void addTagToDialog(View view, String color) {
        try {
            EditText editText = new EditText(getActivity());

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
            );

            editText.setFocusable(false);
            editText.setFocusableInTouchMode(false);
            editText.setInputType(InputType.TYPE_NULL);

            editText.setBackgroundTintMode(PorterDuff.Mode.SRC_OVER);
            editText.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(color)));
            editText.setBackgroundResource(R.drawable.edittext_bg);


            Button btn = view.findViewById(R.id.add_tag_button);
            LinearLayout ll = view.findViewById(R.id.tag_layout);

            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) btn.getLayoutParams();

            int marginLeft = params.leftMargin;
            int marginTop = params.topMargin;
            int marginRight = params.rightMargin;
            int marginBottom = params.bottomMargin;

            btn.post(new Runnable() {
                @Override
                public void run() {
                    int buttonWidth = btn.getWidth();
                    int layoutWidth = ll.getWidth();
                    int editTextWidth = (layoutWidth - buttonWidth - (marginLeft * 3) - (marginRight * 3)) / 2;
                    editText.setWidth(editTextWidth);
                }
            });

            layoutParams.setMargins(marginLeft, marginTop, marginRight, marginBottom);
            editText.setLayoutParams(layoutParams);

            LinearLayout linearLayout = view.findViewById(R.id.tag_layout);
            linearLayout.addView(editText);
        } catch (Exception e) {
            e.printStackTrace();
            showToast("An exception occurred");
        }
    }

    public void removeTagFromDialog(View view, String color){
            try {
                LinearLayout linearLayout = view.findViewById(R.id.tag_layout);
                int childCount = linearLayout.getChildCount();
                for (int i = 0; i < childCount; i++) {
                    View childView = linearLayout.getChildAt(i);
                    if (childView instanceof EditText) {
                        EditText editText = (EditText) childView;
                        ColorStateList backgroundTintList = editText.getBackgroundTintList();

                        int backgroundColor = backgroundTintList.getDefaultColor();
                        int targetColor = Color.parseColor(color);

                        if (backgroundColor == targetColor) {
                            linearLayout.removeViewAt(i);
                            break;

                        }
                    }
                }
            }
            catch (Exception e){
                e.printStackTrace();
                showToast("An exception occurred while removing");
            }
    }

    public List getListByIndex(int index){
        return board.getLists().get(index);
    }

    public Task getTaskByIndex(int listIndex, int taskIndex){
        return board.getLists().get(listIndex).getTasks().get(taskIndex);
    }


    public void showToast(String msg){
        Toast.makeText(getActivity(),msg,Toast.LENGTH_SHORT).show();
    }

}