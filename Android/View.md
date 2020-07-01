View
======

* View : 일반적으로 컨트롤이나 위젯으로 불리는 UI 구성 요소
* ViewGroup : View들을 포함하고 있는 그룹으로, ViewGroup내에서 View의 위치를 지정할 수 있다.   
  ViewGroup은 View를 상속하여, ViewGroup을 View처럼 다룰 수도 있다.
<hr/>

<h3>Widget과 Layout으로 구분되는 View</h3>

* Widget : View 중에서 일반적인 컨트롤의 역할을 하는 것.
* Layout : ViewGroup 중에서 내부에 View들을 포함하며, 그것들을 배치하는 역할을 하는 것.
  * Layout은 Layout이 담고 있는 Widget들이 어디에 배치되어야 할지를 결정한다. 따라서 Layout 내에   
    Layout을 넣으면 각각의 Layout내에서 버튼과 같은 widget의 위치를 잡을 수 있다.
<hr/>

<h3>View의 크기 속성</h3>

* View는 화면의 일정 영역을 차지하기 때문에 모든 View는 반드시 __크기 속성을 가져야 한다.__
* View들은 XML에 태그로 정의할 수 있는데, 태그 내의 속성들은 공백이나 개행으로 구별된다.   
* 속성들 중에는 대부분 `android:` 가 앞에 붙는데, 이 속성은 __안드로이드의 기본 API에서 정의한 속성__ 이다.
  * `android:layout_width` : View의 가로 크기
  * `android:layout_height` : View의 세로 크기

* `android:layout_width`와 `android:layout_height`는 다음 값들이 들어갈 수 있다.

<table>
    <tr>   
        <td>wrap_content</td>
        <td>view에 들어있는 내용물의 크기에 자동으로 맞춘다.</td>
    </tr>
    <tr>   
        <td>match_parent</td>
        <td>view를 담고 있는 viewgroup의 여유 공간을 가득 채운다.</td>
    </tr>
    <tr>   
        <td>숫자로 크기 지정</td>
        <td>숫자를 사용해 크기를 지정한다. (항상 px, dp 등의 단위를 지정해야 한다.)</td>
    </tr>
</table>

<hr/>

<h3>Layout의 기초</h3>

* 화면에 들어가는 View를 배치할 때에는 Layout을 사용한다. Android에서는 기본적인 layout을   
  __Constraint Layout(제약 레이아웃)__ 으로 자동 설정한다.
<hr/>

<h3>제약 조건의 이해</h3>

* 제약 레이아웃의 가장 큰 특징은 __View의 크기와 위치 결정 시 제약 조건을 사용한다__ 는 것이다.   
  제약 조건이란, view가 레이아웃 내의 다른 요소와 어떻게 연결되는지 알려주는 것으로,   
  __View의 연결점(Anchor point)와 대상(Target)을 연결__ 한다.
* Target은 다음 것들이 될 수 있다.
  * 같은 부모 레이아웃 내에 들어있는 다른 view의 연결점
  * 부모 레이아웃의 연결점
  * 가이드라인
* Anchor Point는 다음 것들이 될 수 있다.
  * 위쪽(Top), 아래쪽(Bottom), 왼쪽(Left, start), 오른쪽(Right, End)
  * 가로축의 가운데(CenterX), 세로축의 가운데(CenterY)
  * BaseLine (Text를 보여주는 view인 경우에만 해당)

* Margin : 연결점과 target 사이의 거리