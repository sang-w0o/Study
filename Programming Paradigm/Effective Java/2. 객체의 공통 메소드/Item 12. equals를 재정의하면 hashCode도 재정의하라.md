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
