package com.robin.gesturelockdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.robin.lib.GestureLockView;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    GestureLockView gestureLockView;

    TextView info;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        info = (TextView) findViewById(R.id.info);

        gestureLockView = (GestureLockView) findViewById(R.id.lockview);
        gestureLockView.reset();
        gestureLockView.setGestureLockListener(new GestureLockView.GestureLockListener() {
            @Override
            public void onNext(List<Integer> lockArray) {
                Toast.makeText(MainActivity.this, "设置手势密码成功", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(List<Integer> lockArray, int errCode) {
                switch (errCode) {
                    case GestureLockView.OUT_OF_MAX_LOCK_LENGTH:
                        Toast.makeText(MainActivity.this, "超过允许最大长度", Toast.LENGTH_LONG).show();
                        break;
                    case GestureLockView.LOWER_THAN_MIN_LOCK_LENGTH:
                        Toast.makeText(MainActivity.this, "未达到最小长度", Toast.LENGTH_LONG).show();
                        break;
                    case GestureLockView.REPEAT_NOT_ALLOWED:
                        Toast.makeText(MainActivity.this, "不支持重复的手势密码", Toast.LENGTH_LONG).show();
                        break;
                }
            }
        });


        findViewById(R.id.reset).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gestureLockView.reset();
            }
        });
    }
}
