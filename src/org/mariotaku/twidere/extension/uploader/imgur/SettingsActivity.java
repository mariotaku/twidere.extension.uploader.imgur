package org.mariotaku.twidere.extension.uploader.imgur;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class SettingsActivity extends PreferenceActivity implements Constants {

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getPreferenceManager().setSharedPreferencesName(SHARED_PREFERRENCES_NAME);
		addPreferencesFromResource(R.xml.settings);
	}

}
