package com.example.irc.los;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.os.Handler;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class Play extends Activity {

    //field and layouts
    private int[][] generatedMap;
    private FieldView mField;
    private RelativeLayout mLayout;

    //turn
    private int turn = 0;
    private TextView tvTurn;
    private ImageView bgTurn;

    //sonar
    private int sonarLimit = sonarLimitMax;
    private TextView tvLimit;
    private Button sonar;

    //fx
    private View background;
    private ImageView img;
    public static Bitmap gameover;
    public static Bitmap gameclear;

    //options
    public static int sonarLimitMax = 3;
    public static int mapSize = 31;
    public static int enemyNum = 20;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hide();
        setContentView(R.layout.activity_play);
        tvTurn = (TextView)findViewById(R.id.turn);
        bgTurn = (ImageView)findViewById(R.id.bgturn);
        tvLimit = (TextView)findViewById(R.id.sonarLimit);
        sonar = (Button)findViewById(R.id.sonar);
        mField = (FieldView)findViewById(R.id.mField);
        mLayout = (RelativeLayout)findViewById(R.id.mR);

        AlphaAnimation a = new AlphaAnimation(0,1);
        a.setDuration(400);
        a.setStartOffset(1600);
        a.setFillAfter(true);
        bgTurn.startAnimation(a);
        tvTurn.startAnimation(a);
        tvLimit.startAnimation(a);
        sonar.startAnimation(a);
        if (!fieldInit()) {
            System.out.println("FIELD IS NULL");
        }
        updateTv();
    }
    @Override
    protected void onResume() {
        super.onResume();
        /*if (Build.VERSION.SDK_INT >= 19) {
            View decor = this.getWindow().getDecorView();
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }*/
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
            return true;
        } else {
            //finish();
            return true;
        }
    }

    public void hide() {
        if (Build.VERSION.SDK_INT >= 19) {
            View decor = this.getWindow().getDecorView();
            //decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        } else {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }
    public boolean fieldInit() {
        if (mField == null) return false;
        mField.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        retryDialog(mField.isGameEnd());
                        break;
                    case MotionEvent.ACTION_UP:
                        //ソナー発動中なら元のフォーカス状態に戻す
                        if (mField.getShowState() == FieldView.STATE_SONAR) {
                            mField.setFocus(mField.getFocusedX(), mField.getFocusedY());
                        }
                        //プレイヤーの移動
                        else {
                            int destX = mField.getRow(motionEvent.getX());
                            int destY = mField.getColumn(motionEvent.getY());
                            if (mField.isWalkable(destX, destY)){
                                if (mField.getCnt() <= 0) {
                                    if (mField.moveObjectAndFocusTo(FieldView.SNAKE, 0, destX, destY)) {
                                        turn++;
                                        updateTv();
                                        new Handler().postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                mField.moveEnemy();
                                                fx(mField.isGameEnd());
                                            }
                                        }, 250);
                                    }
                                }
                            }
                        }
                        break;
                }
                return true;
            }
        });
        int n = FieldView.SNAKE, c = FieldView.SCORPION, b = FieldView.BLOCK, l = FieldView.LAND, g = FieldView.GOAL;
        generatedMap = generateNewMap();
        mField.setFieldMap(generatedMap.clone());
        /*mField.setFieldMap(new int[][]{
                {n, l, l, l, l, l, b, l, l, b, l},
                {l, b, l, b, l, b, l, l, l, l, l},
                {l, l, l, l, l, l, l, c, b, l, b},
                {l, b, l, b, l, b, l, l, l, l, l},
                {l, l, c, b, l, l, l, b, c, l, l},
                {b, l, l, l, l, b, l, b, l, l, l},
                {l, b, l, l, b, l, l, l, l, b, l},
                {l, l, l, l, l, l, l, l, l, b, l},
                {b, l, c, l, b, b, l, b, l, l, l},
                {l, l, l, l, l, l, l, l, l, b, l},
                {l, l, l, b, l, l, l, b, l, l, g}
        });*/
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mField.setFocus(0, 0);
                mField.blackout(false);
            }
        }, 500);
        return true;
    }
    public void fx(int game) {
        int bc;
        Bitmap bmp;
        switch (game) {
            case FieldView.GAME_ONGOING:
                return;
            case FieldView.GAME_OVER:
                bc = Color.argb(224,0xff,0xff,0xff);
                bmp = gameover;
                break;
            default:
                bc = Color.argb(224,0xff,0xff,0xff);
                bmp = gameclear;
                break;
        }
        mField.setColorFilter(Color.argb(250,0,0,0));

        if (background != null) {
            mLayout.removeView(background);
            mLayout.removeView(img);
        }
        background = new View(this);
        background.setBackgroundColor(bc);
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,640);
        lp.topMargin = mLayout.getBottom()/2-(lp.height/2);
        background.setLayoutParams(lp);
        mLayout.addView(background);

        img = new ImageView(this);
        img.setImageBitmap(bmp);
        RelativeLayout.LayoutParams lp2 = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,400);
        lp2.topMargin = mLayout.getBottom()/2-200;
        img.setLayoutParams(lp);

        background.startAnimation(getAnim(true));
        TranslateAnimation t = getAnim(true);
        t.setStartOffset(700);
        //img.startAnimation(t);
        ScaleAnimation s = new ScaleAnimation(1f,1.1f,1f,1.1f);
        s.setDuration(300);
        s.setStartOffset(800);

        AnimationSet set = new AnimationSet(true);
        set.addAnimation(t);
        img.startAnimation(set);
        mLayout.addView(img);
    }
    public void eraseFx() {
        if (background == null)return;
        background.startAnimation(getAnim(false));
        img.startAnimation(getAnim(false));
    }
    public TranslateAnimation getAnim(boolean run) {
        TranslateAnimation t;
        if (run) {
            t = new TranslateAnimation(-1400, 0, 0, 0);
        }
        else {
            t = new TranslateAnimation(0,1400,0,0);
        }
        t.setFillAfter(true);
        t.setDuration(320);
        t.setStartOffset(500);
        return t;
    }
    public void retryDialog(int game) {
        if (game == FieldView.GAME_ONGOING)return;
        mField.setEnabled(false);
        final AlertDialog dialog = getDialog(game);
        dialog.show();
    }
    public android.app.AlertDialog getDialog(int game) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setCancelable(false);
        if (game == FieldView.GAME_OVER) {
            builder.setTitle("同じマップで再挑戦しますか？")
                    .setPositiveButton("はい", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //set same map to FieldView
                            restartGame();
                        }
                    })
                    .setNegativeButton("いいえ", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //ask whether user to challenge another map
                            retryDialog(FieldView.GAME_CLEAR);
                        }
                    });
        }
        else {
            builder.setTitle("別のマップに挑戦しますか？")
                    .setPositiveButton("はい", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //set another map to FieldView and restart
                            generatedMap = generateNewMap();
                            restartGame();
                        }
                    })
                    .setNegativeButton("いいえ", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //back to title
                            finish();
                        }
                    });
        }
        return builder.create();
    }
    public void restartGame() {
        mField.setFieldMap(generatedMap.clone());
        mField.setFocus(0,0);
        eraseFx();
        mField.setEnabled(true);
        mField.setGameEnd(FieldView.GAME_ONGOING);
        turn = 0;
        sonarLimit = sonarLimitMax;
        updateTv();
        hide();
    }
    public void updateTv() {
        tvTurn.setText(" " + turn + " ターン経過 ");
        int c;
        String st;
        if (sonarLimit >= sonarLimitMax) {
            c = Color.argb(0xff,0x55,0xff,0x55);
            st = String.valueOf(sonarLimit);
        }
        else if (sonarLimit >= 2) {
            c = Color.argb(0xff,0xff,0xff,0x55);
            st = String.valueOf(sonarLimit);
        }
        else if (sonarLimit >= 1) {
            c = Color.argb(0xff,0xff,0x55,0x55);
            st = String.valueOf(sonarLimit);
        }
        else {
            c = Color.argb(0xff,0xff,0x55,0xff);
            st = String.valueOf(0);
        }
        tvLimit.setTextColor(c);
        tvLimit.setText(st);
    }
    public void sonarButton(View view) {
        //ソナー発動中なら元のフォーカス状態に戻す
        if (mField.getShowState() == FieldView.STATE_SONAR) {
            mField.setFocus(mField.getFocusedX(), mField.getFocusedY());
            return;
        }
        //ソナー
        else if (sonarLimit > 0){
            if (mField.invokeSonar()) {

            }
        }
        else if (sonarLimit %4 == 0){
            Toast.makeText(this,"ソナーは使えません！",Toast.LENGTH_SHORT).show();
        }
        sonarLimit--;
        updateTv();
    }
    private int[][] generateNewMap(){

        int mapSize=this.mapSize;
        int scNum=this.enemyNum;

        int[][] fieldMap=new int[mapSize][mapSize];
        for (int i=0;i<mapSize;i++){
            for(int j=0;j<mapSize;j++){
                fieldMap[i][j]=FieldView.LAND;
            }
        }


        /*for(int i=1;i<mapSize;i+=2){
            for(int j=1;j<mapSize;j+=2){
                int rnd=(int)Math.round( Math.random()*2)-1;
                int rnd2=(int)Math.round( Math.random()*2)-1;
                fieldMap[i+rnd][j+rnd2]=FieldView.BLOCK;

                int rnd3=(int)Math.round( Math.random()*2)-1;
                int rnd4=(int)Math.round( Math.random()*2)-1;
                fieldMap[i+rnd3][j+rnd4]=FieldView.BLOCK;
            }
        }*/

        //棒倒し法
        for(int i=1;i<mapSize;i+=2){
            for(int j=1;j<mapSize;j+=2) {
                fieldMap[i][j]=FieldView.BLOCK;
            }
        }
        for(int i=1;i<mapSize;i+=2){
            Point point;
            do {
                point= udlr();
            }while(fieldMap[1+point.y][i+point.x]==FieldView.BLOCK);
            fieldMap[1+point.y][i+point.x]=FieldView.BLOCK;
        }
        for(int i=3;i<mapSize;i+=2){
            for(int j=1;j<mapSize;j+=2){
                Point point;
                do {
                    point= dlr();
                }while(fieldMap[i+point.y][j+point.x]==FieldView.BLOCK);
                fieldMap[i+point.y][j+point.x]=FieldView.BLOCK;
            }
        }

        for(int i=1;i<mapSize;i+=2){
            for(int j=1;j<mapSize;j+=2) {
                if(Math.floor( Math.random()*5)==0) {
                    fieldMap[i][j] = FieldView.LAND;
                }
            }
        }

        boolean[][] flagMap=new boolean[mapSize][mapSize];
        for(int i=0;i<mapSize;i++){
            for(int j=0;j<mapSize;j++){
                flagMap[i][j]=false;
            }
        }
        for(int i=0;i<mapSize;i++){
            for(int j=0;j<mapSize;j++){
                if(fieldMap[i][j]==FieldView.BLOCK&&!flagMap[i][j]){
                    int cnt=1;
                    ArrayList<Point> q=new ArrayList<>();
                    ArrayList<Point> q2=new ArrayList<>();
                    q.add(new Point(j,i));
                    flagMap[i][j]=true;
                    while(q.size()>0){
                        if(q.get(0).y>0&&fieldMap[q.get(0).y-1][q.get(0).x]==FieldView.BLOCK&&!flagMap[q.get(0).y-1][q.get(0).x]){
                            cnt++;
                            q.add(new Point(q.get(0).x,q.get(0).y-1));
                            q2.add(new Point(q.get(0).x,q.get(0).y-1));
                            flagMap[q.get(0).y-1][q.get(0).x]=true;
                        }
                        if(q.get(0).y<mapSize-1&&fieldMap[q.get(0).y+1][q.get(0).x]==FieldView.BLOCK&&!flagMap[q.get(0).y+1][q.get(0).x]){
                            cnt++;
                            q.add(new Point(q.get(0).x,q.get(0).y+1));
                            q2.add(new Point(q.get(0).x,q.get(0).y+1));
                            flagMap[q.get(0).y+1][q.get(0).x]=true;
                        }
                        if(q.get(0).x>0&&fieldMap[q.get(0).y][q.get(0).x-1]==FieldView.BLOCK&&!flagMap[q.get(0).y][q.get(0).x-1]){
                            cnt++;
                            q.add(new Point(q.get(0).x-1,q.get(0).y));
                            q2.add(new Point(q.get(0).x-1,q.get(0).y));
                            flagMap[q.get(0).y][q.get(0).x-1]=true;
                        }
                        if(q.get(0).x<mapSize-1&&fieldMap[q.get(0).y][q.get(0).x+1]==FieldView.BLOCK&&!flagMap[q.get(0).y][q.get(0).x+1]){
                            cnt++;
                            q.add(new Point(q.get(0).x+1,q.get(0).y));
                            q2.add(new Point(q.get(0).x+1,q.get(0).y));
                            flagMap[q.get(0).y][q.get(0).x+1]=true;
                        }
                        q.remove(0);
                    }
                    if(cnt>3){
                        int rnd=(int)Math.floor( Math.random()*q2.size());
                        fieldMap[q2.get(rnd).y][q2.get(rnd).x]=FieldView.LAND;
                        if(q2.get(rnd).y>0&&fieldMap[q2.get(rnd).y-1][q2.get(rnd).x]==FieldView.BLOCK){
                            fieldMap[q2.get(rnd).y-1][q2.get(rnd).x]=FieldView.LAND;
                        }
                        else if(q2.get(0).y<mapSize-1&&fieldMap[q2.get(0).y+1][q2.get(0).x]==FieldView.BLOCK){
                            fieldMap[q2.get(0).y+1][q2.get(0).x]=FieldView.LAND;
                        }
                        else if(q2.get(0).x>0&&fieldMap[q2.get(0).y][q2.get(0).x-1]==FieldView.BLOCK){
                            fieldMap[q2.get(0).y][q2.get(0).x-1]=FieldView.LAND;
                        }
                        else if(q2.get(0).x<mapSize-1&&fieldMap[q2.get(0).y][q2.get(0).x+1]==FieldView.BLOCK){
                            fieldMap[q2.get(0).y][q2.get(0).x+1]=FieldView.LAND;
                        }

                        for(int ii=0;ii<(cnt-4)/3;ii++){
                            int rnd2;
                            do {
                                rnd2 = (int) Math.floor(Math.random() * q2.size());
                            }
                            while(fieldMap[q2.get(rnd2).y][q2.get(rnd2).x]!=FieldView.BLOCK);
                            fieldMap[q2.get(rnd2).y][q2.get(rnd2).x]=FieldView.LAND;


                        }
                    }

                }
            }
        }

        boolean[][] scflag=new boolean[mapSize][mapSize];
        for(int i=0;i<mapSize;i++){
            for(int j=0;j<mapSize;j++){
                if(fieldMap[i][j]==FieldView.LAND)
                    scflag[i][j]=true;
                else
                    scflag[i][j]=false;

            }
        }
        for(int i=0;i<4;i++){
            for(int j=0;j<4;j++){
                scflag[i][j]=false;
            }
        }

        for(int c=0;c<scNum;c++){
            int trueCnt=0;
            for(int i=0;i<mapSize;i++){
                for(int j=0;j<mapSize;j++){
                    if(scflag[i][j]){
                        trueCnt++;
                    }
                }
            }
            ArrayList<Integer> b=new ArrayList<>();
            for (int i=0;i<trueCnt;i++){
                b.add(i);
            }
            for(int i=0;i<trueCnt-1;i++){
                int rnd=(int)Math.floor(Math.random()*b.size());
                b.remove(rnd);
            }
            trueCnt=0;
            loopTruecnt: for(int i=0;i<mapSize;i++){
                for(int j=0;j<mapSize;j++) {
                    if(scflag[i][j]){
                        if(trueCnt==b.get(0)){
                            fieldMap[i][j]=FieldView.SCORPION;
                            for(int iy=(i-3>=0?i-3:0);iy<=(i+3<mapSize?i+3:mapSize-1);iy++){
                                for(int jx=(j-3>=0?j-3:0);jx<=(j+3<mapSize?j+3:mapSize-1);jx++){
                                    scflag[iy][jx]=false;
                                }
                            }
                            break loopTruecnt;
                        }
                        trueCnt++;
                    }
                }
            }
        }


        fieldMap[0][0]=FieldView.SNAKE;
        fieldMap[0][1]=fieldMap[1][0]=FieldView.LAND;
        fieldMap[mapSize-1][mapSize-1]=FieldView.GOAL;
        fieldMap[mapSize-2][mapSize-1]=fieldMap[mapSize-1][mapSize-2]=FieldView.LAND;


        return fieldMap;
    }
    private Point udlr(){
        int rnd=(int)Math.floor(Math.random()*4);
        switch (rnd){
            case 0:
                return new Point(0,-1);
            case 1:
                return new Point(0,1);
            case 2:
                return new Point(-1,0);
            case 3:
                return new Point(1,0);

            default:
                return new Point(0,-1);
        }
    }
    private Point dlr(){
        int rnd=(int)Math.floor(Math.random()*3);
        switch (rnd){
            case 0:
                return new Point(0,1);
            case 1:
                return new Point(-1,0);
            case 2:
                return new Point(1,0);

            default:
                return new Point(0,1);
        }
    }
}
