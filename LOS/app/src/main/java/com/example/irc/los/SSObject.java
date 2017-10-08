package com.example.irc.los;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Handler;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by IRC on 2017/09/01.
 */
abstract class SSObject {

    protected boolean visibility = false;
    protected int cover = Color.argb(255,10,10,10);
    protected int thumbnailResource;
    protected int value[] = new int[9];
    protected double dx=0,dy=0;
    private boolean walkable = false;
    private int x,y;
    private int id;
    private int cnt = 0;
    private int thorn = 0;
    private SSObject underObject = null;
    private Handler handler = new Handler();
    private ArrayList<Boolean> afterTask = new ArrayList<>();

    abstract int  getClassId();

    public boolean isVisible() {
        return visibility;
    }
    public boolean isWalkable() {
        return this.walkable;
    }
    public boolean isUnderNull() {
        if (this.getUnderObject() == null) {
            return true;
        }
        else {
            return false;
        }
    }
    public int getX() {
        return x;
    }
    public int getY() {
        return y;
    }
    public int getCover() {
        return cover;
    }
    public int getId() {
        return this.id;
    }
    public int getThorn() {
        return thorn;
    }
    public Bitmap getThumbnail() {
        return null;
    }
    public SSObject getUnderObject() {
        return this.underObject;
    }
    public SSObject getLowestObject() {
        if (!isUnderNull()) {
            return this.underObject.getLowestObject();
        } else {
            return this;
        }
    }

    public void setLayout(int x,int y) {
        this.x = x;
        this.y = y;
    }
    public boolean setVisibility(final boolean dVisibility) {
        if(visibility == dVisibility){return false;}
        if(this.cnt > 0) {
            this.afterTask.add(dVisibility);
            return false;
        }
        cnt = 12;
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                cnt --;
                //可視
                if (dVisibility) {
                    cover = Color.argb((int)(255d/12*cnt),10,10,10);
                }
                //不可視
                else {
                    cover = Color.argb((int)(255d/12*(12-cnt)),10,10,10);
                }
                if (cnt <= 0) {
                    visibility = dVisibility;
                    if (afterTask.size() > 0) {
                        setVisibility(afterTask.get(0));
                        afterTask.remove(0);
                    }
                    return;
                }
                handler.postDelayed(this,13);
            }
        }, 13);

        this.visibility = dVisibility;
        return true;
    }
    public boolean setWalkable(boolean walkable){
        this.walkable = walkable;
        return true;
    }
    public void setX(int x) {
        this.x = x;
    }
    public void setY(int y) {
        this.y = y;
    }
    public void setCover(int cover) {
        this.cover = cover;
    }
    public void setId(int id) {
        this.id = id;
    }
    public void setThorn(int thorn) {
        this.thorn = thorn;
    }
    public void setThumbnailResource(int thumbnailResource) {
        this.thumbnailResource = thumbnailResource;
    }
    public void setUnderObject(SSObject object) {
        this.underObject = object;
    }
}

class ActionData{

    private int id;
    private int x,y;

    ActionData(int id,int x,int y){
        this.id=id;
        this.x=x;
        this.y=y;
    }

    //アクションのID
    public void setId(int id) {
        this.id = id;
    }
    public int getId() {
        return id;
    }

    //xの移動量
    public void setX(int x) {
        this.x = x;
    }
    public int getX() {
        return x;
    }

    //yの移動量
    public void setY(int y) {
        this.y = y;
    }
    public int getY() {
        return y;
    }
}
class Route implements Cloneable{
    private ArrayList<ActionData> actData =new ArrayList<>();
    private int x=0,y=0;

    public void push(ActionData actionData){
        actData.add(actionData);
    }
    public ActionData front(){
        if (actData.size() <= 0) {
            return null;
        }
        return actData.get(0);
    }
    public void pop(){
        actData.remove(0);
    }
    public ActionData pull(){
        ActionData a=front();
        this.pop();
        return a;
    }
    public int size(){
        return actData.size();
    }

    public void setList(ArrayList<ActionData> actData){
        this.actData=actData;
    }
    public ArrayList<ActionData> getList(){
        return actData;
    }

    public void setY(int y) {
        this.y = y;
    }
    public int getY() {
        return y;
    }
    public void moveY(int my){
        this.y+=my;
    }

    public void setX(int x) {
        this.x = x;
    }
    public int getX() {
        return x;
    }
    public void moveX(int mx){
        this.x+=mx;
    }

    @Override
    public Route clone(){
        Route route=new Route();
        try {
            route=(Route)super.clone();
        }catch (Exception e){
            e.printStackTrace();
        }
        return route;
    }

}

class Snake extends SSObject {

    static Point afterAction[];

    Snake() {
        afterAction = new Point[9];
        for(int i = -1;i <= 1;i++) {
            for(int j = -1;j <= 1;j++) {
                afterAction[(i+1)*3 + (j+1)] = new Point(j,i);
            }
        }
        afterAction[0] = new Point(0,-2);
        afterAction[2] = new Point(-2,0);
        afterAction[6] = new Point(2,0);
        afterAction[8] = new Point(0,2);
        System.out.println(afterAction);

    }

    @Override
    public boolean setWalkable(boolean walkable){
        return false;
    }
    @Override
    public int getClassId() {
        return FieldView.SNAKE;
    }
}

class Scorpion extends SSObject {

    static Point afterAction[];
    private HashMap<String,ActionData> actions=new HashMap<>();
    public static int n = 3;

    Scorpion() {
        afterAction = new Point[9];
        for(int i = -1;i <= 1;i++) {
            for(int j = -1;j <= 1;j++) {
                afterAction[(i+1)*3 + (j+1)] = new Point(j,i);
            }
        }

        int i=0;
        actions.put("lu",ad(i++,-1,-1));
        actions.put("u", ad(i++, 0,-1));
        actions.put("ru",ad(i++, 1,-1));
        actions.put("l", ad(i++,-1, 0));
        actions.put("c", ad(i++, 0, 0));
        actions.put("r", ad(i++, 1, 0));
        actions.put("ld",ad(i++,-1, 1));
        actions.put("d", ad(i++, 0, 1));
        actions.put("rd",ad(i  , 1, 1));
    }

    private ActionData ad(int id,int x,int y){
        return new ActionData(id,x,y);
    }

    @Override
    public boolean setWalkable(boolean walkable){
        return false;
    }
    @Override
    public int getClassId() {
        return FieldView.SCORPION;
    }

    private String TAG="Scorpion Action";
    public int action(int[][] fieldMap) {
        int actionId= -1;

        if(fieldMap==null){
            Log.d(TAG,"fieldMap is null!!");
            return actionId;
        }
        int xmin=0,ymin=0;
        int xMax=fieldMap.length,yMax=fieldMap[0].length;

        int mx=this.getX(),my=this.getY();  //自分(Scorpion)の座標
        int ex=-1,ey=-1;                    //相手(Snake)の座標

        for(int i=xmin;i<xMax;i++){
            for(int j=ymin;j<yMax;j++){
                switch (fieldMap[i][j]){

                    case FieldView.SNAKE:
                        ey=i;
                        ex=j;
                        break;
                }
            }
        }

        if(ex==-1||ey==-1){
            Log.d(TAG,"Snake is not found!!");
            return actionId;
        }

        //nマス以内に入ったら近づく処理
        int n=Scorpion.n;

        if(Math.abs( mx-ex ) < n&&Math.abs( my-ey ) < n){
            actionId = hbtan(fieldMap,new Point(mx,my),new Point(ex,ey));
            Log.i(TAG,"go to player");
        }
        else{
            int space=5;
            ArrayList<Point> list=new ArrayList<>();
            for(int i=0;i<=yMax-space;i++){
                for(int j=0;j<=xMax-space;j++){

                    boolean flag=false;
                    for(int iy=0;iy<5;iy++){
                        for(int jx=0;jx<5;jx++){
                            if(fieldMap[i+iy][j+jx]==FieldView.SCORPION){
                                flag=true;
                            }
                        }
                    }
                    if(!flag){
                        //System.out.println("list : ("+(j+space/2)+","+(i+space/2)+")");
                        list.add(new Point(j+space/2,i+space/2));
                    }

                }
            }

            if(list.size()>0){
                Point nearPoint=null;
                for(Point p : list){
                    if(nearPoint==null)nearPoint=p;
                    else if(Math.pow(Math.abs(nearPoint.x-mx),2)+Math.pow(Math.abs(nearPoint.y-my),2)>Math.pow(Math.abs(p.x-mx),2)+Math.pow(Math.abs(p.y-my),2)){
                        nearPoint=p;
                    }
                }
                actionId=hbtan(fieldMap,new Point(mx,my),nearPoint);
                Log.i(TAG,"go to nearPoint");
                System.out.println("from : ("+mx+","+my+")");
                System.out.println("to : ("+nearPoint.x+","+nearPoint.y+")");
            }
            else{
                Object[] allkey= actions.keySet().toArray();
                System.out.println(allkey.length);
                int rndId,ay,ax;
                do{
                    rndId=(int)Math.floor( (float)Math.random()*(allkey.length));
                    ay=my+actions.get( (String) allkey[rndId]).getY();
                    ax=mx+actions.get((String) allkey[rndId]).getX();

                }while (ax<xmin||ax>=xMax||ay<ymin||ay>=yMax||fieldMap[ay][ax]!=FieldView.LAND);
                actionId=actions.get( (String) allkey[rndId]).getId();
                Log.i(TAG,"random action");
                System.out.println(allkey.length);

            }
        }
        return actionId;
    }

    private int hbtan(int[][] fieldMap,Point start,Point end){
        int mx=start.x,my=start.y;
        int xmin=0,ymin=0;
        int xMax=fieldMap.length,yMax=fieldMap[0].length;

        ArrayList<Route> queue=new ArrayList<>();
        Route route=new Route();
        route.setX(mx);
        route.setY(my);
        queue.add(route);
        boolean[][] a=new boolean[yMax][xMax];
        for(int i=0;i<yMax;i++){
            for(int j=0;j<xMax;j++){
                a[i][j]=false;
            }
        }
        tansaku:while(queue.size() > 0){
            for(String key : actions.keySet()){

                //移動先の座標
                int ax=queue.get(0).getX()+actions.get(key).getX();
                int ay=queue.get(0).getY()+actions.get(key).getY();

                //System.out.println("("+queue.get(0).getX()+","+queue.get(0).getY()+")");
                //System.out.println("("+ax+","+ay+")");

                //Indexの確認
                if(ax<xmin||ax>=xMax||ay<ymin||ay>=yMax)
                    continue;
                //既に通った座標かどうか確認
                if(a[ay][ax])
                    continue;

                //移動先が目的地だったら終了
                if(ax==end.x&&ay==end.y) {
                    route = queue.get(0);
                    route.setX(ax);
                    route.setY(ay);
                    route.push(actions.get(key));
                    break tansaku;
                }

                //移動
                if(fieldMap[ay][ax]==FieldView.LAND){
                    Route r=queue.get(0).clone();
                    r.setX(ax);
                    r.setY(ay);
                    ArrayList<ActionData> arrayList=new ArrayList<>();
                    for(ActionData actionData:r.getList()){
                        arrayList.add(actionData);
                    }
                    r.setList(arrayList);
                    //Log.d(TAG,key);
                    //System.out.println("("+ax+","+ay+")");
                    r.push(actions.get(key));
                    queue.add(r);
                    a[ay][ax]=true;

                }



            }
            queue.remove(0);
        }
        if (route.front() == null) {
            return -1;
        }
        return route.front().getId();
    }
}

class Block extends SSObject {
    Block() {
    }

    @Override
    public boolean setWalkable(boolean walkable){
        return false;
    }
    @Override
    public int getClassId() {
        return FieldView.BLOCK;
    }
}

class Land extends SSObject {
    Land() {
    }
    @Override
    public int getClassId() {
        return FieldView.LAND;
    }
}

class Goal extends SSObject{
    Goal() {
    }

    @Override
    public boolean setWalkable(boolean walkable){
        return false;
    }
    @Override
    public int getClassId() {
        return FieldView.GOAL;
    }
}