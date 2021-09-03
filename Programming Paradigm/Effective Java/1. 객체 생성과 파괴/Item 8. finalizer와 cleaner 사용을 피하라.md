# finalizer와 cleaner 사용을 피하라

- Java는 두 가지 객체 소멸자를 제공한다.  
  그 중 **finalizer는 예측할 수 없고, 상황에 따라 위험할 수 있어 일반적으로 불필요하다.**  
  오동작, 낮은 성능, 이식성 문제의 원인이 되기도 한다.  
  finalizer는 나름의 쓰임새가 몇 가지 있긴 하지만 기본적으로 _쓰지 말아야_ 한다.  
  그래서 Java9에서는 finalizer를 deprecated API로 지정하고 cleaner를 그 대안으로 소개했다.  
  하지만 Java 라이브러리에서도 여전히 finalizer를 사용하긴 한다.  
  **cleaner는 finalizer보다는 덜 위험하지만, 여전히 예측할 수 없고, 느리고, 일반적으로는 불필요하다.**

- Java의 finalizer와 cleaner는 C++의 파괴자(Destructor)와는 다른 개념이다.  
  C++에서 파괴자는 생성자의 꼭 필요한 대척점으로 특정 객체와 관련된 자원을 회수하는 보편적인 방법이다.  
  Java에서는 접근할 수 없게된 객체를 회수하는 역할을 GC가 담당하고, 프로그래머에게는 아무런  
  작업도 요구하지 않는다. C++의 파괴자는 비메모리 자원을 회수하는 용도로도 쓰인다.  
  하지만 Java에서는 try-with-resources와 try-finally를 이용해 해결한다.

- finalizer와 cleaner는 죽시 수행된다는 보장이 없다. 객체에 접근할 수 없게 된 후 finalizer나  
  cleaner가 실행되기까지 얼마나 걸릴지 알 수 없다. **즉 finalizer와 cleaner로는 제때 실행되어야 하는**  
  **작업은 절대 할 수 없다.** 예를 들어 파일 닫기를 finalizer나 cleaner에 맡기면 중대한 오류를  
  일으킬 수 있다. 시스템이 동시에 열 수 있는 파일 개수에 한계가 있기 때문이다.  
  시스템이 finalizer나 cleaner의 실행을 게을리해서 파일을 계속 열어둔다면 새로운 파일을 열지 못해  
  프로그램이 실패할 수 있다.

- finalizer나 cleaner를 얼마나 신속하게 수행할지는 전적으로 GC의 알고리즘에 달렸으며,  
  이는 GC 구현마다 천차만별이다. finalizer나 cleaner의 수행 시점에 의존하는 프로그램의  
  동작 또한 마찬가지다. 로컬의 JVM에서는 완벽하게 동작하던 프로그램이 가장 중요한 고객의 시스템에서는  
  엄청난 재앙을 일으킬지도 모른다.

- 궁뜬 finalizer 처리는 현업에서도 실제로 문제를 일으킨다. 클래스이 finalizer를 달아두면 그 인스턴스의  
  자원 회수가 제멋대로 지연될 수 있다. 자원 회수가 계속해서 지연되어 회수될 자원들이 쌓이게 되면 애플리케이션이  
  `OutOfMemoryError`를 던지며 죽는다. Java 언어 명세는 어떤 스레드가 finalizer를 수행할지 명시하지  
  않으니 이 문제를 예방할 보편적인 방법은 없다. 딱 하나, **finalizer를 사용하지 않는 방법** 뿐이다.  
  한편 cleaner는 자신을 수행할 스레드를 제어할 수 있다는 면에서 조금 낫다. 하지만 여전히 백그라운드에서  
  수행되며 GC의 통제하에 있으니 즉각 수행되리라는 보장은 없다.

- Java 언어 명세는 finalizer나 cleaner의 수행 시점 뿐 아니라 수행 여부조차 보장하지 않는다.  
  접근할 수 없는 객체에 딸린 종료 작업을 전혀 수행하지 못한 채 프로그램이 중단될 수도 있다는 뜻이다.  
  따라서 프로그램의 생명주기와 관계없는, **상태를 영구적으로 수정하는 작업에서는 절대 finalizer나**  
  **cleaner에 의존해서는 안된다.** 예를 들어 DB같은 공유 자원의 경우 영구 lock 해제를  
  finalizer나 cleaner에 맡겨 놓으면 분산 시스템 전체가 서서히 멈출 것이다.

- `System.gc()`나 `System.runFinalization()` 메소드에 현혹되지 말자. finalizer와 cleaner가  
  실행될 가능성을 높여줄 수는 있으나, 보장해주지 않는다. 사실 이를 보장해주는 메소드가  
  `System.runFinalizersOnExit()`, `Runtime.runFinalizersOnExit()`으로 2개가 있었는데,  
  이 두 메소드는 심각한 결함(Thread stop) 때문에 수십 년간 지탄 받아 왔다.

- finalizer의 부작용은 여기서 끝이 아니다. finalizer 동작 중 발생한 예외는 무시되며, 처리할 작업이  
  남았더라도 그 순간 종료된다. 잡지 못한 예외 때문에 해당 객체는 자칫 마무리가 덜 된 상태로 남을 수 있다.  
  그리고 다른 스레드가 이처럼 훼손된 객체를 사용하려 한다면 어떻게 동작할지 예측할 수 없다.  
  보통의 경우엔 잡지 못한 예외가 스레드를 중단시키고 스택 추적 내역을 출력하겠지만, 같은 일이 finalizer에서  
  발생한다면 경고조차 출력되지 않는다. 그나마 cleaner를 사용하는 라이브러리는 자신의 스레드를 통제하기에  
  이러한 문제가 발생하지 않는다.

- **finalizer와 cleaner는 심각한 성능 문제도 동반한다.** finalizer는 GC의 효율을 떨어뜨린다.  
  cleaner도 클래스의 모든 인스턴스를 수거하는 형태로 사용하면 성능은 finalizer와 비슷하다.

- **finalizer를 사용한 클래스는 finalizer 공격에 노출되어 심각한 보안 문제를 일으킬 수도 있다.**  
  finalizer 공격 원리는 간단하다. 생성자나 직렬화 과정(`readObject()`, `readResolve()`)에서  
  예외가 발생하면, 이 생성되다 만 객체에서 악의적인 하위 클래스의 finalizer가 수행될 수 있게 한다.  
  있어서는 안될 일이다. 이 finalizer는 정적 필드에 자신의 참조를 할당하여 GC가 수집하지 못하게  
  막을 수 있다. 이렇게 일그러진 객체가 만들어지고 나면, 이 객체의 메소드를 호출해 애초에는 허용되지  
  않았을 작업을 수행하는 건 일도 아니다. **객체 생성을 막으려면 생성자에서 예외를 던지는 것만으로**  
  **충분하지만, finalizer가 있다면 그렇지도 않다**. 이러한 공격은 끔찍한 결과를 초래할 수 있다.  
  final 클래스들은 그 누구도 하위 클래스를 만들 수 없으니 이 공격에서 안전하다.  
  **final이 아닌 클래스를 finalizer 공격으로부터 방어하려면 아무 일도 하지 않는 finalize 메소드를**  
  **만들고 final로 선언하자.**

- 그렇다면 파일이나 스레드 등 종료해야 할 자원을 담고 있는 객체의 클래스에서 finalizer나 cleaner를  
  대신해줄 묘안은 무엇일까? 그저 **AutoClosable을 구현**해주고, 클라이언트에서 인스턴스를 다 쓰고 나면  
  예외가 발생해도 제대로 종료되도록 try-with-resources로 `close()`를 호출해주면 된다.  
  구체적인 구현법과 관련하여 알아두면 좋을 것이 있는데, 각 인스턴스는 자신이 닫혔는지를 추적하는 것이 좋다.  
  다시 말해 `close()` 메소드에서 이 객체는 더 이상 유효하지 않음을 필드에 기록하고, 다른 메소드는  
  이 필드를 검사해서 객체가 닫힌 후에 불렸다면 `IllegalStateException`을 던지도록 하는 것이다.

- 이제 cleaner와 finalizer는 대체 언제 쓰일지 알아보자.  
  하나는 _자원의 소유자가 `close()` 메소드를 호출하지 않는 것에 대비한 안전망 역할_ 이다.  
  cleaner나 finalizer가 즉시(혹은 끝까지) 호출되리라는 보장은 없지만, 클라이언트가 하지 않은  
  자원 회수를 늦게대로 해주는 것이 아예 안 하는 것보다는 낫기 때문이다.  
  이런 안전망 역할의 finalizer를 작성할 때는 그럴만한 값어치가 있는지 심사숙고하자.  
  Java 라이브러리의 일부 클래스는 안전망 역할의 finalizer를 제공한다. `FileInputStream`,  
  `FileOutputStream`, `ThreadPoolExecutor`가 대표적이다.

- 두 번째 예시는 네이티브 피어(Native Peer)와 연결된 객체에서다. 네이티브 피어란 일반 Java 객체가  
  네이티브 메소드를 통해 기능을 위임한 네이티브 객체를 말한다. 네이티브 피어는 Java 객체가 아니니  
  GC는 그 존재를 알지 못한다. 그 결과 Java Peer를 회수할 때 네이티브 피어까지 회수하지 못한다.  
  cleaner나 finalizer가 나서서 처리하기 적당한 작업이다. 단 성능 저하를 감당할 수 있고  
  네이티브 피어가 심각한 자원을 가지고 있지 않을 때에만 해당된다. 성능 저하를 감당할 수 없거나  
  네이티브 피어가 사용하는 자원을 즉시 회수해야 한다면 앞서 본 `close()` 메소드를 사용해야 한다.

- cleaner는 사용하기에 조금 까다롭다. 아래의 `Room` 클래스로 이 기능을 보자.  
  Room 자원을 수거하기 전에 반드시 `clean` 해야 한다고 가정해보자.  
  `Room` 클래스는 `AutoClosable`을 구현한다. 사실 자동 청소 안전망이 cleaner를 사용할지 말지는  
  순전히 내부 구현 방식에 관한 문제다. 즉 finalizer와 달리 cleaner는 클래스의 Public API에  
  나타나지 않는다는 이야기다.

```java
public class Room implements AutoCloseable {
    private static final Cleaner cleaner = Cleaner.create();

    // 청소가 필요한 자원, 절대 Room을 참조해서는 안된다!
    private static class State implements Runnable {
	int junkPileNumber;

	State(int junkPileNumber) {
	    this.junkPileNumber = junkPileNumber;
	}

	// close 메소드나 cleaner가 호출한다.
	@Override
	public void run() {
	    System.out.println("Cleaning room");
	    junkPileNumber = 0;
	}
    }

    // 방의 상태. cleanable과 공유한다.
    private final State state;

    // cleanable 객체. 수거 대상이 되면 방을 청소한다.
    private final Cleaner.Cleanable cleanable;

    public Room(int junkPileNumber) {
	state = new State(junkPileNumber);
	cleanable = cleaner.register(this, state);
    }

    @Override
    public void close() {
	cleanable.clean();
    }
}
```

- static으로 선언된 중첩 클래스인 `State`는 cleaner가 `Room`을 청소할 때 수거할 자원들을  
  담는다. 이 예시에서는 단순히 방 안에서 쓰레기 수를 뜻하는 junkPileNumber 필드가 수거할  
  자원에 해당한다. 더 현실적으로 만들려면 이 필드는 Native Peer를 가리키는 포인터를 담은 final long  
  변수여야 한다. `State`는 `Runnable`을 구현하고, 그 안의 `run()` 메소드는 cleanable에 의해  
  딱 한 번만 호출될 것이다. 이 cleanable 객체는 `Room` 생성자에서 cleaner에 `Room`과 `State`를  
  등록할 때 얻는다. `run()` 메소드가 호출되는 상황은 둘 중 하나다. 보통은 `Room#close()`를  
  호출할 때다. `close()`에서 `Cleanable#clean()` 을 호출하면 이 메소드 내에서 `run()`을 호출한다.  
  혹은 GC가 `Room`을 회수할 때까지 클라이언트가 `close()`를 호출하지 않는다면 cleaner가 아마  
  `State`의 `run()` 메소드를 호출해줄 것이다.

- `State` 인스턴스는 **절대로** `Room` 인스턴스를 참조해서는 안된다.  
  `Room` 인스턴스를 참조할 경우 순환 참조가 생겨 GC가 `Room` 인스턴스를 회수해갈 기회가 오지 않는다.  
  `State`가 중첩 클래스인 이유가 여기에 있다. 정적이 아닌 중첩 클래스는 자동으로 바깥 객체의 참조를  
  갖게 되기 때문이다. 이와 비슷하게 람다 역시 바깥 객체의 참조를 갖기 쉬우니 사용하지 않는 것이 좋다.

- 앞서 말한대로 `Room`의 cleaner는 단지 안전망으로만 쓰였다.  
  만약 클라이언트가 모든 `Room` 인스턴스의 생성을 try-with-resources 블록으로 감쌌다면  
  자동 청소는 전혀 필요 없다. 아래는 잘 짜인 클라이언트의 코드 예시이다.

```java
public class Adult {
    public static void main(String[] args) {
	try(Room myRoom = new Room(7)) {
	    System.out.println("HI~");
	}
    }
}
```

- 기대한 대로 `Adult` 프로그램은 "HI~"를 출력하고, 이어서 "Cleaning Room"을 출력한다.  
  이번엔 결코 방 청소를 하지 않는 프로그램을 살펴보자.

```java
public class Teenager {
    public static void main(String[] args) {
	new Room(99);
	System.out.println("HA");
    }
}
```

- 위 코드에서는 "Cleaning Room"이 출력되지 않는 경우가 있다. 이것이 바로 앞서 말한 cleaner와  
  finalizer의 작동은 _예측할 수 없다_ 고 한 상황이다. 아래는 cleaner의 명세이다.

> `System.exit`을 호출할 때의 cleaner 동작은 구현하기 나름이다.  
> 청소가 잘 이뤄질지는 보장하지 않는다.

- 명세에서는 명시하지 않았지만 일반적인 프로그램 종료에서도 마찬가지이다.

<hr/>

<h2>핵심 정리</h2>

- Cleaner(Java8까지는 Finalizer)는 안전망 역할이나 중요하지 않은 네이티브 자원 회수용으로만  
  사용하자. 물론 이런 경우라도 불확실성과 성능 저하에 주의해야 한다.

<hr/>
