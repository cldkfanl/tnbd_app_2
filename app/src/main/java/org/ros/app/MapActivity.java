package org.ros.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.widget.Toolbar;

import org.ros.R;
import org.ros.android.AppCompatRosActivity;
import org.ros.android.BitmapFromCompressedImage;
import org.ros.android.view.RosImageView;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMainExecutor;

import sensor_msgs.CompressedImage;

public class MapActivity extends AppCompatRosActivity {


    private RosImageView<CompressedImage> rosImageView;
    private Toolbar toolbar;
    private Button RvizB,MapB;
    public MapActivity() {
        super("CameraTest", "CameraTest");
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Cmaera");

        rosImageView = (RosImageView<CompressedImage>) findViewById(R.id.image);
        rosImageView.setTopicName("image_raw/compressed");
        rosImageView.setMessageType(sensor_msgs.CompressedImage._TYPE);
        rosImageView.setMessageToBitmapCallable(new BitmapFromCompressedImage());

        RvizB = findViewById(R.id.RvizB);
        RvizB.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(MapActivity.this ,CameraActivity.class);
                startActivity(intent);
            }
        });
        MapB = findViewById(R.id.MapB);
        MapB.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(MapActivity.this ,CameraActivity.class);
                startActivity(intent);
            }
        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_sng, menu);
        return true;
    }


    @Override
    protected void init(NodeMainExecutor nodeMainExecutor){
        NodeConfiguration nodeConfiguration = NodeConfiguration.newPublic(getRosHostname());
        nodeConfiguration.setMasterUri(getMasterUri());
        nodeMainExecutor.execute(rosImageView, nodeConfiguration);

    }

}