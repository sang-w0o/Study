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

* Fragment는 Activity와 마찬가지로 상태에 따라 API에서 미리 정해둔 callback함수가 호출되므로 그 안에 필요한 기능을 넣을 수 있다.   
  Fragment를 사용하는 목적 중 하나가 분할된 화면들의 상태를 관리하는 것인데, 이를 가능하게 하는 것이 생명주기 메소드들이다.   
  즉, Activity안에 들어 있는 Fragment도 필요할 때 화면에 보이거나 보이지 않게 되므로 Activity처럼 각각의 상태를 관리해야 한다.

* Fragment는 Activity위에 올라가는 것이므로 Fragment의 생명주기도 Activity의 생명주기에 종속적이지만 Fragment만 가질 수 있는   
  독립적인 상태 정보들이 존재한다. 특히 Fragment가 화면에 보이기 전이나 중지 상태가 됐을 때 Activity처럼 `onResume()` 메소드와   
  `onPause()` 메소드가 호출되는데, Fragment는 Activity에 종속돼 있으므로 이 상태 메소드 외에도 세분화된 상태 메소드들이 있다.
<table>
    <tr>
        <td>onAttach(Activity)</td>
        <td>Fragment가 Activity와 연결될 때 호출된다.</td>
    </tr>
    <tr>
        <td>onCreate(Bundle)</td>
        <td>Fragment가 초기화될 때 사용된다. (new 연산자를 통해 새로운 Fragment 객체가 생성될 때 호출되는 것이 아님에 주의한다.)</td>
    </tr>
    <tr>
        <td>onCreateView(LayoutInflater, ViewGroup, Bundle)</td>
        <td>Fragment와 관련되는 View 계층을 만들어 반환한다.</td>
    </tr>
    <tr>
        <td>onActivityCreated(Bundle)</td>
        <td>Fragment와 연결된 Activity가 onCreate() 메소드의 작업을 완료했을 때 호출된다.</td>
    </tr>
    <tr>
        <td>onStart()</td>
        <td>Fragment와 연결된 Activity가 onStart() 되어 사용자에게 Fragment가 보일 때 호출된다.</td>
    </tr>
    <tr>
        <td>onResume()</td>
        <td>Fragment와 연결된 Activity가 onResume() 되어 사용자와 상호작용을 할 수 있을 때 호출된다.</td>
    </tr>
</table>

* 위 표의 메소드들은 Fragment가 처음 화면에 만들어지고 화면에 나타나기 전에 호출되는 메소드들이다.   
  Activity와 마찬가지로 Fragment도 메모리에 올라갈 때(초기화 될 때) `onCreate()` 메소드가 호출된다.   
  단, __Fragment는 Activity안에 추가되어 사용되면서 동시에 Activity에 종송되어 있어 Fragment와 Activity가 연결되어야 초기화될 수 있다__.   
  즉 Fragment는 Activity위에 올라가야 제대로 동작한다. 이 때문에 new 연산자를 사용해 Fragment 객체를 만드는 시점과 `onCreate()`가   
  호출되는 시점이 달라진다.

* 과정을 순서대로 보면 먼저 `onAttach()` 메소드가 호출되며 Activity에 Fragment가 추가되고, 그 다음에 `onCreate()`가 호출된다.   
  다시 말해 `onAttach()`가 호출될 때 인자로 전달되는 `Activity` 객체 위에 Fragment가 올라가 있게 된다.   
  그러므로 Activity를 위해 설정해야하는 정보들은 `onAttach()` 메소드에서 처리해야 한다.   
  Fragment가 새로 만들어질 때 그 안에 View들을 포함하게 되면 ViewGroup처럼 View들의 Layout을 결정해야 한다.   
  `onCreateView()`는 Fragment와 관련되는 View들의 계층도를 구성하는 과정에서 호출된다.   
  Activity의 경우, 메모리에 처음 만들어질 때 `onCreate()`가 호출되지만 Fragment는 `onCreateView()` 메소드가 호출된다.   
  이 메소드는 Fragment에서 Activity가 만들어지는 상태를 알 수 있도록 해주는데, Fragment에서 재정의한 `onCreate()` 메소드와 구별해야 한다.

* 아래는 Fragment가 화면에서 보이지 않게 되면서 호출되는 상태 메소드들이다.
<table>
    <tr>
        <td>onPause()</td>
        <td>Fragment와 연결된 Activity가 onPause() 되어 사용자와 상호작용을 중지할 때 호출된다.</td>
    </tr>
    <tr>
        <td>onStop()</td>
        <td>Fragment와 연결된 Activity가 onStop() 되어 화면에서 더 이상 보이지 않을 때, 또는 Fragment의 기능이 중지되었을 때 호출된다.</td>
    </tr>
    <tr>
        <td>onDestroyView()</td>
        <td>Fragment와 관련된 View 리소스들을 해제할 수 있도록 호출된다.</td>
    </tr>
    <tr>
        <td>onDestroy()</td>
        <td>Fragment의 상태를 마지막으로 정리할 수 있도록 호출된다.</td>
    </tr>
    <tr>
        <td>onDetach()</td>
        <td>Fragment가 Activity와 연결을 끊기 바로 전에 호출된다.</td>
    </tr>
</table>

* `onPause()`, `onStop()` 메소드는 Activity의 `onPause()`, `onStop()`이 호출될 때와 같은 상태 메소드이다.   
  `onDestroyView()` 메소드는 Fragment 내에 있는 View들의 리소스를 해제할 때 재정의하여 사용하며, `onDetach()` 메소드는   
  `onAttach()`와 반대로 Fragment가 Activity와의 연결을 끊기 바로 전에 사용된다.
<hr/>

<h2>Fragment로 화면 만들기</h2>

* 먼저 `fragment_list.xml`에 3개의 버튼 (각각의 id : button, button1, button2)를 추가하자.

* 다음에는 `ListFragment.java` 파일을 만들자.
```java
public class ListFragment extends Fragment {

    public static interface ImageSelectionCallback {
        public void onImageSelected(int position);
    }

    public ImageSelectionCallback callback;
    
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        
        if(context instanceof ImageSelectionCallback) {
            callback = (ImageSelectionCallback) context;
        }
    }
    
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup)inflater.inflate(R.layout.fragment_list, container, false);
        
        Button button = rootView.findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(callback != null) {
                    callback.onImageSelected(0);
                }
            }
        });
        
        Button button2 = rootView.findViewById(R.id.button2);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(callback != null) {
                    callback.onImageSelected(1);
                }
            }
        });
        
        Button button3 = rootView.findViewById(R.id.button3);
        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(callback != null) {
                    callback.onImageSelected(2);
                }
            }
        });
        
        return rootView;
    }
}
```
* 각각의 Button을 클릭했을 때는 `callback#onImageSelected()` 메소드를 호출한다. `onAttach()` 메소드는 Fragment가 Activity위에   
  올라오는 시점에 호출된다. 따라서 Fragment에서 Activity객체를 참조하고 싶다면 `onAttach()`로 전달되는 인자로 참조하거나,   
  `getActivity()` 메소드를 호출하여 반환되는 객체를 참조할 수 있다. 그리고 그 객체를 변수에 할당하면 Fragment 클래스 내에서   
  자유롭게 참조할 수 있게 된다.

* 위에서는 callback 변수의 자료형을  `ImageSelectionCallback`로 지정했다. 화면에서 선택된 버튼에 따라 다른 Fragment의   
  이미지를 바꿔주려면 Activity쪽으로 데이터를 전달해야 하므로 Activity에 `onImageSelected()` 메소드를 정의한 후 그 메소드를   
  호출하도록 했다. 즉, 만약 `MainActivity`가 `ImageSelectionCallback` 인터페이스를 구현하도록 한다면 이 Fragment에서는   
  Activity 객체를 참조할 때 인터페이스 타입으로 참조한 후 `onImageSelected()` 메소드를 호출할 수 있다.   
  `onAttach()` 메소드 안에서는 `MainActivity`객체를 참조한 후 `ImageSelectionCallback`타입인 callback 변수에 할당한다.

* 다음으로는 이미지를 보여줄 Fragment인 `ViewFragment.java`와 `fragment_viewer.xml`을 만들자.
```xml
<!-- fragment_viewer.xml -->
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    tools:context=".ViewerFragment">

   <ImageView
       android:layout_width="match_parent"
       android:layout_height="wrap_content"
       android:src="@drawable/dream1"
       android:id="@+id/imageView"
       />

</LinearLayout>
```

```java
// ViewerFragment.java
public class ViewerFragment extends Fragment {

    ImageView imageView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_viewer, container, false);

        imageView = rootView.findViewById(R.id.imageView);
        return rootView;
    }
    
    public void setImage(int resId) {
        imageView.setImageResource(resId);
    }
}
```

* 이제 2개의 Fragment를 만들었으니, 이들을 `activity_main.xml`에 추가하자.
```xml
<!-- activity_main.xml -->
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    tools:context=".MainActivity">
    
    <fragment
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:layout_weight="1"
        android:name="com.techtown.samplefragment2.ListFragment"
        android:id="@+id/listFragment"/>
    
    <fragment
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:layout_weight="1"
        android:name="com.techtown.samplefragment2.ViewerFragment"
        android:id="@+id/viewerFragment" />

</LinearLayout>
```

* 마지막으로 `MainActivity.java`는 다음과 같다.
```java
public class MainActivity extends AppCompatActivity implements ListFragment.ImageSelectionCallback {

    ListFragment listFragment;
    ViewerFragment viewerFragment;

    int[] images = {R.drawable.dream1, R.drawable.dream2, R.drawable.dream3};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FragmentManager manager = getSupportFragmentManager();
        listFragment = (ListFragment)manager.findFragmentById(R.id.listFragment);
        viewerFragment = (ViewerFragment)manager.findFragmentById(R.id.viewerFragment);
    }

    @Override
    public void onImageSelected(int position) {
        viewerFragment.setImage(images[position]);
    }
}
```
<hr/>

<h2>Action Bar 사용하기</h2>

* 안드로이드 OS가 업그레이드 되면서 단말에 `하드웨어[메뉴] 버튼`이 없어도 화면 내에 `[시스템]` 버튼이 표시될 수 있게 되었다.   
  이렇게 `시스템[메뉴] 버튼`을 눌렀을 때 숨어있던 메뉴가 보이도록 할 수도 있고, app의 상단 버튼을 눌러 메뉴가 보이게 할 수 있다.   
  이런 메뉴를 `Option Menu`라 한다. 그리고 Option menu와 다르게 입력상자를 길게 눌러 나타나는 "복사하기", "붙여넣기" 등의 팝업   
  형태의 메뉴는 `Context Menu`라 한다. Option Menu는 각각의 화면마다 설정할 수 있으며 Context Menu는 각각의 View마다 설정할 수 있다.

<table>
    <tr>
        <td>Option Menu</td>
        <td>시스템 메뉴 버튼을 눌렀을 때 나타나는 메뉴로, 각 화면마다 설정할 수 있는 주요 메뉴.</td>
    </tr>
    <tr>
        <td>Context Menu</td>
        <td>화면을 길게 누르면 나타나는 메뉴로 View에 설정하여 나타나게 할 수 있다. TextView의 편집 상태를 바꾸거나 할 때 사용한다.</td>
    </tr>
</table>

* Option Menu는 `Action Bar`에 포함되어 보이도록 만들어져 있다.   
  Action Bar는 app의 제목이 보이는 위쪽 부분을 말한다. Option Menu와 Context Menu는 각각의 Activity마다 설정할 수 있으므로   
  Activity에 추가하고 싶은 경우에는 다음의 두 메소드를 재정의하여 메뉴 아이템을 추가할 수 있다.
```java
public boolean onCreateOptionsMenu(Menu menu);
public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo);
```

* 위 두 메소드를 보면 `Menu`나 `ContextMenu`객체가 인자로 전달 되는 것을 알 수 있는데, 이 객체들의 `add()` 메소드를 사용해서   
  메뉴 아이템을 추가할 수 있다. 메뉴 아이템을 추가하는 대표적인 메소드들은 아래와 같다.
```java
MenuItem add(int groupId, int itemId, int order, CharSequence title);
MenuItem add(int groupId, int itemId, int order, int titleRes);
SubMenu addSubMenu(int titleRes);
```

* groupId는 아이템을 하나의 그룹으로 묶을 때 사용하며, itemId는 아이템이 갖는 고유의 ID값으로, 아이템이 선택됐을 때   
  각각의 아이템을 구분할 때 사용할 수 있다. 아이템이 많아서 Sub Menu로 추가하고 싶을 때는 `addSubMenu()` 메소드를 사용한다.   
  하지만 이 방법보다는 XML에서 메뉴의 속성을 정의한 후 객체로 로딩하여 참조하는 것이 더 간단하다.

* 안드로이드 스튜디오는 `/app/res/menu` 폴더 안에 메뉴를 위한 XML 파일이 들어가는 것을 알고 있다. 따라서 Menu관련   
  XML 파일을 작성하고 싶으면 `/app/res/` 하위에 menu 폴더를 추가해야 한다.
```xml
<!-- /app/res/menu/menu_main.xml -->
<menu xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app ="http://schemas.android.com/apk/res-auto">

    <item android:id="@+id/menu_refresh"
        android:title="REFRESH"
        android:icon="@drawable/dream1"
        app:showAsAction="always"/>

    <item android:id="@+id/menu_search"
        android:title="SEARCH"
        android:icon="@drawable/dream2"
        app:showAsAction="always"/>

    <item
        android:id="@+id/menu_settings"
        android:title="SETTINGS"
        android:icon="@drawable/dream3"
        app:showAsAction="always"/>
</menu>
```
* `<item>`태그는 하나의 메뉴에 대한 정보를 담고 있다. id 속성은 각각의 메뉴를 구분하기 위해 사용되며, title속성은 메뉴에   
  표시되는 글자이다. 아이콘을 표시하고 싶을 때는 icon 속성에 이미지를 넣을 수 있다. showAsAction속성은 이 메뉴를 항상 보이게   
  할 것인지 아닌지, 아니면 숨겨둘 것인지를 지정할 수 있다. 위 코드에서는 always로 지정했으므로 메뉴 아이콘이 항상 보인다.   
  `android:`로 시작하는 속성은 기본 API의 속성이고, `app:`로 시작하는 속성은 해당 프로젝트에 들어 있는 속성이다.

* 아래는 showAsAction 속성에 지정할 수 있는 값들이다.
<table>
    <tr>    
        <td>always</td>
        <td>항상 ActionBar에 아이템을 추가하여 표시한다.</td>
    </tr>
    <tr>    
        <td>never</td>
        <td>ActionBar에 아이템을 추가하여 표시하지 않는다. (기본값)</td>
    </tr>
    <tr>    
        <td>ifRoom</td>
        <td>ActionBar에 여유 공간이 있을 때만 아이템을 표시한다.</td>
    </tr>
    <tr>    
        <td>withText</td>
        <td>title 속성으로 설정된 제목을 같이 표시한다.</td>
    </tr>
    <tr>    
        <td>collapseActionView</td>
        <td>아이템에 설정한 View(actionViewLayout)의 아이콘만 표시한다.</td>
    </tr>
</table>

* `MainActivity.java`에 재정의된 `onCreateOptionsMenu()` 메소드는 Activity가 만들어질 때 미리 자동 호출되어, 화면에 메뉴 기능을   
  추가할 수 있도록 한다. 위에서 만든 메뉴 XML 파일은 XML Layout처럼 소스 코드에서 inflation한 후 메뉴에 설정할 수 있다.   
  이때 메뉴를 위한 XML 정보를 메모리에 로딩하기 위해 `MenuInflater` 객체를 사용한다.
```java
// MainActivity.java
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int curId = item.getItemId();
        switch(curId) {
            case R.id.menu_refresh:
                Toast.makeText(this,"refresh menu selected.", Toast.LENGTH_LONG).show();
                break;
            case R.id.menu_search:
                Toast.makeText(this, "search menu selected.", Toast.LENGTH_LONG).show();
                break;
            case R.id.menu_settings:
                Toast.makeText(this, "settings menu selected.", Toast.LENGTH_LONG).show();
        }
        return super.onOptionsItemSelected(item);
    }
}
```

* 만약 화면이 처음 만들어질 때 메뉴를 정해놓은 것이 아니라 화면이 띄워진 이후에 메뉴를 바꾸고 싶다면 `onPrepareOptionsMenu()`메소드를   
  재정의하면 된다. 이 메소드는 메뉴가 새로 보일 때마다 호출되므로 메뉴 항목을 추가하거나 뺄 수 있어 메뉴 아이템들을 변경할 수 있다.   
  특히 메뉴의 속성을 바꿀 수 있으므로 메뉴를 활성화,비활성화하여 사용자에게 app의 상태에 따라 메뉴를 사용하거나 사용하지 못하게 할 수 있다.

* 메뉴를 선택했을 때 처리하는 방법은 위에서 재정의한 `onOptionsMenuSelected()`를 사용하면 된다. 사용자가 하나의 메뉴 아이템을   
  선택했을 때 이 메소드는 자동으로 호출되며, item 객체가 인수로 넘어오기에 item객체에 대헤 `getId()`를 수행하여 item의 id를 가져오고,   
  각 id에 맞게 처리할 수 있다.

* Option Menu를 Activity에 등록하고 사용자가 옵션 메뉴를 선택했을 때 알맞게 처리하기 위해 두 개의 메소드를 재정의한 것 처럼   
  Context Menu도 두 개의 메소드를 재정의해서 사용하면 된다. 우선 Context Menu를 특정 View에 등록하고 싶으면 `registerForContextMenu()`   
  메소드를 사용하고, 이 메소드로 Context Menu를 등록하고 사용자가 각각의 메뉴 아이템을 선택했을 때는 `onContextItemSelected()`   
  메소드를 사용하는데, 이 메소드의 인자로 들어오는 `MenuItem` 객체를 사용해 해당 아이템의 정보를 확인한 후 처리할 수 있다.
```java
void Activity.registerForContextMenu(View view);
```

* Activity의 위쪽에 보이는 타이틀 부분과 옵션 메뉴는 ActionBar로 합쳐져 보이게 된다.   
  먼저 ActionBar는 기본적으로 제목을 보여주는 타이틀의 기능을 하므로 app의 제목을 보여줄 수 있으며, 화면에 보이거나 보이지 않도록   
  만들 수 있다. 소스 코드에서 ActionBar를 보이게 하고 싶다면 아래 코드 처럼 `show()` 메소드를 호출하고, 감추고 싶다면 `hide()`를 사용한다.
```java
Actionbar abar = getActionBar();
abar.show();
abar.hide();
```

* `setSubtitle()` 메소드를 사용하면 타이틀의 부제목을 달아줄 수 도 있다.

```java
public class MainActivity extends AppCompatActivity {
    
    ActionBar abar;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        abar = getSupportActionBar();
        
        Button button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                abar.setLogo(R.drawable.home);
                abar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME| ActionBar.DISPLAY_USE_LOGO);
            }
        });
    }
}
```
* ActionBar는 `android.support.v7.app` 패키지의 클래스를 import하여 사용한다.   
  위 코드에서는 `onCreate()` 내에서 `getSupportActionBar()`를 사용하여 XML Layout에 들어 있는 ActionBar 객체를 참조한다.   
  `ActionBar`객체는 XML Layout에 직접 추가할 수도 있고, Activity에 적용한 테마에 따라 자동으로 부여될 수도 있다.   
  버튼이 클릭됐을 때 ActionBar가 보이는 모양을 바꾸도록 `ActionBar#setDisplayOptions()` 메소드를 사용한다.   
  이 메소드에는 미리 정의된 상수들이 인자로 전달될 수 있으며, 그 상수들은 아래와 같다.
<table>
    <tr>    
        <td>DISPLAY_USE_LOGO</td>
        <td>홈 아이콘 부분에 로고 아이콘을 사용한다.</td>
    </tr>
    <tr>    
        <td>DISPLAY_SHOW_HOME</td>
        <td>홈 아이콘을 표시하도록 한다.</td>
    </tr>
    <tr>    
        <td>DISPLAY_HOME_AS_UP</td>
        <td>홈 아이콘에 뒤로 가기 모양의 < 아이콘을 같이 표시한다.</td>
    </tr>
    <tr>    
        <td>DISPLAY_SHOW_TITLE</td>
        <td>타이틀을 표시하도록 한다.</td>
    </tr>
</table>

* 이번에는 ActionBar에 검색어를 입력할 수 있는 입력 상자를 넣어보자.
```xml
<!-- search_layout.xml -->
<LinearLayout
    xmlns:android="http://schemas.android.com/apk/res/android" 
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="horizontal">
    
    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Search : "
        android:textSize="16sp"
        android:textColor="#ffad8745"/>
    
    <EditText
        android:layout_width="100dp"
        android:layout_height="wrap_content"
        android:id="@+id/editText"
        android:layout_marginLeft="4dp"
        android:inputType="text"
        android:imeActionId="1337"
        android:imeOptions="actionDone"/>
</LinearLayout>
```
* 이후 이전에 만들었던 `menu_main.xml`에 위에서 만든 `search_layout.xml`을 item으로 수정하자.
```xml
<item
        android:id="@+id/menu_search"
        android:title="search"
        android:orderInCategory="102"
        app:showAsAction="always|withText"
        android:actionLayout="@layout/search_layout" />
```
* 그 다음으로는 `MainActivity.java`를 Layout에 있는 EditText 객체에 사용자가 검색어를 입려갛고 "완료" 버튼을 누르면   
  원하는 기능을 수행할 수 있도록 구현해보자.
```java
 @Override
public boolean onCreateOptionsMenu(Menu menu) {
    // 아래 코드는 XML로 정의된 메뉴 정보를 inflation하여 메모리에 로딩한다.
    getMenuInflater().inflate(R.menu.menu_main, menu);
        
    // 아래 코드는 메뉴 아이템 중 검색을 위해 정의한 아이템을 View 객체로 참조한다.
    View v = menu.findItem(R.id.menu_search).getActionView();
    if(v != null) {
        EditText editText = v.findViewById(R.id.editText);
        if(editText != null) {
            editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                    Toast.makeText(getApplicationContext(), "inserted.", Toast.LENGTH_LONG).show();
                    return true;
                }
            });
        }
    }
    return true;
}
```
<hr/>

<h2>상단 탭 만들기</h2>

* 상단 탭을 만들기 위해서는 `Dependencies --> Library Dependency`에서 `com.android.support:design`을 추가한다.
* `activity_main.xml`을 다음과 같이 작성하자.
```xml
<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    >

   <androidx.coordinatorlayout.widget.CoordinatorLayout
       android:layout_width="match_parent"
       android:layout_height="match_parent">
       <com.google.android.material.appbar.AppBarLayout
           android:layout_width="match_parent"
           android:layout_height="wrap_content"
           android:theme="@style/ThemeOverlay.AppCompat.Dark.ActionBar">
           
           <androidx.appcompat.widget.Toolbar
               android:layout_width="match_parent"
               android:layout_height="wrap_content"
               android:background="@color/colorPrimaryDark"
               android:theme="@style/ThemeOverlay.AppCompat.Dark"
               android:elevation="1dp"
               android:id="@+id/toolbar"
               >
               <TextView
                   android:layout_width="wrap_content"
                   android:layout_height="wrap_content"
                   android:id="@+id/titleText"
                   android:textAppearance="@style/Base.TextAppearance.Widget.AppCompat.Toolbar.Title"/>
           </androidx.appcompat.widget.Toolbar>
           
           <com.google.android.material.tabs.TabLayout
               android:layout_width="match_parent"
               android:layout_height="wrap_content"
               android:id="@+id/tabs"
               app:tabMode="fixed"
               app:tabGravity="fill"
               app:tabTextColor="@color/colorPrimary"
               app:tabSelectedTextColor="@color/colorAccent"
               android:elevation="1dp"
               android:background="@android:color/background_light"/>
       </com.google.android.material.appbar.AppBarLayout>
       
       <FrameLayout
           android:layout_width="match_parent"
           android:layout_height="match_parent"
           app:layout_behavior="@string/appbar_scrolling_view_behavior"
           android:id="@+id/container">
       </FrameLayout>
   </androidx.coordinatorlayout.widget.CoordinatorLayout>

</RelativeLayout>
```
* `CoordinatorLayout`은 ActionBar 영역을 포함한 전체 화면의 위치를 잡아주는 역할을 하므로 가장 바깥에 위치한다.   
  `CoordinatorLayout`내에 `AppBarLayout`과 함께 다른 layout을 넣으면, 그 둘의 간격이나 위치가 자동으로 결종된다.   
  `AppBarLayout`은 ActionBar를 가리키는데, 이 안에는 `Toolbar`가 들어갈 수 있으며, 탭을 사용하는 경우에는 탭의 버튼들이 들어갈 수 있는   
  `TabLayout`을 추가할 수 있다. `AppBarLayout`아래에는 `FrameLayout`을 넣어 화면의 내용을 구성할 수 있다.

* XML Layout에서 정의한 `Toolbar`객체는 `setSupportActionBar()`로 ActionBar로 설정할 수 있다.
```java
Toolbar toolbar = findViewById(R.id.toolbar);
setSupportActionBar(toolbar);
```

* `TabLayout`에 탭을 추가할 때는 `addTab()` 메소드를 사용한다.
```java
TabLayout tabs = findViewById(R.id.tabs);
tabs.addTab(tabs.newTab().setText("TAB 1"));
tabs.addTab(tabs.newTab().setText("TAB 2"));
```
* `TabLayout`의 선택된 것에 따라 다르게 처리하고 싶다면 아래와 같이 한다.
```java
tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        int position = tab.getPosition();
        switch(tab) {
            //..
        }
    }
});
```
<hr/>

<h3>하단 탭 만들기</h3>

* 하단 탭은 `BottomNavigationView` 위젯으로 만들 수 있다. 상단 탭과 마찬가지로 design 라이브러리를 추가해야 한다.
* `/app/res/menu/` 폴더에 `menu_bottom.xml`이 있고, 그 파일에는 `<menu>` 태그 내에 3개의 `<item>`태그들이 있다 하자.
* 아래는 `activity_main.xml` 이다.
```xml
<androidx.constraintlayout.widget.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    >
    
    <FrameLayout
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:id="@+id/container"
        app:layout_behavior="@string/appbar_scrolling_view_behavior"/>

    <com.google.android.material.bottomnavigation.BottomNavigationView
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:id="@+id/bottom_navigation"
        android:layout_marginEnd="0dp"
        android:layout_marginStart="0dp"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintLeft_toLeftOf="parent"
        app:layout_constraintRight_toRightOf="parent"
        app:itemBackground="@color/colorPrimary"
        app:itemIconTint="@drawable/item_color"
        app:itemTextColor="@drawable/item_color"
        app:menu="@menu/menu_bottom"/>
</androidx.constraintlayout.widget.ConstraintLayout>
```
* 위에서 하단 탭을 보여주는 `BottomNavigationView`는 `ConstraintLayout`안에 넣었고, `layout_constraintButtom_toBottomOf`   
  등의 ConstraintLayout의 속성을 지정해서 화면의 아래 부분에 배치했다.

* 하단 탭이 선택됐을 때의 이벤트를 받아 처리하려면 `BottomNavigationView#setOnNavigationItemSelectedListener()`메소드를   
  사용하면 된다. 아래는 예시이다.
```java
BottomNavigationView bottomNavigation = findViewById(R.id.bottom_navigation);
bottomNavigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch(item.getIemId()) {

            //..
        }
        return true;
    }
});
```
<hr/>

<h2>ViewPager 만들기</h2>

* 