package com.chemistry.android;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import androidx.core.graphics.PathParser;

public class MaskedImageView extends androidx.appcompat.widget.AppCompatImageView {

    private Bitmap maskBitmap;
    private Paint paint;
    private String pathData;

    public MaskedImageView(Context context) {
        super(context);
    }

    public MaskedImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.MaskedImageView,
                0, 0);

        try {
            pathData = a.getString(R.styleable.MaskedImageView_maskPathData);
        } finally {
            a.recycle();
        }
    }

    public MaskedImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Drawable drawable = getDrawable();
        if (drawable == null) {
            super.onDraw(canvas);
            return;
        }

        if (maskBitmap == null) {
            maskBitmap = getMaskBitmap();
        }

        if (paint == null) {
            paint = new Paint();
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        }

        int saveCount = canvas.saveLayer(0, 0, getWidth(), getHeight(), null, Canvas.ALL_SAVE_FLAG);

        super.onDraw(canvas);

        canvas.drawBitmap(maskBitmap, 0, 0, paint);

        canvas.restoreToCount(saveCount);
    }

    private Bitmap getMaskBitmap() {
        Bitmap maskBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(maskBitmap);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.BLACK);
        canvas.drawPath(getMaskPath(), paint);
        return maskBitmap;
    }

    private Path getMaskPath() {
        Path path = PathParser.createPathFromPathData(pathData);
        RectF bounds = new RectF(0, 0, getWidth(), getHeight());
        Path scaledPath = new Path();
        Matrix matrix = new Matrix();
        RectF pathBounds = new RectF();
        path.computeBounds(pathBounds, true);
        matrix.setRectToRect(pathBounds, bounds, Matrix.ScaleToFit.CENTER);
        path.transform(matrix, scaledPath);
        return scaledPath;
    }
}