package ru.ilukas.gglogger;

import ru.ilukas.gglogger.common.AppSettings;
import ru.ilukas.gglogger.common.IActionListener;
import ru.ilukas.gglogger.common.Session;
import ru.ilukas.gglogger.common.Utilities;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.actionbarsherlock.view.Menu;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.LatLng;


public class GpsMapActivity extends FragmentActivity  implements
IGpsLoggerServiceClient, View.OnClickListener, IActionListener {

	private final static String TAG = "GpsMapActivity";
	private static Intent serviceIntent;
	private GoogleMap mMap;
	private UiSettings mUiSettings;
	private static LatLng currentLocation;
	private PolylineOptions rectOptions = new PolylineOptions();
	private boolean firstStart;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gps_map);
		firstStart = true;
		setUpMapIfNeeded();
		
	}
	private final ServiceConnection gpsServiceConnection = new ServiceConnection() {

		public void onServiceDisconnected(ComponentName name) {
		}

		public void onServiceConnected(ComponentName name, IBinder service) {
			((GpsLoggingService.GpsLoggingBinder) service)
					.getService();
			GpsLoggingService.SetServiceClient(GpsMapActivity.this);

			if (Session.isStarted()) {
				//mSkyView.setSats(Session.getSatelliteslInfo());
			}

			// Form setup - toggle button, display existing location info
		}
	};
	public void onStart() {
		super.onStart();
		// satelliteActive = true;
		Log.d(TAG, "onStart");
		BindService();
		
	}

	@Override
	public void onStop() {
		super.onStop();
		Log.d(TAG, "onStart");
		UnbindService();
	}
	
	private void BindService() {
		Log.d(TAG, "GpsMapActivity BindService - binding now");
		serviceIntent = new Intent(this, GpsLoggingService.class);
		// Now bind to service
		bindService(serviceIntent, gpsServiceConnection,
				Context.BIND_AUTO_CREATE);
	}
	
	private void UnbindService() {
		Log.d(TAG, "GpsMapActivity unBindService - binding now");
		unbindService(gpsServiceConnection);
	}
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }
    
    private void setUpMap() {
        mMap.setMyLocationEnabled(true);
        mUiSettings = mMap.getUiSettings();
        mUiSettings.setZoomControlsEnabled(true);
        mUiSettings.setCompassEnabled(true);
        //mUiSettings.setMyLocationButtonEnabled(true);
        mUiSettings.setScrollGesturesEnabled(true);
        mUiSettings.setZoomGesturesEnabled(true);
        mUiSettings.setTiltGesturesEnabled(true);
        mUiSettings.setRotateGesturesEnabled(true);
        //mUiSettings.setAllGesturesEnabled(true);
        
        
        
    }
	/**
	 * Called when the menu is created.
	 */

	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.mapmenu, (android.view.Menu) menu);
		//getActionBar().setDisplayShowTitleEnabled(false);
		return true;
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
	public void onClick(View arg0) {
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
	public void OnLocationUpdate(Location loc) {
		// TODO Auto-generated method stub
		setLocation(loc);
		
	}

	private void setLocation(Location loc) {
		// TODO Зависимость Увеличения от скорости
		currentLocation = new LatLng(loc.getLatitude(), loc.getLongitude());
		//mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 17));
		float bearingDegrees;
		
		if (loc.hasBearing()) {
			bearingDegrees = loc.getBearing();
		} else {
			bearingDegrees = 0;
		}
		
		
		
		CameraPosition cameraPosition = new CameraPosition.Builder()
	    .target(currentLocation)      // Sets the center of the map to Mountain View
	    .zoom(17)                   // Sets the zoom
	    .bearing(bearingDegrees)                // Sets the orientation of the camera to east
	    .tilt(30)                   // Sets the tilt of the camera to 30 degrees
	    .build();                   // Creates a CameraPosition from the builder
		mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
		
		drawPolyline(loc);
		typeText(loc);
		//mMap.animateCamera(CameraUpdateFactory.zoomIn());

		// Zoom out to zoom level 10, animating with a duration of 2 seconds.
		//mMap.animateCamera(CameraUpdateFactory.zoomTo(10), 2000, null);
		
	}

	private void typeText(Location loc) {
		TextView txtSpeed = (TextView) findViewById(R.id.mapSpeed);
		TextView txtDirection = (TextView) findViewById(R.id.mapBearing);
		if (loc.hasSpeed()) {

			float speed = loc.getSpeed();
			String unit;
			if (AppSettings.shouldUseImperial()) {
				if (speed > 1.47) {
					speed = speed * 0.6818f;
					unit = getString(R.string.miles_per_hour);

				} else {
					speed = Utilities.MetersToFeet(speed);
					unit = getString(R.string.feet_per_second);
				}
			} else {
				if (speed > 0.277) {
					speed = speed * 3.6f;
					unit = getString(R.string.kilometers_per_hour);
				} else {
					unit = getString(R.string.meters_per_second);
				}
			}

			txtSpeed.setText(String.valueOf(speed) + unit);

		} else {
			txtSpeed.setText(R.string.not_applicable);
		}
		if (loc.hasBearing()) {

			float bearingDegrees = loc.getBearing();
			String direction;

			direction = Utilities.GetBearingDescription(bearingDegrees,
					getApplicationContext());

			txtDirection.setText(direction + "("
					+ String.valueOf(Math.round(bearingDegrees))
					+ getString(R.string.degree_symbol) + ")");
		} else {
			txtDirection.setText(R.string.not_applicable);
		}
		
	}

	private void drawPolyline(Location loc) {
		if (firstStart)
		{
			rectOptions.addAll(Session.getLatLonList());
			firstStart=false;
		}
		
		currentLocation = new LatLng(loc.getLatitude(), loc.getLongitude());
		rectOptions.color(Color.BLUE);
		rectOptions.width(3);
		rectOptions.geodesic(true);
		
		rectOptions.add(currentLocation);
	
		mMap.addPolyline(rectOptions);
	}

	@Override
	public void OnSatelliteCount(int count) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void ClearForm() {
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
