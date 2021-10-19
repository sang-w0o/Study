# 다중정의는 신중히 사용하라

- 아래는 컬렉션을 집합, 리스트, 그 외로 구분하고자 만든 프로그램이다.

```java
public class CollectionClassifier {
    public static String classify(Set<?> s) {
	return "Set";
    }

    public static String classify(List<?> l) {
	return "List";
    }

    public static String classify(Collection<?> c) {
	return "Else";
    }

    public static void main(String[] args) {
	Collection<?>[] collections = {
	    new HashSet<String>(),
	    new ArrayList<BigInteger>(),
	    new HashMap<String, String>().values()
	};

	for(Collection<?> c : collections) {
	    System.out.println(classify(c));
	}
    }
}
```

- 위 프로그램의 실행 결과가 `Set List Else`일 것 같지만, 실제로 수행해보면 `Else Else Else`가  
  나온다. 이유가 뭘까? 다중정의(overloading)된 세 `classify()` 중 **어느 메소드를 호출할지가**  
  **컴파일타임에 정해지기 때문이다.** 컴파일타임에는 for문 안의 c는 항상 `Collection<?>` 타입이다.  
  런타임에는 타입이 매번 달라지지만, 호출할 메소드를 선택하는 데는 영향을 주지 못한다. 따라서 컴파일타임의  
  매개변수 타입을 기준으로 항상 세 번째 메소드인 `classify(Collection<?>)`가 호출되는 것이다.

- 이처럼 직관과 어긋나는 이유는 **재정의한 메소드는 동적으로 선택되고, 다중정의한 메소드는 정적으로 선택되기 때문이다.**  
  메소드를 재정의했다면 해당 객체의 런타임 타입이 어떤 메소드를 호출할지의 기준이 된다. 메소드 재정의란 상위  
  클래스가 정의한 것과 똑같은 시그니처의 메소드를 하위 클래스에서 다시 정의한 것을 말한다. 메소드를  
  재정의한 다음 _하위 클래스의 인스턴스_ 에서 그 메소드를 호출하면 재정의한 메소드가 실행된다.  
  컴파일타임에 그 인스턴스 타입이 무엇이었냐는 상관없다. 아래 코드는 이러한 상황을 구체적으로 보여준다.

```java
class Wine {
    String name() { return "wine"; }
}

class SparklingWine extends Wine {
    @Override String name() { return "sparkling wine"; }
}

class Champagne extends SparklingWine {
    @Override String name() { return "champagne"; }
}

public class App {
   public static void main(String[] args) {
	List<Wine> wineList = List.of(
	    new Wine(), new SparklingWine(), new Champagne()
	);

	for(Wine wine : wineList) {
	    System.out.println(wine.name());
	}
   }
}
```

- `Wine` 클래스에 정의된 `name()` 메소드는 하위 클래스인 `SparklingWine`과 `Champagne`에서  
  재정의된다. 예상한 것처럼 이 프로그램은 `wine sparkling wine champagne`을 출력한다.  
  for 문에서의 컴파일타임 타입이 모두 `Wine`인 것과 무관하게 항상 _가장 하위에서 정의한_ 재정의 메소드가  
  실행되는 것이다.

- 한편, 다중정의된 메소드 사이에서는 객체의 런타임 타입은 전혀 중요치 않다. 선택은 컴파일타임에,  
  오직 매개변수의 컴파일타임 타입에 의해 이뤄진다.

- 위의 코드 중 `CollectionClassifier` 예시에서 프로그램의 원래 의도는 매개변수의 런타임 타입에 기초해  
  다중정의 메소드로 자동 분배되는 것이었다. `Wine`의 `name()`과 똑같이 말이다. 하지만 다중정의는  
  이렇게 동작하지 않는다. 이 문제는 정적 메소드를 사용해도 좋다면 `CollectionClassifier`의 모든  
  `classify()`를 하나로 합친 후 instanceof로 명시적으로 검사하면 말끔히 해결된다.

```java
public class CollectionClassifier {
    //..

    public static String classify(Collection<?> c) {
	return c instanceof Set ? "Set" : C instanceof List ? "List" : "Else";
    }
}
```

- 프로그래머에게는 재정의가 정상적인 동작 방식이고, 다중정의가 예외적인 동작으로 보일 것이다.  
  즉 재정의한 메소드는 프로그래머가 기대한 대로 동작하지만, `CollectionClassifier` 처럼 다중정의한  
  메소드는 이러한 기대를 가볍게 무시한다. 헷갈릴 수 있는 코드는 작성하지 않는 게 좋다. 특히나 공개  
  API라면 더욱 신경써야 한다. API 사용자가 매개변수를 넘기면서 어떤 다중정의 메소드가 호출될지를  
  모른다면 프로그램이 오동작하기 쉽다. 런타임에 이상하게 행동할 것이며 API 사용자들은 문제를 진단하느라  
  긴 시간을 허비할 것이다. 그러니 **다중정의가 혼동을 일으키는 상황을 피해야 한다.**

- 정확히 어떻게 사용했을 때 다중정의가 혼란을 주느냐에 대해서는 논란의 여지가 있다.  
  **안전하고 보수적으로 가려면 매개변수 수가 같은 다중정의는 만들지 말자.** 가변인수(varargs)를 사용하는  
  메소드라면 다중정의를 아예 하지 말아야 한다. 이 규칙만 잘 따르면 어떤 다중정의 메소드가 호출될지  
  헷갈릴 일은 전혀 없을 것이다. 또한 특별히 따르기 어려운 규칙도 아니다.  
  **다중정의하는 대신 메소드명을 다르게 지어주는 방법도 항상 열려있다.**

- 이번에는 `ObjectOutputStream` 클래스를 살펴보자. 이 클래스의 `write()` 메소드는 모든  
  기본타입과 일부 참조 타입용 변형을 가지고 있다. 그런데 다중정의가 아닌, 모든 메소드에 다른 이름을  
  지어주는 길을 택했다. `writeBoolean(boolean)`, `writeInt(int)`, `writeLong(long)`과  
  같은 식이다. 이 방식이 다중정의보다 나은 또 다른 점은 `read()`메소드명과 짝을 맞추기도 좋다는 것이다.  
  예를 들어 `readBoolean()`, `readInt()`, `readLong()`과 같은 식이다. 실제로도  
  `ObjectOutputStream`의 `read()`는 이렇게 되어 있다.

- 한편, 생성자는 이름을 다르게 지을 수 없으니 두 번째 생성자부터는 무조건 다중정의가 된다.  
  하지만 정적 팩토리라는 대안을 활용할 수 있는 경우가 많다. 또한 생성자는 재정의할 수 없으니  
  다중정의와 재정의가 혼용될 걱정은 넣어둬도 된다. 그래도 여러 생성자가 같은 수의 매개변수를 받아야 하는  
  경우를 완전히 피해갈 수는 없을 테니, 그럴 때를 대비해 안전책을 살펴보자.

- 매개변수 수가 같은 다중정의 메소드가 많더라도 그중 어느것이 주어진 매개변수 중 하나 이상이  
  _"근본적으로 다르다(radically different)"_ 면 헷갈릴 일이 없다. 근본적으로 다르다는 것은  
  두 타입의 null이 아닌 값을 서로 어느쪽으로든 형변환할 수 없다는 뜻이다. 이 조건만 충족하면 어느  
  다중정의 메소드를 호출할지가 매개변수들의 런타임 타입만으로 결정된다. 따라서 컴파일타임 타입에는  
  영향을 받지 않게 되고, 혼란을 주는 주된 원인이 사라진다. 예를 들어 `ArrayList`에는 int를 받는  
  생성자와 `Collection`을 받는 생성자가 있는데, 어떤 상황에서든 두 생성자 중 어느 것이 호출될지  
  헷갈릴 일은 없을 것이다.

- Java4 까지는 모든 기본 타입이 모든 참조 타입과 근본적으로 달랐지만, Java5에서 Auto Boxing이  
  도입되면서 평화롭던 시대가 막을 내렸다. 아래 프로그램을 보자.

```java
public class SetList {
    public static void main(String[] args) {
	Set<Integer> set = new TreeSet<>();
	List<Integer> list = new ArrayList<>();

	for(int i = -3; i < 3; i++) {
	    set.add(i);
	    list.add(i);
	}

	for(int i = 0; i < 3; i++) {
	    set.remove(i);
	    list.remove(i);
	}

	System.out.println(set + " " + list);
    }
}
```

- 이 프로그램은 -3부터 2까지의 정수를 정렬된 집합과 리스트에 각각 추가한 다음, 양쪽에 똑같이  
  `remove()`를 세 번 호출한다. 그러면 이 프로그램은 음이 아닌 값, 즉 0, 1, 2를 제거한 후에  
  `[-3, -2, -1], [-3, -2, -1]`을 출력하리라 예상할 것이다. 하지만 실제로 출력되는 내용은  
  `[-3, -2, -1], [-2, 0, 2]` 이다. 이렇게 되는 이유는 `set.remove(i)`의 시그니처는  
  `remove(Object)`이다. 다른 다중정의된 메소드가 없으니 기대한 대로 동작해 집합에서 0 이상의 수를  
  제거한다. 반면 `list.remove(i)`는 다중정의된 `remove(int index)`를 선택한다.

- 이 문제는 `list.remove()`의 인수를 `Integer`로 형변환하여 올바른 다중정의 메소드를 선택하게  
  하면 해결된다.

```java
for(int i = 0; i < 3; i++) {
    set.remove(i);
    list.remove((Integer)i);
    // 또는 list.remove(Integer.valueOf(i));
}
```

- 위 예시가 혼란스러웠던 이유는 `List<E>` 인터페이스가 `remove(Object)`와 `remove(int)`를 다중정의했기  
  때문이다. 제네릭이 도입되기 전인 Java4까지의 `List`에서는 `Object`와 int가 근본적으로 달라서 문제가 없었다.  
  그런데 제네릭과 auto boxing이 등장하면서 두 메소드의 매개변수 타입이 더는 근본적으로 다르지 않게 되었다.  
  정리하자면, Java에 제네릭과 auto boxing을 더한 결과로 `List` 인터페이스가 취약해졌다.  
  다행이 같은 피해를 입은 API는 거의 없지만, 다중정의 시 주의를 기울여야 할 근거로는 충분하다.

- 그런데 여기서 끝이 아니다. Java8에 도입된 람다와 메소드 참조 역시 다중정의 시의 혼란을 키웠다.

```java
// (1)
new Thread(System.out::println).start();

// (2)
ExecutorService exc = Executors.newCachedThreadPool();
exc.submit(System.out::println);
```

- `(1)`와 `(2)`의 모습은 비슷하지만, `(2)`만 컴파일 오류가 난다. 넘겨진 인수는 모두 `System.out::println`으로  
  똑같고, 양쪽 모두 `Runnable`을 받는 형제 메소드를 다중정의하고 있다. 그런데 왜 한쪽만 실패할까?  
  원인은 바로 `submit()` 다중정의 메소드 중에는 `Callable<T>`를 받는 메소드도 있다는 데 있다.  
  하지만 모든 `println()`이 void를 반환하니, 반환값이 있는 `Callable`과는 헷갈릴 일이 없다고 생각할 수도 있다.  
  합리적인 추론이지만, 다중정의 해소(resolution: 적절한 다중정의 메소드를 찾는 알고리즘)는 이렇게 동작하지 않는다.  
  놀라운 사실 하나는 만약 `println()`이 다중정의 없이 단 하나만 존재했다면 이 `submit()`의 호출이  
  제대로 컴파일되었을 것이라는 사실이다. 지금은 참조된 메소드(`println()`)와 호출한 메소드(`submit()`) 양쪽 다  
  다중정의되어, 다중정의 해소 알고리즘이 우리의 기대처럼 동작하지 않는 상황이다.

- 기술적으로 보면 `System.out::println`은 부정확한 메소드 참조(inexact method reference)이다.  
  또한 _"암시적 타입 람다식(implicitly typed lambda expression)"_ 이나 부정확한 메소드 참조 같은  
  인수 표현식은 목표 타입이 선택되기 전에는 그 의미가 정해지지 않기 때문에 적용성 테스트(applicability test) 때  
  무시된다. 말이 어렵더라도, 핵심은 다중정의된 메소드 혹은 생성자들이 함수형 인터페이스를 인수로 받을 때  
  비록 서로 다른 함수형 인터페이스라도 인수 위치가 같으면 혼란이 생긴다는 것이다. 따라서 **메소드를 다중정의할 때,**  
  **서로 다른 함수형 인터페이스라도 같은 위치의 인수로 받아서는 안 된다.** 이 말은 서로 다른 함수형 인터페이스라도  
  서로 근본적으로 다르지 않다는 뜻이다. 컴파일할 때 명령줄 스위치로 `-Xlint:overloads`를 지정하면 이런 종류의  
  다중정의를 경고해줄 것이다.

- `Object` 외의 클래스 타입과 배열 타입은 근본적으로 다르다. `Serializable`과 `Clonable`외의 인터페이스 타입과  
  배열 타입도 근본적으로 다르다. 한편, `String`과 `Throwable`처럼 상위/하위 관계가 아닌 두 클래스는  
  _"관련 없다(unrelated)"_ 고 한다. 그리고 어떤 객체도 관련없는 두 클래스의 공통 인스턴스가 될 수 없으므로  
  관련 없는 클래스들끼리도 근본적으로 다르다.

- 이 외에도 어떤 방향으로도 형변환할 수 없는 타입 쌍이 있지만, 어쨌든 앞 문단에서 나열한 간단한 예시보다  
  복잡해지면 대부분의 프로그래머는 어떤 다중정의 메소드가 선택될지를 구분하기 어려워할 것이다. 다중정의된  
  메소드 중 하나를 선택하는 규칙은 매우 복잡하며, Java가 업데이트 될수록 더욱 복잡해지고 있어 이 모두를  
  이해하고 있기란 쉽지 않다.

- 이번 아이템에서 설명한 지침을 어기고 싶을 때도 있을 것이다. 이미 만들어진 클래스가 끼어들면 더욱 그렇다.  
  예를 들어 `String`은 Java4 부터 `contentEquals(StringBuffer)`를 가지고 있었다.  
  그런데 Java5 에서 `StringBuffer`, `StringBuilder`, `String`, `CharBuffer` 등의 비슷한  
  부류의 타입을 위한 공통 인터페이스로 `CharSequence`가 등장했고, 자연스럽게 `String`에도  
  `CharSequence`를 받은 `contentEquals()`가 다중정의 되었다.

- 그 결과 이번 아이템의 지침을 대놓고 어기는 모습이 되었다. 다행이 이 두 메소드는 같은 객체를 입력하면  
  완전히 같은 작업을 수행해주니 해로울 것은 전혀 없다. 이처럼 어떤 다중정의 메소드가 불리는지 몰라도  
  기능이 똑같다면 신경 쓸 것이 없다. 이렇게 하는 가장 일반적인 방법은 상대적으로 더 특수한 다중정의  
  메소드에서 덜 특수한(더 일반적인) 다중정의 메소드로 일을 넘겨버리는(forward) 것이다.

```java
public boolean contentEquals(StringBuffer sb) {
    return contentEquals((CharSequence) sb);
}
```

- Java 라이브러리는 이번 아이템의 정신을 지켜내려 애쓰고 있지만, 실패한 클래스도 몇 개 있다.  
  예를 들어 `String`의 `valueOf(char[])`과 `valueOf(Object)`는 같은 객체를 건네더라도  
  전혀 다른 일을 수행한다. 이렇게 해야 할 이유가 없었음에도 혼란을 불러올 수 있는 잘못된 사례로  
  남게 되었다.

<hr/>

## 핵심 정리

- 프로그래밍 언어가 다중정의를 허용한다 해서 다중정의를 꼭 활용하라는 뜻은 아니다.  
  일반적으로 **매개변수 수가 같을 때는 다중정의를 피하는 것이 좋다.** 상황에 따라,  
  특히 생성자라면 이 조언을 따르기가 불가능할 수 있다. 그럴 때는 헷갈릴 만한 매개변수는  
  형변환하여 정확한 다중정의 메소드가 선택되도록 해야 한다. 이것이 불가능하면, 예를 들어  
  기존 클래스를 수정해 새로운 인터페이스를 구현해야 할 때는 같은 객체를 입력받는 다중정의  
  메소드들이 모두 동일하게 작동하도록 해야 한다. 그렇지 못하면 프로그래머들은 다중정의된  
  메소드나 생성자를 효과적으로 사용하지 못할 것이고, 의도대로 동작하지 않는 이유를 이해하지도  
  못할 것이다.

<hr/>
