package com.project.sysumobilelibrary;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.VolleyError;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.jauker.widget.BadgeView;
import com.project.sysumobilelibrary.entity.LikeBook;
import com.project.sysumobilelibrary.entity.OrderBook;
import com.project.sysumobilelibrary.global.MyApplication;
import com.project.sysumobilelibrary.global.MyURL;
import com.project.sysumobilelibrary.utils.MyAlgorithm;
import com.project.sysumobilelibrary.utils.MyDBHelper;

import android.R.bool;
import android.R.integer;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.RadioGroup.OnCheckedChangeListener;

public class MainActivity extends Activity implements OnCheckedChangeListener {
	private static final String TAG = "MainActivity";

	private static final int heart_time = 300000;// 5分钟
	private static boolean timing_heart = true;

	private RadioGroup rgTabBar;
	private RadioButton rbChannel;


	
	private FragmentManager fragmentManager;
	private UserFragment userFragment;
	private SearchFragment searchFragment;
	private LikeFragment likeFragment;

	// 声明通知（消息）管理器
	private static NotificationManager notificationManager;
//	private static boolean canNotifyMeet = true;
//	private static boolean canNotifyMiss = true;
	private final static int CODE_TOAST_MSG = 0;

	public static int meetOrderNum = 0;
	public static int missBorrowNum = 0;
	private static BadgeView meetBadgeView;
	private static BadgeView missBadgeView;

	private static Context context;

	private static Button btUser;

	Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case CODE_TOAST_MSG:
				Toast.makeText(MainActivity.this, msg.obj.toString(),
						Toast.LENGTH_SHORT).show();
				break;
			}
		};
	};

	public void myToast(String msg) {
		handler.obtainMessage(CODE_TOAST_MSG, msg).sendToTarget();
	}

	public static void setMeetOrderNum(int n) {
		if (meetOrderNum == n){
			return;
		}
		meetOrderNum = n;
		if (n > 0) {
		
				showMeetOrderNotice(n);
			
			meetBadgeView.setVisibility(View.VISIBLE);
			meetBadgeView.setTargetView(btUser);
			meetBadgeView.setTypeface(Typeface.create(Typeface.SANS_SERIF,
					Typeface.ITALIC));
			meetBadgeView.setShadowLayer(2, -1, -1, Color.GREEN);
			meetBadgeView.setBadgeGravity(Gravity.CENTER | Gravity.LEFT);
			meetBadgeView.setBackgroundColor(Color.GREEN);
			meetBadgeView.setBadgeCount(n);
		} else {
			meetBadgeView.setVisibility(View.GONE);
		}
	}

	@SuppressWarnings("deprecation")
	private static void showMeetOrderNotice(int n) {
//		canNotifyMeet = false;
		notificationManager.cancel(1);
		Notification notice = new Notification(
				R.drawable.ic_meet, "你有"+n+"个满足的预约",
				System.currentTimeMillis());
		notice.flags = Notification.FLAG_AUTO_CANCEL;
		Intent appIntent = new Intent(Intent.ACTION_MAIN);
		appIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		appIntent.setComponent(new ComponentName(context.getPackageName(),
				context.getPackageName() + "." + "MainActivity"));
		appIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				| Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);// 关键的一步，设置启动模式
		PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
				appIntent, 0);
		notice.setLatestEventInfo(context, n+"个预约满足了：", "点击进入...:", contentIntent);
		notificationManager.notify(1, notice);
	}

	public static void setMissBorrowNum(int n) {
		if (missBorrowNum == n){
			return;
		}
		missBorrowNum = n;
		if (n > 0) {
			
				showMissBorrowNotice(n);
			
			missBadgeView.setVisibility(View.VISIBLE);
			missBadgeView.setTargetView(btUser);
			missBadgeView.setTypeface(Typeface.create(Typeface.SANS_SERIF,
					Typeface.ITALIC));
			missBadgeView.setShadowLayer(2, -1, -1, Color.RED);
			missBadgeView.setBadgeGravity(Gravity.CENTER | Gravity.RIGHT);
			missBadgeView.setBackgroundColor(Color.RED);
			missBadgeView.setBadgeCount(n);
		} else {
			missBadgeView.setVisibility(View.GONE);
		}
	}
	@SuppressWarnings("deprecation")
	private static void showMissBorrowNotice(int n) {
		notificationManager.cancel(2);
		Notification notice = new Notification(
				R.drawable.ic_miss, "你有"+n+"本书还没还哦...",
				System.currentTimeMillis());
		notice.flags = Notification.FLAG_AUTO_CANCEL;
		Intent appIntent = new Intent(Intent.ACTION_MAIN);
		appIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		appIntent.setComponent(new ComponentName(context.getPackageName(),
				context.getPackageName() + "." + "MainActivity"));
		appIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				| Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);// 关键的一步，设置启动模式
		PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
				appIntent, 0);
		notice.setLatestEventInfo(context, "你有书还没还或即将过期了哦：", "点击进入...:", contentIntent);
		notificationManager.notify(2, notice);
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);// 去掉标题栏
		setContentView(R.layout.activity_main);
		context = MainActivity.this;
		btUser = (Button) findViewById(R.id.bt_user);
		meetBadgeView = new BadgeView(this);
		missBadgeView = new BadgeView(this);
//		canNotifyMeet = true;
//		canNotifyMiss = true;
		notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

		initView();
		handler.postDelayed(updateUserInfoAsHeart, heart_time);
	}

	@Override
	protected void onResume() {
		getOrderMsg();
		getBorrowMsg();
		super.onResume();

	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		handler.removeCallbacks(updateUserInfoAsHeart);
		super.onDestroy();
	}

	private void initView() {
		fragmentManager = getFragmentManager();
		rgTabBar = (RadioGroup) findViewById(R.id.rg_tab_bar);
		rgTabBar.setOnCheckedChangeListener(this);
		rbChannel = (RadioButton) findViewById(R.id.rb_search);
		rbChannel.setChecked(true);
		

	}

	

	// 隐藏所有Fragment
	private void hideAllFragments(FragmentTransaction fragmentTransaction) {
		if (userFragment != null)
			fragmentTransaction.hide(userFragment);
		if (searchFragment != null)
			fragmentTransaction.hide(searchFragment);
		if (likeFragment != null)
			fragmentTransaction.hide(likeFragment);
	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		FragmentTransaction transaction = fragmentManager.beginTransaction();// .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE)
		hideAllFragments(transaction);

		switch (checkedId) {
		case R.id.rb_user:
			if (userFragment == null) {
				userFragment = new UserFragment();
				transaction.add(R.id.fl_content, userFragment);
			} else {
				transaction.show(userFragment);
			}

			break;
		case R.id.rb_search:
			if (searchFragment == null) {
				searchFragment = new SearchFragment();
				transaction.add(R.id.fl_content, searchFragment);
			} else {
				transaction.show(searchFragment);
			}
			break;
		case R.id.rb_like:
			if (likeFragment == null) {
				likeFragment = new LikeFragment();
				transaction.add(R.id.fl_content, likeFragment);
			} else {
				transaction.show(likeFragment);
			}
			break;
		}
		transaction.commit();
	}

	Runnable updateUserInfoAsHeart = new Runnable() {

		@Override
		public void run() {
			if (timing_heart) {
				getUserInfo();
			}
			handler.postDelayed(this, heart_time);
		}
	};

	private void getUserInfo() {
		Listener<String> listener = new Listener<String>() {

			@Override
			public void onResponse(String response) {
				try {
					JSONObject jsonObject = new JSONObject(response);
					Log.e(TAG, jsonObject.getString("msg"));
					int code = jsonObject.getInt("code");
					if (code == 1) {
						MyApplication.getUser()
								.updateFromJSONObject(jsonObject);
						Log.e(TAG, "定时获取用户信息成功");
						// updateHint();
					} else {
						Log.e(TAG, "定时获取用户信息失败");
						reLogin();
					}
				} catch (JSONException e) {
					e.printStackTrace();
					Log.e(TAG, "get user info json new error");
				}
			}
		};
		ErrorListener errorListener = new ErrorListener() {

			@Override
			public void onErrorResponse(VolleyError error) {
				Log.e(TAG, "get user info error; " + error.toString());
			}
		};
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("token", MyApplication.getUser().getToken());
		MyApplication.getMyVolley().addPostStringRequest(
				MyURL.GET_USER_INFO_URL, listener, errorListener, map, TAG);
	}

	private void reLogin() {
		String username = MyApplication.getUser().getUsername();
		String password = MyApplication.getUser().getPassword();

		Listener<String> listener = new Listener<String>() {

			@Override
			public void onResponse(String response) {
				try {
					JSONObject jsonObject = new JSONObject(response);
					Log.e(TAG, jsonObject.getString("msg"));
					int code = jsonObject.getInt("code");
					if (code == 1) {
						MyApplication.getUser()
								.updateFromJSONObject(jsonObject);
						Log.e(TAG, "重登陆成功");
					} else {
						Log.e(TAG, "重登陆失败");
					}
				} catch (JSONException e) {
					e.printStackTrace();
					Log.e(TAG, "login json new error");
				}

			}

		};
		ErrorListener errorListener = new ErrorListener() {

			@Override
			public void onErrorResponse(VolleyError error) {
				Log.e(TAG, "login error; " + error.toString());
			}
		};
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("username", username);
		map.put("password", password);
		MyApplication.getMyVolley().addPostStringRequest(MyURL.LOGIN_URL,
				listener, errorListener, map, TAG);
	}

	private long mExitTime = 0;

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if ((System.currentTimeMillis() - mExitTime) > 2000) {
				Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
				mExitTime = System.currentTimeMillis();
			} else {
				// finish();
				Intent intent = new Intent(Intent.ACTION_MAIN);
				intent.addCategory(Intent.CATEGORY_HOME);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
				android.os.Process.killProcess(android.os.Process.myPid());
			}
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// http://my.oschina.net/wangxnn/blog/417581
		// super.onSaveInstanceState(outState);
	}

	public void getOrderMsg() {
		Listener<String> listener = new Listener<String>() {
			@Override
			public void onResponse(String response) {
				try {
					JSONObject jsonObject = new JSONObject(response);
					Log.e(TAG, jsonObject.getString("msg"));
					int code = jsonObject.getInt("code");
					// Log.e(TAG, response);
					if (code == 1) {
						JSONArray jsonArray = jsonObject.getJSONArray("loans");
						int n = jsonArray.length();
						MyApplication.getUser().setOrder_num(n + "");

						int num = 0;
						for (int i = 0; i < n; ++i) {
							String meet_date = jsonArray.getJSONObject(i)
									.getString("meet_date");
							if (!meet_date.trim().equals("0")) {
								num++;
							}
						}
						setMeetOrderNum(num);
						Log.e(TAG, "获取预约成功");
					} else {
						Log.e(TAG, "获取预约失败");
					}
				} catch (JSONException e) {
					e.printStackTrace();
					Log.e(TAG, "get order books json error");
				}
			}
		};
		ErrorListener errorListener = new ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				Log.e(TAG, "get order books error; " + error.toString());
			}
		};
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("token", MyApplication.getUser().getToken());
		MyApplication.getMyVolley().addPostStringRequest(
				MyURL.GET_MY_BOR_HOLD_URL, listener, errorListener, map, TAG);
	}

	public static void getBorrowMsg() {
		Listener<String> listener = new Listener<String>() {
			@Override
			public void onResponse(String response) {
				try {
					JSONObject jsonObject = new JSONObject(response);
					Log.e(TAG, jsonObject.getString("msg"));
					int code = jsonObject.getInt("code");
					// Log.e(TAG, response);
					if (code == 1) {

						JSONArray jsonArray = jsonObject.getJSONArray("loans");
						int n = jsonArray.length();
						MyApplication.getUser().setBorrow_num(n + "");

						int num = 0;
						String pat = "yyyyMMdd";
						SimpleDateFormat sdf = new SimpleDateFormat(pat);
						Date now_date = new Date();// 获取当前时间
						String now_date_str = sdf.format(now_date);
						int now = Integer.parseInt(now_date_str);
						for (int i = 0; i < n; ++i) {
							String due_dates = jsonArray.getJSONObject(i)
									.getString("due_date");
							String due_date_str = Pattern.compile("[^0-9]")
									.matcher(due_dates).replaceAll("");
							try {
								Date due_date = sdf.parse(due_date_str);
								int due = Integer.parseInt(due_date_str);
								int diff = MyAlgorithm.getGapCount(now_date,
										due_date);
								if (diff <= 1) {
									num++;
								}
							} catch (ParseException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
						}
						setMissBorrowNum(num);

						Log.e(TAG, "获取外借成功");
					} else {
						Log.e(TAG, "获取外借失败");
					}
				} catch (JSONException e) {
					e.printStackTrace();
					Log.e(TAG, "get borrow books json error");
				}
			}
		};
		ErrorListener errorListener = new ErrorListener() {

			@Override
			public void onErrorResponse(VolleyError error) {
				Log.e(TAG, "get borrow books error; " + error.toString());
			}
		};
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("token", MyApplication.getUser().getToken());
		MyApplication.getMyVolley().addPostStringRequest(
				MyURL.GET_MY_BOR_LOAN_URL, listener, errorListener, map, TAG);
	}
}
