위치 기반 서비스와 App Widget 사용하기
======

<h2>GPS로 위치 확인하기</h2>

* 안드로이드 기반의 스마트폰 위치는 `LocationManager`라는 시스템 서비스가 관리한다. 실제로 `android.location` 패키지에는   
  이 클래스들을 포함하여 위치 정보를 확인하거나 획인된 위치 정보를 사용하는데 필요한 클래스들이 정의되어 있다.

* 현재 단말의 위치를 확인하는 가장 기본적인 방법은 위치 관리자에게 위치 정보를 요청하는 것으로, 아래 단계를 거친다.
  1. `위치 관리자 객체(LocationManager)` 참조하기 : 시스템 서비스로 제공되므로, `getSystemService()`를 호출하여 참조한다.
  2. `위치 리스너(LocationListener)` 구현 : `LocationManager`가 알려주는 현재 위치는 `LocationListener`를 통해 받게 되므로   
    새로운 리스너를 구현하여 전달받은 위치 정보를 처리한다.
  3. 위치 정보 업데이트 요청하기 : `LocationManager`에게 위치 정보가 변경될 때마다 알려달라고 요청하기 위해   
    `requestLocationUpdates()` 메소드를 호출한다. 이 메소드의 파라미터로는 `LocationListener`객체가 들어간다.
  4. Manifest에 권한 추가하기 : GPS를 사용할 수 있도록 `AndroidManifest.xml`에 ACCESS_FINE_LOCATION 권한을 추가하고,   
    위험권한을 위한 설정과 코드를 추가한다.

* 내 위치를 확인하는 기능을 구현해보자. 먼저 아래는 `activity_main.xml` 코드이다.
```xml
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    tools:context=".MainActivity">
    
    <Button
        android:id="@+id/button"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_gravity="center_horizontal"
        android:textSize="20sp"
        android:text="Check my location." />
    
    <TextView
        android:id="@+id/textView"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:text="Location appears here."
        android:textSize="20sp" />
</LinearLayout>
```

<hr/>

<h3>#1. 위치 관리자 객체 참조하기</h3>

* `위치관리자(LocationManager)` 객체는 시스템 서비스이므로 참조하기 위해 `getSystemService()` 메소드를 호출해야 한다.   
  버튼 클릭 시 `startLocationService()` 메소드가 호출되도록 하자. `startLocationService()`는 내 위치를 확인하는 메소드이다.
```java
public class MainActivity extends AppCompatActivity {
    
    TextView textView;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        textView = findViewById(R.id.textView);

        Button button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startLocationService();
            }
        });
    }
    
    public void startLocationService() {
        // 아래처럼 LocationManager 객체를 참조한다.
        LocationManager manager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        
        try {
            Location location = manager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if(location != null) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                String message = "Recent location -> Latitude : " + latitude + "\nLongitude : " + longitude;
                textView.setText(message);
            }
        } catch(SecurityException e) {
            e.printStackTrace();
        }
    }
}
```
* `LocationManager`객체를 참조하기 위해 `getSystemService()`에 전달한 상수는 `Context.LOCATION_SERVICE` 이다.   
  최근 위치 정보를 확인하기 위해 사용하는 `getLastKnownLocation()` 메소드에는 위치 정보를 제공하는 위치 제공자인   
  `LocationProvider`를 전달한다. 안드로이드는 위치 제공자를 크게 `GPS_PROVIDER`와 `NETWORK_PROVIDER`로 구분하고 있으며, 이 둘중   
  하나를 파라미터로 전달하면 된다. 실제 app에서는 대부분 GPS를 이용하므로 GPS_PROVIDER를 전달하면 `Location`객체가 반환된다.   
  `Location`객체는 위도, 경도값을 가지며 `getLatitude()`와 `getLongitude()` 메소드로 각각의 값을 참조할 수 있다.

* 다음으로는 `AndroidManifest.xml`에 위치 확인을 위해 필요한 권한을 추가해주자.
```xml
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>
```
<hr/>

<h3>#2. LocationListener 구현하기</h3>

* `LocationListener`는 `LocationManager`에서 전달하는 위치 정보를 받기 위해 정의된 인터페이스이다. 즉 `LocationManager`가   
  위치 정보를 전달할 때 호출되므로, 위치 정보를 받아 처리하려면 이 리스너의 `onLocationChanged()` 메소드를 구현해야 한다.   
  `MainActivity.java`에 `LocationListener`를 구현하는 클래스를 내부 클래스로 정의하자.
```java
public class MainActivity extends AppCompatActivity {

    //..

    class GPSListener implements LocationListener {
        @Override
        public void onLocationChanged(Location location) {
            Double latitude = location.getLatitude();
            Double longitude = location.getLongitude();
            String message = "My location -> Latitude : " + latitude + "\nLongitude : " + longitude;
            textView.setText(message);
        }

        @Override
        public void onProviderEnabled(String s) { }

        @Override
        public void onProviderDisabled(String s) { }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) { }
    }
}
```
<hr/>

<h3>#3. 위치 정보 업데이트 요청하기</h3>

* `LocationManager`는 일정한 시간을 간격으로 위치 정보를 확인하거나, 일정 거리 이상을 이동했을 때 위치 정보를 전달하는   
  기능을 제공한다. 위치 관리자에게 현재 위치를 알려달라고 요청하기 위해서는 `requestLocationUpdates()` 메소드를 호출해야하는데,   
  파라미터로는 최소시간과 최소 거리, 그리고 `LocationListener` 객체가 전달되어야 한다. `MainActivity.java`를 아래와 같이 수정하자.
```java
public class MainActivity extends AppCompatActivity {

    //..  

    public void startLocationService() {
        // 아래처럼 LocationManager 객체를 참조한다.
        LocationManager manager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

        try {
            Location location = manager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if(location != null) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                String message = "Recent location -> Latitude : " + latitude + "\nLongitude : " + longitude;
                textView.setText(message);
            }
            
            GPSListener gpsListener = new GPSListener();
            long minTime = 1000;
            float minDistance = 0;
            
            manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTime, minDistance, gpsListener);
            Toast.makeText(getApplicationContext(), "My location check requested.", Toast.LENGTH_LONG).show();
        } catch(SecurityException e) {
            e.printStackTrace();
        }
    }
}
```
<hr/>

<h3>#4. 위험 권한을 위한 코드 추가</h3>

* GPS를 이용해 위치 정보를 받기 위한 권한은 `ACCESS_FINE_LOCATION`으로 정의되어 있으므로, 위에서 `AndroidManifest.xml`에   
  이 권한을 추가했다. 그런데 이 권한은 위험 권한으로 분류되어 있어, 위험 권한을 부여하는 설정과 코드를 추가해야 한다.

* 먼저 `build.gradle(Module:app)`에 `AutoPermissions`라이브러리를 위한 의존 코드를 추가해주자.
```gradle
allprojects {
    repositories {
        maven{url 'https://jitpack.io'}
    }
}
dependencies {
    implementation 'com.github.pedroSG94:AutoPermissions:1.0.3'
}
```

* 위험 권한 부여 코드를 추가한 최종적인 `MainActivity.java`는 아래와 같다.
```java
public class MainActivity extends AppCompatActivity implements AutoPermissionsListener {

    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = findViewById(R.id.textView);

        Button button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startLocationService();
            }
        });

        AutoPermissions.Companion.loadAllPermissions(this, 101);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        AutoPermissions.Companion.parsePermissions(this, requestCode, permissions, this);
    }
    
    @Override
    public void onDenied(int requestCode, @NonNull String[] permissions) {
        Toast.makeText(this, "Permission denied : " + permissions.length, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onGranted(int i, String[] strings) {
        Toast.makeText(this, "Permission granted : " + strings.length, Toast.LENGTH_LONG).show();
    }

    public void startLocationService() {
        // 아래처럼 LocationManager 객체를 참조한다.
        LocationManager manager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

        try {
            Location location = manager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if(location != null) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                String message = "Recent location -> Latitude : " + latitude + "\nLongitude : " + longitude;
                textView.setText(message);
            }

            GPSListener gpsListener = new GPSListener();
            long minTime = 1000;
            float minDistance = 0;

            manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTime, minDistance, gpsListener);
            Toast.makeText(getApplicationContext(), "My location check requested.", Toast.LENGTH_LONG).show();
        } catch(SecurityException e) {
            e.printStackTrace();
        }
    }

    class GPSListener implements LocationListener {
        @Override
        public void onLocationChanged(Location location) {
            Double latitude = location.getLatitude();
            Double longitude = location.getLongitude();
            String message = "My location -> Latitude : " + latitude + "\nLongitude : " + longitude;
            textView.setText(message);
        }

        @Override
        public void onProviderEnabled(String s) { }

        @Override
        public void onProviderDisabled(String s) { }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) { }
    }
}
```