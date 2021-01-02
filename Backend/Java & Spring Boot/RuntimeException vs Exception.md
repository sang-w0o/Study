<h1>RuntimeException과 Exception의 차이</h1>

<h2>들어가며</h2>

* `java.lang.RuntimeException`과 `java.lang.Exception`의 차이를 알기 전에   
  `Exception`의 계층 구조를 먼저 알아보자.

* `Exception`과 `Error` 클래스들은 모두 `Throwable` 클래스를 상속받는다.   
  반면 `RuntimeException`은 `Exception`을 상속받는다.

<h2>차이점</h2>

* `RuntimeException`을 상속받는 클래스들은 __unchecked__ 예외로 처리되며,   
  그 외의 모든 `Exception`들은 __chcked__ 예외로 처리된다.

* __checked__ 예외는 __반드시 코드 어디선가에서 `CATCH` 되어야 하며__, 그렇지 않을 시 컴파일이 되지 않는다.   
  반대로 __unchecked__ 예외는 코드에서의 처리를 강요하지 않으며, 실제로 catch문으로 처리하지 않아도 된다.

* 따라서 컴파일러가 코드 작성자에 handle하기를 강요하는 예외들은 모두 `java.lang.Exception`을 상속받는 것들이며,   
  그렇지 않은 것들은 `java.lang.RuntimeException`을 상속받는 클래스 들이다.
<hr/>

<h2>코드에서의 차이점</h2>

* 먼저, 아래 코드를 보자.
```java
@RequiredArgsConstructor
@Service
public UserService {

    private final UsersRepository usersRepository;

    public void soutUserInfo(Integer userId) throws UserIdNotFoundException {
        Users user = usersRepository.findById(userId).orElseThrow(UserIdNotFoundException::new);
        System.out.println(user);
    }
}
```

* 위 코드는 `users` 테이블에서 id값을 통해 조회한 후, 결과가 없다면 `UserIdNotFoundException`을 던진다.
* 이때, `UserIdNotFoundException`은 아래와 같다.
```java
public class UserIdNotFoundException extends Exception {
    private static final long serialVersionUID = -1L;

    public UserIdNotFoundException() {
        super("USER ID IS NOT FOUND");
    }
}
```

* 위에서 말한대로 `UserIdException`은 __checked__ 예외인 `Exception`을 상속받으므로   
  명시적으로 throws 구문을 써야 하며, `UserService#soutUserInfo()`를 호출하는 코드에서   
  try-catch 문으로 예외 처리를 해줘야 한다.

* 하지만 `UserIdNotFoundException`이 `RuntimeException`을 상속받도록 하면   
  예외 발생 가능성이 있는 코드에서 명시적으로 throws를 사용하지 않아도 되며,   
  해당 코드를 호출하는 부분에서 예외를 처리하지 않아도 된다.(물론 무조건 처리하는것이 좋다.)
<hr/>