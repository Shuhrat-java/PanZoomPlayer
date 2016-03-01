package uzb.uz.PanZoomPlayer.pan.zoom;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.TextureView;
import android.view.View;
import android.widget.MediaController;
import uzb.uz.PanZoomPlayer.pan.zoom.gestures.MoveGestureDetector;

/**
 * Created by shuhrat on 3/1/16.
 */
public class ZoomableTextureView extends TextureView implements View.OnTouchListener {

    private Matrix mMatrix;

    private ScaleGestureDetector mScaleDetector;

    private MoveGestureDetector mMoveDetector;

    private float mScaleFactor = 1.f;

    private float mFocusX = 0.f;

    private float mFocusY = 0.f;

    private MediaController controller;

    public ZoomableTextureView(Context context) {
        super(context);
        init(context);
    }

    public ZoomableTextureView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ZoomableTextureView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ZoomableTextureView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context) {

        mMatrix = new Matrix();

        // Setup Gesture Detectors
        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());

        mMoveDetector = new MoveGestureDetector(context, new MoveListener());

    }

    public void setDisplayMetrics(int width, int height) {

        mFocusX = width / 2;

        mFocusY = height / 2;
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {

        if (motionEvent.getAction()==MotionEvent.ACTION_DOWN){

            if (motionEvent.getPointerCount() == 1) {

                if (controller.isShowing()) {

                    controller.hide();

                } else {

                    controller.show(10000);
                }
            }
        }

        mScaleDetector.onTouchEvent(motionEvent);

        mMoveDetector.onTouchEvent(motionEvent);

        float scaledImageCenterX = (getWidth() * mScaleFactor) / 2;

        float scaledImageCenterY = (getHeight() * mScaleFactor) / 2;

        mMatrix.reset();

        mMatrix.postScale(mScaleFactor, mScaleFactor);

        float dx = mFocusX - scaledImageCenterX;

        float dy = mFocusY - scaledImageCenterY;

        if (dx < ((1 - mScaleFactor) * getWidth())) {

            dx = (1 - mScaleFactor) * getWidth();

            mFocusX = dx + scaledImageCenterX;

        }

        if (dy < ((1 - mScaleFactor) * getHeight())) {

            dy = (1 - mScaleFactor) * getHeight();

            mFocusY = dy + scaledImageCenterY;

        }
        if (dx > 0) {

            dx = 0;

            mFocusX = dx + scaledImageCenterX;
        }

        if (dy > 0) {

            dy = 0;

            mFocusY = dy + scaledImageCenterY;
        }

        mMatrix.postTranslate(dx, dy);

        setTransform(mMatrix);

        setAlpha(1);

        return true; // indicate event was handled

    }

    public void setMediaController(MediaController mediacontroller) {
        this.controller = mediacontroller;
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {

            mScaleFactor *= detector.getScaleFactor(); // scale change since previous event

            // Don't let the object get too small or too large.
            mScaleFactor = Math.max(1.f, Math.min(mScaleFactor, 4.0f));

            return true;
        }
    }

    private class MoveListener extends MoveGestureDetector.SimpleOnMoveGestureListener {
        @Override
        public boolean onMove(MoveGestureDetector detector) {

            PointF d = detector.getFocusDelta();

            mFocusX += d.x;

            mFocusY += d.y;

            return true;
        }
    }
}
