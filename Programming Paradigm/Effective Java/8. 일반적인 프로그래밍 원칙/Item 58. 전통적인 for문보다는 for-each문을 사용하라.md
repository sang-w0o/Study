# 전통적인 for문보다는 for-each문을 사용하라

- [Item 45](https://github.com/sang-w0o/Study/blob/master/Programming%20Paradigm/Effective%20Java/6.%20%EB%9E%8C%EB%8B%A4%EC%99%80%20%EC%8A%A4%ED%8A%B8%EB%A6%BC/Item%2045.%20%EC%8A%A4%ED%8A%B8%EB%A6%BC%EC%9D%80%20%EC%A3%BC%EC%9D%98%ED%95%B4%EC%84%9C%20%EC%82%AC%EC%9A%A9%ED%95%98%EB%9D%BC.md)에서 봤듯이 `Stream`이 제격인 작업이 있고, 반복이 제격인 작업이 있다.  
  아래는 전통적인 for문으로 컬렉션을 순회하는 코드다.

```java
for(Iterator<Element> i = c.iterator(); i.hasNext(); ) {
  Element e = i.next();
  // compute with e
}
```

- 그리고 아래는 전통적인 for문으로 배열을 순회하는 코드다.

```java
for(int i = 0; i < a.length; i++) {
  // compute with a[i]
}
```

- 이 관용구들은 while문보다는 낫지만 가장 좋은 방법은 아니다. 반복자와 인덱스 변수는 모두  
  코드를 지저분하게 할 뿐 우리에게 진짜 필요한건 원소들뿐이다. 더군다나 이처럼 쓰이는 요소 종류가  
  늘어나면 오류가 생길 가능성이 높아진다. 1회 반복에서 반복자는 세 번 등장하며, 인덱스는 네 번이나  
  등장해 변수를 잘못 사용할 틈새가 높아진다. 혹시라도 잘못된 변수를 사용했을 때 컴파일러가 잡아주리라는  
  보장도 없다. 마지막으로 컬렉션이냐 배열이냐에 따라 코드 형태가 상당히 달라지므로 주의해야 한다.

- 이상의 문제는 for-each문을 사용하면 모두 해결된다. 참고로 for-each 문의 정식 이름은  
  _향상된 for문(enhanced for statement)_ 이다. 반복자와 인덱스 변수를 사용하지 않으니 코드가  
  깔끔해지고 오류가 날 일도 없다. 하나의 관용구로 컬렉션과 배열을 모두 처리할 수 있어서 어떤 컨테이너를  
  다루는지는 신경쓰지 않아도 된다.

```java
for(Element e : elements) {
  // compute with e
}
```

- 반복 대상이 컬렉션이든 배열이든, for-each문을 사용해도 속도는 그대로다. for-each 문이 만들어내는 코드는  
  사실상 사람이 직접 최적화한 것과 같기 때문이다.

- 컬렉션을 중첩해서 순회해야 한다면 for-each 문의 이점이 더욱 커진다. 아래 코드에서 버그를 찾아보자.  
  반복문을 중첩할 때 흔히 저지르는 실수가 담겨져 있다.

```java
enum Suit { CLUB, DIAMOND, HEART, SPAD }
enum Rank { ACE, DEUCE, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE, TEN, JACK, QUEEN, KING }

// ..

static Collection<Suit> suits = Arrays.asList(Suit.values());
static Collection<Rank> ranks = Arrays.asList(Rank.values());

List<Card> deck = new ArrayList<>();
for(Iterator<Suit> i = suits.iterator(); i.hasNext();)
  for(Iterator<Rank> j = ranks.iterator(); j.hasNext();)
    deck.add(new Card(i.next(), j.next()));
```

- 위 코드의 문제는 suits의 반복자에서 `next()`가 너무 많이 불린다는 것이다. 마지막 줄의 `i.next()`를  
  주목하자. 이 `next()`는 suit 하나 당 한 번씩 불려야 하는데, 안쪽 반복문에서 호출되는 바람에 rank 하나당  
  한 번씩 불리고 있다. 그래서 suit가 바닥나면 반복문에서 `NoSuchElementException`을 던진다.

- 정말 운이 나빠서 바깥 컬렉션의 크기가 안쪽 컬렉션 크기의 배수라면 이 반복문은 예외조차 던지지 않고 종료한다.  
  물론 우리가 원하는대로 수행되지 않는다.

- for-each문을 중첩하는 것으로 이 문제는 간단히 해결된다. 코드 또한 놀라울만큼 간결해진다.

```java
for(Suit suit : suits)
  for(Rank rank : ranks)
    deck.add(new Card(suit, rank));
```

- 하지만 안타깝게도 for-each문을 사용할 수 없는 상황이 세 가지 존재한다.

  - 파괴적인 필터링(destructive filtering): 컬렉션을 순회하면서 선택된 원소를 제거해야 한다면  
    반복자의 `remove()`를 호출해야 한다. Java8부터는 `Collection#removeIf()`를 사용해  
    컬렉션을 명시적으로 순회하는 일은 피할 수 있다.

  - 변형(transforming): 리스트나 배열을 순회하면서 그 원소의 값 일부 혹은 전체를 교체해야 한다면  
    리스트의 반복자나 배열의 인덱스를 사용해야 한다.

  - 병렬 반복(parallel iteration): 여러 컬렉션을 병렬로 순회해야 한다면 각각의 반복자와 인덱스  
    변수를 사용해 엄격하고 명시적으로 제어해야 한다.

- 위 세 가지 상황 중 하나에 속할 때는 일반적인 for문을 사용하되, 이번 아이템에서 본 문제들을 경계하자.

- for-each문은 컬렉션과 배열은 물론 `Iterable` 인터페이스를 구현한 객체라면 무엇이든 순회할 수 있다.  
  `Iterable` 인터페이스는 아래와 같이 메소드가 단 하나뿐이다.

```java
public interface Iterable<E> {
  Iterator<E> iterator();
}
```

- `Iterable`을 처음부터 직접 구현하기는 까다롭지만, 원소들의 묶음을 표현하는 타입을 작성해야 한다면  
  `Iterable`을 구현하는 쪽으로 고민하자. 해당 타입에서 `Collection` 인터페이스는 구현하지 않기로  
  했더라도 말이다. `Iterable`을 구현해두면 그 타입을 사용하는 클라이언트가 for-each문을 매우  
  편리하게 사용할 수 있다.

---

## 핵심 정리

- 전통적인 for문과 비교했을 때 for-each문은 명료하고, 유연하고, 버그를 예방해준다.  
  성능 저하도 없다. 가능한 모든 곳에서 for문이 아닌 for-each문을 사용하자.

---
