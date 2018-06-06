package com.gionee.autotest.traversal.exceptions;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private Button normal, anr, crash, wtf, hello_android, hello_world ;

    private static class BadBehaviorException extends RuntimeException {
        BadBehaviorException() {
            super("Whatcha gonna do, whatcha gonna do",
                    new IllegalStateException("When they come for you"));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        normal = (Button) findViewById(R.id.normal);
        normal.setOnClickListener(this);
        anr = (Button) findViewById(R.id.anr);
        anr.setOnClickListener(this);
        crash = (Button) findViewById(R.id.crash);
        crash.setOnClickListener(this);
        wtf = (Button) findViewById(R.id.wtf);
        wtf.setOnClickListener(this);
        hello_android = (Button) findViewById(R.id.hello_android);
        hello_android.setOnClickListener(this);
        hello_world = (Button) findViewById(R.id.hello_world);
        hello_world.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.normal:

                break ;

            case R.id.anr:
                Log.i("ExceptionTest", "ANR pressed -- about to hang");
                try {
                    Thread.sleep(20000);
                } catch (InterruptedException e){
//                    Log.wtf("ExceptionTest", e);
                }
                Log.i("ExceptionTest", "hang finished -- returning");
                break ;

            case R.id.crash:
                throw new BadBehaviorException();

            case R.id.wtf:
//                Log.wtf("ExceptionTest", "Apps Behaving Badly");
                break ;

            case R.id.hello_android:

                break ;
            case R.id.hello_world:

                break ;
        }
    }
}
