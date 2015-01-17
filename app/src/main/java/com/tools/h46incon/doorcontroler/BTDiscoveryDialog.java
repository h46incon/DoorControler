package com.tools.h46incon.doorcontroler;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
/**
 * Created by Administrator on 2015/1/17.
 */
public class BTDiscoveryDialog extends DialogFragment{
	public interface OnDevSelect {
		public void onDevSelect(BluetoothDevice device);
	}

	private final String TAG = "BTDiscoveryDialog";
	private List<BluetoothDevice> btDevices;
	private Context mContext; // The calling activities context.
	private DisplayMetrics displayMetrics; // display metrics, use to convert dip to pix and so on.
	private DeviceAdapter deviceAdapter;
	private OnDevSelect mListener = null;
	private BluetoothAdapter btAdapter;


	public void setOnDevSelectListener(OnDevSelect mListener)
	{
		this.mListener = mListener;
	}


	// Create a BroadcastReceiver for ACTION_FOUND
	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			// When discovery finds a device
			if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				// Get the BluetoothDevice object from the Intent
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				btDevices.add(device);
				deviceAdapter.notifyDataSetChanged();
				Log.d(TAG, "Find new bt device: " + device.getAddress());
			}
		}
	};

	// Override the Fragment.onAttach() method to instantiate the
	// FontPickerDialogListener
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		btAdapter = BluetoothAdapter.getDefaultAdapter();
	}

	@Override
	public void onDestroy()
	{
		if (btAdapter != null) {
			Log.i(TAG, "cancel bluetooth discovery");
			btAdapter.cancelDiscovery();
		}

		super.onDestroy();
	}

	@Override
	public void onStart()
	{
		super.onStart();
		Log.v(TAG, "start bluetooth discovery");
		btAdapter.startDiscovery();
		MyApp.showSimpleToast("正在搜索蓝牙设备");
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState)
	{
		Log.v(TAG, "onCreateDialog");
		// get context
		mContext = getActivity();

		// get display metrics
		Resources resources = getResources();
		displayMetrics = resources.getDisplayMetrics();

		btDevices = new ArrayList<>();
		// Not need to add bonded device because they will be found again.

		// Instantiate an AlertDialog.Builder with its constructor
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		// set adapter to show fonts
		deviceAdapter = new DeviceAdapter();
		builder.setAdapter(deviceAdapter, new OnClickListener() {
			@Override
			public void onClick(DialogInterface arg0, int arg1)
			{
				int magicNumber = arg1;
				BluetoothDevice selectedDev = btDevices.get(arg1);
				// TODO: connect?
				if (mListener != null) {
					mListener.onDevSelect(selectedDev);
				}
			}
		});

		builder.setTitle("Select A Device");

		// Add the buttons
		builder.setNegativeButton("取消",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id)
					{
						// don't have to do anything on cancel
					}
				});

		// Get the AlertDialog from create()
		AlertDialog dialog = builder.create();

		// Register the BroadcastReceiver
		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		mContext.registerReceiver(mReceiver, filter); // Don't forget to unregister during onDestroy
		return dialog;
	}


	/**
	 * Create an adapter to show the fonts in the dialog.
	 * Each font will be a text view with the text being the
	 * font name, written in the style of the font
	 */
	private class DeviceAdapter extends BaseAdapter {
		// view's style
		private final float paddingDP = 20;     // Left and right padding in a view
		private final float textSize = 20;      // Use a bigger font's size
		// Use min height to make echo view have the same height in most case
		// Use the unit with SP to make this height relate to font size
		private final float minHeightSP = 50;

		// convert value to pixel value
		private final int paddingPX = dpToPix(paddingDP);
		private final int minHeightPX = spToPix(minHeightSP);

		@Override
		public int getCount()
		{
			return btDevices.size();
		}

		@Override
		public Object getItem(int position)
		{
			return btDevices.get(position);
		}

		@Override
		public long getItemId(int position)
		{
			// We use the position as ID
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			TextView view = (TextView) convertView;

			// This function may be called in two cases: a new view needs to be
			// created,
			// or an existing view needs to be reused
			if (view == null) {
				view = new TextView(mContext);

			} else {
				view = (TextView) convertView;
			}

			BluetoothDevice device = btDevices.get(position);
			String textToShow = device.getName();
//			String textToShow = String.format("%02d. %s", position, fontName);
			view.setText(textToShow);

			// Set style
			view.setGravity(Gravity.CENTER_VERTICAL);
			view.setPadding(paddingPX, 0, paddingPX, 0);
			view.setTextSize(textSize);
			view.setMinHeight(minHeightPX);

			return view;

		}
	}

	private int dpToPix(float dp)
	{
		return (int)TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP, dp, displayMetrics);
	}

	private int spToPix(float dp)
	{
		return (int)TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_SP, dp, displayMetrics);
	}
}
