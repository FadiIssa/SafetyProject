package com.example.fadi.testingrx.ui.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;

import com.example.fadi.testingrx.R;

import java.util.HashMap;

/**
 * Created by fadi on 11/10/2017.
 */

public class GaugeView extends View {
    private static final String TAG = "GaugeView";
    private Paint mPaint;
    private Paint mShaderPaint;
    private RectF mRect;
    private RectF mArcRect;
    private final Path mPath = new Path();
    private int mGaugeBarColor;
    private int mGaugeGradientStartColor;
    private int mGaugeGradientEndColor;
    private int mProgressColor;
    private float mStrokeWidth = 16.0F;
    private int mProgress = 1;
    private ValueAnimator mValueAnimator;
    private long mAnimDuration = 400L;
    private HashMap _$_findViewCache;

    public final long getMAnimDuration() {
        return this.mAnimDuration;
    }

    public final void setMAnimDuration(long var1) {
        this.mAnimDuration = var1;
    }

    public final void init(@Nullable Context context, @Nullable AttributeSet attrs) {
        TypedArray typedArray = context != null ? context.obtainStyledAttributes(attrs, R.styleable.GaugeView, 0, 0) : null;
        if (typedArray != null) {
            this.mProgressColor = typedArray.getColor(0, ContextCompat.getColor(context, R.color.colorAccent));
            typedArray.recycle();
        }

        this.mGaugeBarColor = ContextCompat.getColor(context, R.color.gaugeBar);
        this.mGaugeGradientStartColor = ContextCompat.getColor(context, R.color.gaugeGradientStart);
        this.mGaugeGradientEndColor = ContextCompat.getColor(context, R.color.gaugeGradientEnd);
        this.mPaint = new Paint();
        this.mShaderPaint = new Paint();

        Paint paint = this.mPaint;
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(this.mStrokeWidth);
        paint = this.mPaint;

        paint.setAntiAlias(true);
        paint = this.mPaint;

        paint.setShader((Shader) null);
        paint = this.mPaint;

        paint.setColor(-1);
        paint = this.mShaderPaint;

        paint.setStyle(Paint.Style.FILL);
        paint = this.mShaderPaint;

        paint.setAntiAlias(true);
        this.mRect = new RectF();
        this.mArcRect = new RectF();
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        this.setMeasuredDimension(this.getMeasuredWidth(), this.getMeasuredWidth() / 2);
    }

    protected void onDraw(@Nullable Canvas canvas) {
        super.onDraw(canvas);
        float width = (float) this.getWidth() - this.mStrokeWidth;
        Paint paint = this.mPaint;


        paint.setColor(this.mGaugeBarColor);
        paint = this.mPaint;

        paint.setStyle(Paint.Style.STROKE);
        RectF rectF = this.mArcRect;


        rectF.set(this.mStrokeWidth, this.mStrokeWidth, width, (float) (this.getHeight() * 2));
        Path path = this.mPath;
        RectF arcRect = this.mArcRect;


        path.addArc(arcRect, 180.0F, 180.0F);
        Path pathArc;
        Paint paintArc;
        if (canvas != null) {
            pathArc = this.mPath;
            paintArc = this.mPaint;

            canvas.drawPath(pathArc, paintArc);
        }

        this.mPath.reset();
        path = this.mPath;
        arcRect = this.mArcRect;


        path.addArc(arcRect, 180.0F, this.progressToDegrees());
        paint = this.mPaint;


        paint.setColor(this.mProgressColor);
        paint = this.mPaint;


        paint.setStyle(Paint.Style.STROKE);
        if (canvas != null) {
            pathArc = this.mPath;
            paintArc = this.mPaint;

            canvas.drawPath(pathArc, paintArc);
        }

        paint = this.mPaint;

        paint.setColor(-1);
        paint = this.mPaint;

        paint.setStyle(Paint.Style.FILL);
        if (canvas != null) {
            arcRect = this.mArcRect;

            float x = arcRect.centerX();
            RectF rect = this.mArcRect;

            float y = rect.centerY();
            Paint mPaint = this.mPaint;

            canvas.drawCircle(x, y, 36.0F, mPaint);
        }

        rectF = this.mArcRect;


        rectF.set(this.mStrokeWidth + this.mStrokeWidth / (float) 2, this.mStrokeWidth + this.mStrokeWidth / (float) 2, width - this.mStrokeWidth / (float) 2, (float) (this.getHeight() * 2) - this.mStrokeWidth / (float) 2);
        rectF = this.mArcRect;


        float radius = rectF.width() / (float) 2 - this.mStrokeWidth / (float) 2;
        paint = this.mShaderPaint;


        RectF radialGr = this.mArcRect;


        float centerX = radialGr.centerX();
        RectF rect = this.mArcRect;
        RadialGradient radialGradient = new RadialGradient(centerX, rect.centerY(), radius, new int[]{this.mGaugeGradientEndColor, this.mGaugeGradientStartColor}, new float[]{0.33F, 1.0F}, Shader.TileMode.CLAMP);

//        radialGradient.<init>
//        (centerX, rect.centerY(), radius, new int[]{this.mGaugeGradientEndColor, this.mGaugeGradientStartColor}, new float[]{0.33F, 1.0F}, Shader.TileMode.CLAMP);
        paint.setShader((Shader) radialGradient);
        if (canvas != null) {
            arcRect = this.mArcRect;
            centerX = this.progressToDegrees();
            Paint mShaderPaint = this.mShaderPaint;
            canvas.drawArc(arcRect, 180.0F, centerX, true, mShaderPaint);
        }

    }

    private final float progressToDegrees() {
        return (float) this.mProgress * 1.8F;
    }

    public final void setProgress(int progress) throws Throwable {
        if (progress >= 0 && progress <= 100) {
            int previousProgress = this.mProgress;
            this.mProgress = progress;
            this.animateGauge(previousProgress, progress);
        } else {
            throw (Throwable) (new IllegalArgumentException("Progress out of range"));
        }
    }

    private final void animateGauge(int previousProgress, int newProgress) {
        ValueAnimator valueAnimator = this.mValueAnimator;
        if (this.mValueAnimator != null) {
            valueAnimator.cancel();
        }

        valueAnimator = this.mValueAnimator;
        if (this.mValueAnimator != null) {
            valueAnimator.removeAllUpdateListeners();
        }

        this.mValueAnimator = ValueAnimator.ofInt(new int[]{previousProgress, newProgress});
        valueAnimator = this.mValueAnimator;
        if (this.mValueAnimator != null) {
            valueAnimator.setDuration(this.mAnimDuration);
        }

        valueAnimator = this.mValueAnimator;
        if (this.mValueAnimator != null) {
            valueAnimator.addUpdateListener((ValueAnimator.AnimatorUpdateListener) (new ValueAnimator.AnimatorUpdateListener() {
                public final void onAnimationUpdate(ValueAnimator it) {
                    GaugeView gaugeView = GaugeView.this;
                    Object object = it.getAnimatedValue();
                    if (object != null) {
                        gaugeView.mProgress = ((Integer) object).intValue();
                        GaugeView.this.invalidate();
                    }
                }
            }));
        }

        valueAnimator = this.mValueAnimator;
        if (this.mValueAnimator != null) {
            valueAnimator.start();
        }

        this.invalidate();
    }

    public GaugeView(@Nullable Context context) {
        super(context);
        this.init(context, (AttributeSet) null);
    }

    public GaugeView(@Nullable Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.init(context, attrs);
    }

    public GaugeView(@Nullable Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.init(context, attrs);
    }

}