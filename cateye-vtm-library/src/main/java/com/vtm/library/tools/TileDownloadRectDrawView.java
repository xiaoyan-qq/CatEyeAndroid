package com.vtm.library.tools;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class TileDownloadRectDrawView extends View {
    private Paint mPaint;
    private Canvas mBufferCanvas;
    private Bitmap mBufferBitmap;
    private Point startPoint;

    public TileDownloadRectDrawView(Context context) {
        super(context);
    }

    public TileDownloadRectDrawView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(Color.GREEN);
        setBackgroundColor(Color.WHITE);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                if (mBufferBitmap == null) {
                    mBufferBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
                    mBufferCanvas = new Canvas(mBufferBitmap);
                }
                mBufferCanvas.restore();
                if (startPoint == null) {
                    startPoint = new Point();
                }
                startPoint.set((int) event.getX(), (int) event.getY());
                break;
            case MotionEvent.ACTION_MOVE:
                mBufferCanvas.drawRect(startPoint.x <= event.getX() ? startPoint.x : event.getX(), startPoint.y <= event.getY() ? startPoint.y : event.getY(),startPoint.x >= event.getX() ? startPoint.x : event.getX(), startPoint.y >= event.getY() ? startPoint.y : event.getY(),mPaint );
                invalidate();
                break;
        }
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mBufferBitmap == null) {
            return;
        }
        canvas.drawBitmap(mBufferBitmap, 0, 0, null);
    }
}
