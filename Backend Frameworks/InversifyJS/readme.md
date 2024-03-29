# InversifyJS로 의존성 주입 구현

- Spring, NestJS와 같은 백엔드 프레임워크는 자체적으로 의존성 주입 기능을 제공한다.  
  InversifyJS 또한 의존성 주입을 위한 프레임워크인데, 의존성 주입만 할 수도 있기에  
  의존성 주입 기능을 원래 제공하지 않는 프레임워크(ex. express만 사용한 애플리케이션)를  
  사용할 때 손쉽게 의존성 주입을 사용할 수 있다.

## 의존성 주입

- 의존성 주입의 개념을 이야기할 때 빠질 수 없는 개념이 서비스 추상화와 의존성 역전 원칙이다.  
  예를 들어, 서비스가 있고 그 서비스를 사용하는 클라이언트가 있다고 해보자.  
  `Controller` - `Service` - `DAO` 의 3개 layer로 구성된 백엔드 애플리케이션의 구조에서  
  `Service` layer에 있는 코드를 사용하는 부분은 보통 `Controller` layer일 것이다.  
  여기서는 서비스를 사용하는 클라이언트가 컨트롤러가 된다.  
  이때, 서비스 코드를 컨트롤러 코드에서 직접 가져다 사용할 수도 있지만, 이는 서비스 코드와 클라이언트가  
  매우 강한 의존성을 갖게 하며, 서비스 코드가 바뀔 때 클라이언트쪽 코드도 바뀔 수 있기에 유지 보수에도  
  어렵다. 이를 해결하기 위해 나온 방법이 서비스 코드와 클라이언트 사이에 인터페이스를 두어, 클라이언트가  
  서비스에 직접적인 의존성을 갖지 않고, 서비스의 인터페이스에만 의존성을 갖게 하는 방법이다.  
  이렇게 하면 컨트롤러는 서비스 코드에 간접적인 의존성을 갖게 되어, 느슨한 결합 구조를 유지할 수 있다.

- 의존성 주입에는 아래 4개의 부분이 존재한다.

  - 서비스
  - 서비스를 사용하는 클라이언트
  - 서비스를 추상화한 인터페이스
  - 클라이언트에게 서비스 구현체를 주입해주는 injector

- 위 4개 중 마지막 부분을 보자. 라이언트가 서비스의 인터페이스에만 의존성을 갖기에  
  실제로 해당 서비스 인터페이스의 어떤 구현체가 런타임에 인스턴스화되어 클라이언트에서 사용할지 정해야 한다.  
  InversifyJS는 이 부분을 담당한다.

---
