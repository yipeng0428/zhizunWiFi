package com.zhizun.zhizunwifi.view;

import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewConfiguration;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.zhizun.zhizunwifi.R;
import com.zhizun.zhizunwifi.utils.L;

import net.qiujuer.genius.ui.widget.Loading;

/**
 * 可进行下拉刷新的自定义控件。
 *
 * @author guolin
 *
 */
public class RefreshableView extends LinearLayout implements OnTouchListener {

	/**
	 * 下拉状态
	 */
	public static final int STATUS_PULL_TO_REFRESH = 0;

	/**
	 * 释放立即刷新状态
	 */
	public static final int STATUS_RELEASE_TO_REFRESH = 1;

	/**
	 * 正在刷新状态
	 */
	public static final int STATUS_REFRESHING = 2;

	/**
	 * 刷新完成或未刷新状态
	 */
	public static final int STATUS_REFRESH_FINISHED = 3;

	/**
	 * 下拉头部回滚的速度
	 */
	public static final int SCROLL_SPEED = -30;

	/**
	 * 一分钟的毫秒值，用于判断上次的更新时间
	 */
	public static final long ONE_MINUTE = 60 * 1000;

	/**
	 * 一小时的毫秒值，用于判断上次的更新时间
	 */
	public static final long ONE_HOUR = 60 * ONE_MINUTE;

	/**
	 * 一天的毫秒值，用于判断上次的更新时间
	 */
	public static final long ONE_DAY = 24 * ONE_HOUR;

	/**
	 * 一月的毫秒值，用于判断上次的更新时间
	 */
	public static final long ONE_MONTH = 30 * ONE_DAY;

	/**
	 * 一年的毫秒值，用于判断上次的更新时间
	 */
	public static final long ONE_YEAR = 12 * ONE_MONTH;

	/**
	 * 上次更新时间的字符串常量，用于作为SharedPreferences的键值
	 */
	private static final String UPDATED_AT = "updated_at";

	/**
	 * 下拉刷新的回调接口
	 */
	private PullToRefreshListener mListener;

	/**
	 * 用于存储上次更新时间
	 */
	private SharedPreferences preferences;

	/**
	 * 下拉头的View
	 */
	private View header;

	/**
	 * 需要去下拉刷新的LayoutView
	 */
	private ScrollView LayoutView;

	/**
	 * 刷新时显示的进度条
	 */
	private Loading progressBar;

	/**
	 * 指示下拉和释放的箭头
	 */
	private ImageView arrow;

	/**
	 * 指示下拉和释放的文字描述
	 */
//	private TextView description;

	/**
	 * 指示下拉和释放的文字描述
	 */
	private TextView listview_header_hint_textview;

	/**
	 * 上次更新时间的文字描述
	 */
//	private TextView updateAt;

	/**
	 * 下拉头的布局参数
	 */
	private MarginLayoutParams listViewLayoutParams;

	/**
	 * 上次更新时间的毫秒值
	 */
	private long lastUpdateTime;

	/**
	 * 为了防止不同界面的下拉刷新在上次更新时间上互相有冲突，使用id来做区分
	 */
	private int mId = -1;

	/**
	 * 下拉头的高度
	 */
	private int hideHeaderHeight;

	/**
	 * 下拉头的最高的高度
	 */
	private int overHeight = 10;

	/**
	 * 当前处理什么状态，可选值有STATUS_PULL_TO_REFRESH, STATUS_RELEASE_TO_REFRESH,
	 * STATUS_REFRESHING 和 STATUS_REFRESH_FINISHED
	 */
	private int currentStatus = STATUS_REFRESH_FINISHED;;

	/**
	 * 记录上一次的状态是什么，避免进行重复操作
	 */
	private int lastStatus = currentStatus;

	/**
	 * 手指按下时的屏幕纵坐标
	 */
	private float yDown;

	/**
	 * 在被判定为滚动之前用户手指可以移动的最大值。
	 */
	private int touchSlop;

	/**
	 * 是否已加载过一次layout，这里onLayout中的初始化只需加载一次
	 */
	private boolean loadOnce;

	/**
	 * 当前是否可以下拉，只有ListView滚动到头的时候才允许下拉
	 */
	private boolean ableToPull;

	/**
	 * 下拉刷新控件的构造函数，会在运行时动态添加一个下拉头的布局。
	 *
	 * @param context
	 * @param attrs
	 */
	public RefreshableView(Context context, AttributeSet attrs) {
		super(context, attrs);
		preferences = PreferenceManager.getDefaultSharedPreferences(context);
//		header = LayoutInflater.from(context).inflate(R.layout.pull_to_refresh, null, true);
		header = LayoutInflater.from(context).inflate(R.layout.listview_header, null, true);
		progressBar = (Loading) header.findViewById(R.id.progress_bar);
		arrow = (ImageView) header.findViewById(R.id.arrow);
		listview_header_hint_textview = (TextView) header.findViewById(R.id.listview_header_hint_textview);
//		description = (TextView) header.findViewById(R.id.description);
//		updateAt = (TextView) header.findViewById(R.id.updated_at);
		touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
		refreshUpdatedAtValue();
		setOrientation(VERTICAL);
		addView(header, 0);
	}

	public void setTitle(String title){
		listview_header_hint_textview.setText(title);
	}
	/**
	 * 进行一些关键性的初始化操作，比如：将下拉头向上偏移进行隐藏，给ListView注册touch事件。
	 */
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		if (changed && !loadOnce) {
			hideHeaderHeight = -header.getHeight();
			LayoutView = (ScrollView) getChildAt(1);
			listViewLayoutParams = (MarginLayoutParams) LayoutView.getLayoutParams();
			listViewLayoutParams.topMargin = hideHeaderHeight;
			LayoutView.setOnTouchListener(this);
			loadOnce = true;
		}
	}

	private boolean isTouchDown = false;

	/**
	 * 当ListView被触摸时调用，其中处理了各种下拉刷新的具体逻辑。
	 */
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if(currentStatus == STATUS_REFRESHING)
			return false;
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				isTouchDown = true;
				yDown = event.getRawY();
				break;
			case MotionEvent.ACTION_MOVE:
				float yMove = event.getRawY();
				float distance = yMove - yDown;
				yDown = yMove;
				if(!isTouchDown){
					isTouchDown = true;
					return true;
				}

				// 如果手指是上滑状态，并且列表为初始位置，不操作
				if(distance <= 0 && listViewLayoutParams.topMargin <= hideHeaderHeight){
					L.d("WifiUtils","正常上滑");
					return false;
				}

				if(distance > 0 && LayoutView.getScrollY() > 0){
					L.d("WifiUtils","正常下滑");
					return false;
				}

				// 是否可下拉滚动
				if(LayoutView.getScrollY() <= 0 && distance >= 0){
					if(LayoutView.getScrollY() != 0){
						LayoutView.post(new Runnable() {
							@Override
							public void run() {
								LayoutView.smoothScrollTo(0, 0);
							}
						});
					}
					setViewUpLoction(distance);
					return true;
				}
				if(setViewUpLoction(distance))
					return true;
				break;
			case MotionEvent.ACTION_UP:
			default:
				isTouchDown = false;
				L.e("RefreshableView", "手指抬起 currentStatus= " + currentStatus);
				if (currentStatus == STATUS_RELEASE_TO_REFRESH) {
					// 松手时如果是释放立即刷新状态，就去调用正在刷新的任务
//				new RefreshingTask().execute();
					setTitle("正在刷新");
					animateTopTo(0, true);
				} else if (currentStatus == STATUS_PULL_TO_REFRESH) {
					// 松手时如果是下拉状态，就去调用隐藏下拉头的任务
//				new HideHeaderTask().execute();
					animateTopTo(hideHeaderHeight, false);
				}
				break;
		}
		return realRefreshView();
	}

	public boolean setViewUpLoction(float distance){
		if (currentStatus != STATUS_REFRESHING) {
			// 通过偏移下拉头的topMargin值，来实现下拉效果
			int nowHeaderHeight = (int)(distance / 2) + listViewLayoutParams.topMargin;
			if(nowHeaderHeight > overHeight)
				nowHeaderHeight = overHeight;
			listViewLayoutParams.topMargin = nowHeaderHeight;
			LayoutView.setLayoutParams(listViewLayoutParams);
			if (listViewLayoutParams.topMargin >= 0) {
				currentStatus = STATUS_RELEASE_TO_REFRESH;
			} else {
				currentStatus = STATUS_PULL_TO_REFRESH;
			}
			realRefreshView();
			return true;
		}
		return false;
	}

	/**
	 * 时刻记得更新下拉头中的信息
	 * @return
	 */
	public boolean realRefreshView(){
		if (currentStatus == STATUS_PULL_TO_REFRESH
				|| currentStatus == STATUS_RELEASE_TO_REFRESH) {
//			Log.i("RefreshableView", "updateHeaderView============ ");
			updateHeaderView();
			// 当前正处于下拉或释放状态，要让ListView失去焦点，否则被点击的那一项会一直处于选中状态
			LayoutView.setPressed(false);
			LayoutView.setFocusable(false);
			LayoutView.setFocusableInTouchMode(false);
			lastStatus = currentStatus;
			// 当前正处于下拉或释放状态，通过返回true屏蔽掉ListView的滚动事件
			return false;
		}
		return false;
	}

	/**
	 * 给下拉刷新控件注册一个监听器。
	 *
	 * @param listener
	 *            监听器的实现。
	 * @param id
	 *            为了防止不同界面的下拉刷新在上次更新时间上互相有冲突， 请不同界面在注册下拉刷新监听器时一定要传入不同的id。
	 */
	public void setOnRefreshListener(PullToRefreshListener listener, int id) {
		mListener = listener;
		mId = id;
	}

	/**
	 * 当所有的刷新逻辑完成后，记录调用一下，否则你的ListView将一直处于正在刷新状态。
	 */
	public void finishRefreshing() {
		currentStatus = STATUS_REFRESH_FINISHED;
		preferences.edit().putLong(UPDATED_AT + mId, System.currentTimeMillis()).commit();
//		new HideHeaderTask().execute();
		animateTopTo(hideHeaderHeight, false);
		header.setVisibility(INVISIBLE);
	}

	/**
	 * 根据当前ListView的滚动状态来设定 {@link #ableToPull}
	 * 的值，每次都需要在onTouch中第一个执行，这样可以判断出当前应该是滚动ListView，还是应该进行下拉。
	 *
	 * @param event
	 */
	private void setIsAbleToPull(MotionEvent event) {

		if(LayoutView.getScrollY() == 0){
			if (!ableToPull) {
				yDown = event.getRawY();
			}
			// 如果首个元素的上边缘，距离父布局值为0，就说明ListView滚动到了最顶部，此时应该允许下拉刷新
			ableToPull = true;
		} else {
			ableToPull = false;
		}
	}

	/**
	 * 更新下拉头中的信息。
	 */
	private void updateHeaderView() {
		if (lastStatus != currentStatus) {
			if (currentStatus == STATUS_PULL_TO_REFRESH) {
//				description.setText(getResources().getString(R.string.pull_to_refresh));
				//arrow.setVisibility(View.VISIBLE);
				header.setVisibility(VISIBLE);
				progressBar.setVisibility(View.VISIBLE);
				//rotateArrow();
			} else if (currentStatus == STATUS_RELEASE_TO_REFRESH) {
//				description.setText(getResources().getString(R.string.release_to_refresh));
				//arrow.setVisibility(View.VISIBLE);
				progressBar.setVisibility(View.VISIBLE);
				//rotateArrow();
			} else if (currentStatus == STATUS_REFRESHING) {
//				description.setText(getResources().getString(R.string.refreshing));
				progressBar.setVisibility(View.VISIBLE);
				//arrow.clearAnimation();
				//arrow.setVisibility(View.GONE);
			}
			refreshUpdatedAtValue();
		}
	}

	/**
	 * 根据当前的状态来旋转箭头。
	 */
	private void rotateArrow() {
		float pivotX = 0.5f;
		float pivotY = 0.5f;
		float fromDegrees = 0f;
		float toDegrees = 0f;
		if (currentStatus == STATUS_PULL_TO_REFRESH) {
			fromDegrees = 180f;
			toDegrees = 360f;
			setTitle("下拉扫描免费WiFi");
		} else if (currentStatus == STATUS_RELEASE_TO_REFRESH) {
			fromDegrees = 0f;
			toDegrees = 180f;
			setTitle("松开扫描免费WiFi");
		}
		RotateAnimation animation = new RotateAnimation(fromDegrees, toDegrees, Animation.RELATIVE_TO_SELF, pivotX, Animation.RELATIVE_TO_SELF, pivotY);
		animation.setDuration(100);
		animation.setFillAfter(true);
		arrow.startAnimation(animation);
	}

	/**
	 * 刷新下拉头中上次更新时间的文字描述。
	 */
	private void refreshUpdatedAtValue() {/*
		lastUpdateTime = preferences.getLong(UPDATED_AT + mId, -1);
		long currentTime = System.currentTimeMillis();
		long timePassed = currentTime - lastUpdateTime;
		long timeIntoFormat;
		String updateAtValue;
		if (lastUpdateTime == -1) {
			updateAtValue = getResources().getString(R.string.not_updated_yet);
		} else if (timePassed < 0) {
			updateAtValue = getResources().getString(R.string.time_error);
		} else if (timePassed < ONE_MINUTE) {
			updateAtValue = getResources().getString(R.string.updated_just_now);
		} else if (timePassed < ONE_HOUR) {
			timeIntoFormat = timePassed / ONE_MINUTE;
			String value = timeIntoFormat + "分钟";
			updateAtValue = String.format(getResources().getString(R.string.updated_at), value);
		} else if (timePassed < ONE_DAY) {
			timeIntoFormat = timePassed / ONE_HOUR;
			String value = timeIntoFormat + "小时";
			updateAtValue = String.format(getResources().getString(R.string.updated_at), value);
		} else if (timePassed < ONE_MONTH) {
			timeIntoFormat = timePassed / ONE_DAY;
			String value = timeIntoFormat + "天";
			updateAtValue = String.format(getResources().getString(R.string.updated_at), value);
		} else if (timePassed < ONE_YEAR) {
			timeIntoFormat = timePassed / ONE_MONTH;
			String value = timeIntoFormat + "个月";
			updateAtValue = String.format(getResources().getString(R.string.updated_at), value);
		} else {
			timeIntoFormat = timePassed / ONE_YEAR;
			String value = timeIntoFormat + "年";
			updateAtValue = String.format(getResources().getString(R.string.updated_at), value);
		}
//		updateAt.setText(updateAtValue);
	*/}

	/**
	 * 正在刷新的任务，在此任务中会去回调注册进来的下拉刷新监听器。
	 *
	 * @author guolin
	 */
//	public class RefreshingTask extends AsyncTask<Void, Integer, Void> {
//
//		@Override
//		protected Void doInBackground(Void... params) {
//			int topMargin = listViewLayoutParams.topMargin;
//			while (true) {
//				topMargin = topMargin + SCROLL_SPEED;
//				if (topMargin <= 0) {
//					topMargin = 0;
//					break;
//				}
//				publishProgress(topMargin);
//				sleep(10);
//			}
//			currentStatus = STATUS_REFRESHING;
//			Log.i("RefreshableView", "正在刷新 currentStatus= " + currentStatus);
//			publishProgress(0);
//			if (mListener != null) {
//				mListener.onRefresh();
//			}
//			return null;
//		}
//
//		@Override
//		protected void onProgressUpdate(Integer... topMargin) {
//			updateHeaderView();
//			listViewLayoutParams.topMargin = topMargin[0];
//			listView.setLayoutParams(listViewLayoutParams);
//		}
//
//	}

	/**
	 * 隐藏下拉头的任务，当未进行下拉刷新或下拉刷新完成后，此任务将会使下拉头重新隐藏。
	 *
	 * @author guolin
	 */
	public void animateTopTo(final int y, final boolean isRefresh) {
		if (listViewLayoutParams == null){
			LayoutView = (ScrollView) getChildAt(1);
			listViewLayoutParams = (MarginLayoutParams) LayoutView.getLayoutParams();
			listViewLayoutParams.topMargin = hideHeaderHeight;
		}
		ValueAnimator animator = ValueAnimator.ofInt(listViewLayoutParams.topMargin, y);
		animator.setDuration(300);
		animator.addUpdateListener(new AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				// TODO Auto-generated method stub
				int frameValue = (Integer) animation.getAnimatedValue();

				listViewLayoutParams.topMargin = frameValue;
				LayoutView.setLayoutParams(listViewLayoutParams);

				if(y == frameValue){

					if(isRefresh){
						currentStatus = STATUS_REFRESHING;
						updateHeaderView();
						if (mListener != null) {
							mListener.onRefresh();
						}
					} else{
						currentStatus = STATUS_REFRESH_FINISHED;
					}
				}
			}
		});
		animator.start();
	}

	/**
	 * 使当前线程睡眠指定的毫秒数。
	 *
	 * @param time
	 *            指定当前线程睡眠多久，以毫秒为单位
	 */
	private void sleep(int time) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 下拉刷新的监听器，使用下拉刷新的地方应该注册此监听器来获取刷新回调。
	 *
	 * @author guolin
	 */
	public interface PullToRefreshListener {

		/**
		 * 刷新时会去回调此方法，在方法内编写具体的刷新逻辑。注意此方法是在子线程中调用的， 你可以不必另开线程来进行耗时操作。
		 */
		void onRefresh();

	}

}