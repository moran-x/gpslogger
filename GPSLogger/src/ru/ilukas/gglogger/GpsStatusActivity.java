/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ru.ilukas.gglogger;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;
import ru.ilukas.gglogger.common.IActionListener;
import ru.ilukas.gglogger.common.Session;
import ru.ilukas.gglogger.common.Utilities;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;

public class GpsStatusActivity extends SherlockActivity implements
		IGpsLoggerServiceClient, View.OnClickListener, IActionListener {
	private final static String TAG = "GpsStatusActivity";
	static boolean satelliteActive = false;

	private Resources mRes;

	private TextView mLatitudeView;
	private TextView mLongitudeView;
	private TextView mFixTimeView;
	private TextView mTTFFView;
	private TextView mAltitudeView;
	private TextView mAccuracyView;
	private TextView mSpeedView;
	private TextView mBearingView;
	private SvGridAdapter mAdapter;

	private int mSvCount;
	private int mPrns[];
	private float mSnrs[];
	private float mSvElevations[];
	private float mSvAzimuths[];
	private int mEphemerisMask;
	private int mAlmanacMask;
	private int mUsedInFixMask;
	private long mFixTime;
	private static final int PRN_COLUMN = 0;
	private static final int SNR_COLUMN = 1;
	private static final int ELEVATION_COLUMN = 2;
	private static final int AZIMUTH_COLUMN = 3;
	private static final int FLAGS_COLUMN = 4;
	private static final int COLUMN_COUNT = 5;

	private static final String EMPTY_LAT_LONG = "             ";

	/**
	 * General all purpose handler used for updating the UI from threads.
	 */
	private static Intent serviceIntent;
	/**
	 * Provides a connection to the GPS Logging Service
	 */
	private final ServiceConnection gpsServiceConnection = new ServiceConnection() {

		public void onServiceDisconnected(ComponentName name) {
		}

		public void onServiceConnected(ComponentName name, IBinder service) {
			((GpsLoggingService.GpsLoggingBinder) service)
					.getService();
			GpsLoggingService.SetServiceClient(GpsStatusActivity.this);

			if (Session.isStarted()) {

				DisplaySatelliteInfo(Session.getCurrentLocationInfo());
			}

			// Form setup - toggle button, display existing location info
		}
	};

	public void onStart() {
		super.onStart();
		// satelliteActive = true;
		Log.d(TAG, "GpsStatusActivity onStart");
		BindService();
	}

	@Override
	public void onStop() {
		super.onStop();
		Log.d(TAG, "GpsStatusActivity onStart");
		UnbindService();
	}
	
	private void BindService() {
		Log.d(TAG, "GpsStatusActivity BindService - binding now");
		serviceIntent = new Intent(this, GpsLoggingService.class);
		// Now bind to service
		bindService(serviceIntent, gpsServiceConnection,
				Context.BIND_AUTO_CREATE);
	}
	
	private void UnbindService() {
		Log.d(TAG, "GpsStatusActivity unBindService - binding now");
		unbindService(gpsServiceConnection);
	}
	public boolean onOptionsItemSelected(MenuItem item) {
		int itemId = item.getItemId();
		Utilities.LogInfo("Option item selected - "
				+ String.valueOf(item.getTitle()));
		switch (itemId) {
		case android.R.id.home:
            Intent intent = new Intent(this, GpsMainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            break;
		}
		return false;
	}

	public void DisplaySatelliteInfo(Location loc) {
		if (loc == null) {
			return;
		}
		Log.d(TAG, "GpsStatusActivity onLocationChanged");
		mLatitudeView.setText((String.valueOf(String.format("%.5f", loc.getLatitude()))) + " ");
		mLongitudeView.setText((String.valueOf(String.format("%.5f", loc.getLongitude()))) + " ");
		mFixTime = Session.getLatestTimeStamp();

		if (loc.hasAltitude()) {
			mAltitudeView.setText(String.valueOf(String.format("%.5f", (loc.getAltitude()))) + " m");
		} else {
			mAltitudeView.setText("");
		}
		if (loc.hasAccuracy()) {
			mAccuracyView.setText(String.valueOf(loc.getAccuracy()) + " m");
		} else {
			mAccuracyView.setText("");
		}
		if (loc.hasSpeed()) {
			mSpeedView.setText(String.valueOf(String.format("%.5f", (loc.getSpeed()))) + " m/sec");
		} else {
			mSpeedView.setText("");
		}
		if (loc.hasBearing()) {
			mBearingView.setText(String.valueOf(loc.getBearing()) + " deg");
		} else {
			mBearingView.setText("");
		}
		updateStatus(Session.getSatelliteslInfo());
		// updateFixTime();
	}

	public void OnLocationUpdate(Location loc) {
		Log.d(TAG, "GpsStatusActivity OnLocationUpdate");
		DisplaySatelliteInfo(loc);
	}

	public void onStatusChanged(String provider, int status, Bundle extras) {
		// ignore
	}

	public void onProviderEnabled(String provider) {
		// ignore
	}

	public void onProviderDisabled(String provider) {
		// ignore
	}

	private class SvGridAdapter extends BaseAdapter {
		public SvGridAdapter(Context c) {
			mContext = c;
		}

		public int getCount() {
			// add 1 for header row
			return (mSvCount + 1) * COLUMN_COUNT;
		}

		public Object getItem(int position) {
			Log.d(TAG, "GpsStatusActivity getItem(" + position + ")");
			return "foo";
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			TextView textView;
			if (convertView == null) {
				textView = new TextView(mContext);
			} else {
				textView = (TextView) convertView;
			}

			int row = position / COLUMN_COUNT;
			int column = position % COLUMN_COUNT;
			CharSequence text = null;

			if (row == 0) {
				switch (column) {
				case PRN_COLUMN:
					text = mRes.getString(R.string.gps_prn_column_label);
					break;
				case SNR_COLUMN:
					text = mRes.getString(R.string.gps_snr_column_label);
					break;
				case ELEVATION_COLUMN:
					text = mRes.getString(R.string.gps_elevation_column_label);
					break;
				case AZIMUTH_COLUMN:
					text = mRes.getString(R.string.gps_azimuth_column_label);
					break;
				case FLAGS_COLUMN:
					text = mRes.getString(R.string.gps_flags_column_label);
					break;
				}
			} else {
				if (mPrns[row]>0){
				row--;
				switch (column) {
				case PRN_COLUMN:
					text = Integer.toString(mPrns[row]);
					break;
				case SNR_COLUMN:
					text = Float.toString(mSnrs[row]);
					break;
				case ELEVATION_COLUMN:
					text = Float.toString(mSvElevations[row]);
					break;
				case AZIMUTH_COLUMN:
					text = Float.toString(mSvAzimuths[row]);
					break;
				case FLAGS_COLUMN:
					char[] flags = new char[3];
					flags[0] = (char) mEphemerisMask;
					flags[1] = (char) mAlmanacMask;
					flags[2] = (char) mUsedInFixMask;
					/*flags[0] = (mEphemerisMask < 1 ? ' ' : 'E');
					flags[1] = (mAlmanacMask < 1 ? ' ' : 'A');
					flags[2] = (mUsedInFixMask < 1 ? ' ' : 'U');
					flags[0] = ((mEphemerisMask & (1 << (mPrns[row] - 1))) == 0 ? ' '
							: 'E');
					flags[1] = ((mAlmanacMask & (1 << (mPrns[row] - 1))) == 0 ? ' '
							: 'A');
					flags[2] = ((mUsedInFixMask & (1 << (mPrns[row] - 1))) == 0 ? ' '
							: 'U');*/
					text = new String(flags);
					break;
				}
				}
			}

			textView.setText(text);

			return textView;
		}

		private Context mContext;
	}

	@Override
	public void onCreate(Bundle icicle) {
		Log.d(TAG, "GpsStatusActivity onCreate");
		super.onCreate(icicle);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		mRes = getResources();
		setContentView(R.layout.gps_status);

		mLatitudeView = (TextView) findViewById(R.id.latitude);
		mLongitudeView = (TextView) findViewById(R.id.longitude);
		mFixTimeView = (TextView) findViewById(R.id.fix_time);
		mTTFFView = (TextView) findViewById(R.id.ttff);
		mAltitudeView = (TextView) findViewById(R.id.altitude);
		mAccuracyView = (TextView) findViewById(R.id.accuracy);
		mSpeedView = (TextView) findViewById(R.id.speed);
		mBearingView = (TextView) findViewById(R.id.bearing);

		mLatitudeView.setText(EMPTY_LAT_LONG);
		mLongitudeView.setText(EMPTY_LAT_LONG);

		GridView gridView = (GridView) findViewById(R.id.sv_grid);
		mAdapter = new SvGridAdapter(this);
		gridView.setAdapter(mAdapter);
		gridView.setFocusable(false);
		gridView.setFocusableInTouchMode(false);

		// GpsMainActivity.getInstance().addSubActivity(this);
	}

	public void ClearForm() {
		mLatitudeView.setText(EMPTY_LAT_LONG);
		mLongitudeView.setText(EMPTY_LAT_LONG);
		mFixTime = 0;
		mTTFFView.setText("");
		mAltitudeView.setText("");
		mAccuracyView.setText("");
		mSpeedView.setText("");
		mBearingView.setText("");
		mSvCount = 0;
		mAdapter.notifyDataSetChanged();
	}

	@Override
	public void onResume() {
		super.onResume();
		satelliteActive = true;
		Log.d(TAG, "GpsStatusActivity onResume");
		BindService();
	}
	
	private void updateStatus(double info[][]) {

		if (info[0].length<=0)
		{
			return;
		}
		mSvCount = 0;
        if (mPrns == null) {
	        
            mPrns = new int[info[0].length];
            mSnrs = new float[info[0].length];
            mSvElevations = new float[info[0].length];
            mSvAzimuths = new float[info[0].length];
        }
        mEphemerisMask = 0;
        mAlmanacMask = 0;
        mUsedInFixMask = 0;
		for (int i = 0; i < info[0].length; i++) {
			int prn = (int) info[0][i];
            //int prnBit = (1 << (prn - 1));	
            mPrns[mSvCount] = prn;
            mSnrs[mSvCount] = (float) info[1][i];
            mSvElevations[mSvCount] = (float) info[2][i];
            mSvAzimuths[mSvCount] = (float) info[3][i];
            mEphemerisMask = (int) info[4][i];
            mAlmanacMask = (int) info[5][i];
            mUsedInFixMask = (int) info[6][i];
            mSvCount++;
		}
		mAdapter.notifyDataSetChanged();
        /*Iterator<GpsSatellite> satellites = status.getSatellites().iterator();

        if (mPrns == null) {
            int length = status.getMaxSatellites();
            mPrns = new int[length];
            mSnrs = new float[length];
            mSvElevations = new float[length];
            mSvAzimuths = new float[length];
        }

        mSvCount = 0;
        mEphemerisMask = 0;
        mAlmanacMask = 0;
        mUsedInFixMask = 0;
        while (satellites.hasNext()) {
            GpsSatellite satellite = satellites.next();
            int prn = satellite.getPrn();
            int prnBit = (1 << (prn - 1));
            mPrns[mSvCount] = prn;
            mSnrs[mSvCount] = satellite.getSnr();
            mSvElevations[mSvCount] = satellite.getElevation();
            mSvAzimuths[mSvCount] = satellite.getAzimuth();
            if (satellite.hasEphemeris()) {
                mEphemerisMask |= prnBit;
            }
            if (satellite.hasAlmanac()) {
                mAlmanacMask |= prnBit;
            }
            if (satellite.usedInFix()) {
                mUsedInFixMask |= prnBit;
            }
            mSvCount++;
        }

        mAdapter.notifyDataSetChanged();*/
    }
	

	@Override
	public void OnComplete() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void OnFailure() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void OnStatusMessage(String message) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void OnFatalMessage(String message) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void OnSatelliteCount(int count) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void OnStopLogging() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Activity GetActivity() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onFileName(String newFileName) {
		// TODO Auto-generated method stub
		
	}
}
