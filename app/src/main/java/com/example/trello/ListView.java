package com.example.trello;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.Vector;

public class ListView extends View {
    private static final int LONG_PRESS_TIMEOUT = 300;
    private boolean isLongPress = false;
    private boolean isMove = false;
    private Handler longPressHandler = new Handler();

    private float lastTouchX;
    private float lastTouchY;
    private Vector<List> lists;

    private Vector<RectF> listSquares;
    private Vector<RectF> taskSquares;
    private Vector<RectF> addTaskSquares;

    private Paint listPaint;
    private Paint textPaint;
    private Paint taskPaint;
    private OnRectangleClickListener listener;

    private int scrollableHeight;
    private int scrollableTop;
    private int scrollableBottom;

    private int scrollableWidth;
    private int scrollableLeft;
    private int scrollableRight;

    private int minMargin;

    public ListView(Context context, Vector<List> lists) {
        super(context);
        listSquares = new Vector<RectF>();
        taskSquares = new Vector<RectF>();
        addTaskSquares = new Vector<RectF>();

        listPaint = new Paint();
        listPaint.setColor(Color.parseColor("#101204"));
        listPaint.setStyle(Paint.Style.FILL);

        taskPaint = new Paint();
        taskPaint.setColor(Color.parseColor("#22272b"));
        taskPaint.setStyle(Paint.Style.FILL);

        textPaint = new Paint();
        textPaint.setColor(Color.parseColor("#a9b4bf"));
        textPaint.setFakeBoldText(true);

        this.lists = lists;
        createSquares();
        textPaint.setTextSize(minMargin / 0.66153f);
    }

    private void initScrollableArea() {
        int totalHeight = 0;
        int totalWidth = 0;
        if (!listSquares.isEmpty()){
            totalWidth = (int)(listSquares.lastElement().right + minMargin);
            for(int i = 0; i < listSquares.size(); i++) {
                totalHeight = Math.max(totalHeight, (int)listSquares.get(i).bottom + minMargin);
            }
        }

        scrollableHeight = Math.max(totalHeight, getHeight());
        scrollableTop = 0;
        scrollableBottom = scrollableHeight;

        scrollableWidth = Math.max(totalWidth, getWidth());
        scrollableLeft = 0;
        scrollableRight = scrollableWidth;
    }

    public void createSquares() {
        listSquares.clear();
        taskSquares.clear();
        addTaskSquares.clear();

        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int screenHeight = getResources().getDisplayMetrics().heightPixels;

        float fRatio = ((float) screenWidth) / screenHeight;

        float marginPercent = 0.03f;
        minMargin = (int) (Math.min(screenWidth, screenHeight) * marginPercent);

        int squaresRowCount = fRatio > 1 ? 2 : 1;
        int squareSize = ((screenWidth - (squaresRowCount + 1) * minMargin) / squaresRowCount) - (minMargin * 2);

        int taskSize = squareSize - 2 * minMargin;
        int taskHeight = taskSize / 3;

        int firstMargin = minMargin;
        int lastMargin = firstMargin;

        for (int i = 0; i < lists.size(); i++) {
            Vector<Task> tasks = lists.get(i).getTasks();
            int tasksSize = tasks.size();
            int taskCount = Math.max(tasksSize - 1, 0);
            float divider = tasksSize == 0 ? 2.5f : 2;

            int listStartX = firstMargin + i * (squareSize + minMargin);
            int listStartY = minMargin;
            int withTagsCount = 0;

            for (int j = 0; j < tasksSize; j++) {
                boolean withTags = tasks.get(i).getTags().size() != 0;
                withTagsCount++;
                int taskStartY = listStartY + minMargin * (6 - squaresRowCount) + j * (taskHeight + minMargin + (withTagsCount > 0 ? minMargin : 0));
                RectF taskRect = new RectF(listStartX + minMargin, taskStartY, listStartX + squareSize - lastMargin, (withTags ? minMargin : 0) + taskStartY + taskHeight);
                taskSquares.add(taskRect);
            }

            RectF square = new RectF(listStartX, listStartY, listStartX + squareSize, listStartY + squareSize / divider + taskCount * (taskHeight + minMargin));
            if (tasksSize != 0) {
                square.bottom -= (square.bottom - minMargin * 4 - square.bottom - withTagsCount * minMargin);
            }

            RectF addSquare = new RectF(listStartX + minMargin, square.bottom - minMargin * 4, listStartX + squareSize - lastMargin, square.bottom - minMargin);

            listSquares.add(square);
            addTaskSquares.add(addSquare);
        }

        initScrollableArea();
        invalidate();
    }

    public void setOnRectangleClickListener(OnRectangleClickListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        setClickable(true);
        setFocusable(true);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float touchX = event.getX();
        float touchY = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                isLongPress = false;
                isMove = false;
                lastTouchX = touchX;
                lastTouchY = touchY;
                longPressHandler.postDelayed(longPressRunnable, LONG_PRESS_TIMEOUT);
                break;

            case MotionEvent.ACTION_MOVE:
                float deltaX = touchX - lastTouchX;
                float deltaY = touchY - lastTouchY;

                if(Math.abs(deltaX) > 10 || Math.abs(deltaY) > 10){
                    longPressHandler.removeCallbacks(longPressRunnable);
                    isMove = true;
                }

                int newScrollX = getScrollX() - (int) deltaX;
                int newScrollY = getScrollY() - (int) deltaY;

                newScrollX = Math.max(scrollableLeft, Math.min(newScrollX, scrollableRight - getWidth()));
                newScrollY = Math.max(scrollableTop, Math.min(newScrollY, scrollableBottom - getHeight()));

                scrollTo(newScrollX, newScrollY);

                lastTouchX = touchX;
                lastTouchY = touchY;
                break;

            case MotionEvent.ACTION_UP:
                getParent().requestDisallowInterceptTouchEvent(false);
                lastTouchX = 0;
                lastTouchY = 0;
                longPressHandler.removeCallbacks(longPressRunnable);
                if (!isMove && !isLongPress) {
                    if(checkSquareTouch(touchX, touchY))
                        return true;
                }
                break;
        }
        return super.onTouchEvent(event);
    }

    private boolean checkSquareTouch(float touchX, float touchY){
        int scrollX = getScrollX();
        int scrollY = getScrollY();
        int touchXWithScroll = (int) (touchX + scrollX);
        int touchYWithScroll = (int) (touchY + scrollY);
        int minTask = 0;

        for (int i = 0; i < listSquares.size(); i++) {
            RectF listRect = listSquares.get(i);
            List list = lists.get(i);

            if (listRect.contains(touchXWithScroll, touchYWithScroll)) {
                String data = lists.get(i).getId();
                int type = 1;

                if(addTaskSquares.get(i).contains(touchXWithScroll, touchYWithScroll)){
                    type = 2;
                }

                for (int j = minTask; j < minTask + list.getTasks().size(); j++) {
                    RectF taskRect = taskSquares.get(j);
                    if (taskRect.contains(touchXWithScroll, touchYWithScroll)) {
                        Task task = list.getTasks().get(j - minTask);
                        data = task.getId();
                        type = 3;
                        break;
                    }
                }

                if (listener != null) {
                    listener.onRectangleClick(data, type, isLongPress);
                }
                return true;
            }

            minTask += list.getTasks().size();
        }
        return false;
    }

    private Runnable longPressRunnable = new Runnable() {
        @Override
        public void run() {
            isLongPress = true;
            checkSquareTouch(lastTouchX, lastTouchY);
        }
    };

    private void drawDate(RectF parent, Canvas canvas, String date) {
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.clock);

        float padding = parent.width() * 0.05f;
        float rectHeight = parent.height() / 4;
        int bitmapSize = Math.min((int)rectHeight, bitmap.getWidth());

        float textWidth = textPaint.measureText(date);
        float textHeight = textPaint.getTextSize();

        float bitmapLeft = parent.left + padding + textHeight / 3;
        float bitmapTop = parent.bottom - bitmapSize - padding + textHeight / 3;
        float bitmapRight = bitmapLeft + bitmapSize - textHeight * 0.7f;
        float bitmapBottom = parent.bottom - padding - textHeight / 3;

        RectF bitmapRect = new RectF(bitmapLeft, bitmapTop, bitmapRight, bitmapBottom);

        float left = parent.left + padding;
        float top = parent.bottom - rectHeight - padding;
        float right = left + bitmapRect.width() + textWidth;
        float bottom = parent.bottom - padding;

        RectF childRect = new RectF(left, top, right, bottom);

        float radius = minMargin / 2.15f;
        Paint paint = new Paint();
        paint.setColor(Color.parseColor("#4cab83"));

        canvas.drawRoundRect(childRect, radius, radius, paint);

        paint.setColor(Color.BLACK);
        paint.setTextSize(minMargin / 0.86f);
        paint.setTextAlign(Paint.Align.CENTER);

        canvas.drawBitmap(bitmap, null, bitmapRect, paint);

        float textLeft = bitmapRight + bitmapSize / 4;
        float textTop = bitmapTop + rectHeight / 2;

        paint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText(date, textLeft, textTop, paint);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int minTask = 0;
        for (int i = 0; i < listSquares.size(); i++) {
            RectF listRect = listSquares.get(i);
            RectF addTaskRect = addTaskSquares.get(i);
            String listText = lists.get(i).getName();
            float textHeight = textPaint.getTextSize();

            float listPadding = listRect.width() * 0.05f;

            float listX = listRect.left + listPadding;
            float listY = listRect.top + listPadding + textHeight;

            float tileX = addTaskRect.left + listPadding;
            float tileY = addTaskRect.top + listPadding / 2 + textHeight;

            float radius = minMargin / 1.075f;

            canvas.drawRoundRect(listRect, radius, radius, listPaint);
            canvas.drawRoundRect(addTaskRect, radius, radius, listPaint);
            canvas.drawText(listText, listX, listY, textPaint);
            canvas.drawText("Додати картку", tileX, tileY, textPaint);

            Vector<Task> tasks = lists.get(i).getTasks();

            for (int j = minTask; j < minTask + tasks.size(); j++) {

                Task task = tasks.get(j - minTask);
                int tagsCount = task.getTags().size();

                RectF rect = taskSquares.get(j);
                float taskTextHeight = textPaint.getTextSize();

                float taskPadding = rect.width() * 0.05f;
                float taskX = rect.left + taskPadding;
                float taskY = rect.top + taskPadding + taskTextHeight + (tagsCount > 0 ? minMargin * 2 : 0);

                canvas.drawRoundRect(rect, radius, radius, taskPaint);
                canvas.drawText(task.getName(), taskX, taskY, textPaint);
                drawDate(rect, canvas, task.getFormatDeadline());

                float tagStartX = taskX;

                for(int k = 0; k < tagsCount; k++){
                    Paint tagPaint = new Paint();
                    tagPaint.setColor(Color.parseColor(task.getTags().get(k).getColor()));

                    RectF tagRect = new RectF(tagStartX, taskY - minMargin * 3.5f, tagStartX + rect.width() / 4, taskY - minMargin * 2.5f);
                    canvas.drawRoundRect(tagRect, radius, radius, tagPaint);

                    tagStartX += rect.width() / 3.75;
                }
            }

            minTask += tasks.size();
        }
    }

}
