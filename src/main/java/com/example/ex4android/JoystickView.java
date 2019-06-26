package com.example.ex4android;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.View;
import android.graphics.RectF;
import androidx.constraintlayout.solver.widgets.Rectangle;

/**
 * This class is a view for activities with joysticks, used to control a plane in flight gear
 * simulator.
 */
public class JoystickView extends View {
    static private final String ElevatorPath = "controls/flight/elevator";
    static private final String AileronPath = "controls/flight/aileron";
    //this paint will be used to decorate this view.
    private Paint paint;
    /*
    This value determines, how much from limits of joystick area,
     the joystick allowed to be moved(Joystick are- oval around the
     circle of the joystick).
     */
    private float restrictingVal;
    private Float joystickX;
    private Float joystickY;
    //this client will be connected to the server of the simulator.
    private TcpClient client;
    //Is joystick moving by user now.
    private boolean isMoving;
    public JoystickView(Context context, TcpClient tcpClient) {
        super(context);
        // create the Paint and set its color
        paint = new Paint();
        paint.setColor(Color.GRAY);
        /*
        We put here null, and only then initialize this X and Y coordinates,
        because when the constructor is called, the width and height of a screen
        is not known yet and therefore we cannot yet to determine a start position of
        the joystick to match properly to a size of a screen.
         */
        joystickX = null;
        joystickY = null;
        client = tcpClient;

    }

    private float getJoystickRadius(){
        double diagonalLen = Math.sqrt ( Math.pow(getWidth(),2) + Math.pow(getHeight(),2) );
        return (float)(diagonalLen / 10);
    }

    /**
     * Sets coordinate-x for joystick location. If a passed value is out of allowed area,
     * the max/min allowed value will be set instead. That is, if x is less than minimal
     * allowed X, than minimal allowed x will be set. If x is more than max
     * allowed x, than max allowed x will be set.
     * @param x - x coordinate to set.
     */
    public void setJoystickX(Float x) {
        if (x <= this.getOvalLeft() + restrictingVal )
            joystickX = this.getOvalLeft()+ restrictingVal;
        else if( x>= this.getOvalRight() - restrictingVal )
            joystickX = this.getOvalRight() - restrictingVal;
        else
            joystickX = x;
    }
    /**
     * Sets coordinate-y for joystick location. If a passed value is out of allowed area,
     * the max/min allowed value will be set instead. That is, if y is less than minimal
     * allowed y, than minimal allowed y will be set. If y is more than max
     * allowed y, than max allowed y will be set.
     * @param y - y coordinate to set.
     */
    public void setJoystickY(Float y) {
        if (y <= this.getOvalTop() + restrictingVal)
            joystickY = this.getOvalTop() + restrictingVal;
        else if( y >= this.getOvalBottom() - restrictingVal )
            joystickY = this.getOvalBottom() - restrictingVal;
        else
            joystickY = y;
    }
    public float getOvalLeft() {
        return getWidth() / 11;
    }
    public float getOvalRight() {
        return 10 * getWidth() / 11;
    }
    public float getOvalTop() {
        return getHeight() / 11;
    }
    public float getOvalBottom() {
        return 10* getHeight() / 11;
    } /**
     * Function that draws this view.
     */
    @Override
    protected void onDraw(Canvas canvas) {
        //Setting a restricting value.(When this function has being called, a screen
        //size already is known and therefore we can set it here).
        restrictingVal = this.getJoystickRadius() / 2;
        canvas.drawColor(Color.BLUE);
        RectF rect = new RectF(getOvalLeft(),getOvalTop(), getOvalRight(), getOvalBottom());
        //drawing an oval, which represents a limited area for moving joystick.
        canvas.drawOval(rect,paint);
        Paint paintCircle = new Paint();
        paintCircle.setColor(Color.GREEN);
        float centerX;
        float centerY;
        /*
        initializing joystickX and joystickY if there were not yet(we know screen size in this
        function, therefore we can initialize it here, and not in the constructor for example).
         */

        if (joystickX == null || joystickY == null) {
            centerX = getWidth() / 2;
            centerY = getHeight() / 2;
            joystickX = centerX;
            joystickY = centerY;
        } else {
            centerX = joystickX;
            centerY = joystickY;
        }
        canvas.drawCircle(centerX, centerY, getJoystickRadius(), paintCircle);
    }

    /**
     * This function checks whether passed x and y coordinates represents a point
     * whick is within a joystick circle.
     * @param x - x coordinate.
     * @param y - y coordinate.
     * @return true if (x,y) point within the joystick and false otherwise.
     */
    public boolean isWithinJoystick(float x, float y) {
        float difX = this.joystickX - x;
        float difY = this.joystickY - y;
        float dist = (float) Math.sqrt( Math.pow(difX,2) + Math.pow(difY,2) );
        return this.getJoystickRadius() >= dist;
    }
    @Override
    /**
     * OnTouchEvent method. If a user moves the joystick, this function
     * will change appropriate values on the server.Also, we will change x and y
     * coordinates of the joystick on the view and redraw the view,
     * so that move-touches of a user move joystick in a nice animation.
     */
    //todo: IMPLEMENT THIS FUNCTION!
    public boolean onTouchEvent(MotionEvent event) {
        int curAction = event.getAction();
        if (curAction == MotionEvent.ACTION_DOWN &&
                this.isWithinJoystick(event.getX(), event.getY()) ) {
            isMoving = true;
        } else if (isMoving && curAction == MotionEvent.ACTION_MOVE) {
            this.setJoystickX(event.getX());
            /*
            Computing new elevator value. We normalize the value for the simulator,
            because simulator gets values between -1 and 1 for aileron, and coordinates
            on the screen can take any range of values(depends on a screen size).
            */
            float newAileron = JoystickView.normalizeValue(
                    this.getOvalLeft() + restrictingVal,
                    this.getOvalRight() - restrictingVal, joystickX);
            //setting newAileron on the simulator.
            client.sendMessage(
                    "set " + JoystickView.AileronPath + " " + Float.toString(newAileron) + "\r\n");

            this.setJoystickY(event.getY());
             /*
            Computing new elevator value. We normalize the value for the simulator,
            because simulator get values between -1 and 1 for elevator, and coordinates
            on the screen can take any range of values(depends on a screen size).
            Normalized value we multiply by -1, because y-coordinates goes from top
            to down on the android, however a user wants down top aproach: more y-value - the higher
            joystick is located.
             */
            float newElevator = - JoystickView.normalizeValue(
                    this.getOvalTop() + restrictingVal,
                    this.getOvalBottom() - restrictingVal, joystickY);
            //setting newAileron on the simulator.
            client.sendMessage(
                    "set " + JoystickView.ElevatorPath + " " + Float.toString(newElevator) + "\r\n");
        } else if (isMoving &&
                (curAction == MotionEvent.ACTION_UP || curAction == MotionEvent.ACTION_CANCEL) ) {
            isMoving = false;
            //If a user stops moving a joystick, we return it to its start position.
            this.setJoystickX((float) getWidth() / 2);
            this.setJoystickY((float) getHeight() / 2);
            //updating start position on the simulator too.
            client.sendMessage(
                    "set " + JoystickView.AileronPath + " " + Float.toString(0) + "\r\n");
            client.sendMessage(
                    "set " + JoystickView.ElevatorPath + " " + Float.toString(0) + "\r\n");
        }
        invalidate();
        return true;
    }
    /**
     * Converts value from section [minVal, maxVal] to appropriate to it value between [-1,1].
     *In other words, maps a value from previous distribution between minVal and maxVal to normal
     *distribution(between -1 and 1).
     */
    static private float normalizeValue(float minVal, float maxVal, float val) {
        if (val < minVal)
            return -1;
        if (val > maxVal)
            return 1;
        float middle = (maxVal + minVal) / 2;
        float difference;
        if (val >= middle){
            difference = val - middle;
            return difference / (middle - minVal);
        }
        difference = middle - val;
        return - difference/ (middle - minVal);
    }


}