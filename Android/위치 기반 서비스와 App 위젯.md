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
<hr/>

<h2>현재 위치의 지도 보여주기</h2>

* 안드로이드는 app 화면 내에 지도를 넣을 수 있도록 `MapFragment`가 제공된다. 이는 새로운 방식의 GoogleMap Service v2 기능을   
  사용할 수 있도록 추가된 기능으로 Google Play Service 모듈을 사용한다. 다음은 XML Layout에 MapFragment를 추가해서 지도를   
  보여줄 때 필요한 과정이다.
  * Google Play Services 라이브러리 사용 설정하기
  * XML Layout에 MapFragment 추가
  * 소스코드에서 내 위치로 지도 이동시키기
  * Manifest에 권한 설정 추가하기
  * 지도 API Key 발급받기
<hr/>

<h3>Google Play Services 라이브러리 사용 설정하기</h3>

* 안드로이드 스튜디오의 SDK Manager에서 Android SDK 선택 후 SDK Tools에서 Google Play Services 모듈을 설치한다.
<hr/>

<h3>XML Layout에 MapFragment 추가하기</h3>

* `File -> Project Structure -> Modules -> app` 에서 `+` 버튼 클릭 후 `Library Dependency`를 선택하고,   
  `com.google.android.gms:play-services-maps` 항목을 선택한다.

* 이후 `activity_main.xml`에 지도를 보여주기 위한 fragment를 추가하자.
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
        android:id="@+id/button"
        android:textSize="Request my location" />
    
    <fragment
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:id="@+id/map"
        class="com.google.android.gms.maps.SupportMapFragment" />

</LinearLayout>
```

* Fragment는 View처럼 화면의 일정 영역을 할당받게 되며, `<fragment>` 태그를 사용한다. Fragment에서 할당받은 화면 영역에   
  보이는 것은 class 속성으로 지정된 클래스인데, 위 코드에서는 `SupportMapFragment` 클래스가 사용된다.
<hr/>

<h3>소스 코드에서 내 위치로 지도 이동시키기</h3>

* 지도를 보여주기 위해 XML Layout에 추가한 fragment는 class 속성으로 `SupportMapFragment`가 할당되어 있다.   
  이 객체는 소스 코드에서 참조할 수 있으며, `SupportMapFragment` 안에 있는 `GoogleMap` 객체 위에 지도가 표시된다.   
  `GoogleMap` 객체는 fragment가 초기화된 이후에 참조할 수 있는데, XML Layout에 정의한 `SupportMapFragment`를 참조한 후   
  `getMapAsync()` 메소드를 호출하면 `GoogleMap` 객체를 참조할 수 있다. `getMapAsync()` 메소드는 내부적으로 지도를 다루는   
  `GoogleMap` 객체를 초기화하는데, 이를 비동기 방식으로 처리한다. 따라서 callback 객체를 파라미터로 전달한 후 초기화가 완료될 때   
  callback 객체 내의 메소드가 자동으로 호출되도록 한다. `MainActivity.java`를 아래와 같이 작성하자.
```java
public class MainActivity extends AppCompatActivity {

    SupportMapFragment mapFragment;
    GoogleMap map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // XML Layout에 추가한 fragment 객체를 참조한다.
        mapFragment = (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                Log.d("Map", "map is ready.");
                map = googleMap;
            }
        });

        try {
            MapsInitializer.initialize(this);
        } catch(Exception e) {
            e.printStackTrace();
        }

        Button button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startLocationService();
            }
        });
    }
}
```
* 위 코드에서는 `SupportMapFragment` 객체를 참조한 후 `getMapAsync()` 메소드를 호출했다. 이 메소드는 비동기 방식으로 작동하기 때문에   
  지도가 사용 가능하게된 후에 `onMapReady()` 메소드가 자동으로 호출된다.

* 지도가 준비되었다면 버튼을 클릭했을 때 `startLocationService()` 메소드를 호출하는데, 이 메소드는 `LocationManager`로부터   
  현재 위치를 전달받도록 구현한다. 또한 `LocationListener`도 전과 동일하게 구현하는데, `onLocationChanged()`에는   
  현재 위치를 지도에 보여주는 코드를 추가해야 한다.
```java
public class MainActivity extends AppCompatActivity {

    SupportMapFragment mapFragment;
    GoogleMap map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // XML Layout에 추가한 fragment 객체를 참조한다.
        mapFragment = (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                Log.d("Map", "map is ready.");
                map = googleMap;
            }
        });

        try {
            MapsInitializer.initialize(this);
        } catch(Exception e) {
            e.printStackTrace();
        }

        Button button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startLocationService();
            }
        });
    }

    public void startLocationService() {
        LocationManager manager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        try {
            //Location location = manager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            GPSListener gpsListener = new GPSListener();
            long minTime = 1000;
            float minDistance = 0;

            manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTime, minDistance, gpsListener);
            
        } catch(SecurityException e) {
            e.printStackTrace();
        }
    }

    class GPSListener implements LocationListener {
        @Override
        public void onLocationChanged(Location location) {
            Double latitude = location.getLatitude();
            Double longitude = location.getLongitude();
            showCurrentPosition(latitude, longitude);
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) { }

        @Override
        public void onProviderEnabled(String s) { }

        @Override
        public void onProviderDisabled(String s) { }

        private void showCurrentPosition(Double latitude, Double longitude) {
            LatLng curPoint = new LatLng(latitude, longitude);
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(curPoint, 15));
        }
    }
}
```
* `showCurrentLocation()`메소드로 전달된 위도와 경도값은 `LatLng` 객체로 만들면 지도상에 표시할 수 있다.   
  `LatLng` 객체는 경위도 좌표로 구성된 위치를 지도에 표시할 수 있도록 정의된 객체이다. 이 객체로 지구상의 특정 위치를   
  표현할 수 있으며, `GoogleMap#animateCamera()` 메소드를 이용하여 그 위치를 중심으로 지도를 보여줄 수 있다.   
  `CameraUpdateFactory.newLatLngZoom()`의 두 번째 인자는 지도의 축척(Scale)을 지정한다.
<hr/>

<h3>Manifest에 정보 등록하기</h3>

* Manifest에는 Google Map 라이브러리를 사용한다는 정보와 함께 GPS, INTERNET 사용 권한과 기타 설정 정보를 등록해야 한다.   
  아래는 `AndroidManifest.xml` 코드이다.
```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="com.techtown.location">
    <permission android:name="com.techtown.location.permission.MAPS_RECEIVE"
        android:protectionLevel="signature" />
    <uses-permission android:name="com.techtown.location.permission.MAPS_RECEIVE" />
    <uses-permission android:name="android.permission.INTERNET"/>
    <uses-permission android:name="com.google.android.providers.gsf.permission.READ_GSERVICES" />
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
    
    <uses-feature android:glEsVersion="0x00020000"
        android:required="true" />
    
    <application
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/AppTheme"
        android:usesCleartextTraffic="true">
        
        <uses-library android:name="com.google.android.maps" />
        <uses-library android:name="org.apache.http.legacy"
            android:required="false" />
        
        <!-- API Key 설정 -->
        <meta-data
            android:name="com.google.android.maps.v2.API_KEY"
            android:value="ACQUIRED API KEY"/>
        
        <meta-data
            android:name="com.google.android.gms.version"
            android:value="@integer/google_play_services_version" />
        
        <activity android:name=".MainActivity">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />

                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
</manifest>
```
* Google Map은 인터넷을 사용하므로 INTERNET과 같은 일반 권한과 함께 Google Map Service를 위해 필요한 권한을 등록한다.   
  이 권한 중 `ACCESS_FINE_LOCATION`은 위험 권한이므로 위험 권한을 부여하기 위한 설정과 코드를 추가해야 한다.

* 지도 서비스와 같은 외부 라이브러리를 추가할 때는 기본적으로 사용하는 appcompat의 버전과 맞지 않아 오류가 나는 경우가 있다.   
  이를 방지하기 위해 아래처럼 `build.gradle(Module:app)`를 아래처럼 수정하자.
```gradle
allprojects {
    repositories {
        maven {url 'https://jitpack.io'}
    }
}

dependencies {
    implementation fileTree(dir: "libs", include: ["*.jar"])
    implementation 'androidx.appcompat:appcompat:1.1.0'
    implementation 'androidx.constraintlayout:constraintlayout:1.1.3'
    implementation 'com.google.android.gms:play-services-maps:17.0.0'
    testImplementation 'junit:junit:4.12'
    androidTestImplementation 'androidx.test.ext:junit:1.1.1'
    androidTestImplementation 'androidx.test.espresso:espresso-core:3.2.0'
    implementation 'com.github.pedroSG94:AutoPermissions:1.0.3'

}
```
<hr/>

<h3>GoogleMap API Key 발급받기</h3>

* `console.developers.google.com`에서 키를 발급받고, `AndroidManifest.xml`에 추가한다.
<hr/>

