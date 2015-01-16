package com.tools.h46incon.doorcontroler;

import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;

/**
 * Created by Administrator on 2015/1/16.
 */
public class OutputConsole {
	public OutputConsole(TextView consoleTV)
	{
		this.consoleTV = consoleTV;
		consoleTV.setMovementMethod(new ScrollingMovementMethod());
	}

	public void printNewItem(String str)
	{
		CharSequence oldText = consoleTV.getText();
		consoleTV.setText(oldText + "\n" + str);
	}

	public void append(String str)
	{
		CharSequence oldText = consoleTV.getText();
		consoleTV.setText(oldText + str);
	}

	private TextView consoleTV;

}
