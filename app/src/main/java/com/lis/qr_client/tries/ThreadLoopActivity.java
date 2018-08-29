package com.lis.qr_client.tries;

import android.os.Handler;
import android.os.HandlerThread;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;
import com.lis.qr_client.R;

import java.util.concurrent.TimeUnit;

public class ThreadLoopActivity extends AppCompatActivity {

    private Handler handler = new Handler();
    private MyThreadHandler myThreadHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thread_loop);

        myThreadHandler = new MyThreadHandler("MyThreadHandler");

        Runnable task = new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < 4; i++) {
                    try {
                        TimeUnit.SECONDS.sleep(2);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    if (i == 2) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(ThreadLoopActivity.this,
                                        "I am at the middle of background task",
                                        Toast.LENGTH_LONG)
                                        .show();
                            }
                        });
                    }

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(ThreadLoopActivity.this,
                                    "Background task is completed",
                                    Toast.LENGTH_LONG)
                                    .show();
                        }
                    });
                }
            }
        };

        myThreadHandler.start();
        myThreadHandler.prepareHandler();
        myThreadHandler.postTask(task);
        myThreadHandler.postTask(task);


    }

    @Override
    protected void onDestroy() {
        myThreadHandler.quit();
        super.onDestroy();
    }

    class MyThreadHandler extends HandlerThread {
        private Handler inMyThreadHandler;

        public MyThreadHandler(String name) {
            super(name);
        }

        public void postTask(Runnable task) {
            inMyThreadHandler.post(task);
        }

        public void prepareHandler() {
            inMyThreadHandler = new Handler(getLooper());
        }
    }
}
