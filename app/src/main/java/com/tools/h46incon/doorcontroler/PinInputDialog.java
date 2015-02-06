package com.tools.h46incon.doorcontroler;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

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

		keyboardView = (KeyboardView) view.findViewById(R.id.pin_box_keyboard);
		initKeyboard();

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
			lp.width = getResources().getDimensionPixelSize(R.dimen.pin_input_dialog_width);
//			lp.width = WindowManager.LayoutParams.MATCH_PARENT;
			// when set height to MATCH_PARENT, the box is just align to the top
			dialog.getWindow().setAttributes(lp);
		}

	}

	private void initKeyboard()
	{
		Keyboard keyboard = new Keyboard(mContext, R.xml.digit_kb);
		keyboardView.setKeyboard(keyboard);
		keyboardView.setEnabled(true);
		keyboardView.setPreviewEnabled(false);
		keyboardView.setOnKeyboardActionListener(new KeyboardView.OnKeyboardActionListener() {
			@Override
			public void onPress(int primaryCode)
			{

			}

			@Override
			public void onRelease(int primaryCode)
			{

			}

			@Override
			public void onKey(int primaryCode, int[] keyCodes)
			{

			}

			@Override
			public void onText(CharSequence text)
			{

			}

			@Override
			public void swipeLeft()
			{

			}

			@Override
			public void swipeRight()
			{

			}

			@Override
			public void swipeDown()
			{

			}

			@Override
			public void swipeUp()
			{

			}
		});
	}

	private static final String TAG = "PinInputDialog";
	private Context mContext;
	private KeyboardView keyboardView;
}
