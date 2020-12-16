package com.example.mediaplayer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.IOException;

public class AllTracks extends AppCompatActivity {

    ListView lv_tracks;
    SeekBar seekBar;
    TextView name;
    TextView author;
    Song song;
    Handler handler;
    Runnable runnable;
    MediaPlayer mp;
    ImageButton play;
    Integer current;
    String last;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_tracks);

        lv_tracks = (ListView) findViewById(R.id.tracks);
        seekBar = findViewById(R.id.seek_bar);
        name = (TextView) findViewById(R.id.name);
        author = (TextView) findViewById(R.id.author);
        play = (ImageButton) findViewById(R.id.play);

        SharedPreferences sp = getSharedPreferences("track", MODE_PRIVATE);
        last = sp.getString("last", "");

        seekBar.setEnabled(false);

        if (!last.equals("") && last != null) {
            current = Integer.parseInt(last.split(" ")[0]);
            Integer position = Integer.parseInt(last.split(" ")[1]);
            Integer max = Integer.parseInt(last.split(" ")[2]);

            song = Tracks.songs.get(current);

            name.setText(song.getName());
            author.setText(song.getPerformer());

            seekBar.setMax(max);
            seekBar.setProgress(position);

            mp = new MediaPlayer();
            mp = start(mp, song.getUrl());
            mp.pause();
        }

        if (mp == null) {
            play.setImageDrawable(getResources().getDrawable(android.R.drawable.ic_media_play));
        } else if (!mp.isPlaying()) {
            play.setImageDrawable(getResources().getDrawable(android.R.drawable.ic_media_play));
        } else {
            play.setImageDrawable(getResources().getDrawable(android.R.drawable.ic_media_pause));
        }

        handler = new Handler();

        SongAdapter adapter = new SongAdapter(this, R.layout.song_item, Tracks.songs);
        lv_tracks.setAdapter(adapter);

        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mp == null) {
                    return;
                } else {
                    if (mp.isPlaying()) {
                        mp.pause();
                    } else {
                        mp.seekTo(seekBar.getProgress());
                        mp.start();
                    }
                }

                if (mp.isPlaying()) {
                    play.setImageDrawable(getResources().getDrawable(android.R.drawable.ic_media_pause));
                    seekBar.setEnabled(true);
                    playCycle();
                } else {
                    play.setImageDrawable(getResources().getDrawable(android.R.drawable.ic_media_play));
                }
            }
        });

        lv_tracks.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                song = Tracks.songs.get(position);

                current = position;

                if (mp != null && mp.isPlaying()) {
                    mp.stop();
                }

                mp = new MediaPlayer();

                play.setImageDrawable(getResources().getDrawable(android.R.drawable.ic_media_pause));

                mp = start(mp, song.getUrl());

                seekBar.setEnabled(true);

                seekBar.setMax(mp.getDuration());

                name.setText(song.getName());
                author.setText(song.getPerformer());

                playCycle();
            }
        });


        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mp.seekTo(seekBar.getProgress());
            }
        });
    }

    public MediaPlayer start(MediaPlayer mp, String url) {
        mp.setAudioAttributes(
                new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
        );
        try {
            mp.setDataSource(url);
            mp.prepare();
        } catch (
                IOException e) {
            e.printStackTrace();
        }
        mp.start();
        return mp;
    }

    public void playCycle() {
        if (mp.isPlaying()) {
            seekBar.setProgress(mp.getCurrentPosition());
            runnable = new Runnable() {
                @Override
                public void run() {
                    seekBar.setProgress(mp.getCurrentPosition());
                    playCycle();
                }
            };
        }
        handler.postDelayed(runnable, 100);
    }

    private void beforeFinish() {
        if (mp != null) {
            SharedPreferences sp = getSharedPreferences("track", MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("last", current + " " + seekBar.getProgress() + " " + mp.getDuration());
            editor.apply();
            mp.stop();
            mp.release();
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    protected void onStop() {
        handler.removeCallbacks(runnable);
        beforeFinish();
        finish();
        super.onStop();
    }
}
