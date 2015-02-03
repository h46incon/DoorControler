package com.tools.h46incon.doorcontroler;

import android.os.Handler;
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
		showingStr.append("\n");
		for (int i = 0; i < indent; ++i) {
			showingStr.append('\t');
		}
		showingStr.append(str);

		this.showStr();
	}

	public void append(String str)
	{
		showingStr.append(str);
		this.showStr();
	}

	public void indent()
	{
		++indent;
	}

	public void unIndent()
	{
		if (indent > 0) {
			--indent;
		}
	}

	private void showStr()
	{
		mHandel.post(setTextRunner);
	}

	private Runnable setTextRunner = new Runnable() {
		@Override
		public void run()
		{
			consoleTV.setText(showingStr);
		}
	};

	private StringBuilder showingStr = new StringBuilder();
	private TextView consoleTV;
	private int indent = 0;
	private static Handler mHandel = new Handler(MyApp.getContext().getMainLooper());


}
