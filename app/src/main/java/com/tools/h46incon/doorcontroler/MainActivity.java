package com.tools.h46incon.doorcontroler;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;


public class MainActivity extends ActionBarActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Button BTSettingBtn = (Button) findViewById(R.id.btsetting_btn);
		BTSettingBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v)
			{
				startActivity(new Intent(Settings.ACTION_BLUETOOTH_SETTINGS));
			}
		});

	}


}
