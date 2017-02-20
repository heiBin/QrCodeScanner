package com.duoyi.provider.qrscan.activity;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.duoyi.provider.qrscan.R;
import com.duoyi.provider.qrscan.camera.CameraManager;
import com.duoyi.qrdecode.BarcodeFormat;
import com.duoyi.provider.qrscan.decoding.CaptureActivityHandler;
import com.duoyi.provider.qrscan.decoding.InactivityTimer;
import com.duoyi.provider.qrscan.view.ViewfinderView;
import com.duoyi.qrdecode.DecodeEntry;

import java.io.IOException;

public class MipcaActivityCapture extends Activity implements Callback {

    private static int RESULT_LOAD_IMAGE = 1000;
    private CaptureActivityHandler handler;
    private ViewfinderView viewfinderView;
    private boolean hasSurface;
    private String characterSet;
    private InactivityTimer inactivityTimer;
    private MediaPlayer mediaPlayer;
    private boolean playBeep;
    private static final float BEEP_VOLUME = 0.10f;
    private boolean vibrate;
    private Button languageBtn;
    private Dialog dialog;
    private SurfaceHolder surfaceHolder;
    private BarcodeFormat barcodeFormat;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture);
        CameraManager.init(getApplication());
        viewfinderView = (ViewfinderView) findViewById(R.id.viewfinder_view);
        findViewById(R.id.decode_file).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                Intent i = new Intent(
                        Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                startActivityForResult(i, RESULT_LOAD_IMAGE);
            }
        });
        languageBtn = (Button) findViewById(R.id.decode_language);
        languageBtn.setText(getResources().getString(R.string.language_text) +
                "C++(Zxing,Zbar)");
        hasSurface = false;
        inactivityTimer = new InactivityTimer(this);
        barcodeFormat = new BarcodeFormat();
        barcodeFormat.add(BarcodeFormat.BARCODE);
        barcodeFormat.add(BarcodeFormat.QRCODE);

    }

    @Override
    protected void onResume() {
        super.onResume();
        SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
        surfaceHolder = surfaceView.getHolder();
        if (hasSurface) {
            initCamera(surfaceHolder);
        } else {
            surfaceHolder.addCallback(this);
            surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }
        characterSet = null;

        playBeep = true;
        AudioManager audioService = (AudioManager) getSystemService(AUDIO_SERVICE);
        if (audioService.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
            playBeep = false;
        }
        initBeepSound();
        vibrate = true;

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (handler != null) {
            handler.quitSynchronously();
            handler = null;
        }
        CameraManager.get().closeDriver();
    }

    @Override
    protected void onDestroy() {
        inactivityTimer.shutdown();
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};

            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            new AsyncTask<String, Void, String>() {

                @Override
                protected String doInBackground(String... arg0) {
                    // TODO Auto-generated method stub
                    return DecodeEntry.decodeFromFile(arg0[0], barcodeFormat);
                }

                @Override
                protected void onPreExecute() {
                    dialog = ProgressDialog.show(MipcaActivityCapture.this, null, "Decoding...");
                    CameraManager.get().closeDriver();
                }

                @Override
                protected void onPostExecute(String result) {
                    initCamera(surfaceHolder);
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                    Intent resultIntent = new Intent(MipcaActivityCapture.this, ResultActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("result", result);
                    resultIntent.putExtras(bundle);
                    MipcaActivityCapture.this.startActivity(resultIntent);
                }

            }.execute(picturePath);

            cursor.close();

        }
    }

    /**
     * @param result
     */
    public void handleDecode(String result) {
        // inactivityTimer.onActivity();
//		playBeepSoundAndVibrate();
        Toast.makeText(MipcaActivityCapture.this, "" + result,
                Toast.LENGTH_SHORT).show();
        String resultString = result;
        if (resultString.equals("")) {
            Toast.makeText(MipcaActivityCapture.this, "Scan failed!",
                    Toast.LENGTH_SHORT).show();
        } else {
            Intent resultIntent = new Intent(MipcaActivityCapture.this, ResultActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString("result", resultString);
            resultIntent.putExtras(bundle);
            this.startActivity(resultIntent);
        }
    }

    private void initCamera(SurfaceHolder surfaceHolder) {
        try {
            CameraManager.get().openDriver(surfaceHolder);
        } catch (IOException ioe) {
            return;
        } catch (RuntimeException e) {
            return;
        }
        if (handler == null) {
            handler = new CaptureActivityHandler(this, barcodeFormat);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (!hasSurface) {
            hasSurface = true;
            initCamera(holder);
        }

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        hasSurface = false;

    }

    public ViewfinderView getViewfinderView() {
        return viewfinderView;
    }

    public Handler getHandler() {
        return handler;
    }

    public void drawViewfinder() {
        viewfinderView.drawViewfinder();

    }

    private void initBeepSound() {
        if (playBeep && mediaPlayer == null) {
            setVolumeControlStream(AudioManager.STREAM_MUSIC);
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setOnCompletionListener(beepListener);

            AssetFileDescriptor file = getResources().openRawResourceFd(
                    R.raw.beep);
            try {
                mediaPlayer.setDataSource(file.getFileDescriptor(),
                        file.getStartOffset(), file.getLength());
                file.close();
                mediaPlayer.setVolume(BEEP_VOLUME, BEEP_VOLUME);
                mediaPlayer.prepare();
            } catch (IOException e) {
                mediaPlayer = null;
            }
        }
    }

    private static final long VIBRATE_DURATION = 200L;

    private void playBeepSoundAndVibrate() {
        if (playBeep && mediaPlayer != null) {
            mediaPlayer.start();
        }
        if (vibrate) {
            Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            vibrator.vibrate(VIBRATE_DURATION);
        }
    }

    /**
     * When the beep has finished playing, rewind to queue up another one.
     */
    private final OnCompletionListener beepListener = new OnCompletionListener() {
        public void onCompletion(MediaPlayer mediaPlayer) {
            mediaPlayer.seekTo(0);
        }
    };

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        switch (event.getKeyCode()) {
            case KeyEvent.KEYCODE_FOCUS:
            case KeyEvent.KEYCODE_CAMERA:
                return true;
            case KeyEvent.KEYCODE_VOLUME_UP:
                CameraManager.get().zoomIn();
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                CameraManager.get().zoomOut();
                return true;
        }
        return super.dispatchKeyEvent(event);
    }


}