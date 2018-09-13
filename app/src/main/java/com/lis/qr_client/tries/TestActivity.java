package com.lis.qr_client.tries;

import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.lis.qr_client.R;
import lombok.extern.java.Log;

import java.util.Map;
@Log
public class TestActivity extends AppCompatActivity {

    Handler handler;

    String[] position = {"Программер", "Бухгалтер", "Программер",
            "Программер", "Бухгалтер", "Директор", "Программер", "Охранник"};
    int salary[] = {13000, 10000, 13000, 13000, 10000, 15000, 13000, 8000};

    int[] colors = new int[2];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        log.info("---Oncreate---");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                log.info("---Load adr handler callback---");
            }
        };


        colors[0] = Color.parseColor("#559966CC");
        colors[1] = Color.parseColor("#55336699");

        LinearLayout l1 = findViewById(R.id.lin1);

        LayoutInflater inflater = getLayoutInflater();

        for(int i=0; i<position.length; i++ ){
            View item = inflater.inflate(R.layout.text, l1, false);
            TextView tv1 = item.findViewById(R.id.tv1);
            tv1.setText(position[i]);
            TextView tv2 = item.findViewById(R.id.tv2);
            tv2.setText(String.valueOf(salary[i]));

            item.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
            item.setBackgroundColor(colors[i%2]);
            l1.addView(item);
        }

        log.info("---End oncreate---");

    }

    @Override
    protected void onStart() {
        log.info("---On start---");

        super.onStart();
        Thread thread = new Thread(runLoadAddress);

        thread.start();
    }

    Runnable runLoadAddress = new Runnable() {
        @Override
        public void run() {
            log.info("---Loading adresses---");
            System.out.println("------Some work-----");
            handler.sendEmptyMessage(1);

        }
    };
}
