package com.fh.anitrack.ui.browse.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.fh.anitrack.R;

/**
 * Custom Range Slider with capsule-shaped thumbs that display the current value inside.
 * The thumbs are rounded capsules with a blue background and white text.
 */
public class LabeledRangeSlider extends View {

    // Paint objects
    private Paint trackPaint;
    private Paint activeTrackPaint;
    private Paint thumbPaint;
    private Paint thumbTextPaint;

    // Dimensions
    private float trackHeight;
    private float thumbWidth;
    private float thumbHeight;
    private float thumbCornerRadius;
    private float thumbTextSize;
    private float thumbPadding;

    // Values
    private float minValue = 0;
    private float maxValue = 100;
    private float minSelectedValue;
    private float maxSelectedValue;
    private int stepSize = 1;
    private boolean showAsInt = true;

    // State
    private boolean isDraggingMin = false;
    private boolean isDraggingMax = false;
    private float touchSlop;

    // Rects for drawing
    private RectF trackRect = new RectF();
    private RectF activeTrackRect = new RectF();
    private RectF minThumbRect = new RectF();
    private RectF maxThumbRect = new RectF();

    // Listener
    private OnRangeChangedListener listener;

    public interface OnRangeChangedListener {
        void onRangeChanged(float minValue, float maxValue);
    }

    public LabeledRangeSlider(Context context) {
        super(context);
        init(context, null);
    }

    public LabeledRangeSlider(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public LabeledRangeSlider(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        // Default dimensions (in dp)
        float density = getResources().getDisplayMetrics().density;
        trackHeight = 8 * density;
        thumbWidth = 60 * density;
        thumbHeight = 32 * density;
        thumbCornerRadius = 16 * density;
        thumbTextSize = 14 * density;
        thumbPadding = 8 * density;
        touchSlop = 24 * density;

        // Parse attributes if provided
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.LabeledRangeSlider);
            minValue = a.getFloat(R.styleable.LabeledRangeSlider_lrs_minValue, 0);
            maxValue = a.getFloat(R.styleable.LabeledRangeSlider_lrs_maxValue, 100);
            minSelectedValue = a.getFloat(R.styleable.LabeledRangeSlider_lrs_minSelectedValue, minValue);
            maxSelectedValue = a.getFloat(R.styleable.LabeledRangeSlider_lrs_maxSelectedValue, maxValue);
            stepSize = a.getInt(R.styleable.LabeledRangeSlider_lrs_stepSize, 1);
            showAsInt = a.getBoolean(R.styleable.LabeledRangeSlider_lrs_showAsInt, true);
            a.recycle();
        } else {
            minSelectedValue = minValue;
            maxSelectedValue = maxValue;
        }

        // Initialize paints
        trackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        trackPaint.setColor(ContextCompat.getColor(context, R.color.lightGrey));
        trackPaint.setStyle(Paint.Style.FILL);

        activeTrackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        activeTrackPaint.setColor(ContextCompat.getColor(context, R.color.darkBlue));
        activeTrackPaint.setStyle(Paint.Style.FILL);

        thumbPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        thumbPaint.setColor(ContextCompat.getColor(context, R.color.darkBlue));
        thumbPaint.setStyle(Paint.Style.FILL);

        thumbTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        thumbTextPaint.setColor(ContextCompat.getColor(context, R.color.white));
        thumbTextPaint.setTextSize(thumbTextSize);
        thumbTextPaint.setTextAlign(Paint.Align.CENTER);
        thumbTextPaint.setFakeBoldText(true);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int desiredHeight = (int) (thumbHeight + getPaddingTop() + getPaddingBottom());
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = resolveSize(desiredHeight, heightMeasureSpec);
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float width = getWidth() - getPaddingLeft() - getPaddingRight();
        float centerY = getHeight() / 2f;

        // Calculate thumb positions
        float trackStartX = getPaddingLeft() + thumbWidth / 2;
        float trackEndX = getWidth() - getPaddingRight() - thumbWidth / 2;
        float trackWidth = trackEndX - trackStartX;

        float minRatio = (minSelectedValue - minValue) / (maxValue - minValue);
        float maxRatio = (maxSelectedValue - minValue) / (maxValue - minValue);

        float minThumbCenterX = trackStartX + (trackWidth * minRatio);
        float maxThumbCenterX = trackStartX + (trackWidth * maxRatio);

        // Draw inactive track (full length)
        float trackTop = centerY - trackHeight / 2;
        float trackBottom = centerY + trackHeight / 2;
        trackRect.set(trackStartX, trackTop, trackEndX, trackBottom);
        canvas.drawRoundRect(trackRect, trackHeight / 2, trackHeight / 2, trackPaint);

        // Draw active track (between thumbs)
        activeTrackRect.set(minThumbCenterX, trackTop, maxThumbCenterX, trackBottom);
        canvas.drawRoundRect(activeTrackRect, trackHeight / 2, trackHeight / 2, activeTrackPaint);

        // Draw min thumb
        minThumbRect.set(
                minThumbCenterX - thumbWidth / 2,
                centerY - thumbHeight / 2,
                minThumbCenterX + thumbWidth / 2,
                centerY + thumbHeight / 2
        );
        canvas.drawRoundRect(minThumbRect, thumbCornerRadius, thumbCornerRadius, thumbPaint);

        // Draw max thumb
        maxThumbRect.set(
                maxThumbCenterX - thumbWidth / 2,
                centerY - thumbHeight / 2,
                maxThumbCenterX + thumbWidth / 2,
                centerY + thumbHeight / 2
        );
        canvas.drawRoundRect(maxThumbRect, thumbCornerRadius, thumbCornerRadius, thumbPaint);

        // Draw text inside thumbs
        Paint.FontMetrics fm = thumbTextPaint.getFontMetrics();
        float textY = centerY - (fm.ascent + fm.descent) / 2;

        String minText = formatValue(minSelectedValue);
        String maxText = formatValue(maxSelectedValue);

        canvas.drawText(minText, minThumbCenterX, textY, thumbTextPaint);
        canvas.drawText(maxText, maxThumbCenterX, textY, thumbTextPaint);
    }

    private String formatValue(float value) {
        if (showAsInt) {
            return String.valueOf((int) value);
        } else {
            return String.format("%.1f", value);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // Check if touch is on min thumb
                if (isInThumbArea(x, y, minThumbRect)) {
                    isDraggingMin = true;
                    getParent().requestDisallowInterceptTouchEvent(true);
                    return true;
                }
                // Check if touch is on max thumb
                if (isInThumbArea(x, y, maxThumbRect)) {
                    isDraggingMax = true;
                    getParent().requestDisallowInterceptTouchEvent(true);
                    return true;
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (isDraggingMin || isDraggingMax) {
                    float trackStartX = getPaddingLeft() + thumbWidth / 2;
                    float trackEndX = getWidth() - getPaddingRight() - thumbWidth / 2;
                    float trackWidth = trackEndX - trackStartX;

                    float ratio = (x - trackStartX) / trackWidth;
                    ratio = Math.max(0, Math.min(1, ratio));
                    float newValue = minValue + ratio * (maxValue - minValue);

                    // Snap to step
                    newValue = Math.round(newValue / stepSize) * stepSize;
                    newValue = Math.max(minValue, Math.min(maxValue, newValue));

                    if (isDraggingMin) {
                        minSelectedValue = Math.min(newValue, maxSelectedValue - stepSize);
                    } else if (isDraggingMax) {
                        maxSelectedValue = Math.max(newValue, minSelectedValue + stepSize);
                    }

                    invalidate();
                    notifyListener();
                    return true;
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                isDraggingMin = false;
                isDraggingMax = false;
                getParent().requestDisallowInterceptTouchEvent(false);
                return true;
        }

        return super.onTouchEvent(event);
    }

    private boolean isInThumbArea(float x, float y, RectF thumbRect) {
        return x >= thumbRect.left - touchSlop && x <= thumbRect.right + touchSlop &&
                y >= thumbRect.top - touchSlop && y <= thumbRect.bottom + touchSlop;
    }

    private void notifyListener() {
        if (listener != null) {
            listener.onRangeChanged(minSelectedValue, maxSelectedValue);
        }
    }

    // Public setters
    public void setRange(float min, float max) {
        this.minValue = min;
        this.maxValue = max;
        this.minSelectedValue = Math.max(minSelectedValue, min);
        this.maxSelectedValue = Math.min(maxSelectedValue, max);
        invalidate();
    }

    public void setSelectedRange(float minSelected, float maxSelected) {
        this.minSelectedValue = Math.max(minValue, Math.min(minSelected, maxValue));
        this.maxSelectedValue = Math.max(minValue, Math.min(maxSelected, maxValue));
        invalidate();
        notifyListener();
    }

    public void setStepSize(int stepSize) {
        this.stepSize = stepSize;
    }

    public void setShowAsInt(boolean showAsInt) {
        this.showAsInt = showAsInt;
        invalidate();
    }

    public void setOnRangeChangedListener(OnRangeChangedListener listener) {
        this.listener = listener;
    }

    // Public getters
    public float getMinSelectedValue() {
        return minSelectedValue;
    }

    public float getMaxSelectedValue() {
        return maxSelectedValue;
    }

    public float getMinValue() {
        return minValue;
    }

    public float getMaxValue() {
        return maxValue;
    }
}
