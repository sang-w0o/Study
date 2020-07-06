간단한 웹 application의 구조
======

<h2>간단한 Web App의 구성 요소</h2>

* 간단한 웹 application 개발 시 사용하는 전형적인 구조는 다음 요소를 포함한다.
  * Front Servlet : 브라우저의 모든 요청을 받는 창구 역할(Spring MVC에서는 DispatcherServlet에 해당)
  * Controller + View : 실제 웹 브라우저의 요청 처리 (컨트롤러는 요청 처리 후 그 결과를 View에 전달)
  * Service
  * DAO

* Front Servlet : 브라우저의 모든 요청을 받는 창구 역할(Spring MVC에서는 DispatcherServlet에 해당)
* Controller + View : 실제 웹 브라우저의 요청 처리 (컨트롤러는 요청 처리 후 그 결과를 View에 전달)
  * Controller의 주요 역할
    1. client가 요구한 기능 실행
    2. 응답 결과를 생성하는데 필요한 Model 생성
    3. 응답 결과를 생성할 View 선택
  * Controller는 app이 제공하는 기능과 사용자 요청을 연결하는 매개체로서, 기능 제공을 위한 logic을 직접 수행햐지는 않는다.   
    대신 해당 logic을 제공하는 서비스에 그 처리를 위임한다. 아래 코드를 보자.
```java
@PostMapping
public String submit(@ModelAttribute("command")ChangePwdCommand pwdCmd, Errors errors, HttpSession session) {
    new ChangePwdCommandValidator().validate(pwdCmd, errors);
    if(errors.hasErrors()) {
        return "edit/changePwdForm";
    }
    AuthInfo authInfo = (AuthInfo)session.getAttribute("authinfo");
    try {

        // Controller는 logic의 수행을 서비스에 위임한다.
        changePasswordService.changePassword(authInfo.getEmail(), pwdCmd.getCurentPassword(), pwdCmd.getNewPassword());
        return "edit/changedPwd";
    } catch(IdPasswordNotMatchingException e) {
        errors.rejectValue("currentPassword", "notMatching");
        return "edit/changePwdForm";
    }
}
```

* Service는 기능의 logic을 구현한다. 그리고 DB연동이 필요하다면 DAO 객체를 사용한다.
* DAO는 Data Access Object로, DB와 Web App 사이에 데이터를 이동시켜 주는 역할을 맡는다.   
  app은 DAO를 통해 DB에 데이터를 추가하거나 DB로부터 데이터를 읽어온다.
<hr/>

<h2>서비스의 구현</h2>

* 서비스는 핵심이 되는 기능의 logic을 제공한다.   
  예를 들어 비밀번호 변경 기능은 다음 login을 서비스에서 수행한다.
  * DB에서 비밀번호를 변경할 회원의 데이터를 구한다.
  * 존재하지 않으면 예외를 발생시킨다.
  * 회원 데이터의 비밀번호를 변경한다.
  * 변경 내역을 DB에 반영한다.

* Web App을 사용하든, 명령행이서 실행하든 비밀번호 변경 기능을 제공하는 서비스는 동일한 logic을 수행한다.   
  이런 logic들은 주로 한 번의 과정으로 끝나기 보다는 여러 과정을 거치는데, 중간 과정에서 실패가 나면   
  이전까지 했던 것들을 취소해야하고, 모든 과정을 성공적으로 진행했을 때 완료해야 한다.   
  이런 이유로 서비스 메소드들은 Transaction 범위에서 실행한다.
```java
@Transactional
public void changePassword(String email, String oldPwd, String newPwd) {
    Member member = memberDao.selectByEmail(email);
    if(member == null) {
        throw new MemberNotFoundException();
    }
    member.changePassword(oldPwd, newPWd);
    memberDao.update(member);
}
```

* Service 메소드는 기능을 실행한 후에 결과를 알려주어야 한다. 결과는 크게 다음 2가지 방식으로 알려준다.
  * 반환값을 이용한 정상 결과
  * Exception을 이용한 비정상 결과

* 위 두 가지를 보여주는 예시 코드를 보자.
```java
public class AuthService {

    //..

    public AuthInfo authenticate(String email, String password) throws MemberException {
        Member member = memberDao.selectByEmail(email);
        if(member == null) {
            throw new WrongIdPasswordException();
        }
        if(!member.matchPassword(password)) {
            throw new WrongIdPasswordException();
        }
        return new AuthInfo(member.getId(), member.getEmail(), member.getName());
    }
}
```
* 위와 같이 정상 처리됐을 경우에 반환 값과 예외 발생 시 Exception을 throw하면 컨트롤러에서 처리가 수월해진다.
```java
@RequestMaping(method=RequestMethod.POST)
public String submit(LoginCommand loginCmd, Errors errors, HttpSession session, HttpServletResponse response) {
    
    //..

    try {
        AuthInfo authInfo = authService.authenticate(loginCmd.getEmail(), loginCmd.getPassword());
        session.setAttribute("authInfo", authInfo);
        //..
        return "login/loginSuccess";
    } catch(MemberException e) {
        // Service는 기능 실행 실패 시 Exception을 발생시킨다.
        errors.reject("idPasswordNotMatching!");
        return "login/loginForm";
    }
}
```
<hr/>
