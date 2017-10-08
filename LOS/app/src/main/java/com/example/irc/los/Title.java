package com.example.irc.los;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;

import java.util.ArrayList;

public class Title extends Activity {

    private int left = -1;
    ImageView startButton;
    Handler handler = new Handler();
    Runnable r = new Runnable() {
        @Override
        public void run() {
            if (left == -1) {
                left = startButton.getLeft();
                startButton.offsetLeftAndRight(800);
                startButton.setVisibility(View.VISIBLE);
            }
            int dif = startButton.getLeft()-left;
            if (dif< 10) {
                startButton.offsetLeftAndRight(-dif);
            }
            else {
                startButton.offsetLeftAndRight(dif/-7);
                handler.postDelayed(this,10);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_title);
        FieldView.bmps = new Bitmap[8];
        FieldView.bmps[FieldView.BLOCK] = BitmapFactory.decodeResource(getResources(),R.drawable.block);
        FieldView.bmps[FieldView.LAND] = BitmapFactory.decodeResource(getResources(),R.drawable.land);
        FieldView.bmps[FieldView.SNAKE] = BitmapFactory.decodeResource(getResources(),R.drawable.snake);
        FieldView.bmps[FieldView.SCORPION] = BitmapFactory.decodeResource(getResources(),R.drawable.scorpion);
        FieldView.bmps[FieldView.GOAL] = BitmapFactory.decodeResource(getResources(),R.drawable.goal);
        FieldView.bmps[5] = BitmapFactory.decodeResource(getResources(),R.drawable.scorpion_down1);
        FieldView.bmps[6] = BitmapFactory.decodeResource(getResources(),R.drawable.scorpion_down2);
        FieldView.bmps[7] = BitmapFactory.decodeResource(getResources(),R.drawable.scorpion_down3);
        Play.gameover  = BitmapFactory.decodeResource(getResources(),R.drawable.gameover2);
        Play.gameclear = BitmapFactory.decodeResource(getResources(),R.drawable.gameclear);
        startButton = (ImageView)findViewById(R.id.startButton);
        startButton.setVisibility(View.INVISIBLE);
        handler.postDelayed(r,800);
        setAnimations();
        startAnimation();
    }

    int animCnt=0;
    ArrayList<TranslateAnimation> snAnimList =new ArrayList<>();
    ArrayList<TranslateAnimation> sc1AnimList=new ArrayList<>();
    ArrayList<TranslateAnimation> sc2AnimList=new ArrayList<>();
    ArrayList<TranslateAnimation> sc3AnimList=new ArrayList<>();
    Handler scHandler=new Handler();
    Runnable scRun;
    Animation scAnimation;

    private void setAnimations() {

        snAnimList.add(animation(-0.5f, -0.5f, 1, 1, true));
        sc1AnimList.add(animation(-0.5f, -0.5f, 1, 1, false));
        sc2AnimList.add(animation(-0.6f, -0.5f, 0.9f, 1, false));
        sc3AnimList.add(animation(-0.4f, -0.5f, 1.1f, 1, false));

        snAnimList.add(animation(1, 0.5f, -1, 0.5f, true));
        sc1AnimList.add(animation(1, 0.5f, -1, 0.5f, false));
        sc2AnimList.add(animation(1.1f, 0.5f, -0.9f, 0.5f, false));
        sc3AnimList.add(animation(1.2f, 0.5f, -0.8f, 0.5f, false));

        snAnimList.add(animation(-0.2f, 0.7f, 1.1f, 0.4f, true));
        sc1AnimList.add(animation(-0.2f, 0.7f, 1.1f, 0.4f, false));
        sc2AnimList.add(animation(-0.3f, 0.6f, 1, 0.3f, false));
        sc3AnimList.add(animation(-0.1f, 0.8f, 1.2f, 0.5f, false));

        snAnimList.add(animation(0.3f, -0.2f, 0.3f, 1, true));
        sc1AnimList.add(animation(0.2f, -0.2f, 0.2f, 1, false));
        sc2AnimList.add(animation(0.3f, -0.2f, 0.3f, 1, false));
        sc3AnimList.add(animation(0.4f, -0.2f, 0.4f, 1, false));
    }

    private void startAnimation(){
        findViewById(R.id.snake).startAnimation(snAnimList.get(animCnt));
        scRun=new Runnable() {
            @Override
            public void run() {
                findViewById(R.id.scorpion1).startAnimation(sc1AnimList.get(animCnt));
                findViewById(R.id.scorpion2).startAnimation(sc2AnimList.get(animCnt));
                findViewById(R.id.scorpion3).startAnimation(sc3AnimList.get(animCnt));
            }
        };
        scHandler.postDelayed(scRun,300);
    }

    private void setNextAnimation(){
        animCnt++;
        if(animCnt>= snAnimList.size()){
            animCnt=0;
        }
    }


    private TranslateAnimation animation(float startX,float startY,float endX,float endY,final boolean next){
        TranslateAnimation animation= new TranslateAnimation(
                Animation.RELATIVE_TO_PARENT, startX,
                Animation.RELATIVE_TO_PARENT, endX,
                Animation.RELATIVE_TO_PARENT, startY,
                Animation.RELATIVE_TO_PARENT, endY);
        animation.setDuration(3*1000);
        animation.setFillBefore(true);
        animation.setFillAfter(true);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if(next) {
                    setNextAnimation();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            startAnimation();

                        }
                    }, 1000);
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        return animation;
    }

    public void start(View view) {
        Intent intent = new Intent(Title.this,Manual.class);
        startActivity(intent);
    }
}
