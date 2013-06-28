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

package ru.ilukas.gglogger.senders.opengts;

import android.content.Intent;
import android.os.Bundle;
import android.preference.*;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.view.KeyEvent;
import android.webkit.URLUtil;
import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.MenuItem;
import ru.ilukas.gglogger.GpsMainActivity;
import ru.ilukas.gglogger.R;
import ru.ilukas.gglogger.common.Utilities;

public class OpenGTSActivity extends SherlockPreferenceActivity implements
        OnPreferenceChangeListener,
        OnPreferenceClickListener
{

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // enable the home button so you can go back to the main screen
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        addPreferencesFromResource(R.xml.opengtssettings);

        CheckBoxPreference chkEnabled = (CheckBoxPreference) findPreference("autoopengts_enabled");
        EditTextPreference txtOpenGTSServer = (EditTextPreference) findPreference("opengts_server");
        EditTextPreference txtOpenGTSServerPort = (EditTextPreference) findPreference("opengts_server_port");
        ListPreference txtOpenGTSCommunicationMethod = (ListPreference) findPreference("opengts_server_communication_method");
        EditTextPreference txtOpenGTSServerPath = (EditTextPreference) findPreference("autoopengts_server_path");
        EditTextPreference txtOpenGTSDeviceId = (EditTextPreference) findPreference("opengts_device_id");

        chkEnabled.setOnPreferenceChangeListener(this);
        txtOpenGTSServer.setOnPreferenceChangeListener(this);
        txtOpenGTSServerPort.setOnPreferenceChangeListener(this);
        txtOpenGTSCommunicationMethod.setOnPreferenceChangeListener(this);
        txtOpenGTSServerPath.setOnPreferenceChangeListener(this);
        txtOpenGTSDeviceId.setOnPreferenceChangeListener(this);

    }


    /**
     * Called when one of the menu items is selected.
     */
    public boolean onOptionsItemSelected(MenuItem item)
    {

        int itemId = item.getItemId();
        Utilities.LogInfo("Option item selected - " + String.valueOf(item.getTitle()));

        switch (itemId)
        {
            case android.R.id.home:
                Intent intent = new Intent(this, GpsMainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);

                break;
        }
        return super.onOptionsItemSelected(item);
    }




    public boolean onPreferenceClick(Preference preference)
    {
        if (!IsFormValid())
        {
            Utilities.MsgBox(getString(R.string.autoopengts_invalid_form),
                    getString(R.string.autoopengts_invalid_form_message),
                    OpenGTSActivity.this);
            return false;
        }
        return true;
    }

    private boolean IsFormValid()
    {
        CheckBoxPreference chkEnabled = (CheckBoxPreference) findPreference("opengts_enabled");
        EditTextPreference txtOpenGTSServer = (EditTextPreference) findPreference("opengts_server");
        EditTextPreference txtOpenGTSServerPort = (EditTextPreference) findPreference("opengts_server_port");
        ListPreference txtOpenGTSCommunicationMethod = (ListPreference) findPreference("opengts_server_communication_method");
        EditTextPreference txtOpenGTSServerPath = (EditTextPreference) findPreference("autoopengts_server_path");
        EditTextPreference txtOpenGTSDeviceId = (EditTextPreference) findPreference("opengts_device_id");

        return !chkEnabled.isChecked()
                || txtOpenGTSServer.getText() != null && txtOpenGTSServer.getText().length() > 0
                && txtOpenGTSServerPort.getText() != null && isNumeric(txtOpenGTSServerPort.getText())
                && txtOpenGTSCommunicationMethod.getValue() != null && txtOpenGTSCommunicationMethod.getValue().length() > 0
                && txtOpenGTSDeviceId.getText() != null && txtOpenGTSDeviceId.getText().length() > 0
                && URLUtil.isValidUrl("http://" + txtOpenGTSServer.getText() + ":" + txtOpenGTSServerPort.getText() + txtOpenGTSServerPath.getText());

    }

    private static boolean isNumeric(String str)
    {
        for (char c : str.toCharArray())
        {
            if (!Character.isDigit(c))
            {
                return false;
            }
        }
        return true;
    }

    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if (keyCode == KeyEvent.KEYCODE_BACK)
        {
            if (!IsFormValid())
            {
                Utilities.MsgBox(getString(R.string.autoopengts_invalid_form),
                        getString(R.string.autoopengts_invalid_form_message),
                        this);
                return false;
            }
            else
            {
                return super.onKeyDown(keyCode, event);
            }
        }
        else
        {
            return super.onKeyDown(keyCode, event);
        }
    }


    public boolean onPreferenceChange(Preference preference, Object newValue)
    {
        return true;
    }

}
