package com.example.trello;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.trello.databinding.FragmentFirstBinding;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.Date;
import java.util.Locale;
import java.util.Vector;

public class FirstFragment extends Fragment implements OnRectangleClickListener  {

    private FragmentFirstBinding binding;
    private Vector<Board> boards;
    private BoardView boardView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getActivity().setTitle("Дошки");
        binding = FragmentFirstBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        boards = new Vector<Board>();
        TrelloManager.loadData(getActivity(), boards);

        boardView = new BoardView(getContext(), boards);
        boardView.setOnRectangleClickListener(this);

        RelativeLayout relativeLayout = binding.relativeLayout;
        relativeLayout.addView(boardView);
        binding.fabFragmentFirst.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showEditDialog("", false);
            }
        });

        return view;
    }

    @Override
    public void onRectangleClick(String data, boolean isLongPress) {
        int index = findIndex(boards, data);
        if(index != -1) {
            if(isLongPress){
                showDialog(data);
            }
            else {
                Bundle args = new Bundle();
                args.putParcelable("data", boards.get(index));
                NavHostFragment.findNavController(FirstFragment.this).navigate(R.id.action_FirstFragment_to_SecondFragment, args);
            }
        }
    }

    @Override
    public void onRectangleClick(String data, int type, boolean isLongPress) {
    }

    public int findIndex(Vector<Board> boards, String id){
        for(int i = 0; i < boards.size(); i++){
            if(boards.get(i).getId().compareTo(id) == 0)
                return i;
        }
        return -1;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public void showDialog(String id){
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        dialogBuilder.setTitle("Меню дошки");

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
                showEditDialog(id, true);
            }
        });

        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int index = findIndex(boards,id);
                Board board = boards.get(index);

                if(board.getTaskCount() > 0){
                    showToast("Дошку не можна видалити, доки в ній є завдання");
                    return;
                }
                dialog.dismiss();

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
                        boards.remove(index);
                        TrelloManager.saveData(getActivity(), boards);
                        boardView.createSquares();
                    }
                });

                no.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        deleteDialog.dismiss();
                    }
                });
            }
        });

        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
    }

    public void showEditDialog(String id, boolean edit){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(edit ? "Редагування" : "Нова дошка");
        Board board = edit ? boards.get(findIndex(boards, id)) : null;

        final View customLayout = getLayoutInflater().inflate(R.layout.dialog_text_layout, null);
        builder.setView(customLayout);
        EditText text = customLayout.findViewById(R.id.dialog_editText);
        text.setText(edit ? board.getName() : "");

        Button acceptBtn = customLayout.findViewById(R.id.accept_button);
        Button cancelBtn = customLayout.findViewById(R.id.cancel_button);

        final AlertDialog dialog  = builder.show();

        acceptBtn.setText(edit ? "Зберегти" : "Додати");
        acceptBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    String name = text.getText().toString();
                    Validator.validateName(name);

                    if (edit) {
                        board.setName(name);
                        boardView.invalidate();
                    } else {
                        boards.add(new Board(UniqueCodeGenerator.generateUniqueCode(), name));
                        boardView.createSquares();
                    }

                    TrelloManager.saveData(getActivity(), boards);
                    dialog.dismiss();
                }
                catch (Exception e){
                    showToast(e.getMessage());
                }
            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("first", "Cancel");
                dialog.dismiss();
            }
        });

    }

    public void showToast(String msg){
        Toast.makeText(getActivity(),msg,Toast.LENGTH_SHORT).show();
    }
}