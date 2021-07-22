package com.chitranjank.apps.socialchats.Fragments.Options;

import android.app.ProgressDialog;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.chitranjank.apps.socialchats.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;

public class MusicActivity extends AppCompatActivity {
    private SeekBar seekBar;
    private TextView tvCurrentDur, tvTotalDur;
    private CircleImageView btnPlay;

    static MediaPlayer mediaPlayer = new MediaPlayer();

    ProgressDialog pd;

    Handler handler = new Handler();
    Runnable runnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music);

        seekBar = findViewById(R.id.seek_bar);
        tvCurrentDur = findViewById(R.id.durationCurrent);
        tvTotalDur = findViewById(R.id.durationTotal);
        btnPlay = findViewById(R.id.p_p);
        TextView textViewTitle = findViewById(R.id.song_title);
        CircleImageView downLoadBtn = findViewById(R.id.save_music);

        Intent intent = getIntent();
        String url = intent.getStringExtra("MUSIC");
        String title = intent.getStringExtra("Title");
        textViewTitle.setText(title);

        pd = new ProgressDialog(this);
        pd.setMessage("Loading....");
        pd.show();

        downLoadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!url.trim().isEmpty()) {
                    downloadPdfFile(url, title);
                }
            }
        });

        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mediaPlayer.isPlaying()) {
                    mediaPlayer.start();
                    seekBar.setMax(mediaPlayer.getDuration());
                    setSeekBarUpdate();
                    btnPlay.setImageResource(R.drawable.ic_baseline_pause_circle_24);
                } else {
                    mediaPlayer.pause();
                    seekBar.setMax(mediaPlayer.getDuration());
                    setSeekBarUpdate();
                    btnPlay.setImageResource(R.drawable.ic_baseline_play_circle_24);
                }
            }
        });

        if (!url.trim().isEmpty()) {
            create_media(url);
        }
    }

    private void create_media(String mp3File) {
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            mediaPlayer.setDataSource(mp3File);
            mediaPlayer.prepare();

            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    pd.dismiss();
                    mediaPlayer.start();
                    seekBar.setMax(mediaPlayer.getDuration());
                    setSeekBarUpdate();
                    btnPlay.setImageResource(R.drawable.ic_baseline_pause_circle_24);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void downloadPdfFile(String url, String title) {

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReferenceFromUrl(url);

        ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Downloading....");
        pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pd.setCancelable(false);
        pd.setMax(100);
        pd.show();

        final File rootPath = new File(Environment.getExternalStorageDirectory(), "ChatNow");

        final File localFile = new File(rootPath, title);

        storageRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {

                if (localFile.canRead()) {
                    pd.dismiss();
                    Toast.makeText(MusicActivity.this, "Download completed", Toast.LENGTH_LONG).show();
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                pd.dismiss();
                Toast.makeText(MusicActivity.this, "Download failed", Toast.LENGTH_LONG).show();
            }
        }).addOnProgressListener(new OnProgressListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull FileDownloadTask.TaskSnapshot snapshot) {
                int progress = (int) (100*snapshot.getBytesTransferred()/snapshot.getTotalByteCount());
                pd.setProgress(progress);
            }
        });
    }

    @Override
    public void onBackPressed() {
        mediaPlayer.stop();
        finish();
        super.onBackPressed();
    }

    public void setSeekBarUpdate() {
        String maxDuration = createTimer(mediaPlayer.getDuration());
        tvTotalDur.setText(maxDuration);

        final int currentPos = mediaPlayer.getCurrentPosition();
        String string = createTimer(currentPos);
        tvCurrentDur.setText(string);

        if (mediaPlayer.isPlaying()) {
            seekBar.setProgress(currentPos);
            runnable = new Runnable() {
                @Override
                public void run() {
                    setSeekBarUpdate();
                }
            };
            handler.postDelayed(runnable, 1000);
        }

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mediaPlayer.seekTo(seekBar.getProgress());
                setSeekBarUpdate();
            }
        });
    }

    public String createTimer(int duration) {
        String timerDuration = "";
        int min = duration / 1000 / 60;
        int sec = duration / 1000 % 60;

        timerDuration += min + ":";

        if (sec < 10) {
            timerDuration += "0";
        }

        timerDuration += sec;

        return timerDuration;
    }
}