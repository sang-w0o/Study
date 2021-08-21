About Maven Project
======

* Maven은 프로젝트 빌드와 life cycle, 사이트 생성 등 프로젝트 전반을 위한 관리 도구로서   
  많은 Java Project가 Maven을 사용해서 프로젝트를 관리하고 있다.

<hr/>

<h2>Maven Archetype을 이용한 프로젝트 생성</h2>

* Maven Project 폴더와 pom.xml을 직접 생성해도 되지만, Maven이 제공하는 archetype을 사용하면   
  미리 정의된 폴더 구조와 기반이 되는 pom.xml 파일을 사용해서 Maven Project를 생성할 수 있다.
* Archetype을 이용한 Maven Project 생성 명령어
```text
mvn archetype:generate
```
* 위 명령어를 실행하면 Maven Project를 생성하는데 필요한 정보를 입력하라는 메시지가 단계적으로 뜬다.   
  각 항목별로 알맞은 값을 입력하면 된다.
  * groupId : 프로젝트가 속하는 그룹 식별자 (패키지 형식으로 계층을 표현한다.)
  * artifactId : 프로젝트 결과물의 식별자 (프로젝트나 모듈을 의미하는 값이 온다.)
  * version : 결과물의 버전을 입력한다. (기본값 : 1.0-SNAPSHOT)
  * package : 생성할 패키지를 입력한다. (미입력 시 groupId와 동일한 구조의 패키지 생성)
<hr/>

<h2>Maven Project의 기본 Directory 구조</h2>

* `archetype:generate` 이 성공적으로 실행되면, artifactdId에 입력한 값과 동일한 이름의   
  폴더가 생성된다.
* maven project의 주요 폴더는 다음과 같다.

<table>
    <tr>
        <td>src/main/java</td>
        <td>Java 소스 파일이 위치한다.</td>
    </tr>
    <tr>
        <td>src/main/resources</td>
        <td>프로퍼티나 XML 등의 리소스 파일이 위치한다.</td>
    </tr>
    <tr>
        <td>src/main/webapp</td>
        <td>웹 application 관련 파일이 위치한다.(WEB-INF 폴더, JSP 파일 등)</td>
    </tr>
    <tr>
        <td>src/test/java</td>
        <td>Test Java 소스 파일이 위치한다.</td>
    </tr>
    <tr>
        <td>src/test/resources</td>
        <td>테스트 과정에서 사용되는 리소스 파일이 위치한다.</td>
    </tr>
</table>

<hr/>

<h2>Java 버전 수정</h2>

* pom.xml을 열어서 다음 코드를 추가하자.
```xml
<!--  생략 -->

<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <configuration>
                <source>1.8</source>
                <target>1.8</target>
                <encoding>UTF-8</encoding>
            </configuration>
        </plugin>
    </plugins>
</build>
```
* 위 설정은 Java 버전을 1.8로 설정한다.

<hr/>

<h2>컴파일 / 테스트 실행 / 패키지</h2>

* 소스 코드를 컴파일 하려면 다음 명령어를 실행한다.
```text
mvn compile
```
  * 컴파일된 결과는 __target/classes__ 폴더에 생성된다.

* 테스트 클래스를 실행하고 싶다면 다음 명령어를 실행한다.
```text
mvn test
```
  * 위 명령어를 실행하면 test code를 컴파일 하고 실행한 뒤 테스트 성공 여부를 출력한다.
  * 컴파일된 테스트 클래스들은 __target/test-classes__ 폴더에 생성되고,   
    결과 리포트는 __target/surefire-reports__ 폴더에 저장된다.

* 프로젝트를 패키징하여 jar 파일로 배포하고 싶다면 다음 명령어를 실행한다.
```text
mvn package
```
  * 위 명령어가 성공적으로 실행되면 __target__ 폴더에 프로젝트명과 버전에 따라   
    알맞는 이름을 갖는 jar 파일이 생성된다.
</hr>

<h2>POM.xml 기본</h2>

* Maven Project를 생성하면 pom.xml 파일이 프로젝트 루트 폴더에 생성된다.
* pom.xml파일은 __Project Object Model__ 정보를 담고 있는 파일이다.
  * 프로젝트 정보 : 프로젝트명, 개발자 목록, 라이센스 등의 정보 기술
  * 빌드 설정 : 소스, 리소스, lifecycle별 실행할 플러그인 등 빌드와 관련된 설정 기술
  * 빌드 환경 : 사용자 환경별로 달라질 수 있는 프로파일 정보 기술
  * POM 연관 정보 : 의존 프로젝트(모듈), 상위 프로젝트, 포함하고 있는 하위 모듈 등을 기술 

* 프로젝트 정보를 기술하는 태그
<table>
    <tr>
        <td>name</td>
        <td>프로젝트명</td>
    </tr>
    <tr>
        <td>url</td>
        <td>프로젝트 사이트 URL</td>
    </tr>
</table>

* POM 연관 정보 (프로젝트 간의 연관 정보)
<table>
    <tr>
        <td>groupId</td>
        <td>프로젝트의 그룹 ID 설정</td>
    </tr>
    <tr>
        <td>artifactId</td>
        <td>프로젝트의 Artifact ID 설정</td>
    </tr>
    <tr>
        <td>version</td>
        <td>버전 설정</td>
    </tr>
    <tr>
        <td>packaging</td>
        <td>패키징 타입 설정</td>
    </tr>
    <tr>
        <td>dependencies</td>
        <td>이 프로젝트에서 의존하는 다른 프로젝트 정보 기술</td>
    </tr>
    <tr>
        <td>dependency</td>
        <td>dependencies의 하위 태그로, 의존하는 프로젝트의 POM 정보 기술</td>
    <tr>
        <td>groupId, artifactId, version, scope</td>
        <td>dependency의 하위 태그로, 의존하는 프로젝트의 정보를 담는다.</td>
    </tr>
</table>

</hr>

<h2>의존 설정</h2>

* Maven을 사용하지 않을 경우, 개발자들은 코드에서 필요로 하는 library를 각각 다운로드 받아야 한다.   
  이 때, 하나의 library를 사용하기 위해서 추가적으로 필요한 다른 library가 있는 경우가 있는데,   
  maven을 사용하면 __코드에서 직접 사용하는 모듈에 대한 의존만 추가__ 하면 된다.
* 예를들어 commons-dbcp 모듈을 사용하고 싶다고 하자.
```xml
<dependency>
    <groupId>commons-dbcp</groupId>
    <artifactId>commons-dbcp</artifactId>
    <version>1.2.1</version>
</dependency>
```
* 위 코드만 기술하면 Maven은 commons-dbcp 뿐만 아니라 commons-dbcp가 의존하는   
  library들도 자동으로 처리해준다.
* Maven을 사용하지 않으면, 개발자는 의존하는 모듈들을 일일히 설치해야 한다.
<hr/>

<h3>dependency 태그 내의 scope 태그</h3>

* `scope` 태그는 의존하는 모듈이 언제 사용되는지를 결정한다.
* `scope` 태그에 올 수 있는 값들
  * compile : 컴파일 시 필요(기본값)
  * runtime : 런타임에 필요 (컴파일 시에는 필요하지 않지만 실행 시에는 필요함을 의미하며, 배포 시 포함)
  * provided : 컴파일 시 필요하지만 실제 runtime에서는 컨테이너 등에서 기본적으로 제공되는 모듈임을 의미   
    (배포 시 제외)
  * test : 테스트 코드를 컴파일 할 때 필요함을 의미한다.(테스트 시에 클래스패스에 포함되며, 배포 시 제외)
<hr/>

<h2>Remote repository와 Local Repository</h2>

* Maven은 컴파일이나 패키징 등의 작업 시에 필요한 plugin이나 pom.xml 파일의 `dependency`   
  에 설정한 모듈을 __maven 중앙 repository__ 에서 다운로드 받는다.   
  중앙 repository 주소 : http://repo1.maven.org/maven2/

* 원격 repository에서 다운로드 받은 모듈은 local repository에 저장된다.
* Local repository는 __[USER_HOME]/.m2/repository__ 폴더에 생성된다.
* 폴더의 구조는 다음과 같다.
```text
[groupId]/[artifactId]/[version]
```
  * 예를 들어 commons-dbcp 1.2.1버전의 경우 다음과 같이 생성된다.
  ```text
  [USER_HOME]/.m2/repository/commons-dbcp/commons-dbcp/1.2.1
  ```
* 일단 remote repository에서 파일을 다운로드해서 local에 저장하면, 그 뒤로는   
  local repository에 저장된 파일을 사용한다.
<hr/>

<h2>Maven Lifecycle과 plugin 실행</h2>

* Maven은 프로젝트의 빌드 lifecycle을 제공한다.
* lifecycle은 크게 __clean, build, site__ 3가지가 있으며, 각 lifecycle은 순서를 갖는   
  단계(phase)로 구성된다. 또한 각 단계별로 실행할 plugin goal이 정의되어 있어서 각 단계마다   
  알맞은 작업을 실행한다.

* Default lifecycle의 주요 단계(phase)
<table>
    <tr>
        <td>단계</td>
        <td>설명</td>
        <td>단계에 묶인 플러그인 실행</td>
    </tr>
    <tr>
        <td>generate-sources</td>
        <td>컴파일 과정에 포함될 소스를 생성한다.</td>
        <td>-</td>
    </tr>
    <tr>
        <td>process-sources</td>
        <td>필터와 같은 작업을 소스코드에 처리한다.</td>
        <td>-</td>
    </tr>
    <tr>
        <td>generate-resources</td>
        <td>패키지에 포함할 자원을 생성한다.</td>
        <td>-</td>
    </tr>
    <tr>
        <td>process-resources</td>
        <td>필터와 같은 작업을 자원 파일에 처리하고, 자원 파일을 클래스 출력 폴더에 복사한다.</td>
        <td>resources:resources</td>
    </tr>
    <tr>
        <td>compile</td>
        <td>소스코드를 컴파일해서 클래스 출력 폴더에 클래스를 생성한다.</td>
        <td>compiler:compile</td>
    </tr>
    <tr>
        <td>generate-test-sources</td>
        <td>테스트 소스 코드를 생성한다.</td>
        <td>-</td>
    </tr>
    <tr>
        <td>process-test-sources</td>
        <td>필터와 같은 작업을 테스트 소스 코드에 처리한다.</td>
        <td>resources:testResources</td>
    </tr>
    <tr>
        <td>test-compile</td>
        <td>테스트 소스 코드를 컴파일하여 테스트 클래스 출력 폴더에 클래스를 생성한다.</td>
        <td>compiler:testCompile</td>
    </tr>
    <tr>
        <td>test</td>
        <td>테스트를 실행한다.</td>
        <td>surefire:test</td>
    </tr>
    <tr>
        <td>package</td>
        <td>컴파일한 코드와 자원 파일들을 jar, war와 같은 배포 형식으로 패키징한다.</td>
        <td>jar : jar:jar, war : war:war</td>
    </tr>
    <tr>
        <td>install</td>
        <td>local repo에 패키지를 복사한다.</td>
        <td>install:install</td>
    </tr>
    <tr>
        <td>deploy</td>
        <td>생성한 패키지 파일을 remote repo에 등록하여 다른 프로젝트에서 사용할 수 있도록 한다.</td>
        <td>deploy:deploy</td>
    </tr>
</table>

* lifecycle의 특정 단계를 실행하려면 다음과 같이 `mvn [단계이름]` 명령어를 실행하면 된다.
```text
mvn test
mvn deploy
```
* lifecycle의 특정 단계를 실행하면 __그 단계의 앞에 위치한 모든 단계를 실행__ 한다. 예를 들어   
  `test` 단계를 실행하면 `test`단계를 실행하기에 앞서 `generate-sources`단계부터   
  `test-compile` 단계까지 각 단계를 순서대로 실행한다.

* 플러그인을 직접 실행할 수도 있다.
```text
mvn surefire:test
```
  * 단, 위와 같이 plugin goal을 직접 명시한 경우에는 해당 plugin만 실행하기에   
    lifecycle의 단계를 실행하지는 않는다.
