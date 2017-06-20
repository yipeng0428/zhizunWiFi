package com.zhizun.zhizunwifi.view;

import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.Path.Direction;
import android.graphics.Shader.TileMode;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import com.zhizun.zhizunwifi.R;

/**
 * 仿iphone带进度的进度条，线程安全的View，可直接在线程中更新进度
 * @author xiaanming
 *
 */
@SuppressLint("DrawAllocation") public class RoundProgressBar extends View {
	/**
	 * 画笔对象的引用
	 */
	private Paint paint;

	/**
	 * 圆环的颜色
	 */
	private int roundColor;

	/**
	 * 圆环进度的颜色
	 */
	private int roundProgressColor;

	/**
	 * 底图颜色
	 */
	private int roundBasebackground;

	/**
	 * 外部圆弧颜色
	 */
	private int roundArcOutColor;

	/**
	 * 刻度颜色
	 */
	private int roundScaleColor;

	/**
	 * 刻度字体颜色
	 */
	private int roundScaleTextColor;

	/**
	 * 进度背景颜色
	 */
	private int roundProgressBackgroundColor;

	/**
	 * 中间进度百分比的字符串的颜色
	 */
	private int textColor;

	/**
	 * 中间进度百分比的字符串的字体
	 */
	private float textSize;

	/**
	 * 圆环的宽度
	 */
	private float roundWidth;

	/**
	 * 画圆环弧渐变颜色
	 */
	LinearGradient linearGradient;

	/**
	 * 最大进度
	 */
	private int max;

	/**
	 *
	 */
	private float startAngleDefalut = 152.5f;
	private float sweepAngleDefault = 235.5f;

	int out_inSpace = 40;

	/**
	 * 指针图
	 */
//	Bitmap pointerBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.btn_tspeed_pointer);
//	private float pointerDegree = 180f;

	Bitmap pointerBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.speed_pointer);
	private float pointerDegree = 145f;

	/**
	 * 当前进度
	 */
	private float progress = 0;
	/**
	 * 是否显示中间的进度
	 */
	private boolean textIsDisplayable;

	/**
	 * 进度的风格，实心或者空心
	 */
	private int style;

	public static final int STROKE = 0;
	public static final int FILL = 1;

	/**
	 * 是否画外圆弧
	 */
	private boolean isOutAcrDisplay = true;

	float[] maxSpeedValues = {256 * 1024, 512 * 1024, 768 * 1024, 1 * 1024 * 1024, 10 * 1024 * 1024};//10M
//	float maxSpeedValue = 10 * 1024 * 1024;//10M
	/**
	 * [][0]:均分str
	 * [][1]：与前一段距离均分等份
	 */
	String[][] alertValue_Lenght = {{"0Kb", "0"}, {"", "8"}, {"", "8"}, {"", "8"}, {"", "8"}, {"", "9"}};

	int centre = 0; //获取圆心的x坐标
	int outRadius; //圆环的半径
	int radius; //圆环的半径
	int radius2; //无圆环的半径

	public RoundProgressBar(Context context) {
		this(context, null);
	}

	public RoundProgressBar(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public RoundProgressBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		paint = new Paint();


		TypedArray mTypedArray = context.obtainStyledAttributes(attrs,
				R.styleable.RoundProgressBar);

		//获取自定义属性和默认值
//		roundColor = mTypedArray.getColor(R.styleable.RoundProgressBar_roundColor, Color.RED);
		roundColor = mTypedArray.getColor(R.styleable.RoundProgressBar_roundColor, Color.TRANSPARENT);
		roundProgressColor = mTypedArray.getColor(R.styleable.RoundProgressBar_roundProgressColor, Color.GREEN);
		roundBasebackground = mTypedArray.getColor(R.styleable.RoundProgressBar_roundBaseBackground, Color.WHITE);
		roundScaleTextColor = mTypedArray.getColor(R.styleable.RoundProgressBar_roundScaleTextColor, Color.GREEN);
		roundArcOutColor = mTypedArray.getColor(R.styleable.RoundProgressBar_roundArcOutColor, Color.GREEN);
		roundScaleColor = mTypedArray.getColor(R.styleable.RoundProgressBar_roundScaleColor, Color.WHITE);
		roundProgressBackgroundColor = mTypedArray.getColor(R.styleable.RoundProgressBar_roundProgressBackgroundColor, 0xAE000000);
		textColor = mTypedArray.getColor(R.styleable.RoundProgressBar_roundTextColor, Color.GREEN);
		textSize = mTypedArray.getDimension(R.styleable.RoundProgressBar_roundTextSize, 15);
		roundWidth = mTypedArray.getDimension(R.styleable.RoundProgressBar_roundWidth, 5);
		max = mTypedArray.getInteger(R.styleable.RoundProgressBar_max, 100);
		textIsDisplayable = mTypedArray.getBoolean(R.styleable.RoundProgressBar_textIsDisplayable, true);
		style = mTypedArray.getInt(R.styleable.RoundProgressBar_style, 0);

		mTypedArray.recycle();

//		int[] colors = {0xFF6AB8F2, 0xFF3698E6, 0xFF003C6C};
//		linearGradient = new LinearGradient(0, 0, getWidth(), roundWidth, colors, null, TileMode.MIRROR);
	}

	public void initData(){
		centre = getWidth()/2; //获取圆心的x坐标
		outRadius = centre; //圆环的半径
		radius = (int) (centre - textSize - out_inSpace - roundWidth/2); //圆环的半径
		radius2 = (int) (centre - textSize - out_inSpace); //无圆环的半径

		int[] colors = {0xFF6AB8F2, 0xFF3698E6, 0xFF007AD9};
		linearGradient = new LinearGradient(0, 0, (radius) * 2, (radius) * 2, colors, null, TileMode.MIRROR);

//		RadialGradient radialGradientShader=new RadialGradient(centerX,centerY, radius, gradientColors, gradientPositions, TileMode.CLAMP);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		if(centre == 0)
			initData();

		paint.setStyle(Paint.Style.STROKE); //设置空心
		paint.setStrokeWidth(2); //设置圆环的宽度
		paint.setAntiAlias(true);  //消除锯齿
		paint.setTextSize(textSize);

		float unitOutSize = textSize / 4 * 3;
		RectF ovalOut = new RectF(unitOutSize, unitOutSize, getWidth() - unitOutSize, getHeight() - unitOutSize);
		float unitScaleDegreeOutSize1 = 1.25f * textSize;
		RectF ovalPaintDegreeOut1 = new RectF(unitScaleDegreeOutSize1, unitScaleDegreeOutSize1, getWidth() - unitScaleDegreeOutSize1, getHeight() - unitScaleDegreeOutSize1);
		float unitScaleDegreeOutSize2 = 1.5f * textSize;
		RectF ovalPaintDegreeOut2 = new RectF(unitScaleDegreeOutSize2, unitScaleDegreeOutSize2, getWidth() - unitScaleDegreeOutSize2, getHeight() - unitScaleDegreeOutSize2);


		Path path = new Path();
		path.addCircle(centre, centre, outRadius, Direction.CW);
		float[] textWidths = new float[alertValue_Lenght.length];
		double circleLen = Math.PI * outRadius * 2;
		float preBottomDegree = 0;
		double prePointDegree = -1;
		for(int i = 0; i < textWidths.length; i++){
			textWidths[i] = paint.measureText(alertValue_Lenght[i][0]);
			double curDegree = ((double)i / (textWidths.length - 1) * sweepAngleDefault + startAngleDefalut) % 360;
			double curDegreeTop = (12 - textWidths[i] / 2) / circleLen * 360 + curDegree;
			if(curDegreeTop < 0)
				curDegreeTop += 360;
			float hOffset = (float) (circleLen * (curDegreeTop / 360));

			if(isOutAcrDisplay && preBottomDegree > 0){
				double sweepAngleDegree = (curDegreeTop - preBottomDegree) % 360;
				if(sweepAngleDegree < 0)
					sweepAngleDegree += 360;

				paint.setStyle(Paint.Style.STROKE);
				paint.setColor(roundArcOutColor);  //设置圆弧颜色
				if(alertValue_Lenght[i][0].equals(""))
					canvas.drawArc(ovalOut, preBottomDegree, (float)sweepAngleDegree + 2, false, paint);  //根据进度画圆弧
				else
					canvas.drawArc(ovalOut, preBottomDegree + 2, (float)sweepAngleDegree - 4, false, paint);  //根据进度画圆弧
			}

			paint.setStyle(Paint.Style.FILL);
			paint.setColor(roundScaleColor);  //设置刻度的颜色

			if(curDegree < 0)
				curDegree += 360;

			if(i > 0){
				int count = Integer.parseInt(alertValue_Lenght[i][1]);
				double RangeDegree = curDegree - prePointDegree;
				if(RangeDegree < 0)
					RangeDegree += 360;
				double unitDegree = RangeDegree / count;

				for(int j = 1; j < count; j++){
					RectF ScaleOval;
					float moveDegree;
					if(count % 2 != 0 || j % 2 == 0){
						moveDegree = 1f;
//						ScaleOval = ovalOut;
						ScaleOval = ovalPaintDegreeOut2;
					} else {
						moveDegree = 0.1f;
						ScaleOval = ovalPaintDegreeOut2;
					}
					float startAngleDegree = (float)(prePointDegree + j * unitDegree);
					canvas.drawArc(ScaleOval, startAngleDegree - moveDegree / 2, moveDegree, true, paint);  //根据进度画圆弧刻度
				}
			}
			canvas.drawArc(ovalPaintDegreeOut1, (float)curDegree - 0.5f, 1f, true, paint);  //点刻度
			prePointDegree = curDegree;

			preBottomDegree = (float)(textWidths[i] / circleLen * 360) + (float)curDegreeTop;

			if(!alertValue_Lenght[i][0].equals("")){
				paint.setColor(roundScaleTextColor);  //设置进度的颜色
				canvas.drawTextOnPath(alertValue_Lenght[i][0], path, hOffset, textSize, paint);
			}
		}


		/**
		 * 画进度百分比
		 */
		float percent = progress / max;  //中间的进度百分比，先转换成float在进行除法运算，不然都为0

		/**
		 * 画圆弧 ，画圆环的进度
		 */

		//设置进度是实心还是空心
		paint.setStrokeWidth(roundWidth); //设置圆环的宽度
		paint.setStrokeCap(Paint.Cap.ROUND); // 圆弧两头是一个半圆
		RectF oval = new RectF(centre - radius, centre - radius, centre
				+ radius, centre + radius);  //用于定义的圆弧的形状和大小的界限

		paint.setStyle(Paint.Style.FILL);
		paint.setColor(roundBasebackground);
		RectF ovalFill = new RectF(centre - radius - roundWidth / 2, centre - radius - roundWidth / 2, centre
				+ radius + roundWidth / 2, centre + radius + roundWidth / 2);
		canvas.drawArc(ovalFill, startAngleDefalut, sweepAngleDefault, false, paint);  //根据进度画圆弧

		paint.setColor(roundProgressColor);  //设置进度的颜色
		paint.setStyle(Paint.Style.STROKE);

		paint.setShader(linearGradient);

		canvas.drawArc(oval, startAngleDefalut, sweepAngleDefault, false, paint);  //根据进度画圆弧

		paint.setShader(null);

		switch (style) {
			case STROKE:{ // 空心
				paint.setStyle(Paint.Style.STROKE);
//			paint.setColor(Color.parseColor("#1885D8"));  //设置进度的颜色,蓝色
				// 当等于50kb时，指针转动的角度是80，需要从180-27.5 = 152.5度画到 152.5 + 80 = 232.5
//			canvas.drawArc(oval, 27.5f, -(235 * progress / max), false, paint);  //根据进度画圆弧

//			paint.setStrokeCap(Paint.Cap.SQUARE); // 两头是圆切面
//			if(percent > 0){
//				float percentLenght = sweepAngleDefault * percent;
//				canvas.drawArc(oval, startAngleDefalut, percentLenght, false, paint);  //根据进度画圆弧
//			}
				break;
			}
			case FILL:{ // 实心
				paint.setStyle(Paint.Style.FILL_AND_STROKE);
//			if(progress !=0)
//				canvas.drawArc(oval, 0, (360 * progress / max), true, paint);  //根据进度画圆弧
				break;
			}
		}

		float percentLenght = sweepAngleDefault * percent;
		if(percent > 0){
			paint.setStrokeWidth(0);
			paint.setColor(roundProgressBackgroundColor);  //设置进度的颜色,蓝色
			RectF oval2 = new RectF(centre - radius2, centre - radius2, centre
					+ radius2, centre + radius2);  //用于定义的圆弧的形状和大小的界限
			canvas.drawArc(oval2, startAngleDefalut, percentLenght, true, paint);  //根据进度画圆弧
		}

		canvas.rotate(startAngleDefalut - pointerDegree + percentLenght, centre, centre);
		canvas.drawBitmap(pointerBitmap , centre - pointerBitmap.getWidth() / 2, centre - pointerBitmap.getHeight() / 2,new Paint());
		canvas.restore();

		paint.setStrokeWidth(0);
		paint.setColor(textColor);
		paint.setTextSize(textSize);
		paint.setTypeface(Typeface.DEFAULT_BOLD); //设置字体
		float textWidth = paint.measureText(percent + "%");   //测量字体宽度，我们需要根据字体的宽度设置在圆环中间

		if(textIsDisplayable && percent != 0){
			canvas.drawText((percent * 100) + "%", centre - textWidth / 2, centre + textSize/2, paint); //画出进度百分比
		}

	}


	public synchronized int getMax() {
		return max;
	}

	/**
	 * 设置进度的最大值
	 * @param max
	 */
	public synchronized void setMax(int max) {
		if(max < 0){
			throw new IllegalArgumentException("max not less than 0");
		}
		this.max = max;
	}

	/**
	 * 获取进度.需要同步
	 * @return
	 */
	public synchronized float getProgress() {
		return progress;
	}

	/**
	 * 设置进度，此为线程安全控件，由于考虑多线的问题，需要同步
	 * 刷新界面调用postInvalidate()能在非UI线程刷新
	 * @param progress
	 */
	public synchronized void setProgress(int progress) {
		if(progress < 0){
			throw new IllegalArgumentException("progress not less than 0");
		}
		if(progress > max){
			progress = max;
		}
		if(progress <= max){
			this.progress = progress;
			postInvalidate();
		}

	}

	public synchronized void setCurNetSpeedValue(long curSpeedValue) {
		if(progress == 0 && curSpeedValue == 0)
			return;
		float preProgress = progress;
		float curProgress = 0;
		for(int i = 0; i < maxSpeedValues.length; i++){
			if(curSpeedValue > maxSpeedValues[i]){
				if(i == maxSpeedValues.length - 1)
					curProgress = max;
				continue;
			} else{
				float startValue = i == 0 ? 0 : maxSpeedValues[i - 1];
				float EndValue = maxSpeedValues[i];
				float RangeValue = EndValue - startValue;

				curProgress = ((float)i / maxSpeedValues.length + (curSpeedValue - startValue) / RangeValue * (1.0f / maxSpeedValues.length)) * max;
				break;
			}
		}
		if(curProgress < 0){
			curProgress = 0;
			return ;
		}
		animateView(preProgress, curProgress);
	}

	@SuppressLint("NewApi")
	public void animateView(final float preProgress, final float curProgress) {

		ValueAnimator animator = ValueAnimator.ofFloat(preProgress, curProgress);
		animator.setDuration(1000);
		animator.addUpdateListener(new AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				// TODO Auto-generated method stub
				float frameValue = (Float) animation.getAnimatedValue();
				progress = frameValue;
				postInvalidate();
			}
		});
		animator.start();
	}

	public int getCricleColor() {
		return roundColor;
	}

	public void setCricleColor(int cricleColor) {
		this.roundColor = cricleColor;
	}

	public int getCricleProgressColor() {
		return roundProgressColor;
	}

	public void setCricleProgressColor(int cricleProgressColor) {
		this.roundProgressColor = cricleProgressColor;
	}

	public int getTextColor() {
		return textColor;
	}

	public void setTextColor(int textColor) {
		this.textColor = textColor;
	}

	public float getTextSize() {
		return textSize;
	}

	public void setTextSize(float textSize) {
		this.textSize = textSize;
	}

	public float getRoundWidth() {
		return roundWidth;
	}

	public void setRoundWidth(float roundWidth) {
		this.roundWidth = roundWidth;
	}

}