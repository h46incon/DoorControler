package com.tools.h46incon.doorcontroler.PinInputDialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

import com.tools.h46incon.doorcontroler.R;

/**
 * Created by h46incon on 2015/2/4.
 */
public class PinInputDialog extends DialogFragment{
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState)
	{
		Log.v(TAG, "onCreateDialog");
		// get context
		mContext = getActivity();

		// Not need to add bonded device because they will be found again.

		// Instantiate an AlertDialog.Builder with its constructor
		AlertDialog.Builder builder = new AlertDialog.Builder(
				getActivity(),
				R.style.LightDialog);



		// Get the AlertDialog from create()
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View view = inflater.inflate(R.layout.dialog_pin_input, null);
		final AlertDialog dialog = builder.setView(view).create();
		return dialog;
	}

	@Override
	public void onStart()
	{
		super.onStart();

		// Make dialog bigger
		Dialog dialog = getDialog();
		if (dialog != null) {
			WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
			lp.copyFrom(dialog.getWindow().getAttributes());
			lp.width = WindowManager.LayoutParams.MATCH_PARENT;
			// when set height to MATCH_PARENT, the box is just align to the top
			dialog.getWindow().setAttributes(lp);
		}

	}

	private static final String TAG = "PinInputDialog";
	private Context mContext;
}
