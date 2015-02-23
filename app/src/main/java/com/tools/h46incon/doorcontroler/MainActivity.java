package com.tools.h46incon.doorcontroler;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.tools.h46incon.doorcontroler.BGWorker.BGWorker;
import com.tools.h46incon.doorcontroler.BGWorker.SerialBGWorker;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;
import java.util.concurrent.Callable;


public class MainActivity extends ActionBarActivity {

	private static enum  State{
		BT_SETTING,
		OPEN_DOOR,
		EXIT
	}

	private static interface OnBTClick{
		public void onBTClick(State state);
	}

	private class StateManager{
		public StateManager(OnBTClick onBTClick, State initState)
		{
			this.onBTClick = onBTClick;
			this.curState = initState;

			initVar();

			// Set button's callback
			for (int i = 0; i < stateNum; ++i) {
				final State currentState = State.values()[i];
				buttons[i].setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v)
					{
						BTClickHandler(currentState);
					}
				});
			}

			arrowHLManager.highlight(imageViews[curState.ordinal()]);
		}

		public State getCurState()
		{
			return curState;
		}

		public void changeState(State state)
		{
			curState = state;
			arrowHLManager.highlight(imageViews[curState.ordinal()]);
		}


		private void BTClickHandler(State state)
		{
			boolean isLegal = false;

			// Exit button should always be handled
			if (state == State.EXIT) {
				isLegal = true;
			} else {
				// Should able to handle current step
				// Also allow user to restart previous step
				if (state.ordinal() <= curState.ordinal()) {
					isLegal = true;
				} else {
					isLegal = false;
				}
			}

			if (isLegal) {
				onBTClick.onBTClick(state);
			}
		}

		private void initVar()
		{
			// get views
			btSettingBtn = (Button) findViewById(R.id.btConnect_btn);
			openDoorBtn = (Button) findViewById(R.id.openDoor_btn);
			exitBtn = (Button) findViewById(R.id.exit_btn);

			btSettingArrowIV = (ImageView) findViewById(R.id.btConnect_arrow_iv);
			openDoorArrowIV = (ImageView) findViewById(R.id.openDoor_arrow_iv);
			exitArrowIV = (ImageView) findViewById(R.id.exit_arrow_iv);

			// store views into array
			buttons = new Button[stateNum];
			buttons[State.BT_SETTING.ordinal()] = btSettingBtn;
			buttons[State.OPEN_DOOR.ordinal()] = openDoorBtn;
			buttons[State.EXIT.ordinal()] = exitBtn;

			imageViews = new ImageView[stateNum];
			imageViews[State.BT_SETTING.ordinal()] = btSettingArrowIV;
			imageViews[State.OPEN_DOOR.ordinal()] = openDoorArrowIV;
			imageViews[State.EXIT.ordinal()] = exitArrowIV;

			// init other member
			this.arrowHLManager =
					new ArrowHLManager(R.drawable.arrow_default, R.drawable.arrow_highlight);
		}

		private final int stateNum = State.values().length;
		private final String TAG = "StateManager";

		private State curState;
		private ArrowHLManager arrowHLManager;
		private OnBTClick onBTClick;

		// Arrays of views
		private Button[] buttons;
		private ImageView[] imageViews;
		// Views
		private Button btSettingBtn;
		private Button openDoorBtn;
		private Button exitBtn;
		private ImageView btSettingArrowIV;
		private ImageView openDoorArrowIV;
		private ImageView exitArrowIV;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		initMember();
		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED);
		registerReceiver(btDisconnectReceiver, filter);
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		unregisterReceiver(btDisconnectReceiver);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			exiting();
			return true;
		}
		return false;
	}

	private void initMember()
	{
		TextView consoleTV = (TextView) findViewById(R.id.console_tv);
		outputConsole = new OutputConsole(consoleTV);

		btAdapter = BluetoothAdapter.getDefaultAdapter();

		initExitingWithTurnOffBTDialog();

		OnBTClick onBTClick = new OnBTClick() {
			@Override
			public void onBTClick(State state)
			{
				switch (state) {
					case BT_SETTING:
						connectBTDev();
						break;
					case OPEN_DOOR:
						openDoor();
						break;
					case EXIT:
						exiting();
						break;
				}

			}
		};
		stateManager = new StateManager(onBTClick, State.BT_SETTING);

		btDisconnectReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent)
			{
				if (stateManager.curState == State.OPEN_DOOR) {
					String disconnectMsg = "蓝牙连接已断开";
					MyApp.showSimpleToast(disconnectMsg);
					outputConsole.printNewItem(disconnectMsg);
					stateManager.changeState(State.BT_SETTING);
				}
			}
		};
	}

	private void initExitingWithTurnOffBTDialog()
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("退出时要顺便关闭蓝牙吗？");
		builder.setTitle("退出");

		builder.setPositiveButton("关了吧", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				Log.i(TAG, "shutting down bluetooth...");
				btAdapter.disable();
				MyApp.showSimpleToast("蓝牙已关闭");
				dialog.dismiss();
				MainActivity.this.finish();
			}
		});

		builder.setNegativeButton("不关", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				dialog.dismiss();
				MainActivity.this.finish();
			}
		});

		exitingWithTurnOffBTDialog = builder.create();
	}

	private void connectBTDev()
	{
		BTDiscoveryDialog btDiscoveryDialog = new BTDiscoveryDialog();
		btDiscoveryDialog.setOnDevSelectListener(
				new BTDiscoveryDialog.OnDevSelect() {
					@Override
					public void onDevSelect(final BluetoothDevice device)
					{
						Log.d(TAG, "received selected device " + device.getAddress());
						connectDoorCtrlDevice(device);
					}
				}
		);
		btDiscoveryDialog.show(getFragmentManager(), "btDiscoveryDialog");
	}

	private void exiting()
	{
		if (btAdapter.isEnabled()) {
			exitingWithTurnOffBTDialog.show();
		} else {
			this.finish();
		}
	}

	private void btDisconnect()
	{
		try {
			if (btSocketIn != null) {
				btSocketIn.close();
			}
			if (btSocketOut != null) {
				btSocketOut.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			if (btSocket != null) {
				btSocket.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		btSocket = null;
		btSocketIn = null;
		btSocketOut = null;
	}

	private void showWrongDeviceDialog()
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder .setTitle("握手失败")
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setMessage("是否连错蓝牙？")
				.setCancelable(true)
				.setPositiveButton("确定", null)
				.show();
	}

	// connected device
	// It will finish bluetooth device connection and hand shaking work
	private void connectDoorCtrlDevice(final BluetoothDevice btDevice)
	{

		outputConsole.printNewItem(
				String.format("正在尝试连接蓝牙设备: %s (%s)", btDevice.getName(), btDevice.getAddress()));


		outputConsole.indent();

		final SerialBGWorker serialBGWorker = new SerialBGWorker(this);

		// socket connect task
		SerialBGWorker.taskInfo connectSocketTask = new SerialBGWorker.taskInfo();
		connectSocketTask.message = "正在建立连接...";
		connectSocketTask.onPerWorkFinished = new SerialBGWorker.OnPerWorkFinished() {
			@Override
			public boolean onPerWorkFinished(BGWorker.WorkState state, Object reslut)
			{
				boolean needContinue = false;
				if ((state == BGWorker.WorkState.SUCCESS) && (Boolean) reslut) {
					// This step will be finished in some millisecond
					needContinue = getSocketStream();
				}
				if (needContinue) {
					return true;
				} else {
					outputConsole.unIndent();
					outputConsole.printNewItem("连接设备失败");
					return false;
				}
			}
		};
		connectSocketTask.task = new Callable() {
			@Override
			public Object call() throws Exception
			{
				return connectBTSSPSocket(btDevice);
			}
		};
		connectSocketTask.timeout = 30 * 1000;      // 10s

		// Dev shake hand task
		final SerialBGWorker.taskInfo devShakeTask = new SerialBGWorker.taskInfo();
		final int maxTryTimes = 3;         // try 5 times when failed
		final String shakeHandMsgFormat = "正在尝试第(%d/%d)次握手...";
		devShakeTask.message = String.format(shakeHandMsgFormat, 1, maxTryTimes);
		devShakeTask.task = new Callable() {
			@Override
			public Object call() throws Exception
			{
				// need some time to let it wake up
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				return devShakeHand();
			}
		};
		devShakeTask.onPerWorkFinished = new SerialBGWorker.OnPerWorkFinished() {

			int triedTimes = 0;
			@Override
			public boolean onPerWorkFinished(BGWorker.WorkState state, Object reslut)
			{
				boolean needContinue = false;
				++triedTimes;
				switch (state) {
					case SUCCESS:
						if ((Boolean) reslut) {
							outputConsole.unIndent();
							// Changing UI.
							mHandler.post(new Runnable() {
								@Override
								public void run()
								{
									stateManager.changeState(State.OPEN_DOOR);
									outputConsole.printNewItem("连接设备成功");
								}
							});
							needContinue = true;
							break;
						}
						// fall down
					case TIME_OUT:
						if (triedTimes < maxTryTimes) {
							// try again
							devShakeTask.message = String.format(shakeHandMsgFormat, triedTimes+1, maxTryTimes);
							serialBGWorker.addTaskInFirst(devShakeTask);
							needContinue = true;
						} else {
							showWrongDeviceDialog();
							needContinue = false;
						}
						break;

					case EXCEPTION:
						Log.e(TAG, "exception in device shaking, stop trying.");
						needContinue = false;
						break;

					case CANCEL:
						needContinue = false;
						break;

					default:
						Log.w(TAG, "Unhandled state in callback of device shake hand");
						needContinue = true;
						break;
				}

				if (needContinue) {
					return true;
				} else {
					btDisconnect();
					outputConsole.unIndent();
					outputConsole.printNewItem("连接设备失败");
					return false;
				}
			}
		};
		devShakeTask.timeout = 10 * 1000;


		serialBGWorker.addTask(connectSocketTask);
		serialBGWorker.addTask(devShakeTask);
		serialBGWorker.start();

	}


	private boolean connectBTSSPSocket(final BluetoothDevice device)
	{
		// Try to create a SSP socket
		// This step will be finished in some millisecond
		try {
			btSocket = device.createRfcommSocketToServiceRecord(BlueToothSSPUUID);
		} catch (IOException e) {
			btSocket = null;
			Log.e(TAG, "Can not create BlueTooth socket!");
			outputConsole.printNewItem("获取蓝牙串口Socket失败！");
			e.printStackTrace();
			return false;
		}


		// Try to connect this socket
		try {
			btSocket.connect();
		} catch (IOException e) {
			try {
				btSocket.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			btSocket = null;
			Log.e(TAG, "Can not connect BT socket!");
			outputConsole.printNewItem("Socket连接...失败！");
			e.printStackTrace();
			return false;
		}

		outputConsole.printNewItem("Socket连接...成功");
		return true;
	}

	private boolean getSocketStream()
	{
		// Check null~~
		if (btSocket == null || !btSocket.isConnected()) {
			btSocketIn = null;
			btSocketOut = null;
			Log.e(TAG, "Bluetooth socket is null or not connected");
			return false;
		}

		outputConsole.printNewItem("获取Socket输入流...");
		try {
			btSocketIn = btSocket.getInputStream();
			btSocketOut = btSocket.getOutputStream();
		} catch (IOException e) {
			btDisconnect();
			outputConsole.append("失败!");
			Log.e(TAG, "Cannot get bluetooth socket's input or output stream");
			e.printStackTrace();
			return false;
		}
		outputConsole.append("成功");

		return true;
	}

	private boolean devShakeHand()
	{
		if (btSocketIn == null || btSocketOut == null) {
			Log.d(TAG, "Bluetooth socket is null");
			return false;
		}

		int byteRead = -1;
		outputConsole.printNewItem("正在进行设备握手...");
		try {
			btSocketOut.write(0x69);
			btSocketOut.flush();
			byteRead = btSocketIn.read();
		} catch (IOException e) {
			btDisconnect();
			outputConsole.append("失败！（通信出错）");
			e.printStackTrace();
			return false;
		}

		if (byteRead == 0x96) {
			Log.i(TAG, "Device shake hand successful");
			outputConsole.append("成功");
			return true;
		} else {
			Log.e(TAG, "Device respond is error when shaking hand");
			outputConsole.append("失败！（设备未正确回应）");
			return false;
		}

	}

	private boolean openDoor()
	{
		outputConsole.printNewItem("正在发送开门指令...");

		if (btSocketIn == null || btSocketOut == null) {
			return false;
		}

		int byteRead = -1;
		try {
			btSocketOut.write(0x38);
			// TODO
			MyApp.showSimpleToast("指令已发送，等待回应");
			byteRead = btSocketIn.read();
		} catch (IOException e) {
			outputConsole.append("失败！（通信出错）");
			e.printStackTrace();
			return false;
		}

		if (byteRead != 0x83) {
			Log.e(TAG, "Key error when opening door");
			outputConsole.append("失败！（密码错误？）");
			return false;
		} else {
			Log.i(TAG, "Door open successful");
			outputConsole.append("成功");
			stateManager.changeState(State.EXIT);
			return true;
		}
	}

	private final String TAG = "MainActivity";
	private final UUID BlueToothSSPUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

	private OutputConsole outputConsole;

	private BluetoothSocket btSocket;
	private InputStream btSocketIn;
	private OutputStream btSocketOut;

	private AlertDialog exitingWithTurnOffBTDialog;
	private StateManager stateManager;
	private BluetoothAdapter btAdapter;
	private Handler mHandler = new Handler();

	private BroadcastReceiver btDisconnectReceiver;

}
