package com.example.butterkinftool;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.annotationlib.BindView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;

public class MainActivity extends AppCompatActivity {

    @ViewBind(R.id.hello_world)
    AppCompatTextView appCompatTextView;
    @BindView(R.id.hello_world_1)
    AppCompatTextView helloWorld1;
    @BindView(R.id.hello_world_2)
    AppCompatTextView helloWorld2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ViewInjust.bind(this);
        ButterKnife.bind(this);
        helloWorld1.setText("xxxxxxxxx");
        helloWorld2.setText("123");
    }

    @OnClick(R.id.hello_world)
    public void viewClick(View v) {
        Toast.makeText(this, "vvvvv=" + v.getId(), Toast.LENGTH_LONG).show();
    }
}
