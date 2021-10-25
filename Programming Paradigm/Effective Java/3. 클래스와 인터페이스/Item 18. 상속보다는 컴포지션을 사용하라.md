# 상속보다는 컴포지션을 사용하라

- 상속은 코드를 재사용하는 강력한 수단이지만, 항상 최선은 아니다. 잘못 사용하면 오류를 내기 쉬운  
  소프트웨어를 만들게 된다. 상위 클래스와 하위 클래스를 모두 같은 프로그래머가 통제하는 패키지  
  안에서라면 상속도 안전한 벙밥이다. 하지만 **일반적인 구체 클래스를 패키지 경계를 넘어, 즉 다른**  
  **패키지의 구체 클래스를 상속하는 일은 위험하다.** 상기하자면, 여기서의 _상속_ 은  
  클래스가 다른 클래스를 확장하는 _구현 상속_ 을 의미한다. 이번 아이템에서 논하는 문제는  
  클래스가 인터페이스를 구현하거나, 인터페이스가 다른 인터페이스를 확장하는 상속과는 무관하다.

- **메소드 호출과 달리, 상속은 캡슐화를 깨뜨린다.** 다르게 말하면, 상위 클래스가 어떻게 구현되느냐에  
  따라 하위 클래스의 동작에 이상이 생길 수 있다. 상위 클래스는 릴리즈마다 내부 구현이 달라질 수 있으며,  
  그 여파로 코드 한 줄 건드리지 않은 하위 클래스가 오동작할 수 있다는 말이다. 이러한 이유로 상위 클래스  
  설계자가 확장을 충분히 고려지 않고 문서화도 제대로 해두지 않으면, 하위 클래스는 상위 클래스의 변화에  
  알맞게 수정되어야만 한다.

- 구체적인 예시를 살펴보자. `HashSet`을 사용하는 프로그램이 있다고 가정해보자. 성능을 높이려면  
  이 `HashSet`은 처음 생성된 이후 원소가 몇 개 더해졌는지 알 수 있어야 한다. 그래서 아래 코드와 같이  
  변형된 `HashSet`을 만들어 추가된 원소의 수를 저장하는 변수와 접근자 메소드를 추가했다.  
  그런 다음 `HashSet`에 원소를 추가하는 메소드인 `add()`와 `addAll()`을 정의했다.

```java
public class InstrumentedHashSet<E> extends HashSet<E> {

    // 추가된 원소의 수
    private int addCount = 0;

    public InstrumentedHashSet() { }

    public InstrumentedHashSet(int initialCapacity, float loadFactor) {
	super(initialCapacity, loadFactor);
    }

    @Override public boolean add(E e) {
	addCount++;
	return super.add(e);
    }

    @Override public boolean addAll(Collection<? extends E> c) {
	addCount += c.size();
	return super.addAll(c);
    }

    public int getAddCount() {
	return addCount;
    }
}
```

- 위 클래스는 잘 구현된 것처럼 보이지만, 제대로 작동하지 않는다.  
  위 클래스의 인스턴스에 `addAll()` 메소드로 3개의 원소를 더했다고 해보자.  
  아래 코드는 Java9부터 지원하는 `List.of()`로 리스트를 생성했다.

```java
IntstrumentedHashSet<String> s = new InstrumentedHashSet<>();
s.addAll(List.of("a", "b", "c"));
```

- 이제 `s.getAddCount()`를 호출하면 3을 반환하리라 기대했지만, 실제로는 6을 반환한다.  
  어디서 잘못된걸까? 그 원인은 `HashSet.addAll()`이 `HashSet.add()`를 사용해 구현된 데 있다.  
  이런 내부 구현 방식은 당연히 `HashSet`의 문서에는 쓰여 있지 않다. `InstrumentedHashSet#addAll()`은  
  addCount에 3을 더한 후, `HashSet#addAll()` 구현을 호출한다. `HashSet#addAll()`은 각 원소를  
  `add()` 메소드를 호출해 추가하는데, 이때 불리는 `add()`는 `InstrumentedHashSet`에서  
  재정의한 `add()` 메소드이다. 따라서 addCount에 값이 중복해서 더해져, 최종값이 6으로 늘어난 것이다.  
  즉, `addAll()`로 추가한 원소 하나당 addCount의 값이 2씩 늘어났다.

- 이 경우, `InstrumentedHashSet`, 즉 하위 클래스에서 `addAll()`을 재정의하지 않으면  
  문제를 고칠 수 있다. 하지만 당장은 제대로 동작할지 모르나, `HashSet`의 `addAll()`이  
  `add()` 메소드를 이용해 구현했음을 _가정_ 한 해법이라는 한계를 지닌다. 이처럼 자신의  
  다른 부분을 사용하는 _자기 사용(self-use)_ 여부는 해당 클래스의 내부 구현 방식에 해당하며,  
  Java 플랫폼의 전반적인 정책인지, 그래서 다음 릴리즈에서도 유지될지는 알 수 없다.  
  따라서 이런 가정에 기댄 `InstrumentedHashSet`도 깨지기 쉽다.

- `addAll()` 메소드를 다른 방식으로 재정의할 수도 있다. 예를 들어 주어진 컬렉션을 순회하며  
  원소 하나당 `add()`를 호출하는 것이다. 이 방식은 `HashSet#addAll()`을 더 이상 호출하지 않으니  
  `addAll()`이 `add()`를 사용하는지와 관계없이 결과가 옳다는 점에서 좀 더 나은 해법이다.  
  하지만 여전히 문제는 남는다. 상위 클래스의 메소드 동작을 다시 구현하는 이 방식은 어렵고,  
  시간도 더 들고, 자칫 오류를 내거나 성능을 떨어뜨릴 수도 있다. 또한 하위 클래스에서는 접근할 수 없는  
  private 필드를 써야 하는 상황이라면 이 방식으로는 구현 자체가 불가능하다.

- 하위 클래스가 깨지기 쉬운 이유는 더 있다. 다음 릴리즈에서 상위 클래스에 새로운 메소드를 추가한다면  
  어떨까? 보안 때문에 컬렉션에 추가된 모든 원소가 특정 조건을 만족해야만 하는 프로그램을 생각해보자.  
  그 컬렉션을 상속하여 원소를 추가하는 모든 메소드를 재정의해 필요한 조건을 먼저 검사하게끔 하면  
  될 것 같다. 하지만 이 방식이 통하는 것은 상위 클래스에 또다른 원소 추가 메소드가 만들어지기  
  전까지다. 다음 릴리즈에서 우려한 일이 생기면, 하위 클래스에서 재정의하지 못한 그 새로운 메소드를 사용해  
  _'허용되지 않은'_ 원소를 추가할 수 있게 된다. 실제로도 컬렉션 프레임워크 이전부터 존재하던  
  `HashTable`과 `Vector`를 컬렉션 프레임워크에 추가시키자, 이와 관련한 보안 구멍들을 수정해야하는  
  상황이 생겼다.

- 이상의 두 문제는 모두 **메소드 재정의가 원인**이었다. 따라서 클래스를 확장하더라도 메소드를 재정의하는 대신  
  새로운 메소드를 추가하면 괜찮다고 생각할 수 있다. 이 방식이 훨씬 안전한 것은 맞지만, 위험이 전혀  
  없는 것은 아니다. 다음 릴리즈에서 상위 클래스에 새로운 메소드가 추가되었는데, 운이 없게도 하필 직접  
  하위클래스에 정의한 메소드와 시그니처가 같고, 반환 타입은 다르다면 컴파일 조차 실패할 것이다.  
  반환 타입마저 같다면, 결국 재정의한게 되어버리니 앞서 본 문제와 똑같은 상황에 부딪힌다.  
  문제는 여기서 그치지 않는다. 개발자가 이 메소드를 작성할 때는 상위 클래스의 메소드는 존재하지도 않았으니,  
  개발자가 만든 메소드는 상위 클래스의 메소드가 요구하는 규약을 만족하지 못할 가능성이 크다.

- 다행이 이 문제를 모두 피해가는 묘안이 있다. **기존 클래스를 확장하는 대신, 새로운 클래스를 만들고**  
  **private 필드로 기존 클래스의 인스턴스를 참조하도록 하자.** 기존 클래스가 새로운 클래스의 구성 요소로  
  쓰인다는 뜻에서, 이러한 설계를 **컴포지션(Composition, 구성)** 이라 한다. 새 클래스의 인스턴스  
  메소드들은 private 필드로 참조하는 기존 클래스의 대응하는 메소드를 호출해 그 결과를 반환한다.  
  이 방식을 **전달(forwarding)** 이라 하며, 새 클래스의 메소드들을 **전달 메소드(forwarding method)** 라  
  부른다. 그 결과 새로운 클래스는 기존 클래스의 내부 구현 방식의 영향에서 벗어나며, 심지어 기존 클래스에  
  새로운 메소드가 추가되더라도 전혀 영향받지 않는다. 구체적인 예시를 위해 위에서 본  
  `InstrumentedHashSet`을 상속이 아닌 컴포지션과 전달 방식으로 구현해보자.  
  이번 구현은 두 개의 클래스로 나뉘는데, 하나는 **집합 클래스 자신**이고, 다른 하나는 **전달 메소드만으로 이뤄진**  
  **재사용 가능한 전달 클래스**이다.

```java
// 집합 클래스 자신
public class InstrumentedSet<E> extends ForwardingSet<E> {
    private int addCount = 0;

    public InstrumentedHashSet(Set<E> s) { super(s); }

    @Override public boolean add(E e) {
	addCount++;
	return super.add(e);
    }

    @Override public boolean addAll(Collection<? extends E> c) {
	addCount += c.size();
	return super.addAll(c);
    }

    public int getAddCount() { return addCount; }
}

// 전달 메소드만으로 이뤄진 재사용 가능한 전달 클래스
public class ForwardingSet<E> implements Set<E> {
    private final Set<E> s;
    public ForwardingSet(Set<E> s) { this.s = s; }

    public void clear() { s.clear(); }
    public boolean contains(Object o) { return s.contains(o); }
    public boolean isEmpty() { return s.isEmpty(); }
    public int size() { return s.size(); }
    public Iterator<E> iterator() { return s.iterator(); }
    public boolean add(E e) { return s.add(e); }
    public boolean remove(Object o) { return s.remove(o); }
    public boolean containsAll(Collection<?> c) { return s.containsAll(c); }
    public boolean removeAll(Collection<?> c) { return s.removeAll(c); }
    public boolean retainAll(Collection<?> c) { return s.retainAll(c); }
    public Object[] toArray() { return s.toArray(); }
    public <T> T[] toArray(T[] a) { return s.toArray(a); }

    @Override public boolean equals(Object o) { return s.equals(o); }
    @Override public int hashCode() { return s.hashCode(); }
    @Override public String toString() { return s.toString(); }
}
```

- `InstrumentedSet`은 `HashSet`의 모든 기능을 정의한 `Set` 인터페이스를 활용해 설계되어 견고하고  
  아주 유연하다. 구체적으로는 `Set` 인터페이스를 구현했고, `Set`의 인스턴스를 인수로 받는 생성자를 하나  
  제공한다. 임의의 `Set`에 계측 기능을 덧씌워 새로운 `Set`으로 만드는 것이 이 클래스의 핵심이다.  
  상속 방식은 구체 클래스 각각을 따로 확장해야 하며, 지원하고 싶은 상위 클래스의 생성자 각각에 대응하는  
  생성자를 별도로 정의해줘야 한다. 하지만 바로 위의 컴포지션 방식은 한 번만 구현해두면 어떠한  
  `Set` 구현체라도 계측할 수 있으며, 기존 생성자들과도 함께 사용할 수 있다.

```java
// client code
Set<Instant> times = new InstrumentedSet<>(new TreeSet<>(cmp));
Set<E> s = new InstrumentedSet<>(new HashSet<>(INITIAL_CAPACITY));
```

- `InstrumentedSet`을 이용하면 대상 `Set` 인스턴스를 특정 조건하에서만 임시로 계측할 수 있다.

```java
static void walk(Set<Dog> dogs) {
    InstrumentedSet<Dog> iDogs = new InstrumentedSet<>(dogs);
    // dogs 대신 iDogs를 사용하여 계측 기능 사용
}
```

- 다른 `Set` 인스턴스를 감싸고 있다는 뜻에서 `InstrumentedSet` 같은 클래스를 **wrapper 클래스**라  
  하며, 다른 `Set`에 계측 기능을 덧씌운다는 뜻에서 **데코레이터 패턴**이라고 한다.  
  컴포지션과 전달의 조합은 넓은 의미로 **위임(Delegation)** 이라고 부른다.  
  단, 엄밀히 따지면 wrapper 객체가 내부 객체에 자기 자신의 참조를 넘기는 경우만 위임에 해당한다.

- Wrapper 클래스는 단점이 거의 없다. 한 가지, Wrapper 클래스가 callback 프레임워크와는  
  어울리지 않는다는 점만 주의하면 된다. Callback 프레임워크에서는 자기 자신의 참조를 다른 객체에게  
  넘겨서 다음 호출(callback)할 때 사용하도록 한다. 내부 객체는 자신을 감싸고 있는 wrapper의  
  존재를 모르니, 대신 자신(this)의 참조를 넘기고, callback 때는 wrapper가 아닌 내부 객체를  
  호출하게 된다. 이를 SELF 문제라고 한다. 전달 메소드가 성능에 주는 영향이나 wrapper 객체가  
  메모리 사용량에 주는 영향을 걱정하는 사람도 있지만, 실전에서는 둘 다 별다른 영향이 없다고 밝혀졌다.  
  전달 메소드들을 작성하는게 지루하겠지만, 재사용할 수 있는 전달 클래스를 인터페이스당 하나씩만  
  만들어두면 원하는 기능을 덧씌우는 wrapper 클래스들은 아주 쉽게 구현할 수 있다. 좋은 예시로, Guava는  
  모든 컬렉션 인터페이스용 전달 메소드를 전부 구현해두었다.

- **상속은 반드시 하위 클래스가 상위 클래스의 '진짜' 하위 타입인 상황에서만 쓰여야 한다.**  
  다르게 말하면, 클래스 B가 클래스 A와 _is-a_ 관계일 때만 클래스 A를 상속해야 한다.  
  클래스 A를 상속하는 클래스 B를 작성하려 한다면 _'B가 정말 A인가?'_ 를 자문해보자.  
  그렇다고 확신할 수 없다면, B는 A를 상속해서는 안된다. 대답이 _아니다_ 라면 A를  
  private 인스턴스로 두고, A와는 다른 API를 제공해야 하는 상황이 대다수다. 즉, A는 B의  
  필수 구성요소가 아니라 구현하는 방법 중 하나일 뿐이다.

- Java 플랫폼 라이브러리에도 이 원칙을 명백히 위반한 클래스들을 찾아볼 수 있다. 예를 들어, `Stack`은  
  `Vector`가 아니므로 `Stack`은 `Vector`를 확장해서는 안됐다. 마찬가지로, 속성 목록도  
  `HashTable`이 아니므로 `Properties`도 `HashTable`을 확장해서는 안됐다. 두 사례 모두 컴포지션을  
  사용했다면 더 좋았을 것이다.

- 컴포지션을 써야 할 상황에서 상속을 사용하는 것은 내부 구현을 불필요하게 노출하는 꼴이다.  
  그 결과 API가 내부 구현에 묶이고, 그 클래스의 성능도 영원히 제한된다. 더 심각한 문제는  
  클라이언트가 노출된 내부에 직접 접근할 수 있다는 점이다. 다른 문제는 접어두더라도, 사용자를  
  혼란스럽게 할 수 있다. 예컨데, `Properties`의 인스턴스인 p가 있을 때, `p.getProperty(key)`와  
  `p.get(key)`는 결과가 다를 수 있다. 전자가 `Properties`의 기본 동작인 데 반해, 후자는  
  `Properties`의 부모 클래스인 `HashTable`로부터 물려받은 메소드이기 때문이다. 가장 심각한 문제는  
  **클라이언트에서 상위 클래스를 직접 수정하여 불변식을 해칠 수 있다**는 사실이다. 예컨데 `Properties`는  
  key와 value로 문자열만 허용하도록 설계하려 했으나, 상위 클래스인 `HashTable`의 메소드를  
  호출하면 이 불변식을 깨버릴 수 있다. 불변식이 한번 깨지면 `load()`나 `store()` 같은  
  `Properties`의 다른 API는 더 이상 사용할 수 없게 된다. 이 문제가 밝혀졌을 때는 이미  
  수많은 사용자가 문자열 이외의 타입을 `Properties`의 key, value에 사용하고 있었다.  
  문제를 바로잡기에는 너무 늦어버린 것이다.

- 컴포지션 대신 상속을 사용하기로 결정하기 전에 마지막으로 자문해야 할 질문을 보자.  
  _확장하려는 클래스의 API에 아무런 결함이 없는가?_  
  _결함이 있다면, 이 결함이 다른 클래스의 API까지 전파돼도 괜찮은가?_  
  컴포지션으로는 이런 결함을 숨기는 API를 설계할 수 있지만, 상속은 상위 클래스의 API를  
  _그 결함까지도_ 그대로 승계한다.

<hr/>

## 핵심 정리

- 상속은 강력하지만 캡슐화를 해친다는 문제가 있다. 상속은 상위 클래스와 하위 클래스가 순수한  
  _is-a_ 관계일 때만 써야 한다. is-a 관계일 때도 안심할 수많은 없는게, 하위 클래스의  
  패키지가 상위 클래스와 다르고, 상위 클래스가 확장을 고려해 설계되지 않았다면 여전히 문제가  
  될 수 있다. 상속의 취약점을 피하려면 상속 대신 컴포지션과 전달을 사용하자. 특히 wrapper  
  클래스로 구현할 적당한 인터페이스가 있다면 더욱 그렇다. Wrapper 클래스는 하위 클래스보다  
  더 견고하고 강력하다.

<hr/>
