package clqwq.press.autosleep;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.KeyguardManager;
import android.app.TimePickerDialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;

public class MainActivity extends AppCompatActivity {

    private int minute;
    private int hour;
    private Button start;
    private TextView timer;
    private boolean userCheck;
    private boolean running;        // 标记时钟是否在计时
    private boolean isReset;
    private boolean unMove;
    private boolean wakeup;
    private int forgetTime;
    private boolean alarmStart;
    private boolean startEnd;
    private RelativeLayout all;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        start = findViewById(R.id.start);
        timer = findViewById(R.id.timer);
        all = findViewById(R.id.all);
        isReset = false;
        hour = 0;
        minute = 30;
        running = false;
        forgetTime = 0;
        unMove = true;
        userCheck = false;
        wakeup = false;
        alarmStart = false;
        startEnd = false;
        addListener();
        MoveThread();
        new ForgetThread().start();
        new PassThread().start();
        new AlarmThread().start();
    }

    // 播放闹钟的线程，如果用户一分钟没有起床，则会将声音调到最大
    class AlarmThread extends Thread {
        @Override
        public void run() {
            alarmStart = false;
            while(true) {
                if(startEnd) {
                    MediaPlayer m = null;
                    if(!alarmStart) {
                        m = MediaPlayer.create(MainActivity.this, R.raw.alarm);
                        m.setLooping(true);
                        m.start();
                        alarmStart = true;
                    }
                    int cnt = 0;
                    while(!wakeup) {
                        try {
                            sleep(1000);
                            cnt++;
                            AudioManager am=(AudioManager)getSystemService(Context.AUDIO_SERVICE);
                            int maxVolume = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                            if(cnt > 60) {
                                am.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume, AudioManager.FLAG_PLAY_SOUND);
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    m.stop();
                    wakeup = false;
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    public void MoveThread() {
        SensorManager sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor s = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        SensorEventListener el = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent e) {
                if(userCheck) {
                    if(e.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
                        float x = e.values[0];
                        float y = e.values[1];
                        float z = e.values[2];
                        if ((Math.abs(x)>12||Math.abs(y)>12||Math.abs(z)>12)) {
                            isReset = true;
                            running = false;
                            unMove = false;
                            start.callOnClick();
                        } else {
                            if(!startEnd) {
                                running = true;
                            }
                        }
                    }
                }
            }
            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {
            }
        };
        sm.registerListener(el,s,SensorManager.SENSOR_DELAY_UI);
    }


    class ForgetThread extends Thread {
        @Override
        public void run() {
            int cnt = 0;
            boolean fall = false;
            while(true) {
                KeyguardManager mKeyguardManager = (KeyguardManager) getSystemService (Context. KEYGUARD_SERVICE);
                boolean flag = mKeyguardManager.inKeyguardRestrictedInputMode();
                // 如果屏幕是亮的，并且没有移动，记录cnt
                if(!fall && flag && unMove) {
                    isReset = true;
                    running = false;
                    cnt++;
                } else {
                    cnt = 0;
                }
                // 如果时间超过10min，则认为睡着
                if (cnt > 600) {
                    running = true;
                    fall = true;
                    forgetTime = 600;
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    class PassThread extends Thread{
        @Override
        public void run() {
            int cnt = 0;
            while(true) {
                try {
                    Thread.sleep(60000);
                    int all = 60 * hour + minute;
                    if(isReset) {
                        isReset = false;
                        cnt = 0;
                    }
                    if (running) {
                        cnt++;
                        int finalCnt = cnt;
                        int temp = all - finalCnt - forgetTime;
                        System.out.println("temp = "+temp );
                        if (temp <= 0) {
//                            new AlarmThread().start();
                            startEnd = true;
                            running = false;
                        }
                        runOnUiThread(new Runnable() {
                            @SuppressLint("SetTextI18n")
                            @Override
                            public void run() {
                                int temp = all - finalCnt;
                                timer.setText(temp / 60 + ":" + temp % 60);
                            }
                        });
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void addListener() {
        // 切换状态
        start.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onClick(View view) {
                userCheck = !userCheck;
                if (userCheck) {
                    start.setText("暂停");
                    all.setBackgroundColor(R.color.sleep);
                } else {
                    start.setText("开始");
                    running = false;
                    isReset = true;
                    all.setBackgroundResource(0);
                }
                if(!wakeup) {
                    wakeup = true;
                }
            }
        });

        timer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new TimePickerDialog(MainActivity.this, AlertDialog.THEME_HOLO_LIGHT,new TimePickerDialog.OnTimeSetListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onTimeSet(TimePicker view, int hour_, int minute_) {
                        running = false;
                        isReset = true;
                        start.setText("开始");
                        hour = hour_;
                        minute = minute_;
                        timer.setText(hour_ + ":" + minute_);
                    }
                }, hour, minute, true).show();
            }
        });
    }

}