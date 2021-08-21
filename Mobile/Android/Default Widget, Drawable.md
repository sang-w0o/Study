기본 위젯과 Drawable
======

<h2>기본 Widget</h2>

<h3>#1. TextView</h3>

* TextView는 화면을 구성할 때 가장 많이 사용되는 기본 위젯으로, 화면에 글자를 보여준다.   
  TextView의 다양한 속성들은 다음과 같다.
<hr/>

<h4>TextView : text 속성</h4>

* text 속성은 TextView의 문자열을 설정한다. 이는 __필수 지정 속성__ 으로, 문자열이 없으면 해당 TextView가 차지하는   
  영역도 알 수 없다. text 속성을 추가하는 방법은 text 속성으로 직접 문자열을 넣는 방법과 `/app/res/values` 폴더에서   
  strings.xml파일에 작성한 문자열을 지정하는 방법이 있다.
* 지금까지는 XML Layout 파일에 직접 문자열을 입력했지만, strings.xml 파일에 문자열을 미리 작성한 다음, 이 값을   
  text속성에 지정하는 방법이 더 권장된다. 예를 들어 다국어 지원의 경우, 이 방식을 사용하면 훨씬 효율적이다.
```xml
<!-- /app/res/values/strings.xml -->
<resources>
    <string name="app_name">SampleWidget</string>
    <string name="person_name">Sangwoo</string>
</resources>
```
* 이 값을 TextView에서 참조하는 방법은 아래와 같다.
```xml
<!-- activity_main.xml -->
<TextView
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:text="@string/person_name"/>
```
* 위와 같이 `/app/res/values/strings.xml`에 정의된 문자열은 text 속성에서 `@string/이름` 형식으로 참조해야 한다.
<hr/>

<h4>TextView : textColor 속성</h4>

* textColor 속성은 TextView에서 표시하는 문자열의 색상을 설정한다.   
  색상 설정은 일반적으로 `#AARRGGBB` 포맷을 사용하며, 각각 Alpha, Red, Green, Blue를 의미한다.   
  이때 투명도를 나타내는 Alpha값은 투명하지 않음(FF), 투명함(00), 반투명함(88)을 설정할 수 있다.
<hr/>

<h4>TextView : textSize 속성</h4>

* textSize 속성은 TextView에서 표시하는 문자열의 폰트 크기를 설정한다.   
  크기의 단위로는 `dp`, `sp`, `px`를 사용할 수 있다. 이 때 `sp`는 단말의 해상도에 따라 글자의 크기를 일정한 크기로   
  보일 수 있게 하며, 폰트를 변경했을 때 해당 폰트도 반영되도록 해준다.
<hr/>

<h4>TextView : textStyle 속성</h4>

* textStyle 속성은 TextView에서 표시할 문자열의 스타일 속성을 지정한다.   
  속성값으로는 `normal`, `bold`, `italic` 등의 값을 지정할 수 있으며, `|` 기호를 통해 여러 개의 속성값을 지정할 수 있다.   
  __주의 : | 기호의 앞뒤에 공백이 있으면 안된다.__
<hr/>

<h4>TextView : typeFace 속성</h4>

* typeFace 속성은 TextView에서 표시할 문자열의 폰트를 설정한다.   
  일반적으로는 `normal`, `sans`, `serif`, `monospace` 중 하나를 지정한다.
<hr/>

<h4>TextView : maxLines 속성</h4>

* maxLines 속성은 TextView에서 표시할 문자열의 최대 줄 수를 지정한다. 만약 한 줄로만 표시하고 싶다면 이 값을 1로 지정하면 된다.   
  지정된 영역을 넘어가는 문자열은 표시되지 않고 잘린다.
<hr/>

<h3>#2. Button</h3>

* Button은 사용자가 클릭했을 때, 클릭에 대한 반응을 하는 위젯이다. Button은 TextView를 상속하여 정의돼있으므로,   
  TextView의 속성도 그대로 가지고 있고, 사용할 수 있다.
* Android는 CheckBox, RadioButton 등 다양한 유형의 버튼을 제공한다.
* 사용자에 발생한 이벤트(사용자의 클릭 등)를 가장 간단하게 처리하는 방법은 `OnClickListener`를 정의하는 것이다.

* CheckBox와 RadioButton은 단순히 Click 이벤트만 처리하는 것이 아니라, 상태 값을 저장하고 선택/해제 상태를 표시할 수 있다.   
  이러한 작업이 가능하도록 `CompoundButton` 클래스가 제공되는데, 이 클래스는 다음 메소드들을 포함한다.
  * `public boolean isChecked()` : checkbox 또는 radiobutton이 체크 되었는지의 유무 반환
  * `public void setChecked(boolean checked)` : checkbox 또는 radiobutton을 체크시킬 수 있다.
  * `public void toggle()`
* 버튼의 상태가 바뀌는 것을 알고 싶다면 다음 메소드를 재정의하여 사용하면 된다.   
  `void onCheckedChanged(CompoundButton buttonView, boolean isChecked);`

* 하나의 RadioButton이 선택 되면, 다른 RadioButton들은 해제돼야 하는데, 이를 구현하기 위해 `RadioGroup`을 지정한다.

* 아래의 xml 코드를 보자.
```xml
<!-- app/res/layout/button.xml -->
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:orientation="vertical"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <Button
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:id="@+id/button"
        android:text="CHOOSE!"
        android:textSize="24sp"
        android:textStyle="bold"
        android:gravity="center"/>
    <RadioGroup
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:id="@+id/radioGroup01"
        android:orientation="horizontal"
        android:paddingLeft="10dp"
        android:paddingRight="10dp"
        android:layout_marginTop="20dp">

        <RadioButton
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:id="@+id/radio01"
            android:text="MALE"
            android:textColor="#ff55aaff"
            android:textStyle="bold"
            android:textSize="24sp"/>
        <RadioButton
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:id="@+id/radio02"
            android:text="FEMALE"
            android:textColor="#ff55aaff"
            android:textSize="24sp"
            android:textStyle="bold"/>
    </RadioGroup>

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_gravity="center_vertical|center_horizontal"
        android:orientation="horizontal"
        android:paddingTop="20dp">

        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="ALL DAY"
            android:textSize="24sp"
            android:paddingRight="10dp"
            android:textColor="#ff55aaff"/>
        <CheckBox
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:id="@+id/allDay" />

    </LinearLayout>
</LinearLayout>
```
* 위 코드를 보면, `RadioButton` 태그들은 모두 `RadioGroup` 태그 내에 설정되어 있다.
<hr/>

<h3>#3. EditText</h3>

* EditText는 입력 상자의 역할을 하며, 사용자에게 값을 입력받을 때 사용된다.
* 아래 xml 파일을 보자.
```xml
<!-- app/res/layout/edittext.xml -->
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:orientation="vertical" 
    android:layout_width="match_parent"
    android:layout_height="match_parent">
    
    <EditText
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:id="@+id/usernameInput"
        android:textSize="24sp"
        android:inputType="text"
        android:hint="INSERT NAME" />

</LinearLayout>
```
* hint 속성은 EditText에 간단한 안내글을 표시한다. 이 안내글은 사용자의 입력이 진행되면 사라진다.
* inputType 속성은 입력되는 글자의 유형을 정의할 수 있다. 즉, 글자 입력 시 보일 keypad의 유형을 정할 수 있다.   
  위 코드에서는 "text"로 지정하여 글자 키패드가 보이도록 했다.
<hr/>

<h3>#4. ImageView & ImageButton</h3>

* ImageView와 ImageButton은 이미지를 화면에 표시할 때 사용하는 가장 간단한 위젯이다.   
  두 위젯의 차이점은 버튼처럼 사용할 수 있다는 점 외에는 없다. 아래 정리는 ImageView를 기준으로 정리한다.

* Image를 나타내려면 먼저 `app/res/drawable` 폴더에 저장한 이미지 파일을 넣은 후, src 속성값을 다음 형식으로 지정한다.   
  `@drawable/imagefilename`   
  이때 이미지 파일명은 확장자를 제외하고 작성한다.
<hr/>

<h4>ImageView : src 속성</h4>

* src 속성은 원본 이미지를 설정한다. TextView에 text 속성이 필수인 것 처럼 ImageView에서는 src 속성이 필수이다.   
  ImageView에 추가할 이미지의 확장자는 `.jpg` 또는 `.png` 파일이다.
<hr/>

<h4>ImageView : maxWidth, maxHeight 속성</h4>

* 두 속성은 이미지가 표시되는 최대 폭과 최대 높이를 지정한다.   
  이 속성을 지정하지 않으면 원본 이미지가 그대로 나타난다. 이미지의 원본이 너무 큰 경우 이 속성으로 최대 크기를 제한할 수 있다.
<hr/>

<h4>ImageView : tint 속성</h4>

* tint 속성은 ImageView에 보이는 이미지의 색상을 설정할 수 있다.   
  색상은 `#AARRGGBB` 포맷으로 적용하면 된다.
<hr/>

<h4>ImageView : scaleType 속성</h4>

* scaleType 속성은 ImageView의 크기에 맞게 원본 이미지의 크기를 자동으로 조절하여 보여줄 때 사용된다.   
  이때 원본 이미지를 무조건 늘리거나 줄이는 것이 아니라, 원하는 형태로 확대 및 축소할 수 있다.   
* scaleType 속성에는 `fitXY`, `centerCrop`, `centerInside` 등의 이미지 변환 알고리즘이 적용된 미리 정의된 값을 사용할 수 있다.

* 아래 xml 코드를 보자.
```xml
<!-- app/res/layout/image.xml -->
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:orientation="vertical" 
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <ImageButton
        android:layout_width="50dp"
        android:layout_height="50dp"
        android:id="@+id/imageButton"
        android:layout_marginTop="40dp"
        android:layout_marginLeft="40dp"
        android:background="@drawable/a"
        android:contentDescription="ok button"/>
    
    <ImageView
        android:layout_width="50dp"
        android:layout_height="50dp"
        android:id="@+id/imageView"
        android:layout_marginTop="160dp"
        android:layout_marginLeft="160dp"
        android:background="@drawable/a"
        android:contentDescription="person button"/>
</LinearLayout>
```
<hr/>

<h3>TextView와 EditText 등의 View로부터 상속받은 속성</h3>

* TextView, EditText 등은 모두 `View`를 상속받아 만들어진 것이므로 `View`의 속성을 그대로 가진다.
<hr/>

<h4>View : 커서 관련 속성</h4>

* EditText는 문자나 숫자를 입력하는 역할을 하며, 커서가 깜박이는 동작이 있다.   
  그리고 커서 위치는 입력된 문자열의 마지막 지점으로 이동하도록 되어 있다. 하지만 경우에 따라 사용자가 EditText를   
  선택할 때 마다 전체 내용을 수정할 수 있도록 편의를 제공해야하는 경우가 있다. 커서에 설정하는 메소드들은 다음과 같다.
  * `public int getSelectionStart()` : 선택된 영역의 시작 위치를 알려준다. (선택 영역 없으면 현재 커서의 위치 반환)
  * `public int getSelectionEnd()` : 선택된 영역의 끝 위치를 알려준다. (선택 영역 없으면 현재 커서의 위치 반환)
  * `public void setSelection(int start, int stop)` : 선택 영역을 지정할 때 사용된다.
  * `public void setSelection(int index)` : 선택 영역을 지정할 때 사용된다.
  * `public void selectAll()` : 전체 문자열이 선택된다.

* 커서에 관련된 속성들은 다음과 같다.
  * selectAllOnFocus : true로 설정하면 focus를 받을 때 문자열 전체가 선택된다.
  * cursorVisible : false로 설정하면 커서가 보이지 않는다.
<hr/>

<h4>View : 자동 링크 관련 속성</h4>

* autoLink 속성을 true로 설정하면, 문서에 포함된 웹페이지 주소나 이메일 주소를 링크 색상으로 표시하며,   
  해당 링크를 클릭하면 웹 페이지에 바로 접속하거나 메일 편집기를 띄워주는 기능을 간단하게 넣을 수 있다.
<hr/>

<h4>View : 준 간격 조정 관련 속성</h4>

* 줄 간격 조정 관련 속성을 사용하면 TextView의 줄 간격을 조절하여 가독성을 높일 수 있다.
* lineSpacingMultiplier : 줄 간격을 기본 줄 간격의 배수로 설정한다. (기본 값을 1.0으로 놓고 값을 조절하면 된다.)
* lineSpacingExtra : 줄 간격을 여유 값으로 설정한다.
<hr/>

<h4>대소문자 표시 관련 속성</h4>

* capitalize 속성을 사용하면 글자, 단어, 문장 단위로 대소문자를 조절할 수 있다.   
  속성값에는 `characters`, `words`, `sentences` 등을 지정할 수 있는데, 각 값을 지정하면 글자, 단어, 문장   
  단위로 맨 앞 글자를 대문자로 표시해준다.
<hr/>

<h4>줄임 표시 관련 속성</h4>

* TextView는 한 줄로 되어있는데, 많은 문자를 입력하면 입력한 내용의 뒷부분은 잘리고, `...`로 표시된다.   
  이때 ellipsize 속성을 사용하면, 입력한 내용의 생략 부분을 설정할 수 있다.   
  Default값인 `none`은 뒷부분을 자르고, `start`, `middle`, `end` 값들은 각각 앞부분, 중간부분, 뒷부분을 자른다.
<hr/>

<h4>편집 기능 관련 속성</h4>

* EditText에 입력되어 있는 문자열을 편집하지 못하게 하려면 editable 속성값을 false로 지정하면 된다.   
  (기본값 : true)
<hr/>

<h4>힌트 표시 관련 속성</h4>

* EditText에 어떤 내용을 입력할지 안내문으로 알려줄 때는 hint 속성을 사용한다.   
  만약 hint 속성으로 나타낸 글자의 색상을 바꾸고 싶다면 textColorHint 속성에 색상을 지정하면 된다.
<hr/>

<h4>문자열 변경 처리 관련 속성</h4>

* EtitText에 입력된 문자를 확인하거나 format 체크 시에는 `getText()` 메소드를 사용한다.   
  이 메소드가 반환하는 것은 `Editable` 객체인데, 이 객체의 `toString()` 메소드는 일반 String 타입의 문자열을 확인할 수 있다.  
* 만약 문자열이 사용자의 입력에 따라 바뀔 때 마다 확인하는 기능을 넣고 싶다면 `TextChangedListener`를 재정의하면 된다.   
  `public void addTextChangedListener(TextWatcher watcher);`   
  이 메소드를 사용하면 `TextWatcher` 객체를 설정할 수 있다. 이 객체는 Text가 변경될 때 마다 발생하는 이벤트를 처리한다.   
  `TextWatcher` 인터페이스에는 다음 메소드들이 정의되어 있다.
  * `public void beforeTextChanged(CharSequence s, int start, int count, int after)` : 문자열이 편집되기 전
  * `public void afterTextChanged(Editable s)` : 문자열이 편집된 후
  * `public void onTextChanged(CharSequence s, int start, int before, int count)` : 문자열이 편집된 순간
<hr/>

<h2>Drawable 만들기</h2>

* `Drawable`은 View에 설정할 수 있는 객체이며, 그 위에 그래픽을 그릴 수 있다. 이 작업은 보통 소스코드에서   
  작성하지만, XML로 그래픽을 그릴 수 있다면 좀 더 편리하다.

* Drawable xml 파일은 이미지를 버튼 배경으로 설정하는 것 처럼 `/app/res/drawable` 폴더 내에 넣어 Button(View)의   
  배경으로 설정할 수 있다. 즉, drawable 폴더 내에 이미지가 아닌 XML 파일이 들어가 이미지처럼 설정되는 것이다.

* Drawable에는 다음과 같은 종류들이 있다.

<table>
    <tr>
        <td>BitmapDrawable</td>
        <td>이미지 파일을 보여줄 때 사용한다. Bitmap 그래픽 파일(png, jpg, gif 등)을 사용해서 생성한다.</td>
    </tr>
    <tr>
        <td>StateListDrawable</td>
        <td>상태별로 다른 비트맵 그래픽을 참조한다.</td>
    </tr>
    <tr>
        <td>TransitionDrawable</td>
        <td>두 개의 drawable을 서로 전환할 수 있다.</td>
    </tr>
    <tr>
        <td>ShapeDrawable</td>
        <td>색상과 그라데이션을 포함하여 도형 모양을 정의할 수 있다.</td>
    </tr>
    <tr>
        <td>InsetDrawable</td>
        <td>지정한 거리만큼 다른 Drawable을 들어도록 만들 수 있다.</td>
    </tr>
    <tr>
        <td>ClipDrawable</td>
        <td>Level 값을 기준으로 다른 drawable을 clipping할 수 있다.</td>
    </tr>
    <tr>
        <td>ScaleDrawable</td>
        <td>Level 값을 기준으로 다른 drawable의 크기를 변경할 수 있다.</td>
    </tr>
</table>

<hr/>

<h3>StateListDrawable(상태 Drawable) 만들기</h3>

* 상태 drawable은 View의 상태에 따라 view에 보여줄 그래픽을 다르게 지정할 수 있다.
* `/app/res/drawable` 폴더에 `New Drawable Resource File`을 선택하고, 새로운 xml 파일을 만든다.
```xml
<!-- /app/res/drawable/finger_drawable.xml -->
<selector xmlns:android="http://schemas.android.com/apk/res/android">
    <item android:state_pressed="true"
        android:drawable="@drawable/finger_pressed" />
    <item android:drawable="@drawable/finger" />
</selector>
```
* 위 xml 파일에서 최상위 태그인 `<selector>`태그 내에는 `<item>` 태그를 넣을 수 있으며, `<item>` 태그의   
  drawable 속성에는 이미지나 다른 그래픽을 설정하여 화면에 보여줄 수 있다.
* `state_`로 시작하는 속성은 상태를 나타내는데, 위 코드의 `state_pressed`의 경우는 눌린 상태를 의미한다.   
  또한 `state_focused`는 포커스를 받은 상태를 의미한다.
* 위 코드에서 `finger_pressed.jpg`는 View가 눌렸을 때 보이게 된다.
<hr/>

<h3>ShapeDrawable 만들기</h3>

* 아래의 xml 코드를 보자.
```xml
<!-- /app/res/drawable/rect_drawable.xml -->
<shape xmlns:android="http://schemas.android.com/apk/res/android"
    android:shape="rectangle">

    <size android:width="200dp" android:height="120dp" />
    <stroke android:width="1dp" android:color="#0000ff" />
    <solid android:color="#aaddff" />
    <padding android:bottom="1dp" />
</shape>
```
* 위 코드의 최상위 태그는 `<shape>` 태그이며, shape 속성으로 rectangle을 지정했다.   
  이 속성값은 사각형을 그린다는 뜻이며, 만약 oval을 지정하면 타원을 그릴 수 있다.
  * `<size>` : 도형의 크기를 지정하는 태그.
  * `<stroke>` : 도형의 테두리 속성을 지정하는 태그로, width는 선의 굵기, color는 선의 색상을 지정한다.
  * `<solid>` : 도형의 내부를 채우는 태그.
  * `<padding>` : 테두리 안쪽 공간을 띄우고 싶을 때 사용하는 태그.
  
* 아래 코드도 보자.
```xml
<shape xmlns:android="http://schemas.android.com/apk/res/android"
    android:shape="rectangle">

   <gradient
        android:startColor="#7288DB"
        android:centerColor="#3250B4"
        android:endColor="#254095"
        android:angle="90"
        android:centerY="0.5" />
    
    <corners android:radius="2dp" />
</shape>
```
* 위 코드처럼 `<shape>` 태그 내에 `<gradient>` 태그를 넣으면 그라데이션이 만들어진다.   
  startColor는 시작 부분의 색상을 지정하며, centerColor는 가운데 부분의 색상, endColor는 끝 부분의 색상을 지정한다.
<hr/>

<h2>Toast, Snackbar, 대화상자</h2>

* Toast는 간단한 메시지를 잠깐 보여주었다가 없어지는 View로, App 위에 떠 있는 View라 할 수 있다.   
  이는 대화상자와 함께 사용자에게 필요한 정보를 알려주는 역할을 하는 대표적인 위젯이다.   
  Toast는 focus를 받지 않기에 대화상자보다 더 쉽고 간단하게 사용할 수 있다.   
  Toast메시지를 만들어서 보여주는 전형적인 방법은 아래와 같다.
```java
Toast.makeText(Context context, String message, int duration).show();
```
* `Context` 객체는 일반적으로 `Context` 클래스를 상속한 Activity를 사용할 수 있으며, Activity를 참조할 수 없는 경우에는   
  `getApplicationContext()` 메소드를 호출하여 `Context` 객체를 받아올 수 있다. Toast를 보여주려면 message와 노출 시간을   
  duration 파라미터로 전달하고, `show()` 메소드를 호출하면 된다. Toast는 그 위치나 모양을 바꿀 수 있는데, 아래의 두 메소드는   
  Toast의 위치와 여백을 지정할 수 있도록 해준다.
```java
public void setGravity(int gravity, int xOffset, int yOffset);
public void setMargin(float horizontalMargin, float verticalMargin);
```
* `setGravity()` 메소드는 Toast가 보이는 위치를 지정할 때 사용되는데, gravity 파라미터는 `Gravity.CENTER`와 같이   
  정렬 위치를 지정한다. `setMargin()` 메소드는 외부 여백을 지정할 때 사용되는데, 이 값을 이용해 Toast를 중앙이나   
  우측 하단에 배치시킬 수 있다.
<hr/>

<h3>Toast 위치 바꿔 보여주기</h3>

* Toast를 EditText에 입력받은 좌표에 띄우는 코드를 작성해보자.
```xml
<!-- activity_main.xml -->
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    tools:context=".MainActivity">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="horizontal">
        
        <EditText
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:id="@+id/editText"
            android:layout_weight="1"
            android:textSize="20sp"
            android:hint="X 위치"
            android:inputType="numberSigned" />
    
        <EditText
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:id="@+id/editText2"
            android:layout_weight="1"
            android:textSize="20dp"
            android:hint="Y 위치"
            android:inputType="numberSigned"/>
        
        <Button
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:id="@+id/button"
            android:text="띄우기"
            android:textSize="20sp"
            android:onClick="onButton1Clicked"/>
    </LinearLayout>

</LinearLayout>
```

* 다음은 Java 소스코드를 작성하자.
```java
// MainActivity.java
public class MainActivity extends AppCompatActivity {
    
    EditText editText;
    EditText editText2;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        editText = findViewById(R.id.editText);
        editText2 = findViewById(R.id.editText2);
    }
    
    public void onButton1Clicked(View v) {
        try {
            Toast toastView = Toast.makeText(this, "Toast Message", Toast.LENGTH_LONG);
            int xOffset = Integer.parseInt(editText.getText().toString());
            int yOffset = Integer.parseInt(editText2.getText().toString());
            
            toastView.setGravity(Gravity.TOP | Gravity.TOP, xOffset, yOffset);
            toastView.show();
        } catch(NumberFormatException e) {
            e.printStackTrace();
        }
    }
}
```
<hr/>

<h3>Toast의 모양 바꿔 보여주기</h3>

* Toast는 위치 뿐만 아니라 그 모양도 바꿔 보여줄 수 있다. 기본적으로 보이는 Toast의 모양은 검정 계열의 바탕색과   
  흰 글자인데, UI에 따라 Toast 메시지의 색상을 바꿔야할 경우가 발생할 수 있다.
```java
public void onButton2Clicked(View v) {
    LayoutInflater inflater = getLayoutInflater();

    View layout = inflater.inflate(R.layout.toastborder, (ViewGroup)findViewById(R.id.toast_layout_root));
        
    TextView text = layout.findViewById(R.id.text);
        
    Toast toast = new Toast(this);
    text.setText("Changed shape toast");
    toast.setGravity(Gravity.CENTER, 0, -100);
    toast.setDuration(Toast.LENGTH_SHORT);
    toast.setView(layout);
    toast.show();
}
```
* 위 메소드에서는 `LayoutInflater` 객체를 사용해 XML로 정의된 layout을 메모리에 객체화 하고 있다.   
  이 과정은 XML Layout을 메모리에 로딩하는데 사용된다. Activity를 위해 만든 XML Layout 파일은 `setContentView()` 메소드를   
  사용해 Activity에 설정되지만, Toast만을 위한 Layout을 정의한다면 이 layout은 Activity를 위한 것이 아니기 때문에   
  `LayoutInflater` 객체를 사용해 직접 메모리에 객체화 해야 한다.

* 이제 위 소스코드의 대상이 되는 toastborder.xml 파일을 작성하자.
```xml
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:orientation="horizontal"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:padding="10dp">
    
    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:id="@+id/text"
        android:padding="20dp"
        android:textSize="32sp"
        android:background="@drawable/toast"/>

</LinearLayout>
```
<hr/>

<h3>Snackbar</h3>

* 간단한 메시지를 보여줄 때, Toast 대신 Snackbar를 사용하는 경우도 많다.   
  Snackbar는 외부 라이브러리로 추가됐기 때문에, Snackbar가 들어있는 Design Library를 추가해야 사용할 수 있다.   
* 의존 추가 방법 : `com.android:support:design` 검색 후 28.0.0 버전 추가
```java
public void onButton3Clicked(View v) {
    Snackbar.make(v, "This is snackbar.", Snackbar.LENGTH_LONG).show();
}
```
<hr/>

<h3>알림 대화상자</h3>

* 알림 대화상자는 사용자에게 확인을 받거나 선택하게 할 때 사용한다.   
  보통 알림 대화상자는 사용자의 입력을 받기 보다는 일방적으로 메시지를 전달하는 역할을 하며,   
  예 아니오와 같은 전형적인 응답을 처리한다.
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
                showMessage();
            }
        });
    }
    
    private void showMessage() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("INFO");
        builder.setMessage("QUIT?");
        builder.setIcon(android.R.drawable.ic_dialog_alert);
        
        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                String message = "YES button clicked.";
                textView.setText(message);
            }
        });
        
        builder.setNeutralButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String message="CANCEL button clicked.";
                textView.setText(message);
            }
        });
        
        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String message="NO button clicked.";
                textView.setText(message);
            }
        });
        
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
```
* `AlertDialog` 클래스는 알림 대화상자를 보여주는 가장 간단한 방법을 제공하는데,   
  `setTitle()` 메소드로 알림 대화상자의 제목을 설정하고, 내용은 `setMessage()` 메소드로 설정할 수 있다.   
  아이콘은 `setIcon()` 메소드로 지정하는데, `android.R.drawable`은 안드로이드 기본 API에 포함된 아이콘 중 하나를 사용한다는 것이다.   
  '예', '아니오'와 같은 버튼의 설정은 `setPositiveButton()`과 `setNegativeButton()` 메소드를 사용하여 설정하고,   
  이러한 메소드에는 `OnClickListener`를 설정할 수 있다.
<hr/>

<h2>ProgressBar</h2>

* ProgressBar는 어떤 작업의 진행 상태를 보여줄 수 있는 수단이다.  
  XML Layout에 ProgressBar를 추가할 때는 `<ProgressBar>` 태그가 사용되는데, 이 상태바의 최대 범위는   
  max 속성으로 설정하고, 현재 값은 progress 속성으로 설정할 수 있다.   
  Java 코드에서 Progressbar의 현재 값을 바꿀 때 사용되는 대표적인 메소드들은 다음과 같다.
```java
void setProgress(int progress);
void incrementProgressBy(int diff);
```
* `setProgress()` 메소드는 정수 값을 받아 progressbar의 현재 값으로 설정하고, `incrementProgressBy()` 메소드는 인자로 전달된   
  값을 기준으로 값을 더하거나 뺄 때 사용한다. ProgressBar는 항상 보일 필요가 없으므로 화면에서 보이는 공간을 줄일 수 있도록   
  타이틀바에 표시할 수 있는데, 이 기능은 Window 속성으로 정의되어 있으므로 다음 메소드를 사용해야 한다.
```java
requestWindowFeature(Window.FEATURE_PROGRESS);
```
* 타이틀바에 표시되는 progressbar는 범위를 따로 지정할 수 없으며 default 값으로는 0부터 1000 사이의 값을 가질 수 있다.
* 다음은 두 가지 형태의 ProgressBar 이다.
<table>
    <tr>
        <td>막대 모양</td>
        <td>작업 진행 정도를 막대 모양으로 표시하며, style 속성값을 '?android:attr/progressBarStyleHorizontal'로 설정하면 된다.</td>
    </tr>
    <tr>
        <td>원 모양</td>
        <td>작업이 진행중임을 알려주며, 원 모양으로 된 ProgressBar가 반복적으로 표시된다.</td>
    </tr>
</table>

* 아래 소스 코드는 ProgressBar를 사용한 간단한 소스 코드이다.
```java
public class MainActivity extends AppCompatActivity {

    ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ProgressBar progressBar = findViewById(R.id.progressBar);
        progressBar.setIndeterminate(false);
        progressBar.setProgress(80);

        Button button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog = new ProgressDialog(MainActivity.this);
                dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                dialog.setMessage("Validating Data..");
                dialog.show();
            }
        });

        Button button2 = findViewById(R.id.button2);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
    }
}
```
* 위 코드에서는 XML Layout에 있는 ProgressBar를 `findViewById()` 메소드로 찾은 후, 그 값을 80으로 설정한다.   
  버튼이 클릭됐을 때는 ProgressBar 대화샂아가 표시되도록 한다. 멈추지 않는 ProgressBar를 대화상자 내에서 보여주려면   
  `ProgressDialog` 객체를 하나 만들고 그 스타일을 `ProgressDialog.STYLE_SPINNER`로 설정하면 된다. 이렇게 만든   
  `ProgressDialog` 객체는 `show()` 메소드를 호출하면 화면에 표시된다.   
* `ProgressDialog` 객체를 생성할 때는 `Context` 객체가 파라미터로 전달돼야 하는데, 위 경우에는 Activity인   
  `MainActivity` 객체를 전달하기 위해 `MainActivity.this`를 파라미터로 전달했다.   
  위 코드를 실행하면 Progress 대화상자가 보이는 영역 밖을 터치하면 ProgressBar가 사라지는데, 어떤 이벤트가 발생했을 때   
  대화상자를 보이지 않게 하고 싶다면 `dismiss()` 메소드를 호출하면 된다.