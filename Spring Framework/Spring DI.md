Spring DI
======

<h2>의존</h2>

* __DI(Dependency Injection)__ 는 __의존 주입__ 이라 한다.
* Dependency(의존) : 객체 간의 의존

```java
// 회워 가입을 처리하는 예시 코드

import java.time.LocalDateTime;

public class MemberRegisterService {

    private MemberDao memberDao = new MemberDao();

    public void regist(RegisterRequest req) throws Exception {

        // Email로 회원 데이터 조회
        Member member = memberDao.selectByEmail(req.getEmail());

        // 해당 이메일을 가진 회원이 이미 존재하는 경우
        if(member != null) {
            throw new DuplicateMemberException("dup email" + req.getEmail());
        }

        // 해당 이메일을 가진 회원이 존재하지 않는 경우
        Member newMember = new Member(
            req.getEmail(), req.getPassword(), req.getName(), LocalDateTime.now());
        
        memberDao.insert(newMember);
    }
}
```
* 위 코드에서 `MemberRegisterService` 클래스는 `MemberDao` 클래스의 메소드를 사용한다.   
  이렇게 한 클래스가 다른 클래스의 메소드를 실행하는 것을 __의존__ 이라 한다.
* 위 코드의 경우에는 `MemberRegisterService` 클래스가 `MemberDao` 클래스에 의존하는 것이다.
<hr/>

<h2>DI를 통한 의존 처리</h2>

* DI는 의존하는 객체를 직접 생성하는 대신 __의존 객체를 전달받는 방식__ 을 사용한다.
```java
// 위 회원가입 처리 과정에 DI 방식 적용하기

import java.time.LocalDateTime;

public class MemberRegisterService {

    private MemberDao memberDao;

    public MemberRegisterService(MemberDao memberDao) {
        this.memberDao = memberDao;
    }

    public Long regist(RegisterRequest req) {

        Member member = memberDao.selectByEmail(req.getEmail());

        if(member != null) {
            throw new DuplicateMemberException("dup email" + req.getEmail());
        }
        
        Member newMember = new Member(
            req.getEmail(), req.getPassword(), req.getName(), LocalDateTime.now());
        
        memberDao.insert(newMember);
        return newMember.getId();
    }
}
```
* 위에서는 MemberDao 객체를 생성자를 통해 전달받는다. 즉 생성자를 통해 `MemberRegisterService`가   
  의존하고 있는 `MemberDao` 객체를 __주입__ 받은 것이다.
<hr/>

<h2>DI와 의존 객체 변경의 유연함</h2>

* 의존 객체를 직접 생성하는 방식은 필드나 생성자에서 __new 연산자__ 를 이용하여 객체를 생성한다.
* `MemberDao` 클래스는 회원 데이터를 db에 저장한다고 가정하자. 이 상태에서   
  회원 데이터의 빠른 조회를 위해 cache를 적용해야하는 상황이 발생했다. 그래서 `MemberDao`를   
  상속받은 `CachedMemberDao` 클래스를 만들었다.
```java
public class CachedMemberDao extends MemberDao {
    //...
}
```

* `ChangePasswordService` 도 MemberDao 객체를 사용해야 한다고 하자.

* DI 방식을 사용하면 다음과 같이 객체를 생성해야 한다.
```java
MemberDao memberDao = new CachedMemberDao();
MemberRegisterService regSvc = new MemberRegisterService(memberDao);
ChangePasswordWervice pwdSvc = new ChangePasswordService(memberDao);
```
* 만약 DI 방식을 사용하지 않으면, 두 `Service` 클래스 내의 코드를 각각 바꿔야 할 것이다.

<hr/>

<h2>객체 조립기(Assembler)</h2>

* assembler는 __객체를 주입하는 코드__ 가 기술된 객체이다.
```java
// chap03/src/main/java/assembler/Assembler.java
package assembler;

import spring.ChangePasswordService;
import spring.MemberDao;
import spring.MemberRegisterService;

public class Assembler {
	
	private MemberDao memberDao;
	private MemberRegisterService regSvc;
	private ChangePasswordService pwdSvc;
	
	public Assembler() {
		memberDao = new MemberDao();
		regSvc = new MemberRegisterService(memberDao);
		pwdSvc = new ChangePasswordService();
		pwdSvc.setMemberDao(memberDao);
	}
	
	public MemberDao getMemberDao() {
		return this.memberDao;
	}
	
	public MemberRegisterService getMemberRegisterService() {
		return this.regSvc;
	}
	
	public ChangePasswordService getChangePasswordService() {
		return this.pwdSvc;
	}
}
```

* 다음으로는 main 메소드가 있는 `MainForAssembler` 클래스를 만들자.
```java
// chap03/src/main/java/main/MainForAssembler.java

package main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import assembler.Assembler;
import spring.ChangePasswordService;
import spring.DuplicateMemberException;
import spring.MemberNotFoundException;
import spring.MemberRegisterService;
import spring.RegisterRequest;
import spring.WrongIdPasswordException;

public class MainForAssembler {
	
	public static void main(String[] args) throws IOException {
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		while(true) {
			System.out.println("input command : ");
			String command = reader.readLine();
			if(command.equalsIgnoreCase("exit")) {
				System.out.println("Exiting program..");
				break;
			}
			if(command.startsWith("new ")) {
				processNewCommand(command.split(" "));
				continue;
			} else if(command.startsWith("change ")) {
				processChangeCommand(command.split(" "));
				continue;
			}
			printHelp();
		}
	}
	
	private static Assembler assembler = new Assembler();
	
	private static void processNewCommand(String[] args) {
		if(args.length != 5) {
			printHelp();
			return;
		}
		MemberRegisterService regSvc = assembler.getMemberRegisterService();
		RegisterRequest req = new RegisterRequest();
		req.setEmail(args[1]);
		req.setName(args[2]);
		req.setPassword(args[3]);
		req.setConfirmPassword(args[4]);
		
		if(!req.isPasswordEqualsToConfirmPassword()) {
			System.out.println("Password does not match!");
			return;
		}
		try {
			regSvc.regist(req);
			System.out.println("Register completed.");
		} catch(DuplicateMemberException e) {
			System.out.println("This email already exists.");
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private static void processChangeCommand(String[] args) {
		if(args.length != 4) {
			printHelp();
			return;
		}
		ChangePasswordService changePwdSvc = assembler.getChangePasswordService();
		try {
			changePwdSvc.changePassword(args[1], args[2], args[3]);
			System.out.println("Password changed.");
		} catch(MemberNotFoundException e) {
			System.out.println("Member with this email does not exist.");
		} catch(WrongIdPasswordException e) {
			System.out.println("Email and password doesnt match.");
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private static void printHelp() {
		System.out.println();
		System.out.println("Wrong command. See usage below.");
		System.out.println("Usage 1 : new email name password confirmpassword");
		System.out.println("Usage 2 : change email oldpassword newpassword");
		System.out.println();
	}
}
```
<hr/>

<h2>Spring의 DI 설정</h2>

* Spring은 위에서 구현한 assembler와 유사한 기능을 제공한다.   
  즉, Spring은 Assembler 클래스의 생성자 코드 처럼 필요한 객체를 생성하고 생성한 객체에   
  의존을 주입한다. 또한 Spring은 `getMemberRegisterService()` 메소드 처럼 객체를   
  제공하는 기능을 정의하고 있다.

* 위에서 구현한 `Assembler` 대신 Spring을 사용하는 코드를 작성해보자.
* Spring을 사용하려면 먼저 __Spring이 어떤 객체를 생성하고, 의존을 어떻게 주입할지를__   
  __정의한 설정 정보를 작성__ 해야 한다.
```java
// chap03/src/main/config/AppCtx.java
package config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import spring.ChangePasswordService;
import spring.MemberDao;
import spring.MemberRegisterService;

@Configuration
public class AppCtx {
	
	@Bean
	public MemberDao memberDao() {
		return new MemberDao();
	}
	
	@Bean
	public MemberRegisterService memberRegSvc() {
		return new MemberRegisterService(memberDao());
	}
	
	@Bean
	public ChangePasswordService changePwdSvc() {
		ChangePasswordService pwdSvc = new ChangePasswordService();
		pwdSvc.setMemberDao(memberDao());
		return pwdSvc;
	}
}
```
* __@Configuration__ 어노테이션은 __Spring 설정 클래스__ 를 의미한다.
* __@Bean__ 어노테이션은 해당 메소드가 생성한 객체를 __Spring Bean__ 이라고 설정한다.
  * 예를 들어 `memberDao()`메소드를 이용해서 생성한 Bean 객체는 "memberDao"라는 이름으로 Spring에 등록된다.

* 다음으로는 객체를 생성하고 의존 객체를 주입하는 __Spring Container__ 를 생성하자.   
* Spring Container는 다음 클래스를 이용해서 생성할 수 있다.
```java
ApplicationContext ctx = new AnnotationConfigApplicationConftext(AppCtx.class);
```
* Container를 생성하면 `getBean()`메소드를 이용해서 사용할 객체를 구할 수 있다.
```java
MemberRegisterService regSvc = ctx.getBean("memberRegSvc", MemberRegisterService.class);
```

* 이제 `Assembler` 클래스를 이용한 `MainForAssembler` 클래스를 Spring Container를 사용하도록 변경해보자.
```java
package main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import config.AppCtx;
import spring.ChangePasswordService;
import spring.DuplicateMemberException;
import spring.MemberNotFoundException;
import spring.MemberRegisterService;
import spring.RegisterRequest;
import spring.WrongIdPasswordException;

public class MainForSpring {
	
	private static ApplicationContext ctx = null;
	
	public static void main(String[] args) throws IOException {
		
		// Spring Container 생성
		ctx = new AnnotationConfigApplicationContext(AppCtx.class);
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		while(true) {
			System.out.println("input command : ");
			String command = reader.readLine();
			if(command.equalsIgnoreCase("exit")) {
				System.out.println("Exiting program..");
				break;
			}
			if(command.startsWith("new ")) {
				processNewCommand(command.split(" "));
				continue;
			} else if(command.startsWith("change ")) {
				processChangeCommand(command.split(" "));
				continue;
			}
			printHelp();
		}
	}
	
	private static void processNewCommand(String[] args) {
		if(args.length != 5) {
			printHelp();
			return;
		}
		MemberRegisterService regSvc = ctx.getBean("memberRegSvc", MemberRegisterService.class);
		RegisterRequest req = new RegisterRequest();
		req.setEmail(args[1]);
		req.setName(args[2]);
		req.setPassword(args[3]);
		req.setConfirmPassword(args[4]);
		
		if(!req.isPasswordEqualsToConfirmPassword()) {
			System.out.println("Password does not match!");
			return;
		}
		try {
			regSvc.regist(req);
			System.out.println("Register completed.");
		} catch(DuplicateMemberException e) {
			System.out.println("This email already exists.");
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private static void processChangeCommand(String[] args) {
		if(args.length != 4) {
			printHelp();
			return;
		}
		ChangePasswordService changePwdSvc = ctx.getBean("changePwdSvc", ChangePasswordService.class);
		try {
			changePwdSvc.changePassword(args[1], args[2], args[3]);
			System.out.println("Password changed.");
		} catch(MemberNotFoundException e) {
			System.out.println("Member with this email does not exist.");
		} catch(WrongIdPasswordException e) {
			System.out.println("Email and password doesnt match.");
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private static void printHelp() {
		System.out.println();
		System.out.println("Wrong command. See usage below.");
		System.out.println("Usage 1 : new email name password confirmpassword");
		System.out.println("Usage 2 : change email oldpassword newpassword");
		System.out.println();
	}
}
```

<hr/>

<h2>@Congifuration 설정 클래스의 @Bean 설정과 Singleton</h2>

* Spring container는 __@Bean__ 어노테이션이 붙은 메소드에 대해서 __단 1개의 객체만__ 생성한다.   
  이는 다른 설정 메소드에서 `memberDao()`를 몇 번을 호출하더라도 항상 같은 객체를 반환함을 의미한다.

* Spring은 한 번 생성한 객체를 보관했다가, 이후에는 동일한 객체를 반환한다.
<hr/>

<h2>2 개 이상의 설정 파일 사용하기</h2>

* Spring을 이용해서 app개발을 하다보면 수많은 Bean 객체를 설정하게 되는데, 이들을   
  한 개의 클래스 파일에 설정하는 것 보다는 영역별로 나누면 관리하기 편해진다.
* 아래는 Appconf1.java 파일이다.
```java
package config;

// import 문

@Configuration
public class AppConf1 {
    @Bean
    public MemberDao memberDao() {
        return new MemberDao();
    }

    @Bean
    public MemberPrinter memberPrinter() {
        return new MemberPrinter();
    }
}
```
* 다음은 Appconf2.java 파일이다.
```java
package config;

// import 문

@Configuration
public class Appconf2 {

    @Autowired
    private MemberDao memberDao;
    @Autowired
    private MemberPrinter memberPrinter;

    @Bean
    public MemberRegisterService memberRegSvc() {
        return new MemberRegisterService(memberDao);
    }

    @Bean
    public ChangePasswordService changePwdSvc() {
        ChangePasswordService pwdSvc = new ChangePasswordService();
        pwdSvc.setMemberDao(memberDao);
        return pwdSvc;
    }

    //...
}
```
* 위 코드에서 새로운 개념은 __@Autowired__ 어노테이션이다.
```java
@Autowired
private MemberDao memberDao;
```
* __@Autowired__ 어노테이션은 __Spring의 자동 주입 기능__ 을 위한 것이다.   
  Spring 설정 클래스의 필드에 __@Autowired__ 어노테이션을 붙이면, 해당 타입의 Bean을 찾아서 필드에 할당한다.

* 설정 클래스가 두 개 이상이어도 Spring Container를 생성하는 코드는 다르지 않다.   
  단지 설정 클래스들을 인자로 넣어주면 된다.
```java
ApplicationContext ctx = new AnnotationConfigApplicationContext(AppConf1.class, AppConf2.class); 
```
<hr/>

<h2>@Configuration, @Autowired 어노테이션</h2>

* __@Autowired__ 어노테이션은 __Spring Bean에 의존하는 다른 Bean을 자동으로 주입하고 싶을 때__ 사용한다.
```java
// import 문

public class MemberInfoPrinter {

    @Autowired
    private MemberDao memberDao;
    @Autowired
    private MemberPrinter memberPrinter;

    public void printMemberInfo(String email) {
        Member member = memberDao.selectByEmail(email);
        if(member == null) {
            System.out.println("No Data.");
            return;
        }
        printer.print(member);
        System.out.println();
    }

    // Getters, Setters..
}
```
* 위에서는 두 필드에 __@Autowired__ 어노테이션을 붙였는데, 이처럼 __@Autowired__ 어노테이션을   
  의존 주입 대상에 붙이면, __Spring 설정 클래스의 @Bean 메소드에서 의존 주입을 위한 코드를__   
  __작성할 필요가 없다__. 
```java
@Bean
public MemberInfoPrinter infoPrinter() {
    MemberInfoPrinter infoPrinter = new MemberInfoPrinter();

    // Setter 메소드를 사용하여 의존 주입을 하지 않아도
    // Spring Container가 @Autowired를 붙인 필드에
    // 자동으로 해당 type의 Bean 객체를 주입한다.
    return infoPrinter;
}
```
<hr/>

<h2>@Import 어노테이션의 사용</h3>

* 두 개 이상의 설정 파일을 사용하는 또 다른 방법은 __@Import__ 어노테이션을 사용하는 것이다.
* __@Import__ 어노테이션은 함께 사용할 설정 클래스를 인수로 지정한다.
```java
package config;

// import 문

@Configuration
@Import(AppConf2.class)
public class AppConfImport {
    @Bean
    public MemberDao memberDao() {
        return new MemberDao();
    }

    @Bean
    public MemberPrinter memberPrinter() {
        return new MemberPrinter();
    }
}
```
* 이제는 Spring Container를 생성할 때 `AppConfImport` 클래스를 사용하면, __@Import__ 어노테이션으로   
  지정한 `AppConf2` 설정 클래스도 함께 사용하기 때문에 `AppConf2` 설정 클래스는 인수로 지정할 필요가 없다.
```java
public class MainForSpring {

    private static ApplicationContext ctx = null;

    public static void main(String[] args) {
        ctx = new AnnotationConfigApplicationContext(AppConfImport.class);
        //...
    }
}
```

* __@Import__ 어노테이션은 배열을 사용하여 두 개 이상의 설정 클래스도 import할 수 있다.
```java
@Configuration
@Import({AppConf1.class, AppConf2.class})
public class AppConfImport {
    //..
}
```
<hr/>

<h2>getBean() 메소드의 사용</h2>

```java
VersionPrinter versionPrinter = ctx.getBean("versionPrinter", VersionPrinter.class);
```
* `getBean()` 메소드의 첫 번째 인자는 __Bean의 이름__ 이고, 두 번째 인자는 __Bean의 Type__ 이다.
* 만약 존재하지 않는 Bean의 이름을 인자로 넣으면, __NoSuchBeanDefinitionException__ 이 발생한다.
* 만약 Bean의 이름과 맞지 않는 Bean의 Type을 지정하면, __BeanNotOfRequiredTypeException__ 이 발생한다.
* Bean의 이름을 지정하지 않고, __Bean의 Type만 지정__ 하여 Bean 객체를 구할 수도 있다. 
  * 이 때, 해당 type의 Bean객체가 존재하지 않으면 __NoSuchBeanDefinitionException__ 이 발생한다.
* 같은 type의 Bean객체가 두 개 이상 존재할 경우, Bean의 이름을 지정하지 않으면   
  __NoUniqueBeanDefinitionException__ 이 발생한다.
<hr/>

<h2>주입 대상 객체는 항상 Bean객체로 설정해야할까?</h2>

* 주입할 객체가 꼭 Spring Bean이어야 할 필요는 없다.   
  객체를 Spring Bean으로 등록하는것의 여부는 Spring Container가 객체를 관리하는지의 여부이다.
* Spring Container는 자동 주입, lifecycle 관리 등 단순 객체 생성 외에 객체 관리를 위한   
  다양한 기능을 제공하는데, Bean으로 등록한 객체에만 이러한 기능들을 적용한다.