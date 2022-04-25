## 睡半小时实验报告

### 1. 布局与功能设计

#### P1. 进入主界面，展示开始按钮，设置时间按钮

#### P2. 点击开始会记录进入计时线程，记录时间流逝，并切换背景到暗色背景

#### P3. 判定为尚未入睡，重新计时，更换背景（判定方式见下方流程图）

#### P4. 点击设置时间按钮，设置时间，最多24小时

#### （到时间响铃，一分钟未醒调大音量）

<img src="https://s2.loli.net/2022/04/25/p7ihtGZHdkxIq6D.jpg" ><img src="https://s2.loli.net/2022/04/25/npR8gkGJsfEbULY.jpg" >
<img src="https://s2.loli.net/2022/04/25/8WaXtNhrf172yOF.jpg" ><img src="https://s2.loli.net/2022/04/25/kjYCxd4J8cKrBtH.jpg">
### 2. 判断入睡逻辑


<img src="https://s2.loli.net/2022/04/25/xHUOvRIthfsSYl4.png" >


### 3. 代码实现

#### 3.1 协作线程通信关系图解

<img src="https://s2.loli.net/2022/04/25/Lt4imQuP2q3nEAF.png" alt="未命名文件" style="zoom:50%;" />

#### 3.2 关键代码实现

##### 3.2.1 播放音乐，调大音量

```java
// 播放闹钟音乐
MediaPlayer m = null;
if(!alarmStart) {
    m = MediaPlayer.create(MainActivity.this, R.raw.alarm);
    m.setLooping(true);
    m.start();
    alarmStart = true;
}
// 一分钟未醒，获取最大音量，并设置为最大
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
```

##### 3.2.1 获取加速度传感器，并根据阈值做出响应

```java
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
```

##### 3.2.2 获取屏幕状态，做忘记关闭熄屏判断

```java
KeyguardManager mKeyguardManager = (KeyguardManager) getSystemService (Context. KEYGUARD_SERVICE);
boolean flag = mKeyguardManager.inKeyguardRestrictedInputMode();
```

##### 3.3.3 修改背景颜色前需要清空背景，不然会造成颜色叠加

```java
all.setBackgroundResource(0);
all.setBackgroundColor(R.color.sleep);
```

### 4. 总结

这个项目还有很多不成熟的地方，判断入睡逻辑不够完备，并且如果可以根据睡眠时间长度动态响应，会更好。

使用线程过多，对资源有一定的消耗，并且线程间通信逻辑比较混乱，不利于修改。







