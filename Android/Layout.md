Layout
======

* 안드로이드에서 제공하는 대표적인 레이아웃들은 다음과 같다.

<table>
    <tr>
        <td>제약 레이아웃(ConstraintLayout)</td>
        <td>제약 조건 기반 모델, 제약 조건을 사용해 화면을 구성하며 android studio에서 자동으로 설정하는 default layout이다.</td>
    </tr>
    <tr>
        <td>리니어 레이아웃(LinearLayout)</td>
        <td>Box 모델, 한 쪽 방향으로 차례대로 View를 추가하며 화면을 구성하며 View가 차지할 수 있는 사각형 영역을 할당한다.</td>
    </tr>
    <tr>
        <td>상대 레이아웃(RelativeLayout)</td>
        <td>Rule 기반 모델, 부모 컨테이너나 다른 View와의 상대적 위치로 화면을 구성하며 제약 레이아웃에 밀려 권장되지 않는다.</td>
    </tr>
    <tr>
        <td>프레임 레이아웃(FrameLayout)</td>
        <td>Single 모델, 가장 상위에 있는 하나의 view 또는 viewgroup만 보여주며 여러 개의 view가 들어가면 중첩하여 쌓게 된다.
            가장 단순하지만 여러 개의 view를 중첩한 후 각 view를 전환하여 보여주는 방식으로 자주 사용된다.</td>
    </tr>
    <tr>
        <td>테이블 레이아웃(TableLayout)</td>
        <td>Grid 모델, 격자 모양의 배열을 사용하여 화면을 구성하며 HTML에서 많이 사용하는 정렬 방식과 유사하지만 많이 사용되지는 않는다.</td>
    </tr>
</table>

* LinearLayout은 Box 모델을 사용하는 레이아웃으로, View가 차지하는 영역을 Box로 보고, 한 쪽 방향으로 쌓는다.   
  가로 방향은 __Horizontal__, 세로 방향은 __Vertical__ 로 지정한다.

* RelativeLayout은 부모 컨테이너, 즉 부모 레이아웃과의 상대적 위치 또는 같은 레이아웃에 들어있는 view와의 상대적 위치를   
  이용해 화면을 배치한다.

* FrameLayout은 가장 위에 있는 하나의 View만 화면에 보여준다. 그 안에 추가된 여러 개의 View, Viewgroup들은 그 아래에   
  중첩하여 쌓인다.

* SrcollView : 하나의 view나 viewgroup을 넣을 수 있고, 어떤 view의 내용물이 화면보다 크면 scroll을 만들어준다.   
  따라서 View를 배치하는 목적을 가진 레이아웃이라기보단 view를 담는 viewgroup의 역할을 한다고 볼 수 있다.

<hr/>

<h3>View 영역 알아보기</h3>

* View가 레이아웃에 추가될 때는 __보이지 않는 View의 테두리(Border)가 있다__.   
  * Padding : 내용물과 Border 사이의 간격
  * Margin : Border와 view의 영역(Box) 사이의 간격
* View와 padding의 값을 조절할 수 있는 속성은 다음과 같다.
```text
layout_margin, layout_marginTop, layout_marginBottom, layout_marginLeft, layout_marginRight
padding, paddingTop, paddingBottom, paddingLeft, paddingRight
```
* layout_margin 또는 padding을 사용하면 각각의 상하좌우 값을 한번에 조절할 수 있다.

<hr/>

<h3>View의 배경색</h3>

* XML Layout에서 색상 지정시에는 #ARGB 값을 지정한다.
* 배경을 지정하는 속성은 __background__ 속성이다.
<hr/>

<h2>Linear Layout 사용하기</h2>

* LinearLayout의 필수 속성에는 `android:layout_width`, `android:layout_height`, `android:orientation` 이 있다.   
```xml
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    tools:context=".MainActivity">

   <Button
       android:id="@+id/button1"
       android:layout_width="match_parent"
       android:layout_height="wrap_content"
       android:text="BUTTON1" />

    <Button
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:id="@+id/button2"
        android:text="BUTTON2"/>

    <Button
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:id="@+id/button3"
        android:text="BUTTON3" />
</LinearLayout>
```
* 위 코드는 LinearLayout 의 orientation 속성을 vertical로 주고, Button 3개를 추가한 코드이다.   
  각 버튼은 가로 방향을 부모 컨테이너에 맞게 채우며 (match_parent) 추가된다.
<hr/>

<h3>Java 코드에서 화면 구성하기</h3>

* `MainActivity.java` 파일을 보자.
```java
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}
```
* 위 코드에서 `setContentView()` 의 값으로 R.layout.activity_main이 전달되는데, 이는 `activity_main.xml`과   
  연결하라는 뜻이다. 즉, acticity_main.xml 파일과 MainActivity.java가 연결되어 하나의 화면을 구성하는 것이다.

* Java 코드에서 위의 xml 코드를 구성하면 다음과 같다.
```java
// LayoutCodeActivity.java
public class LayoutCodeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );

        Button button1 = new Button(this);
        button1.setText("BUTTON1");
        button1.setLayoutParams(params);
        mainLayout.addView(button1);

        Button button2 = new Button(this);
        button1.setText("BUTTON2");
        button1.setLayoutParams(params);
        mainLayout.addView(button1);

        Button button3 = new Button(this);
        button1.setText("BUTTON3");
        button1.setLayoutParams(params);
        mainLayout.addView(button1);

        setContentView(mainLayout);
    }
}
```

* 하나의 화면은 `Activity`라고 부르는데, 앱을 실행했을 때 처음 보이는 화면을 `Main Activity` 라 한다.   
  즉, 프로젝트를 처음 만들면 Main Activity가 만들어지고, 그 activity를 위한 java 소스 파일은 `MainActivity.java`가 된다.   
  그리고 이 main activity는 `AndroidManifest.xml` 파일 안에 자동으로 등록된다.

* `AndroidManifest.xml` 파일에 등록할 activity 소스 파일은 아래 부분을 수정하면 된다.
```xml
<!-- AndroidManifest.xml -->
<activity android:name=".LayoutCodeActivity">
  <intent-filter>
    <action android:name="android.intent.action.MAIN" />
      <category android:name="android.intent.category.LAUNCHER" />
  </intent-filter>
</activity>
```
<hr/>

<h3>화면 생성 과정 분석하기</h3>

* `LayoutCodeActivity.java` 를 보면, `onCreate()` 메소드 내에 `setContentView()` 메소드를 호출하는 부분이 있다.   
  Layout으로 만든 객체를 이 메소드의 파라미터로 전달하면, 그 레이아웃이 화면에 표시된다.

* XML Layout에서 정의할 수 있는 대부분의 속성은 java 소스 코드에서도 사용할 수 있도록 메소드로 제공된다.   
  예를 들어 `new LinearLayout()`으로 LinearLayout 객체를 만들고, `setOrientation(LinearLayout.VERTICAL)` 로   
  세로 방향으로 view를 추가하도록 설정했다.

* `LayoutCodeActivity.java`에서 사용된 `this`는 `Context` 객체를 의미하는데, new 연산자를 사용해서   
  View 객체를 코드에서 만들때에는 __항상 Context 객체가 전달__ 되어야 한다. 즉, 안드로이드의 모든 UI 객체들은   
  `Context` 객체를 전달받도록 되어있는 것이다. `AppCompatActicty` 클래스는 `Context` 클래스를 상속하므로   
  이 클래스 안에서는 this를 `Context` 객체로 사용할 수 있지만, 만약 `Context`를 상속받지 않은 클래스에서   
  `Context` 객체를 전달해야 한다면 `getApplicationContext()` 메소드를 호출하여 app에서 참조할 수 있는   
  `Context` 객체를 사용해야 한다.

* Java 코드에서 View를 만들어 ViewGroup에 추가할 때는 __View의 배치를 위한 속성을 설정할 수 있는__ `LayoutParams`   
  객체를 사용한다. 이 객체는 레이아웃에 추가되는 view의 속성 중에서 레이아웃과 관련된 속성을 담고 있다.   
  `LayoutParams` 객체를 새로 만들 때에는 __반드시 view의 가로, 세로 속성을 지정해야 하며__, 이 때에는   
  `LayoutParams.MATCH_PARENT` 또는 `LayoutParams.WRAP_CONTENT`를 사용할 수 있다.   
  필요한 경우에는 이 두 가지 상수가 아닌 가로와 세로의 크기값을 직접 숫자로 지정할 수 도 있다.

* Java 코드에서 레이아웃에 view를 추가하려면 `addView()` 메소드를 사용해야 한다.
<hr/>

<h3>View 정렬하기</h3>

* LinearLayout 내의 view는 왼쪽, 가운데, 오른쪽 등의 방향을 지정하여 정렬할 수 있다.   
  이때 정렬을 위해 사용하는 속성 이름은 `gravity` 이다. 레이아웃에서 정렬 기능이 필요한 경우는 아래의 두 가지가 있다.

<table>
  <tr>
    <td>layout_gravity</td>
    <td>부모 컨테이너의 여유 공간에 view가 모두 채워지지 않아 여유 공간이 생겼을 때, 여유 공간 내에서 view를 정렬한다.</td>
  </tr>
  <tr>
    <td>gravity</td>
    <td>view 안에 표시하는 내용물을 정렬한다. (TextView의 경우에는 글자가 되고, ImageView의 경우에는 이미지에 해당한다.)</td>
  </tr>
</table>

* layout_gravity 속성은 View의 layout_width 나 layout_height를 wrap_content로 만든 후에 사용할 수 있다.   
  예를 들어 orientation이 vertical인 LinearLayout에서 layout_width가 wrap_content라면, Button을 예로 들면   
  버튼의 가로 공간에 여유 공간이 생기게 된다. 안드로이드는 이렇게 여유 공간이 생기면 일반적으로 왼쪽 정렬을 하는데,   
  layout_gravity 속성을 직접 설정하면 왼쪽, 중앙 또는 오른쪽 정렬을 할 수 있다.

* gravity 속성은 view의 내용물의 위치를 결정하는 것이다. 이 속성은 view가 화면에서 차지하는 영역이 충분히   
  큰 경우에 생기는 여유 공간 내에서 내용물을 어떻게 정렬할 것인지를 결정한다.
<hr/>

<h4>View 정렬 속성 : layout_gravity</h4>

* Project 탕에서 `/app/res/layout` 폴더 선택 후, New Layout Resource File을 선택하면 대화상자가 뜨는데,   
  Root Element에는 최상위 레이아웃이 무엇인지를 지정한다. 이름은 `gravity.xml`로 지정해보자.
* 생성된 gravity.xml 파일을 아래와 같이 작성해보자.
```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical">

    <Button
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:id="@+id/button4"
        android:layout_gravity="left"
        android:text="left" />
    <Button
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:id="@+id/button5"
        android:layout_gravity="center"
        android:text="center" />
    <Button
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:id="@+id/button6"
        android:layout_gravity="right"
        android:text="right" />
</LinearLayout>
```
* 위 코드의 디자인을 보면, 각 버튼이 text에 맞게 좌, 중, 우로 정렬됨을 확인할 수 있다.
<hr/>

<h4>내용물 정렬 속성 : gravity</h4>

* gravity 속성에 넣을 수 있는 값들은 layout_gravity와 같으며, 필요한 경우에는 `|` 연산자를 사용하여   
  여러 개의 값을 같이 설정할 수 있다. 단, `|` 연산자의 __양쪽에 공백이 없어야 한다__.
* 당연한 이야기지만, 만약 Button이나 TextView의 크기를 wrap_content로 지정하면, view의 크기가   
  내용물에 알맞게 결정되므로 여유 공간이 없다. 이러한 경우에는 gravity 속성을 지정해도 아무런 변화가 없다.
* gravity 속성으로 지정할 수 있는 값들은 다음과 같다.

<table>
  <tr>
    <td>top</td>
    <td>대상 객체를 위쪽 끝에 배치한다.</td>
  </tr>
  <tr>
    <td>bottom</td>
    <td>대상 객체를 아래쪽 끝에 배치한다.</td>
  </tr>
  <tr>
    <td>left</td>
    <td>대상 객체를 왼쪽 끝에 배치한다.</td>
  </tr>
  <tr>
    <td>right</td>
    <td>대상 객체를 오른쪽 끝에 배치한다.</td>
  </tr>
  <tr>
    <td>center_vertical</td>
    <td>대상 객체를 수직 방향의 중앙에 배치한다.</td>
  </tr>
  <tr>
    <td>center_horizontal</td>
    <td>대상 객체를 수평 방향의 중앙에 배치한다.</td>
  </tr>
  <tr>
    <td>fill_vertical</td>
    <td>대상 객체를 수직 방향으로 여유 공간만큼 확대하여 채운다.</td>
  </tr>
  <tr>
    <td>fill_horizontal</td>
    <td>대상 객체를 수평 방향으로 여유 공간만큼 확대하여 채운다.</td>
  </tr>
  <tr>
    <td>center</td>
    <td>대상 객체를 수직 방향과 수평 방향의 중간에 배치한다.</td>
  </tr>
  <tr>
    <td>fill</td>
    <td>대상 객체를 수직 방향과 수평 방향으로 여유 공간만큼 확대하여 채운다.</td>
  </tr>
  <tr>
    <td>clip_vertical</td>
    <td>대상 객체의 상하 길이가 여유 공간보다 클 경우에 남는 부분을 잘라낸다.</td>
  </tr>
  <tr>
    <td>clip_horizontal</td>
    <td>대상 객체의 좌우 길이가 여유 공간보다 클 경우에 남는 부분을 잘라낸다.</td>
  </tr>
</table>

* 종종 TextView로 화면을 구성할 때, layout_gravity나 gravity 속성을 설정하는것 만으로 정렬을 맞추기 힘든 경우가 있다.   
  이런 경우에는 `baselineAligned` 속성을 사용할 수 있다.

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:baselineAligned="false">
  
  <!-- 중략 -->
```
<hr/>

<h3>View의 margin과 padding 설정하기</h3>

* View의 영역은 테두리선으로 표시할 수 있다. View는 이 테두리선을 기준으로 바깥 공간과 안쪽 공간이 있으며,   
  이 모든 공간을 포함한 View의 공간을 __Cell__ 이라 한다. Button이나 TextView를 위젯이라 하기에 이 공간을   
  __Widget Cell__ 이라 하기도 한다. 
* 테두리선 기준으로 바깥쪽 공간을 __Margin__ 이라 하고, 이 값은 `layout_margin` 속성으로 지정할 수 있다.
* 테두리선 안쪽의 공간을 __Paddiing__ 이라 하고, 이 값은 `padding` 속성으로 지정할 수 있다.
* Margin과 Padding은 각각 상하좌우를 따로 지정할 수 있는데, Margin의 경우는 layout_marginLeft, layout_marginRight,   
  layout_marginTop, layout_marginBottom이 있다. 이 네가지의 값을 한번에 지정할 때에는 layout_margin 속성으로 지정하면 된다.   
  Padding도 Margin과 마찬가지로 left, top, bottom, right가 있으며, 한번에 지정할 때는 padding 속성으로 지정하면 된다.
<hr/>

<h3>여유 공간을 분할하는 layout_weight 속성</h3>

* 부모 컨테이너에 추가한 view들의 공간을 제외한 여유 공간은 `layout_weight` 속성으로 분할할 수 있다.   
  즉, layout_weight 속성은 부모 컨테이너에 남아 있는 여유 공간을 분할하여 기존에 추가했던 view들에게 할당할 수 있다.
* 만약 2개의 view에 대해 각각 layout_weight 속성에 1을 지정하면, 이 두 개의 view는 1:1의 비율이 적용되어   
  반반씩 여유 공간을 나눠 갖게 된다. 만약 각각 1, 2를 지정하면 이 둘은 각각 1/3 과 2/3만큼 여유 공간을 나눠갖는다.
* 이때, layout_width나 layout_height로 지정하는 View의 크기는 __wrap_content나 숫자 값으로 지정__ 되어 있어야 한다.   
  만약 이 속성들이 match_parent이라면 예상하지 못한 결과가 나타날 수 있다.
```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:baselineAligned="false">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="horizontal">
        
        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:id="@+id/textView1"
            android:text="TEXT1"
            android:layout_weight="1" />
        
        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="TEXT2"
            android:layout_weight="1" />
    </LinearLayout>
</LinearLayout>
```
* 위 코드는 vertical 한 LinearLayout 내에 horizontal한 LinearLayout이 들어있다.   
  이때, 두 TextView는 모두 layout_weight 속성값이 1 이므로 1:1의 비율을 가져 반반씩 좌우 여유 공간을 나눠가질 것이다.
<hr/>

<h2>Relative Layout 사용하기</h2>

* 상대 layout으로 만들 수 있는 화면은 대부분 Constraint Layout으로 만들 수 있기에 사용이 권장되지 않는다.
* 상대 layout은 부모 컨테이너나 다른 view와의 상대적인 위치를 이용해 view의 위치를 결정할 수 있게 한다.
```xml
<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <Button
        android:id="@+id/button1"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:layout_alignParentLeft="true"
        android:layout_alignParentStart="true"
        android:layout_alignParentTop="true"
        android:layout_above="@+id/button2"
        android:background="#ff0088ff"
        android:layout_below="@+id/button3"
        android:text="BUTTON1" />

    <Button
        android:id="@+id/button2"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_alignParentBottom="true"
        android:layout_alignParentLeft="true"
        android:layout_alignParentStart="true"
        android:text="BUTTON2" />

    <Button
        android:id="@+id/button3"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_alignParentLeft="true"
        android:layout_alignParentStart="true"
        android:layout_alignParentTop="true"
        android:text="BUTTON3" />
</RelativeLayout>
```
* 상대 layout에서 부모 컨테이너와의 상대적 위치를 이용해 view를 배치할 수 있는 속성들은 다음과 같다.
<table>
  <tr>
    <td>layout_alignParentTop</td>
    <td>부모 컨테이너의 위쪽과 view의 위쪽을 맞춘다.</td>
  </tr>
  <tr>
    <td>layout_alignParentBottom</td>
    <td>부모 컨테이너의 아래쪽과 view의 아래쪽을 맞춘다.</td>
  </tr>
  <tr>
    <td>layout_alignParentLeft</td>
    <td>부모 컨테이너의 왼쪽 끝과 view의 왼쪽 끝을 맞춘다.</td>
  </tr>
  <tr>
    <td>layout_alignParentRight</td>
    <td>부모 컨테이너의 오른쪽 끝과 view의 오른쪽 끝을 맞춘다.</td>
  </tr>
  <tr>
    <td>layout_centerHorizontal</td>
    <td>부모 컨테이너의 수평 방향 중앙에 배치한다.</td>
  </tr>
  <tr>
    <td>layout_centerVertical</td>
    <td>부모 컨테이너의 수직 방향 중앙에 배치한다.</td>
  </tr>
  <tr>
    <td>layout_centerInParent</td>
    <td>부모 컨테이너의 수평과 수직 방향의 중앙에 배치한다.</td>
  </tr>
</table>

* 부모 컨테이너가 아닌 다른 View와의 상대적 위치를 이용해 View를 배치하는 속성들은 다음과 같다.
<table>
  <tr>
    <td>layout_above</td>
    <td>지정한 View의 위쪽에 배치한다.</td>
  </tr>
  <tr>
    <td>layout_below</td>
    <td>지정한 view의 아래쪽에 배치한다.</td>
  </tr>
  <tr>
    <td>layout_toLeftOf</td>
    <td>지정한 view의 왼쪽에 배치한다.</td>
  </tr>
  <tr>
    <td>layout_toRightOf</td>
    <td>지정한 view의 오른쪽에 배치한다.</td>
  </tr>
  <tr>
    <td>layout_alignTop</td>
    <td>지정한 view의 위쪽과 맞춘다.</td>
  </tr>
  <tr>
    <td>layout_alignBottom</td>
    <td>지정한 view의 아래쪽과 맞춘다.</td>
  </tr>
  <tr>
    <td>layout_alignLeft</td>
    <td>지정한 view의 왼쪽과 맞춘다.</td>
  </tr>
  <tr>
    <td>layout_alignRight</td>
    <td>지정한 view의 오른쪽과 맞춘다.</td>
  </tr>
  <tr>
    <td>layout_alignBaseline</td>
    <td>지정한 view와 내용물의 아래쪽 기준선(baseline)을 맞춘다.</td>
  </tr>
</table>

<hr/>

<h2>Table Layout</h2>

* TableLayout의 각 행은 `TableRow` 태그를 사용하여 지정할 수 있다.
* 유용한 속성 : `android:stretchColumns`
  * 이 속성은 가로 방향으로 여유 공간이 있다면, 그 여유 공간까지 모두 채워서 컬럼을 설정한다.   
    예를 들어 3개의 버튼이 있고, 이들이 여유 공간을 나눠 가지면서 가로 공간을 채우게 하려면   
    stretchColumns 속성값을 "0,1,2"로 주면 된다. (각 숫자는 컬럼의 인덱스를 가리킨다, 0-base)
  * `stretchColumns`와 반대되는 속성은 `shrinkColumns` 속성이다.
<hr/>

<h2>Frame Layout과 View의 전환</h2>

* Frame layout에는 __중첩__ 기능이 있다. View를 하나 이상 추가할 경우, 추가된 순서대로 view는 쌓인다.   
  가장 먼저 추가한 view가 가장 아래쪽에 쌓이고, 그 다음에 추가한 view는 그 위에 쌓여서 __가장 나중에__   
  __쌓인 view만 화면에 보이게 된다__. 이때 가장 위에 있는 view를 보이지 않게 하면 그 아래의 view가 보이는데,   
  이렇게 view를 보이거나 보이지않게 하는 속성이 __Visibility(가시성)__ 속성이다.

* View를 추가할 때는 visibility 속성을 줄 수 있는데, 값으로는 visible, invisible 또는 gone을 줄 수 있다.   
  만약 Java 코드에서 작성할 때에는 `setVisibility()` 메소드를 사용하면 된다.
```xml
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical">
    
    <Button
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:id="@+id/button"
        android:layout_gravity="center"
        android:text="Change Image!"
        android:onClick="onButton1Clicked" />
    
    <FrameLayout
        android:layout_width="match_parent"
        android:layout_height="match_parent">
        
        <ImageView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:id="@+id/imageView"
            android:src="@drawable/img1"
            android:visibility="invisible" />
        <ImageView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:id="@+id/imageView2"
            android:src="@drawable/img2"
            android:visibility="visible" />
    </FrameLayout>
</FrameLayout>
```
* 위 코드에서는 `ImageView`의 src 속성을 통해 이미지 파일의 경로를 지정했는데, 이미지 파일은   
  `/app/res/drawable` 폴더에 저장해야 한다.
* 다음으로는 Button 클릭 시 이미지가 변경되도록 Java 코드를 작성해보자.
```java
public class MainActivity extends AppCompatActivity {
    ImageView imageView;
    ImageView imageView2;
    
    int imageIndex = 0;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        imageView = findViewById(R.id.imageView);
        imageView2 = findViewById(R.id.imageView2);
    }
    
    public void onButton1Clicked(View v) {
        changeImage();
    }
    
    private void changeImage() {
        if(imageIndex == 0) {
            imageView.setVisibility(View.VISIBLE);
            imageView2.setVisibility(View.INVISIBLE);
            imageIndex = 1;
        } else if(imageIndex == 1) {
            imageView.setVisibility(View.INVISIBLE);
            imageView2.setVisibility(View.VISIBLE);
            imageIndex = 0;
        }
    }
}
```
<hr/>

<h2>ScrollView 사용하기</h2>

* ScrollView는 추가된 View의 영역이 한눈에 다 보이지 않을 때 사용된다. 이때, 단순히 ScrollView를   
  추가하고 그 안에 View를 넣으면 스크롤이 생긴다.

```xml
<!-- activity_main.xml -->
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical">

   <Button
       android:layout_width="wrap_content"
       android:layout_height="wrap_content"
       android:id="@+id/button"
       android:layout_gravity="center"
       android:text="show different image!"
       android:onClick="onButton1Clicked"/>
    
    <HorizontalScrollView
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:id="@+id/horScrollView">
        <ScrollView
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:id="@+id/scrollView" >
            <ImageView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:id="@+id/imageView" />
        </ScrollView>
    </HorizontalScrollView>
</LinearLayout>
```
* ScrollView는 기본적으로 수직 방향의 스크롤을 지원한다. 만약 수평 방향의 스크롤을 사용하려면   
  `HorizontalScrollView`를 사용하면 된다.
* 다음으로는 소스 코드를 작성하자.
```java
// MainActivity.java

public class MainActivity extends AppCompatActivity {
    
    ScrollView scrollView;
    ImageView imageView;
    BitmapDrawable bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        scrollView = findViewById(R.id.scrollView);
        imageView = findViewById(R.id.imageView);
        // 수평 Scroll Bar 사용 기능 설정
        scrollView.setHorizontalScrollBarEnabled(true);
        
        // 리소스의 이미지 참조
        Resources res = getResources();
        bitmap = (BitmapDrawable)res.getDrawable(R.drawable.img1);
        int bitmapWidth = bitmap.getIntrinsicWidth();
        int bitmapHeight = bitmap.getIntrinsicHeight();
        
        // 이미지 리소스와 이미지 크기 설정
        imageView.setImageDrawable(bitmap);
        imageView.getLayoutParams().width = bitmapWidth;
        imageView.getLayoutParams().height = bitmapHeight;
    }

    public void onButton1Clicked(View v) {
        changeImage();
    }

    private void changeImage() {
       Resources res = getResources();
       bitmap = (BitmapDrawable)res.getDrawable(R.drawable.img2);
       int bitmapWidth = bitmap.getIntrinsicWidth();
       int bitmapHeight = bitmap.getIntrinsicHeight();
       
       imageView.setImageDrawable(bitmap);
       imageView.getLayoutParams().width = bitmapWidth;
       imageView.getLayoutParams().height = bitmapHeight;
    }
}
```