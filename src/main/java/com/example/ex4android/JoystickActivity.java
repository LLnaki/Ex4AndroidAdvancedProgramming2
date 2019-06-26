package com.example.ex4android;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;

import android.os.Bundle;


/**
 * This class is a Joystick activity.
 */
public class JoystickActivity extends AppCompatActivity {
    //This client will be connected to a server of the simulator.
    private TcpClient mTcpClient;
    //View for this activity.
    private JoystickView view;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_joystick);

        Intent intent = getIntent();
        String ip = intent.getStringExtra("ip");
        int port = Integer.parseInt(intent.getStringExtra("port"));
        //Creating a connecting task, which will be responsible for communication with a server.
        ConnectTask connectTask = new ConnectTask(ip, port);
        //Starting a communication(connection to a server as client) with a server.
        connectTask.execute("");
        /*
        Getting created by connectTask TcpClient,
        which we will use only for sending messages to the server.
         */
        mTcpClient = connectTask.getTcpClient();
        view = new JoystickView(this, mTcpClient);
        setContentView(this.view);
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        //on destroy we break up the connection between the joystick and the simulator.
        mTcpClient.stopClient();
    }

}


