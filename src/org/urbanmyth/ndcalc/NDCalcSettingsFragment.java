package org.urbanmyth.ndcalc;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.ToggleButton;

public class NDCalcSettingsFragment extends Fragment implements OnClickListener
{
	private String TAG = "NDCalcSettingsFragment";
	SharedPreferences mSettings = null;
	private ToggleButton mToggleButtonAlert = null;
	private ToggleButton mToggleButtonVibration = null;
	private ToggleButton mToggleButtonPreventSleep = null;
	private ToggleButton mToggleButtonScreenOn = null;
	private ToggleButton mToggleButtonSpeech = null;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		Context mContext = getActivity();
		View rootView = inflater.inflate(R.layout.fragment_ndcalc_settings, container, false);

		mToggleButtonAlert = (ToggleButton) rootView.findViewById(R.id.toggleButtonAlert);
		mToggleButtonVibration = (ToggleButton) rootView.findViewById(R.id.toggleButtonVibration);
		mToggleButtonPreventSleep = (ToggleButton) rootView.findViewById(R.id.toggleButtonPreventSleep);
		mToggleButtonScreenOn = (ToggleButton) rootView.findViewById(R.id.toggleButtonScreenOn);
		mToggleButtonSpeech = (ToggleButton) rootView.findViewById(R.id.toggleButtonSpeech);

		mSettings = mContext.getSharedPreferences(mContext.getPackageName() + "_preferences", 0);

		mToggleButtonAlert.setChecked(			mSettings.getBoolean("alert", true));
		mToggleButtonVibration.setChecked(		mSettings.getBoolean("vibrate", true));
		mToggleButtonPreventSleep.setChecked(	mSettings.getBoolean("preventsleep", true));
		mToggleButtonScreenOn.setChecked(		mSettings.getBoolean("screenon", false));
		mToggleButtonSpeech.setChecked(			mSettings.getBoolean("speech", false));

		mToggleButtonAlert.setOnClickListener(this);
		mToggleButtonVibration.setOnClickListener(this);
		mToggleButtonPreventSleep.setOnClickListener(this);
		mToggleButtonScreenOn.setOnClickListener(this);
		mToggleButtonSpeech.setOnClickListener(this);

		return rootView;
	}

	@Override
	public void onClick(View v)
	{
		SharedPreferences.Editor editor = mSettings.edit();

		// Log.i(TAG, "Settings: alert: " + mToggleButtonAlert.isChecked() + " vibrate: " + mToggleButtonVibration.isChecked());
		editor.putBoolean("alert", mToggleButtonAlert.isChecked());
		editor.putBoolean("vibrate", mToggleButtonVibration.isChecked());

		// Log.i(TAG, "Settings: prevent sleep: " + mToggleButtonPreventSleep.isChecked() + " screen on: " + mToggleButtonScreenOn.isChecked());
		editor.putBoolean("preventsleep", mToggleButtonPreventSleep.isChecked());
		editor.putBoolean("screenon", mToggleButtonScreenOn.isChecked());

		editor.putBoolean("speech", mToggleButtonSpeech.isChecked());

		if (mSettings.getBoolean("screenon", false))
			getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		editor.commit();
	}
}
