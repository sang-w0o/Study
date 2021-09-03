# try-finally 보다는 try-with-resources를 사용하라

- Java 라이브러리에는 `close()` 메소드를 호출해 직접 닫아줘야하는 자원이 많다.  
  `InputStream`, `OutputStream`, `java.sql.Connection` 등이 좋은 예다.  
  자원 닫기는 클라이언트가 놓치기 쉬워서 예측할 수 없는 성능 문제로 이어지기도 한다.  
  이런 자원 중 상당수가 안전망으로 finalizer를 활용하고는 있지만 finalizer는  
  그리 믿을만하지 못하다.

- 전통적으로 자원이 제대로 닫힘을 보장하는 수단으로 try-finally가 쓰였다.  
  예외가 발생하거나 메소드에서 반환되는 경우를 포함해서 말이다.

```java
static String firstLineOfFile(String path) throws IOException {
    BufferedReader bufferedReader = new BufferedReader(new FileReader(path));
    try {
	return bufferedReader.readLine();
    } finally {
	bufferedReader.close();
    }
}
```

- 나쁘지 않지만, 자원을 하나 더 사용한다면 어떨까?

```java
static void copy(String src, String dst) throws IOException {
    InputStream inputStream = new FileInputStream(src);
    try {
	OutputStream outputStream = new FileOutputStream(dst);
	try {
	    byte[] buffer = new byte[BUFFER_SIZE];
	    int n;
	    while((n = inputStream.read(buffer)) >= 0)
	    	outputStream.write(buffer, 0, n);
	} finally {
	    outputStream.close();
	}
    } finally {
	inputStream.close();
    }
}
```

- try-finally문을 제대로 사용한 앞의 두 코드 예제에는 미묘한 결점이 있다.  
  예외는 try 블록과 finally 블록 모두에서 발생할 수 있는데, 예를 들어 기기에  
  물리적인 문제가 생긴다면 `firstLineOfFile()` 메소드 내의 `readLine()` 메소드가  
  예외를 던지고, 같은 이유로 `close()` 메소드도 실패할 것이다.  
  이런 상황이라면 두 번째 예외가 첫 번째 예외를 완전히 집어삼켜 버린다.  
  그러면 스택 추적 내역에 첫 번째 예외에 관한 정보는 남지 않게 되어, 실제 시스템에서의 디버깅을  
  몹시 어렵게 한다. 물론 두 번째 예외 대신 첫 번째 예외를 기록하도록 코드를 수정할 수는 있지만,  
  코드가 너무 지저분해져서 실제로 그렇게까지 하는 경우는 거의 없다.

- 이러한 문제들은 Java7에서 등장한 try-with-resources 덕분에 모두 해결되었다.  
  이 구조를 사용하려면 해당 자원이 `AutoClosable` 인터페이스를 구현해야 한다.  
  이 인터페이스는 단순히 void를 반환하는 `close()` 메소드 하나만 덩그러니 정의되어 있다.  
  Java 라이브러리와 third party 라이브러리들의 수많은 클래스와 인터페이스가 이미 `AutoClosable`을  
  구현하거나 확장해뒀다. 만약 닫아야하는 자원을 뜻하는 클래스를 작성한다면 `AutoClosable`을  
  반드시 구현하도록 하자.

- 아래는 위 코드를 try-with-resources를 사용해 다시 작성한 것이다.

```java
static String firstLineOfFile(String path) throws IOException {
    try(BufferedReader bufferedReader = new BufferedReader(new FileReader(path))) {
	return bufferedReader.readLine();
    }
}

static void copy(String src, String dst) throws IOException {
    InputStream inputStream = new FileInputStream(src);
    try (
	InputStream inputStream = new FileInputStream(src);
	OutputStream outputStream = new FileOutputStream(dst)) {
	byte[] buffer = new byte[BUFFER_SIZE];
	int n;
	while((n = inputStream.read(buffer)) >= 0)
	    outputStream.write(buffer, 0, n);
    }
}
```

- try-with-resources 를 사용한 버전이 짧고 읽기 수워할 뿐 아니라 문제를 진단하기에도  
  훨씬 좋다. `firstLineOfFile()` 메소드를 생각해보자. `readLine()`과 `close()` 호출  
  양쪽에서 예외가 발생하면, `close()`에서 발생한 예외는 숨겨지고 `readLine()`에서 발생한  
  예외가 기록된다. 이처럼 실전에서는 프로그래머에게 보여줄 예외 하나만 보존되고 여러 개의 다른 예외가  
  숨겨질 수도 있다. 이렇게 숨겨진 예외들도 그냥 버려지지는 않고 스택 추적 내역에 _suppressed_ 꼬리표를  
  달고 출력된다. 또한 Java7에서 `Throwable`에 추가된 `getSuppressed()` 메소드를 이용하면  
  프로그램 코드에서도 가져올 수 있다.

- 보통의 try-finally에서처럼 try-with-resources에서도 catch절을 쓸 수 있다.  
  catch절 덕분에 try문을 더 중첩하지 않고도 다수의 예외를 처리할 수 있다.  
  아래 코드는 `firstLineOfFile()` 메소드를 살짝 수정하여 파일을 열거나 데이터를 읽지 못했을 때  
  예외를 던지는 대신 기본값을 반환하도록 했다.

```java
static String firstLineOfFile(String path, String defaultValue) throws IOException {
    try(BufferedReader bufferedReader = new BufferedReader(new FileReader(path))) {
	return bufferedReader.readLine();
    } catch(IOException e) {
	return defaultValue;
    }
}
```

<hr/>

<h2>핵심 정리</h2>

- 꼭 회수해야 하는 자원을 다룰 때는 try-finally 대신 try-with-resources를 사용하자.  
  예외는 없다. 코드는 더 짧고 분명해지며 만들어지는 예외 정보도 훨씬 유용하다.  
  try-finally로 작성하면 실용적이지 못할 만큼 코드가 지저분해지는 경우라도,  
  try-with-resources로는 정확하고 쉽게 자원을 회수할 수 있다.

<hr/>
