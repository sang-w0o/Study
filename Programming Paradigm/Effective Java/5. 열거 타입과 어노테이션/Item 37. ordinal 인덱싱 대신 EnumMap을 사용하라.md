# ordinal 인덱싱 대신 EnumMap을 사용하라

- 배열이나 리스트에서 원소를 꺼낼 때 `ordinal()`로 인덱스를 얻는 코드가 있다.  
  식물을 간단히 나타낸 아래 클래스를 예로 보자.

```java
class Plant {
    enum LifeCycle { ANNUAL, PERENNIAL, BIENNIAL }
    final String name;
    final LifeCycle lifeCycle;

    Plant(String name, LifeCycle lifeCycle) {
	this.name = name;
	this.lifeCycle = lifeCycle;
    }

    @Override public String toString() {
	return name;
    }
}
```

- 이제 정원에 심은 식물들을 배열 하나로 관리하고, 이들을 생애주기별로 묶어보자.  
  생애주기별로 총 3개의 집합을 만들고 정원을 한 바퀴 돌며 각 식물을 해당 집합에  
  넣는다. 이때 어떤 프로그래머는 집합들을 배열 하나에 넣고 생애주기의 ordinal  
  값을 그 배열의 인덱스로 사용하려 할 것이다.

```java
public class Client {
    public void sortPlants() {
	Set<Plant>[] plantsByLifeCycle = (Set<Plant>[]) new Set[Plant.LifeCycle.values().length];
	for(int i = 0; i < plantsByLifeCycle.length; i++) {
	    plantsByLifeCycle[i] = new HashSet<>();
	}
	for(Plant p : garden) {
	    plansByLifeCycle[p.lifeCycle.ordinal()].add(p);
	}
    }
}
```

- 위 코드는 동작은 하지만 문제가 한가득이다. 우선 배열은 제네릭과 호환되지 않으니 비검사 형변환을  
  수행해야 하고 깔끔히 컴파일되지 않을 것이다. 배열은 각 index의 의미를 모르니 출력 결과에  
  직접 레이블을 달아야 한다. 가장 심각한 문제는 정확한 정수값을 사용한다는 것을 클라이언트가  
  직접 보증해야 한다는 점이다. 정수는 열거 타입과 달리 타입 안전하지 않기 때문이다.  
  잘못된 값을 사용하면 잘못된 동작을 묵묵히 수행하거나 `ArrayIndexOutOfBoundsException`을 던질 것이다.

- 해결책은 뭘까? 여기서 배열은 실질적으로 열거 타입 상수를 값으로 매핑하는 일을 한다.  
  그러니 `Map`을 사용할 수도 있을 것이다. 사실 열거 타입을 key로 사용하도록 설계한 아주 빠른  
  `Map` 구현체가 존재하는데, 바로 `EnumMap`이다. 위 코드를 `EnumMap`을 사용하도록 수정해보자.

```java
public class Client {
    public void sortPlants() {
	Map<Plant.LifeCycle, Set<Plant>> plantsbyLifeCycle = new EnumMap<>(Plant.LifeCycle.class);
	for(Plant.LifeCycle lc : Plant.LifeCycle.values()) {
	    plantsbyLifeCycle.put(lc, new HashSet<>());
	}
	for(Plant p : garden) {
	    plantsbyLifeCycle.get(p.lifeCycle).add(p);
	}
    }
}
```

- 더 짧고 명료하고 안전하고 성능도 원래 버전과 동등하다. 안전하지 않은 형변환은 쓰지 않고,  
  맵의 key인 열거 타입이 그 자체로 출력용 문자열을 제공하니 출력 결과에 직접 레이블을  
  달 일도 없다. 나아가 배열 인덱스를 계산하는 과정에서 오류가 날 가능성도 원천봉쇄된다.  
  `EnumMap`의 성능이 `ordinal()`을 쓴 배열에 비견되는 이유는 그 내부에서 배열을  
  사용하기 때문이다. 내부 구현 방식을 안으로 숨겨 `Map`의 타입 안전성과 배열의 성능을  
  모두 얻어낸 것이다. 여기서 `EnumMap`의 생성자가 받는 key 타입의 `Class` 객체는  
  한정적 타입 토큰으로, 런타임 제네릭 타입 정보를 제공한다.

- `Stream`을 사용해 맵을 관리하면 코드를 더 줄일 수 있다. 아래는 위 동작을 거의 그대로  
  모방한 가장 단순한 형태의 `Stream` 기반 코드다.

```java
public class Client {
    public void sortPlants() {
	Arrays.stream(garden).collect(groupingBy(p -> p.lifeCycle));
    }
}
```

- 위 코드는 `EnumMap`이 아닌 고유한 맵 구현체를 사용했기에 `EnumMap`을 써서 얻은 공간과  
  성능 이점이 사라진다는 문제가 있다. 이 문제를 조금 더 구체적으로 살펴보자.  
  매개변수 3개짜리 `Collectors.groupBy()`는 mapFactory 매개변수에 원하는 맵 구현체를  
  명시해 호출할 수 있다.

```java
public class Client {
    public void sortPlants() {
	Arrays.stream(garden).collect(groupingBy(p -> p.lifeCycle, () -> new EnumMap<>(LifeCycle.class), toSet()));
    }
}
```

- 위처럼 단순한 프로그램에서는 최적화가 굳이 필요 없지만, 맵을 빈번히 사용하는 프로그램에서는 꼭  
  필요할 것이다.

- `Stream`을 사용하면 `EnumMap`만 사용했을 때와는 살짝 다르게 동작한다.  
  `EnumMap` 버전은 언제나 식물의 생애주기 당 하나식의 중첩 맵을 만들지만, `Stream` 버전은  
  해당 생애주기에 속하는 식물이 있을 때만 만든다. 예를 들어, 정원에 한해살이와 여러해살이 식물만  
  살고 두해살이는 없다면, `EnumMap` 버전에서는 맵을 3개 만들고 `Stream` 버전에서는 2개 만든다.

- 두 열거 타입 값들을 매핑하느라 `ordinal()`을 두 번이나 쓴 배열들의 배열을 본 적이 있을 것이다.  
  다음은 이 방식을 적용해 두 가지 상태(`Phase`)를 전이(`Transition`)와 매핑하도록 구현한  
  프로그램이다. 예를 들어, `LIQUID`에서 `SOLID`로의 전이는 `FREEZE`가 되고, `LIQUIED`에서  
  `GAS`로의 전이는 `BOIL`이 된다.

```java
public enum Phase {
    SOLID, LIQUID, GAS;

    public enum Transition {
	MELT, FREEZE, BOIL, CONDENSE, SUBLIME, DEPOSIT;
    }

    // 행은 from의 ordinal을, 열은 to의 ordinal을 인덱스로 쓴다.
    private static final Transition[][] TRANSITIONS = {
	{ null, MELT, SUBLIME },
	{ FREEZE, null, BOIL },
	{ DEPOSIT, CONDENSE, null }
    };

    // 한 상태에서 다른 상태로의 전이를 반환한다.
    public static Transition from(Phase from, Phase to) {
	return TRANSITIONS[from.ordinal()][to.ordinal()];
    }
}
```

- 앞서 본 간단한 정원 예제와 마찬가지로 컴파일러는 ordinal과 배열 인덱스 간의 관계를  
  알 수가 없다. 즉, `Phase`나 `Phase.Transition` 열거 타입을 수정하면서 상태전이 표인  
  TRANSITIONS를 함께 수정하지 않거나 잘못 수정하면 런타임 오류가 날 것이다. 운이 좋으면  
  `ArrayIndexOutOfBoundsException`이나 NPE가 던져질 수도 있다.  
  그리고 상태 전이 표의 크기는 상태의 가짓수가 늘어나면 제곱해서 커지며, null로 채워지는  
  칸도 늘어날 것이다.

- 다시 이야기하지만, `EnumMap`을 사용하는 편이 훨씬 낫다. 전이 하나를 얻으려면  
  이전 상태(from)와 이후 상태(to)가 필요하니, 맵 2개를 중첩하면 쉽게 해결할 수 있다.  
  안쪽 맵은 이전 상태와 전이를 연결하고, 바깥 맵은 이후 상태와 안쪽 맵을 연결한다.  
  전이 전후의 두 상태를 전이 열거 타입 `Transition`의 입력으로 받아, 이 `Transition`  
  상수들로 중첩된 `EnumMap`을 초기화하면 된다.

```java
public enum Phase {
    SOLID, LIQUID, GAS;

    public enum Transition {
	MELT(SOLID, LIQUID), FREEZE(LIQUID, SOLID),
	BOIL(LIQUID, GAS), CONDENSE(GAS, LIQUID),
	SUBLIME(SOLID, GAS), DEPOSIT(GAS, SOLID);

	private final Phase from;
	private final Phase to;

	Transition(Phase from, Phase to) {
	    this.from = from;
	    this.to = to;
	}

	// 상태 전이 맵 초기화
	private static final Map<Phase, Map<Phase, Transition>> m =
	    Stream.of(values()).collect(groupingBy(t -> t.from, () -> new EnumMap<>(Phase.class),
	        toMap(t -> t.to, t -> t, (x, y) -> y, () -> new EnumMap<>(Phase.class))));

	public static Transition from(Phase from, Phase to) {
	    return m.get(from).get(to);
	}
    }
}
```

- 상태 전이 맵을 초기화하는 코드는 제법 복잡하다. 이 맵의 타입인 `Map<Phase, Map<Phase, Transition>>`은  
  _"이전 상태에서 '이후 상테에서 전이로의 맵'에 대응시키는 맵"_ 이라는 뜻이다. 이러한 맵의 맵을 초기화하기  
  위해 `Collector` 2개를 차례로 이용했다. 첫 번째 `Collector`인 `groupingBy()`에서는 전이를  
  이전 상태를 기준으로 묶고, 두 번째 `Collector`인 `toMap()`에서는 이후 상태를 전이에 대응시키는  
  `EnumMap`을 생성한다. `toMap()`의 병함 함수인 `(x, y) -> y`는 선언한 하고 실제로는 쓰이지 않는데,  
  이는 단지 `EnumMap`을 얻으려면 맵 팩토리가 필요하고 `Collector`들은 점층적 팩토리를 제공하기  
  때문이다.

- 이제 여기에 새로운 상태인 `PLASMA`를 추가해보자. 이 상태와 연결된 전이는 2개이다.  
  첫 번째는 `GAS`에서 `PLASMA`로 변하는 `IONIZE`이며, 두 번째는 `PLASMA`에서 `GAS`로  
  변하는 `DEIONIZE`이다. 배열로 만들어진 코드였다면 새로운 상수를 `Phase`에 1개,  
  `Phase.Transition`에 2개를 추가하고, 원소 9개짜리인 배열들을 원소 16개짜리 배열로  
  교체해야 한다. 원소 수를 너무 적거나 많이 기입하거나, 잘못된 순서로 나열하면 이 프로그램은  
  컴파일은 통과하더라도 런타임에 문제를 일으킬 것이다. 반면 `EnumMap` 버전에서는 상태 목록에  
  `PLASMA`를 추가하고, 전이 목록에 `IONIZE(GAS, PLASMA)`와 `DEIONIZE(PLASMA, GAS)`만  
  추가하면 끝이다.

```java
public enum Phase {
    SOLID, LIQUID, GAS, PLASMA;

    public enum Transition {
	MELT(SOLID, LIQUID), FREEZE(LIQUID, SOLID),
	BOIL(LIQUID, GAS), CONDENSE(GAS, LIQUID),
	SUBLIME(SOLID, GAS), DEPOSIT(GAS, SOLID),
	IONIZE(GAS, PLASMA), DEIONIZE(PLASMA, GAS);

	//..
    }
}
```

- 나머지는 기존 로직에서 잘 처리해주기에 잘못 수정할 가능성이 극히 적다.  
  실제 내부에서는 맵들의 맵이 배열들의 배열로 구현되니 낭비되는 공간과 시간도  
  거의 없어 명확하고 안전하고 유지보수하기 좋다.

<hr/>

## 핵심 정리

- **배열의 index를 얻기 위해 ordinal을 사용하는 것은 일반적으로 좋지 않으니, 대신 `EnumMap`을**  
  **사용하라.** 다차원 관계는 `EnumMap<..., EnumMap<...>>`으로 표현하라.

<hr/>
