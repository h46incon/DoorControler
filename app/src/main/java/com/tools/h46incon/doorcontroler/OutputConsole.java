package com.tools.h46incon.doorcontroler;

import android.os.Handler;
import android.text.Layout;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;

/**
 * Created by h46incon on 2015/1/16.
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

			// Reference: http://stackoverflow.com/questions/3506696/auto-scrolling-textview-in-android-to-bring-text-into-view
			// find the amount we need to scroll.  This works by
			// asking the TextView's internal layout for the position
			// of the final line and then subtracting the TextView's height
			Layout layout = consoleTV.getLayout();
			if (layout != null) {
				final int scrollAmount =  layout.getLineTop(consoleTV.getLineCount()) - consoleTV.getHeight();
				// if there is no need to scroll, scrollAmount will be <=0
				if (scrollAmount > 0)
					consoleTV.scrollTo(0, scrollAmount);
				else
					consoleTV.scrollTo(0, 0);
			}
		}
	};

	private StringBuilder showingStr = new StringBuilder();
	private TextView consoleTV;
	private int indent = 0;
	private static Handler mHandel = new Handler(MyApp.getContext().getMainLooper());


}
