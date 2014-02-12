package org.urbanmyth.ndcalc;

import java.util.ArrayList;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class NDCalcFragment extends Fragment implements OnClickListener, OnInitListener
{
	private String TAG = "NDCalcFragment";
	private SharedPreferences mSettings = null;
	
	private ConstantsHelper mConstantsHelper = null;
	private ArrayList<String> mShutterSpeeds = null;
	private ArrayList<Float> mShutterSpeedsFloats = null;

	private PowerManager mPM = null;
	private PowerManager.WakeLock mWakeLock = null;

	private TextToSpeech mTTS = null;

	private boolean mTimerRunning = false;
	private Handler mTimerCallbackHandler; 
	private Runnable mTimerCallbackRunnable;

	private int mShutterSpeedIndex = 23;
	private int mFilterIndex = 10;

	private float mShutterSpeedInSeconds = 0;
	private int mRemainingTimeInSeconds = 0;

	private TextView mHoursMinutesSeconds = null;
	private ListView mShutterSpeedList = null;
	private ListView mFilterList = null;
	private Button mStartStopButton = null;

	private int secondsInAMinute = 60;
	private int secondsInAnHour = 60 * 60;
	private int secondsInADay = 60 * 60 * 24;

	@Override
	public void onInit(int status)
	{
		if (status == TextToSpeech.SUCCESS) {
			mTTS.setLanguage(Locale.getDefault());
		} else {
			mTTS = null;
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View rootView = inflater.inflate(R.layout.fragment_ndcalc_lists, container, false);
		mTimerCallbackHandler = new Handler();
		mTimerCallbackRunnable = new Runnable() {
			public void run() {
				timerCallbackUpdateCountdown();
			}
		};

		mPM = (PowerManager) getActivity().getSystemService(Context.POWER_SERVICE);

		mSettings = getActivity().getSharedPreferences(getActivity().getPackageName() + "_preferences", 0);
		if (mSettings.getBoolean("screenon", false))
			getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		mWakeLock = mPM.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "NDCalc");

		mConstantsHelper = new ConstantsHelper();
		mConstantsHelper.initialise(getActivity());

		mShutterSpeeds = mConstantsHelper.getShutterSpeedArray();
		mShutterSpeedsFloats = mConstantsHelper.getShutterSpeedFloatsArray();

		mShutterSpeedList = (ListView) rootView.findViewById(R.id.list_shutterspeed);
		ArrayAdapter<String> shutterSpeedListArrayAdapter = new ArrayAdapter<String>(getActivity(), R.layout.dense_list_item, mShutterSpeeds) {
			@Override
			public boolean isEnabled(int position) {
				return (!mTimerRunning);
			}
		};

		mShutterSpeedList.setAdapter(shutterSpeedListArrayAdapter);
		mShutterSpeedList.post(new Runnable() {
			@Override
			public void run() {
				mShutterSpeedList.setItemChecked(mShutterSpeedIndex, true);
				mShutterSpeedList.setSelectionFromTop(mShutterSpeedIndex, mShutterSpeedList.getHeight()/2);
				onSomethingChanged();
			}
		});

		mFilterList = (ListView) rootView.findViewById(R.id.list_filter);
		ArrayAdapter<String> filterListArrayAdapter = new ArrayAdapter<String>(getActivity(), R.layout.dense_list_item, mConstantsHelper.getStopsArray()) {
			@Override
			public boolean isEnabled(int position) {
				return (!mTimerRunning);
			}
		};

		mFilterList.setAdapter(filterListArrayAdapter);
		mFilterList.post(new Runnable() {
			@Override
			public void run() {
				mFilterList.setItemChecked(mFilterIndex, true);
				mFilterList.setSelectionFromTop(mFilterIndex, mFilterList.getHeight()/2);
				onSomethingChanged();
			}
		});

		mShutterSpeedList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapter, View v, int position, long arg3)
			{
				mShutterSpeedIndex = position;
				onSomethingChanged();
			}
		});

		mFilterList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapter, View v, int position, long arg3)
			{
				mFilterIndex = position;
				onSomethingChanged();
			}
		});

		mHoursMinutesSeconds = (TextView) rootView.findViewById(R.id.texttime);

		mStartStopButton = (Button) rootView.findViewById(R.id.startstopbutton);
		mStartStopButton.setOnClickListener(this);

		mTTS = new TextToSpeech(getActivity(), this);
		mTTS.setLanguage(Locale.UK);

		onSomethingChanged();

		return rootView;
	}

	@Override
	public void onDestroyView()
	{
		super.onDestroyView();
		mTimerCallbackHandler.removeCallbacks(mTimerCallbackRunnable);
		if (mTTS != null) {
			mTTS.stop();
			mTTS.shutdown();
		}
	}

	void updateHoursMinutesSeconds(int _HoursMinutesSeconds)
	{
		int days = (int) (_HoursMinutesSeconds / secondsInADay);
		int hours = (int) ((_HoursMinutesSeconds % secondsInADay) / secondsInAnHour);
		int minutes = (int) ((_HoursMinutesSeconds % secondsInAnHour) / secondsInAMinute);
		int seconds = (int) (_HoursMinutesSeconds % secondsInAMinute);

		String hoursminuteseconds = "";
		if (days!=0)
			hoursminuteseconds += String.format("%02d", days) + "d ";
		if (hours!=0)
			hoursminuteseconds += String.format("%02d", hours) + "h ";
		if (minutes!=0)
			hoursminuteseconds += String.format("%02d", minutes) + "m ";
		hoursminuteseconds += String.format("%02d", seconds) + "s";

		if (_HoursMinutesSeconds == 0) {
			mHoursMinutesSeconds.setTextSize(35f);
			mHoursMinutesSeconds.setText("Less than a second.");
		} else {
			mHoursMinutesSeconds.setTextSize(40f);
			mHoursMinutesSeconds.setText(hoursminuteseconds);
		}

		if ((mRemainingTimeInSeconds != 0) && (mSettings.getBoolean("speech", false))) {
			String string_to_read = "";

			// n days
			// one day
			// 23 ... 2 hours
			// one hour
			// 50...40...30...20...10 minutes
			// 9 ... 2 minutes
			// one minute
			// 50...40...30...20...10 seconds
			// 10 ... 1
			if (days > 0) {
				// At least a day, "n days" or "one day"
				if ((_HoursMinutesSeconds % secondsInADay) == 0) {
					if (days == 1)
						string_to_read = "one day.";
					else
						string_to_read = days + " days.";
				}
			} else {
				if (hours > 0) {
					// At least an hour but less than a day, "n hours" or "one hour"
					if ((_HoursMinutesSeconds % secondsInAnHour) == 0) {
						if (hours == 1)
							string_to_read = "one hour.";
						else
							string_to_read = days + " hours.";
					}
				} else {
					if (minutes > 0) {
						// At least one minute but less than an hour, "50..40..30..20..10..9..8..7..6..5..4..3..2 minutes" or "one minute"
						if ((_HoursMinutesSeconds % secondsInAMinute) == 0) {
							if (minutes == 1)
								string_to_read = "one minute.";
							else if ((minutes % 10 == 0))
								string_to_read = minutes + " minutes.";
							else if (minutes < 10)
								string_to_read = minutes + " minutes.";
						}
					} else {
						// less than one minute
						if (seconds <= 10)
							string_to_read = seconds + ".";
						else if ((seconds % 10 == 0))
							string_to_read = seconds + " seconds.";
					}
				}
			}

			mTTS.speak(string_to_read, TextToSpeech.QUEUE_ADD, null);
		}
	}

	public void timerCallbackUpdateCountdown()
	{
		// Log.i(TAG, "timerCallbackUpdateCountdown: " + mRemainingTimeInSeconds);
		mRemainingTimeInSeconds--;
		if (mRemainingTimeInSeconds > 0) {
			// It would be better if this didn't tick every second when handling insanely large times, but it's
			// using right sized call backs is non trivial when you include updating the timer on the screen and
			// whether the screen is on/off.
			//
			// In any case, it's not going to be an issue in the ways I'm going to use this timer
			updateHoursMinutesSeconds(mRemainingTimeInSeconds);
			mTimerCallbackHandler.postDelayed(mTimerCallbackRunnable, 1000); // 1 second
		} else {
			mStartStopButton.setText(R.string.start);
			mHoursMinutesSeconds.setText("Time up!");
			mHoursMinutesSeconds.setTextColor(Color.WHITE);
			mTimerRunning = false;

			if(mSettings.getBoolean("alert", true)) {
				try {
					Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
					Ringtone r = RingtoneManager.getRingtone(getActivity(), notification);
					r.play();
				} catch (Exception e) {}
			}

			if (mSettings.getBoolean("vibrate", true)) {
				Vibrator v = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
				long[] pattern = {0, 1000, 500, 1000, 500, 1000};
				v.vibrate(pattern, -1);
			}

			if (mWakeLock.isHeld())
				mWakeLock.release();

			mRemainingTimeInSeconds = 0;
		}
	}

	private void onSomethingChanged()
	{
		//		Log.i(TAG, "onSomethingChanged: shutterindex " + mShutterSpeedIndex + " filterindex " + mFilterIndex);
		int filterPosition1 = mFilterIndex;
		int filtershift = mConstantsHelper.offsetFromFilterPosition(filterPosition1);
		int newindex = mShutterSpeedIndex + filtershift;
		if (newindex < mShutterSpeedsFloats.size()) {
			mShutterSpeedInSeconds = mConstantsHelper.getShutterSpeed(newindex);
			updateHoursMinutesSeconds((int) mShutterSpeedInSeconds);
		} else {
			mHoursMinutesSeconds.setText("TODO: Set in calendar feature...");
			mHoursMinutesSeconds.setTextSize(30f);
		}
		mHoursMinutesSeconds.setTextColor(Color.WHITE);
	}

	// Wakelock acquire dependent on preventsleep setting being true, eclipse has trouble seeing relationship and warns
	// release not always taken.
	@SuppressLint("Wakelock")
	public void onClick(View v)
	{
		// Log.i(TAG, "onClick");
		switch (v.getId()) {
		case R.id.startstopbutton:
			if (mTimerRunning) {
				// Cancel
				mTimerRunning = false;
				mStartStopButton.setText(R.string.start);
				mTimerCallbackHandler.removeCallbacks(mTimerCallbackRunnable); // Stop timer
				onSomethingChanged();
				mRemainingTimeInSeconds = 0;
				if (mWakeLock.isHeld())
					mWakeLock.release();
			} else {
				// Start
				mTimerRunning = true;
				mStartStopButton.setText(R.string.reset);
				mHoursMinutesSeconds.setTextColor(Color.GREEN);
				mRemainingTimeInSeconds = (int) mShutterSpeedInSeconds;

				// 30 minutes, arbitrary time
				if (mRemainingTimeInSeconds > (30 * secondsInAMinute))
					Toast.makeText(getActivity(), R.string.inefficient_message, Toast.LENGTH_LONG).show();

				if (mSettings.getBoolean("preventsleep", true))
					mWakeLock.acquire();
				mTimerCallbackHandler.postDelayed(mTimerCallbackRunnable, 1000); // Start 1 second timer
			}
			break;
		}
	}	
}