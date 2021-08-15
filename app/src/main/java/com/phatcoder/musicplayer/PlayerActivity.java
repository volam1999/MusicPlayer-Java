package com.phatcoder.musicplayer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class PlayerActivity extends AppCompatActivity {
    Button btnPlay, btnBack, btnNext, btnReplay, btnPlaylist;
    TextView tvSongName, tvElapsedTime, tvRemainingTime;
    SeekBar positionBar;
    CircleImageView imgSong;
    ArrayList<String> mySongURLs;
    ArrayList<String> mySongNames;
    LinearLayout linearLayout;

    static MediaPlayer mediaPlayer;
    int totalTime;
    int position;
    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        setWidget();
        setEvent();
        intent = getIntent();
        Bundle bundle = intent.getExtras();
        mySongURLs = (ArrayList<String>) bundle.getSerializable(MainActivity.SONG_URL);
        mySongNames = (ArrayList<String>) bundle.getSerializable(MainActivity.SONG_LIST);
        //RotateAnimation rotate = new RotateAnimation(0, 180, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        //rotate.setDuration(Animation.INFINITE);
        //rotate.setInterpolator(new LinearInterpolator());
       // imgSong.startAnimation(rotate);
        position = bundle.getInt(MainActivity.VT_I);
        play();
        getSupportActionBar().setTitle("Now Playing");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        //Thread update position bar && timer
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (mediaPlayer != null) {
                    try {
                        Message message = new Message();
                        message.what = mediaPlayer.getCurrentPosition();
                        handler.sendMessage(message);
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            }
        }).start();
    }

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            int currentPosition = msg.what;
            positionBar.setProgress(currentPosition);

            //Update Labels
            String elapsedTime = createTimeLabel(currentPosition);
            tvElapsedTime.setText(elapsedTime);

            String remainingTime = createTimeLabel(totalTime - currentPosition);
            tvRemainingTime.setText(remainingTime);

        }
    };

    private String createTimeLabel(int time) {
        int min = time / 1000 / 60;
        int sec = time / 1000 % 60;
        String timeLabel = min + ":";
        if (sec < 10) {
            timeLabel += "0";
        }
        timeLabel += sec;
        return timeLabel;
    }

    @SuppressLint("NewApi")
    private void play() {
        String URL = mySongURLs.get(position);
        tvSongName.setText(mySongNames.get(position));
        Uri uri = Uri.parse(URL);
        Bitmap bitmap = GetImage(URL);
        if (bitmap == null) {
            imgSong.setImageResource(R.drawable.cover_art);
            linearLayout.setBackgroundResource(0);
        } else {
            imgSong.setImageBitmap(bitmap);
            BitmapDrawable da = new BitmapDrawable(getResources(), bitmap);
            linearLayout.setBackgroundDrawable(da);
        }
        positionBar.getProgressDrawable().setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.MULTIPLY);
        positionBar.getThumb().setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.SRC_IN);
        //media player
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(this, uri);
            mediaPlayer.seekTo(0);
            mediaPlayer.setVolume(1.0f, 1.0f);
            totalTime = mediaPlayer.getDuration();
            mediaPlayer.start();
        } else {
            mediaPlayer.stop();
            mediaPlayer = MediaPlayer.create(this, uri);
            mediaPlayer.seekTo(0);
            totalTime = mediaPlayer.getDuration();
            mediaPlayer.start();
        }

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                position = ((position+1)%mySongNames.size());
                play();
            }
        });

        //position bar
        positionBar.setMax(totalTime);
        positionBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mediaPlayer.seekTo(progress);
                    positionBar.setProgress(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        //text view Song name
        tvSongName.setSelected(true);
    }

    public void setWidget() {
        btnPlay = findViewById(R.id.btn_pause);
        btnBack = findViewById(R.id.btn_back);
        btnNext = findViewById(R.id.btn_next);
        btnReplay = findViewById(R.id.btn_replay);
        btnPlaylist = findViewById(R.id.btn_playlist);
        tvSongName = findViewById(R.id.tv_song_name);
        positionBar = findViewById(R.id.sb_position);
        tvElapsedTime = findViewById(R.id.tv_elapsed_time);
        tvRemainingTime = findViewById(R.id.tv_remaining_time);
        imgSong = findViewById(R.id.img_song);
        linearLayout = findViewById(R.id.background_image);
    }

    public void setEvent() {
        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mediaPlayer.isPlaying()) {
                    //Stopping
                    mediaPlayer.start();
                    btnPlay.setBackgroundResource(R.drawable.ic_pause);
                } else {
                    mediaPlayer.pause();
                    btnPlay.setBackgroundResource(R.drawable.ic_play);
                }
            }
        });

        btnReplay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mediaPlayer.isLooping()) {
                    mediaPlayer.setLooping(false);
                    btnReplay.setBackgroundResource(R.drawable.ic_replay_black_24dp);
                } else {
                    mediaPlayer.setLooping(true);
                    btnReplay.setBackgroundResource(R.drawable.ic_replay_blue_24dp);
                }
            }
        });

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                position = ((position+1)%mySongNames.size());
                play();
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                position = ((position-1)<0)?(mySongNames.size()-1):(position-1);
                play();
            }
        });
        btnPlaylist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    Bitmap GetImage(String filepath)              //filepath is path of music file
    {
        Bitmap image;

        MediaMetadataRetriever mData = new MediaMetadataRetriever();
        mData.setDataSource(filepath);
        try {
            byte art[] = mData.getEmbeddedPicture();
            image = BitmapFactory.decodeByteArray(art, 0, art.length);
        } catch (Exception e) {
            image = null;
        }
        return image;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }
}
