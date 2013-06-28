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

package ru.ilukas.gglogger.shortcuts;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import ru.ilukas.gglogger.GpsLoggingService;
import ru.ilukas.gglogger.common.Utilities;

public class ShortcutStart extends Activity
{
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        Utilities.LogInfo("Shortcut - start logging");
        Intent serviceIntent = new Intent(getApplicationContext(), GpsLoggingService.class);
        serviceIntent.putExtra("immediate", true);
        getApplicationContext().startService(serviceIntent);

        finish();

    }
}