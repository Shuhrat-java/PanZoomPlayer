package uzb.uz.PanZoomPlayer;

import android.app.Activity;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.MediaController;
import android.widget.Toast;
import uzb.uz.PanZoomPlayer.pan.zoom.ZoomableTextureView;

/**
 * Class for playing local videos using custom texture view which has pan and zoom features
 *
 * @author Shuhrat
 */
public class Player extends Activity implements MediaController.MediaPlayerControl {

    private DisplayMetrics displayMetrics;

    private MediaController mediacontroller;

    private MediaPlayer mediaPlayer;

    private ZoomableTextureView videoView;

    private int displayWidth;

    private int displayHeight;

    private Surface surface;

    private FrameLayout topView;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);

        topView = (FrameLayout) findViewById(R.id.top_view);

        topView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                return videoView.onTouch(view, motionEvent);
            }
        });

        displayMetrics = new DisplayMetrics();

        //------------------- Get display fileSize  ---------------------------------
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        displayWidth = displayMetrics.widthPixels;

        displayHeight = displayMetrics.heightPixels;

        videoView = (ZoomableTextureView) findViewById(R.id.mp4view);

        videoView.setSurfaceTextureListener(textureListener);

        videoView.setDisplayMetrics(displayWidth, displayHeight);

    }

    private TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {

            surface = new Surface(surfaceTexture);

            if (mediaPlayer == null) {
                prepareVideoView();
            }

        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int width, int height) {
            setAspectRatio();
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
        }
    };

    private void setOnPrepare() {

        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

            // Close the progress bar and play the video
            public void onPrepared(MediaPlayer mp) {

                try {

                    mediaPlayer.start();

                    mediacontroller.show();

                    mediacontroller.setEnabled(true);

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            setAspectRatio();
                        }
                    }, 2000);

                } catch (Exception xc) {
                    xc.printStackTrace();
                }
            }
        });
    }

    //prepare video
    private void prepareVideoView() {

        try {
            // Start the MediaController

            mediaPlayer = new MediaPlayer();

            mediacontroller = new MediaController(Player.this);

            mediacontroller.setAnchorView(videoView);

            videoView.setMediaController(mediacontroller);

            mediacontroller.setMediaPlayer(this);

            Uri video = Uri.parse(Environment.getExternalStorageDirectory() + "/video.mp4");

            mediaPlayer.setDataSource(Player.this, video);

            mediaPlayer.setOnVideoSizeChangedListener(new MediaPlayer.OnVideoSizeChangedListener() {
                @Override
                public void onVideoSizeChanged(MediaPlayer mediaPlayer, int width, int height) {

                    setAspectRatio();

                }
            });

            if (surface != null) {
                mediaPlayer.setSurface(surface);
            }

            mediaPlayer.prepareAsync();

        } catch (Exception e) {
            e.printStackTrace();
        }


        mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {

                Toast.makeText(Player.this, "Can not read file", Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        setOnPrepare();

        setOnCompletion();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        mediaPlayer.release();
        mediaPlayer = null;
    }

    public final void setAspectRatio() {

        final int videoWidth = mediaPlayer.getVideoWidth();

        final int videoHeight = mediaPlayer.getVideoHeight();

        Log.d("set aspect ratio ", ": width:" + videoWidth + ", height:" + videoHeight);

        // calculate aspect ratio of video
        float aspectRatio = (float) videoWidth / (float) videoHeight;

        int surfaceW, surfaceH;

        if (displayHeight * aspectRatio > displayWidth) {
            surfaceW = displayWidth;
            surfaceH = (int) (displayWidth / aspectRatio);
        } else {
            surfaceH = displayHeight;
            surfaceW = (int) (displayHeight * aspectRatio);
        }
        //  >>1 equals to: /2
        final int x = (displayWidth - surfaceW) >> 1;
        final int y = (displayHeight - surfaceH) >> 1;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                videoView.layout(x, y, videoWidth + x, videoHeight + y);

            }
        });
    }

    private void setOnCompletion() {

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {

                Log.d("current position: " + mediaPlayer.getCurrentPosition(), "  duration: " + mediaPlayer.getDuration() + " oncompletion");
                finish();

            }
        });
    }

    @Override
    public void start() {
        mediaPlayer.start();
    }

    @Override
    public void pause() {
        mediaPlayer.pause();

    }

    @Override
    public int getDuration() {
        return mediaPlayer.getDuration();
    }

    @Override
    public int getCurrentPosition() {
        return mediaPlayer.getCurrentPosition();
    }

    @Override
    public void seekTo(int i) {
        mediaPlayer.seekTo(i);
    }

    @Override
    public boolean isPlaying() {
        return mediaPlayer.isPlaying();
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public boolean canPause() {
        return false;
    }

    @Override
    public boolean canSeekBackward() {
        return false;
    }

    @Override
    public boolean canSeekForward() {
        return false;
    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }
}
