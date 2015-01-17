package com.tools.h46incon.doorcontroler;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;


public class MainActivity extends ActionBarActivity {


	private static enum  State{
		BT_SETTING,
		DEV_SHAKEHAND,
		OPEN_DOOR,
		EXIT,

		_STATE_NUMBER

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
			btSettingBtn = (Button) findViewById(R.id.btsetting_btn);
			devShakehandBtn = (Button) findViewById(R.id.devShakeHand_btn);
			openDoorBtn = (Button) findViewById(R.id.openDoor_btn);
			exitBtn = (Button) findViewById(R.id.exit_btn);

			btSettingArrowIV = (ImageView) findViewById(R.id.btSetting_arrow_iv);
			devShakeHandArrowIV = (ImageView) findViewById(R.id.devShakeHand_arrow_iv);
			openDoorArrowIV = (ImageView) findViewById(R.id.openDoor_arrow_iv);
			exitArrowIV = (ImageView) findViewById(R.id.exit_arrow_iv);

			btSettingInfoTV = (TextView) findViewById(R.id.btInfo_tv);
			devShakeHandInfoTV = (TextView) findViewById(R.id.devShakeHandInfo_tv);

			// store views into array
			buttons = new Button[stateNum];
			buttons[State.BT_SETTING.ordinal()] = btSettingBtn;
			buttons[State.DEV_SHAKEHAND.ordinal()] = devShakehandBtn;
			buttons[State.OPEN_DOOR.ordinal()] = openDoorBtn;
			buttons[State.EXIT.ordinal()] = exitBtn;

			imageViews = new ImageView[stateNum];
			imageViews[State.BT_SETTING.ordinal()] = btSettingArrowIV;
			imageViews[State.DEV_SHAKEHAND.ordinal()] = devShakeHandArrowIV;
			imageViews[State.OPEN_DOOR.ordinal()] = openDoorArrowIV;
			imageViews[State.EXIT.ordinal()] = exitArrowIV;

			// init other member
			this.arrowHLManager =
					new ArrowHLManager(R.drawable.arrow_default, R.drawable.arrow_highlight);
		}

		private final int stateNum = State._STATE_NUMBER.ordinal();
		private final String TAG = "StateManager";

		private State curState;
		private ArrowHLManager arrowHLManager;
		private OnBTClick onBTClick;

		// Arrays of views
		private Button[] buttons;
		private ImageView[] imageViews;
		// Views
		private Button btSettingBtn;
		private Button devShakehandBtn;
		private Button openDoorBtn;
		private Button exitBtn;
		private ImageView btSettingArrowIV;
		private ImageView devShakeHandArrowIV;
		private ImageView openDoorArrowIV;
		private ImageView exitArrowIV;
		private TextView btSettingInfoTV;
		private TextView devShakeHandInfoTV;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		initMember();

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
					case EXIT:
						exiting();
				}

				// TODO: test code
				if (state != State.EXIT) {
					stateManager.changeState(State.values()[state.ordinal() + 1]);
				}
			}
		};
		stateManager = new StateManager(onBTClick, State.BT_SETTING);

		Log.d(TAG, "current bt state: " + btAdapter.getState());
		if (btAdapter.getState() == BluetoothAdapter.STATE_CONNECTED) {
			Log.d(TAG, "BT is connected");
			stateManager.changeState(State.DEV_SHAKEHAND);
		}
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
		if (!btAdapter.isEnabled()) {
			btAdapter.enable();
		}

		BTDiscoveryDialog btDiscoveryDialog = new BTDiscoveryDialog();
		btDiscoveryDialog.setOnDevSelectListener(onBTDevSelectedListener);
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

	private BTDiscoveryDialog.OnDevSelect onBTDevSelectedListener = new BTDiscoveryDialog.OnDevSelect() {
		@Override
		public void onDevSelect(BluetoothDevice device)
		{
			Log.d(TAG, "received selected device " + device.getAddress());
		}
	};

	private final String TAG = "MainActivity";
	private static final int REQUEST_ENABLE_BT = 0x67;
	private OutputConsole outputConsole;
	private AlertDialog exitingWithTurnOffBTDialog;
	private StateManager stateManager;
	private BluetoothAdapter btAdapter;


}
