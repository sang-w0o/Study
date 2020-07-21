Thread와 Handler
======

<h2>Handler 이해하기</h2>

* 새로운 프로젝트 생성 시에 자동으로 생성되는 MainActivity는 app이 실행될 때 하나의 프로세스에서 처리된다.   
  같은 프로세스 내에서 일련의 기능이 순서대로 실행될 때 대부분은 문제가 없지만, 대기 시간이 길어지는 네트워크 요청 등의 기능을   
  수행할 때는 화면에 보이는 UI도 멈춤 상태로 있게 되는 문제가 생길 수 있다.

* 위와 같은 문제를 해결하려면 하나의 프로세스 내에서 여러 개의 작업이 동시에 수행되는 __멀티 스레드 방식__ 을 사용하게 된다.   
  `Thread`는 __동시 수행이 가능한 작업 단위__ 이며, 현재 수행 중인 작업 이외의 기능을 동시에 처리하고 싶을 때에는 새로운 스레드를   
  만들어 처리한다. 이러한 멀티 스레드 방식은 같은 프로세스 안에 들어있으면서 메모리 리소스를 공유하므로 효율적인 처리가 가능하다.   
  하지만 동시에 리소스에 접근할 때 DeadLock이 발생하여 시스템이 비정상 종료하게 될 수도 있다.

* 여러 개의 스레드가 동시에 공통 메모리 리소스에 접근할 때 DeadLock이 발생한다. DeadLock이란 __동시에 두 곳 이상에서 요청이 생겼을 때__   
  __어떤 것을 먼저 처리할지 판단할 수 없어 발생하는 시스템 상의 문제__ 이다. 이는 RuntimeException으로 디버깅하기 쉽지 않다.

* 지연시간이 길어질 수 있는 app이라면 오랜 시간 작업을 수행하는 코드를 별도로 분리한 다음, UI에 응답을 보내는 방식을 사용한다.   
  이를 위해 안드로이드가 제공하는 두 가지 시나리오는 아래와 같다.
  * 서비스 사용하기 : 백그라운드 작업은 서비스로 실행하고 사용자에게는 알림 서비스로 알려준다. 만약 MainActivity로 결과값을   
    전달하고 이를 이용해서 다른 작업을 수행하고자 한다면 Broadcasting으로 결과값을 전달할 수 있다.
  * 스레드 사용하기 : 스레드는 같은 프로세스 내에 있기에 작업 수행의 결과를 바로 처리할 수 있다. 그러나 UI객체는 직접   
    접근할 수 없으므로 `Handler`객체를 사용한다.

* 안드로이드에서 UI를 처리할 때 사용되는 기본 스레드를 `Main Thread`라 한다. Main Thread에서 이미 UI에 접근하고 있으므로   
  새로 생성한 다른 스레드에서는 `Handler`객체를 사용해서 메시지를 전달함으로써 Main Thread에서 처리하도록 만들 수 있다.
<hr/>

<h3>Thread 사용하기</h3>

* Android에서는 표준 Java의 Thread를 그대로 사용할 수 있다. Thread는 new 연산자로 객체를 생성한 후 `start()` 메소드를   
  호출하면 시작할 수 있다. `Thread` 클래스에 정의된 생성자는 크게 파라미터가 없는 경우와 `Runnable` 구현 객체를 파라미터로   
  갖는 두 가지로 구분할 수 있다. 일반적으로 `Thread` 클래스를 상속한 새로운 클래스를 정의한 후 객체를 만들어 시작하는 방법을 쓴다.

* `activity_main.xml`에 id가 button인 Button이 있고, 이 버튼을 누르면 스레드가 동작될 수 있도록 구현해보자.
```java
public class MainActivity extends AppCompatActivity {
    
    int value = 0;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BackgroundThread thread = new BackgroundThread();
                thread.start();
            }
        });
    }
    
    class BackgroundThread extends Thread {
        public void run() {
            for(int i = 0; i < 100; i++) {
                try {
                    Thread.sleep(1000);
                } catch(Exception e) {}
                value += 1;
                Log.d("Thread", "Value : " + value);
            }
        }
    }
}
```

* `activity_main.xml`에 위 코드의 value값을 출력하는 id가 textView인 TextView가 있다고 하자.   
  아래와 같이 `MainActivity.java`를 수정해보자.
```java
public class MainActivity extends AppCompatActivity {

    int value = 0;
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
                BackgroundThread thread = new BackgroundThread();
                thread.start();
            }
        });
    }

    class BackgroundThread extends Thread {
        public void run() {
            for(int i = 0; i < 100; i++) {
                try {
                    Thread.sleep(1000);
                } catch(Exception e) {}
                value += 1;
                Log.d("Thread", "Value : " + value);
                textView.setText("Value : " + value);
            }
        }
    }
}
```
* 위 코드는 컴파일에러는 안나지만, RuntimeException이 발생한다. 그 이유는 개발자가 직접 생성한 `BackgroundThread` 객체에서   
  UI 객체제 직접 접근했기 때문이다. 결국 MainThread에서 관리하는 UI 객체는 직접 만든 Thread 객체에서는 접근할 수 없음을 의미한다.   
  이러한 문제를 해결해주는 것이 `Handler` 이다.
<hr/>

<h3>Handler로 메시지 전송하기</h3>

* App 실행 시 프로세스가 만들어지면 그 안에 MainThread가 함께 만들어진다. 그리고 최상위에서 관리되는 app 구성요소인   
  Activity, Broadcast 수신자 등과 새로 만들어지는 윈도우를 관리하기 위한 `Message Queue`를 실행한다.   
  메시지 큐를 사용하면 순차적으로 코드를 수행할 수 있는데, 이렇게 메시지 큐로 메인 스레드에서 처리할 메시지를   
  전달하는 역할을 `Handler` class가 담당한다. 결국 Handler는 실행하려는 특정 기능이 있을 때 Handler가 포함되어 있는   
  스레드에서 순차적으로 실행시킬 때 사용하게 된다. Handler를 사용하면 특정 메시지가 미래의 어떤 시점에 실행되도록   
  스케쥴링할 수도 있다.

* 새로 만든 Thread가 수행하려는 정보를 MainThread로 전달하기 위해서는 먼저 Handler가 관리하는 Message Queue에서 처리할 수 있는   
  메시지 객체 하나를 참조해야 한다. 이 과정에서는 `obTainMessage()` 메소드를 사용하여 메시지 객체를 반환받을 수 있다.   
  그 후 메시지 객체에 필요한 정보를 넣은 후 `sendMessage()`메소드를 이용해 Message Queue에 메시지를 넣을 수 있다.   
  Message Queue에 들어간 메시지는 순서대로 Handler가 처리하게 되며, 이때 `handleMessage()`에 정의된 기능이 수행된다.   
  이때 `handleMessage()`에 들어 있는 코드가 수행되는 위치는 새로만든 Thread가 아닌 Main Thread가 된다.

* 아래 코드는 위에서 RunTimeException이 발생한 `MainActivity.java`를 수정한 코드이다.
```java
public class MainActivity extends AppCompatActivity {

    MainHandler handler;
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
                BackgroundThread thread = new BackgroundThread();
                thread.start();
            }
        });
        
        handler = new MainHandler();
    }

    class BackgroundThread extends Thread {
        int value = 0;
        public void run() {
            for(int i = 0; i < 100; i++) {
                try {
                    Thread.sleep(1000);
                } catch(Exception e) {}
                value += 1;
                Log.d("Thread", "Value : " + value);

                Message message = handler.obtainMessage();
                Bundle bundle = new Bundle();
                bundle.putInt("value", value);
                message.setData(bundle);
                
                handler.sendMessage(message);
            }
        }
    }
    
    class MainHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            
            Bundle bundle = msg.getData();
            int value = bundle.getInt("value");
            textView.setText("Value : " + value);
        }
    }
}
```
* 위 코드에서는 `Handler`클래스를 상속한 `MainHandler`클래스를 저의했다. `Handler#handleMessage()` 메소드는 재정의하면 메시지가   
  MainThread에서 수행될 때 필요한 기능을 넣어들 수 있다. 이렇게 정의한 handler는 `onCreate()`에서 new 연산자로 만들어진다.
* 새로 만든 Thread객체에서 수행한 작업의 결과가 나왔을 때는 `Handler#obtainMessage()`로 `Message`객체를 하나 참조한 후,   
  필요한 정보를 Bundle 객체에 넣고, `Handler#sendMessage()`로 Message Queue에 넣게 된다.
<hr/>

<h3>Runnable 객체 실행하기</h3>

* `Handler` 클래스는 메시지 전송 방법 외에도 `Runnable` 구현 객체를 실행시킬 수 있는 방법을 제공한다. 즉, 새로 만든 `Runnable`   
  구현 객체를 `Handler#post()` 메소드로 전달해주면 이 객체에 정의된 `run()` 메소드 내의 코드들은 MainThread내에서 실행된다.

* 아래 코드는 위에서 만든 코드를 `Runnable` 구현 객체를 사용하는 방식으로 변경한 것이다.
```java
public class MainActivity extends AppCompatActivity {
    
    TextView textView;
    
    Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = findViewById(R.id.textView);
        Button button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BackgroundThread thread = new BackgroundThread();
                thread.start();
            }
        });
    }

    class BackgroundThread extends Thread {
        int value = 0;
        public void run() {
            for(int i = 0; i < 100; i++) {
                try {
                    Thread.sleep(1000);
                } catch(Exception e) {}
                value += 1;
                Log.d("Thread", "Value : " + value);

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        textView.setText("Value : " + value);
                    }
                });
            }
        }
    }
}
```
<hr/>

<h2>일정 시간 후에 실행하기</h2>

* 웹서버와 같은 원격 서버에 접속한 후 웹페이지를 요청할 때 응답이 늦어지거나 없으면 app이 대기하고 있는 상황이 지속된다.   
  이런 경우에는 기본적으로 별도의 Thread를 만들어 처리하게 된다. 하지만 버튼을 클릭해서 간단하게 접속을 처리하는 경우에는   
  MainThread내에서 지연 시간을 주는 것 만으로도 UI의 멈춤 현상을 방지할 수 있다. 단순히 `Thread.sleep()`로 잠시 대기 상태로   
  있다가 다시 실행할 수도 있지만, Handler로 지연 시간을 주었을 때 Handler로 실행되는 코드는 Message Queue를 통과하면서   
  순차적으로 실행되기 때문에 UI 객체들에 영향을 주지 않으면서 지연 시간을 두고 실행된다.

* `activity_main.xml`에 id가 textView인 TextView와, id가 button인 Button이 있다고 하자.   
  일정 시간 후에 알림대화상자를 띄우도록 `MainActivity.java`를 구현해보자.
```java
public class MainActivity extends AppCompatActivity {

    TextView textView;

    Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = findViewById(R.id.textView);
        
        Button button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               request();
            }
        });
    }

    private void request() {
        String title = "Remote Request";
        String message = "Request data ?";
        String titleButtonYes = "YES";
        String titleButtonNo = "NO";
        AlertDialog dialog = makeRequestDialog(title, message, titleButtonYes, titleButtonNo);
        dialog.show();
        textView.setText("Showing dialog...");
    }
    
    private AlertDialog makeRequestDialog(CharSequence title, CharSequence message, CharSequence titleButtonYes, CharSequence titleButtonNo) {
        
        AlertDialog.Builder requestDialog = new AlertDialog.Builder(this);
        requestDialog.setTitle(title);
        requestDialog.setMessage(message);
        requestDialog.setPositiveButton(titleButtonYes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                textView.setText("Result shown after 5 seconds.");
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        textView.setText("Request completed.");
                    }
                }, 5000);
            }
        });
        
        requestDialog.setNegativeButton(titleButtonNo, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                
            }
        });
        return requestDialog.create();
    }
}
```
* 화면에 있는 Button을 누르면 `request()` 메소드가 수행되는데, 이 메소드는 `AlertDialog`를 이용하여 대화상자를 출력한다.   
  대화상자의 YES를 누르면 `Handler`객체의 `postDelayed()` 메소드를 사용하여 5초의 시간이 지난 후 코드가 실행되게 했다.   
* Handler는 Message Queue를 사용하므로 메시지들을 순서대로 처리하지만, 메시지를 넣을 때 시간을 지정하면 원하는 시간에   
  메시지를 처리하게 할 수 있다. 따라서 `postDelayed()` 메소드는 일정 시간 후에 특정 기능을 실행시킬 때 유용하게 사용된다.   
  시간을 지정할 때는 `Handler`객체의 아래 두 메소드를 사용할 수 있다.
```java
public boolean sendMessageAtTime(Message message, long uptimeMillis);
public boolean sendMessageDelayed(Message message, long delayMillis);
```

* `sendMessageAtTime()` 메소드는 메시지를 보낼 때 시간을 지정할 수 있으며, `sendMessageDelayed()` 메소드는 메시지가 일정 시간이   
  지난 후 실행되도록 할 수 있다. `Runnable`객체를 실행하는 `post()` 메소드도 위 2개와 같이 `postAtTime()`과 `postDelayed()`   
  메소드가 있어 같은 기능을 수행한다.
<hr/>

<h2>Thread로 메시지 전송하기</h2>

* 위에서 Thread의 작업 결과물을 Message로 만들어 전달하는 이유는 별도의 Thread에서 MainThread가 관리하는 UI객체에   
  직접 접근할 수 없기 때문이다. 하지만 이외 반대로 MainThread에서 별도의 Thread로 메시지를 전달해야할 때가 있다.   
  이를 위해 MainThread에서 변수를 선언하고 별도의 Thread가 그 값을 읽어오는 방법을 사용할 수 있다. 하지만 별도의 Thread가   
  관리하는 동일한 객체를 여러 Thread가 접근할 때는 별도의 Thread안에 들어있는 Message Queue를 이용해 순서대로 접근하도록해야 한다.

* Handler가 처리하는 Message Queue는 `Looper`로 처리되는데, Looper는 Message Queue에 들어오는 메시지를 지속적으로 보면서   
  하나씩 처리하게 된다. MainThread는 UI 객체들을 처리하기 위해 Message Queue와 Looper를 사용한다. 그러나 별도의 Thread를   
  새로 만들었을 때는 Looper가 없다. 따라서 Main Thread나 다른 Thread에서 메시지 전송 방식으로 thread에 데이터를 전달할 후   
  순차적으로 작업을 수행하고 싶다면 Looper를 만든 후 실행해야 한다.

* `activity_main.xml`에 EditText 가 있고, 이 값을 Button을 누르면 MainThread에서 EditText의 값을 읽어와 저장하고,   
  별도의 Thread에서 이 값을 읽어와 TextView에 출력하도록 구현해보자.
```java
public class MainActivity extends AppCompatActivity {

    EditText editText;
    TextView textView;

    Handler handler = new Handler();
    
    ProcessThread thread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editText = findViewById(R.id.editText);
        textView = findViewById(R.id.textView);

        Button button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               String input = editText.getText().toString();
               Message message = Message.obtain();
               message.obj = input;
               
               thread.processHandler.sendMessage(message);
            }
        });

        thread = new ProcessThread();
    }

    class ProcessThread extends Thread {
        ProcessHandler processHandler = new ProcessHandler();
        public void run() {
            Looper.prepare();
            Looper.loop();
        }
    }

    class ProcessHandler extends Handler {
        
        public void handleMessage(Message message) {
            final String output = message.obj + " from thread.";
            handler.post(new Runnable() {
                @Override
                public void run() {
                    textView.setText(output);
                }
            });
        }
    }
}
```
* `onCreate()` 메소드 내의 `onClick()` 메소드 내에서는 `Message`객체에 문자열을 포함시켜 전송하기 위해 `Message.obj`에   
  문자열을 할당한다. `Message`객체는 `Message.obtain()` 메소드로 참조할 수 있으며, 새로 만든 thread의 handler변수를   
  이용해서 `sendMessage()` 메소드를 호출하면 `Message`객체가 thread로 전송된다. `ProcessThread`를 정의할 때 `ProcessHandler`   
  객체를 만들고, `Looper.prepare()`와 `Looper.loop()` 메소드를 호출했으므로 이 Thread에서는 `Message`객체를 전달받을 수 있다.   
  `ProcessHandler` 내에 정의된 `handleMessage()` 메소드는 전달받는 `Message`객체의 obj변수를 읽어와 textView에 할당한다.
<hr/>

<h2>AsyncTask 사용하기</h2>

* `AsyncTask` 클래스를 상속하여 새로운 클래스를 만들면 그 안에 thread를 위한 코드와 UI 접근 코드를 한꺼번에 넣을 수 있다. 따라서 thread로   
  처리해야하는 코드를 하나의 `AsyncTask`클래스로 정의할 수 있다는 장점이 있다. 예를 들어, 웹서버에서 고객 이름을 가져오는 작업과 웹서버에서   
  제품 정보를 가져오는 작업을 서로 다른 코드로 분리시키고 싶다면 두 개의 `AsyncTask` 상속 클래스로 만든 후 각각의 코드를 작성하면 된다.

* `AsyncTask` 상속 클래스를 만들고, `execute()` 메소드를 실행하면 그 객체는 정의된 백그라운드 작업을 수행하고, 필요한 경우에 그 결과를   
  MainThread에서 실행하므로 UI 객체에 접근하는데 문제가 없다.

* `AsyncTask`를 상속하여 새로운 클래스를 정의하면 그 내부에서 필요한 경우마다 callback 메소드가 자동으로 호출된다. `doInBackground()` 메소드에는   
  새로 만들어진 thread에서 실행되어야 할 코드들을 넣을 수 있다. 즉, thread에서 동작하는 것이다. 하지만 `onPreExecute()`, `onProgressUpdate()`와   
  `onPostExecute()` 메소드는 새로 만든 thread가 아닌 MainThread에서 실행된다. 따라서 UI 객체에 자유롭게 접근할 수 있다. 결국 하나의 클래스 안에   
  thread에서 동작해야하는 작업과 그 작업의 결과를 UI 객체에 반영하는 코드를 같이 작성할 수 있는 것이다.

* 아래는 `AsyncTask`에 정의된 주요 메소드들에 대한 설명이다.

<table>
    <tr>
        <td>doInBackground()</td>
        <td>새로 만든 thread에서 백그라운드 작업을 수행한다. execute() 메소드를 호출할 때 사용된 파라미터를 배열로 전달받는다.</td>
    </tr>
    <tr>
        <td>onPreExecute()</td>
        <td>백그라운드 작업을 수행하기 직전에 호출된다. MainThread에서 수행되며 초기화 작업에 사용된다.</td>
    </tr>
    <tr>
        <td>onProgressUpdate()</td>
        <td>백그라운드 작업의 진행 상태를 표시하기 위해 호출된다. 작업 수행 중간중간에 UI 객체에 접근하는 경우에 사용된다. 이 메소드가 호출되도록
            하려면 백그라운드 작업 중간에 publishProgress() 메소드를 호출해야 한다.</td>
    </tr>
    <tr>
        <td>onPostExecute()</td>
        <td>백그라운드 작업이 끝난 직후에 호출된다. MainThread에서 실행되며 메모리 리소스를 해제하는 등의 작업에 사용된다. 백그라운드 작업의 결과는
            Result 타입의 파라미터로 전달된다.</td>
    </tr>
</table>

* `AsyncTask#cancel()` 메소드를 호출하면 작업을 취소할 수 있는데, 이 메소드로 작업을 취소했을 때는 `onCancelled()` 메소드가 호출된다.   
  작업의 진행 상황을 확인하고 싶을 때는 `AsyncTask#getStatus()` 메소드를 사용할 수 있다. 이 메소드의 반환결과인 `AsyncTask.Status`객체는 상태를 표현하며,   
  각각의 상태는 작업이 시작되지 않았다는 `PENDING`과 실행 중이라는 `RUNNING`, 그리고 종료되었음을 의미하는 `FINISHED` 상수값이 있다.

* 아래와 같이 `activity_main.xml`을 작성하자.
```xml
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    tools:context=".MainActivity">

    <ProgressBar
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:max="100"
        android:id="@+id/progressBar"/>

    <Button
        android:layout_width="100dp"
        android:layout_height="wrap_content"
        android:text="RUN"
        android:id="@+id/button" />

    <Button
        android:layout_width="100dp"
        android:layout_height="wrap_content"
        android:text="CANCEL"
        android:id="@+id/button2" />

</LinearLayout>
```

* 이 화면의 버튼을 클릭하면 별도의 thread에서 값을 1씩 증가시키도록 하고, 100ms마다 한번씩 증가시켜 ProgressBar의 최대값으로 지정된 100이 될 때까지   
  10초가 걸린다. 아래는 `AsyncTask`를 사용해 백그라운드 작업을 수행하는 `MainActivity.java`의 코드이다.
```java
public class MainActivity extends AppCompatActivity {

    BackgroundTask task;
    int value;

    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progressBar = findViewById(R.id.progressBar);
        
        Button button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                task = new BackgroundTask();
                task.execute();
            }
        });
        
        Button button2 = findViewById(R.id.button2);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // CANCEL 버튼 클릭 시 task를 cancel한다.
                // cancel되면 BackgroundTask.onCancelled()가 호출된다.
                task.cancel(true);
            }
        });
    }
    
    class BackgroundTask extends AsyncTask<Integer, Integer, Integer> {

        @Override
        protected void onPreExecute() {
            value = 0;
            progressBar.setProgress(value);
        }

        @Override
        protected Integer doInBackground(Integer... values) {
            while(isCancelled() == false) {
                value++;
                if(value >= 100) {
                    break;
                } else {
                    // 아래 메소드를 호출하여 onProgressUpdate() 메소드가 호출되도록 한다.
                    publishProgress(value);
                }
                
                try {
                    Thread.sleep(100);
                } catch(Exception e) {}
            }
            return value;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            progressBar.setProgress(values[0].intValue());
        }

        @Override
        protected void onPostExecute(Integer integer) {
            progressBar.setProgress(0);
        }

        @Override
        protected void onCancelled() {
            progressBar.setProgress(0);
        }
    }
}
```
* 백그라운드 작업을 수행할 클래스는 `BackGroundTask` 클래스로 정의하고, `AsyncTask`를 상속받도록 했다. 먼저 `onPreExecute()` 메소드는 초기화   
  단계에서 사용되므로 값을 저장하기 위해 MainActivity에 정의한 value 변수의 값을 0으로 초기화하고 progressBar값도 0으로 설정해준다.   
  `doInBackground()` 메소드는 주된 작업을 하는데 수행되므로 while문으로 value값을 100ms에 하나씩 증가하도록 한다. 그리고 중간중간 상태를   
  UI에 업데이트하도록 하기 위해 `publishProgress()` 메소드를 호출한다. 이 메소드의 호출로 인해 실행되는 `onProgressUpdated()` 메소드는 progressBar의   
  상태를 업데이트해준다. 
<hr/>

<h2>Thread로 Animation 만들기</h2>

* 여러 이미지를 연속해서 바꿔가며 애니메이션 효과를 만들고 싶을 때 등의 경우에 thread를 사용하는 경우가 많다.   
* thread로 간단한 애니메이션을 구현해보자.
```java
public class MainActivity extends AppCompatActivity {

    ImageView imageView;
    ArrayList<Drawable> drawableList = new ArrayList<Drawable>();
    Handler handler = new Handler();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Resources res = getResources();
        drawableList.add(res.getDrawable(R.drawable.face1));
        drawableList.add(res.getDrawable(R.drawable.face2));
        drawableList.add(res.getDrawable(R.drawable.face3));
        drawableList.add(res.getDrawable(R.drawable.face4));
        drawableList.add(res.getDrawable(R.drawable.face5));
        
        imageView = findViewById(R.id.imageView);
        Button button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AnimThread thread = new AnimThread();
                thread.start();
            }
        });
    }
    
    class AnimThread extends Thread {
        public void run() {
            int index = 0;
            for(int i = 0; i < 100; i++) {
                final Drawable drawable = drawableList.get(index);
                index += 1;
                if(index > 4) index = 0;
                
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        imageView.setImageDrawable(drawable);
                    }
                });
                try {
                    Thread.sleep(100);
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
```