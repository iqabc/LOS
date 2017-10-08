package com.example.irc.los;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Movie;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Created by IRC2016 on 2017/10/05.
 */

public class DifficultyOption extends Activity {

    private static final int EASY   = 0;
    private static final int NORMAL = 1;
    private static final int HARD   = 2;

    private LinearLayout linears[] = new LinearLayout[3];

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_difficulty);
        RelativeLayout diffR[] = new RelativeLayout[3];
        diffR[EASY]   = (RelativeLayout)findViewById(R.id.easy);
        diffR[NORMAL] = (RelativeLayout)findViewById(R.id.normal);
        diffR[HARD]   = (RelativeLayout)findViewById(R.id.hard);
        linears[0] = (LinearLayout)diffR[0].getChildAt(0);
        linears[1] = (LinearLayout)diffR[1].getChildAt(0);
        linears[2] = (LinearLayout)diffR[2].getChildAt(0);
        for (int i = 0;i < diffR.length;i++) {
            final int index = i;
            diffR[i].setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    int c;
                    switch (motionEvent.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            c = Color.argb(128,0,0,0);
                            view.getBackground().setColorFilter(c, PorterDuff.Mode.DARKEN);
                            for (int i = 0;i < 2;i++) {
                                ((TextView)linears[index].getChildAt(i)).setTextColor(Color.argb(128,0xff,0xff,0xff));
                            }
                            //((TextView)(((RelativeLayout)view).getChildAt(0))).setTextColor(Color.argb(255,128,128,128));
                            break;
                        case MotionEvent.ACTION_MOVE:
                            break;
                        case MotionEvent.ACTION_UP:
                            c = Color.argb(0,0,0,0);
                            view.getBackground().setColorFilter(c, PorterDuff.Mode.DARKEN);
                            for (int i = 0;i < 2;i++) {
                                ((TextView)linears[index].getChildAt(i)).setTextColor(Color.argb(255,0xff,0xff,0xff));
                            }
                            //((TextView)(((RelativeLayout)view).getChildAt(0))).setTextColor(Color.WHITE);
                            break;
                    }
                    return false;
                }
            });
        }
    }

    public void onClick_kantan(View view){
        getDialog(EASY).show();
    }
    public void onClick_hutuu(View view){
        getDialog(NORMAL).show();
    }
    public void onClick_muzukasii(View view){
        getDialog(HARD).show();
    }

    public android.app.AlertDialog getDialog(int diff) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setCancelable(false);
        switch (diff) {
            case EASY:
                Play.sonarLimitMax = 5;
                Play.mapSize = 11;
                Play.enemyNum = 3;
                FieldView.setDrawingQuality(5);
                if (Build.VERSION.SDK_INT < 21) {
                    FieldView.setDrawingQuality(3);
                }
                FieldView.setFocusableBlock(7);
                Scorpion.n = 3;
                builder.setTitle("「 かんたん 」に挑戦しますか？");
                break;
            case NORMAL:
                Play.sonarLimitMax = 3;
                Play.mapSize = 13;
                Play.enemyNum = 4;
                FieldView.setDrawingQuality(3);
                if (Build.VERSION.SDK_INT < 21) {
                    FieldView.setDrawingQuality(2);
                }
                FieldView.setFocusableBlock(5);
                Scorpion.n = 4;
                builder.setTitle("「 ふつう 」に挑戦しますか？");
                break;
            case HARD:
                Play.sonarLimitMax = 1;
                Play.mapSize = 17;
                Play.enemyNum = 9;
                FieldView.setDrawingQuality(2);
                if (Build.VERSION.SDK_INT < 21) {
                    FieldView.setDrawingQuality(1);
                }
                FieldView.setFocusableBlock(5);
                Scorpion.n = 6;
                builder.setTitle(" 「 むずかしい 」に挑戦しますか？ ");
                break;
        }
        builder.setPositiveButton("はい", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent(DifficultyOption.this, Play.class));
            }
        })
        .setNegativeButton("いいえ", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //
            }
        });
        return builder.create();
    }
}
