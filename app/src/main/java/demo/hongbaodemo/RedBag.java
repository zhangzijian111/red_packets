package demo.hongbaodemo;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import java.util.Random;

/**
 * 注意
 * 1.如果不覆写onmeasure，那么画布的范围是整个屏幕（new出来的默认是matchparent），如果覆写了的话画布就是一个bitmap的宽高
 * 2.构造函数必须要设置根布局和根布局的宽高
 * 3.需要设置一个抢到红包个数的监听器
 **/

public class RedBag extends View {

    private float alpha = 255;  //透明度
    private float scale = 1;    //缩放比例
    private int degree = 0;     //旋转角度
    private float nowX;
    private float nowY;
    private float perX;
    private float perY;
    private float perAlpha;
    private float scaleper;

    private int width;//父控件的宽
    private int height;//父控件的高
    private Bitmap bitmap;
    private int bitmapWidth;
    private int bitmapHeight;
    private Paint paint;
    public boolean isflying = false;
    private Activity mContext;
    private ViewGroup rootView;

    public RedBag(Context context) {
        this(context, null);
    }

    public RedBag(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * 构造函数
     * @param context   当前的activity
     * @param height    红包根布局的高度
     * @param width     红包根布局的宽度
     * @param viewGroup 红包的根布局
     * @param observer  抢到红包的监听器
     */
    public RedBag(Context context, int height, int width, ViewGroup viewGroup,RedBagObserver observer) {
        this(context);
        mContext = (Activity) context;
        this.width = width;
        this.height = height;
        this.rootView = viewGroup;
        this.observer = observer;
        initEvent();
        initData();
    }

    private void initData() {

        bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
        bitmapWidth = bitmap.getWidth();
        bitmapHeight = bitmap.getHeight();

        paint = new Paint();
        //随机的坐标
        Random random = new Random();
        nowX = random.nextInt(width - bitmapWidth);
        nowY = random.nextInt(height - bitmapHeight);
        //分成100份
        float unit = 100;
        perAlpha = alpha / unit;
        perX = nowX / unit;
        perY = nowY / unit;
        scaleper = scale / unit;

    }

    private void initEvent() {
        /*this.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //这是这个view的点击事件，而这个view是画布画出来的，画布默认是matchparent的（也必须是这样才能让图片画出来到随机位置），
                //而点击事件只需要设置在固定的图片的区域，只能在ontouch上面设置了
            }
        });*/

        this.setClickable(true);//不设置无法响应点击事件
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Matrix matrix = new Matrix();
        // 缩放
        matrix.postScale(scale, scale);
        // 旋转
        matrix.postRotate(degree, nowX + bitmapWidth / 2, nowY + bitmapHeight / 2);
        // 创建
        Bitmap dstbmp = Bitmap.createBitmap(bitmap, 0, 0, bitmapWidth, bitmapHeight, matrix, true);
        // 绘制
        canvas.drawBitmap(dstbmp, nowX, nowY, paint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
                if (event.getX() <= nowX + bitmap.getWidth() && event.getX() >= nowX) {
                    if (event.getY() <= nowY + bitmap.getHeight() && event.getY() >= nowY) {
                        new MyThread().start();
                        observer.count();//抢到红包的回调
                        RedBag.this.removeObserver();
                    }
                }
                break;
        }
        return super.onTouchEvent(event);
    }

    //循环动画
    class MyThread extends Thread {
        @Override
        public void run() {
            while (true) {
                if (nowX <= 0 || nowY <= 0) {
                    paint.setAlpha((int) (alpha = 0));
                    degree = 0;
                    scale = 0.5f;
                    postInvalidate();
                    isflying = false;
                    mContext.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            rootView.removeView(RedBag.this);
                            Log.i("zzj", "动画结束，移除红包");
                        }
                    });
                    break;
                }
                paint.setAlpha((int) (alpha -= perAlpha));//用画笔设置透明度
                nowX -= perX;
                nowY -= perY;
                degree += 10;
                if (scale > 0.5f) {
                    scale = scale - scaleper;
                } else {
                    scale = 0.5f;
                }
                postInvalidate();
                isflying = true;
                SystemClock.sleep(10);
            }
        }
    }

    /*@Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int width;
        int height ;
        if (widthMode == MeasureSpec.EXACTLY)
        {
            width = widthSize;
        } else
        {
            int desired = (int) (getPaddingLeft() + bitmap.getWidth() + getPaddingRight());
            width = desired;
        }
        if (heightMode == MeasureSpec.EXACTLY)
        {
            height = heightSize;
        } else
        {
            int desired = (int) (getPaddingTop() + bitmap.getHeight() + getPaddingBottom());
            height = desired;
        }
        setMeasuredDimension(width, height);
    }*/

    /**
     * 定义上传观察者
     */
    public interface RedBagObserver {
        void count();
    }

    RedBagObserver observer;

    /**
     * 设置抢到红包的监听
     * @param o
     */
    public synchronized void setObserver(RedBagObserver o) {
        if (o == null)
            throw new NullPointerException();
        this.observer = o;
    }

    public RedBagObserver getObserver() {
        return observer;
    }

    public void removeObserver() {
        observer = null;
    }

}
