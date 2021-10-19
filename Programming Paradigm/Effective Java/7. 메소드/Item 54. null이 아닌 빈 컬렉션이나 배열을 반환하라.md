# null이 아닌 빈 컬렉션이나 배열을 반환하라

- 아래는 주변에서 흔히 볼 수 있는 메소드다.

```java
private final List<Cheese> cheesesInStock = /* ... */;

/**
 * @return 매장 내의 모든 치즈 목록
 * 단, 재고가 하나도 없다면 null을 반환한다.
 */
public List<Cheese> getCheeses() {
    return cheesesInStock.isEmpty() ? null : new ArrayList<>(cheesesInStock);
}
```

- 사실 재고가 없다 해서 특별히 취급할 이유는 없다. 그럼에도 위 코드처럼 null을 반환한다면, 클라이언트는  
  이 null 상황을 처리하는 코드를 추가적으로 작성해야 한다.

```java
List<Cheese> cheeses = shop.getCheeses();
if (cheeses != null && cheeses.contains(Cheese.STILTON)) {
    //..
}
```

- 컬렉션이나 배열 같은 컨테이너(container)가 비었을 때 null을 반환하는 메소드를 사용할 때면  
  항상 이와 같은 방어 코드를 넣어줘야 한다. 클라이언트에서 방어 코드를 빼먹으면 오류가 발생할 수 있다.  
  실제로 객체가 0개일 가능성이 거의 없는 상황에서는 수년 뒤에야 오류가 발생하기도 한다. 한편, null을  
  반환하려면 반환하는 쪽에서도 이 상황을 특별히 취급해줘야 해서 코드가 더 복잡해진다.

- 때로는 빈 컨테이너를 할당하는 데도 비용이 드니 null을 반환하는 쪽이 낫다는 주장도 있다.  
  하지만 이는 두 가지 면에서 틀린 주장이다. 첫 번째, 성능 분석 결과 이 할당이 성능 저하의 주범이라고  
  확인되지 않는 한, 이 정도의 성능 차이는 신경 쓸 수준이 못된다. 둘째, 빈 컬렉션과 배열은 굳이 새로  
  할당하지 않고도 반환할 수 있다. 아래는 빈 컬렉션을 반환하는 전형적인 코드로, 대부분의 상황에서는  
  이렇게 하면 된다.

```java
public List<Cheese> getCheeses() {
    return new ArrayList<>(cheesesInStock);
}
```

- 가능성은 작지만, 사용 패턴에 따라 빈 컬렉션 할당이 성능을 눈에 띄게 떨어뜨릴 수도 있다.  
  다행이 해법은 간단하다. 매번 똑같은 빈 불변 컬렉션을 반환하는 것이다. 불변 객체는 자유롭게  
  공유해도 안전하다. 아래 코드에서 사용하는 `Collections.emptyList()`가 그러한 예시다.  
  집합이 필요하면 `Collections.emptySet()`을, 맵이 필요하면 `Collections.emptyMap()`을 사용하면  
  된다. 단, 이 역시 최적화에 해당하니 꼭 필요할 때만 사용하자. 최적화가 필요하다고 판단되면 수정 전과  
  후의 성능을 측정하여 실제로 성능이 개선되는지 꼭 확인하자.

```java
public List<Cheese> getCheeses() {
    return cheesesInStock.isEmpty() ? Collections.emptyList() : new ArrayList<>(cheesesInStock);
}
```

- 배열을 쓸 때도 마찬가지다. 절대 null을 반환하지 말고 길이가 0인 배열을 반환하자. 보통은 단순히 정확한  
  길이의 배열을 반환하기만 하면 된다. 그 길이가 0일 수도 있을 뿐이다. 아래 코드에서 `toArray()` 메소드에 건넨  
  길이 0짜리 배열은 우리가 원하는 반환 타입(이 경우에는 `Cheese[]`)을 알려주는 역할을 한다.

```java
public Cheese[] getCheeses() {
    return cheesesInStock.toArray(new Cheese[0]);
}
```

- 이 방식이 성능을 떨어뜨릴 것 같다면 길이 0짜리 배열을 미리 선언해두고 매번 그 배열을 반환하면 된다.  
  길이가 0인 배열은 모두 불변이기 때문이다.

```java
private static final Cheese[] EMPTY_CHEESE_ARRAY = new Cheese[0];

public Cheese[] getCheeses() {
    return cheesesInStock.toArray(EMPTY_CHEESE_ARRAY);
}
```

- 이 최적화 버전의 `getCheeses()`는 항상 EMPTY_CHEESE_ARRAY를 인수로 넘겨 `toArray()`를  
  호출한다. 따라서 cheesesInStock이 비었을 때면 언제나 EMPTY_CHEESE_ARRAY를 반환하게 된다.  
  단순히 성능을 개선할 목적이라면 `toArray()`에 넘길 배열을 미리 할당하는 것을 추천하지 않는다.  
  오히려 성능이 떨어진다는 연구 결과도 있다.

```java
return cheesesInStock.toArray(new Cheese[cheesesInStock.size()]);
```

> `<T> T[] List.toArray(T[] a)` 메소드는 주어진 배열 a가 충분히 크면 a 안에 원소를 담아  
> 반환하고, 그렇지 않으면 `T[]` 타입 배열을 새로 만들어 그 안에 원소를 담아 반환한다. 따라서 만약  
> 원소가 하나라도 있다면 `Cheese[]` 타입의 배열을 새로 생성해 반환하고, 원소가 0개면  
> EMPTY_CHEESE_ARRAY를 반환한다.

<hr/>

## 핵심 정리

- **null이 아닌 빈 배열이나 컬렉션을 반환하자.** null을 반환하는 API는 사용하기 어렵고 오류 처리  
  코드도 늘어난다. 그렇다고 성능이 좋은 것도 아니다.

<hr/>
