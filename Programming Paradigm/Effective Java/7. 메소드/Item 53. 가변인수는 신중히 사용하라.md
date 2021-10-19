# 가변인수는 신중히 사용하라

- 가변인수(varargs) 메소드는 명시한 타입의 인수를 0개 이상 받을 수 있다. 가변인수 메소드를  
  호출하면 가장 먼저 인수의 개수와 길이가 같은 배열을 만들고 인수들을 이 배열에 저장하여  
  가변인수 메소드에 건네준다.

- 아래는 입력받은 int 인수들의 합을 계산해주는 가변인수 메소드다.  
  `sum(1, 2, 3)`은 6을, `sum()`은 0을 반환한다.

```java
static int sum(int... values) {
    int sum = 0;
    for (int value : values) {
	sum += value;
    }
    return sum;
}
```

- 인수가 1개 이상이어야 할 때도 있다. 예를 들어 최소값을 찾는 메소드인데 인수를 0개만  
  받을 수도 있도록 설계하는 것은 좋지 않다. 인수 개수는 런타임에 자동 생성된 배열의 길이로  
  알 수 있다.

```java
static int min(int... args) {
    if(args.length == 0) {
	throw new IllegalArgumentException("No values");
    }
    int min = args[0];
    for (int i = 1; i < args.length; i++) {
	if (args[i] < min) {
	    min = args[i];
	}
    }
    return min;
}
```

- 이 방식에는 몇 가지 문제가 있다. 가장 심각한 문제는 인수를 0개만 넣어 호출하면 컴파일타임이 아닌  
  런타임에 실패한다는 점이다. 코드도 지저분하다. args에 대한 유효성 검사를 명시적으로 해야 하고,  
  min의 초기값을 `Integer.MAX_VALUE`로 설정하지 않고는 더 명료한 for-each문도 사용할 수 없다.

- 다행이 훨씬 나은 방법이 있다. 아래 코드처럼 매개변수를 2개 받도록 하면 된다.  
  즉 첫번째로는 평범한 매개변수를 받고, 가변인수는 두 번째로 받으면 앞서의 문제가 말끔히 사라진다.

```java
static int min(int firstArg, int... remainingArgs) {
    int min = firstArg;
    for (int arg : remainingArgs) {
	if (arg < min) {
	    min = arg;
	}
    }
    return min;
}
```

- 위의 예시에서 보듯, 가변 인수는 인수의 개수가 정해지지 않았을 때 아주 유용하다. `printf()`는 가변인수와  
  한 묶음으로 Java에 도입되었고, 이때 핵심 리플렉션 기능도 재정비되었다. `printf()`와 리플렉션 모두  
  가변인수 덕을 톡톡히 보고 있다.

- 그런데 성능에 민감한 상황이라면 가변인수가 걸림돌이 될 수 있다. 가변인수 메소드는 호출될 때마다 배열을  
  새로 하나 할당하고 초기화한다. 다행히 이 비용을 감당할 수는 없지만 가변인수의 유연성이 필요할 때  
  선택할 수 있는 멋진 패턴이 있다. 예를 들어 해당 메소드 호출의 95%가 인수를 3개 이하로 사용한다 해보자.  
  그렇다면 아래처럼 인수가 0개인 것부터 4개인 것까지, 총 5개를 다중정의하자. 마지막 다중정의 메소드가 인수가  
  4개 이상인 5%의 호출을 담당하는 것이다.

```java
public void foo() { }
public void foo(int a1) { }
public void foo(int a1, int a2) { }
public void foo(int a1, int a2, int a3) { }
public void foo(int a1, int a2, int a3, int... rest) { }
```

- 따라서 메소드 호출 중 단 5%만이 배열을 생성한다. 대다수의 성능 최적화와 마찬가지로 이 기법도 보통 때는  
  별 이득이 없지만, 꼭 필요한 특수 상황에서는 사막의 오아시스가 되어줄 것이다.

- `EnumSet`의 정적 팩토리도 이 기법을 사용해 열거 타입 집합 생성 비용을 최소화한다.  
  아래는 실제 `EnumSet`의 정적 메소드인 `of()`가 다중정의된 모습이다.

```java
package java.util;

@SuppressWarnings("serial") // No serialVersionUID declared
public abstract class EnumSet<E extends Enum<E>> extends AbstractSet<E>
    implements Cloneable, java.io.Serializable
{
    //..

    public static <E extends Enum<E>> EnumSet<E> of(E e) {
        EnumSet<E> result = noneOf(e.getDeclaringClass());
        result.add(e);
        return result;
    }

    public static <E extends Enum<E>> EnumSet<E> of(E e1, E e2) {
        EnumSet<E> result = noneOf(e1.getDeclaringClass());
        result.add(e1);
        result.add(e2);
        return result;
    }

    public static <E extends Enum<E>> EnumSet<E> of(E e1, E e2, E e3) {
        EnumSet<E> result = noneOf(e1.getDeclaringClass());
        result.add(e1);
        result.add(e2);
        result.add(e3);
        return result;
    }

    public static <E extends Enum<E>> EnumSet<E> of(E e1, E e2, E e3, E e4) {
        EnumSet<E> result = noneOf(e1.getDeclaringClass());
        result.add(e1);
        result.add(e2);
        result.add(e3);
        result.add(e4);
        return result;
    }

    public static <E extends Enum<E>> EnumSet<E> of(E e1, E e2, E e3, E e4,
                                                    E e5)
    {
        EnumSet<E> result = noneOf(e1.getDeclaringClass());
        result.add(e1);
        result.add(e2);
        result.add(e3);
        result.add(e4);
        result.add(e5);
        return result;
    }

    @SafeVarargs
    public static <E extends Enum<E>> EnumSet<E> of(E first, E... rest) {
        EnumSet<E> result = noneOf(first.getDeclaringClass());
        result.add(first);
        for (E e : rest)
            result.add(e);
        return result;
    }

    //..
}
```

<hr/>

## 핵심 정리

- 인수 개수가 일정하지 않은 메소드를 정의해야 한다면 가변인수가 반드시 필요하다.  
  메소드를 정의할 때 필수 매개변수는 가변인수 앞에 두고, 가변인수를 사용할 때는  
  성능 문제까지 고려하자.

<hr/>
