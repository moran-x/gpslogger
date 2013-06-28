/*
*    This file is part of GPSLogger for Android.
*
*    GPSLogger for Android is free software: you can redistribute it and/or modify
*    it under the terms of the GNU General Public License as published by
*    the Free Software Foundation, either version 2 of the License, or
*    (at your option) any later version.
*
*    GPSLogger for Android is distributed in the hope that it will be useful,
*    but WITHOUT ANY WARRANTY; without even the implied warranty of
*    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*    GNU General Public License for more details.
*
*    You should have received a copy of the GNU General Public License
*    along with GPSLogger for Android.  If not, see <http://www.gnu.org/licenses/>.
*/

package ru.ilukas.gglogger;

import android.location.*;
import android.os.Bundle;
import ru.ilukas.gglogger.common.Session;
import ru.ilukas.gglogger.common.Utilities;

import java.util.Iterator;

class GeneralLocationListener implements LocationListener, GpsStatus.Listener
{

    private static GpsLoggingService mainActivity;
    private double satArrays[][];
	private int mSvCount;
    GeneralLocationListener(GpsLoggingService activity)
    {
        Utilities.LogDebug("GeneralLocationListener constructor");
        mainActivity = activity;
    }

    /**
     * Event raised when a new fix is received.
     */
    public void onLocationChanged(Location loc)
    {


        try
        {
            if (loc != null)
            {
                Utilities.LogVerbose("GeneralLocationListener.onLocationChanged");
                mainActivity.OnLocationChanged(loc);
            }

        }
        catch (Exception ex)
        {
            Utilities.LogError("GeneralLocationListener.onLocationChanged", ex);
            mainActivity.SetStatus(ex.getMessage());
        }

    }

    public void onProviderDisabled(String provider)
    {
        Utilities.LogInfo("Provider disabled");
        Utilities.LogDebug(provider);
        mainActivity.RestartGpsManagers();
    }

    public void onProviderEnabled(String provider)
    {

        Utilities.LogInfo("Provider enabled");
        Utilities.LogDebug(provider);
        mainActivity.RestartGpsManagers();
    }

    public void onStatusChanged(String provider, int status, Bundle extras)
    {
        if (status == LocationProvider.OUT_OF_SERVICE)
        {
            Utilities.LogDebug(provider + " is out of service");
            mainActivity.StopManagerAndResetAlarm();
        }

        if (status == LocationProvider.AVAILABLE)
        {
            Utilities.LogDebug(provider + " is available");
        }

        if (status == LocationProvider.TEMPORARILY_UNAVAILABLE)
        {
            Utilities.LogDebug(provider + " is temporarily unavailable");
            mainActivity.StopManagerAndResetAlarm();
        }
    }

    public void onGpsStatusChanged(int event)
    {

        switch (event)
        {
            case GpsStatus.GPS_EVENT_FIRST_FIX:
                Utilities.LogDebug("GPS Event First Fix");
                mainActivity.SetStatus(mainActivity.getString(R.string.fix_obtained));
                break;

            case GpsStatus.GPS_EVENT_SATELLITE_STATUS:

                Utilities.LogDebug("GPS Satellite status obtained");
                GpsStatus status = mainActivity.gpsLocationManager.getGpsStatus(null);

                int maxSatellites = status.getMaxSatellites();

                Iterator<GpsSatellite> satellites = status.getSatellites().iterator();
                int count = 0;
                
                //double[][] satArrays;
                if (satArrays == null) {
                    //int length = status.getMaxSatellites();
                    //Utilities.LogDebug("GeneralLocation "+Integer.toString(maxSatellites));
                    //Log.d("!!!", Integer.toString(length));
                    satArrays = new double[7][maxSatellites];
					/*satArrays[0]//mPrns
                    satArrays[1]//mSnrs
                    satArrays[2]//mSvElevations
                    satArrays[3];//mSvAzimuths
                    satArrays[4]//mEphemerisMask
                    satArrays[5]//mAlmanacMask
                    satArrays[6]//mUsedInFixMask*/
                }

                mSvCount = 0;
                
                while (satellites.hasNext() && count <= maxSatellites)
                {
                    //it.next();
                    GpsSatellite satellite = satellites.next();
                    int prn = satellite.getPrn();
                    satArrays[0][mSvCount] = prn;
                    satArrays[1][mSvCount] = satellite.getSnr();
                    satArrays[2][mSvCount] = satellite.getElevation();
                    satArrays[3][mSvCount] = satellite.getAzimuth();
                    satArrays[4][mSvCount]=0;
                    satArrays[5][mSvCount]=0;
                    satArrays[6][mSvCount]=0;
                    if (satellite.hasEphemeris()) {
                    	//double d = Double.parseDouble(Integer.toString(prnBit)); 
                    	satArrays[4][mSvCount] = 1;//  mEphemerisMask |= prnBit;
                    }
                    if (satellite.hasAlmanac()) {
                    	//double d = Double.parseDouble(Integer.toString(prnBit)); 
                    	satArrays[5][mSvCount] = 1;// mAlmanacMask |= prnBit;
                    }
                    if (satellite.usedInFix()) {
                    	//double d = Double.parseDouble(Integer.toString(prnBit)); 
                    	satArrays[6][mSvCount] = 1;//  mUsedInFixMask |= prnBit;
                    }
                    mSvCount++;
                    count++;
                }
                Session.setSatellitesInfo(satArrays);
                satArrays=null;
                mainActivity.SetSatelliteInfo(count);
                break;

            case GpsStatus.GPS_EVENT_STARTED:
                Utilities.LogInfo("GPS started, waiting for fix");
                mainActivity.SetStatus(mainActivity.getString(R.string.started_waiting));
                break;

            case GpsStatus.GPS_EVENT_STOPPED:
                Utilities.LogInfo("GPS Stopped");
                mainActivity.SetStatus(mainActivity.getString(R.string.gps_stopped));
                break;

        }
    }
}
