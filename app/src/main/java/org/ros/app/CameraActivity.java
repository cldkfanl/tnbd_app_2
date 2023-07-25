package org.ros.app;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;

import org.ros.R;
import org.ros.android.AppCompatRosActivity;
import org.ros.android.BitmapFromCompressedImage;
import org.ros.android.view.RosImageView;
import org.ros.namespace.GraphName;
import org.ros.node.ConnectedNode;
import org.ros.node.DefaultNodeMainExecutor;
import org.ros.node.Node;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMain;
import org.ros.node.NodeMainExecutor;
import org.ros.node.topic.Publisher;

import sensor_msgs.CompressedImage;
import std_msgs.String;
import geometry_msgs.Twist;

public class CameraActivity  extends AppCompatRosActivity {

    private RosImageView<CompressedImage> camera_View;
    private Publisher<Twist> cmd_publisher;
    private Toolbar toolbar;
    private ImageButton button_up, button_down, button_left, button_right, button_re;
    private Button RvizB,MapB;
    private Handler handler;
    private Runnable publishRunnable;
    private Switch switch_OnOff;
    private TextView text_OnOff;
    private double linearVel = 0.0;
    private double angularVel = 0.0;


    public CameraActivity(){
        super("ROS Example", "ROS Example");
    }

    @SuppressLint("WrongViewCast")
    @Override
    protected  void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        camera_View = findViewById(R.id.image_View);
        camera_View.setTopicName("image_raw/compressed");
        camera_View.setMessageType(sensor_msgs.CompressedImage._TYPE);
        camera_View.setMessageToBitmapCallable(new BitmapFromCompressedImage());

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Cmaera");

        RvizB = findViewById(R.id.RvizB);
        RvizB.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(CameraActivity.this ,MapActivity.class);
                startActivity(intent);
            }
        });
        MapB = findViewById(R.id.MapB);
        MapB.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(CameraActivity.this ,MapActivity.class);
                startActivity(intent);
            }
        });

        button_up = findViewById(R.id.button_up);
        button_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(switch_OnOff.isChecked()){
                    linearVel += 0.01;
                    startPublishingCmdVel();
                }
            }
        });

        button_down = findViewById(R.id.button_down);
        button_down.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(switch_OnOff.isChecked()){
                    linearVel -= 0.01;
                    startPublishingCmdVel();
                }
            }
        });

        button_left = findViewById(R.id.button_left);
        button_left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(switch_OnOff.isChecked()){
                    angularVel += 0.1;
                    startPublishingCmdVel();
                }
            }
        });

        button_right = findViewById(R.id.button_right);
        button_right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(switch_OnOff.isChecked()) {
                    angularVel -= 0.1;
                    startPublishingCmdVel();
                }
            }
        });

        button_re = findViewById(R.id.button_re);
        button_re.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(switch_OnOff.isChecked()) {
                    angularVel = 0.0;
                    linearVel = 0.0;
                    startPublishingCmdVel();
                }
            }
        });

        text_OnOff = findViewById(R.id.text_OnOff);
        switch_OnOff = findViewById(R.id.switch_OnOff);
        switch_OnOff.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if(isChecked){
                    text_OnOff.setText("ON");
                    angularVel = 0.0;
                    linearVel = 0.0;
                    startPublishingCmdVel();
                }
                else{
                    text_OnOff.setText("OFF");
                    stopPublishingCmdVel();
                }
            }
        });

        handler = new Handler(Looper.getMainLooper());
        publishRunnable = new Runnable() {
            @Override
            public void run() {
                publishCmdVel(linearVel, angularVel);
                handler.postDelayed(this, 100); // 0.1초(100ms)마다 호출
            }
        };

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_sng, menu);
        return true;
    }

    private void stopPublishingCmdVel(){
        handler.removeCallbacks(publishRunnable);
    }

    private void startPublishingCmdVel() {
        handler.removeCallbacks(publishRunnable); // 기존에 실행 중인 publish 작업을 중지
        handler.post(publishRunnable); // 새로운 publish 작업 시작
    }

    private void publishCmdVel(double linearVel, double angularVel) {
        if (cmd_publisher != null) {
            Twist cmdVelMsg = cmd_publisher.newMessage();
            cmdVelMsg.getLinear().setX(linearVel);
            cmdVelMsg.getAngular().setZ(angularVel);
            cmd_publisher.publish(cmdVelMsg);
        }
    }

    @Override
    protected void init(NodeMainExecutor nodeMainExecutor){

        NodeConfiguration nodeConfiguration = NodeConfiguration.newPublic(getRosHostname());
        nodeConfiguration.setMasterUri(getMasterUri());
        nodeMainExecutor.execute(camera_View, nodeConfiguration);

        NodeMain nodeMain = new NodeMain(){
            @Override
            public GraphName getDefaultNodeName() {
                return GraphName.of("camera_node");
            }
            @Override
            public void onStart(ConnectedNode connectedNode) {
                cmd_publisher = connectedNode.newPublisher("turtle1/cmd_vel", Twist._TYPE);
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
        };
        nodeMainExecutor.execute(nodeMain, nodeConfiguration);
    }



}
