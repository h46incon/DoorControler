package com.tools.h46incon.doorcontroler.PinInput;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import com.tools.h46incon.doorcontroler.R;

/**
 * Created by h46incon on 2015/2/4.
 */
public class PinInputDialog extends DialogFragment {
	public static interface OnPinInputFinish {
		public void onFinish(boolean isSuccess, char[] input);
	}

	public PinInputDialog setOnPinInputFinish(OnPinInputFinish onPinInputFinish)
	{
		this.onPinInputFinish = onPinInputFinish;
		return this;
	}

	public PinInputDialog setMessage(CharSequence message)
	{
		this.message = message;
		return this;
	}

	public PinInputDialog setTitle(CharSequence title)
	{
		this.title = title;
		return this;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState)
	{
		Log.v(TAG, "onCreateDialog");
		// get context
		mContext = getActivity();

		if (shouldVibrate()) {
			vibrator = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
			shouldVibrate = true;
		} else {
			shouldVibrate = false;
		}


		// Instantiate an AlertDialog.Builder with its constructor
		AlertDialog.Builder builder = new AlertDialog.Builder(
				getActivity(),
				R.style.LightDialog);


		// Get the AlertDialog from create()
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View rootView = inflater.inflate(R.layout.dialog_pin_input, null);

		SetTextViewText(rootView, R.id.pin_box_title, this.title);
		SetTextViewText(rootView, R.id.pin_pox_message_text, this.message);

		pinBoxHighLighter = new PinBoxHighLighter(rootView);
		keyboardView = (KeyboardView) rootView.findViewById(R.id.pin_box_keyboard);
		initKeyboard();

		final AlertDialog dialog = builder.setView(rootView).create();


		// cancel setting
		dialog.setCancelable(true);
		dialog.setCanceledOnTouchOutside(false);
		View cancel_btn = rootView.findViewById(R.id.pin_box_close_btn);
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

	private static class PinBoxHighLighter{
		public PinBoxHighLighter(View dialogRootView)
		{
			initPinBoxViews(dialogRootView);
		}

		public void setHighLightedNum(int num)
		{
			if (num < 0 || num > pinNum) {
				Log.e(TAG, "Wrong pin box high lighted number: " + num);
				return;
			}

			// High light
			for (int i = highLightedNum; i < num; ++i) {
				pinBoxViews[i].setBackgroundResource(R.drawable.pin_input_bullet);
			}

			// Un high light
			for (int i = num; i < highLightedNum; i++) {
				pinBoxViews[i].setBackgroundColor(Color.TRANSPARENT);
			}

			highLightedNum = num;
		}

		private void initPinBoxViews(View dialogRootView)
		{
			ViewGroup pin_boxes = (ViewGroup) dialogRootView.findViewById(R.id.pin_boxes);

			ViewGroup pin_box_layout;

			int i = 0;
			pin_box_layout = (ViewGroup) pin_boxes.findViewById(R.id.pin_box_layout_1);
			pinBoxViews[i++] = pin_box_layout.findViewById(R.id.pin_box);

			pin_box_layout = (ViewGroup) pin_boxes.findViewById(R.id.pin_box_layout_2);
			pinBoxViews[i++] = pin_box_layout.findViewById(R.id.pin_box);

			pin_box_layout = (ViewGroup) pin_boxes.findViewById(R.id.pin_box_layout_3);
			pinBoxViews[i++] = pin_box_layout.findViewById(R.id.pin_box);

			pin_box_layout = (ViewGroup) pin_boxes.findViewById(R.id.pin_box_layout_4);
			pinBoxViews[i++] = pin_box_layout.findViewById(R.id.pin_box);

			pin_box_layout = (ViewGroup) pin_boxes.findViewById(R.id.pin_box_layout_5);
			pinBoxViews[i++] = pin_box_layout.findViewById(R.id.pin_box);

			pin_box_layout = (ViewGroup) pin_boxes.findViewById(R.id.pin_box_layout_6);
			pinBoxViews[i++] = pin_box_layout.findViewById(R.id.pin_box);
		}

		private View[] pinBoxViews = new View[pinNum];
		private int highLightedNum = 0;
	}

	private boolean shouldVibrate()
	{
		AudioManager audioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
		if (audioManager.getRingerMode() == AudioManager.RINGER_MODE_SILENT) {
			return false;
		} else {
			// The notification manager will not vibrate if the policy doesn't allow it,
			// so the client should always set a vibrate pattern,
			// and let the notification manager control whether or not to actually vibrate.
			return true;
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
		// Trigger a vibrator
		final int vibrateTime = 10;     //ms
		if (shouldVibrate) {
			vibrator.vibrate(vibrateTime);
		}

		// Special key code handler
		// Delete key
		if (primaryCode == Keyboard.KEYCODE_DELETE) {
			if (pins_index > 0) {
				// reset unneeded char
				pins[pins_index] = 0;
				--pins_index;
				pinBoxHighLighter.setHighLightedNum(pins_index);
			}
		}

		// Hidden Egg....
		else if (primaryCode == 4869) {
			AlertDialog.Builder builder = new AlertDialog.Builder(
					getActivity(),
					R.style.LightDialog);

			builder.setTitle("哇这又是一个对话框")
					.setMessage("被吓一跳了吧（づ￣3￣）づ");
			builder.create().show();
		}

		// Normal character input
		else {
			// legal input is ASCII code
			if (primaryCode >= 0 && primaryCode <= 127) {
				pins[pins_index] = (char) primaryCode;
				++pins_index;
				pinBoxHighLighter.setHighLightedNum(pins_index);

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

	private static void SetTextViewText(View rootView, int textViewID, CharSequence text)
	{
		if (text != null) {
			TextView textView = (TextView) rootView.findViewById(textViewID);
			if (textView != null) {
				textView.setText(text);
			} else {
				Log.w(TAG, "Could not find text view: " + textViewID);
			}
		}
	}


	private static final String TAG = "PinInputDialog";
	private static final int pinNum = 6;
	private static boolean shouldVibrate;

	private Context mContext;
	private KeyboardView keyboardView;
	private Vibrator vibrator;
	private OnPinInputFinish onPinInputFinish = null;
	private PinBoxHighLighter pinBoxHighLighter;

	private CharSequence title;
	private CharSequence message;

	// has input success
	private boolean hasSuccess = false;

	// inputted pins
	private char[] pins = new char[pinNum];
	private int pins_index = 0;

}
