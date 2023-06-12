# 상속을 고려해 설계하고 문서화하라. 그렇지 않았다면 상속을 금지하라.

- Item 18에서는 상속을 염두에 두지 않고 설계했고, 상속할 때의 주의점도 문서화해놓지 않은  
  _'외부'_ 클래스를 상속할 때의 위험성을 봤다. 여기서 _외부_ 란 프로그래머의 통제권 밖에  
  있어서 언제 어떻게 변경될지 모른다는 뜻이다. 그렇다면 상속을 고려한 설계와 문서화란 무엇일까?

- 우선 메소드를 재정의하면 어떤 일이 일어나는지를 정확히 정리하여 문서로 남겨야 한다.  
  달리 말하면, **상속용 클래스는 재정의할 수 있는 메소드들을 내부적으로 어떻게 이용하는지**  
  **(자기사용) 문서로 남겨야 한다.** 클래스의 API로 공개된 메소드에서 클래스 자신의 또다른  
  메소드를 호출할 수도 있다. 그런데 마침 호출되는 메소드가 재정의 가능 메소드라면 그 사실을  
  호출하는 메소드의 API 설명에 적시해야 한다. 덧붙여서 어떤 순서로 호출하는지, 각각의  
  호출 결과가 이어지는 처리에 어떤 영향을 주는지도 담아야 한다.  
  (_재정의 가능_ 이란 public, protected 메소드 중 final이 아닌 모든 메소드를 뜻한다.)  
  더 넓게 말하면, 재정의 가능 메소드를 호출할 수 있는 모든 상황을 문서로 남겨야 한다.  
  예를 들어 백그라운드 스레드나 정적 초기화 과정에서도 호출이 일어날 수 있다.

- API 문서의 메소드 설명 끝에서 종종 "Implementation Requirements"로 시작하는 절을  
  볼 수 있는데, 그 메소드의 내부 동작 방식을 설명하는 부분이다. 이 절은 메소드의 주석에  
  `@implSpec` 태그를 붙여주면 Javadoc 도구가 생성해준다.  
  아래는 `java.util.AbstractCollection`에서 발췌한 예시이다.

```java
/**
 * {@inheritDoc}
 *
 * <p>This implementation iterates over the collection looking for the
 * specified element.  If it finds the element, it removes the element
 * from the collection using the iterator's remove method.
 *
 * <p>Note that this implementation throws an
 * <tt>UnsupportedOperationException</tt> if the iterator returned by this
 * collection's iterator method does not implement the <tt>remove</tt>
 * method and this collection contains the specified object.
 *
 * @throws UnsupportedOperationException {@inheritDoc}
 * @throws ClassCastException            {@inheritDoc}
 * @throws NullPointerException          {@inheritDoc}
 */
public boolean remove(Object o) { /* ... */ }
```

- 위 설명에 따르면 `iterator()` 메소드를 재정의하면 `remove()`의 동작에 영향을 줄 수  
  있음을 확실히 알 수 있다. 이는 Item 18에서 `HashSet`을 상속하여 `add()`를 재정의한 것이  
  `addAll()`에까지 영향을 준다는 사실을 알 수 없던 것과 아주 대조적이다.

- 하지만 이런 식은 _"좋은 API 문서란 '어떻게' 가 아닌 '무엇'을 하는지를 설명해야 한다"_ 라는 격언과는  
  대조된다. 상속이 캡슐화를 해치기 때문에 일어나는 안타까운 현실이다. 클래스를 안전하게 상속할 수 있게  
  하려면 내부 구현 방식을 설명해야만 한다.

- 이처럼 내부 메커니즘을 문서로 남기는 것만이 상속을 위한 설계의 전부는 아니다. 효율적인 하위 클래스를  
  큰 어려움 없이 만들 수 있게 하려면 **클래스의 내부 동작 과정 중간에 끼어들 수 있는 hook을 잘 선별하여**  
  **protected 메소드 형태로 공개해야 할 수도 있다.** 드물게는 protected 필드도 공개해야 할 수도 있다.  
  `java.util.AbstractList`의 `removeRange()` 메소드를 예로 보자.

```java
/**
 * Removes from this list all of the elements whose index is between
 * {@code fromIndex}, inclusive, and {@code toIndex}, exclusive.
 * Shifts any succeeding elements to the left (reduces their index).
 * This call shortens the list by {@code (toIndex - fromIndex)} elements.
 * (If {@code toIndex==fromIndex}, this operation has no effect.)
 *
 * <p>This method is called by the {@code clear} operation on this list
 * and its subLists.  Overriding this method to take advantage of
 * the internals of the list implementation can <i>substantially</i>
 * improve the performance of the {@code clear} operation on this list
 * and its subLists.
 *
 * <p>This implementation gets a list iterator positioned before
 * {@code fromIndex}, and repeatedly calls {@code ListIterator.next}
 * followed by {@code ListIterator.remove} until the entire range has
 * been removed.  <b>Note: if {@code ListIterator.remove} requires linear
 * time, this implementation requires quadratic time.</b>
 *
 * @param fromIndex index of first element to be removed
 * @param toIndex index after last element to be removed
 */
protected void removeRange(int fromIndex, int toIndex) {
  ListIterator<E> it = listIterator(fromIndex);
  for (int i=0, n=toIndex-fromIndex; i<n; i++) {
    it.next();
    it.remove();
  }
}
```

- `List` 구현체의 최종 사용자는 `removeRange()` 메소드에는 관심이 없다. 그럼에도 이 메소드를  
  제공한 이유는 단지 하위 클래스에서 부분리스트의 `clear()` 메소드를 고성능으로 만들기 쉽게 하기  
  위함이다. `removeRange()`가 없다면 하위 클래스에서 `clear()`를 호출하면 제거할 원소 수의  
  제곱에 비례해 성능이 느려지거나, 부분리스트의 메커니즘을 밑바닥부터 새롭게 구현해야 했을 것이다.

> 참고로 `AbstractList#clear()`의 구현부는 아래와 같다.
>
> ```java
> public void clear() { removeRange(0, size()); }
> ```

- 그렇다면 상속용 클래스를 설계할 때 어떤 메소드를 protected로 노출해야 할까?  
  안타깝게도 마법은 없다. 심사숙고해서 잘 예측해본 다음, 실제 하위 클래스를 만들어 시험해 보는 것이  
  최선이다. protected 메소드 하나하나가 내부 구현에 해당하므로 그 수는 가능한 한 적어야 한다.  
  한편으로는 너무 적게 노출해서 상속으로 얻는 이점마저 없애지 않도록 주의해야 한다.

- **상속용 클래스를 시험하는 방법은 직접 하위 클래스를 만들어보는 것이 '유일'하다.**  
  꼭 필요한 protected 멤버를 놓쳤다면 하위 클래스를 작성할 때 그 빈자리가 확연히 드러난다.  
  거꾸로, 하위 클래스를 여러 개 만들 때까지 전혀 쓰이지 않는 protected 멤버는  
  사실 private이어야 할 가능성이 크다. 대략 하위 클래스를 3개 정도 만들어 테스트해보자.  
  그리고 이 중 하나 이상은 제3자가 작성하게 해보자.

- 널리 쓰일 클래스를 상속용으로 설계한다면 개발자가 문서화한 내부 사용 패턴과 protected 메소드,  
  필드를 구현하면서 선택한 결정에 영원히 책임져야함을 잘 인식해야 한다. 이 결정들이 그 클래스의  
  성능과 기능에 영원한 족쇄가 될 수 있다. 그러니 **상속용으로 설계한 클래스는 배포 전에 반드시**  
  **하위 클래스를 만들어 검증해야 한다.**

- 또한 상속하려는 사람을 위해 덧붙인 설명은 단순히 그 클래스의 인스턴스만 만들어 사용할 프로그래머에게는  
  필요 없는 군더더기일 뿐이다. 현재로서는 일반적인 API 설명과 상속용 설명을 구분해주는 도구가  
  마땅치 않다.

- 상속을 허용하는 클래스가 지켜야할 제약은 아직 몇 개 더 있다. **상속용 클래스의 생성자는 직접적으로든**  
  **간접적으로든 재정의 가능 메소드를 호출해서는 안된다.** 이 규칙을 어기면 프로그램이 오동작할 것이다.  
  상위 클래스의 생성자가 하위 클래스의 생성자보다 먼저 실행되므로, 하위 클래스에서 재정의한 메소드가  
  하위 클래스의 생성자보다 먼저 호출된다. 이때 그 재정의한 메소드가 하위 클래스의 생성자에서 초기화하는  
  값에 의존한다면 의도대로 동작하지 않을 것이다. 이 규칙을 어기는 코드를 보자.

```java
public class Super {

  // 잘못되었다 - 생성자가 재정의 가능 메소드를 호출한다.
  public Super() {
    overrideMe();
  }

  public void overrideMe() {}
}
```

- 이제 위 클래스를 상속받는 하위 클래스를 보자.

```java
public final class Sub extends Super {

  // 초기화되지 않은 final 필드, 생성자에서 초기화된다.
  private final Instant instant;

  Sub() {
    instant = Instant.now();
  }

  // 재정의 가능 메소드, 상위 클래스의 생성자에서 호출된다.
  @Override public void overrideMe() {
    System.out.println(instant);
  }
}
```

- 클라이언트는 아래와 같이 사용한다.

```java
Sub sub = new Sub();
sub.overrideMe();
```

- 위 프로그램이 instant를 두번 호출하리라 기대했지만, 첫 번째는 null을 출력한다.  
  상위 클래스의 생성자가 하위 클래스의 생성자가 instant 필드를 초기화하기도 전에  
  `overrideMe()`를 호출하기 때문이다. final 필드의 상태가 이 프로그램에서는 두 가지임에 주목하자.  
  (정상이라면 단 하나뿐이어야 한다.) `overrideMe()`에서 instant 객체의 메소드를 호출하려 한다면  
  상위 클래스의 생성자가 `overrideMe()`를 호출하려 할 때 NPE를 던지게 된다.  
  이 프로그램이 NPE를 발생시키지 않는 유일한 이유는 `println()`이 null도 받아들이기 때문이다.

> private, final, static 메소드는 재정의가 불가하니 생성자에서 안심하고 호출해도 된다.

- `Clonable`과 `Serializable` 인터페이스는 상속용 설계의 어려움을 한층 더해준다.  
  둘 중 하나라도 구현한 클래스를 상속할 수 있게 설계하는 것은 일반적으로 좋지 않은 생각이다.  
  그 클래스를 확장하려는 프로그래머에게 엄청난 부담을 지우기 때문이다. 물론 이 인터페이스를 하위클래스에서  
  원한다면 구현하도록 하는 특별한 방법도 있다.

- `clone()`과 `readObject()`는 생성자와 비슷한 효과를 낸다.(새로운 객체 생성)  
  따라서 상속용 클래스에서 `Clonable` 이나 `Serializable`을 구현할지 정해야 한다면,  
  이들을 구현할 때 따르는 제약도 생성자와 비슷하다는 점에 주의하자. 즉 **`clone()`과 `readObject()`**  
  **모두 직접적으로든 간접적으로든 재정의 가능한 메소드를 호출해서는 안된다.** `readObject()`의 경우  
  하위 클래스의 상태가 미처 역직렬화 되기 전에 재정의한 메소드부터 호출하게 된다. `clone()`의 경우  
  하위 클래스의 `clone()` 메소드가 복제본의 상태를 올바른 상태로 수정하기 전에 재정의한 메소드를 호출한다.  
  어느 쪽이든 프로그램의 오작동으로 이어질 것이다. 특히 `clone()`이 잘못되면 복제본뿐만 아니라  
  원본 객체에도 피해를 줄 수 있다. 예를 들어 재정의한 메소드에서 원본 객체의 깊숙한 내부 자료구조까지  
  복제본으로 완벽히 복사됐다고 가정하고, 복제본을 수정했다고 하자. 그런데 사실은 `clone()`이  
  완벽하지 못했어서 복제본의 내부 어딘가에서 여전히 원본 객체의 데이터를 참조하고 있다면  
  원본 객체도 피해를 입는 것이다.

- 마지막으로 `Serializable`을 구현한 상속용 클래스가 `readResolve()`나 `writeReplace()`  
  메소드를 갖는다면 이 메소드들은 private이 아닌 protected로 선언해야 한다. private으로 선언한다면  
  하위 클래스에서 무시되기 때문이다. 이 역시 상속을 허용하기 위해 내부 구현을 클래스 API로  
  공개하는 예시 중 하나이다.

- 이제 **클래스를 상속용으로 설계하려면 엄청난 노력이 들고, 그 클래스에 안기는 제약도 상당함**을 알았다.  
  절대 가볍게 생각하고 정할 문제가 아니다. 추상 클래스나 인터페이스의 골격 구현처럼 상속을 허용하는 것이  
  명백히 정당한 상황이 있고, 불변 클래스처럼 명백히 잘못된 상황이 있다.

- 그렇다면 그 외의 일반적인 구체 클래스는 어떨까? 전통적으로 이런 클래스들은 final도 아니고 상속용으로  
  설계되거나 문서화되지도 않았다. 하지만 그대로 두면 위험하다. 클래스에 변화가 생길 때마다 하위 클래스를  
  오동작하게 만들 수 있기 때문이다. 실제로도 보통의 구체 클래스를 그 내부만 수정했음에도 이를 확장한  
  클래스에서 문제가 생겼다는 bug report를 받는 일이 드물지 않다.

- **이 문제를 해결하는 가장 좋은 방법은 상속용으로 설계하지 않은 클래스는 상속을 금지하는 것이다.**  
  상속을 금지하는 방법은 두 가지다. 둘 중 더 쉬운 쪽은 클래스를 final로 선언하는 방법이다.  
  두 번째 방법은 모든 생성자를 private 혹은 package-private으로 선언하고, public 정적  
  팩토리를 만들어주는 방법이다. 정적 팩토리 방법은 내부에서 다양한 하위 클래스를 만들어 쓸 수 있는  
  유연성을 준다. 둘 중 어느 방식이든 좋다.

- 이 이야기(상속 금지)는 다소 논란의 여지가 있다. 그동안 수많은 프로그래머들이 일반적인 구체 클래스를  
  상속해 계측, 통지, 동기화, 기능 제약 등을 추가해왔을 테니 말이다. 핵심 기능을 정의한 인터페이스가 있고,  
  클래스가 그 인터페이스를 구현했다면 상속을 금지해도 개발하는 데 아무런 어려움이 없을 것이다.  
  `Set`, `Map`, `List` 가 좋은 예시이다. Item 18에서 설명한 Wrapper class 패턴 역시  
  기능을 증강할 때 상속 대신 쓸 수 있는 좋은 대안이다.

- 구체 클래스가 표준 인터페이스를 구현하지 않았는데 상속을 금지하면 사용하기에 꽤 번거로워진다.  
  이런 클래스라도 상속을 꼭 허용해야겠다면 합당한 방법이 하나 있다. **클래스 내부에서는 재정의 가능한**  
  **메소드를 사용하지 않게 만들고, 이 사실을 문서로 남기는 것**이다. 재정의 가능 메소드를 호출하는  
  자기 사용 코드를 완벽히 제거하라는 말이다. 이렇게 하면 상속해도 그리 위험하지 않은 클래스를  
  만들 수 있다. 메소드를 재정의해도 다른 메소드의 동작에 아무런 영향을 주지 않기 때문이다.

- 클래스의 동작을 유지하면서 재정의 가능 메소드를 사용하는 코드를 제거하는 기계적인 방법은 아래와 같다.

  - (1) 각각의 재정의 가능 메소드의 본문 코드를 private _도우미 메소드_ 로 옮긴다.
  - (2) 위에서 만든 *도우미 메소드*를 호출하도록 바꾼다.

---

## 핵심 정리

- 상속용 클래스를 설계하기란 결코 만만치 않다. 클래스 내부에서 스스로를 어떻게 사용하는지(자기사용 패턴)  
  모두 문서로 남겨야 하며, 일단 문서화한 것은 그 클래스가 쓰이는 한 반드시 지켜야 한다.  
  그렇지 않으면 그 내부 구현 방식을 믿고 활용하던 하위 클래스를 오동작하게 만들 수 있다.  
  다른 사람이 효율 좋은 하위 클래스를 만들 수 있도록 일부 메소드를 protected로 제공해야 할 수도 있다.  
  그러니 클래스를 확장해야 할 명확한 이유가 떠오르지 않으면 상속을 금지하는 편이 낫다.  
  상속을 금지하려면 클래스를 final로 선언하거나, 생성자 모두를 외부에서 접근할 수 없게 만들면 된다.

---
