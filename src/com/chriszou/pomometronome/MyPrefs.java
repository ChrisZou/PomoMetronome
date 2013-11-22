package com.chriszou.pomometronome;

import com.googlecode.androidannotations.annotations.sharedpreferences.DefaultInt;
import com.googlecode.androidannotations.annotations.sharedpreferences.SharedPref;

@SharedPref
public interface MyPrefs {

	// The field age will have default value 42
	@DefaultInt(80)
	int rate();

}
