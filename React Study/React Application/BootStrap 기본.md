BootStrap 기본
======

* BootStrap은 CSS 프레임워크중 하나로, 수많은 오픈소스 프로젝트에서 사용된다.
* 또한, element의 일관적인 스타일을 위해 적용할 수 있는 CSS 클래스의 모음으로   
  이루어져있다.
<hr/>

<h2>기본 부트스트랩 클래스 적용</h2>

* 부트스트랩의 스타일은 className 프로퍼티를 통해 적용할 수 있다.
  * 이 프로퍼티는 class속성에 대응하며, 관련된 element들을   
    그룹화하기 위해 사용된다.
* 예시 코드
```HTML
<h4 className="bg-primary text-white text-center p-2 m-1">
    {message}
</h4>
```
* 위 코드는 className 프로퍼티를 이용해 공백으로 구분된 5개의 클래스를 할당했다.
* 각 클래스의 정의를 보자.
<table>
    <tr>
        <td>bg-primary</td>
        <td>element의 용도에 대한 시각적 단서를 제공하는 스타일 컨텍스트</td>
    </tr>
    <tr>
        <td>text-white</td>
        <td>element content의 텍스트에 흰색 스타일 적용</td>
    </tr>
    <tr>
        <td>text-center</td>
        <td>element content에 수평 기준의 가운데 정렬 적용</td>
    </tr>
    <tr>
        <td>p-2</td>
        <td>element content의 주변에 여백 추가</td>
    </tr>
    <tr>
        <td>m-1</td>
        <td>element의 주변에 여백 추가</td>
    </tr>
</table>

<hr/>

<h2>컨텍스트 클래스(Context Class)</h2>

* Style Context : 연관된 element들에게 일관된 스타일을 적용한다.

<table>
    <tr>
        <td>primary</td>
        <td>컨텐츠의 주된 영역을 나타낸다.</td>
    </tr>
    <tr>
        <td>secondary</td>
        <td>컨텐츠의 보조 영역을 나타낸다.</td>
    </tr>
    <tr>
        <td>success</td>
        <td>결과가 성공적임을 나타낸다.</td>
    </tr>
    <tr>
        <td>info</td>
        <td>추가 정보를 나타낸다.</td>
    </tr>
    <tr>
        <td>warning</td>
        <td>경고성 정보를 나타낸다.</td>
    </tr>
    <tr>
        <td>danger</td>
        <td>심각한 경고성 정보를 나타낸다.</td>
    </tr>
    <tr>
        <td>muted</td>
        <td>컨텐츠가 덜 강조되게, 즉 흐리게 한다.</td>
    </tr>
    <tr>
        <td>dark</td>
        <td>검은색으로 명암 대비를 높인다.</td>
    </tr>
    <tr>
        <td>white</td>
        <td>흰색으로 명암 대비를 높인다.</td>
    </tr>
</table>

* 위 예시 코드에서 __bg-primary__ 는 이 element의 배경색이   
  app의 기본 목적과 관련되어 있음을 나타낸다.
<hr/>

<h2>Margin, Padding</h2>

* Padding : element의 경계선과 그 안의 컨텐츠 사이의 여백
* Margin : element의 경계선과 둘러싼 element 사이의 여백

* 패딩을 적용하는 클래스는 p, 마진을 적용하는 클래스는 m이다.
* p 또는 m 뒤에 오는 문자
  * (1) t : 상측, b : 하측, l : 좌측, r : 우측
  * (2) - (하이픈)
  * (3) 0에서 5까지의 숫자   
    ((1)단계의 문자를 생략하면 4측면에 모두 적용됨)
<hr/>

<h2>부트스트랩으로 그리드 생성</h2>

* 부트스트랩은 최대 12개 column까지 각기 다른 유형의   
  grid layout을 지원하는 스타일 클래스를 제공한다.
* 예시 코드
```HTML
<div className="container-fluid p-4">
    <div className="row bg-info text-white p-4">
        <div className="col font-weight-bold">Value</div>
        <div className="col-6 font-weight-bold">Even?</div>
    </div>
    <div className="row bg-light p-2 border">
        <div className="col">{this.state.count}</div>
        <div className="col-6">{this.isEven(this.state.count)}</div>
    </div>
    <div className="row">
        <div className="col">
            <button className="btn btn-info m-2" onClick={this.handleClick}>Click Me</button>
        </div>
    </div>
</div>
```
* Bootstrap 그리드 시스템의 사용법
  * (1) 그리드의 최상위 div 요소에 container 클래스 할당   
    (가능한 공간을 모두 쓰고 싶다면 container-fluid 클래스 사용)
  * (2) 자식 div요소에 row 클래스 할당
  * (3) 그 자식 div요소에 col 클래스 할당

* 각 row는 12개의 컬럼을 갖는데, __col-n__ 형식으로   
  n에는 1~12의 값이 들어갈 수 있다.
  * n은 차지할 컬럼의 수를 의미한다.
  * ex) col-1 : 현재 element가 한 컬럼을 차지한다.
  * ex) col-2 : 현재 element가 두 컬럼을 차지한다.
  * n을 지정하지 않으면, 각 요소에 동일하게 나눈 수의 컬럼이 할당된다.
<hr/>

<h2>부트스트랩으로 테이블 스타일 적용</h2>

* 테이블을 위한 주요 부트스트랩 클래스
<table>
    <tr>
        <td>table</td>
        <td>table요소와 그 row에 일반적인 스타일 적용</td>
    </tr>
    <tr>
        <td>table-striped</td>
        <td>table의 row마다 교대로 배경색을 달리한다.(줄무늬)</td>
    </tr>
    <tr>
        <td>table-bordered</td>
        <td>모든 row, column에 테두리 적용</td>
    </tr>
    <tr>
        <td>table-sm</td>
        <td>테이블의 공간을 줄여 더 촘촘한 레이아웃 생성</td>
    </tr>
</table>

* 주의 : 테이블 정의 시에는 __tbody,thead__ 등을 생략하지 말자.   
  즉, element의 풀 세트를 사용하자.(예기치 못한 결과 방지)
<hr/>

<h2>부트스트랩으로 form 스타일 적용</h2>

* 부트스트랩은 app의 다른 요소와 일관성을 유지할 수 있게끔   
  form 요소를 위한 스타일 클래스를 제공한다.
* 예시 코드
```HTML
<div className="m-2">
    <div className="form-group">
        <label>Name:</label>
        <input className="form-control" />
    </div>
    <div className="form-group">
        <label>City:</label>
        <input className="form-control"/>
    </div>
</div>
```
* form에 기본 스타일을 적용하려면 label과 input요소를 포함하는   
  div요소에 form-group 클래스를 지정하면 된다.
