# 비검사 경고를 제거하라

- 제네릭을 사용하기 시작하면 수많은 컴파일러 경고를 보게될 것이다. 비검사 형변환 경고,  
  비검사 메소드 호출 경고, 비검사 매개변수화 가변인수 타입 경고, 비검사 변환 경고 등이다.  
  제네릭에 익숙해질 수록 마주치는 경고 수는 줄겠지만, 새로 작성한 코드가 한 번에 깨끗하게  
  컴파일되리라 기대하지는 말자.

- 대부분의 비검사 코드는 쉽게 제거할 수 있다.  
  코드를 아래처럼 잘못 작성했다 해보자.

```java
Set<Lark> exaltation = new HashSet();
```

- 그러면 컴파일러는 무엇이 잘못됐는지 친절히 설명해줄 것이다.  
  javac 명령줄 인수에 `-Xlink:uncheck` 옵션을 추가해야 한다.

```
unchecked conversion
Set<Lark> = new HashSet();
            ^
required: Set<Lark>
found: HashSet
```

- 컴파일러가 알려준대로 수정하면 경고가 사라진다. 사실 컴파일러가 알려준 타입 매개변수를  
  명시하지 않고, Java7 부터 지원하는 다이아몬드 연산자 `<>`만으로 해결할 수 있다.  
  그럼 컴파일러가 올바른 실제 타입 매개변수인 `Lark`를 추론해준다.

```java
Set<Lark> exaltation = new HashSet<>();
```

- 제거하기 훨씬 어려운 경고도 있다. 이번 장은 그러한 경고를 내는 예제들로 가득 차있다.  
  곧바로 해결되지 않는 경고가 나타나도 포기하지 말자!  
  **할 수 있는 한 모든 비검사 경고를 제거하라.** 모두 제거한다면 그 코드는 타입 안정성이 보장된다.  
  즉, 런타임에 `ClassCastException`이 발생할 일이 없고, 개발자가 의도한 대로 잘 동작하리라  
  확신할 수 있다.

- **경고를 제거할 수는 없지만 타입 안전하다고 확신할 수 있다면 `@SuppressWarnings("unchecked")`**  
  **어노테이션을 달아 경고를 숨기자.** 단, 타입 안전함을 검증하지 않은 채 경고를 숨긴다면 스스로에게  
  잘못된 보안 인식을 심는 꼴이다. 그 코드는 경고 없이 컴파일되겠지만, `ClassCastException`이 발생할 수 있다.  
  한편, 안전하다고 검증된 비검사 경고를 숨기지 않고 그대로 두면, 진짜 문제를 알리는 새로운 경고가 나와도  
  눈치채지 못할 수 있다. 제거하지 않은 수많은 거짓 경고 속에 새로운 경고가 파묻힐 것이기 때문이다.

- `@SuppressWarnings` 어노테이션은 개별 지역변수 선언부터 클래스 전체까지의 선언에도  
  달 수 있다. 하지만 **`@SuppressWarnings` 어노테이션은 가능한 한 좁은 범위에 적용하자.**  
  보통은 변수 선언, 아주 짧은 메소드, 혹은 생성자가 될 것이다. 자칫 심각한 경고를  
  놓칠 수도 있으니 절대로 클래스 전체에 적용해서는 안된다.

- 한 줄이 넘는 메소드나 생성자에 달린 `@SuppressWarnings` 어노테이션을 발견하면,  
  지역변수 선언 쪽으로 옮기자. 이를 위해 지역변수를 새로 선언하는 수고를 해야할 수도 있지만,  
  그만한 값어치가 있을 것이다. `ArrayList`에서 가져온 `toArray()` 메소드를 예시로 보자.

```java
public <T> T[] toArray(T[] a) {
    if (a.length < size)
        return (T[]) Arrays.copyOf(elementData, size, a.getClass());
    System.arraycopy(elementData, 0, a, 0, size);
    if (a.length > size)
        a[size] = null;
    return a;
}
```

- `ArrayList`를 컴파일하면 이 메소드에서 아래 경고가 나타난다.

```
warning: [unchecked] unchecked cast
return (T[]) Arrays.copyOf(elementData, size, a.getClass());
                           ^
required: T[]
found: Object[]
```

- 애노테이션은 선언에만 달 수 있기 때문에 return문에는 `@SuppressWarnings`를 다는게 불가하다.  
  그렇다면 이제 메소드 전체에 달고 싶겠지만, 범위가 필요 이상으로 넓어지니 자제하자.  
  그대신 반환값을 담을 지역변수를 하나 선언하고, 그 변수에 어노테이션을 달아주자.

```java
public <T> T[] toArray(T[] a) {
    if (a.length < size) {
	// 생성한 배열과 매개변수로 받은 배열의 타입이 모두 T[]로 같으므로
	// 올바른 형변환이다.
	@SuppressWarnings("unchecked")
	T[] result = (T[]) Arrays.copyOf(elementData, size, a.getClass());
	return result;
    }
    System.arraycopy(elementData, 0, a, 0, size);
    if (a.length > size)
        a[size] = null;
    return a;
}
```

- 위 코드는 깔끔하게 컴파일되고, 비검사 경고를 숨기는 범위도 최소로 줄였다.  
  **`@SuppressWarnings("unchecked")`를 사용할 때면 그 경고를 무시해도 안전한 이유를 항상**  
  **주석으로 남겨야 한다.** 다른 사람이 그 코드를 이해하는 데 도움이 되며, 더 중요하게는 다른  
  사람이 그 코드를 잘못 수정하여 타입 안정성을 잃는 상황을 줄여준다.

<hr/>

## 핵심 정리

- 비검사 경고는 중요하니 무시하지 말자. 모든 비검사 경고는 런타임에 `ClassCastException`을  
  일으킬 수 있는 잠재적 가능성을 뜻하니 최선을 다해 제거하자. 경고를 없앨 방법을 찾지 못하겠다면  
  그 코드가 타입 안전함을 증명하고 가능한 한 범위를 좁혀 `@SuppressWarnings("unchecked")`를 사용하여  
  경고를 숨기자. 그런 다음 경고를 숨기기로 한 근거를 주석으로 남기자.

<hr/>

- 희안하지만 내 JDK 에서는 메소드 레벨에 `@SuppressWarnings("unchecked")`가 적용되어 있다...

```java
@SuppressWarnings("unchecked")
public <T> T[] toArray(T[] a) {
if (a.length < size)
        // Make a new array of a's runtime type, but my contents:
        return (T[]) Arrays.copyOf(elementData, size, a.getClass());
System.arraycopy(elementData, 0, a, 0, size);
if (a.length > size)
        a[size] = null;
return a;
}
```
