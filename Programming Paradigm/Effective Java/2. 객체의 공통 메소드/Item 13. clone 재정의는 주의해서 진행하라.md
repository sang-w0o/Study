# clone 재정의는 주의해서 진행하라

- `Clonable`은 복제해도 되는 클래스임을 명시하는 용도인 Mix-In  
  인터페이스이지만, 아쉽게도 의도한 목적을 제대로 이루지 못했다.  
  가장 큰 문제는 `clone()` 메소드가 선언된 곳이 `Clonable`이 아닌  
  `Object`이고, 그마저도 protected라는 데 있다. 그래서 `Clonable`을  
  구현하는 것만으로는 외부 객체에서 `clone()` 메소드를 호출할 수 없다.  
  리플렉션을 사용하면 가능하지만, 100% 성공하는 것도 아니다. 해당 객체가 접근이  
  허용된 `clone()` 메소드를 제공한다는 보장이 없기 때문이다. 하지만 이를 포함한  
  여러 문제점에도 불구하고 `Clonable` 방식은 널리 쓰이고 있어서 알아두는 것이 좋다.  
  이번 아이템에서는 `clone()` 메소드를 잘 동작하게끔 구현하는 방법과  
  언제 그렇게 해야 하는지를 보고, 가능한 다른 선택지에 대해 논의해보자.

- 메소드 하나 없는 `Clonable` 인터페이스는 무슨 일을 할까?  
  이 인터페이스는 놀랍게도 `Object`의 protected 메소드인 `clone()`의  
  동작 방식을 결정한다. `Clonable`을 구현한 클래스의 인스턴스에서 `clone()`을  
  호출하면, 그 객체의 필드들을 하나하나 복사한 객체를 반환하며, 그렇지 않은 클래스의  
  인스턴스에서 호출하면 `CloneNotSupportedException`을 던진다. 이는 인터페이스를  
  상당히 이례적으로 사용한 예이니 따라 하지는 말자. 인터페이스를 구현한다는 것은 일반적으로  
  해당 클래스가 그 인터페이스에서 정의한 기능을 제공한다고 선언하는 행위다.  
  그런데 `Clonable`의 경우에는 상위 클래스에 정의된 protected 메소드의  
  동작 방식을 변경한 것이다.

- 명세에서는 이야기하지 않지만 **실무에서 `Clonable`을 구현한 클래스는 `clone()`**  
  **메소드를 public으로 제공하며, 사용자는 당연히 복제가 이뤄지리라 기대한다.**  
  이 기대를 만족시키려면 그 클래스와 모든 상위 클래스는 복잡하고, 강제할 수 없고,  
  허술하게 기록된 프로토콜을 지켜야만 하는데, 그 결과로 깨지기 쉽고, 위험하고,  
  모순적인 매커니즘이 탄생한다. 생성자를 호출하지 않고도 객체를 생성할 수 있게 되는 것이다.

- `clone()` 메소드의 일반 규약은 허술하다.  
  `Object` 명세에서 가져온 다음 설명을 보자.

> 이 객체의 복사본을 생성해 반환한다. _복사_ 의 정확한 뜻은 그 객체를 구현한 클래스에 따라  
> 다를 수 있다. 일반적인 의도는 다음과 같다. 어떤 객체 x에 대해서 다음 식은 참이다.
>
> `x.clone() != x`
>
> 또한 다음 식도 참이다.
>
> `x.clone().getClass() == x.getClass()`
>
> 하지만 이상의 요구를 반드시 만족해야 하는 것은 아니다.  
> 한편 다음 식도 일반적으로 참이지만, 역시 필수는 아니다.
>
> `x.clone().equals(x)`
>
> 관례상, 이 메소드가 반환하는 객체는 `super.clone()`을 호출해 얻어야 한다.  
> 이 클래스와 `Object`를 제외한 모든 상위 클래스가 이 관례를 따른다면 다음 식은 참이다.
>
> `x.clone().getClass() == x.getClass()`
>
> 관례상, 반환된 객체와 원본 객체는 독립적이어야 한다. 이를 만족하려면 `super.clone()`으로  
> 얻은 객체의 필드 중 하나 이상을 반환 전에 수정해야 할 수도 있다.

- 강제성이 없다는 점만 빼면 생성자 연쇄(Constructor Chaining)와 살짝 비슷한 메커니즘이다.  
  즉, `clone()`메소드가 `super.clone()`이 아닌, 생성자를 호출해 얻은 인스턴스를  
  반환해도 컴파일러는 불평하지 않을 것이다. 하지만 이 클래스의 하위 클래스에서 `super.clone()`을  
  호출한다면 잘못된 클래스의 객체가 만들어져, 결국 하위 클래스의 `clone()`메소드가 제대로  
  동작하지 않게 된다. `clone()`을 재정의한 클래스가 final이라면 걱정해야할 하위 클래스가 없으니  
  이 관례는 무시해도 안전하다. 하지만 final 클래스의 `clone()`메소드가 `super.clone()`을  
  호출하지 않는다면 `Clonable`을 구현할 이유도 없다. `Object`의 `clone()` 구현의  
  동작 방식에 기댈 필요가 없기 때문이다.

- 제대로 동작하는 `clone()`메소드를 가진 상위 클래스를 상속해 `Clonable`을 구현하고 싶다 해보자.  
  먼저 `super.clone()`을 호출한다. 그렇게 얻은 객체는 원본의 완벽한 복제본일 것이다.  
  클래스에 정의된 모든 필드는 원본 필드와 똑같은 값을 갖는다. 모든 필드가 기본 타입이거나  
  불변 객체를 참조한다면 이 객체는 완벽히 우리가 원하는 상태라 더 손볼 것이 없다.  
  이전에 본 `PhoneNumber` 클래스가 여기에 해당한다. 그런데 쓸데없는 복사를 지양한다는 관점에서  
  보면, 불변 클래스는 굳이 `clone()` 메소드를 제공하지 않는게 좋다. 이 점을 고려해 `PhoneNumber`의  
  `clone()`메소드는 아래처럼 구현할 수 있다.

```java
@Override public PhoneNumber clone() {
  try {
    return (PhoneNumber)super.clone();
  } catch(CloneNotSupportedException e) {
    throw new AssertionError();  // 일어날 수 없는 일
  }
}
```

- 이 메소드가 동작하게 하려면 `PhoneNumber` 클래스가 `Clonable`을 구현해야 한다.  
  `Object.clone()`은 `Object`를 반환하지만, `PhoneNumber.clone()`은  
  `PhoneNumber`를 반환하게 했다. Java가 공변 반환 타이핑을 지원하니 이렇게 하는 것이  
  가능하고, 권장하는 방식이기도 하다. 달리 말해서, 재정의한 메소드의 반환 타입은 상위 클래스의  
  메소드가 반환하는 타입의 하위 타입일 수 있다. 이 방식으로 클라이언트가 형변환하지 않아도  
  되게끔 해주자. 이를 위해 앞 코드에서는 `super.clone()`으로 얻은 객체를 반환하기 전에  
  `PhoneNumber`로 형변환 하였다. (절대 실패하지 않는다.)

- `super.clone()` 호출을 try-catch 블록으로 감싼 이유는 `Object#clone()`이  
  Checked Exception인 `CloneNotSupportedException`을 던지도록 선언되었기 때문이다.  
  `PhoneNumber`가 `Clonable`을 구현하니, `super.clone()`이 성공할 것임을 안다.  
  이 거추장스러운 코드는 `CloneNotSupportedException`이 Unchecked Exception이었어야  
  한다는 신호다.

- 간단했던 앞서의 구현이 클래스가 가변 객체를 참조하는 순간 재앙으로 돌변한다.  
  `Stack` 클래스를 보자.

```java
public class Stack {
  private Object[] elements;
  private int size = 0;
  private static final int DEFAULT_INITIAL_CAPACITY = 16;

  public Stack() {
    elements = new Object[DEFAULT_INITIAL_CAPACITY];
  }

  public void push(Object o) {
    ensureCapacity();
    elements[size++] = o;
  }

  public Object pop() {
    if(size == 0) {
      throw new EmptyStackException();
    }
    Object result = elements[--size];
    elements[size] = null;
    return result;
  }

  private void ensureCapacity() {
    if(elements.length == size) {
      elements = Arrays.copyOf(elements, size * 2 + 1);
    }
  }
}
```

- 이 클래스를 복제할 수 있도록 만들어보자. `clone()` 메소드가 단순히  
  `super.clone()`의 결과를 그대로 반환하면 어떻게 될까? 반환된 `Stack`  
  인스턴스의 size 필드는 올바른 값을 갖겠지만, elements 필드는 원본 `Stack`  
  인스턴스와 똑같은 배열을 참조할 것이다. 원본이나 복제본 중 하나를 수정하면, 다른 하나도  
  수정되어 불변식을 해친다는 이야기다. 따라서 프로그램이 이상하게 동작하거나  
  NPE를 던질 것이다.

- `Stack` 클래스의 하나뿐인 생성자를 호출한다면, 이러한 상황은 절대 일어나지 않는다.  
  **`clone()`메소드는 사실상 생성자와 같은 효과를 낸다. 즉, `clone()`은 원본**  
  **객체에 아무런 해를 끼치지 않는 동시에 복제된 객체의 불변식을 보장해야 한다.**  
  그래서 `Stack`의 `clone()`메소드는 제대로 동작하려면 스택 내부 정보를 복사해야 하는데,  
  가장 쉬운 방법은 elements 배열의 `clone()`을 재귀적으로 호출해주는 것이다.

```java
@Override public Stack clone {
  try {
    Stack result = (Stack) super.clone();
    result.elements = elements.clone();
    return result;
  } catch (CloneNotSupportedException e) {
    throw new AssertionError();
  }
}
```

- `elements.clone()`의 결과를 `Object[]`로 형변환할 필요는 없다.  
  배열의 `clone()`은 런타임 타입과 컴파일타임 타입 모두가 원본 배열과 똑같은 배열을 반환한다.  
  따라서 배열을 복제할 때는 배열의 `clone()` 메소드를 사용하라고 권장한다.  
  사실, 배열이 `clone()` 기능을 제대로 사용하는 유일한 예라 할 수 있다.

- 한편, elements 필드가 final이었다면 앞서의 방식은 작동하지 않는다.  
  final 필드에는 새로운 값을 할당할 수 없기 때문이다. 이는 근본적인 문제로, 직렬화와 마찬가지로  
  `Clonable` 아키텍쳐는 **가변 객체를 참조하는 필드는 final로 선언하라**는 일반 용법과 충돌한다.  
  단, 원본과 복제된 객체가 그 가변 객체를 공유해도 안전하다면 괜찮다. 그래서 복제할 수 있는  
  클래스를 만들기 위해 일부 필드에서는 final 한정자를 제거해야 할 수도 있다.

- `clone()`을 재귀적으로 호출하는 것만으로는 충분하지 않을 때도 있다. 이번에는 해시테이블용  
  `clone()`메소드를 생각해보자. 해시테이블 내부는 버킷들의 배열이고, 각 버킷은 key-value의  
  쌍을 담는 연결 리스트의 첫 번째 엔트리를 참조한다. 그리고 성능을 위해 `java.util.LinkedList` 대신  
  직접 구현한 경량 연결 리스트를 사용해보자.

```java
public class HashTable implements Clonable {
  private Entry[] buckets = /* ... */;

  private static class Entry {
    final Object key;
    Object value;
    Entry next;

    Entry(Object key, Object value, Entry next) {
      this.key = key;
      this.value = value;
      this.next = next;
    }
  }
  //..
}
```

- `Stack`에서처럼 단순히 버킷 배열의 `clone()`을 재귀적으로 호출해보자.

```java
@Override public HashTable clone {
  try {
    HashTable result = (HashTable) super.clone();
    result.buckets = buckets.clone();
    return result;
  } catch(CloneNotSupportedException e) {
    throw new AssertionError();
  }
}
```

- 복제본은 자신만의 버킷 배열을 갖지만, 이 배열은 원본과 같은 연결 리스트를  
  참조하여 원본과 복제본 모두 예기치 않게 동작할 가능성이 생긴다.  
  이를 해결하려면 각 버킷을 구성하는 연결 리스트를 복사해야 한다.  
  아래는 일반적인 해법이다.

```java
public class HashTable implements Clonable {
  private Entry[] buckets = /* ... */;

  private static class Entry {
    final Object key;
    Object value;
    Entry next;

    Entry(Object key, Object value, Entry next) {
      this.key = key;
      this.value = value;
      this.next = next;
    }

    // 이 entry가 가리키는 연결 리스트를 재귀적으로 복사
    Entry deepCopy() {
      return new Entry(key, value, next == null ? null : next.deepCopy());
    }
  }

  @Override public HashTable clone() {
    try {
      HashTable result = (HashTable) super.clone();
      result.buckets = new Entry[buckets.length];
      for (int i = 0; i < buckets.length; i++)
        if(buckets[i] != null)
          result.buckets[i] = buckets[i].deepCopy();
      return result;
    } catch(CloneNotSupportedException e) {
      throw new AssertionError();
    }
  }
  //..
}
```

- private 클래스인 `HashTable.Entry`는 깊은 복사(Deep Copy)를 지원하도록  
  보강되었다. `HashTable`의 `clone()`메소드는 먼저 적절한 크기의 새로운 bucket  
  배열을 할당한 다음, 원래의 bucket 배열을 순회하며 비어 있지 않은 각 버킷에 대해  
  깊은 복사를 수행한다. 이때, `Entry`의 `deepCopy()` 메소드는 자신이 가리키는  
  연결 리스트 전체를 복사하기 위해 자신을 재귀적으로 호출한다. 이 기법은 간단하며, 버킷이  
  너무 길지 않다면 잘 작동한다. 하지만 연결 리스트를 복제하는 방법으로는 그다지 좋지 않다.  
  재귀 호출 때문에 리스트의 원소 수만큼 스택 프레임을 소비하여, 리스트가 길면 스택  
  오버플로우를 일으킬 위험이 있기 때문이다. 이 문제를 피하려면 `deepCopy()`를  
  재귀 호출 대신 반복자를 써서 순회하는 방향으로 수정해야 한다.

```java
Entry deepCopy() {
  Entry result = new Entry(key, value, null);
  for(Entry p = result; p.next != null; p = p.next) {
    p.next = new Entry(p.next.key, p.next.value, p.next.next);
  }
  return result;
}
```

- 이제 복잡한 가변 객체를 복제하는 마지막 방법을 살펴보자.  
  먼저 `super.clone()`을 호출하여 얻은 객체의 모든 필드를 초기 상태로 설정한 다음,  
  원본 객체의 상태를 다시 생성하는 고수준 메소드들을 호출한다. `HashTable`의 경우라면  
  buckets 필드를 새로운 버킷 배열로 초기화한 다음, 원본 테이블에 담긴 모든 key-value 쌍  
  각각에 대해 복제본 테이블의 `put(key, value)` 메소드를 호출해 둘의 내용이 똑같게  
  해주면 된다. 이처럼 고수준 API를 활용해 복제하면 보통은 간단하고 제법 우아한 코드를  
  얻게 되지만, 아무래도 저수준에서 바로 처리할 때보다는 느리다. 또한 `Clonable` 아키텍쳐의  
  기초가 되는 필드 단위 객체 복사를 우회하기 때문에 전체 `Clonable` 아키텍쳐와는  
  어울리지 않는 방식이기도 하다.

- 생성자에서는 재정의될 수 있는 메소드를 호출하지 않아야 하는데, `clone()`도 마찬가지다.  
  만약 `clone()`이 하위 클래스에서 재정의한 메소드를 호출하면, 하위 클래스는 복제  
  과정에서 자신의 상태를 교정할 기회를 잃게 되어 원본과 복제본의 상태가 달라질 가능성이 크다.  
  따라서 바로 전에 말한 `put(key, value)`는 final이나 private 메소드여야 한다.

- `Object`의 `clone()`메소드는 `CloneNotSupportedException`을 던진다고 선언했지만,  
  재정의한 메소드는 그렇지 않다. **public인 `clone()`에서는 throws 절을 없애야 한다.**  
  검사 예외를 던지지 않아야 그 메소드를 사용하기 편하기 때문이다.

- 상속해서 쓰기 위한 클래스 설계 방식 두 가지 중 어느 쪽에서든, 상속용 클래스는 `Clonable`을  
  구현하면 안된다. 개발자가 `Object`의 방식을 모방할 수도 있다. 제대로 작동하는 `clone()`를  
  구현해 protected로 두고, `CloneNotSupportedException`도 던질 수 있다고 선언하는 것이다.  
  이 방식은 마치 `Object`를 바로 상속할 때처럼 `Clonable` 구현 여부를 하위 클래스에서  
  선택하도록 해준다. 다른 방법으로는, `clone()`을 동작하지 않게 구현해놓고 하위 클래스에서  
  재정의하지 못하게 할 수도 있다. 아래처럼 `clone()`을 퇴화시켜놓으면 된다.

```java
@Override
protected final Object clone() throws CloneNotSupportedException {
  throw new CloneNotSupportedException();
}
```

- 기억해둬야 할게 하나 더 있는데, `Clonable`을 구현한 스레드 안전 클래스를 작성할 때는  
  `clone()` 메소드 역시 동기화시켜 주어야 한다. `Object`의 `clone()`메소드는 동기화를  
  신경쓰지 않았다. 그러니 `super.clone()` 호출 외에 다른 할 일이 없더라도 `clone()`을  
  재정의하고 동기화해줘야 한다.

- 요약하자면, `Clonable`을 구현하는 모든 클래스는 `clone()`을 재정의해야 한다.  
  이때 접근 제한자는 public으로, 반환 타입은 클래스 자신으로 변경한다.  
  이 메소드는 가장 먼저 `super.clone()`을 호출한 후 필요한 필드를 전부 적절히 수정한다.  
  일반적으로 이 말은 그 객체의 내부 _깊은 구조_ 에 숨어 있는 모든 가변 객체를 복사하고,  
  복제본이 가진 객체 참조 모두가 복사된 객체들을 가리키게 함을 뜻한다. 이러한 내부 복사는 주로  
  `clone()`을 재귀적으로 호출해 구현하지만, 이 방식이 항상 최선인 것은 아니다.  
  기본 타입 필드와 불변 객체 참조만 갖는 클래스라면 아무 필드로 수정할 필요가 없다.  
  단, 일련번호나 고유 ID는 비록 기본 타입이나 불변일지라도 수정해줘야 한다.

- 그런데 이 모든 작업이 꼭 필요한걸까? 다행이도 이처럼 복잡한 경우는 드물다.  
  `Clonable`을 이미 구현한 클래스를 확장한다면 어쩔 수 없이 `clone()`을 잘 작동하도록  
  구현해야 한다. 그렇지 않은 상황에서는 **복사 생성자와 복사 팩토리라는 더 나은 객체 복사 방식을 제공**할  
  수 있다. **복사 생성자**란 단순히 자신과 같은 클래스의 인스턴스를 인수로 받는 생성자를 말한다.  
  아래 예시를 보자.

```java
public Yum(Yum yum) { /* ... */ }
```

- **복사 팩토리**는 복사 생성자를 모방한 정적 팩토리이다.

```java
public static Yum newInstance(Yum yum) { /* ... */ }
```

- 복사 생성자와 그 변형인 복사 팩토리는 `Clonable`/`clone` 방식보다 더 나은 면이 많다.  
  언어 모순적이고 위험천만한 객체 생성 메커니즘(생성자를 쓰지 않는 방식)을 사용하지 않으며,  
  엉성하게 문서화된 규약에 기대지 않고, 정상적인 final 필드 용법과도 충돌하지 않으며,  
  불필요한 검사 예외를 던지지도 않고 형변환도 필요 없다.

- 여기서 끝이 아니다. 복사 생성자와 복사 팩토리는 해당 클래스가 구현한 _인터페이스_ 타입의  
  인스턴스를 인수로 받을 수 있다. 예를 들어 관례상 모든 범용 클래스 구현체는 `Collection`이나  
  `Map` 타입을 받는 생성자를 제공한다. 인스턴스 기반 복사 생성자와 복사 팩토리의 더 정확한 이름은  
  **변환 생성자(Conversion Constructor)** 와 **변환 팩토리(Conversion Factory)** 이다.  
  이들을 사용하면 클라이언트는 원본의 구현 타입에 얽매이지 않고 복제본의 타입을 직접  
  선택할 수 있다. 예를 들어 `HashSet` 객체 s를 `TreeSet` 타입으로 복제할 수 있다.  
  `clone()`으로는 불가한 이 기능을 변환 생성자로는 간단히 `new TreeSet<>(s)`로 처리할 수 있다.

---

## 핵심 정리

- `Clonable`이 몰고 온 모든 문제를 되짚어봤을 때, 새로운 인터페이스를 만들 때는 절대  
  `Clonable`을 확장해서는 안되며, 새로운 클래스도 이를 구현해서는 안된다. final 클래스라면  
  `Clonable`을 구현해도 위험이 크진 않지만, 성능 최적화 관점에서 검토한 후 별다른 문제가  
  없을 때만 드물게 허용해야 한다. 기본 원칙은 _복제 기능은 생성자와 팩토리를 이용하는게 최고_ 라는  
  것이다. 단, 배열만은 `clone()`메소드 방식이 가장 깔끔한, 이 규칙의 예외라 할 수 있다.

---
