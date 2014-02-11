package org.urbanmyth.ndcalc;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends FragmentActivity implements
ActionBar.TabListener {

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a
	 * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
	 * will keep every loaded fragment in memory. If this becomes too memory
	 * intensive, it may be best to switch to a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	SectionsPagerAdapter mSectionsPagerAdapter;
	
	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_viewpager);

		// Set up the action bar.
		final ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		// Create the adapter that will return a fragment for each of the three
		// primary sections of the app.
		mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);

		// When swiping between different sections, select the corresponding
		// tab. We can also use ActionBar.Tab#select() to do this if we have
		// a reference to the Tab.
		mViewPager
		.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
				actionBar.setSelectedNavigationItem(position);
			}
		});

		// For each of the sections in the app, add a tab to the action bar.
		for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
			// Create a tab with text corresponding to the page title defined by
			// the adapter. Also specify this Activity object, which implements
			// the TabListener interface, as the callback (listener) for when
			// this tab is selected.
			actionBar.addTab(actionBar.newTab()
					.setText(mSectionsPagerAdapter.getPageTitle(i))
					.setTabListener(this));
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId()) {
		case R.id.action_settings:
		{
			about_dialog();
			return true;
		}
		default:
			return super.onOptionsItemSelected(item);
		}
	}


	private void about_dialog() {
		// Pop up a dialog with the text in it
		String dialog_text =
				"Utility for calculating and counting down exposure times when using neutral density (ND) filters.\n\n" +
				"Select your starting shutterspeed and intended filter combination, pressing Start will countdown the exposure and Reset will stop the countdown.\n\n" +
				"Options tab has toggle buttons to control playing the default alert tone and vibration when time is up and enabling the speech functionality.\n\n" +
				"When speech is enabled the time remaining is read at interesting points - when the largest unit changes and multiples of tens of minutes or seconds when they become significant." +
				"The count during powersave option will permit the phone to switch off the screen and input devices, but will keep the CPU active. An option is also provided to try and keep the screen during the countdown - these will consume power and your battery mileage may vary!\n\n" +
				"In memory of the Tower Bridge dawn shoot Dec 2013 with RyanH. and PhilS.\n\n" +
				"  Simon Trimmer\n  ndcalc@urbanmyth.org\n\n";
		try {
			ApplicationInfo applicationInfo = getPackageManager().getApplicationInfo(getPackageName(), 0);
			ZipFile zipFile = new ZipFile(applicationInfo.sourceDir);
			ZipEntry zipFileEntry = zipFile.getEntry("classes.dex");
			long fileTimeStamp = zipFileEntry.getTime();
			zipFile.close();

			String dateFormatter = SimpleDateFormat.getInstance().format(new java.util.Date(fileTimeStamp));
			dialog_text += "Package Built: " + dateFormatter + "\n";

			PackageManager packageManager = this.getPackageManager();
			PackageInfo info = packageManager.getPackageInfo(this.getPackageName(), 0);
			dialog_text += "Package versionCode: " + info.versionCode +
					"\nPackage versionName: " + info.versionName + "\n";
		} catch(Exception e) {
		}

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(dialog_text)
		.setTitle(R.string.about_nd_calc);
		builder.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
			}
		});
		AlertDialog dialog = builder.create();
		dialog.show();
	}

	@Override
	public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
		// When the given tab is selected, switch to the corresponding page in
		// the ViewPager.
		mViewPager.setCurrentItem(tab.getPosition());
	}

	@Override
	public void onTabUnselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

	@Override
	public void onTabReselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter
	{
		public SectionsPagerAdapter(FragmentManager fm)
		{
			super(fm);
		}

		@Override
		public Fragment getItem(int position)
		{
			if (position == 0) {
				Fragment fragment = new NDCalcFragment();
				return fragment;
			} else { // if (position == 1), making a benign default
				Fragment fragment = new NDCalcSettingsFragment();
				return fragment;
			}
		}

		@Override
		public int getCount()
		{
			return 2;
		}

		@Override
		public CharSequence getPageTitle(int position)
		{
			Locale l = Locale.getDefault();
			switch (position) {
			case 0:
				return getString(R.string.title_section1).toUpperCase(l);
			case 1:
				return getString(R.string.title_section2).toUpperCase(l);
			}
			return null;
		}
	}
}
