package com.example.irc.los;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.Toast;
import android.widget.ViewFlipper;
public class Manual extends Activity {

    ScrollView scrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual);

        scrollView = (ScrollView) findViewById(R.id.sc);
        AlphaAnimation anim = new AlphaAnimation(1,0.2f);
        anim.setDuration(800);
        anim.setRepeatCount(Animation.INFINITE);
        anim.setRepeatMode(Animation.REVERSE);
        ((Button)findViewById(R.id.button)).startAnimation(anim);
        firstMove();
    }

    public void firstMove(){
        int x = 0;
        int y = 500;
        ObjectAnimator xTranslate = ObjectAnimator.ofInt(scrollView, "scrollX", x);
        ObjectAnimator yTranslate = ObjectAnimator.ofInt(scrollView, "scrollY", y);

        AnimatorSet animators = new AnimatorSet();
        animators.setDuration(1);
        animators.playTogether(xTranslate, yTranslate);
        animators.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator arg0) {}
            @Override
            public void onAnimationRepeat(Animator arg0) {}
            @Override
            public void onAnimationEnd(Animator arg0) {
                // TODO Auto-generated method stub
                scroolToTop();
            }
            @Override
            public void onAnimationCancel(Animator arg0) {}
        });
        animators.start();
    }
    public void scroolToTop() {
        int x = 0;
        int y = 0;
        ObjectAnimator xTranslate = ObjectAnimator.ofInt(scrollView, "scrollX", x);
        ObjectAnimator yTranslate = ObjectAnimator.ofInt(scrollView, "scrollY", y);

        AnimatorSet animators = new AnimatorSet();
        animators.setDuration(680);
        animators.playTogether(xTranslate, yTranslate);
        animators.addListener(new Animator.AnimatorListener() {

            @Override
            public void onAnimationStart(Animator arg0) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onAnimationRepeat(Animator arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onAnimationEnd(Animator arg0) {
                // TODO Auto-generated method stub
                scrollView.scrollTo(0,0);
            }

            @Override
            public void onAnimationCancel(Animator arg0) {
                // TODO Auto-generated method stub

            }
        });
        animators.start();
    }
    public void onstart(View view) {
        startActivity(new Intent(Manual.this,DifficultyOption.class));
    }
}
