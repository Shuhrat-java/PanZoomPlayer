package uzb.uz.PanZoomPlayer;

import android.content.Context;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.TextureView;
import android.widget.MediaController;

/**
 * This view is the changed implementation of PanZoomView class which is created by Bill Lahti
 *
 * Custom child of TextureView supports both zooming and panning.
 *
 * TODO view has some inaccuracy when zooming
 */

public class PanZoomTextureView extends TextureView {

    protected Context mContext;
    protected float mPosX;
    protected float mPosY;

    protected float mFocusX;    // these two focus variables are not needed
    protected float mFocusY;

    protected float mLastTouchX;
    protected float mLastTouchY;

    protected static final int INVALID_POINTER_ID = -1;

    // The ‘active pointer’ is the one currently moving our object.
    protected int mActivePointerId = INVALID_POINTER_ID;

    protected ScaleGestureDetector mScaleDetector;
    protected float mScaleFactor = 1.f;

    // The next three are set by calling supportsPan, supportsZoom, ...
    protected boolean mSupportsPan = true;
    protected boolean mSupportsZoom = true;
    private MediaController controller;


    /**
     */
    public PanZoomTextureView(Context context) {
        this(context, null, 0);
    }

    public PanZoomTextureView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PanZoomTextureView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        setupScaleDetector(context);
    }

    private void invalidateDraw() {
        Matrix ma = new Matrix();

        float x = 0, y = 0;

        x = mPosX;

        y = mPosY;

        if (mSupportsZoom || mSupportsPan) {

            if (mSupportsPan && mSupportsZoom) {

                if (mScaleDetector.isInProgress()) {

                    ma.setScale(mScaleFactor, mScaleFactor, mFocusX, mFocusY);

                    mPosX = (getWidth() - getWidth() * mScaleFactor) * (mFocusX / getWidth()) / 2;

                    mPosY = (getHeight() - getHeight() * mScaleFactor) * (mFocusY / getHeight()) / 2;

                    Log.d("Multitouch", "'in progress' mposx, mposy, focusX, focusY: " + mPosX + " " + mPosY + " " + mFocusX + " " + mFocusY);

                } else {

                    // Pinch zoom is not in progress. Just do translation
                    // of canvas at whatever the current scale is.
                    ma.setTranslate(x, y);

                    ma.postScale(mScaleFactor, mScaleFactor);

                    Log.d("Multitouch", "x, y , focusX, focusY , scalefactor : " + x + " " + y + " " + mFocusX + " " + mFocusY + " " + mScaleFactor);
                }
            }
        }

        setTransform(ma);
    }

    public void setMediaController(MediaController c) {
        this.controller = c;
    }

    /**
     * Handle touch and multitouch events so panning and zooming can be supported.
     */
    @Override
    public boolean onTouchEvent(MotionEvent ev) {

        // Let the ScaleGestureDetector inspect all events.
        mScaleDetector.onTouchEvent(ev);

        final int action = ev.getAction();

        switch (action & MotionEvent.ACTION_MASK) {

            case MotionEvent.ACTION_DOWN: {

                final float x = ev.getX();
                final float y = ev.getY();

                mLastTouchX = x;
                mLastTouchY = y;

                mActivePointerId = ev.getPointerId(0);

                Log.d("OnTouchDown:", "lastX, lastY: " + mLastTouchX + "  ,  " + mLastTouchY);

                if (ev.getPointerCount() == 1) {
                    if (controller.isShowing()) {

                        controller.hide();

                    } else {
                        controller.show(10000);
                    }
                }

                break;
            }

            case MotionEvent.ACTION_MOVE: {
                //register active pointer index
                final int pointerIndex = ev.findPointerIndex(mActivePointerId);

                // x and y values from active pointer
                final float x = ev.getX(pointerIndex);
                final float y = ev.getY(pointerIndex);

                final float dx = (x - mLastTouchX);

                final float dy = (y - mLastTouchY);

                // Only move if the view supports panning and
                // ScaleGestureDetector isn't processing a gesture.
                if (!mScaleDetector.isInProgress()) {

                    Log.d("OnTouch1:", "dX, dY: " + dx + "  ,  " + dy);

                    if (mPosX > 5) {
                        mPosX = 3;
                    }

                    if (mPosY > 5) {
                        mPosY = 3;
                    }

                    if (mPosX < ((-1) * (getWidth() - getWidth() / mScaleFactor) - 5)) {

                        mPosX = ((-1) * (getWidth() - getWidth() / mScaleFactor) - 2);
                    }
                    if (mPosY < ((-1) * (getHeight() - getHeight() / mScaleFactor) - 5)) {

                        mPosY = ((-1) * (getHeight() - getHeight() / mScaleFactor) - 2);

                    }
                    if ((mPosX + dx) > 5 && (mPosY + dy) > 5) {

                        mPosX = 3;
                        mPosY = 3;
                        Log.d("OnTouch1:", "mPosx, mPosy: " + mPosX + "  ,  " + mPosY);

                    } else if ((mPosY + dy) > 5 && (mPosX + dx) < 5 && (mPosX + dx) > ((-1) * (getWidth() - getWidth() / mScaleFactor) - 5)) {

                        mPosY = 3;

                        mPosX += dx;

                        Log.d("OnTouch2:", "mPosx, mPosy: " + mPosX + "  ,  " + mPosY);

                    } else {
                        mPosX += dx;
                        mPosY += dy;
                        Log.d("OnTouch8:", "mPosx, mPosy: " + mPosX + "  ,  " + mPosY);
                    }

                    invalidateDraw();
                }
                mLastTouchX = x;
                mLastTouchY = y;
                break;
            }

            case MotionEvent.ACTION_UP: {

                mActivePointerId = INVALID_POINTER_ID;

                break;
            }

            case MotionEvent.ACTION_CANCEL: {

                mActivePointerId = INVALID_POINTER_ID;

                break;
            }

            case MotionEvent.ACTION_POINTER_UP: {

                final int pointerIndex = (ev.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK)
                        >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;

                final int pointerId = ev.getPointerId(pointerIndex);

                if (pointerId == mActivePointerId) {

                    // This was our active pointer going up. Choose a new
                    // active pointer and adjust accordingly.
                    final int newPointerIndex = pointerIndex == 0 ? 1 : 0;

                    mLastTouchX = ev.getX(newPointerIndex);

                    mLastTouchY = ev.getY(newPointerIndex);

                    mActivePointerId = ev.getPointerId(newPointerIndex);

                }

                break;
            }
        }

        return true;
    }

    /**
     * This method sets up the scale detector object used by the view. It is called by the constructor.
     *
     * @return void
     */

    protected void setupScaleDetector(Context context) {
        // Create our ScaleGestureDetector
        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());

    }

    /**
     * ScaleListener
     */
    protected class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {

            mScaleFactor *= detector.getScaleFactor();
            Log.e("Multitouch", "-------scaling factor:" + mScaleFactor);
            // Don't let the object get too small or too large.
            mScaleFactor = Math.max(1.f, Math.min(mScaleFactor, 4));

            mFocusX = detector.getFocusX();
            mFocusY = detector.getFocusY();


            invalidateDraw();

            return true;
        }
    }
}
