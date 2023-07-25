package org.ros.android.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import org.ros.android.MessageCallable;
import org.ros.message.MessageListener;
import org.ros.namespace.GraphName;
import org.ros.node.ConnectedNode;
import org.ros.node.Node;
import org.ros.node.NodeMain;
import org.ros.node.topic.Subscriber;

import geometry_msgs.PoseWithCovarianceStamped;

public class RosMapView<T> extends androidx.appcompat.widget.AppCompatImageView implements NodeMain {

    private String topicName;
    private String messageType;
    private MessageCallable<Bitmap, T> callable;
    private ScaleGestureDetector scaleGestureDetector;


    public RosMapView(Context context) {
        super(context);
        scaleGestureDetector = new ScaleGestureDetector(context, new ScaleListener());
    }

    public RosMapView(Context context, AttributeSet attrs) {
        super(context, attrs);
        scaleGestureDetector = new ScaleGestureDetector(context, new ScaleListener());
    }

    public RosMapView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        scaleGestureDetector = new ScaleGestureDetector(context, new ScaleListener());
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public void setMessageToBitmapCallable(MessageCallable<Bitmap, T> callable) {
        this.callable = callable;
    }

    @Override
    public GraphName getDefaultNodeName() {
        return GraphName.of("ros_image_view");
    }

    @Override
    public void onStart(ConnectedNode connectedNode) {
        Subscriber<T> subscriber = connectedNode.newSubscriber(topicName, messageType);
        subscriber.addMessageListener(new MessageListener<T>() {
            @Override
            public void onNewMessage(final T message) {
                post(new Runnable() {
                    @Override
                    public void run() {
                        setImageBitmap(callable.call(message));
                    }
                });
                postInvalidate();
            }
        });
        Subscriber<geometry_msgs.PoseWithCovarianceStamped> amclposeSubscriber =
                connectedNode.newSubscriber("amcl_pose", PoseWithCovarianceStamped._TYPE);
        amclposeSubscriber.addMessageListener(new MessageListener<geometry_msgs.PoseWithCovarianceStamped>() {
            @Override
            public void onNewMessage
                    (PoseWithCovarianceStamped poseWithCovarianceStamped ) {
                final geometry_msgs.Point position = poseWithCovarianceStamped.getPose ().getPose().getPosition ();
                final double x = position.getX();
                final double y = position.getY();
                shouldDrawPoint =true;
                pointX = (float) (((float)x + 10.5)*getWidth ()/20);
                pointY = (float) (getHeight
                        ()-((float)y+10.5)*getHeight ()/20);
            }
        });
    }
    private float pointX , pointY ;
    private boolean shouldDrawPoint = false;

    @Override
    protected void onDraw (Canvas canvas) {
        super.onDraw (canvas);
        if ((shouldDrawPoint )) {
            Paint paint = new Paint();
            paint.setColor (android.graphics.Color.RED );
            paint.setStyle (Paint.Style.FILL );
            float cx = pointX ;
            float cy = pointY ;
            canvas.drawCircle (cx, cy, 5, paint);
        }
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        private float scaleFactor = 1.f;

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            scaleFactor *= detector.getScaleFactor();
            scaleFactor = Math.max(0.1f, Math.min(scaleFactor
                    , 10.0f));
            setScaleX(scaleFactor);
            setScaleY(scaleFactor);
            return true;
        }
    }



    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean handled = scaleGestureDetector.onTouchEvent(event);

        return handled || super.onTouchEvent(event);

    }
    @Override
    public void onShutdown(Node node) {
    }

    @Override
    public void onShutdownComplete(Node node) {
    }

    @Override
    public void onError(Node node, Throwable throwable) {
    }

}

