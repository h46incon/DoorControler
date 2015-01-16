package com.tools.h46incon.doorcontroler;

import android.widget.ImageView;

/**
 * Created by Administrator on 2015/1/16.
 */
public class ArrowHLManager {
	public ArrowHLManager(int defaultImageID, int highlightImageID)
	{
		this.defaultImageID = defaultImageID;
		this.highlightImageID = highlightImageID;
	}

	public void highlight(ImageView view)
	{
		unHighlightCurrent();
		view.setImageResource(highlightImageID);
		curHighlightView = view;
	}

	public void unHighlightCurrent()
	{
		if (curHighlightView != null) {
			curHighlightView.setImageResource(defaultImageID);
			curHighlightView = null;
		}
	}

	private ImageView curHighlightView = null;
	private int defaultImageID;
	private int highlightImageID;
}
