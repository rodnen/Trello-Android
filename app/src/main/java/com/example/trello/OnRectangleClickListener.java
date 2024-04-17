package com.example.trello;

public interface OnRectangleClickListener {
    void onRectangleClick(String data, boolean isLongPress);
    void onRectangleClick(String data, int type, boolean isLongPress);
}
