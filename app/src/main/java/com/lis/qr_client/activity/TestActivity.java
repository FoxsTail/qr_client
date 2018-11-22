package com.lis.qr_client.activity;

import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import com.lis.qr_client.R;

public class TestActivity extends AppCompatActivity {
    ConstraintLayout constraintLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test);

      //  View view = LayoutInflater.from(this).inflate(R.layout.test,null);

        constraintLayout = findViewById(R.id.layout_test);
        setDrawable();
    }

    public void setDrawable() {
        constraintLayout.setBackgroundResource(R.drawable.dark_foreground);
    }
}
