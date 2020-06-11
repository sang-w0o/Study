프로퍼티, 상태, 컴포넌트 트리
======

<h2>#1. 프로퍼티 검증</h2>

* JS는 변수값의 데이터타입이 바뀔 수 있다. 따라서 변수 타입을 비효율적으로   
  다루면 디버깅 시 시간이 매우 오래 걸린다.
* React Component는 property type을 지정하고 검증하는 방법을 제공한다.
* 이 기능을 사용하면 디버깅 시간을 줄일 수 있으며, property에 잘못된   
  타입의 값을 지정하면 경고가 표시된다.
* 설치하기
```sh
npm install prop-types --save
```
* prop-types를 추가하면 다음의 검증기를 이용할 수 있다.
<table>
    <tr>
        <td>Arrays</td>
        <td>React.PropTypes.array</td>
    </tr>
    <tr>
        <td>Boolean</td>
        <td>React.PropTypes.bool</td>
    </tr>
    <tr>
        <td>Functions</td>
        <td>React.PropTypes.func</td>
    </tr>
    <tr>
        <td>Numbers</td>
        <td>React.PropTypes.number</td>
    </tr>
    <tr>
        <td>Objects</td>
        <td>React.PropTypes.object</td>
    </tr>
    <tr>
        <td></td>Strings
        <td>React.PropTypes.string</td>
    </tr>
</table>

