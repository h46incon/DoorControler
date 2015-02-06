package com.tools.h46incon.doorcontroler;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
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
public class PinInputDialog extends DialogFragment {
	public static interface OnPinInputFinish {
		public void onFinish(boolean isSuccess, char[] input);
	}

	public void setOnPinInputFinish(OnPinInputFinish onPinInputFinish)
	{
		this.onPinInputFinish = onPinInputFinish;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState)
	{
		Log.v(TAG, "onCreateDialog");
		// get context
		mContext = getActivity();


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

		// cancel setting
		dialog.setCancelable(true);
		dialog.setCanceledOnTouchOutside(false);
		View cancel_btn = view.findViewById(R.id.pin_box_close_btn);
		cancel_btn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v)
			{
				PinInputDialog.this.dismiss();
			}
		});

		return dialog;
	}

	@Override
	public void onDismiss(DialogInterface dialog)
	{
		super.onDismiss(dialog);
		// Check if this cancel is cause by a successful input
		if (!hasSuccess) {
			this.inputFinish(false);
		}
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
				onKeyInput(primaryCode);
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

	private void inputFinish(boolean isSuccess)
	{
		if (isSuccess) {
			// Check if input enough pins
			if (pins_index == pinNum) {
				// Check null
				if (onPinInputFinish != null) {
					onPinInputFinish.onFinish(true, pins);
				}

			} else {
				Log.e(TAG, "Not enough pin inputted, bits: " + pins_index);
			}
		} else {
			// Check null
			if (onPinInputFinish != null) {
				onPinInputFinish.onFinish(false, null);
			}
		}

	}

	private void onKeyInput(int primaryCode)
	{
		if (primaryCode == Keyboard.KEYCODE_DELETE) {   // Delete
			if (pins_index > 0) {
				// reset unneeded char
				pins[pins_index] = 0;
				--pins_index;
				// TODO: UI
			}
		} else if (primaryCode == 4869) {       // Hidden egg
			// TODO: Hidden egg
		} else {
			// legal input is ASCII code
			if (primaryCode >= 0 && primaryCode <= 127) {
				pins[pins_index] = (char) primaryCode;
				++pins_index;
				// TODO: UI
				if (pins_index == pinNum) {
					inputFinish(true);
					hasSuccess = true;
					this.dismiss();
				}
			} else {
				Log.w(TAG, "Illegal pin, code: " + primaryCode);
			}
		}


	}

	private static final String TAG = "PinInputDialog";
	private static final int pinNum = 6;

	private Context mContext;
	private KeyboardView keyboardView;
	private OnPinInputFinish onPinInputFinish = null;

	// has input success
	private boolean hasSuccess = false;

	// inputted pins
	private char[] pins = new char[pinNum];
	private int pins_index = 0;

}
