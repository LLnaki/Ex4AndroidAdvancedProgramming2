package com.example.ex4android;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.view.View;
import android.content.Intent;
public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }
    public void goToJoystick(View view ) {
        EditText ipEditText = (EditText)findViewById(R.id.ipEditText);
        EditText portEditText = (EditText)findViewById(R.id.portEditText);
        Intent intent = new Intent(this, JoystickActivity.class);

        String ip = ipEditText.getText().toString();
        String port = portEditText.getText().toString();
        intent.putExtra("ip",ip);
        intent.putExtra("port",port);
        startActivity(intent);
    }

}
