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
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;

import ru.ilukas.gglogger.common.IActionListener;
import ru.ilukas.gglogger.common.Session;

import com.actionbarsherlock.app.SherlockActivity;

public class GpsSkyActivity extends SherlockActivity implements
IGpsLoggerServiceClient, View.OnClickListener, IActionListener {
	private final static String TAG = "GpsSkyActivity";
    private GpsSkyView mSkyView;
    private SensorManager mSensorManager;
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
			GpsLoggingService.SetServiceClient(GpsSkyActivity.this);

			if (Session.isStarted()) {
				mSkyView.setSats(Session.getSatelliteslInfo());
			}

			// Form setup - toggle button, display existing location info
		}
	};
    
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        mSensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        mSkyView = new GpsSkyView(this);
        setContentView(mSkyView);

        //GpsTestActivity.getInstance().addSubActivity(this);
    }

    @SuppressWarnings("deprecation")
	@Override
    protected void onResume()
    {
        super.onResume();
        Sensor sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        if (sensor != null) {
        	mSensorManager.registerListener(mSkyView, sensor, 
        			SensorManager.SENSOR_DELAY_GAME);
        }
    }

    @Override
    protected void onPause()
    {
        mSensorManager.unregisterListener(mSkyView);
        super.onStop();
    }
    
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
		Log.d(TAG, "GpsSkyActivity BindService - binding now");
		serviceIntent = new Intent(this, GpsLoggingService.class);
		// Now bind to service
		bindService(serviceIntent, gpsServiceConnection,
				Context.BIND_AUTO_CREATE);
	}
	
	private void UnbindService() {
		Log.d(TAG, "GpsSkyActivity unBindService - binding now");
		unbindService(gpsServiceConnection);
	}

    /*public void onGpsStatusChanged(int event, GpsStatus status) {
        switch (event) {
            case GpsStatus.GPS_EVENT_STARTED:
                mSkyView.setStarted();
                break;

            case GpsStatus.GPS_EVENT_STOPPED:
                mSkyView.setStopped();
                break;

            case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
                mSkyView.setSats(status);
                break;
        }
    }*/

    private static class GpsSkyView extends View implements SensorEventListener {
        private Paint mHorizonActiveFillPaint, mHorizonInactiveFillPaint, mHorizonStrokePaint,
                      mGridStrokePaint,
                      mSatelliteFillPaint, mSatelliteStrokePaint;

        private float mOrientation = 0.0f;
        private boolean mStarted;
        private float mSnrs[], mElevs[], mAzims[];
        private int mSvCount;

        private final float mSnrThresholds[];
        private final int mSnrColors[];

        private static final int SAT_RADIUS = 5;

        public GpsSkyView(Context context) {
            super(context);

            mHorizonActiveFillPaint = new Paint();
            mHorizonActiveFillPaint.setColor(Color.WHITE);
            mHorizonActiveFillPaint.setStyle(Paint.Style.FILL);

            mHorizonInactiveFillPaint = new Paint();
            mHorizonInactiveFillPaint.setColor(Color.LTGRAY);
            mHorizonInactiveFillPaint.setStyle(Paint.Style.FILL);

            mHorizonStrokePaint = new Paint();
            mHorizonStrokePaint.setColor(Color.BLACK);
            mHorizonStrokePaint.setStyle(Paint.Style.STROKE);
            mHorizonStrokePaint.setStrokeWidth(2.0f);

            mGridStrokePaint = new Paint();
            mGridStrokePaint.setColor(Color.GRAY);
            mGridStrokePaint.setStyle(Paint.Style.STROKE);

            mSatelliteFillPaint = new Paint();
            mSatelliteFillPaint.setColor(Color.YELLOW);
            mSatelliteFillPaint.setStyle(Paint.Style.FILL);

            mSatelliteStrokePaint = new Paint();
            mSatelliteStrokePaint.setColor(Color.BLACK);
            mSatelliteStrokePaint.setStyle(Paint.Style.STROKE);
            mSatelliteStrokePaint.setStrokeWidth(2.0f);

            mSnrThresholds = new float[] { 0.0f,       10.0f,     20.0f,        30.0f       };
            mSnrColors     = new int[]   { Color.GRAY, Color.RED, Color.YELLOW, Color.GREEN };

            setFocusable(true);
        }

        /*public void setStarted() {
            mStarted = true;
            invalidate();
        }

        public void setStopped() {
            mStarted = false;
            mSvCount = 0;
            invalidate();
        }*/

        public void setSats(double info[][]/*GpsStatus status*/) {
        	Log.d(TAG, "setSats Start");
        	if (info[0].length<=0)
    		{
    			return;
    		}
    		mSvCount = 0;
            if (mSnrs == null) {
                mSnrs = new float[info[0].length];
                mElevs = new float[info[0].length];
                mAzims = new float[info[0].length];
            }

    		for (int i = 0; i < info[0].length; i++) {
    			//Log.d(TAG, "setSats FOR");
    			//int prn = (int) info[0][i];
                //int prnBit = (1 << (prn - 1));	
                mSnrs[mSvCount] = (float) info[1][i];
                mElevs[mSvCount] = (float) info[2][i];
                mAzims[mSvCount] = (float) info[3][i];
                mSvCount++;
    		}
        	
           /* Iterator<GpsSatellite> satellites = status.getSatellites().iterator();

            if (mSnrs == null) {
                int length = status.getMaxSatellites();
                mSnrs = new float[length];
                mElevs = new float[length];
                mAzims = new float[length];
            }

            mSvCount = 0;
            while (satellites.hasNext()) {
                GpsSatellite satellite = satellites.next();
                mSnrs[mSvCount] = satellite.getSnr();
                mElevs[mSvCount] = satellite.getElevation();
                mAzims[mSvCount] = satellite.getAzimuth();
                mSvCount++;
            }*/

            mStarted = true;
            invalidate();
        }

        private void drawLine(Canvas c, float x1, float y1, float x2, float y2) {
            // rotate the line based on orientation
            double angle = Math.toRadians(-mOrientation);
            float cos = (float)Math.cos(angle);
            float sin = (float)Math.sin(angle);

            float centerX = (x1 + x2) / 2.0f;
            float centerY = (y1 + y2) / 2.0f;
            x1 -= centerX;
            y1 = centerY - y1;
            x2 -= centerX;
            y2 = centerY - y2;

            float X1 = cos * x1 + sin * y1 + centerX;
            float Y1 = -(-sin * x1 + cos * y1) + centerY;
            float X2 = cos * x2 + sin * y2 + centerX;
            float Y2 = -(-sin * x2 + cos * y2) + centerY;

            c.drawLine(X1, Y1, X2, Y2, mGridStrokePaint);
        }

        private void drawHorizon(Canvas c, int s) {
            float radius = s / 2;

            c.drawCircle(radius, radius, radius, mStarted ? mHorizonActiveFillPaint : mHorizonInactiveFillPaint);
            drawLine(c, 0, radius, 2 * radius, radius);
            drawLine(c, radius, 0, radius, 2 * radius);
            c.drawCircle(radius, radius, elevationToRadius(s, 60.0f), mGridStrokePaint);
            c.drawCircle(radius, radius, elevationToRadius(s, 30.0f), mGridStrokePaint);
            c.drawCircle(radius, radius, elevationToRadius(s,  0.0f), mGridStrokePaint);
            c.drawCircle(radius, radius, radius, mHorizonStrokePaint);
        }

        private void drawSatellite(Canvas c, int s, float elev, float azim, float snr) {
            double radius, angle;
            float x, y;
            Paint thisPaint;

            thisPaint = getSatellitePaint(mSatelliteFillPaint, snr);

            radius = elevationToRadius(s, elev);
            azim -= mOrientation;
            angle = (float)Math.toRadians(azim);

            x = (float)((s / 2) + (radius * Math.sin(angle)));
            y = (float)((s / 2) - (radius * Math.cos(angle)));

            c.drawCircle(x, y, SAT_RADIUS, thisPaint);
            c.drawCircle(x, y, SAT_RADIUS, mSatelliteStrokePaint);
        }

        private float elevationToRadius(int s, float elev) {
            return ((s / 2) - SAT_RADIUS) * (1.0f - (elev / 90.0f));
        }

        private Paint getSatellitePaint(Paint base, float snr) {
            int numSteps;
            Paint newPaint;

            newPaint = new Paint(base);

            numSteps = mSnrThresholds.length;

            if (snr <= mSnrThresholds[0]) {
                newPaint.setColor(mSnrColors[0]);
                return newPaint;
            }

            if (snr >= mSnrThresholds[numSteps - 1]) {
                newPaint.setColor(mSnrColors[numSteps - 1]);
                return newPaint;
            }

            for (int i = 0; i < numSteps - 1; i++) {
                float threshold = mSnrThresholds[i];
                float nextThreshold = mSnrThresholds[i + 1];
                if (snr >= threshold && snr <= nextThreshold) {
                    int c1, r1, g1, b1, c2, r2, g2, b2, c3, r3, g3, b3;
                    float f;

                    c1 = mSnrColors[i];
                    r1 = Color.red(c1);
                    g1 = Color.green(c1);
                    b1 = Color.blue(c1);

                    c2 = mSnrColors[i + 1];
                    r2 = Color.red(c2);
                    g2 = Color.green(c2);
                    b2 = Color.blue(c2);

                    f = (snr - threshold) / (nextThreshold - threshold);

                    r3 = (int)(r2 * f + r1 * (1.0f - f));
                    g3 = (int)(g2 * f + g1 * (1.0f - f));
                    b3 = (int)(b2 * f + b1 * (1.0f - f));
                    c3 = Color.rgb(r3, g3, b3);

                    newPaint.setColor(c3);

                    return newPaint;
                }
            }

            newPaint.setColor(Color.MAGENTA);

            return newPaint;
        }

        public void onSensorChanged(SensorEvent event) {
            mOrientation = event.values[0];
            invalidate();
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

        @Override
        protected void onDraw(Canvas canvas) {
            int w, h, s;
            //Log.d(TAG, "onDraw Start");
            w = canvas.getWidth();
            h = canvas.getHeight();
            s = (w < h) ? w : h;

            drawHorizon(canvas, s);

            if (mElevs != null) {
            	//Log.d(TAG, "onDraw IF");
                int numSats = mSvCount;

                for (int i = 0; i < numSats; i++) {
                    if (mSnrs[i] > 0.0f && (mElevs[i] != 0.0f || mAzims[i] != 0.0f))
                        drawSatellite(canvas, s, mElevs[i], mAzims[i], mSnrs[i]);
                }
            }
            else{
            	//Log.d(TAG, "onDraw ELSE");
            }
        }
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
		mSkyView.setSats(Session.getSatelliteslInfo());
		
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
