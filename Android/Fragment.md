Fragment
======

<h2>Fragment</h2>

* 여러 개의 화면을 구성할 때는 보통 각각의 화면을 Activity로 만든 후 Activity를 전환하는 방법을 사용한다.   
  하지만 화면의 일부분을 다른 화면으로 구성하고 싶다면 이 방법을 사용하면 안된다.
<hr/>

<h3>Fragment에 대해 이해하기</h3>

* 하나의 화면을 구성하는 XML Layout을 만들 때에는 `LinearLayout`안에 또 다른 `LinearLayout`을 넣을 수 있다.   
  즉 부분 화면은 전체 화면으로 만든 layout안에 들어있는 또 다른 layout인 것이다. 이 방법을 사용하면 코드가 복잡해질 수 있다.

* 하나의 화면 안에 여러 개의 Activity를 부분 화면으로 올려서 보여주는 방법은 `Activity` 클래스의 `ActivityGroup`을 사용하면   
  구현할 수 있다. 하지만 Activity는 하나의 화면을 독립적으로 구성할 때 필요한 여러 가지 속성들을 사용하게 되며, 안드로이드 시스템에서   
  관리하는 app 구성 요소이기 때문에 Activity내에 다른 Activity를 넣는 것은 단말의 리소스를 많이 사용하는 비효율적인 방법이다.

* 위 두 방법을 해결하는 것이 `Fragment`인데, 이는 하나의 화면을 여러 부분으로 나눠서 보여주거나 각각의 부분화면 단위로 바꿔서   
  보여주고 싶을 때 사용하는 것이다. `Fragment`는 하나의 화면 내에 들어가는 부분 화면과 같아서 하나의 Layout처럼 보인다. 하지만   
  Activity처럼 독립적으로 동작하는 부분 화면을 만들 때 사용된다.

* `Fragment`가 사용되는 목적은 다음과 같다.
  * 분할된 화면들을 독립적으로 구성하기 위해 사용.
  * 분할된 화면들의 상태를 관리하기 위해 사용.

* __Fragment는 항상 Activity위에 올라가있어야 한다__. Activity로 만든 화면을 분할한 뒤 각각의 분할화면을 Fragment로 만들고   
  그 Fragment들을 독립적으로 관리하는 것이 목표이기 때문에 Fragment는 Activity위에 올라가 있어야 제 역할을 할 수 있다.   
  따라서 Fragment가 제대로 동작하는 시점은 __Fragment가 메모리에 올라간 시점이 아닌, Activity에 올라가는 시점__ 이다.

* `Activity`가 `ActivityManager`에 의해 `Intent`객체를 전달받을 수 있다면, `Fragment`는 `FragmentManager`를 통해 메소드 호출을 한다.   
  Activity는 App 구성 요소 이므로 안드로이드 시스템에서 관리했다면, 즉 ActivityManager가 Activity의 동작 순서나 처리 방식을 결정했다면   
  Fragment에서는 Activity가 Activity의 ActivityManager가 하는 역할을 담당한다.   
  또한 Fragment끼리 데이터를 주고받을 때에는 `Intent`를 사용할 수 없으며, 단순히 메소드를 만들고 호출하는 방식을 사용한다.

* Activity는 시스템에서 관리하지만 Fragment는 Activity위에 올라가 있어 Activity를 전환하지 않고도 훨씬 가볍게 화면 전환 효과를   
  만들 수 있다. 특히 탭 모양으로 화면을 구성할 때 각각의 `탭` 버튼을 클릭할 때 마다 다른 화면이 보이는 효과를 내고 싶다면   
  Activity가 아닌 Fragment를 사용하는 것이 좋다.
<hr/>

<h3>Fragment를 화면에 추가하는 방법</h3>

* Fragment는 Activity를 본떠 만들어졌기 때문에 Fragment를 만드는 방법도 Activity를 만드는 과정과 비슷하다.   
  Fragment도 하나의 XML Layout파일과 하나의 Java 소스 파일로 동작하게 만든다.

```xml
<fragment
    android:id="@+id/fragment"
    android:name="org.techtown.fragment.MainFragment"
    android:layout_width="match_parent"
    android:layout_height="match_parent" />
```
* Fragment를 위한 자바 소스는 `Fragment` 클래스를 상속하여 만들 수 있다.   
  아래는 `Fragment`클래스에 있는 주요 메소드들이다.
```java
public final Activity getActivity(); 
// 이 Fragment를 포함하는 Activity 반환

public final FragmentManager getFragmentManager();  
// 이 Fragment를 포함하는 Activity에서 Fragment 객체들과 소통하는 FragmentManager 반환

public final Fragment getParentFragment();
// 이 Fragment를 포함하는 부모가 Fragment일 경우, 그 부모를 반환한다.
// 만약 부모가 Fragment가 아닌 Activity라면 null을 반환한다.

public final int getId();
// 이 Fragment의 id값 반환
```

* Fragment에는 `setContentView()` 메소드가 없고, `LayoutInflater` 객체를 사용해 inflation을 진행해야 한다.   
  XML Layout파일의 내용을 inflation한 후 클래스에서 사용하도록 하는 코드는 `onCreateView()` 메소드 안에 들어간다.   
  `onCreateView()`는 콜백 메소드로, inflation이 필요한 시점에 자동으로 호출된다. 따라서 이 메소드 내에서 inflation을 위한   
  `inflate()` 메소드를 호출하면 되고, inflation과정이 끝나면 Fragment가 하나의 View처럼 동작할 수 있는 상태가 된다.

* `Fragment`는 Button, Layout처럼 화면의 일정 공간을 할당받을 수 있으므로 새로 만든 Fragment를 MainActvity에 추가하는 방법은   
  View와 마찬가지로 XML Layout에 `<fragment>` 태그를 사용해 추가할 수 있고, 새로 정의한 Fragment클래스의 인스턴스 객체를   
  new 연산자로 만든 후 `FragmentManager#add()` 메소드를 사용해 Activity에 추가할 수 도 있다.

* `FragmentManager` 클래스에 들어 있는 주요 메소드들은 아래와 같다.
```java
public abstract FragmentTransaction beginTransaction();
// Fragment를 변경하기 위한 transaction을 시작한다.

public abstract Fragment findFragmentById(int id);
// ID를 이용해 Fragment 객체를 찾아 반환한다.

public abstract Fragment findFragmentByTag(String tag);
// Tag 정보를 이용해 Fragment 객체를 찾아 반환한다.

public abstract boolean executePendingTransactions();
// transaction은 commit() 메소드를 호출하면 실행되지만, 비동기 방식으로 실행되므로
// 즉시 실행하고 싶다면 이 메소드를 추가적으로 호출해야 한다.
```

* `FragmentManager` 객체는 Fragment를 Activity에 추가(add), 다른 Fragment로 바꾸거나(replace), 삭제(remove)할 때 사용할 수 있으며,   
  `getFragmentManager()` 메소드를 호출하면 참조할 수 있다.
* 참고 : `getSupportFragmentManager()` 메소드는 `getFragmentManager()`와 같은 작업을 하지만, 이전 버전까지의 호환성을 보장해준다.

* Fragment는 다음과 같은 특성을 가지며, 큰 화면과 해상도를 가진 Tablet의 경우에 더욱 유용하게 사용될 수 있다.
<table>
    <tr>
        <td>View 특성</td>
        <td>ViewGroup에 추가되거나 Layout의 일부가 될 수 있다.</td>
    </tr>
    <tr>
        <td>Activity 특성</td>
        <td>Actvity처럼 생명주기를 가진다.</td>
    </tr>
</table>

* Fragment 클래스는 보통 `Fragment`클래스를 상속하도록 만들지만, Fragment중에는 미리 정의된 몇 가지 Fragment 클래스들이 있어   
  그 클래스들을 그대로 사용할 때도 있다. 예를 들어 `DialogFragment`는 Activity의 수명주기에 의해 관리되는 대화상자를 보여준다.   
  이 Fragment는 Activity의 기본 대화상자 대신 사용할 수 있다.
<hr/>

<h3>Fragment를 만들어 화면에 추가하기</h3>

* `New Fragment(Blank)`를 클릭하여 `MainFragment.java`파일을 만들고, `fragment_main.xml` 파일에 TextView와 Button을 추가하자.   
  아래는 `MainFragment.java`이다.
```java
public class MainFragment extends Fragment {
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }
}
```
* `onCreateView()`의 인자로 `LayoutInflater` 객체가 전달되므로 이에 대해 바로 `inflate()` 메소드를 호출했다.   
  `inflate()` 메소드의 첫 번째 인자로는 XML Layout파일인  `R.layout.fragment_main`이 입력돼있고,   
  두 번째는 이 XML Layout이 설정된 ViewGroup인데, `onCreateView()` 메소드의 두 번째 인자가 이 Fragment의 최상위 layout이다.   
  따라서 container를 그대로 전달했다. `inflate()` 메소드를 호출하면 inflation이 진행되고 그 결과로 `ViewGroup` 객체가 반환된다.

* 이제 새로운 Fragment가 만들어졌으니, 이 Fragment를 MainActivity에 추가하는 방법을 알아보자. 방법 중 하나는 `activity_main.xml`에   
  태그로 추가하는 방법이고, 다른 하나는 MainActivity.java에서 추가하는 방법이다. 태그로 추가하는 방법은 아래와 같다.
```xml
<!-- activity_main.xml -->
<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:id="@+id/container">

    <fragment
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:id="@+id/mainFragment"
        android:name="com.techtown.fragment.MainFragment"/>

</RelativeLayout>
```
* 위 코드에서 최상위 layout은 `RelativeLayout`이며, id 속성값을 container로 지정했다. 그리고 `<RelativeLayout>` 태그 내에   
  `<fragment>` 태그를 추가했다. Fragment는 View와 달라서 View를 담고 있는 공간만 확보한다. 따라서 태그명으로 Fragment의 이름을   
  사용할 수 없으며, name 속성에 새로 만든 `MainFragment`의 이름을 설정했다. Fragment의 이름 설정 시에는 패키지명을 포함한 이름으로 설정하고   
  Fragment의 id값은 mainFragment로 한다.
<hr/>

<h3>Button클릭 시 코드에서 Fragment 추가하기</h3>

* 위 과정에서 새로운 Fragment를 만들어 Activity 화면에 올리는 과정은 다음과 같다.
  1. Fragment를 위한 XML Layout 만들기.
  2. Fragment 클래스 만들기.
  3. Fragment를 Main Activity의 XML Layout에 추가하기

* Fragment도 새로 만든 Fragment를 Activity의 XML Layout에 넣어 화면에 추가하는 것 뿐만 아니라 코드에서 직접 추가할 수 있다.   
  코드에서 Fragment를 추가할 때는 `FragmentManager`에 요청해야 한다. 

```java
// MenuFragment.java
public class MenuFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_menu, container, false);
    }
}
```
* 위 코드에서 `inflate()` 메소드로 전달되는 첫 번째 인자의 값이 `R.layout.fragment_menu`이므로 이 `MenuFragment`클래스에는   
  `fragment_menu.xml` 파일의 내용이 inflation되어 설정된다.

```xml
<!-- fragment_menu.xml -->
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:background="@android:color/holo_orange_light"
    tools:context=".MainFragment">

    <TextView
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="MAIN FRAGMENT"
        android:textSize="30sp"/>
    <Button
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="TO MENU"/>

</LinearLayout>
<!-- 이 파일은 fragment_main.xml 에 background만 orange로 추가해줬다.-->
```

* 이제 `MainFragment.java`를 수정하여 버튼 클릭 시, MenuFragment로 전환되도록 해보자.
```java
// MainFragment.java

public class MainFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_main, container, false);
        
        Button button = rootView.findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity activity = (MainActivity)getActivity();
                activity.onFragmentChanged(0);
            }
        });
        return rootView;
    }
}
```
* `MainFragment`내에 표시되는 최상위 Layout은 inflation을 통해 참조한 rootView 객체이다.   
  최상위 Layout인 rootView는 MainFragment 안에 들어있는 것이며, MainFragment는 이 layout을 보여주기 위한 틀이다.   
  따라서 rootView의 `findViewById()` 메소드를 사용하여 Layout에 들어있는 Button객체를 찾을 수 있는 것이다.
```java
// MainActivity.java

public class MainActivity extends AppCompatActivity {
    
    MainFragment mainFragment;
    MenuFragment menuFragment;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        mainFragment = (MainFragment)getSupportFragmentManager().findFragmentById(R.id.mainFragment);
        menuFragment = new MenuFragment();
    }
    
    public void onFragmentChanged(int index) {
        if(index == 0) {
            getSupportFragmentManager().beginTransaction().replace(R.id.container, menuFragment).commit();
        }
        else if(index == 1) {
            getSupportFragmentManager().beginTransaction().replace(R.id.container, mainFragment).commit();
        }
    }
}
```

* `MainFragment`는 `activity_main.xml`에 추가되어 있으므로 id를 사용해서 찾을 수 있는데, View가 아니기에 `Activity`클래스에 있는   
  `findViewById()`로 찾을 수 없다. 대신 `FragmentManager#findFragmentById()`를 사용해서 참조해야 한다.

* `onFragmentChanged()` 메소드는 Fragment에서 호출할 수 있도록 정의한 것으로, 인자로 전달된 값이 0이면 `MenuFragment`가 보이게 하고,   
  1이면 `MainFragment`가 보이게 했다. 이 메소드 내에서는 `FragmentManager#replace()`를 사용해 Fragment를 바꾸도록 했다.   
  `replace()`의 첫 번째 인자는 해당 Fragment를 담고 있는 layout의 id 이므로 R.id.container를 전달했다.

* `FragmentManager`는 Fragment를 다루는 작업을 해주는 객체로, Fragment의 추가, 교체, 삭제 등의 작업을 하는데, Fragment변경 시   
  오류가 발생할 수 있기에 `Transaction`객체를 만들어 실행한다. `Transaction`객체는 `beginTransaction()`를 호출하면 시작되고,   
  `commit()` 메소드를 호출하면 실행된다.
<hr/>

<h3>Fragment의 생명 주기</h3>

* 