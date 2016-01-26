package demo.hongbaodemo;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements RedBag.RedBagObserver {

    private RelativeLayout rl_layout;
    public TextView tv_rb_num;
    private int height;
    private int width;
    private RedBag rb;
    private HeatBeat mHeatBeat = new HeatBeat();
    private int count = 0;
    private int second = 1000;
    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rl_layout = (RelativeLayout) findViewById(R.id.root_content);
        tv_rb_num = (TextView) findViewById(R.id.rb_num);

        rl_layout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                height = rl_layout.getHeight();
                width = rl_layout.getWidth();
                rl_layout.getViewTreeObserver().removeOnGlobalLayoutListener(this);//一定要移除掉监听，否则的话会一直回调
            }
        });
    }

    @Override
    protected void onResume() {
        mHeatBeat.start();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mHeatBeat.stop();
                Toast toast = Toast.makeText(MainActivity.this, "恭喜您抢到" + count + "个红包", Toast.LENGTH_SHORT);
                toast.show();
            }
        },60000);
        super.onResume();
    }

    @Override
    protected void onPause() {
        mHeatBeat.stop();
        super.onPause();
    }

    @Override
    public void count() {
        tv_rb_num.setText("" + ++count);
    }

    private class HeatBeat extends Handler implements Runnable {
        @Override
        public void run() {
            if (height != 0 && width != 0) {
                //rb指向上一个红包，说明上一个红包没有在飞，那就移除掉上一个红包，然后重新生成一个红包
                if (rb != null && !rb.isflying) {
                    rl_layout.removeView(rb);
                }
                rb = new RedBag(MainActivity.this, height, width, rl_layout, MainActivity.this);
                rl_layout.addView(rb);
            }
            if (second > 500) {
                postDelayed(this, second -= 8);
            }else {
                postDelayed(this, second);
            }
        }

        public void start() {
            stop();
            postDelayed(this, 1000);
        }

        public void stop() {
            removeCallbacks(this);
            rl_layout.removeAllViews();
        }
    }

}
