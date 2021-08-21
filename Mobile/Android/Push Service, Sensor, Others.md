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

* 알림은 `NotificationManager` 시스템 서비스 객체를 이용해 화면 상단에 띄울 수 있다. 알림을 띄우려면 `Notification` 객체를   
  만들어야 하는데, 이 객체는 `NotificationCompat.Builder`객체를 이용해서 만들 수 있다.   
  우선 가장 간단한 알림을 만드는 방법에 대해 알아보자.

* `activity_main.xml`에 id가 button인 Button객체가 하나 있고, 이 버튼을 누르면 알림이 화면 상단에 띄워지도록 구현해보자.   
  아래는 `MainActivity.java` 이다.
```java
public class MainActivity extends AppCompatActivity {
    
    NotificationManager manager;
    
    private static String CHANNEL_ID = "channel1";
    private static String CHANNEL_NAME = "Channel1";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showNoti1();
            }
        });
    }
    
    public void showNoti1() {
        manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = null;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if(manager.getNotificationChannel(CHANNEL_ID) != null) {
                manager.createNotificationChannel(new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT));
                builder = new NotificationCompat.Builder(this, CHANNEL_ID);
            }
        } else {
            builder = new NotificationCompat.Builder(this);
        }
        
        builder.setContentTitle("Simple alert.");
        builder.setContentText("Alert message.");
        builder.setSmallIcon(android.R.drawable.ic_menu_view);
        Notification noti = builder.build();
        
        manager.notify(1, noti);
    }
}
```
* 버튼 클릭 시에는 `showNoti1()` 메소드를 호출하는데, 이 메소드 안에서는 `NotificationManager`객체를 참조한 후   
  `NotificationCompat.Builder` 객체를 생성한다. 하지만 `Builder`객체를 만드는 방식이 Android Oreo 버전을 기준으로 달라져서   
  버전 비교도 진행했다. Oreo 이후 버전에는 (if문 내) 알림 채널이 지정되어야 하며, 채널은 `createNotificationChannel()` 메소드로   
  생성할 수 있다. `Builder`객체가 만들어지면 알림 정보를 설정할 수 있는데, `setContentTitle()` 메소드는 알림의 제목,   
  `setContentText()`는 알림 메시지를, 그리고 `setSmallIcon()` 메소드는 아이콘 설정에 사용된다. 이렇게 `Builder`객체에 설정을 해주고,   
  `build()` 메소드를 호출하면 `Notification`객체가 반환된다. 마지막으로 `NotificationManager#notify()` 메소드를 호출하면서 이   
  `Notification`객체를 전달하면 알림을 띄우게 된다.

* 다음으로는 알림을 띄우고, 어떠한 동작을 하도록 만들어보자. `activity_main.xml`에 id가 button2인 Button을 하나 더 추가한다.   
  이 버튼을 눌렀을 때는 `PendingIntent`를 만들어 `Nofitication`객체를 만들 때 설정할 것이다. `PendingIntent`는 `Intent`와   
  유사하지만 __시스템에서 대기하는 역할 을 한다. 그리고 원하는 상황이 됐을 때 시스템에 의해 해석되고 처리된다__.   
  예를들어 Activity를 띄우는 역할을 하는 메소드가 `startActivity()` 또는 `startActivityForResult()`인데,   
  이 메소드를 호출하면 시스템에서는 즉시 해석하고 처리한다. 하지만 `PendingIntent`는 지정된 상황이 될때까지 보관하고 있는다.

* `MainActivity.java`에 두 번째 버튼을 눌렀을 때 실행될 코드를 추가하자.
```java
public class MainActivity extends AppCompatActivity {

    //..
    
    private static String CHANNEL_ID2 = "channel2";
    private static String CHANNEL_NAME2 = "Channel2";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //..

        Button button2 = findViewById(R.id.button2);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showNoti2();
            }
        });
    }

    //..

    ublic void showNoti2() {
        manager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        
        NotificationCompat.Builder builder = null;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if(manager.getNotificationChannel(CHANNEL_ID2) == null) {
                manager.createNotificationChannel(new NotificationChannel(CHANNEL_ID2, CHANNEL_NAME2, NotificationManager.IMPORTANCE_DEFAULT));
                builder = new NotificationCompat.Builder(this, CHANNEL_ID2);
            }
        } else {
            builder = new NotificationCompat.Builder(this);
        }

        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 101, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        builder.setContentTitle("Simple alert2.");
        builder.setContentText("Alert message 2");
        builder.setSmallIcon(android.R.drawable.ic_menu_view);
        builder.setAutoCancel(true);
        builder.setContentIntent(pendingIntent);
        
        Notification noti = builder.build();
        manager.notify(2, noti);
    }
}
```

* id:button2를 누르면 `showNoti2()` 메소드가 호출되는데, 이 메소드 안에서 실행되는 코드는 `showNoti1()` 메소드와   
  거의 동일하지만 채널을 지정하는 값이 다르다. 그리고 `Builder`를 생성할 때 `setAutoCancel()`와 `setContentIntent()`   
  메소드를 추가로 호출했는데, `setAutoCancel()`은 알림을 클릭했을 때 자동으로 알림 표시를 삭제하라는 설정이며,   
  `setContenIntent()` 메소드에는 `PendingIntent`객체가 파라미터로 전달된다. 그리고 `PendingIntent`는 생성할 때   
  `Intent`객체가 파라미터로 전달되는데, 이렇게 하면 알림을 클릭했을 때 이 `Intent`객체를 이용해 Activity를 띄워준다.   
  이 Activity는 위 코드에서 MainActivity.class를 설정하여 MainActivity로 설정했다.

* 알림을 표시하는 방법은 글자를 표시하거나 이미지를 표시하거나, 목록을 표시하는 방법 등이 있다.   
  이들을 `Styled Notification`이라 부른다. 예를 들어 글자를 많이 표시하는 스타일의 알림을 만들기 위해서는   
  `NotificationCompat.BigTextStyle` 객체를 만들고 `Builder`의 `setStyle()` 메소드를 이용해 설정한다.   
  아래는 예시 코드이다.
```java
NotificationCompat.BigTextStyle style = new NotificationCompat.BigTextStyle();
style.bigText("VERY VERY VERY VERY VERY VERY VERY VERY VERY LONG TEXT!!!!!!");
style.setBigContentTitle("THIS IS TITLE");
style.setSummaryText("This is summary.");

NotificationCompat.Builder builder2 = new NotificationCompat.Builder(this, "channel3").setContentTitle("ALERT TITLE")
    .setContentText("Alert content").setSmallIcon(android.R.drawable.ic_menu_send).setStyle(style);

Notification noti = builder2.build();
manager.notify(3, noti);
```

* `InboxStyle`로 만든 알림은 여러 줄의 텍스트를 보여준다. 보통 이메일 목록 등을 표시할 때 사용한다.
* `MessagingStyle`로 만든 알림은 여러 줄의 메시지를 보여준다. 사진도 보여줄 수 있어 카카오톡 등의 메시징앱에 사용된다.
* `BigPictureStyle`은 큰 이미지를 보여주고 싶을 때 사용한다.
<hr/>

<h2>시스템 서비스 활용하기</h2>

* System Service는 단말이 켜졌을 때 자동으로 실행되어 백그라운드에서 동작한다. 시스템 서비스들 중 `ActivityManager`,   
  `PackageManager`, `AlarmManager`에 대해 알아보자.

* `ActivityManager`는 activity나 서비스를 관리하는 시스템 서비스로, app의 실행 상태를 알 수 있도록 한다.   
  `PackageManager`는 app의 설치에 대한 정보를 알 수 있도록 하며, `AlarmManager`는 일정 시간에 알림을 받을 수 있도록   
  시스템에 등록해주는 역할을 한다.
<hr/>