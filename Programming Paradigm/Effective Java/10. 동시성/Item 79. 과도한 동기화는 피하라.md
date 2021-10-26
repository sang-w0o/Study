# 과도한 동기화는 피하라

- Item 78에서 충분하지 못한 동기화의 피해를 다뤘다면, 이번 아이템에서는 반대 상황을 다룬다.  
  과도한 동기화는 성능을 떨어뜨리고, 교착 상태에 빠뜨리고, 심지어 예측할 수 없는 동작을  
  낳기도 한다.

- **응답 불가와 안전 실패를 피하려면 동기화 메소드나 동기화 블록 안에서는 제어를 절대로**  
  **클라이언트에게 양도하면 안된다.** 예를 들어 동기화된 영역 안에서는 재정의할 수 있는  
  메소드는 호출해서는 안되며, 클라이언트가 넘겨준 함수 객체를 호출해서도 안된다. 동기화된  
  영역을 포함한 클래스 관점에서는 이런 메소드들은 모두 바깥 세상에서 온 외계인이다. 그 메소드가  
  무슨 일을 할지 알지 못하며 통제할 수도 없다는 뜻이다. 외계인 메소드(alien method)가 하는 일에  
  따라 동기화된 영역은 예외를 일으키거나, 교착상태에 빠지거나, 데이터를 훼손할 수도 있다.

- 구체적인 예시를 보자. 아래는 어떤 `Set`을 감싼 wrapper 클래스이고, 이 클래스의 클라이언트는  
  집합에 원소가 추가되면 알림을 받을 수 있다. 바로 관찰자 패턴이다. 핵심만 보여주기 위해 원소가  
  제거되는 부분은 생략했다. 그리고 Item 18에서 사용한 `ForwardingSet`을 재사용해 구현했다.

```java
public class ObservableSet<E> extends ForwardingSet<E> {
    public ObservableSet(Set<E> set) { super(set); }

    private final List<SetObserver<E>> observers = new ArrayList<>();

    public void addObserver(SetObserver<E> observer) {
	      synchronized(observers) {
	          observers.add(observer);
	      }
    }

    public boolean removeObserver(SetObserver<E> observer) {
	      synchronized(observers) {
	          return observers.remove(observer);
	      }
    }

    private void notifyElementAdded(E element) {
	      synchronized(observers) {
	          for(SetObserver<E> observer : observers)
	              observer.added(this, element);
	      }
    }

    @Override public boolean add(E element) {
	      boolean added = super.add(element);
	      if(added) notifyElementAdded(element);
	      return added;
    }

    @Override public boolean addAll(Collection<? extends E> c) {
	      boolean result = false;
	      for(E element : c) {
	          result |= add(element);
	      }
	      return result;
    }
}
```

- 관찰자들은 `addObserver()`와 `removeObserver()` 메소드를 호출해 구독을 신청하거나 해지한다.  
  두 경우 모두 아래 콜백 인터페이스의 인스턴스를 메소드에 건넨다.

```java
@FunctionalInterface
public interface SetObserver<E> {
    // ObservableSet에 원소가 더해지면 호출된다.
    void added(ObservableSet<E> set, E element);
}
```

- 위 인터페이스는 구조적으로 `BiConsumer<ObservableSet<E>, E>`와 똑같다. 그럼에도 커스텀  
  함수형 인터페이스를 정의한 이유는 이름이 더 직관적이고 다중 콜백을 지원하도록 확장할 수 있어서다.  
  물론 `BiConsumer`를 그대로 사용했더라도 별 무리는 없었을 것이다.

- 눈으로 보기에 `ObservableSet`은 잘 작동할 것 같다. 예를 들어, 아래 프로그램은  
  0부터 99까지를 출력한다.

```java
public static void main(String[] args) {
    ObservableSet<Integer> set = new ObservableSet<>(new HashSet<>());
    set.addObserver((s, e) -> System.out.println(e));
    for(int i = 0; i < 100; i++)
	      set.add(i);
}
```

- 이제 조금 흥미진진한 시도를 해보자. 평상시에는 앞서와 같이 집합에 추가된 정수값을 출력하다가,  
  그 값이 23이 된다면 자기 자신을 제거(구독 해지)하는 관찰자를 추가해보자.

```java
public static void main(String[] args) {
    ObservableSet<Integer> set = new ObservableSet<>(new HashSet<>());
    set.addObserver(new SetObserver<>() {
	      public void added(ObservableSet<Integer> set, Integer element) {
	          System.out.println(element);
	          if(element == 23)
	              set.removeObserver(this);
	      }
    });
    for(int i = 0; i < 100; i++)
	      set.add(i);
}
```

> 람다를 사용한 이전 코드와 달리 익명 클래스를 사용했다.  
> `set.removeObserver()` 메소드에 함수 객체 자신을 넘겨야 하기 때문이다.  
> 람다는 자기 자신을 참조할 수단이 없다.

- 이 프로그램은 0부터 23까지 출력한 후 관찰자 자신을 구독해지한 다음 조용히 종료할 것이다.  
  그런데 실제로 실행해보면 그렇게 되지 않는다. 이 프로그램은 23까지 출력한 다음 `ConcurrentModificationException`을  
  던진다. 관찰자의 `added()`메소드 호출이 일어난 시점이 `notifyElementAdded()`가 관찰자들의  
  리스트를 순회하는 도중이기 때문이다. `added()`는 `ObservableSet`의 `removeObserver()` 호출하고,  
  이 메소드는 다시 `observers.remove()`를 호출한다. 여기서 문제가 발생한다. 리스트에서 원소를  
  제거하려는데, 마침 지금은 이 리스트를 순회하는 도중이다. 즉, 허용되지 않은 동작이다.  
  `notifyElementAdded()`에서 수행하는 순회는 동기화 블록 안에 있으므로 동시 수정이  
  일어나지 않도록 보장하지만, 정작 자신이 콜백을 거쳐 되돌아와 수정하는 것까지 막지는 못한다.

- 이번에는 이상한 것을 시도해보자. 구독해지를 하는 관찰자를 작성하는데, `removeObserver()`를  
  직접 호출하지 않고, 실행자 서비스(`ExecutorService`)를 사용해 다른 스레드에게 부탁할 것이다.

```java
public static void main(String[] args) {
    ObservableSet<Integer> set = new ObservableSet<>(new HashSet<>());
    set.addObserver(new SetObserver<>() {
	      public void added(ObservableSet<Integer> set, Integer element) {
	          System.out.println(element);
	          if(element == 23) {
		              ExecutorService exec = Executors.newSingleThreadExecutor();
		              try {
		                  exec.submit(() -> set.removeObserver(this)).get();
		              } catch(ExecutionException | InterruptedException ex) {
		                  throw new AssertionError(ex);
		              } finally {
		                  exec.shutdown();
		              }
	          }
	      }
    });
    for(int i = 0; i < 100; i++)
        set.add(i);
}
```

- 이 프로그램을 실행하면 예외는 나지 않지만 교착 상태에 빠진다. 백그라운드 스레드가 `set.removeObserver()`를  
  호출하면, 관찰자에 대해 lock을 걸려 하지만, lock을 얻을 수 없다. main 스레드가 이미 lock을  
  쥐고 있기 때문이다. 그와 동시에 main 스레드는 백그라운드 스레드가 관찰자를 제거하기만을 기다리는 중이다.  
  이것이 바로 교착 상태다.

- 사실 관찰자가 자신을 구독 해지사는 데 굳이 백그라운드 스레드를 이용할 이유가 없으니 조금  
  억지스러운 예시지이지만, 여기서 보인 문제 자체는 진짜다. 실제 시스템(특히 GUI Toolkit)에서도  
  동기화된 영역 안에서 외계인 메소드를 호출해 교착 상태에 빠지는 사례는 자주 있다.

- 앞서의 두 예시(예외와 교착상태)에서는 운이 좋았다. 동기화 영역이 보호하는 자원(관찰자)은  
  외계인 메소드인 `added()`가 호출될 때 일관된 상태였으니 말이다.

- 그렇다면 똑같은 상황이지만 불변식이 임시로 깨진 경우라면 어떻게 될까?  
  Java 언어의 lock은 재진입(reentrant)을 허용하므로 교착상태에 빠지지는 않는다. 예외를 발생시킨  
  첫 번째 예시에서라면 외계인 메소드를 호출하는 스레드는 이미 lock을 쥐고 있으므로, 다음번 lock  
  획득도 성공한다. 그 lock이 보호하는 데이터에 대해 개념적으로 관련이 없는 다른 작업이 진행중인 데도 말이다.  
  이것 때문에 정말 참혹한 결과가 빚어질 수도 있다. 문제의 주 원인은 lock이 제 구실을 못했기 때문이다.  
  재진입 가능 lock은 객체지향 멀티스레드 프로그램을 쉽게 구현할 수 있도록 해주지만, 응답 불가(교착 상태)가  
  될 상황을 안전 실패(데이터 훼손)로 변모시킬 수도 있다.

- 다행이 이런 문제는 대부분 어렵지 않게 해결할 수 있다. 외계인 메소드 호출을 동기화 블록의  
  바깥으로 옮기면 된다. `notifyElementAdded()` 메소드에서라면 관찰자 리스트를 복사해 사용하면  
  lock 없이도 안전하게 순회할 수 있다. 이 방식을 적용하면 앞서의 두 예제에서 예외 발생과 교착 상태 증상이  
  모두 사라진다.

```java
public class ObservableSet<E> extends ForwardingSet<E> {

    //..

    // 기존 코드
    private void notifyElementAdded(E element) {
	      synchronized(observers) {
	          for(SetObserver<E> observer : observers)
	              observer.added(this, element);
	      }
    }

    // 바뀐 코드
    private void notifyElementAdded(E element) {
	      List<SetObserver<E>> snapshot = null;
	      synchronized(observers) {
	          snapshot = new ArrayList<>(observers);
	      }
	      for(SetObserver<E> observer : snapshot)
	          observer.added(this, element);
    }

    //..
}
```

- 위 코드처럼 동기화 영역 바깥에서 호출되는 외계인 메소드를 열린 호출(open call)이라 한다.  
  외계인 메소드는 얼마나 오래 실행될지 알 수 없는데, 동기화 영역 내에서 호출된다면 그동안  
  다른 스레드는 보호된 자원을 사용하지 못하고 대기해야만 한다. 따라서 열린 호출은 실패 방지  
  효과 외에도 동시성 효율을 크게 개선해준다.

- 사실 외계인 메소드 호출을 동기화 블록 바깥으로 옮기는 더 나은 방법이 있다. Java의  
  동시성 라이브러리의 `CopyOnWriteArrayList`가 정확히 이 목적으로 특별히 설계된 것이다.  
  이름이 말해주듯 `ArrayList`를 구현한 클래스로, 내부를 변경하는 작업은 항상 깨끗한  
  복사본을 만들어 수행하도록 구현했다. 내부의 배열은 절대 수정되지 않으니 순회할 때  
  lock이 필요하지 않아 매우 빠르다. 다른 용도로 쓰인다면 `CopyOnWriteArrayList`는 끔찍히  
  느리겠지만, 수정할 일은 드물고 순회만 빈번히 일어나는 관찰자 리스트 용도로는 최적이다.

- `ObservableSet`을 `CopyOnArrayList`를 사용해 다시 구현하면 메소드들은 아래처럼 바뀐다.  
  `add()`, `addAll()`은 수정할 부분이 없다. 명시적으로 동기화한 곳이 사라졌다는 점에 주목하자.

```java
public class ObservableSet<E> extends ForwardingSet<E> {
    public ObservableSet(Set<E> set) { super(set); }

    private final List<SetObserver<E>> observers = new CopyOnWriteArrayList<>();

    public void addObserver(SetObserver<E> observer) {
	      observers.add(observer);
    }

    public boolean removeObserver(SetObserver<E> observer) {
	      return observers.remove(observer);
    }

    private void notifyElementAdded(E element) {
	      for(SetObserver<E> observer : observers)
	          observer.added(this, element);
    }

    @Override public boolean add(E element) {
	      boolean added = super.add(element);
	      if(added) notifyElementAdded(element);
	      return added;
    }

    @Override public boolean addAll(Collection<? extends E> c) {
	      boolean result = false;
	      for(E element : c) {
	          result |= add(element);
	      }
	      return result;
    }
}
```

- 기본 규칙은 **동기화 영역에서는 가능한 한 일을 적게 하는 것이다.** lock을 얻고, 공유 데이터를  
  검사하고, 필요하면 수정하고, lock을 놓는다. 오래 걸리는 작업이라면 Item 78의 지침을 어기지 않으면서  
  동기화 영역 밖으로 옮기는 방법을 찾아보자.

- 지금까지 정확성에 대해 보았으니, 이제 성능 측면도 간단히 살펴보자. Java의 동기화 비용은 빠르게  
  낮아져 왔지만, 과도한 동기화를 피하는 일은 과거 어느 때보다 중요하다. 멀티코어가 일반화된 오늘날, 과도한  
  동기화가 초래하는 진짜 비용은 lock을 얻는 데 드는 CPU Time이 아니다. 바로 경쟁하느라 낭비하는 시간,  
  즉 병렬로 실행할 기회를 잃고, 모든 코어가 메모리를 일관되게 보기 위한 지연시간이 진짜 비용이다.  
  가상머신의 코드 최적화를 제한한다는 점도 과도한 동기화의 또 다른 숨인 비용이다.

- 가변 클래스를 작성하려거든 아래의 두 선택지 중 하나를 따르자.

  - (1) 동기화를 전혀 하지 말고, 그 클래스를 동시에 사용해야 하는 클래스가 외부에서 알아서 동기화하게 하자.

  - (2) 동기화를 내부에서 수행해 스레드 안전한 클래스로 만들자. 단, 클라이언트가 외부에서 객체 전체에  
    lock을 거는 것보다 동시성을 월등히 개선할 수 있을 때만 이 방법을 선택해야 한다. `java.util`은 이제는  
    구식이 된 `Vector`와 `HashTable`을 제외하고 첫 번째 방식을 취했고, `java.util.concurrent`는  
    두 번째 방식을 선택했다.

- Java도 초창기에는 이 지침을 따르지 않는 클래스가 많았다. 예를 들어 `StringBuffer` 인스턴스는  
  거의 항상 단일 스레드에서 쓰였음에도 불구하고 내부적으로 동기화를 수행했다. 뒤늣게 `StringBuilder`가  
  등장한 이유이기도 하다.(`StringBuilder`는 그저 동기화하지 않은 `StringBuffer`이다.)  
  비슷한 이유로 스레드 안전한 의사 난수 발생기인 `java.util.Random`은 동기화하지 않는 버전인  
  `java.util.concurrent.ThreadLocalRandom`으로 대체되었다. 선택하기 어렵다면 동기화하지 말고,  
  대신 문서에 _"스레드 안전하지 않다."_ 고 명기하자.

- 클래스를 내부에서 동기화하기로 했다면, 락 분할(lock splitting), 락 스트라이핑(lock striping),  
  비차단 동시성 제어(nonblocking concurrency control) 등 다양한 기법을 동원해 동시성을 높힐 수 있다.

- 여러 스레드가 호출할 가능성이 있는 메소드가 정적 필드를 수정한다면, 그 필드를 사용하기 전에 반드시  
  동기해야 한다.(비결정적 행동도 용인하는 클래스라면 상관없다.) 그런데 클라이언트가 여러 스레드로 복제돼  
  구동되는 상황이라면 다른 클라이언트에서 이 메소드를 호출하는 것을 막을 수 없으니 외부에서 동기화할  
  방법이 없다. 결과적으로, 이 정적 필드가 심지어 private 이더라도 서로 관련 없는 스레드들이 동시에  
  읽고 수정할 수 있게 된다. 사실상 전역 변수와 같아진다는 뜻이다. 이전에 `generateSerialNumber()`의  
  nextSerialNumber 필드가 바로 이런 사례다.

```java
private static  int nextSerialNumber = 0;

public static synchronized int generateSerialNumber() {
    return nextSerialNumber++;
}
```

<hr/>

## 핵심 정리

- 교착 상태와 데이터 훼손을 피하려면 동기화 영역 안에서 외계인 메소드를 절대 호출하지 말자.  
  일반화해 이야기하면, 동기화 영역 안에서의 작업은 최소한으로 줄이자. 가변 클래스를 설계할  
  때는 스스로 동기화해야 할지 고민하자. 멀티코어 세상인 지금은 과도한 동기화를 피하는게  
  과거 어느 때보다 중요하다. 합당한 이유가 있을 때만 내부에서 동기화하고, 동기화했는지의  
  여부를 문서에 명확히 밝히자.

<hr/>
