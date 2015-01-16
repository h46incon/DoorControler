package com.tools.h46incon.doorcontroler;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.widget.Toast;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Administrator on 2014/4/11.
 */
public class MyApp extends Application {

	public static void addOnAppInitCallBack(Runnable runnable)
	{
		onAppInitCB.add(runnable);
		// if onCreate has been run, then run the callback here.
		if (hasAppInited) {
			runnable.run();
		}
	}

	@Override
	public void onCreate()
	{
		super.onCreate();
		instance = this;
		applicationContext = getApplicationContext();
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		resources = applicationContext.getResources();
		displayMetrics = resources.getDisplayMetrics();

		if (!hasAppInited) {
			// Run all pending function before set hasAppInited,
			// Could stop new InitCallBack added after now run twice.
			for (Runnable runnable : onAppInitCB) {
				if (runnable != null) {
					runnable.run();
				}
			}
		}
		hasAppInited = true;
	}

	public static float typedValueToPx(int unit, float value)
	{
		return TypedValue.applyDimension(unit, value, displayMetrics);
	}

	public static float PxToTypedValue(int unit, float value)
	{
		DisplayMetrics metrics = getDisplayMetrics();
		switch (unit) {
			case TypedValue.COMPLEX_UNIT_PX:
				return value;
			case TypedValue.COMPLEX_UNIT_DIP:
				return value / metrics.density;
			case TypedValue.COMPLEX_UNIT_SP:
				return value / metrics.scaledDensity;
			case TypedValue.COMPLEX_UNIT_PT:
				return value / (metrics.xdpi * (1.0f / 72));
			case TypedValue.COMPLEX_UNIT_IN:
				return value / metrics.xdpi;
			case TypedValue.COMPLEX_UNIT_MM:
				return value / (metrics.xdpi * (1.0f / 25.4f));
		}
		return 0;
	}

	public static void showSimpleToast(CharSequence msg)
	{
		Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
	}

	public static boolean isServiceRunning(String serviceClassName)
	{
		ActivityManager activityManager =
				(ActivityManager)instance.getSystemService(ACTIVITY_SERVICE);
		List<ActivityManager.RunningServiceInfo> infoList
				= activityManager.getRunningServices(Integer.MAX_VALUE);

		for (ActivityManager.RunningServiceInfo info : infoList) {
			if (info.service.getClassName().equals(serviceClassName)) {
				return true;
			}
		}
		return false;
	}

	public static Context getContext()
	{
		return applicationContext;
	}

	public static SharedPreferences getSharedPreferences()
	{
		return sharedPreferences;
	}

	public static DisplayMetrics getDisplayMetrics()
	{
		return displayMetrics;
	}

	//=================================
	// Private
	private static MyApp instance;
	private static Context applicationContext;
	private static SharedPreferences sharedPreferences;
	private static Resources resources;
	private static DisplayMetrics displayMetrics;

	private static Collection<Runnable> onAppInitCB = new LinkedList<Runnable>();
	private static boolean hasAppInited = false;
}
