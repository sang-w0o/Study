# equals를 재정의하려거든 hashCode도 재정의하라

- **`equals()`를 재정의한 클래스 모두에서 `hashCode()`도 재정의해야 한다.**  
  그렇지 않으면 `hashCode()` 일반 규약을 어기게 되어 해당 클래스의 인스턴스를  
  `HashMap`, `HashSet` 같은 컬렉션의 원소로 사용할 때 큰 문제를 일으킬 것이다.

- 아래는 `Object`의 명세에서 발췌한 규약이다.

> - `equals()` 비교에 사용되는 정보가 변경되지 않았다면, 애플리케이션이 실행되는 동안  
>   그 객체의 `hashCode()` 메소드는 몇 번을 호출해도 일관되게 같은 값을 반환한다.  
>   단, 애플리케이션을 다시 실행한다면 이 값이 달라져도 무방하다.
> - `equals(Object)`가 두 객체를 같다고 판단했다면, 두 객체의 `hashCode()`도 같은 값을  
>   반환해야 한다.
> - `equals(Object)`가 두 객체를 다르다고 판단했더라도, 두 객체의 `hashCode()`가  
>   서로 다른 값을 반환할 필요는 없다. 단, 다른 객체에 대해서는 다른 값을 반환해야  
>   해시테이블의 성능이 좋아진다.

- `hashCode()` 재정의를 잘못했을 때 크게 문제가 되는 조항은 두 번째이다.  
  즉, **논리적으로 같은 객체는 같은 해시코드를 반환해야 한다.**  
  이전에 보았듯이, `equals()`는 물리적으로 다른 두 객체를 논리적으로는 같다고 할 수 있다.  
  하지만 `Object`의 기본 `hashCode()`는 이 둘이 전혀 다르다고 판단하여, 규약과 달리  
  무작위처럼 보이는 서로 다른 값을 반환한다.

- 예를 들어 `PhoneNumber` 클래스의 인스턴스를 `HashMap`의 원소로 사용했다 하자.

```java
// PhoneNumber.java
public final class PhoneNumber {

    private final short areaCode, prefix, lineNum;

    public PhoneNumber(int areaCode, int prefix, int lineNum) {
	this.areaCode = rangeCheck(areaCode, 999, "area code");
	this.prefix = rangeCheck(prefix, 999, "prefix");
	this.lineNum = rangeCheck(lineNum, 9999, "line number");
    }

    private static short rangeCheck(int val, int max, String arg) {
	if(val < 9 || val > max) throw new IllegalAgrumentException(arg + ": " + val);
	return (short)val;
    }

    @Override
    public boolean equals(Object o) {
	if(o == this) return true;
	if(!(o instanceof PhoneNumber)) return false;
	PhoneNumber pn = (PhoneNumber) o;
	return pn.lineNum == lineNum && pn.prefix == prefix && pn.areaCode == areaCode;
    }

    //..
}

// 사용하는 부분
Map<PhoneNumber, String> m = new HashMap<>();
m.put(new PhoneNumber(707, 867, 5309), "Jenny");
```

- 이후에 `m.get(new PhoneNumber(707, 867, 5309))`를 수행하면 `"Jenny"`가 나와야 할 것 같지만  
  실제로는 null을 반환한다. 여기에는 2개의 `PhoneNumber` 인스턴스가 사용되었다.  
  하나는 `HashMap`에 "Jenny"를 넣을 때 사용했고, 두 번째는 논리적 동치로 이를 꺼내려 할 때 사용됐다.  
  `PhoneNumber` 클래스는 `hashCode()`를 재정의하지 않았기 때문에 논리적 동치인 두 객체가 서로 다른  
  해시코드를 반환하여 두 번째 규약을 지키지 못한다. 그 결과 `get()` 메소드는 엉뚱한 해시 버킷에 가서  
  객체를 찾으려 한 것이다. 설사 두 인스턴스를 같은 해시 버킷에 담았다 하더라도 `get()`은 여전히 null을  
  반환하는데, `HashMap`은 해시코드가 다른 entry끼리는 동치성 비교를 시도조차 하지 않도록  
  최적화되어 있기 때문이다.

- 이 문제는 `PhoneNumber`에 적절한 `hashCode()`만 재정의해주면 해결된다.  
  올바른 `hashCode()`는 어떤 모습이어야 할까? 안 좋게 작성하려면 아주 간단하다.  
  예를 들어, 아래 코드는 적법하게 구현했지만 절대 사용해서는 안된다.

```java
@Override public int hashCode() { return 42; }
```

- 위 코드는 동치인 모든 객체에서 똑같은 해시코드를 반환하니 적법하다.  
  하지만 끔찍하게도 **모든 객체에게 똑같은 값만 내어주므로** 모든 객체가 헤시테이블의 버킷 하나에  
  담겨 마치 LinkedList 처럼 동작한다. 그 결과 평균 수행 시간이 O(1)인 해시테이블이  
  O(n)으로 느려져서, 객체가 많아지면 도저히 쓸 수 없게 된다.

- 좋은 해시 함수라면 **서로 다른 인스턴스에 대해 다른 해시코드를 반환**해야 한다.  
  이것이 바로 `hashCode()`의 세 번째 규약이 요구하는 속성이다. 이상적인 해시 함수는 주어진  
  서로 다른 인스턴스들을 32비트 정수 범위에 균일하게 분배해야 한다. 이상을 완벽히 실행하기는 어렵지만  
  비슷하게 만들기는 그다지 어렵지 않다. 아래는 좋은 `hashCode()`를 작성하는 간단한 요령이다.

  - (1) int 변수 result를 선언한 후 값 c로 초기화한다. 이때 c는 해당 객체의 첫 번째 핵심 필드를  
    (2-a) 방식으로 계산한 해시코드이다. 여기서 핵심 필드란 `equals()` 비교에 사용되는  
    필드를 말한다.

  - (2) 해당 객체의 나머지 핵심 필드 f 각각에 대해 다음 작업을 수행한다.

    - (a) 해당 필드의 해시코드 c를 계산한다.

      - (1) 기본 타입 필드라면, `Type.hashCode(f)`를 수행한다. 여기서 `Type`은 해당  
        기본 타입의 박싱 클래스이다.

      - (2) 참조 타입 필드면서 이 클래스의 `equals()` 메소드가 이 필드의 `equals()`를  
        재귀적으로 호출해 비교한다면, 이 필드의 `hashCode()`를 재귀적으로 호출한다.  
        계산이 더 복잡해질 것 같으면 이 필드의 표준형을 만들어 그 표준형의 `hashCode()`를  
        호출한다. 필드의 값이 null이면 0을 사용한다.

      - (3) 필드가 배열이라면, 핵심 원소 각각을 별도 필드처럼 다룬다. 이상의 규칙을 재귀적으로  
        적용해 각 핵심 원소의 해시코드를 계산한 다음, (2-b) 로 갱신한다.  
        배열에 핵심 원소가 하나도 없다면 단순히 상수 (0)을 사용한다. 모든 원소가 핵심 원소라면  
        `Arrays.hashCode()`를 사용한다.

    - (b) (2-a)에서 계산한 해시코드 c로 result를 갱신한다. 코드로는 아래와 같다.  
      `result = 31 * result + c;`

  - (3) result를 반환한다.

- `hashCode()`를 다 구현했다면 이 메소드가 동치인 인스턴스에 대해 동일한 해시코드를  
  반환하는지 자문해보자. 단위 테스트를 작성하고, 동치인 인스턴스가 서로 다른 해시코드를  
  반환한다면 원인을 찾아 해결하면 된다.(AutoValue를 사용하면 안해도 된다.)

- 파생 필드(다른 필드로부터 계산해낼 수 있는 필드)는 해시코드 계산에서 제외해도 된다.  
  또한 **`equals()` 비교에 사용되지 않은 필드는 반드시 제외해야 한다.**  
  그렇지 않으면 `hashCode()`의 두 번째 규약을 어기게 될 위험이 있다.

- 단계 (2-b)의 곱셈 `31 * result`는 필드를 곱하는 순서에 따라 result값이 달라지게 한다.  
  그 결과 클래스에 비슷한 필드가 여러 개일 때 해시 효과를 크게 높여준다.  
  예를 들어 `String`의 `hashCode()`를 곱셈 없이 구현한다면 모든 아나그램  
  (구성하는 철자가 같고 순서만 다른 문자열)의 해시코드가 같아진다.  
  곱할 숫자에 31이 사용된 이유는 홀수이면서 prime number(소수)이기 때문이다.  
  만약 이 숫자가 짝수이고 overflow가 발생한다면 정보를 잃게 된다.  
  2를 곱하는 것은 shift 연산과 같은 효과를 내기 때문이다. 소수를 곱하는 이유는 명확하지는  
  않지만 전통적으로 그렇게 해왔다. 결과적으로 31을 사용하면 이 곱셈을 shift 연산과  
  뺄셈으로 대체해 최적화 할 수 있다. (`31 * i` == `(i << 5) - i`)  
  요즘 VM들은 이런 최적화를 자동으로 해준다.

- 이 요령을 `PhoneNumber`에 적용해보자.

```java
@Override public int hashCode() {
    int result = Short.hashCode(areaCode);
    result = 31 * result + Short.hashCode(prefix);
    result = 31 * result + Short.hashCode(lineNum);
    return result;
}
```

- 이 메소드는 `PhoneNumber` 인스턴스의 핵심 필드 3개만을 사용해 간단한 계산만 수행한다.  
  그 과정에 비결정적 요소는 전혀 없으므로 동치인 `PhoneNumber` 인스턴스들은 같은 해시코드를  
  가질 것이 확실하다. 사실 위 코드는 `PhoneNumber`에 딱 맞게 구현한 `hashCode()`이다.  
  Java 플랫폼 라이브러리의 클래스들이 제공하는 `hashCode()`와 비교해도 손색이 없다.  
  단순하고, 충분히 빠르고, 서로 다른 전화번호들은 다른 해시 버킷들로 제법 훌륭히 분배해준다.

- 이번 아이템에서 본 해시 함수 제작 요령은 최첨단은 아니지만 충분히 훌륭하다.  
  품질 면에서나 해싱 기능 면에서나 Java 플랫폼 라이브러리가 사용한 방식과  
  결줄만하며 대부분의 쓰임에도 문제가 없다.  
  단, 해시 충돌이 더욱 적은 방법을 써야 한다면 Guava의 `com.google.common.hash.Hashing`을 참고하자.

- `Objects` 클래스는 임의의 개수만큼 객체를 받아 해시코드를 계산해주는 정적 메소드인  
  `Objects.hash()` 메소드를 제공한다. 이 메소드를 활용하면 앞서의 요령대로 구현한 코드와  
  비슷한 수준의 `hashCode()`를 단 한줄로 작성할 수 있다.  
  하지만 아쉽게도 속도는 더 느리다. 입력 인수를 담기 위한 배열이 만들어지고, 입력 중 기본 타입이  
  있다면 박싱과 언박싱도 거쳐야하기 때문이다. 그러니 `hash()`는 성능에 민감하지 않은  
  상황에서만 사용하자.

```java
@Override public int hashCode() {
    return Objects.hash(lineNum, prefix, areaCode);
}
```

- 클래스가 불변이고 해시코드를 계산하는 비용이 크다면, 매번 새로 계산하기보다는 캐싱하는  
  방식을 고려해야 한다. 이 타입의 객체가 주로 해시의 key로 사용될 것 같다면 인스턴스가  
  만들어질 때 해시코드를 계산해둬야 한다. 해시의 key로 사용되지 않는 경우라면 `hashCode()`가  
  처음 불릴 때 계산하는 지연 초기화 전략은 어떨까? 필드를 지연 초기화하려면 그 클래스를  
  thread-safe하게 만들도록 신경써야 한다. `PhoneNumber`는 굳이 이렇게 해야할 이유는  
  없지만, 한 번 해보자. `hashCode()` 필드의 초기값은 흔히 생성되는 객체의 해시코드와는  
  달라야 함에 주의하자.

```java
private int hashCode;  // 자동으로 0으로 초기화된다.

@Override public int hashCode() {
    int result = hashCode;
    if(result == 0) {
	result = Short.hashCode(areaCode);
	result = 31 * result + Short.hashCode(prefix);
	result = 31 * result + Short.hashCode(lineNum);
    }
    return result;
}
```

- **성능 향상을 위해 해시코드를 계산할 때 핵심 필드를 생략하면 안된다.**  
  속도야 빨라지겠지만, 해시 품질이 나빠져 해시테이블의 성능을 심각하게 떨어뜨릴 수도 있다.  
  특히 어떤 필드는 특정 영역에 몰린 인스턴스들의 해시코드를 넓은 범위로 고르게 퍼트려주는  
  효과가 있을지도 모른다. 하필 이런 필드를 생략한다면 해당 영역의 수많은 인스턴스가  
  단 몇 개의 해시코드로 집중되어 해시테이블의 속도가 선형으로 느려질 것이다.  
  이 문제는 단지 이론에 그치지 않는다. 실제로 Java2 전의 `String`은 최대 16개의 문자만으로  
  해시코드를 계산했다. 문자열이 길면 균일하게 나눠 16문자만 뽑아내 사용한 것이다.  
  URL 처럼 계층적인 이름을 대량으로 사용한다면 이런 해시 함수는 앞서 이야기한  
  심각한 문제를 고스란히 드러낸다.

- **`hashCode()`가 반환하는 값의 생성 규칙을 API 사용자에게 자세히 공표하지 말자.**  
  **그래야 클라이언트가 이 값에 의지하지 않게 되고, 추후 계산 방식을 바꿀 수도 있다.**  
  `String`, `Integer`를 포함해 Java 라이브러리의 많은 클래스에서는 `hashCode()`가  
  반환하는 정확한 값을 알려준다. 바람직하지 않은 실수지만 바로잡기에는 이미 늦었다.  
  향후 릴리즈에서 해시 기능을 개선할 여지도 없애버렸다. 자세한 규칙을 공표하지 않는다면,  
  해시 기능에서 결함을 발견했거나 더 나은 해시 방식을 알아낸 경우 다음 릴리즈에서 수정할 수 있다.

<hr/>

<h2>핵심 정리</h2>

- `equals()`를 재정의할 때는 `hashCode()`도 반드시 재정의해야 한다.  
  그렇지 않으면 프로그램이 제대로 동작하지 않을 것이다. 재정의한 `hashCode()`는 `Object`의  
  API 문서에 기술된 일반 규약을 따라야 하며, 서로 다른 인스턴스라면 되도록 해시코드도  
  서로 다르게 구현해야 한다. 이렇게 구현하기가 어렵지는 않지만 조금 따분한 일이긴 하다.  
  AutoValue 프레임워크를 사용하면 멋진 `equals()`와 `hashCode()`를 자동으로 만들어준다.  
  IDE도 이런 기능을 일부 제공한다.

<hr/>
