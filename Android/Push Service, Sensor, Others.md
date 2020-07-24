Push Service, Sensor, 단말 기능 사용하기
======

<h2>진동 및 소리로 알려주기</h2>

* 사용자에게 무언가를 알려주는 가장 간단한 방법은 진동과 소리를 활용하는 것이다. 안드로이드에서 진동은 얼마동안   
  울리도록 할 것인지 지정할 수 있으며 `Vibrator`라는 시스템 서비스 객체를 사용한다. `Vibrator`에는 `vibrate()` 메소드가 있어   
  진동이 울리는 패턴이나 시간을 지정할 수 있다.
```java
public void vibrate(long milliseconds);
public void vibrate(VibrationEffect vibe);
```

* `vibrate()` 메소드의 파라미터로는 long 자료형의 값으로 진동의 지속 시간을 전달할 수 있는데, 이는 안드로이드 버전 26부터   
  `VibrationEffect`를 파라미터로 전달하는 메소드로 변경되었다. 따라서 이 변경된 내용을 반영하기 위해서는 `Build.VERSION.SDK_INT`   
  상수를 이용해 현재 단말의 버전을 체크하는 과정이 필요하다.

* 소리를 이용해 사용자에게 알려주고 싶다면 `Ringtone` 객체를 사용할 수 있다. 이 객체는 API에서 제공하는 소리를 재생해준다.
```java
public void play()
```

* `Ringtone` 객체의 `play()` 메소드를 호출하면 소리가 울린다. API에서 제공하는 소리를 사용할 수도 있으며, 직접 음원 파일을 만들어   
  재생할 수도 있다. 음원 파일을 만들어 재생할 때는 `MediaPlayer` 객체를 사용한다.

* 아래와 같이 3개의 버튼으로 구성된 `activity_main.xml`이 있다고 하자.
```xml
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    tools:context=".MainActivity">

    <Button
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="VIBRATE!"
        android:id="@+id/button" />
    <Button
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="MAKE SOUND"
        android:id="@+id/button2" />
    <Button
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="MAKE SOUND W/FILE"
        android:id="@+id/button3" />
</LinearLayout>
```

* id가 button인 버튼이 클릭됐을 땐 진동이 울리도록 하고, id가 button2인 버튼이 클릭됐을 땐 API가 제공하는 소리를, 그리고   
  id가 button3인 버튼이 클릭됐을 땐 직접 프로젝트에 추가한 음원 파일이 재생되도록 `MainActivity.java`를 구현해보자.
```java
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Vibrator vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
                
                // 버전 비교 (vibrate() 메소드의 파라미터 값 비교 위해)
                if(Build.VERSION.SDK_INT >= 26) {
                    vibrator.vibrate(VibrationEffect.createOneShot(1000, 10));
                } else {
                    vibrator.vibrate(100);
                }
            }
        });
        
        Button button2 = findViewById(R.id.button2);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                Ringtone ringtone = RingtoneManager.getRingtone(getApplicationContext(), uri);
                ringtone.play();
            }
        });
        
        Button button3 = findViewById(R.id.button3);
        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MediaPlayer player = MediaPlayer.create(getApplicationContext(), R.raw.beep);
                player.start();
            }
        });
    }
}
```

* id:button이 클릭됐을 땐 먼저 `Vibrator`객체를 참조한다. 이 객체의 `vibrate()` 메소드의 파라미터값이 안드로이드 버전 26부터   
  변경되었다 했으므로 버전에 알맞게 `vibrate()` 메소드의 인자값을 지정해준다.
* id:button2이 클릭됐을 때는 API에서 제공하는 기본 음원을 제공하는데, `Ringtone`객체는 `RingtoneManager.getRingtone()` 메소드를   
  이용해 참조할 수 있으며 `Uri`객체를 전달하면 지정한 음원을 갖는 `Ringtone`객체를 참조할 수 있다. 위 코드에서는   
  `TYPE_NOTIFICATION` 상수로 지정한 음원을 가진 `rINGTONE`객체를 참조했다.
* id:button3이 클릭됐을 때는 직접 지정한 음원을 재생했다. 위 코드에서는 `app/res/raw` 하위에 있는 `beep`라는 파일을 지정했다.   
  이렇게 지정 음원을 선택할 때, __해당 음원의 이름은 모두 소문자로 작성__ 되어 있어야 한다.

* 진동을 울리기 위해서는 `VIBRATE` 권한이 필요한데, `AndroidManifest.xml`에 아래 권한을 추가하자.
```xml
<uses-permission android:name="android.permission.VIBRATE"/>
```
<hr/>

<h2>상단 알림으로 알려주기</h2>

* 
