package cn.berfy.sdk.mvpbase.view;

/*
 * Copyright (C) 2013 Muthuramakrishnan <siriscac@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.*;
import android.support.v7.widget.AppCompatButton;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.view.ViewHelper;

import cn.berfy.sdk.mvpbase.R;
import cn.berfy.sdk.mvpbase.util.LogF;

@SuppressLint("ClickableViewAccessibility")
public class RippleView extends AppCompatButton {

    private final String TAG = "RippleView";
    private float mDownX;
    private float mDownY;
    private float mAlphaFactor;
    private float mDensity;
    private float mRadius;
    private float mMaxRadius;

    private int mRippleColor;
    private boolean mIsAnimating = false;
    private boolean mHover = true;

    private RadialGradient mRadialGradient;
    private Paint mPaint;
    private ObjectAnimator mRadiusAnimator;
    private OnRippleStateListener mOnRippleStateListener;

    public RippleView(Context context) {
        this(context, null);
    }

    public RippleView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RippleView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.RippleView);
        mRippleColor = a.getColor(R.styleable.RippleView_rippleColor,
                mRippleColor);
        mAlphaFactor = a.getFloat(R.styleable.RippleView_alphaFactor,
                mAlphaFactor);
        mHover = a.getBoolean(R.styleable.RippleView_hover, mHover);
        a.recycle();
    }

    public void init() {
        mDensity = getContext().getResources().getDisplayMetrics().density;
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setAlpha(255);
        setRippleColor(Color.BLACK, 1f);
    }

    private int dp(int dp) {
        return (int) (dp * mDensity + 0.5f);
    }

    public void setOnRippleStateListener(OnRippleStateListener onRippleStateListener) {
        mOnRippleStateListener = onRippleStateListener;
    }

    public void setRippleColor(int rippleColor, float alphaFactor) {
        mRippleColor = rippleColor;
        mAlphaFactor = alphaFactor;
    }

    public void setHover(boolean enabled) {
        mHover = enabled;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mMaxRadius = (float) Math.sqrt(w * w + h * h);
    }

    private boolean mAnimationIsCancel;
    private Rect mRect;

    @Override
    public boolean onTouchEvent(final MotionEvent event) {
        Log.d("TouchEvent", String.valueOf(event.getActionMasked()));
        Log.d("mIsAnimating", String.valueOf(mIsAnimating));
        Log.d("mAnimationIsCancel", String.valueOf(mAnimationIsCancel));
        boolean superResult = super.onTouchEvent(event);
        if (event.getActionMasked() == MotionEvent.ACTION_DOWN
                && this.isEnabled() && mHover) {
            mRect = new Rect(getLeft(), getTop(), getRight(), getBottom());
            mAnimationIsCancel = false;
            mDownX = event.getX();
            mDownY = event.getY();
            if (mIsAnimating && null != mRadiusAnimator) {
                mRadiusAnimator.cancel();
            }
            mRadiusAnimator = ObjectAnimator.ofFloat(this, "radius", 0, dp(50))
                    .setDuration(300);
            mRadiusAnimator
                    .setInterpolator(new AccelerateDecelerateInterpolator());
            mRadiusAnimator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {
                    mIsAnimating = true;
                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    setRadius(dp(50));
                    ViewHelper.setAlpha(RippleView.this, 1);
                    mIsAnimating = false;
                }

                @Override
                public void onAnimationCancel(Animator animator) {

                }

                @Override
                public void onAnimationRepeat(Animator animator) {

                }
            });
            mRadiusAnimator.start();
            if (!superResult) {
                return true;
            }
        } else if (event.getActionMasked() == MotionEvent.ACTION_MOVE
                && this.isEnabled() && mHover) {
            mDownX = event.getX();
            mDownY = event.getY();

            // Cancel the ripple animation when moved outside
            if (!mIsAnimating) {
                if (mAnimationIsCancel = !mRect.contains(
                        getLeft() + (int) event.getX(),
                        getTop() + (int) event.getY())) {
                    setRadius(0);
                } else {
                    setRadius(dp(50));
                }
            }
            if (!superResult) {
                return true;
            }
        } else if ((event.getActionMasked() == MotionEvent.ACTION_UP||event.getActionMasked() == MotionEvent.ACTION_CANCEL)
                && !mAnimationIsCancel && this.isEnabled()) {
            LogF.d(TAG, "事件" + event.getActionMasked());
            int action = event.getActionMasked();
            mDownX = event.getX();
            mDownY = event.getY();

            final float tempRadius = (float) Math.sqrt(mDownX * mDownX + mDownY
                    * mDownY);
            float targetRadius = Math.max(tempRadius, mMaxRadius);

            if (mIsAnimating && null != mRadiusAnimator) {
                mRadiusAnimator.cancel();
            }
            mRadiusAnimator = ObjectAnimator.ofFloat(this, "radius", dp(50),
                    targetRadius);
            if (action == MotionEvent.ACTION_CANCEL) {
                mRadiusAnimator.setDuration(1);
            }else{
                mRadiusAnimator.setDuration(300);
            }
            mRadiusAnimator
                    .setInterpolator(new AccelerateDecelerateInterpolator());
            mRadiusAnimator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {
                    mIsAnimating = true;
                    if (null != mOnRippleStateListener) {
                        LogF.d(TAG, "startRipple");
                        mOnRippleStateListener.startRipple(RippleView.this);
                    }
                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    setRadius(0);
                    ViewHelper.setAlpha(RippleView.this, 1);
                    mIsAnimating = false;
                    LogF.d(TAG, "事件" + action);
                    if (action == MotionEvent.ACTION_CANCEL) {
                        if (null != mOnRippleStateListener) {
                            LogF.d(TAG, "cancel");
                            mOnRippleStateListener.cancel(RippleView.this);
                        }
                    } else {
                        if (null != mOnRippleStateListener) {
                            LogF.d(TAG, "finishRipple");
                            mOnRippleStateListener.finishRipple(RippleView.this);
                        }
                    }
                }

                @Override
                public void onAnimationCancel(Animator animator) {
                    if (null != mOnRippleStateListener) {
                        LogF.d(TAG, "cancel");
                        mOnRippleStateListener.cancel(RippleView.this);
                    }
                }

                @Override
                public void onAnimationRepeat(Animator animator) {

                }
            });
            mRadiusAnimator.start();
            if (!superResult) {
                return true;
            }
        }
        return superResult;
    }

    public int adjustAlpha(int color, float factor) {
        int alpha = Math.round(Color.alpha(color) * factor);
//        LogF.d(TAG, "色值" + color + "  透明度" + Color.alpha(color) + "  最终值" + alpha);
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        return Color.argb(alpha, red, green, blue);
    }

    public void setRadius(final float radius) {
        mRadius = radius;
        if (mRadius > 0) {
            mRadialGradient = new RadialGradient(mDownX, mDownY, mRadius,
                    adjustAlpha(mRippleColor, mAlphaFactor), mRippleColor,
                    Shader.TileMode.MIRROR);
            mPaint.setShader(mRadialGradient);
        }
        invalidate();
    }

    private Path mPath = new Path();

    @Override
    protected void onDraw(final Canvas canvas) {

        if (isInEditMode()) {
            return;
        }

        canvas.save();

        mPath.reset();
        mPath.addCircle(mDownX, mDownY, mRadius, Path.Direction.CW);

        canvas.clipPath(mPath);
        canvas.restore();
        canvas.drawCircle(mDownX, mDownY, mRadius, mPaint);
        super.onDraw(canvas);
    }

    public interface OnRippleStateListener {
        void startRipple(RippleView view);

        void finishRipple(RippleView view);

        void cancel(RippleView view);
    }

}
