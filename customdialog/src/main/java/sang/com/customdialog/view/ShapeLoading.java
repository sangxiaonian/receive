package sang.com.customdialog.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;

import sang.com.customdialog.utils.DeviceUtils;

/**
 * Description：
 *
 * @Author：桑小年
 * @Data：2016/11/21 14:13
 */
public class ShapeLoading extends View {

    private Paint mPaint, textPaint;
    private Path mPath;
    private int mWidth, mHeight;
    private final int SQUARE = 0;//正方形
    private final int CIRCLE = 1;//圆形
    private final int TRIGON = 2;//三角形

    private int bitWidth, bitHight, minWidth, textSize;
    private float centerX, centerY;
    private int state = 0;
    private PointF center;
    private int gap;
    float radiu = 2;
    float angle = 0;
    private Rect textRect;

    private String msg = "加载中...";
    int color;

    private int[] colors = {Color.parseColor("#68A0f3"), Color.parseColor("#EC6E58"), Color.parseColor("#36D088")};

    private int[] blowColors;

    public ShapeLoading(Context context) {
        super(context);
        initView(context, null, 0);
    }

    public ShapeLoading(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context, attrs, 0);
    }

    public ShapeLoading(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context, attrs, defStyleAttr);
    }

    private void initView(Context context, AttributeSet attrs, int defStyleAttr) {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPath = new Path();
        textRect = new Rect();
        minWidth = DeviceUtils.dp2px(getContext(), 180);
        center = new PointF(0, 0);
        gap = DeviceUtils.dp2px(getContext(), 10);
        textSize = DeviceUtils.sp2px(getContext(), 20);

        ColorDrawable background = (ColorDrawable) getBackground();

        color = Color.WHITE;
        if (background != null) {
            color = background.getColor();

        }

        color = Color.WHITE;


    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mWidth = Math.max(minWidth, getMeasuredWidth());
        mHeight = Math.max(minWidth, getMeasuredHeight());

        centerX = center.x = mWidth / 2;
        centerY = center.y = mHeight / 2;
        bitHight = mWidth / 3;
        bitWidth = mHeight / 3;
        setMeasuredDimension(mWidth, mHeight);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mPaint.setShader(null);

        Bitmap bitmap = creatBitmap(state);
        canvas.drawBitmap(bitmap, centerX - bitmap.getWidth() / 2, center.y - bitmap.getHeight() / 2, mPaint);
        RadialGradient radio = new RadialGradient(centerX, mHeight * 3 / 5, radiu + gap, Color.parseColor("#aaaaaa"), color, Shader.TileMode.MIRROR);

        mPaint.setShader(radio);
        canvas.drawOval(new RectF(centerX - radiu, centerY + bitmap.getHeight() / 2, centerX + radiu, centerY + bitmap.getHeight() * 3 / 5), mPaint);
        drawText(canvas);

    }

    private void drawText(Canvas canvas) {
        textPaint.setTextSize(textSize);
        textPaint.setColor(Color.parseColor("#888888"));
        textPaint.getTextBounds(msg, 0, msg.length(), textRect);
        int textLen = textRect.width();
        canvas.drawText(msg, (mWidth - textLen) / 2, (centerY + bitHight), textPaint);

    }


    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

    }






    ValueAnimator valueAnimator;

    public void creatAnimation() {

        if (valueAnimator != null && valueAnimator.isRunning()) {
            return;
        }
        valueAnimator = ValueAnimator.ofFloat(1.0f, 0.0f, 1.0f);
        valueAnimator.setDuration(1000);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();

                center.y = value * (mHeight / 2) + (bitHight / 2) * (1 - value);
                radiu = value * bitHight / 2 + gap;
                angle = 180 * animation.getAnimatedFraction();


                postInvalidate();
            }


        });

        valueAnimator.setInterpolator(new LinearInterpolator());
        valueAnimator.addListener(new AnimatorListenerAdapter() {


            @Override
            public void onAnimationRepeat(Animator animation) {
                super.onAnimationRepeat(animation);
                state = (state + 1) % 3;
              if (getWindowVisibility()==GONE){
                    valueAnimator.end();
                    valueAnimator.cancel();
                }
            }
        });

        valueAnimator.setRepeatCount(Integer.MAX_VALUE);
        valueAnimator.start();


    }


    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);

        if (visibility==VISIBLE){
            creatAnimation();
        }else if (visibility==GONE){
            valueAnimator.cancel();
        }


    }

    private Bitmap creatBitmap(int state) {
        Bitmap bitmap = Bitmap.createBitmap(bitWidth, bitHight, Bitmap.Config.ARGB_8888);
        int bitWidth = this.bitWidth * 2 / 3;
        int bitHight = this.bitHight * 2 / 3;
        int len = (this.bitHight - bitHight) / 2;
        int radio = Math.min(bitHight, bitWidth) / 2;
        Canvas canvas = new Canvas(bitmap);
        canvas.rotate(angle, bitmap.getWidth() / 2, bitmap.getHeight() / 2);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(colors[state]);
        switch (state) {
            case SQUARE:
                mPath.addRect(len, len, this.bitWidth - len, this.bitHight - len, Path.Direction.CCW);
                break;
            case CIRCLE:
                mPath.addCircle(this.bitWidth / 2, this.bitHight / 2, radio, Path.Direction.CCW);
                break;
            case TRIGON:
                mPath.moveTo(len, this.bitHight - len);
                mPath.lineTo(this.bitWidth - len, this.bitHight - len);
                mPath.lineTo(this.bitWidth / 2, this.bitHight - len - (this.bitHight - 2 * len) * 0.87f);
                mPath.close();
                break;
        }
        canvas.drawPath(mPath, mPaint);
        mPath.reset();
        return bitmap;
    }

    public void setText(String msg) {
        this.msg=msg;
    }



}
