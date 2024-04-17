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
import android.widget.ScrollView;

import java.util.ArrayList;
import java.util.Vector;

public class BoardView extends View{

    private static final int LONG_PRESS_TIMEOUT = 300;
    private boolean isLongPress = false;
    private boolean isMove = false;
    private Handler longPressHandler = new Handler();

    private float lastTouchX;
    private float lastTouchY;
    private Vector<Board> boards;
    private Vector<RectF> squares;
    private Paint paint;
    private Paint textPaint;
    private OnRectangleClickListener listener;

    private int scrollableHeight;
    private int scrollableTop;
    private int scrollableBottom;

    private int minMargin;

    public BoardView(Context context, Vector<Board> boards) {
        super(context);
        squares = new Vector<RectF>();
        paint = new Paint();
        paint.setColor(Color.parseColor("#101204"));
        paint.setStyle(Paint.Style.FILL);

        textPaint = new Paint();
        textPaint.setColor(Color.parseColor("#a9b4bf"));
        textPaint.setFakeBoldText(true);

        this.boards = boards;
        createSquares();
        initScrollableArea();

        textPaint.setTextSize((int)(minMargin / 0.86));
    }

    private void initScrollableArea() {
        int totalHeight = 0;
        if (!squares.isEmpty()){
            totalHeight = (int)squares.lastElement().bottom + minMargin;
        }

        scrollableHeight = Math.max(totalHeight, getHeight());
        scrollableTop = 0;
        scrollableBottom = scrollableHeight;
    }

    public void createSquares() {
        squares.clear();

        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int screenHeight = getResources().getDisplayMetrics().heightPixels;

        float fRatio = ((screenWidth * 1.0f) / (screenHeight * 1.0f));

        float horizontalMarginPercent = 0.03f;
        float verticalMarginPercent = 0.03f;

        int horizontalMargin = (int) (screenWidth * horizontalMarginPercent);
        int verticalMargin = (int) (screenHeight * verticalMarginPercent);

        minMargin = horizontalMargin > verticalMargin ? verticalMargin : horizontalMargin;

        int squaresRowCount = fRatio > 1 ? 4 : 2;
        int squareSize = (screenWidth - (squaresRowCount + 1) * minMargin) / squaresRowCount;

        int startX = minMargin;
        int startY = minMargin;

        for(int i = 0; i < boards.size(); i++){
            RectF square = new RectF(startX, startY, startX + squareSize, startY + squareSize / 2);
            squares.add(square);

            startX += squareSize + minMargin;
            if (startX > screenWidth - horizontalMargin) {
                startX = minMargin;
                startY += squareSize / 2 + minMargin;
            }
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

                /*Log.d("X", "" + deltaX);
                Log.d("Y", "" + deltaY);*/
                if(Math.abs(deltaX) > 7 || Math.abs(deltaY) > 7){
                    longPressHandler.removeCallbacks(longPressRunnable);
                    isMove = true;
                }

                int newScrollY = getScrollY() - (int) deltaY;
                newScrollY = Math.max(scrollableTop, Math.min(newScrollY, scrollableBottom - getHeight()));
                scrollTo(0, newScrollY);

                lastTouchX = touchX;
                lastTouchY = touchY;
                break;

            case MotionEvent.ACTION_UP:
                getParent().requestDisallowInterceptTouchEvent(false);
                lastTouchX = 0;
                lastTouchY = 0;
                longPressHandler.removeCallbacks(longPressRunnable);
                if (!isMove && !isLongPress) {
                    if(checkSquareTouch(touchX + getScrollX(), touchY + getScrollY()))
                        return true;
                }
                break;
        }
        return super.onTouchEvent(event);
    }

    private boolean checkSquareTouch(float touchX, float touchY){
        for (int i = 0; i < squares.size(); i++) {
            RectF rect = squares.get(i);
            if (rect.contains(touchX, touchY)) {
                String data = boards.get(i).getId();
                if (listener != null) {
                    listener.onRectangleClick(data, isLongPress);
                }
                return true;
            }
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

        float padding = parent.width() * 0.1f;
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

        Paint paint = new Paint();
        paint.setColor(Color.parseColor("#4cab83"));

        float radius = minMargin / 2.15f;
        canvas.drawRoundRect(childRect, radius, radius, paint);

        paint.setColor(Color.BLACK);
        paint.setTextSize(minMargin / 1.075f);
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
        if(squares.size() > 0) {
            for (int i = 0; i < squares.size(); i++) {
                RectF rect = squares.get(i);
                String text = boards.get(i).getName();
                float textWidth = textPaint.measureText(text);
                float textHeight = textPaint.getTextSize();

                float padding = rect.width() * 0.1f;

                float x = rect.left + padding;
                float y = rect.top + padding + textHeight;

                float radius = minMargin / 1.075f;
                canvas.drawRoundRect(rect, radius, radius, paint);
                canvas.drawText(text, x, y, textPaint);
                drawDate(rect, canvas, boards.get(i).getFormatDate());
            }
        }
        else{
            int screenWidth = getResources().getDisplayMetrics().widthPixels;
            int screenHeight = getResources().getDisplayMetrics().heightPixels;

            int rectWidth = Math.min(screenWidth, screenHeight) / 2;
            int rectHeight = Math.min(screenWidth, screenHeight) / 2;

            int left = (screenWidth - rectWidth) / 2;
            int top = (screenHeight - rectHeight) / 2;
            int right = left + rectWidth;
            int bottom = top + rectHeight / 2;

            float radius = minMargin / 1.075f;

            RectF rectangle = new RectF(left, top, right, bottom);
            Paint.FontMetrics fm = textPaint.getFontMetrics();

            String text = "Створіть першу дошку";
            float textWidth = textPaint.measureText(text);
            float textX = rectangle.centerX() - textWidth / 2;
            float textY = rectangle.centerY() - (fm.ascent + fm.descent) / 2;

            canvas.drawRoundRect(rectangle,radius,radius, paint);
            canvas.drawText(text, textX, textY, textPaint);
        }
    }

    public void updateRectangles(Vector<RectF> rectangles) {
        this.squares = rectangles;
        invalidate();
    }
}
