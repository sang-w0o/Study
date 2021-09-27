# 캡슐화

- 모듈을 분리하는 가장 중요한 기준은 아마도 시스템에서 각 모듈이 자신을 제외한 다른 부분에 드러내지  
  않아야할 비밀을 얼마나 잘 숨기느냐에 있을 것이다. 이러한 비밀 중 대표적인 형태인 데이터 구조는  
  **레코드 캡슐화하기**와 **컬렉션 캡슐화하기**로 캡슐화해서 숨길 수 있다. 심지어 기본형 데이터도  
  **기본형을 객체로 바꾸기**를 적용해 캡슐화할 수 있다. 리팩토링할 때 임시 변수가 자주 걸리는데,  
  정확한 순서로 계산해야 하고 리팩토링 후에도 그 값을 사용하는 코드에서 접근할 수 있어야 하기 때문이다.  
  이럴 때는 **임시 변수를 질의 함수로 바꾸기**가 상당히 도움된다. 특히 길이가 너무 긴 함수를  
  쪼개는 데 유용하다.

- 클래스는 본래 정보를 숨기는 용도로 설계되었다. 앞 장에서는 **여러 함수를 클래스로 묶기**로 클래스를  
  만드는 방법을 봤다. 이 외에도 흔히 사용하는 추출하기/인라인하기 리팩토링의 클래스 버전인 **클래스 추출하기**와  
  **클래스 인라인하기**도 활용할 수 있다.

- 클래스는 내부 정보뿐 아니라 클래스 사이의 연결 관계를 숨기는데도 유용하다. 이 용도로는 **위임 숨기기**가  
  있다. 하지만 너무 많이 숨기려다 보면 인터페이스가 비대해질 수 있으니 반대 기법인 **중개자 제거하기**도 필요하다.

- 가장 큰 캡슐화 단위는 클래스와 모듈이지만, 함수도 구현을 캡슐화한다. 때로는 알고리즘을 통째로 바꿔야할 때가  
  있는데, **함수 추출하기**로 알고리즘 전체를 함수 하나에 담은 뒤, **알고리즘 교체하기**를 적용하면 된다.

## 레코드 캡슐화하기

```js
// 리팩토링 적용 전
const organization = { name: "Study", country: "KR" };

// 리팩토링 적용 후
class Organization {
  constructor(data) {
    this._name = data.name;
    this._country = data.country;
  }

  get name() {
    return this._name;
  }
  set name(arg) {
    this._name = arg;
  }
  get country() {
    return this._country;
  }
  set country(arg) {
    this._country = arg;
  }
}
```

### 배경

- 대부분의 프로그래밍 언어는 데이터 레코드를 표현하는 구조를 제공한다. 레코드는 연관된 여러  
  데이털들 직관적인 방식으로 묶을 수 있어서 각각을 따로 취급할 때보다 훨씬 의미 있는 단위로  
  전달할 수 있게 해준다. 하지만 단순한 레코드에는 단점이 있다. 특히, 계산해서 얻을 수 있는  
  값과 그렇지 않은 값을 명확히 구분해 저장해야 하는 점이 번걸보다. 가령, 값의 범위를 표현하려면  
  `{start: 1, end: 5}`나 `{start:1, length: 5}` 등의 방식으로 저장할 수 있다.  
  어떤 식으로 저장하든 _시작_ 과 _끝_ 의 _길이_ 를 알 수 있어야 한다.

- 바로 이 때문에 **가변 데이터를 저장하는 용도로는 레코드보다 객체가 더 적합**하다.  
  객체를 사용하면 어떻게 저장했는지를 숨긴 채 세 가지 값을 각각의 메소드로 제공할 수 있다.  
  사용자는 무엇이 저장된 값이고, 무엇이 계산된 값인지 알 필요가 없다. 캡슐화하면 이름을  
  바꿀 때도 좋다. 필드명을 바꿔도 기존 이름과 새 이름 모두를 각각의 메소드로 제공할 수 있어서,  
  사용자 모두가 새로운 메소드로 옮겨갈 때까지 점진적으로 수정할 수 있기 때문이다.

- 저자는 _가변_ 데이터일 때 객체를 선호한다. 값일 분면이라면 단순히 _시작_ 과 _끝_ 의 _길이_ 를  
  모두 구해서 레코드에 저장하면 된다. 이름을 바꿀 때는 그저 필드를 복제하면 된다. 그러면 앞서 객체를  
  활용해 수정 전후의 두 메소드를 동시에 제공한 방식과 비슷하게 점진적으로 수정할 수 있다.

- 레코드 구조는 두 가지로 구분할 수 있다. 하나는 필드명을 노출하는 형태고, 다른 하나는 필드를 외부로부터  
  숨겨 개발자가 원하는 이름을 쓸 수 있는 형태다. 후자는 주로 라이브러리에서 Hash, Map, HashMap,  
  Dictionary, 연관 배열(Associative Array) 등의 이름으로 제공한다. 많은 프로그래밍 언어가  
  HashMap을 쉽게 만드는 문법을 제공한다. HashMap은 다양한 프로그래밍 작업에 유용하지만, 필드를 명확히  
  알려주지 않는다는 것이 단점이 될 수 있다. 범위를 `{시작, 끝}` 또는 `{시작, 길이}` 중 어떤 방식으로  
  표현하는지 알아내는 유일한 길은 HashMap을 생성하고 사용하는 코드를 직접 확인하는 방법 뿐이다.  
  프로그램에서 HashMap을 쓰는 부분이 적다면 문제되지 않지만, 사용하는 곳이 많을수록 불분명함으로 인해  
  발생하는 문제가 커진다. 이러한 불투명한 레코드를 명시적인 레코드로 리팩토링해도 되지만, 그럴 바에는 레코드  
  대신 클래스를 사용하는 편이 낫다.

- 코드를 작성하다보면 중첩된 리스트나 해시맵을 받아서 JSON, XML 등의 포맷으로 직렬화할 때가 많다.  
  이런 구조 역시 캡슐화할 수 있는데, 그러면 나중에 포맷을 바꾸거나 추적하기 어려운 데이터를 수정하기가  
  수월해진다.

### 절차

- (1) 레코드를 담은 변수를 캡슐화한다.  
  레코드를 캡슐화하는 함수명은 검색하기 쉽게 지어준다.

- (2) 레코드를 감싼 단순한 클래스로 해당 변수의 내용을 교체한다. 이 클래스에 원본 레코드를 반환하는 접근자도  
  정의하고, 변수를 캡슐화하는 함수들이 이 접근자를 사용하도록 수정한다.

- (3) 테스트한다.

- (4) 원본 레코드 대신 새로 정의한 클래스 타입의 객체를 반환하는 함수들을 새로 만든다.

- (5) 레코드를 반환하는 예전 함수를 사용하는 코드를 `(4)`에서 만든 새 함수를 사용하도록 바꾼다.  
  필드에 접근할 때는 객체의 접근자를 사용하며, 적절한 접근자가 없다면 추가한다. 한 부분을 바꿀 때마다 테스트한다.  
  클라이언트가 데이터를 읽기만 한다면 데이터의 복제본이나, 읽기 전용 proxy를 반환할지 고려해본다.

- (6) 클래스에서 원본 데이터를 반환하는 접근자와 원본 레코드를 반환하는 함수들을 제거한다.

- (7) 테스트한다.

- (8) 레코드의 필드도 데이터 구조인 중첩 구조라면, 레코드 캡슐화하기와 컬렉션 캡슐화하기를 재귀적으로 적용한다.

### 예시: 간단한 레코드 캡슐화하기

```js
const organization = { name: "Study", country: "KR" };
```

- 위 상수는 프로그램 곳곳에서 레코드 구조로 사용하는 JS 객체로서, 아래와 같이 읽고 쓴다.

```js
result += `<h1>${organization.name}</h1>`; // 읽기
organization.name = "newName"; // 쓰기
```

- 가장 먼저 _(1) 레코드를 담은 변수를 캡슐화_ 해보자.

```js
function getRawDataOrganization() {
  return organization;
}
```

- 그러면 읽고 쓰는 코드는 아래처럼 바뀐다.

```js
result += `<h1>${getRawDataOrganization().name}</h1>`; // 읽기
getRawDataOrganization().name = "newName"; // 쓰기
```

- 그런데 방금 **변수 캡슐화하기**를 정식으로 따르지 않고, getter를 찾기 쉽도록 의도적으로  
  이상한 이름을 붙였다. 이 getter는 임시적으로 사용할 것이기 때문이다.

- 레코드를 캡슐화하는 목적은 변수 자체는 물론, 그 내용을 조작하는 방식도 통제하기 위함이다.  
  이렇게 하려면 _(2) 레코드를 클래스로 바꾸고_, _(4) 새 클래스의 인스턴스를 반환하는 함수를 새로 만든다._

```js
class Organization {
  constructor(data) {
    this._data = data;
  }
}

const organization = new Organization({ name: "Study", country: "KR" });
function getRawDataOrganization() {
  return organization._data;
}
function getOrganization() {
  return organization;
}
```

- 객체로 만드는 작업이 끝났으니, _(5) 레코드를 사용하던 코드를 살펴보자._  
  레코드를 갱신하던 코드는 모두 setter를 사용하도록 고친다.

```js
class Organization {
  //..
  set name(aString) {
    this._data.name = aString;
  }
}

// client
getOrganization().name = "newName";
```

- 마찬가지로, 레코드를 읽는 코드는 모두 getter를 사용하도록 바꾼다.

```js
class Organization {
  //..
  get name() {
    return this._data.name;
  }
}

// client
result += `<h1>${getOrganization().name}</h1>`;
```

- _(6) 다 바꿨다면 앞서 이상한 이름으로 지었던 임시 함수를 제거한다._  
  마지막으로, \_data의 필드들을 객체 안에 바로 펼쳐놓으면 더 깔끔할 것 같다.

```js
class Organization {
  constructor(data) {
    this._name = data.name;
    this._country = data.country;
  }
  get name() {
    return this._name;
  }
  set name(aString) {
    this._name = aString;
  }
  get country() {
    return this._country;
  }
  set country(aCountryCode) {
    this._country = aCountryCode;
  }
}
```

- 이렇게 하면 입력 데이터 레코드와의 연결을 끊어준다는 이점이 생긴다. 특히 이 레코드를 참조하여  
  캡슐화를 깰 우려가 있는 코드가 많을 때 좋다. 데이터를 개별 필드로 펼치지 않았다면  
  \_data 를 대입할 때 복제하는 식으로 처리했을 것이다.

### 중첩된 레코드 캡슐화하기

- 앞서는 단순한 레코드를 캡슐화하는 방법을 보았는데, JSON 문서처럼 여러 겹 중첩된 레코드라면  
  어떻게 해야할까? 리팩토링의 기본 절차는 똑같고 갱신하는 코드에 주의해야 한다는 점도 같지만,  
  읽는 코드를 다룰 때는 선택지가 몇 가지 더 생긴다.

- 중첩 정도가 심할수록 읽거나 쓸 때 데이터 구조 안으로 더 깊숙히 들어가야 한다.  
  예를 들어, 아래처럼 될 수 있다.

```js
customerData[customerID].usage[year][month] = amount;
```

- 가장 먼저 앞서 본 예시와 마찬가지로 **변수 캡슐화**를 진행하고, 그런 다음 전체 데이터 구조를 표현하는  
  클래스를 정의하고, 이를 반환하는 함수를 새로 만들자.

- 기본 절차에 따르면 customerData 객체를 반환하고, 필요한 접근자를 만들어서 사용하게 하면 된다.  
  따라서 데이터 구조 안으로 들어가는 코드를 setter로 뽑아내야 한다. 그런 다음, 뽑아낸 setter함수를  
  데이터 클래스로 옮긴다.

- 데이터 구조의 덩치가 클 수록 **쓰기 부분에 집중**해야 한다. 캡슐화에서는 값을 수정하는 부분을 명확하게  
  드러내고 한 곳에 모아두는 일이 굉장히 중요하다.

- 읽기 부분을 처리할 때는 몇 가지 방법이 있다.  
  첫째, **setter 때와 같은 방법을 적용할 수 있다. 즉, 읽는 코드를 모두 독립 함수로 추출한 다음 클래스로**  
  **옮기는 것이다.** 이 방법의 가장 큰 장점은 클래스의 모든 쓰임을 명시적인 API로 제공한다는 것이다.  
  클래스만 봐도 데이터 사용 방법을 모두 파악할 수 있다. 하지만 읽는 패턴이 다양하면, 그만큼 작성할  
  코드량이 늘어난다. 요즘 언어들에서는 List-Hash 데이터구조를 쉽게 다룰 수 있는데, 이런 언어를 사용한다면  
  클라이언트에 데이터를 이 형태로 넘겨주는 것도 좋다.

> List-Hash: Hashmap이 List의 원소로 된 자료 구조, <a href="https://martinfowler.com/bliki/ListAndHash.html">링크</a>

- 다른 방법으로, 클라이언트가 데이터 구조를 요청할 때 실제 데이터를 제공해도 된다. 하지만 이렇게 하면  
  클라이언트가 데이터를 직접 수정하지 못하게 막을 방법이 없어서 _모든 쓰기를 함수내에서 처리한다._ 는  
  캡슐화의 핵심 원칙이 깨지게 된다. 따라서 가장 간단한 방법은 내부 데이터를 복제해서 제공하는 것이다.

- 이 방법은 간단하지만 문제가 있다. 바로 눈에 띄는 데이터 구조가 클수록 복제 비용이 커지기에  
  성능이 느려질 수 있다는 것이다. 하지만 다른 경우와 마찬가지로 성능 비용을 감당할 수 있는  
  상황일 수도 있다. 막연히 걱정만 하지 말고, 얼마나 영향을 주는지 실제로 측정 해보자.  
  또다른 문제는, 클라이언트가 원본을 수정하고 있다고 착각할 수 있다는 것이다. 이럴 때는 읽기 전용  
  proxy를 제공하거나, 복제본을 동결(freeze)시켜 데이터를 수정하려 할 때 에러를 던지도록 할 수 있다.

<hr/>

## 컬렉션 캡슐화하기

```js
// 리팩토링 적용 전
class Person {
  get courses() {
    return this._courses;
  }
  set courses(aList) {
    this._courses = aList;
  }
}

// 리팩토링 적용 후
class Person {
  get courses() {
    return this._courses.slice();
  }
  addCourse(aCourse) {
    /*...*/
  }
  removeCourse(aCourse) {
    /*...*/
  }
}
```

### 배경

- 가변 데이터는 모두 캡슐화하는 것이 좋다. 그러면 데이터 구조가 언제 어떻게 수정되는지 파악하기 쉬워서  
  필요한 시점에 데이터 구조를 변경하기도 쉬워지기 때문이다. 특히 객체 지향 개발자들은 캡슐화를  
  적극 권장하는데, 컬렉션을 다룰 때는 곧장 실수를 저지르곤 한다. 예를 들어, 컬렉션 변수로의  
  접근을 캡슐화하면서 getter가 컬렉션 자체를 반환하도록 한다면, 그 컬렉션을 감싼 클래스가 눈치채지  
  못하는 상태에서 컬렉션의 원소들이 바뀌어버릴 수 있다.

- 이런 문제를 방지하기 위해서는 컬렉션을 감싼 클래스에 흔히 `add()`, `remove()` 라는 이름의  
  컬렉션 변경자 메소드를 만드는 방법이 있다. 이렇게 항상 컬렉션을 소유한 클래스를 통해서만 원소를  
  변경하도록 하면 프로그램을 개선하면서 컬렉션 변경 방식도 원하는대로 수정할 수 있다.

- 또한 컬렉션 getter가 원본 컬렉션을 반환하지 않게 만들어서 클라이언트가 실수로 컬렉션을 바꿀  
  가능성을 차단하도록 하자.

- 내부 컬렉션을 직접 수정하지 못하게 막는 방법 중 하나로, 절대로 컬렉션 값을 반환하지 않게 할 수 있다.  
  컬렉션에 접근하려면 컬렉션이 소속된 클래스의 적절한 메소드를 반드시 거치도록 하는 것이다.  
  예를 들어, `customer.orders.size()`처럼 접근하는 코드를 `customer.numberOfOrders()`처럼  
  바꾸는 것이다. 하지만 요즘 언어들은 다양한 컬렉션 클래스들을 표준화된 인터페이스로 제공하며, 컬렉션  
  파이프라인과 같은 패턴을 적용하여 다채롭게 조합할 수 있다. 표준 인터페이스 대신 전용 메소드를 만들어  
  사용하면 부가적인 코드가 상당히 늘어나며, 컬렉션 연산들을 조합해 사용할 수 없다는 단점이 있다.

- 또 다른 방법은 컬렉션을 읽기 전용으로 제공하는 것이다. 예를 들어, Java에서는 컬렉션의 읽기 전용 proxy를  
  반환하게 만들기 쉽다. Proxy가 내부 컬렉션을 읽는 연산은 그대로 전달하고, 쓰기는 예외를 던지는 식으로  
  모두 막는 것이다. Iterator나 열거형 객체를 기반으로 컬렉션을 조합하는 라이브러리들도 비슷한 방식을  
  사용한다. 가령 Iterator에서는 내부 컬렉션을 수정할 수 없게 한다.

- 가장 흔히 사용되는 방식은 아마도 **컬렉션 getter를 제공하되, 내부 컬렉션의 복제본을 반환하는 것**이다.  
  복제본을 수정해도 원본 컬렉션에는 아무런 영향을 주지 않는다. 반환된 컬렉션을 수정하면 원본도 수정될 것이라고  
  기대한 프로그래머는 당황할 수 있지만, 이미 여러 코드베이스에서 많은 프로그래머들이 사용하는 방식이라 크게  
  문제되지는 않을 것이다. 컬렉션이 상당히 크다면 성능 문제가 발생할 수 있다. 하지만 성능에 영향을 줄만큼  
  컬렉션이 큰 경우는 흔치 않으니, _ 성능에 대한 일반 규칙_ 을 따르도록 하자.

- 여기서 중요한 점은 코드베이스에서 일관성을 주는 것이다. 앞서 본 방식들 중 한 가지만 적용해서 컬렉션 접근  
  함수의 동작 방식을 통일해야 한다.

### 절차

- (1) 아직 컬렉션을 캡슐화하지 않았다면, **변수 캡슐화하기** 부터 한다
- (2) 컬렉션에 원소를 추가, 제거하는 함수를 추가한다.  
  컬렉션 자체를 통째로 바꾸는 setter는 제거하고, setter를 제거할 수 없다면 인수로 받은 컬렉션을  
  복제해 저장하도록 한다.
- (3) 정적 검사를 수행한다.
- (4) 컬렉션을 참조하는 부분을 모두 찾는다. 컬렉션의 변경자를 호출하는 코드가 모두 앞에서 추가한  
  추가, 제거 함수를 호출하도록 수정한다. 하나씩 수정할 때마다 테스트한다.
- (5) 컬렉션 getter를 수정해서 원본 내용을 수정할 수 없는 읽기 전용 proxy 혹은 복제본을 반환하게 한다.
- (6) 테스트한다.

### 예시

- 수업(courses) 목록을 필드로 가지는 `Person` 클래스를 보자.

```js
class Person {
  constructor(name) {
    this._name = name;
    this._courses = [];
  }

  get name() {
    return this._name;
  }
  get courses() {
    return this._courses;
  }
  set courses(aList) {
    this._courses = aList;
  }
}

class Course {
  constructor(name, isAdvanced) {
    this._name = name;
    this._isAdvanced = isAdvanced;
  }
  get name() {
    return this._name;
  }
  get isAdvanced() {
    return this._isAdvanced;
  }
}
```

- 위 코드 예시는 모든 필드가 접근자 메소드로 보호받고 있으니, 안이한 개발자는 이렇게만 해도 데이터를 제대로 캡슐화  
  했다고 생각하기 쉽다. 하지만 허점이 있다. Setter를 사용해 수업 컬렉션을 통째로 설정한 클라이언트는 누구든  
  이 컬렉션을 마음대로 수정할 수 있기 때문이다.

```js
// 클라이언트
const basicCourseNames = readBasicCourseNames(file);
person.courses = basicCourseNames.map((name) => new Course(name, false));

// 또다른 클라이언트
for (const name of readBasicCourseNames(file)) {
  person.courses.push(new Course(name, false));
}
```

- 위처럼 목록을 갱신하면 `Person` 클래스는 더 이상 컬렉션을 제어할 수 없게 되니, 캡슐화가 깨진다.  
  필드를 참조하는 과정만 캡슐화했을 뿐, 필드에 담긴 내용은 캡슐화하지 않은 것이 원인이다.

- 물론 `addCourse()`, `removeCourse()`같은 메소드를 제공해도 좋지만, 부가적인 코드를 늘리지  
  않기 위해서, getter에 복제본을 제공하도록 하면 편리하게 끝난다.

```js
class Person {
  //..
  get courses() {
    return this._courses.slice();
  }
}
```

- 컬렉션 관리를 책임지는 클래스를 만든다면, 어느정도 강박증을 갖고서라도 복제본을 제공하도록 하자.  
  또한 컬렉션을 변경할 가능성이 있는 작업을 할 때도 습관적으로 복제본을 만들어 하자.

<hr/>

## 기본형을 객체로 바꾸기

```js
// 리팩토링 적용 전
orders.filter((o) => o.priority === "high" || o.priority === "rush");

// 리팩토링 적용 후
orders.filter((o) => o.priority.higherThan(new Priority("normal")));
```

### 배경

- 개발 초기에는 단순한 정보를 숫자나 문자열 같은 간단한 데이터 항목으로 표현할 때가 많다.  
  그러다 개발이 진행되면서 간단했던 이 정보들이 더 이상 간단하지 않게 변한다. 예를 들어, 처음에는  
  전화번호를 문자열로 표현했는데, 나중에 포맷팅이나 지역 코드 추출 같은 특별한 동작이 필요해질  
  수 있다. 이런 로직들로 금세 중복 코드가 늘어나서, 사용할 때마다 드는 노력도 늘어나게 된다.

- 단순한 출력 이상의 기능이 필요해지는 순간, 그 데이터를 표현하는 전용 클래스를 정의하자.  
  시작은 기본형 데이터를 단순히 감싼 것과 큰 차이가 없기에 효과가 미미하다.  
  하지만 나중에 특별한 동작이 필요해지면, 이 클래스에 추가하면 되니 프로그램이 커질수록 점점  
  유용한 도구가 된다. 그리 대단해 보이지 않을지 모르지만, 코드베이스에 미치는 효과는 놀라울만큼 크다.

### 절차

- (1) 아직 변수를 캡슐화하지 않았다면, 캡슐화한다.
- (2) 단순한 값 클래스를 만든다. 생성자는 기존 값을 인수로 받아서 저장하고, 이 값을 반환하는  
  getter를 추가한다.
- (3) 정적 검사를 수행한다.
- (4) 값 클래스의 인스턴스를 새로 만들어서 필드에 저장하도록 `(1)`에서 만들어진 setter를 수정한다.  
  이미 있다면 필드의 타입을 적절히 변경한다.
- (5) 새로 만든 클래스의 getter를 호출한 결과를 반환하도록 `(1)`에서 만들어진 getter를 수정한다.
- (6) 테스트한다.
- (7) 함수명을 바꾸면 원본 접근자의 동작을 더 잘 드러낼 수 있는지 검토한다.

### 예시

- 기존 코드이다.

```js
class Order {
  constructor(data) {
    this.priority = data.priority;
  }
  //..
}
```

- 우선 _(1) 변수 캡슐화_ 를 진행한다.

```js
class Order {
  //..
  get priority() {
    return this._priority;
  }
  set priority(value) {
    this._priority = value;
  }
}
```

- 다음으로 _(2) 단순한 값 클래스를 만든다._  
  갑 클래스의 getter이름을 `priority()`라 할 수도 있지만, 이 경우에서 클라이언트의 입장에서  
  보면 속성 자체를 받은게 아니라, 해당 속성을 문자열로 표현한 값을 요청한 것이기 때문에  
  `toString()`이라 명명했다.

```js
class Priority {
  constructor(value) {
    this._value = value;
  }
  toString() {
    return this._value;
  }
}
```

- 이제 _(4, 5) 방금 만든 `Priority`를 사용하도록 `Order`를 수정_ 하자.

```js
class Order {
  //..
  get priority() {
    return this._priority.toString();
  }
  set priority(value) {
    this._priority = new Priority(value);
  }
}
```

- 이렇게 하면 `Order`의 getter가 이상해진다. 이 getter가 반환하는 값은 우선순위 자체가 아니라,  
  우선순위를 표현하는 문자열이기 때문이다. 따라서 함수명을 `priority()`에서 `priorityString()`으로  
  바꿔주자.

- 이 아이템의 리팩토링은 여기까지인데, 조금만 더 가다듬어 보자.

```js
class Order {
  //..
  get priority() {
    return this._priority;
  }
  get priorityString() {
    return this._priority.toString();
  }
  set priority(value) {
    this._priority = new Priority(value);
  }
}

class Priority {
  constructor(value) {
    if (value instanceof Priority) return value;
    if (Priority.legalValues().includes(value)) this._value = value;
    else throw new Error(`Invalid priority value: ${value}`);
  }

  toString() {
    return this._value;
  }
  get _index() {
    return Priority.legalValues().findIndex((s) => s === this._value);
  }
  static legalValues() {
    return ["low", "normal", "high", "rush"];
  }
  equals(other) {
    return this._index === other._index;
  }
  higherThan(other) {
    return this._index > other._index;
  }
  lowerThan(other) {
    return this._index < other._index;
  }
}
```

- 위처럼 동작을 추가하도록 수정하면, 클라이언트 코드를 더 의미있게 작성할 수 있게 된다.

```js
// 리팩토링 적용 전
orders.filter((o) => o.priority === "high" || o.priority === "rush");

// 리팩토링 적용 후
orders.filter((o) => o.priority.higherThan(new Priority("normal")));
```

<hr/>

## 임시 변수를 질의 함수로 바꾸기

```js
// 리팩토링 적용 전
const basePrice = this._quantity * this._itemPrice;
if(basePrice > 1000) return basePrice * 0.95;
else return basePrice * 0.98;

// 리팩토링 적용 후

get basePrice() { this._quantity * this._itemPrice; }
//..
if(this.basePrice > 1000) return this.basePrice * 0.95;
else return this.basePrice * 0.98;
```

### 배경

- 함수 안에서 어떤 코드의 결과값을 뒤에서 다시 참조할 목적으로 임시 변수를 쓰기도 한다.  
  임시 변수를 사용하면 값을 계산하는 코드가 반복되는 걸 줄이고, 변수명을 통해 값의 의미를  
  설명할 수도 있어서 유용하다. 그런데, 한 걸음 더 나아가 아예 함수로 만들어서 사용하는  
  편이 더 나을 때가 많다.

- 긴 함수의 한 부분을 별도 함수로 추출하고자 할 때, 먼저 변수들을 각각의 함수로 만들면 일이  
  수월해진다. 추출한 함수에 변수를 따로 전달할 필요가 없어지기 때문이다. 또한 이 덕분에 추출한  
  함수와 원래 함수의 경계가 더 분명해지기도 하는데, 그러면 부자연스러운 의존 관계나 부수효과를  
  찾고 제거하는 데 도움이 된다.

- 변수 대신 함수로 만들어두면 비슷한 계산을 수행하는 다른 함수에서도 사용할 수 있어, 코드 중복이 줄어든다.  
  그래서 저자는 여러 곳에서 똑같은 방식으로 계산되는 변수를 발견할 때마다 함수로 바꿀 수 있는지 살펴본다.

- 이번 리팩토링은 클래스 안에서 적용할 때 효과가 가장 크다. 클래스는 추출할 메소드들에 공유 컨텍스트를  
  제공하기 때문이다. 클래스 바깥의 최상위 함수로 추출하면, 매개변수가 너무 많아져서 함수를 사용하는  
  장점이 줄어든다. 중첩 함수를 사용하면 이런 문제는 없지만, 관련 함수들과 로직을 널리 공유하는 데 한계가 있다.

- 임시 변수를 무조건 질의 함수로 바꾼다고 다 좋아지는 건 아니다. 자고로 변수는 값을 한 번만 계산하고, 그 뒤로는  
  읽기만 해야 한다. 가장 단순한 예로, 변수에 값을 한 번 대입한 뒤 더 복잡한 코드 덩어리에서 여러번 다시  
  대입하는 경우는 모두 질의 함수로 추출해야 한다. 또한 이 계산 로직은 변수가 다음번에 사용될 때 수행해도  
  똑같은 결과를 내야 한다. 그래서 _옛날 주소_ 처럼 snapshot 용도로 쓰이는 변수에는 이 리팩토링을  
  적용하면 안된다.

### 절차

- (1) 변수가 사용되기 전에 값이 확실히 결정되는지, 변수를 사용할 때마다 계산 로직이 매번 다른 결과를  
  내지는 않는지 확인한다.

- (2) 읽기 전용으로 만들 수 있는 변수는 읽기 전용으로 만든다.

- (3) 테스트한다.

- (4) 변수 대입문을 함수로 추출한다. 변수와 함수가 같은 이름을 가질 수 없다면 함수명을 임시로 짓는다.  
  또한, 추출한 함수가 부수 효과를 일으키지는 않는지 확인한다. 부수효화가 있다면 **질의함수와 변경함수 분리하기**로  
  대처한다.

- (5) 테스트한다.

- (6) **변수 인라인하기**로 임시 변수를 제거한다.

### 예시

- 간단한 `Order` 클래스를 보자.

```js
class Order {
  constructor(quantity, item) {
    this._quantity = quantity;
    this._item = item;
  }

  get price() {
    var basePrice = this._item.price * this._quantity;
    var discountFactor = 0.98;

    if (basePrice > 1000) discountFactor -= 0.03;
    return basePrice * discountFactor;
  }
}
```

- 위 코드에서 임시변수인 basePrice와 discountFactor를 메소드로 바꿔보자.

- _(2) 읽기 전용으로 만들기 위해_ basePrice를 const 키워드로 선언하고,  
  _(4) 변수 대입문을 함수로 추출_ 해보자.

```js
class Order {
  //..
  get price() {
    const basePrice = this.basePrice;
    var discountFactor = 0.98;
    if (basePrice > 1000) discountFactor -= 0.03;
    return basePrice * discountFactor;
  }

  get basePrice() {
    return this._item.price * this._quantity;
  }
}
```

- _(5) 테스트하고_, _(6) 변수를 인라인_ 해보자.

```js
class Order {
  //..
  get price() {
    // const basePrice = this.basePrice;
    var discountFactor = 0.98;
    if (this.basePrice > 1000) discountFactor -= 0.03;
    return this.basePrice * discountFactor;
  }
}
```

- discountFactor 또한 _(4) 변수 대입문을 함수로 추출_ 해보자.

```js
class Order {
  //..
  get price() {
    const discountFactor = this.discountFactor;
    return this.basePrice * discountFactor;
  }

  get discountFactor() {
    var discountFactor = 0.98;
    if (this.basePrice > 1000) discountFactor -= 0.03;
    return discountFactor;
  }
}
```

- 이번에는 discountFactor에 값을 대입하는 문장이 둘인데, 모두 추출한 함수에 넣어줘야 한다.  
  마지막으로 변수를 인라인하여 마무리하자.

```js
class Order {
  //..
  get price() {
    return this.basePrice * this.discountFactor;
  }

  get basePrice() {
    return this._item.price * this._quantity;
  }

  get discountFactor() {
    var discountFactor = 0.98;
    if (this.basePrice > 1000) discountFactor -= 0.03;
    return discountFactor;
  }
}
```

<hr/>

## 클래스 추출하기

```js
// 리팩토링 적용 전
class Person {
  get officeAreaCode() {
    return this._officeAreaCode;
  }
  get officeNumber() {
    return this._officeNumber;
  }
}

// 리팩토링 적용 후
class Person {
  get officeAreaCode() {
    return this._telephoneNumber.areaCode;
  }
  get officeNumber() {
    return this._telephoneNumber.number;
  }
}

class TelephoneNumber {
  get areaCode() {
    return this._areaCode;
  }
  get number() {
    return this._number;
  }
}
```

### 배경

- 클래스는 반드시 명확하게 추상화하고, 소수의 주어진 역할만 처리해야 한다는 가이드라인을 들어봤을 것이다.  
  하지만 실무에서는 몇 가지 연산을 추가하고 데이터도 보강하면서 클래스가 점점 비대해지곤 한다.  
  기존 클래스를 굳이 쪼갤 필요까지는 없다고 생각하여 새로운 역할을 덧씌우기 쉬운데, 역할이 갈수록  
  많아지고 새끼를 치면서 클래스가 굉장히 복잡해진다.

- 메소드와 데이터가 너무 많은 클래스는 이해하기가 쉽지 않으니, 잘 살펴보고 적절히 분리해야 한다.  
  특히 일부 데이터와 메소드를 따로 묶을 수 있다면 어서 분리하라는 신호다. 함께 변경되는 일이 많거나  
  서로 의존하는 데이터들도 분리한다. 특정 데이터나 메소드 일부를 제거하면 어떤 일이 일어나는지  
  자문해보면 판단에 도움이 된다. 제거해도 다른 필드나 메소드들이 논리적으로 문제가 없다면  
  분리할 수 있다는 뜻이다.

- 개발 후반으로 접어들면 서브클래스가 만들어지는 방식에서 징후가 나타나기도 한다.  
  예를 들어, 작은 일부의 기능만을 위해 서브클래스를 만들거나, 확장해야 할 기능이 무엇이냐에 따라  
  서브클래스를 만드는 방식도 달라진다면, 클래스를 나눠야 한다는 신호다.

### 절차

- (1) 클래스의 역할을 분리할 방법을 정한다.

- (2) 분리될 역할을 담당할 클래스를 새로 만든다.

- (3) 원래 클래스의 생성자에서 새로운 클래스의 인스턴스를 생성하여 필드에 저장해둔다.

- (4) 분리될 역할에 필요한 필드들을 새 클래스로 옮긴다. (**필드 옮기기**)  
  하나씩 옮길 때마다 테스트한다.

- (5) 메소드들도 새로운 클래스로 옮긴다.(**함수 옮기기**) 이때 저수준 메소드, 즉 다른 메소드를 호출하기  
  보다는 호출을 당하는 일이 많은 메소드부터 옮긴다. 하나씩 옮길 때마다 테스트한다.

- (6) 양쪽 클래스의 인터페이스를 살펴보면서 불필요한 메소드를 제거하고, 이름도 새로운 환경에 맞게 바꾼다.

- (7) 새 클래스를 외부로 노출할지 결정한다. 노출하려거든 새 클래스에 **참조를 값으로 바꾸기**를 적용할지  
  고민해본다.

### 예시

- `Person` 클래스를 보자.

```js
class Person {
  get name() {
    return this._name;
  }
  set name(value) {
    this._name = value;
  }
  get telephoneNumber() {
    return `(${this._telephoneNumber.areaCode}) ${this._telephoneNumber.number}`;
  }
  get officeAreaCode() {
    return this._officeAreaCode;
  }
  set officeAreaCode(value) {
    this._officeAreaCode = value;
  }
  get officeNumber() {
    return this._officeNumber;
  }
  set officeNumber(value) {
    this._officeNumber = value;
  }
}
```

- 우선 _(1) 클래스의 역할을 분리할 방법을 정해보자._ 여기서는 전화번호 관련 동작을 별도 클래스로 뽑아보자.  
  _(2) 먼저 빈 전화번호를 표현하는 `TelephoneNumber` 클래스를 만든다._

```js
class TelephoneNumber {}
```

- 다음으로 _(3) 원래 클래스인 `Person`의 인스턴스를 생성할 때 새로운 클래스인 `TelephoneNumber`의_  
  _인스턴스도 함께 생성해 필드에 저장해주자._

```js
class Person {
  constructor() {
    this._telephoneNumber = new TelephoneNumber();
  }

  //..
}
```

- 그런 다음 _(4) 분리된 역할에 필요한 필드들을 새 클래스인 `Telephone`으로 옮기고,_  
  _(5) 메소드들도 새로운 클래스로 옮기자._

```js
class TelephoneNumber {
  //..
  get officeNumber() {
    return this._officeNumber;
  }
  set officeNumber(value) {
    this._officeNumber = value;
  }
  get officeAreaCode() {
    return this._officeAreaCode;
  }
  set officeAreaCode(value) {
    this._officeAreaCode = value;
  }
}

class Person {
  //..
  get officeNumber() {
    return this._telephoneNumber.officeNumber;
  }
  set officeNumber(value) {
    this._telephoneNumber.officeNumber = value;
  }
  get officeAreaCode() {
    return this._telephoneNumber.officeAreaCode;
  }
  set officeAreaCode(value) {
    this._telephoneNumber.officeAreaCode = value;
  }
}
```

- 이제 _(6) 정리_ 해보자. 새로 만든 클래스는 순수한 전화번호를 뜻하므로 office라는 단어를 쓸 이유가 없다.  
  마찬가지로 전화번호라는 뜻도 메소드명에서 강조할 이유가 없다. 그러니 메소드명을 **함수 선언 바꾸기**로  
  적절히 바꿔주자.

```js
class TelephoneNumber {
  //..
  get areaCode() {
    return this._areaCode;
  }
  set areaCode(value) {
    this._areaCode = value;
  }
  get number() {
    return this._number;
  }
  set number(value) {
    this._number = value;
  }
}

class Person {
  get officeAreaCode() {
    return this._telephoneNumber.areaCode;
  }
  set officeAreaCode(value) {
    this._telephoneNumber.areaCode = value;
  }
  get officeNumber() {
    return this._telephoneNumber.number;
  }
  set officeNumber(value) {
    this._telephoneNumber.number = value;
  }
}
```

- 마지막으로 전화번호를 출력하는 역할도 전화번호 클래스에 맡긴다.

```js
class TelephoneNumber {
  //..
  toString() {
    return `(${this._areaCode}) ${this._number}`;
  }
}
```

<hr/>

## 클래스 인라인하기

```js
// 리팩토링 적용 전
class Person {
  get officeAreaCode() {
    return this._telephoneNumber.areaCode;
  }
  get officeNumber() {
    return this._telephoneNumber.number;
  }
}

class TelephoneNumber {
  get areaCode() {
    return this._areaCode;
  }
  get officeNumber() {
    return this._number;
  }
}

// 리팩토링 적용 후
class Person {
  get officeAreaCode() {
    return this._telephoneNumber.areaCode;
  }
  get officeNumber() {
    return this._telephoneNumber.number;
  }
}
```

- ### 배경

- **클래스 인라인하기**는 **클래스 추출하기**를 거꾸로 돌리는 리팩토링이다. 예를 들어, 더 이상  
  제 역할을 못해서 그대로 두면 안되는 클래스들을 인라인할 수 있다. 역할을 옮기는 리팩토링을 하고 나니,  
  특정 클래스에 남은 역할이 거의 없을 때 이런 현상이 자주 발생한다. 이럴 땐 이 불상한 클래스를 가장  
  많이 사용하는 클래스로 흡수시키자.

- 두 클래스의 기능을 지금과 다르게 분배하고 싶을 때도 클래스를 인라인한다. 클래스를 인라인해서 하나로  
  합친 다음, 새로운 클래스를 추출하는게 쉬울 수도 있기 때문이다. 이는 코드를 재구성할 때 흔히 사용하는  
  방식이기도 하다. 상황에 따라 한 컨텍스트의 요소들을 다른 쪽으로 하나씩 옮기는게 쉬울 수도 있고, 인라인  
  리팩토링으로 하나로 합친 후 추출하기 리팩토링으로 다시 분리하는게 쉬울 수도 있다.

### 절차

- (1) 소스 클래스의 각 public 메소드에 대응하는 메소드들을 타킷 클래스에 생성한다.  
  이 메소드들은 단순히 작업을 소스 클래스로 위임해야 한다.

- (2) 소스 클래스의 메소드를 사용하는 코드를 모두 타깃 클래스의 위임 메소드를 사용하도록 바꾼다.  
  하나씩 바꿀 때마다 테스트한다.

- (3) 소스 클래스의 메소드와 필드를 모두 타깃 클래스로 옮긴다. 하나씩 옮길 때마다 테스트한다.

- (4) 소스 클래스를 삭제한다.

### 예시

- 배송 추적 정보를 표현하는 `TrackingInformation` 클래스를 보자.

```js
class TrackingInformation {
  get shippingCompany() {
    return this._shippingCompany;
  }
  set shippingCompany(value) {
    this._shippingCompany = value;
  }
  get trackingNumber() {
    return this._trackingNumber;
  }
  set trackingNumber(value) {
    this._trackingNumber = value;
  }
  get display() {
    return `${this.shippingCompany}:  ${this.trackingNumber}`;
  }
}
```

- 이 클래스는 `Shipment` 클래스의 일부처럼 사용된다.

```js
class Shipment {
  get trackingInfo() {
    return this._trackingInformation.display;
  }
  get trackingInformation() {
    return this._trackingInformation;
  }
  set trackingInformation(value) {
    this._trackingInformation = value;
  }
}
```

- `TrackingInformation`이 예전에는 유용했을지 몰라도, 현재는 제 역할을 못하고 있으니  
  `Shipment` 클래스로 인라인하려 한다 하자.

- 먼저 `TrackingInformation`의 메소드를 호출하는 부분을 찾자.

```js
shipment.trackingInformation.shippingCompany = request.vendor;
```

- _(1) 소스 클래스인 `TrackingInformation`의 public 메소드를 모두 타깃 클래스인 `Shipment`로_  
  _옮기자._ 이때, 보통 때의 **함수 옮기기**와는 약간 다르게, 먼저 `Shipment`에 위임 함수를 만들고,  
  _(2) 클라이언트가 타깃 클래스의 위임 메소드를 사용하도록 바꾸자_.

```js
class Shipment {
  //..
  set shippingCompany(value) {
    this._trackingInformation.shippingCompany = value;
  }
}

// 클라이언트
shipment.shipmentCompany = request.vendor;
```

- 클라이언트에서 사용하는 `TrackingInformation`의 모든 요소를 이런 식으로 처리하자.  
  다 처리했다면 _(3) 소스 클래스인 `TrackingInformation`의 메소드와 필드를 모두 타깃 클래스인_  
  _`Shipment`로 옮기자._

```js
class Shipment {
  //..
  get trackingInfo() {
    return `${this._trackingInformation.shippingCompany}: ${this._trackingInformation.trackingNumber}`;
  }
  get shippingCompany() {
    return this._shippingCompany;
  }
  set shippingCompany(value) {
    this._shippingCompany = value;
  }
}
```

- 이 과정을 반복하고, _(4) 소스 클래스를 삭제하자._

```js
class Shipment {
  //..
  get trackingInfo() {
    return `${this.shippingCompany}: ${this.trackingNumber}`;
  }
}
```

<hr/>

## 위임 숨기기

```js
// 리팩토링 적용 전
manager = person.department.manager;

// 리팩토링 적용 후
class Person {
  get manager() {
    return this.department.manager;
  }
}
```

### 배경

- 모듈화 설계를 제대로 하는 핵심은 캡슐화다. 어쩌면 가장 중요한 요소일 수도 있다. 캡슐화는  
  모듈들이 시스템의 다른 부분에 대해 알아야 할 내용을 줄여준다. 캡슐화가 잘 되어 있다면  
  무언가를 변경해야 할 때 함께 고려해야 할 모듈 수가 적어져서 코드를 변경하기가 훨씬 쉽다.

- 객체지향을 처음 접할 때는 캡슐화란 필드를 숨기는 것이라 배운다. 그러나 이후에는 캡슐화의  
  역할이 그보다 많다는 사실을 알게 된다.

- 예를 들어, 제공자(server) 객체의 필드가 가리키는 객체(위임 객체)의 메소드를 호출하려면 클라이언트는 이  
  모든 위임 객체들을 알아야 한다. 위임 객체의 인터페이스가 바뀌면 이 인터페이스를 사용하는  
  모든 클라이언트가 코드를 수정해야 한다. 이러한 의존성을 없애려면 제공자 클래스 자체에 위임 메소드를  
  만들어서 위임 객체의 존재를 숨기면 된다. 그러면 위임 객체가 수정되더라도 제공자 코드만 고치면 되며,  
  클라이언트는 아무런 영향도 받지 않는다.

### 절차

- (1) 위임 객체의 각 메소드에 해당하는 위임 메소드를 재공자 클래스에 생성한다.
- (2) 클라이언트가 위임 객체 대신 제공자를 호출하도록 수정한다.  
  하나씩 바꿀 때마다 테스트한다.
- (3) 모두 수정했다면, 제공자로부터 위임 객체를 얻는 접근자를 제거한다.
- (4) 테스트한다.

### 예시

- `Person`과 `Department`를 아래처럼 정의했다 해보자.

```js
class Person {
  constructor(name) {
    this._name = name;
  }
  get name() {
    return this._name;
  }
  get department() {
    return this._department;
  }
  set department(value) {
    this._department = value;
  }
}

class Department {
  get chargeCode() {
    return this._chargeCode;
  }
  set chargeCode(value) {
    this._chargeCode = value;
  }
  get manager() {
    return this._manager;
  }
  set manager(value) {
    this._manager = value;
  }
}
```

- 클라이언트가 어떤 person이 속한 department의 manager를 알고 싶다고 하자.  
  그러기 위해서는 아래처럼 할 것이다.

```js
const manager = person.department.manager;
```

- 보다시피 클라이언트는 `Department`의 작동 방식, 다시 말해 `Department`가 manager 정보를  
  제공한다는 사실을 알고 있어야 한다. 이러한 의존성을 줄이기 위해 _(1)클라이언트가 `Department`를_  
  _볼 수 없게 숨기고, 대신 `Person`에 간단한 위임 메소드를 만들면 된다._

```js
class Person {
  //..
  get manager() {
    return this._department.manager;
  }
}
```

- 이제 _(2) 모든 클라이언트가 위임 객체 대신 제공자를 사용하도록 고치자._

```js
const manager = person.manager;
```

- _(3) 클라이언트 코드를 모두 수정했다면, `Person`의 `department()` 접근자를 삭제_ 한다.

<hr/>
