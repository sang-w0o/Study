Layout
======

* 안드로이드에서 제공하는 대표적인 레이아웃들은 다음과 같다.

<table>
    <tr>
        <td>제약 레이아웃(ConstraintLayout)</td>
        <td>제약 조건 기반 모델, 제약 조건을 사용해 화면을 구성하며 android studio에서 자동으로 설정하는 default layout이다.</td>
    </tr>
    <tr>
        <td>리니어 레이아웃(LinearLayout)</td>
        <td>Box 모델, 한 쪽 방향으로 차례대로 View를 추가하며 화면을 구성하며 View가 차지할 수 있는 사각형 영역을 할당한다.</td>
    </tr>
    <tr>
        <td>상대 레이아웃(RelativeLayout)</td>
        <td>Rule 기반 모델, 부모 컨테이너나 다른 View와의 상대적 위치로 화면을 구성하며 제약 레이아웃에 밀려 권장되지 않는다.</td>
    </tr>
    <tr>
        <td>프레임 레이아웃(FrameLayout)</td>
        <td>Single 모델, 가장 상위에 있는 하나의 view 또는 viewgroup만 보여주며 여러 개의 view가 들어가면 중첩하여 쌓게 된다.
            가장 단순하지만 여러 개의 view를 중첩한 후 각 view를 전환하여 보여주는 방식으로 자주 사용된다.</td>
    </tr>
    <tr>
        <td>테이블 레이아웃(TableLayout)</td>
        <td>Grid 모델, 격자 모양의 배열을 사용하여 화면을 구성하며 HTML에서 많이 사용하는 정렬 방식과 유사하지만 많이 사용되지는 않는다.</td>
    </tr>
</table>

* LinearLayout은 Box 모델을 사용하는 레이아웃으로, View가 차지하는 영역을 Box로 보고, 한 쪽 방향으로 쌓는다.   
  가로 방향은 __Horizontal__, 세로 방향은 __Vertical__ 로 지정한다.

* RelativeLayout은 부모 컨테이너, 즉 부모 레이아웃과의 상대적 위치 또는 같은 레이아웃에 들어있는 view와의 상대적 위치를   
  이용해 화면을 배치한다.

* FrameLayout은 가장 위에 있는 하나의 View만 화면에 보여준다. 그 안에 추가된 여러 개의 View, Viewgroup들은 그 아래에   
  중첩하여 쌓인다.

* SrcollView : 하나의 view나 viewgroup을 넣을 수 있고, 어떤 view의 내용물이 화면보다 크면 scroll을 만들어준다.   
  따라서 View를 배치하는 목적을 가진 레이아웃이라기보단 view를 담는 viewgroup의 역할을 한다고 볼 수 있다.

<hr/>

<h3>View 영역 알아보기</h3>

* View가 레이아웃에 추가될 때는 __보이지 않는 View의 테두리(Border)가 있다__.   
  * Padding : 내용물과 Border 사이의 간격
  * Margin : Border와 view의 영역(Box) 사이의 간격
* View와 padding의 값을 조절할 수 있는 속성은 다음과 같다.
```text
layout_margin, layout_marginTop, layout_marginBottom, layout_marginLeft, layout_marginRight
padding, paddingTop, paddingBottom, paddingLeft, paddingRight
```
* layout_margin 또는 padding을 사용하면 각각의 상하좌우 값을 한번에 조절할 수 있다.

<hr/>

<h3>View의 배경색</h3>

* XML Layout에서 색상 지정시에는 #ARGB 값을 지정한다.
* 배경을 지정하는 속성은 __background__ 속성이다.
<hr/>

<h2>Linear Layout 사용하기</h2>

* LinearLayout의 필수 속성에는 `android:layout_width`, `android:layout_height`, `android:orientation` 이다.   
* 