package com.example.irc.los;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by IRC on 2017/09/01.
 */
public class FieldView extends ImageView implements Runnable{
    public static final int LAND        = 0;  //　土地
    public static final int BLOCK       = 1;  //　壁
    public static final int SNAKE       = 2;  //　蛇
    public static final int SCORPION    = 3;  //　蠍
    public static final int GOAL        = 4;  //　ゴール

    //見えている範囲
    public static final int STATE_SHOW_ALL  = 10; //show Overall view
    public static final int STATE_FOCUS     = 11; //focus to player
    public static final int STATE_SONAR     = 12; //sonar was invoked
    public static final int STATE_BLACKOUT  = 13; //field is invisible

    //isGameEnd()の返り値
    public static final int GAME_OVER    = 20; //蠍が    蛇の上にいる
    public static final int GAME_CLEAR   = 21; //蛇がゴールの上にいる
    public static final int GAME_ONGOING = 22; //ゲームが続いている

    //描画関係_定数
    private static int focusableBlock = 5;    //focus時に表示されるブロックの数
    private static int updateNum = 22;        //animation時の描画更新回数
    private static int interval = 16;         //animation時の描画の実行周期[ms]
    private static int speed = 5;             //objectの移動速度 ( 早い < speed < 遅い )
    private static float changeAmount = 7f;   //animation時の拡大縮小移動の変化量 (x += 移動分 / changeAmount)
    //描画関係_変数
    private boolean blackout = true; //first dark state
    private int curtain = 0;         //Color for darkening (カーテン)
    private int player = SNAKE;      //(未使用)player
    private int enemy  = SCORPION;   //(未使用)enemy
    private int showState = STATE_SHOW_ALL; //Invisible range
    private int gameEnd   = GAME_ONGOING;
    private int cnt = 0;                //count for animation
    private int focusedX=0,focusedY=0;  //focusedXY
    private float tx=0,ty=0,dx,dy,ix,iy;//coordinate information for animation
    private float scale = 1f,dscale;    //displayed range magnification
    private Rect mRect;                 //The size of one square
    private ArrayList<Integer> afterDraw = new ArrayList<>(); //object to draw on top layer
    private ArrayList<Task> afterTask = new ArrayList<>(); //postponed task
    private SSObject fieldMap[][];  //fieldMap
    private Handler handler;        //handler for drawing
    public static Bitmap[] bmps;
    private static Rect size16;
    private static Rect size256;

    FieldView(Context c) {
        super(c);
    }
    FieldView(Context c, AttributeSet attr) {
        super(c,attr);
        handler = new Handler();
        curtain = Color.argb(255,10,10,10);

    }
    FieldView(Context c, AttributeSet attr, int d) {
        super(c,attr,d);
    }

    @Override
    public void onDraw(Canvas canvas) {
        canvas.drawColor(Color.argb(255,0,0,0));
        if (fieldMap == null) {
            super.onDraw(canvas);
            return;
        }
        canvas.save();
        canvas.translate(tx*scale,ty*scale);
        canvas.scale(scale,scale);
        int i,j;
        afterDraw.clear();
        for (i = 0;i < fieldMap.length;i++) {
            for (j = 0;j < fieldMap[i].length;j++) {
                // ヘビかサソリならあとで(最前面に)描画
                if (fieldMap[i][j].getClassId() == SNAKE || fieldMap[i][j].getClassId() == SCORPION) {
                    afterDraw.add(i);
                    afterDraw.add(j);
                    continue;
                }
                drawObject(canvas,i,j);
            }
        }
        for (i = 0;i < afterDraw.size();i+=2) {
            drawObject(canvas,afterDraw.get(i),afterDraw.get(i+1));
        }
        canvas.restore();
        canvas.drawColor(curtain);
        super.onDraw(canvas);
    }
    @Override
    public void run() {
        if (cnt <= 0){
            cnt = -1;
            tx += dx;
            ty += dy;
            scale += dscale;
            expendTask();
            return;
        }
        else if (cnt > 0){
            cnt--;
            tx += dx / changeAmount;
            ty += dy / changeAmount;
            scale += dscale / changeAmount;
            dx -= dx / changeAmount;
            dy -= dy / changeAmount;
            dscale -= dscale / changeAmount;
        }
        handler.postDelayed(this, interval);
    }

    //draw
    public void drawObject(Canvas canvas,int i,int j) {
        int classId;
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(255,0,0,0));
        ArrayList<SSObject> layer = convertObjToLayer(fieldMap[i][j]);
        // layerのオブジェクトを下層から描画していく
        for(int a = layer.size()-1;a >= 0;a--) {
            if(mRect == null)continue;
            //オブジェクトのサムネイル画像の描画
            paint.setColor(Color.argb(255,0,0,0));
            mRect.offsetTo((int)(j*ix+(layer.get(a).dx*ix)),(int)(i*iy+(layer.get(a).dy*iy)));
            classId = layer.get(a).getClassId();
            if (classId == SCORPION && layer.get(a).getThorn() > 0) {
                canvas.drawBitmap(bmps[classId + 1 + layer.get(a).getThorn()],getRect(classId),mRect,paint);
            }
            else {
                canvas.drawBitmap(bmps[classId], getRect(classId), mRect, paint);
            }
            //歩ける場所であるなら青くする
            if (layer.get(a).isWalkable()){
                paint.setColor(Color.argb(170,0x89,0xCE,0xEB));
                canvas.drawRect(mRect,paint);
            }
            //個々のvisibility依存のカバー(暗幕の役目)を描画する
            paint.setColor(layer.get(a).getCover());
            canvas.drawRect(mRect,paint);
        }
    }
    public int getRow(float x) {
        int fx = (int)(x/scale-tx);
        return fx/(int)ix;
    }
    public int getColumn(float y) {
        int fy = (int)(y/scale-ty);
        return fy/(int)iy;
    }
    public static void setDrawingQuality(int quality) {
        updateNum = 12+3*quality;
        interval = 20 - 2*quality;
        speed = (int)((quality/5f*4)+1);
        changeAmount = 2 + quality*3/5;
    }
    //move
    public void moveEnemy() {
        for (int i = 0;i < getSize(SCORPION);i++) {
            Scorpion sc = (Scorpion)getObj(SCORPION,i);
            if (sc.getThorn() > 0) {
                sc.setThorn(sc.getThorn()-1);
            }
            else {
                sc.action(createMap());
            }
        }
    }
    public boolean invokeAction(final int type,final int objId,final int actionId) {
        if (actionId < 0 || 9 < actionId)return false;
        int dx=0,dy=0;
        switch (type){
            case SNAKE:
                dx = Snake.afterAction[actionId].x;
                dy = Snake.afterAction[actionId].y;
                break;
            case SCORPION:
                dx = Scorpion.afterAction[actionId].x;
                dy = Scorpion.afterAction[actionId].y;
                break;
        }
        return moveObject(type,objId,dx,dy);
    }
    public boolean moveObjectTo(int type,int objId,final int x, final int y) {
        if (x<0||fieldMap.length<=x||y<0||fieldMap.length<=y) return false;
        int objX, objY;
        for (int i = 0; i < fieldMap.length; i++) {
            for (int j = 0; j < fieldMap[0].length; j++) {
                if (fieldMap[i][j].getClassId() == type) {
                    if (objId == 0) {
                        objX = j;
                        objY = i;
                        return moveMapsObject(objX, objY, x - objX, y - objY);
                    }
                    else {
                        objId--;
                    }
                }
            }
        }
        return false;
    }
    public boolean moveObjectAndFocusTo(int type,int objId,final int x, final int y) {
        if (x<0||fieldMap.length<=x||y<0||fieldMap.length<=y) return false;
        if (!setFocus(x,y))return false;
        int objX, objY;
        SSObject ssObject = getObj(type,objId);
        objX = ssObject.getX();
        objY = ssObject.getY();
        return moveMapsObject(objX, objY, x-objX, y-objY);
    }
    public boolean moveObject(int type,int objId,final int dx, final int dy) {
        if (dx == 0 && dy == 0) return false;
        int objX, objY;
        SSObject ssObject = getObj(type,objId);
        objX = ssObject.getX();
        objY = ssObject.getY();
        return moveMapsObject(objX, objY, dx, dy);
    }
    private boolean moveMapsObject(final int objX, final int objY, int dx, int dy) {
        // No movement
        if (dx == 0&&dy==0)return false;
        // The destination is out of range
        if (objX + dx < 0 || fieldMap.length <= objX + dx)return false;
        if (objY + dy < 0 || fieldMap.length <= objY + dy)return false;

        final int ax=objX+dx,ay=objY+dy;
        // Destination object
        SSObject box = fieldMap[ay][ax];
        if (fieldMap[objY][objX].getUnderObject() != null) {
            /*     ----三角交換----     */
            // destination = original location 目的地にObjectを代入する
            fieldMap[ay][ax] = fieldMap[objY][objX];
            // original location = original location.under Objectの居た位置をObjectの下にあったObjectにする
            fieldMap[objY][objX] = fieldMap[objY][objX].getUnderObject();
            // destination.under = destination Object(目的地)の下に、目的地に元あったObjectをセットする
            fieldMap[ay][ax].setUnderObject(box);

            //underObjectのvisibilityに合わせる;
            fieldMap[ay][ax].setVisibility(fieldMap[ay][ax].getUnderObject().isVisible());
            /*     ----三角交換End----     */

            //walkable setting
            if (fieldMap[ay][ax].getClassId() == player) {
                showWalkable(ax,ay);
            }

            // Objectsの情報を更新する
            fieldMap[ay][ax].setLayout(ax,ay);
            if (!fieldMap[ay][ax].isUnderNull()) {
                fieldMap[ay][ax].getUnderObject().setLayout(ax,ay);
            }
            fieldMap[objY][objX].setLayout(objX,objY);
            if (!fieldMap[objY][objX].isUnderNull()) {
                fieldMap[objY][objX].getUnderObject().setLayout(objX,objY);
                fieldMap[objY][objX].getUnderObject().visibility = true;
                fieldMap[objY][objX].getUnderObject().cover = Color.argb(0,0,0,0);
            }
            //衝突判定
            collision(ax,ay);
            fieldMap[ay][ax].dx = -dx;
            fieldMap[ay][ax].dy = -dy;
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    fieldMap[ay][ax].dx *= (speed-1d)/speed;
                    fieldMap[ay][ax].dy *= (speed-1d)/speed;
                    if (Math.abs(fieldMap[ay][ax].dy) <= 0.04 && Math.abs(fieldMap[ay][ax].dx) <= 0.04) {
                        fieldMap[ay][ax].dx = 0d;
                        fieldMap[ay][ax].dy = 0d;
                        return;
                    }
                    handler.postDelayed(this,interval);
                }
            }, interval);
        }
        else {
            return false;
        }
        return true;
    }
    public void collision(int x,int y) {
        if(!fieldMap[y][x].isUnderNull()){
            if (fieldMap[y][x] instanceof Snake && fieldMap[y][x].getUnderObject() instanceof Scorpion) {
                fieldMap[y][x].getUnderObject().setThorn(3);
            }
            else if (fieldMap[y][x] instanceof Snake && fieldMap[y][x].getUnderObject() instanceof Goal) {
                gameEnd = GAME_CLEAR;
            }
            else if (fieldMap[y][x] instanceof Scorpion && fieldMap[y][x].getUnderObject() instanceof Snake) {
                gameEnd = GAME_OVER;
            }
        }
    }
    public void showWalkable(int ax,int ay){
        for (int i = 0;i < fieldMap.length;i++) {
            for (int j = 0;j < fieldMap.length;j++) {
                boolean walkable=checkWalkable(j-ax,i-ay,ax,ay);
                fieldMap[i][j].setWalkable(walkable);
                if(!fieldMap[i][j].isUnderNull()){fieldMap[i][j].getUnderObject().setWalkable(walkable);}
            }
        }
    }
    public boolean checkWalkable(int moveX,int moveY,int x,int y) {
        if (player == SNAKE) {
            for (int i = 0;i < 9;i++) {
                if (i == 4)continue;
                if (Snake.afterAction[i].x == moveX && Snake.afterAction[i].y == moveY) {
                    if (i%2==0){
                        try{
                            if (fieldMap[y+(moveY/2)][x+(moveX/2)].getClassId() == BLOCK)continue;
                        } catch (ArrayIndexOutOfBoundsException e) {}
                    }
                    return true;
                }
            }
        }
        return false;
    }
    public boolean isWalkable(int x,int y){
        if (x < 0 || fieldMap.length <= x || y < 0 || fieldMap.length <= y)return false;
        return (fieldMap[y][x].getLowestObject().isWalkable());
    }
    public ArrayList<SSObject> convertObjToLayer(SSObject object) {
        ArrayList<SSObject> layer = new ArrayList<>();
        layer.add(object);
        // layerに下層のオブジェクトをすべて格納する
        while(!layer.get(layer.size()-1).isUnderNull()) {
            layer.add(layer.get(layer.size()-1).getUnderObject());
        }
        return layer;
    }
    //set
    public void setPlayer(int p) {
        this.player = p;
    }
    public boolean setFieldMap(int map[][]) {
        if (map == null)return false;
        this.fieldMap = new SSObject[map.length][map[0].length];
        int snId = 0,scId = 0;
        int i,j;
        for (i = 0;i < map.length;i++) {
            for (j = 0;j <map[i].length;j++) {
                switch (map[i][j]) {
                    case SNAKE:
                        fieldMap[i][j] = new Snake();
                        fieldMap[i][j].setId(snId);
                        snId++;
                        fieldMap[i][j].setUnderObject(new Land());
                        this.focusedY = i;
                        this.focusedX = j;
                        break;
                    case SCORPION:
                        fieldMap[i][j] = new Scorpion() {
                            @Override
                            public int action(int[][] fieldMap) {
                                int actionId = super.action(fieldMap);
                                if (actionId < 0)return actionId;
                                moveObject(SCORPION,this.getId(),afterAction[actionId].x,afterAction[actionId].y);
                                return actionId;
                            }
                        };
                        fieldMap[i][j].setId(scId);
                        scId++;
                        fieldMap[i][j].setUnderObject(new Land());
                        break;
                    case LAND:
                        fieldMap[i][j] = new Land();
                        break;
                    case BLOCK:
                        fieldMap[i][j] = new Block();
                        break;
                    case GOAL:
                        fieldMap[i][j] = new Goal();
                        fieldMap[i][j].setUnderObject(new Land());
                        break;
                }
                fieldMap[i][j].setLayout(j,i);
            }
        }
        showWalkable(0,0);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                invalidate();
                if(ix <= 1||iy<=1) {
                    ix = getWidth() / (float) fieldMap[0].length;
                    iy = getHeight() / (float) fieldMap.length;
                    mRect = new Rect(0,0,(int)ix,(int)iy);
                }
                handler.postDelayed(this,interval);
            }
        }, 100);
        return true;
    }
    public int[][] createMap() {
        int[][] re = new int[fieldMap.length][fieldMap.length];
        for (int i = 0;i<fieldMap.length;i++) {
            for (int j = 0;j<fieldMap.length;j++) {
                SSObject ob = fieldMap[i][j];
                if (ob instanceof Scorpion) {
                    re[i][j] = SCORPION;
                }
                else if (ob instanceof Snake) {
                    re[i][j] = SNAKE;
                }
                else if (ob instanceof Land) {
                    re[i][j] = LAND;
                }
                else {
                    re[i][j] = BLOCK;
                }
            }
        }
        return re;
    }
    public void setGameEnd(int gameEnd) {
        this.gameEnd = gameEnd;
    }
    public static void setFocusableBlock(int focusableBlock) {
        FieldView.focusableBlock = focusableBlock;
    }
    //focus
    class Task{
        private int state,x,y;
        Task(int state,int x,int y) {
            this.state = state;
            this.x = x;
            this.y = y;
        }
        public int getState() {
            return state;
        }
        public int getX() {
            return x;
        }
        public int getY() {
            return y;
        }
    }
    public void expendTask() {
        if (afterTask.size() > 0) {
            Task task = afterTask.get(0);
            switch (task.getState()){
                case STATE_SHOW_ALL:
                    showAll();
                    break;
                case STATE_FOCUS:
                    setFocus(task.getX(),task.getY());
                    break;
                case STATE_SONAR:
                    invokeSonar();
                    break;
                case STATE_BLACKOUT:
                    if (task.getX() == 0) {
                        blackout(false);
                    }
                    else {
                        blackout(true);
                    }
                    break;
            }
            afterTask.remove(0);
        }
    }
    public boolean showAll() {
        if (cnt > 0){
            afterTask.add(afterTask.size(),new Task(STATE_SHOW_ALL,0,0));
            return false;
        }
        for (int i = 0;i < fieldMap.length;i++) {
            for (int j = 0;j < fieldMap.length;j++) {
                fieldMap[i][j].setVisibility(true);
                if (!fieldMap[i][j].isUnderNull()) {
                    fieldMap[i][j].getUnderObject().setVisibility(true);
                }
            }
        }
        this.showState = STATE_SHOW_ALL;
        dx = -tx;
        dy = -ty;
        dscale = 1-scale;
        cnt = updateNum;
        handler.postDelayed(this,interval);
        return true;
    }
    public boolean setFocus(int x,int y) {
        if (cnt > 0){
            afterTask.add(afterTask.size(),new Task(STATE_FOCUS,x,y));
            return false;
        }
        if (x < 0 || fieldMap.length <= x || y < 0 || fieldMap.length <= y)return false;
        for (int i = 0;i < fieldMap.length;i++) {
            for (int j = 0;j < fieldMap.length;j++) {
                int moveX = x-j,moveY = y-i;
                boolean set;
                if (Math.abs(moveX) + Math.abs(moveY) <= focusableBlock/2) {set = true;}
                else {set = false;}
                if (!fieldMap[i][j].isUnderNull()) {
                    fieldMap[i][j].getUnderObject().setVisibility(set);
                }
                fieldMap[i][j].setVisibility(set);
            }
        }

        this.showState = STATE_FOCUS;
        this.focusedX = x;
        this.focusedY = y;
        float destinationX = -(this.getWidth() /(float)fieldMap[0].length)*(x-((focusableBlock-1f)/2f));
        float destinationY = -(this.getHeight()/(float)fieldMap.length)   *(y-((focusableBlock-1f)/2f));
        dx = destinationX-tx;
        dy = destinationY-ty;
        dscale = (fieldMap.length/(float)focusableBlock) -scale;
        cnt = updateNum;
        handler.postDelayed(this,interval);
        return true;
    }
    public boolean invokeSonar() {
        if (cnt > 0){
            afterTask.add(afterTask.size(),new Task(STATE_SONAR,0,0));
            return false;
        }
        this.showState = STATE_SONAR;
        dx = -tx;
        dy = -ty;
        dscale = 1-scale;
        cnt = updateNum;
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                for(int i = 0;i < getSize(SCORPION);i++) {
                    SSObject sc = getObj(SCORPION,i);
                    sc.setVisibility(true);
                    if (!sc.isUnderNull()){
                        sc.getUnderObject().setVisibility(true);
                    }
                }
            }
        }, interval * (updateNum+4));
        handler.postDelayed(this,interval);
        return true;
    }
    public boolean blackout(final boolean io) {
        if (blackout == io) {
            expendTask();
            return false;
        }
        if (cnt > 0){
            if (io) {
                afterTask.add(afterTask.size(), new Task(STATE_BLACKOUT,1,1));
            }
            else {
                afterTask.add(afterTask.size(), new Task(STATE_BLACKOUT,0,0));
            }
            return false;
        }
        cnt = updateNum;
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                cnt--;
                if (blackout) {
                    curtain = Color.argb((int)(255d/updateNum*cnt),10,10,10);
                }
                else {
                    curtain = Color.argb((int)(255d/updateNum*(updateNum-cnt)),10,10,10);
                }
                if (cnt <= 0) {
                    blackout = io;
                    expendTask();
                    return;
                }
                handler.postDelayed(this,interval);
            }
        }, interval);
        return true;
    }
    //get
    public int getFocusedX() {
        return focusedX;
    }
    public int getFocusedY() {
        return focusedY;
    }
    public int getSize(int type){
        int size = 0;
        for (int i=0;i<fieldMap.length;i++){
            for(int j=0;j<fieldMap[0].length;j++) {
                ArrayList<SSObject> arr = convertObjToLayer(fieldMap[i][j]);
                for (int k = 0;k < arr.size();k++) {
                    if (arr.get(k).getClassId() == type) {
                        size++;
                    }
                }
            }
        }
        return size;
    }
    public SSObject getObj(int type,int objId) {
        for (int i=0;i<fieldMap.length;i++){
            for(int j=0;j<fieldMap[0].length;j++) {
                ArrayList<SSObject> arr = convertObjToLayer(fieldMap[i][j]);
                for (int k = 0;k < arr.size();k++) {
                    if (arr.get(k).getClassId() == type && arr.get(k).getId() == objId) {
                        return arr.get(k);
                    }
                }
            }
        }
        return null;//nullnull
    }
    public int getShowState() {
        return this.showState;
    }
    public int isGameEnd() {
        return gameEnd;
    }
    public Rect getRect(int classId) {
        switch (classId) {
            case BLOCK:
            case LAND:
                return size16;
            case SNAKE:
            case SCORPION:
            case GOAL:
                return size256;
            default:
                return null;
        }
    }
    public int getCnt() {
        return cnt;
    }
}
