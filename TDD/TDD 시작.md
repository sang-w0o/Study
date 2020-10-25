<h1>TDD 시작</h1>

<h2>TDD 이전의 개발</h2>

* TDD는 테스트 추도 개발(Test-Driven Development)을 말한다. TDD 이전의 개발은 아래와 같이 진행되었다.
  1. 만들 기능에 대한 설계를 고민한다. 어떤 클래스와 인터페이스를 도출할지 고민하고, 각 타입에 어떤 메소드를 넣을지 생각한다.
  2. 과정 1을 수행하면서 구현에 대해서도 고민한다. 대략 어떻게 구현하면 될지 생각한 후 코드를 작성한다.
  3. 기능에 대한 구현을 완료한 것 같으면 기능을 테스트한다. 만약 버그가 있다면 과정 2에서 작성한 코드를 디버깅하며 원인을 찾는다.

* 과정2에서 한 번에 작성한 코드가 많은 경우에는 디버깅하는 시간도 길어졌다. 원인을 찾기 위해 많은 양의 코드를 탐색해야 했다. 디버깅을 위한 로그   
  메시지를 추가하고, 개발 도구가 제공하는 디버거를 이용해서 코드를 한 줄씩 따라가며 원인을 찾았다. 오히려 디버깅에 투입되는 시간이 많은 경우도 있다.

* 때로는 코드를 작성하는 개발자와 그 코드를 테스트하는 개발자가 다를 때도 있다.

* 테스트 과정도 쉽지 않은데, 웹 애플리케이션의 경우 기능 하나하나를 테스트하기 위해 WAS를 구동해야 하고, 클래스 파일을 변경하면 WAS도 다시 구동해야   
  하기에 시간이 많이 소요된다. 또한 데이터가 올바른지 확인하기 위해 DB에 접속해 SELECT Query를 수행하고, 아이디 중복과 같은 기능을 테스트하기 위해   
  INSERT Query로 데이터를 미리 넣어놔야 했다.

* ~~TDD는 어렵다..~~
<hr/>

<h2>TDD란?</h2>

* TDD는 테스트부터 시작한다. 구현을 먼저 하고 나중에 테스트하는 것이 아니라 __먼저 테스트를 하고 그 다음에 구현__ 한다. 구현 코드가 없는데 어떻게   
  테스트를 진행할 수 있을까? 여기서 테스트를 한다는 것은 기능이 올바르게 동작하는지를 검증하는 테스트 코드를 작성한다는 것을 의미한다. 기능을   
  검증하는 테스트 코드를 먼저 작성하고, 테스트를 통과시키기 위해 개발을 진행한다.

* 간단한 덧셈 기능을 TDD로 구현해보자.
```java
package chap02;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CalculatorTest {
    
    @Test
    void plus() {
        int result = Calculator.plus(1, 2);
        assertEquals(3, result);
    }
}
```

* 위 코드를 작성하면, `Calculator` 클래스가 없기에 컴파일 에러가 발생한다. 컴파일 에러를 없애기 전에 코드를 살펴보자.
  * JUnit은 `@Test` 어노테이션을 붙인 `plus()` 메소드를 테스트 메소드로 인식한다. 테스트 메소드는 기능을 검증하는 코드를 담고 있는 메소드이다.
  * `assertEquals()`는 수행한 결과가 기대한 값인지를 검증하는 메소드이다. `assertEquals()` 메소드는 인자로 받은 두 값이 동일한지 비교한다.   
    이때, 첫 번째 인자는 기대한 값이고 두 번째 인자는 실제 값이다. 즉, `assertEquals()` 메소드는 기대한 값과 실제 값이 동일한지 비교한다. 만약   
    두 값이 동일하지 않다면 `AssertionFailedError`가 발생한다.

* 위 코드에서 눈여겨 볼 점은 덧셈 기능을 제공하는 클래스와 메소드를 미리 `Calculator` 클래스와 `plus()` 메소드로 사용했다는 것이다.   

* 아래와 같이 `Calculator` 클래스를 작성해보자.
```java
package chap02;

public class Calculator {
    public static int plus(int a, int b) {
        return 0;
    }
}
```

* 이 후 `CalculatorTest#plus()`를 수행하면 아래와 같은 결과가 뜬다.
```
expected: <3> but was: <0>
Expected :3
Actual   :0
<Click to see difference>
```

* 위 로그는 기대한 값(Expected)은 3인데 실제(Actual) 값은 0이어서 에러가 발생했음을 의미한다. 만약 `Calculator#plus()`가 무조건 3을 반환하도록   
  작성하면 `CalculatorTest#plus(1, 2)`는 테스트 통과를 할 것이다. 만약 이 테스트 메소드에 `assertEquals(5, Calculator.plus(4, 1))`을 추가하면   
  또 다시 테스트를 통과하지 못할 것이다. 이러한 과정에서 아래와 같이 `Calculator`를 작성하는 것이 맞음을 알 수 있다.
```java
package chap02;

public class Calculator {
    public static int plus(int a, int b) {
        return a + b;
    }
}
```

* 위 코드의 경로를 살펴보면 `Calculator.java`는 파일이 `src/test/java`에 위치한다. `Calculator` 클래스를 `src/main/java` 소스 폴더에 만들어도   
  되지만 아직 완성된 기능이 아니기에 `src/test/java`에 작성했다. `src/test/java` 소스 폴더는 배포 대상이 아니므로 `src/test/java` 폴더에 코드를   
  만들면 완성되지 않은 코드가 배포되는 것을 방지하는 효과가 있다.

* 지금까지 간단한 덧셈 기능을 TDD로 구현해 봤다. 이는 TDD의 기본 흐름을 익히기에 좋은 예시이다.
* 앞서 TDD는 __기능을 검증하는 테스트 코드를 먼저__ 작성한다고 했다. 덧셈 예제에서는 덧셈 기능을 검증하는 테스트 코드를 먼저 작성했다. 이 과정에서   
  테스트 대상이 될 클래스명, 메소드명, 파라미터 개수, 반환형을 고민했다. 또한, 새로운 객체를 생성할지 아니면 정적 메소드로 구현할지도 고민해야 한다.   
  이러한 고민 과정은 실제 코드를 설계하는 과정과 유사하다.

* 테스트 코드를 작성한 뒤에는 컴파일 오류를 없애는데 필요한 클래스와 메소드를 작성했다. 그 후 테스트가 실패한 원인을 로그를 통해 확인하고, 코드를   
  수정해서 테스트를 통과했다. 이런 식으로 TDD는 테스트를 먼저 작성하고 테스트에 실패하면 테스트를 통과시킬 만큼 코드를 추가하는 과정을 반복하면서   
  점진적으로 기능을 완성해 나간다.
<hr/>

<h2>TDD 예시 : 암호 검사기</h2>

* 조금 더 현실적인 기능을 TDD로 구현해보자. 이번에 TDD로 구현할 기능은 암호 검사기이다. 암호 검사기는 문자열을 검사해서 규칙을 준수하는지에 따라   
  암호를 '약함', '보통', '강함'으로 구분한다. 살펴볼 예제는 아래의 규칙을 이용해서 암호를 검사한다.
  * 검사할 규칙은 다음 세 가지 이다.
    * 길이가 8 글자 이상.
    * 0부터 9 사이의 숫자 포함.
    * 영어 대문자 포함.
  * 위 3 개 규칙을 모두 충족하면 '강함', 2개를 충족하면 '보통', 1개 이하를 충족하면 암호는 '약함'에 해당한다.

* 네이밍을 고려하여 아래와 같은 테스트 코드 기반을 작성해보자.
```java
package chap02;

import org.junit.jupiter.api.Test;

public class PasswordStrengthMeterTest {
    
    @Test
    void name() {
        
    }
}
```

<h3>첫 번째 테스트 : 모든 규칙을 충족하는 경우</h3>

* 첫 번째 테스트를 잘 선택하지 않으면 이후 진행 과정이 순탄하게 흘러가지 않을 수 있다. 첫 번째 테스트를 선택할 때는 __가장 쉽거나 가장 예외적인 상황__   
  을 선택해야 한다. 암호 검사 기능에서 가장 쉽거나 가장 예외적인 것은 아래의 두 가지 상황이라 할 수 있다. 
  * 모든 규칙을 충족하는 경우
  * 모든 조건을 충족하지 않는 경우

* 위 중에서 어떤 경우가 시작하기에 좋을까? 먼저 모든 조건을 충족하지 않는 경우를 생각해보자. 모든 조건을 충족하지 않는 테스트를 통과시키려면 각   
  조건을 검사하는 코드를 모두 구현해야 한다. 한 번에 만들어야 할 코드가 많아지므로 첫 번째 테스트 코드를 통과시키는 시간도 길어진다. 이는 사실상   
  구현을 다 하고 테스트를 하는 방식과 다르지 않다.

* 모든 규칙을 충족하는 경우는 어떨까? 테스트를 쉽게 통과시킬 수 있다. 각 조건을 검사하는 코드를 만들지 않고 '강함'에 해당하는 값을 반환하면 테스트에   
  통과할 수 있다. 그래서 모든 조건을 충족하는 경우를 먼저 테스트 코드로 작성해보자.

```java
package chap02;

import org.junit.jupiter.api.Test;

public class PasswordStrengthMeterTest {

    @Test
    void meetsAllCriteria_Then_Strong() {
        PasswordStrenghMeter meter = new PasswordStrenghMeter();
        PasswordStrength result = meter.meter("ab12!@AB");
        assertEquals(PasswordStrength.STRONG, result);
    }
}
```

* 다음으로는 `PasswordStrengthMeter` 타입과 `PasswordStrength` 타입을 작성하여 컴파일 에러를 해결해보자.
```java
package chap02;

public enum PasswordStrength {
    STRONG
}
```

```java
package chap02;

public class PasswordStrengthMeter {
    
    public PasswordStrength meter(String s) {
        return null;
    }
}
```

* 컴파일 에러를 없앴으니 테스트 메소드를 수행해보면, `Expected`가 STRONG이지만 `Actual`이 null이므로 테스트에 실패한다. 이 테스트를 통과시키는   
  방법은 간단한데, `PasswordStrengthMeter#meter()`가 STRONG을 반환하도록 수정하면 된다.

<h3>두 번째 테스트 : 길이만 8글자 미만이고 나머지 조건은 충족하는 경우</h3>

* 두 번째 테스트 메소드를 추가하자. 이번에 테스트할 대상은 패스워드 문자열의 길이가 8글자 미만이고, 나머지 조건은 충족하는 암호이다. 규칙에 따르면   
  이 암호의 강도는 '보통' 이어야 한다.
```java
package chap02;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PasswordStrengthMeterTest {

    // 생략
    
    @Test
    void meetsOtherCriteria_except_for_Length_Then_Normal() {
        PasswordStrengthMeter meter = new PasswordStrengthMeter();
        PasswordStrength result = meter.meter("ab12!@A");
        assertEquals(PasswordStrength.NORMAL, result);
    }
}
```

* 위 테스트의 컴파일 에러를 없애기 위해 `PasswordStrength`에 NORMAL을 추가하고, 테스트를 수행해보면 Expected는 NORMAL이지만 Actual은 STORNG이므로   
  테스트에 실패한다. 마찬가지로 새로 추가한 테스트를 통과시키는 가장 간단한 방법은 `meter()`가 NORMAL을 반환하도록 수정하는 것이다. 하지만 이렇게   
  수정하면 앞서 만든 테스트는 통과하지 못한다. 두 테스트를 모두 통과시킬 수 있는 방법은 길이가 8보다 작으면 NORMAL을 반환하는 코드를 추가하는 것이다.
```java
package chap02;

public class PasswordStrengthMeter {

    public PasswordStrength meter(String s) {
        if(s.length() < 8) {
            return PasswordStrength.NORMAL;
        }
        return PasswordStrength.STRONG;
    }
}
```

<h3>세 번째 테스트 : 숫자를 포함하지 않고 나머지 조건은 충족하는 경우</h3>

* 세 번째 테스트 메소드를 추가해보자. 이번 테스트 대상은 숫자를 포함하지 않고 나머지 조건은 충족하는 암호이다. 이 암호도 '보통'의 강도를 가져야 한다.
```java
package chap02;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PasswordStrengthMeterTest {

    // 생략

    @Test
    void meetsOtherCriteria_except_for_number_Then_Normal() {
        PasswordStrengthMeter meter = new PasswordStrengthMeter();
        PasswordStrength result = meter.meter("ab!@ABqwer");
        assertEquals(PasswordStrength.NORMAL, result);
    }
}
```

* 새로 추가한 테스트를 수행해보면 Expected는 NORMAL이지만 Actual은 STRONG이므로 테스트에 실패했음을 알 수 있다. 이 테스트를 통과하는 방법도 어렵지   
  않은데, 암호가 숫자를 포함했는지를 판단해서 포함하지 않는 경우 NORMAL을 반환하도록 구현하면 된다.
```java
package chap02;

public class PasswordStrengthMeter {

    public PasswordStrength meter(String s) {
        if(s.length() < 8) {
            return PasswordStrength.NORMAL;
        }
        boolean containsNumber = meetsContainingNumberCriteria(s);
        if(!containsNumber) return PasswordStrength.NORMAL;
        return PasswordStrength.STRONG;
    }

    private boolean meetsContainingNumberCriteria(String s) {
        for(char ch : s.toCharArray()) {
            if(ch >= '0' && ch <= '9') {
                return true;
            }
        }
        return false;
    }
}
```

<h3>코드 정리 : 테스트 코드 정리</h3>

* 테스트 코드도 코드이기에 유지 보수 대상이다. 즉 테스트 메소드에서 발생하는 중복을 알맞게 제거하거나 의미가 잘 드러나게 코드를 수정할 필요가 있다.   
  먼저 `PasswordStrengthMeter` 객체를 생성하는 코드의 중복을 없앨 수 있다. 또한 `assertEquals()`로 암호 강도 측정 기능을 수행하고 이를 확인하는   
  코드를 중복 제거할 수 있다. 정리하면 아래와 같다.
```java
package chap02;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PasswordStrengthMeterTest {

    private PasswordStrengthMeter meter = new PasswordStrengthMeter();
    
    private void assertStrength(String password, PasswordStrength expStr) {
        PasswordStrength result = meter.meter(password);
        assertEquals(expStr, result);
    }
    
    @Test
    void meetsAllCriteria_Then_Strong() {
        assertStrength("ab12!@AB", PasswordStrength.STRONG);
    }

    @Test
    void meetsOtherCriteria_except_for_Length_Then_Normal() {
        assertStrength("ab12!@A", PasswordStrength.NORMAL);
    }

    @Test
    void meetsOtherCriteria_except_for_number_Then_Normal() {
        assertStrength("ab!@ABqwer", PasswordStrength.NORMAL);
    }
}
```

<h3>네 번째 테스트 : 값이 없는 경우</h3>

* 테스트 코드를 작성하는 과정에서 아주 중요한 테스트를 놓진 것을 발견했는데, 바로 값이 없는 경우를 테스트하지 않은 것이다. 이러한 예외 상황을 고려하지   
  않으면 소프트웨어는 비정상적으로 동작하게 된다. 예를 들어, `meter()` 메소드에 null을 전달하면 `NPE(NullPointerException)`이 발생하게 된다.   
  `NPE`가 발생하는 것은 원하는 상황이 아니므로 암호 강도 측정기는 null에 대해서도 알맞게 동작해야 한다. 만약 null이 암호로 들어오면   
  `PasswordStrength.INVALID`를 반환하도록 해보자.
```java
package chap02;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PasswordStrengthMeterTest {

    // 생략
    
    @Test
    void nullInput_then_Invalid() {
        assertStrength(null, PasswordStrength.INVALID);
    }
}
```

* 컴파일 에러를 없애기 위해 `PasswordStrength`에 INVALID를 추가해야 하며, `meter()` 메소드에 null을 검사하는 부분을 추가하자.
```java
package chap02;

public class PasswordStrengthMeter {

    public PasswordStrength meter(String s) {
        if(s == null) return PasswordStrength.INVALID;
        if(s.length() < 8) {
            return PasswordStrength.NORMAL;
        }
        boolean containsNumber = meetsContainingNumberCriteria(s);
        if(!containsNumber) return PasswordStrength.NORMAL;
        return PasswordStrength.STRONG;
    }

    private boolean meetsContainingNumberCriteria(String s) {
        for(char ch : s.toCharArray()) {
            if(ch >= '0' && ch <= '9') {
                return true;
            }
        }
        return false;
    }
}
```

* 위와 같이하면 테스트에 통과할 것이다.

* 예외 상황이 null만 있는 것은 아니다. 빈 문자열도 예외 상황이다.
```java
package chap02;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PasswordStrengthMeterTest {

    // 생략
    
    @Test
    void emptyInput_then_Invalid() {
        assertStrength("", PasswordStrength.INVALID);
    }
}
```

* 테스트를 추가했으니 테스트를 수행하면 테스트에 실패한다. Expected는 INVALID인데 Actual은 NORMAL이기 때문이다. 이 테스트를 통과시키기 위해   
  `meter()` 메소드를 아래와 같이 변경해보자.
```java
package chap02;

public class PasswordStrengthMeter {

    public PasswordStrength meter(String s) {
        if(s == null || s.isEmpty()) return PasswordStrength.INVALID;
        if(s.length() < 8) {
            return PasswordStrength.NORMAL;
        }
        boolean containsNumber = meetsContainingNumberCriteria(s);
        if(!containsNumber) return PasswordStrength.NORMAL;
        return PasswordStrength.STRONG;
    }

    private boolean meetsContainingNumberCriteria(String s) {
        for(char ch : s.toCharArray()) {
            if(ch >= '0' && ch <= '9') {
                return true;
            }
        }
        return false;
    }
}
```

<h3>다섯 번째 테스트 : 대문자를 포함하지 않고 나머지 조건을 충족하는 경우</h3>

* 다음 추가할 테스트는 대문자를 포함하지 않고 나머지 조건은 충족하는 경우이다.
```java
package chap02;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PasswordStrengthMeterTest {

    // 생략
    
    @Test
    void meetsOtherCriteria_except_for_Uppercase_Then_Normal() {
        assertStrength("ab12!@df", PasswordStrength.NORMAL);
    }
}
```

* 테스트를 수행하면 새로 추가한 검증 코드에서 실패할 것이다. 실패한 코드를 통과시키도록 해보자.
```java
package chap02;

public class PasswordStrengthMeter {

    public PasswordStrength meter(String s) {
        if(s == null || s.isEmpty()) return PasswordStrength.INVALID;
        if(s.length() < 8) {
            return PasswordStrength.NORMAL;
        }
        boolean containsNumber = meetsContainingNumberCriteria(s);
        if(!containsNumber) return PasswordStrength.NORMAL;
        boolean containsUpper = meetsContainingUppdercaseCriteria(s);
        if(!containsUpper) return PasswordStrength.NORMAL;
        return PasswordStrength.STRONG;
    }

    private boolean meetsContainingNumberCriteria(String s) {
        for(char ch : s.toCharArray()) {
            if(ch >= '0' && ch <= '9') {
                return true;
            }
        }
        return false;
    }
    
    private boolean meetsContainingUppdercaseCriteria(String s) {
        for(char ch : s.toCharArray()) {
            if(Character.isUpperCase(ch)) {
                return true;
            }
        }
        return false;
    }
}
```

<h3>여섯 번째 테스트 : 길이가 8글자 이상인 조건만 충족하는 경우</h3>

* 이제 남은 것은 한 가지 조건만 충족하거나 모든 조건을 충족하지 않는 경우이다. 이 중에서 먼저 길이가 8글자 이상인 조건만 충족하는 경우를 진행해보자.   
  이 경우 암호 강도는 '약함'에 해당한다.
```java
package chap02;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PasswordStrengthMeterTest {

    // 생략
    
    @Test
    void meetsOnlyLengthCriteria_Then_Weak() {
        assertStrength("abdefghi", PasswordStrength.WEAK);
    }
}
```

* 컴파일 에러를 없애기 위해 `PasswordStrength`에 WEAK를 추가해주고, 테스트를 수행하면 Expected는 WEAK지만 Actual은 NORMAL이기에 실패한다.   
  따라서 `meter()` 메소드를 아래와 같이 수정하자.
```java
package chap02;

public class PasswordStrengthMeter {

    public PasswordStrength meter(String s) {
        if(s == null || s.isEmpty()) return PasswordStrength.INVALID;
        boolean lengthEnough = s.length() >= 8;
        boolean containsNumber = meetsContainingNumberCriteria(s);
        boolean containsUpper = meetsContainingUppdercaseCriteria(s);
        if(lengthEnough && !containsNumber && !containsUpper) {
            return PasswordStrength.WEAK;
        }
        if(!lengthEnough) return PasswordStrength.NORMAL;
        if(!containsNumber) return PasswordStrength.NORMAL;
        if(!containsUpper) return PasswordStrength.NORMAL;
        return PasswordStrength.STRONG;
    }

    // 생략
}
```

<h3>일곱 번째 테스트 : 숫자 포함 조건만 충족하는 경우</h3>

* 다음 테스트는 숫자 포함 조건만 충족하는 경우이다. 이를 검증하기 위한 테스트 메소드를 추가해보자.
```java
package chap02;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PasswordStrengthMeterTest {

    // 생략
    
    @Test
    void meetsOnlyNumberCriteria_Then_Weak() {
        assertStrength("1234", PasswordStrength.WEAK);
    }
}
```

* 테스트를 실행하면 실패하며, 아래 처럼 코드를 수정해서 통과하도록 해보자.
```java
package chap02;

public class PasswordStrengthMeter {

    public PasswordStrength meter(String s) {
        if(s == null || s.isEmpty()) return PasswordStrength.INVALID;
        boolean lengthEnough = s.length() >= 8;
        boolean containsNumber = meetsContainingNumberCriteria(s);
        boolean containsUpper = meetsContainingUppdercaseCriteria(s);
        if(lengthEnough && !containsNumber && !containsUpper) {
            return PasswordStrength.WEAK;
        }
        if(!lengthEnough && containsNumber && !containsUpper) {
            return PasswordStrength.WEAK;
        }
        if(!lengthEnough) return PasswordStrength.NORMAL;
        if(!containsNumber) return PasswordStrength.NORMAL;
        if(!containsUpper) return PasswordStrength.NORMAL;
        return PasswordStrength.STRONG;
    }
}
```

<h3>여덟 번째 테스트 : 대문자 포함 조건만 충족하는 경우</h3>

* 이번에는 대문자 포함 조건만 충족하는 경우를 검증하는 테스트를 추가할 차례이다.
```java
package chap02;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PasswordStrengthMeterTest {

    // 생략
    
    @Test
    void meetsOnlyUpperCriteria_Then_Weak() {
        assertStrength("ABZEF", PasswordStrength.WEAK);
    }
}
```

* 테스트는 실패하며, 아래와 같이 코드를 수정해서 통과하도록 하자.
```java
package chap02;

public class PasswordStrengthMeter {

    public PasswordStrength meter(String s) {
        if(s == null || s.isEmpty()) return PasswordStrength.INVALID;
        boolean lengthEnough = s.length() >= 8;
        boolean containsNumber = meetsContainingNumberCriteria(s);
        boolean containsUpper = meetsContainingUppdercaseCriteria(s);
        if(lengthEnough && !containsNumber && !containsUpper) {
            return PasswordStrength.WEAK;
        }
        if(!lengthEnough && containsNumber && !containsUpper) {
            return PasswordStrength.WEAK;
        }
        if(!lengthEnough && !containsNumber && containsUpper) {
            return PasswordStrength.WEAK;
        }
        if(!lengthEnough) return PasswordStrength.NORMAL;
        if(!containsNumber) return PasswordStrength.NORMAL;
        if(!containsUpper) return PasswordStrength.NORMAL;
        return PasswordStrength.STRONG;
    }

    // 생략
}
```

<h3>코드 정리 : meter() 메소드 리팩토링</h3>

* 위에서 작성한 코드는 `meetsContainingNumberCriteria()`, `meetsContainingUppercaseCriteria()`를 메소드로 추출해서 `meter()` 메소드의 길이를   
  줄이긴 했지만, 여전히 if절이 복잡하게 느껴진다. 3 개의 if절은 세 조건 중에서 한 조건만 충족하는 경우 암호 강도가 WEAK라는 것을 판단하기 위해   
  작성된 코드이다. 결국 3개의 if절은 각각 3개의 조건 중 한 조건만 충족한다는 것을 확인하는 것이다. 그렇다면 충족하는 조건 개수를 담는 변수 하나를   
  두고, 그 변수를 활용하면 어떨까?
```java
package chap02;

public class PasswordStrengthMeter {

    public PasswordStrength meter(String s) {
        if(s == null || s.isEmpty()) return PasswordStrength.INVALID;
        int metCounts = 0;
        boolean lengthEnough = s.length() >= 8;
        if(lengthEnough) metCounts++;
        boolean containsNumber = meetsContainingNumberCriteria(s);
        if(containsNumber) metCounts++;
        boolean containsUpper = meetsContainingUppdercaseCriteria(s);
        if(containsUpper) metCounts++;
        
        if(metCounts == 1) return PasswordStrength.WEAK;
        
        if(!lengthEnough) return PasswordStrength.NORMAL;
        if(!containsNumber) return PasswordStrength.NORMAL;
        if(!containsUpper) return PasswordStrength.NORMAL;
        return PasswordStrength.STRONG;
    }

    // 생략
}
```

* 이제 아래의 코드를 리팩토링 해보자.
```java
if(!lengthEnough) return PasswordStrength.NORMAL;
if(!containsNumber) return PasswordStrength.NORMAL;
if(!containsUpper) return PasswordStrength.NORMAL;
```

* 위 코드의 의도는 충족하는 조건이 두 개인 경우, 암호 강도가 NORMAL이라는 규칙을 표현한 것이다. 이도 WEAK의 경우와 마찬가지로 metCounts 변수를   
  활용하여 리팩토링할 수 있다.
```java
package chap02;

public class PasswordStrengthMeter {

    public PasswordStrength meter(String s) {
        if(s == null || s.isEmpty()) return PasswordStrength.INVALID;
        int metCounts = 0;
        boolean lengthEnough = s.length() >= 8;
        if(lengthEnough) metCounts++;
        boolean containsNumber = meetsContainingNumberCriteria(s);
        if(containsNumber) metCounts++;
        boolean containsUpper = meetsContainingUppdercaseCriteria(s);
        if(containsUpper) metCounts++;

        if(metCounts == 1) return PasswordStrength.WEAK;
        if(metCounts == 2) return PasswordStrength.NORMAL;
        
        return PasswordStrength.STRONG;
    }

    // 생략
}
```

* 이로써 `meter()` 메소드는 훨씬 간결해지고 가독성도 높아졌으며, 테스트도 알맞게 수행할 수 있다.

<h3>아홉 번째 테스트 : 아무 조건도 충족하지 않은 경우</h3>

* 아직 테스트하지 않은 상황은 바로 아무 조건도 충족하지 않는 암호이다. 이를 위한 테스트 메소드를 추가해보자.
```java
package chap02;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PasswordStrengthMeterTest {

    // 생략
    
    @Test
    void meetsNoCriteria_Then_Weak() {
        assertStrength("abc", PasswordStrength.WEAK);
    }
}
```

* 이 테스트 코드를 수정하는 방법은 여러 가지가 있지만, 아래와 같이 수정하도록 하자.
```java
package chap02;

public class PasswordStrengthMeter {

    public PasswordStrength meter(String s) {
        if(s == null || s.isEmpty()) return PasswordStrength.INVALID;
        int metCounts = 0;
        boolean lengthEnough = s.length() >= 8;
        if(lengthEnough) metCounts++;
        boolean containsNumber = meetsContainingNumberCriteria(s);
        if(containsNumber) metCounts++;
        boolean containsUpper = meetsContainingUppdercaseCriteria(s);
        if(containsUpper) metCounts++;

        if(metCounts <= 1) return PasswordStrength.WEAK;
        if(metCounts == 2) return PasswordStrength.NORMAL;

        return PasswordStrength.STRONG;
    }

    // 생략
}
```

* 지금까지 새로운 테스트를 추가하거나 기존 코드를 수정하면 습관처럼 테스트를 실행했다. 그리고 실패한 테스트가 있다면 그 테스트를 통과시키기 위한   
  코드를 추가했다.

<h3>코드 정리 : 코드 가독성 개선</h3>

* 코드 정리를 좀 더 하자. 이번 변경 대상은 metCounts 변수를 계산하는 부분이다. 이 부분을 별도의 메소드로 추출하면 `meter()` 메소드의 가독성을 좀 더   
  높일 수 있을 것 같다.
```java
package chap02;

public class PasswordStrengthMeter {

    public PasswordStrength meter(String s) {
        if(s == null || s.isEmpty()) return PasswordStrength.INVALID;
        int metCounts = getMetCriteriaCounts(s);
        boolean lengthEnough = s.length() >= 8;
        if(lengthEnough) metCounts++;
        boolean containsNumber = meetsContainingNumberCriteria(s);
        if(containsNumber) metCounts++;
        boolean containsUpper = meetsContainingUppdercaseCriteria(s);
        if(containsUpper) metCounts++;

        if(metCounts <= 1) return PasswordStrength.WEAK;
        if(metCounts == 2) return PasswordStrength.NORMAL;

        return PasswordStrength.STRONG;
    }
    
    private int getMetCriteriaCounts(String s) {
        int metCounts = 0;
        if(s.length() >= 8) metCounts++;
        if(meetsContainingNumberCriteria(s)) metCounts++;
        if(meetsContainingUppdercaseCriteria(s)) metCounts++;
        return metCounts;
    }
    
    // 생략
}
```

* 이제 `meter()` 메소드의 가독성이 더 좋아졌으며, 이 메소드를 처음 보는 개발자도 전반적인 로직이 보이기에 더 쉽게 해석할 수 있을 것이다.

<h3>테스트에서 메인으로 코드 이동</h3>

* 마지막 남은 한 가지는 `PasswordStrength`와 `PasswordStrengthMeter`를 `src/test/java`에서 `src/main/java`로 이동시키는 것이다. 즉, 배포 대상인   
  폴더로 이동시키는 것이다.
<hr/>

<h2>TDD 흐름</h2>

* 지금까지 예시를 통해 TDD로 구현하는 과정을 살펴보았는데, 정리하면 아래와 같다.
  * `테스트` --> `코딩` --> `리팩토링` (반복)

* TDD는 기능을 검증하는 테스트를 먼저 작성한다. 작성한 테스트를 통과하지 못하면 테스트를 통과할 만큼만 코드를 작성한다. 테스트를 통과한 뒤에는 개선할   
  코드가 있으면 리팩토링한다. 리팩토링을 수행한 뒤에는 다시 테스트를 실행해서 기존 기능의 이상 유무를 확인한다. 이 과정을 반복하면서 점진적으로   
  기능을 완성해 나가는 것이 전형적인 TDD의 흐름이다.

<h3>테스트가 개발을 주도한다.</h3>

* 테스트 코드를 먼저 작성하면 테스트가 개발을 주도하게 된다. 위의 암호 검사 기능을 떠올려보면, 가장 먼저 통과해야할 테스트를 작성했다. 테스트를   
  작성하는 과정에서 구현을 생각하지 않았고, 단지 해당 기능이 올바르게 작동하는지 검증할 수 있는 테스트 코드를 작성했을 뿐이다.

* 테스트 코드를 추가한 뒤에는 테스트를 통과시킬 만큼 기능을 구현했다. 지금까지 작성한 테스트를 통과할 만큼만의 구현을 진행했다. 아직 추가하지 않은   
  테스트를 고려해서 구현하지 않았다. __미리 앞서 나중의 기능을 고려한 기능을 구현하지 않았다는 것 이다.__

* 테스트 코드를 만들면 다음 개발 범위가 정해진다. 테스트 코드가 추가되면서 검증하는 범위가 넓어질수록 구현도 점점 완성되어 간다.   
  이런 식으로 테스트가 개발을 주도해 나가는 것이다.

<h3>지속적인 코드 정리</h3>

* 구현을 완료한 뒤에는 리팩토링을 진행하였다. 리팩토링할 대상이 눈에 들어오면 리팩토링을 진행해서 코드를 정리했다. 당장 리팩토링 대상이나 방식이   
  떠오르지 않으면 다음 테스트를 진행했으며, 테스트 코드 자체도 리팩토링의 대상에 넣었다.

* 당장 리팩토링을 하지 않더라도 테스트 코드가 있으면 리팩토링을 보다 과감하게 진행할 수 있다. 잘 동작하는 코드를 수정하는 것은 불안감을 주기에 수정을   
  꺼리게 만들지만, 해당 기능이 온전하게 동작한다는 것을 검증해주는 테스트가 있으면 코드 수정에 대한 심리적 불안감을 덜어준다. 즉 테스트를 통해   
  리팩토링을 통한 개선을 원활하게 할 수 있게 해주는 것이다.

* TDD는 개발 과정에서 지속적으로 코드 정리를 하므로 코드 품질이 급격히 나빠지지 않게 막아주는 효과가 있다. 이는 향후 유지보수 비용을 낮추는데 기여한다.

<h3>빠른 피드백</h3>

* TDD가 주는 이점은 코드 수정에 대한 피드백이 빠르다는 점이다. 새로운 코드를 추가하거나 기존 코드를 수정하면 테스트를 돌려서 해당 코드가 올바른지   
  바로 확인할 수 있다. 이는 잘못된 코드가 배포되는 것을 방지한다.
<hr/>