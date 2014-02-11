package org.urbanmyth.ndcalc;

import java.util.ArrayList;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

public class ConstantsHelper
{
	private static final int MAX_STOP_COUNT = 25;
	private String TAG = "ConstantsHelper";
	public ArrayList<String> mShutterSpeeds = null;
	public ArrayList<Float> mShutterSpeedsFloats = null;
	public ArrayList<String> mStopStrings = null;

	public ArrayList<String> getShutterSpeedArray()
	{
		return mShutterSpeeds;
	}

	public ArrayList<Float> getShutterSpeedFloatsArray()
	{
		return mShutterSpeedsFloats;
	}

	public float getShutterSpeed(int _position)
	{
		if (_position > mShutterSpeedsFloats.size())
			return 0;

		return mShutterSpeedsFloats.get(_position);
	}

	public ArrayList<String> getStopsArray()
	{
		return mStopStrings;
	}

	public void initialise(Context _context)
	{
		Resources r = _context.getResources();
		int[] fractional = r.getIntArray(R.array.fractional_shutter_speeds);
		int[] whole = r.getIntArray(R.array.whole_shutter_speeds);

		mShutterSpeeds = new ArrayList<String>();
		mShutterSpeedsFloats = new ArrayList<Float>();
		mStopStrings =  new ArrayList<String>();

		// Main body of fractional values
		for (int tmp_fraction : fractional) {
			float value = ((float) 1 / (float) tmp_fraction);
			mShutterSpeeds.add("1/" + tmp_fraction + " s");
			mShutterSpeedsFloats.add(value);
		}
		// Fractional special cases
		float first_special_cases[] = {2.5f, 2f, 1.6f, 1.3f};
		for (float tmp_fraction : first_special_cases) {
			float value = ((float) 1 / (float) tmp_fraction);
			int value_int = (int) tmp_fraction;
			if (value_int == tmp_fraction) {
				mShutterSpeeds.add("1/" + value_int + " s");
			} else {
				mShutterSpeeds.add("1/" + tmp_fraction + " s");
			}
			mShutterSpeedsFloats.add(value);
		}

		mShutterSpeeds.add("1 s");
		mShutterSpeedsFloats.add(1f);

		// The special cases larger than 1s
		float second_special_cases[] = {1.3f, 1.6f, 2f, 2.5f};
		for (float tmp_fraction : second_special_cases) {
			float value = ((float) 1 / (float) tmp_fraction);
			int value_int = (int) tmp_fraction;
			if (value_int == tmp_fraction) {
				mShutterSpeeds.add(value_int + " s");
			} else {
				mShutterSpeeds.add(tmp_fraction + " s");
			}

			mShutterSpeedsFloats.add(value);
		}

		// Main body of speeds larger than 1s
		for (int tmp_whole : whole) {
			if (tmp_whole <= 30) {
				mShutterSpeeds.add(Integer.toString(tmp_whole) + " s");
			}
			mShutterSpeedsFloats.add((float)tmp_whole);
		}

		//		Log.i(TAG, "arraylist contains: " + mShutterSpeeds.size());
		//		int i = 0;
		//		for (String tmp_speed : mShutterSpeeds) {
		//			Log.i(TAG, "value "+ i + ": " + tmp_speed);
		//			i++;
		//		}
		//		Log.i(TAG, "floatlist contains: " + mShutterSpeedsFloats.size());
		//		    	i = 0;
		//			    for (Float tmp_speed : mShutterSpeedsFloats) {
		//			    	Log.i(TAG, "value "+ i + ": " + tmp_speed);
		//			    	i++;
		//			    }

		
		// Build the array of labels for the filter list
		mStopStrings.add("No Filter");
		mStopStrings.add("1 stop : ND 0.3");
		for (int stop_count = 2 ; stop_count <= MAX_STOP_COUNT; stop_count++) {
			float nd_value = (float) stop_count * 0.3f;
			mStopStrings.add(stop_count + " stops : ND " + String.format("%.1f", nd_value));
		}

		//		for (int tmp_stop = 0 ; tmp_stop <= MAX_STOP_COUNT; tmp_stop++)
		//		{
		//			Log.i(TAG, "Stops " + mStopStrings.get(tmp_stop) + " is " + offsetFromFilterPosition(tmp_stop));
		//		}
	}

	public int offsetFromFilterPosition(int _position)
	{
		return (_position * 3);
	}
}
