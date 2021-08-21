View에 Graphic 그리기
======

<h2>View에 Graphic 그리기</h2>

* 그래픽 기능을 확인하는 가장 쉬운 방법은 `View` 클래스를 상속한 클래스를 생성한 후, 그 클래스에 직접 그래픽을 그리는 것이다.   
  `View` 클래스 상속 시 필수로 정의해야하는 생성자는 아래와 같다.
```java
public class MyView extends MyView {
    public MyView(Context context ){}
    public MyView(Context context, AttributeSet attrs) {}
}
```

* `onDraw()` 메소드는 View가 화면에 그려질 때 자동으로 호출된다. 따라서 View에 그래픽을 그리기 위해서는 `onDraw()`내에서   
  원하는 그래픽을 그리면 된다.
* `onTouchEvent()`는 터치 이벤트를 처리하는 일반적인 방법을 제공한다.

* 그래픽을 그릴 때 필요한 주요 클래스들은 다음과 같다.

<table>
    <tr>
        <td>Canvas</td>
        <td>View의 표면에 직접 그릴 수 있도록 만들어주는 객체로, 그래픽을 그리기 위한 메소드들이 정의되어 있다.</td>
    </tr>
    <tr>
        <td>Paint</td>
        <td>그래픽을 그리기 위해 필요한 색상 등의 속성을 담고 있다.</td>
    </tr>
    <tr>
        <td>Bitmap</td>
        <td>픽셀로 구성된 이미지로 메모리에 그래픽을 그리는데 사용한다.</td>
    </tr>
    <tr>
        <td>Drawable</td>
        <td>사각형, 이미지 등의 그래픽 요소가 객체로 정의되어 있다.</td>
    </tr>
</table>

* 아래는 두 개의 사각형에 색상을 지정하여 채운 후 선으로 그리는 과정을 구현한 코드이다.
```java
public class CustomView extends View {
    
    Paint paint;
    
    public CustomView(Context context) {
        super(context);
        init(context);
    }
    
    public CustomView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }
    
    private void init(Context context) {
        paint = new Paint();
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        // 첫 번째 사각형 그리기
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.RED);
        canvas.drawRect(10,10,100,100,paint);
        
        // 첫 번째 사각형을 Stroke 스타일로 설정
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2.0F); // 선의 두께 지정하는 메소드
        paint.setColor(Color.GREEN);
        canvas.drawRect(10, 10, 100, 100, paint);
        
        // 두 번째 사각형을 Fill 스타일로 지정
        paint.setStyle(Paint.Style.FILL);
        paint.setARGB(128, 0, 0, 255);
        canvas.drawRect(120, 10, 210, 100, paint);
        
        // 두 번째 사각형을 Stroke 스타일로 설정하고 PathEffect 적용
        // DashPathEffect : 점선으로 그릴 때 사용, 아래에서는 선이 그려지는 부분과 안그려지는 부분이
        // 각각 5의 크기로 지정되어 있다.
        DashPathEffect dashEffect = new DashPathEffect(new float[]{5,5} , 1);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3.0F);
        paint.setPathEffect(dashEffect);
        paint.setColor(Color.GREEN);
        canvas.drawRect(120, 10, 210, 100, paint);
        
        paint = new Paint();
        
        // 첫 번째 원에 색상 적용
        paint.setColor(Color.MAGENTA);
        canvas.drawCircle(50, 160, 40, paint);
        
        // 두 번째 원에 AntiAlias 지정
        paint.setAntiAlias(true);
        canvas.drawCircle(160, 160, 40, paint);
    }
}
```
<hr/>