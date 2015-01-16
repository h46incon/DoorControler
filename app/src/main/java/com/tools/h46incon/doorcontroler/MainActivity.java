package com.tools.h46incon.doorcontroler;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;


public class MainActivity extends ActionBarActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		initMember();

		btSettingBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v)
			{
//				startActivity(new Intent(Settings.ACTION_BLUETOOTH_SETTINGS));
				outputConsole.printNewItem("time " + System.currentTimeMillis());
				arrowHLManager.highlight(btSettingArrowIV);
			}
		});

		devShakehandBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v)
			{
				arrowHLManager.highlight(devShakeHandArrowIV);
			}
		});
	}

	private void initMember()
	{
		TextView consoleTV = (TextView) findViewById(R.id.console_tv);
		outputConsole = new OutputConsole(consoleTV);

		arrowHLManager = new ArrowHLManager(R.drawable.arrow_default, R.drawable.arrow_highlight);

		// get views
		btSettingBtn = (Button) findViewById(R.id.btsetting_btn);
		devShakehandBtn = (Button) findViewById(R.id.devShakeHand_btn);
		openDoorBtn = (Button) findViewById(R.id.openDoor_btn);
		exitBtn = (Button) findViewById(R.id.exit_btn);

		btSettingArrowIV = (ImageView) findViewById(R.id.btSetting_arrow_iv);
		devShakeHandArrowIV = (ImageView) findViewById(R.id.devShakeHand_arrow_iv);
		openDoorArrowIV = (ImageView) findViewById(R.id.openDoor_arrow_iv);
		exitArrowArrowIV = (ImageView) findViewById(R.id.exit_arrow_iv);

		btSettingInfoTV = (TextView) findViewById(R.id.btInfo_tv);
		devShakeHandInfoTV = (TextView) findViewById(R.id.devShakeHandInfo_tv);
	}

	private OutputConsole outputConsole;
	private ArrowHLManager arrowHLManager;
	// Views
	private Button btSettingBtn;
	private Button devShakehandBtn;
	private Button openDoorBtn;
	private Button exitBtn;
	private ImageView btSettingArrowIV;
	private ImageView devShakeHandArrowIV;
	private ImageView openDoorArrowIV;
	private ImageView exitArrowArrowIV;
	private TextView btSettingInfoTV;
	private TextView devShakeHandInfoTV;


}
