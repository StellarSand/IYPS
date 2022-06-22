/*
 * Copyright (c) 2022 the-weird-aquarian
 *
 *  This file is part of IYPS.
 *
 *  IYPS is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  IYPS is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with IYPS.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.iyps.activities;

import static com.iyps.fragments.main.AboutFragment.OpenURL;
import static com.iyps.preferences.PreferenceManager.THEME_PREF;

import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.iyps.R;
import com.iyps.databinding.ActivityMainBinding;
import com.iyps.databinding.BottomSheetHeaderBinding;
import com.iyps.databinding.BottomSheetThemeBinding;
import com.iyps.fragments.main.MainFragment;
import com.iyps.fragments.main.AboutFragment;
import com.iyps.fragments.main.ScoreHelpFragment;
import com.iyps.preferences.PreferenceManager;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding activityBinding;
    private PreferenceManager preferenceManager;
    private Fragment fragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(activityBinding.getRoot());

        preferenceManager = new PreferenceManager(this);

        /*########################################################################################*/

        // Disable screenshots and screen recordings
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);

        setSupportActionBar(activityBinding.toolbarMain);
        Objects.requireNonNull(getSupportActionBar()).setHomeAsUpIndicator(R.drawable.ic_back);
        activityBinding.toolbarMain.setNavigationOnClickListener(v -> onBackPressed());

        // Default fragment
        if (savedInstanceState == null) {
            DisplayFragment("Main");
        }

    }

    // Setup fragments
    public void DisplayFragment (String fragmentName)
    {

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        switch (fragmentName)
        {

            case "Main":
                Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.app_name);
                fragment = new MainFragment();
                break;

            case "Score Help":
                Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.help);
                fragment = new ScoreHelpFragment();
                transaction.setCustomAnimations(R.anim.slide_from_end, R.anim.slide_to_start,
                                                R.anim.slide_from_start, R.anim.slide_to_end);
                break;

            case "About":
                Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.about_title);
                fragment = new AboutFragment();
                transaction.setCustomAnimations(R.anim.slide_from_end, R.anim.slide_to_start,
                                                R.anim.slide_from_start, R.anim.slide_to_end);
                break;

        }

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(!fragmentName.equals("Main"));

        if (fragmentName.equals("Main")) {
            activityBinding.divider.setVisibility(View.GONE);
        }
        else {
            activityBinding.divider.setVisibility(View.VISIBLE);
        }

        transaction
                .replace(R.id.activity_host_fragment, fragment)
                .addToBackStack(null)
                .commit();

    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_activity_main, menu);

        // Hide overflow in about fragment
        menu.findItem(R.id.more).setVisible(getSupportFragmentManager().getBackStackEntryCount() <= 1);

        return true;

    }

    @Override
    public boolean onOptionsItemSelected (MenuItem item) {

        // Theme
        if (item.getItemId() == R.id.theme) {
            ThemeBottomSheet();
        }

        // Report an issue
        else if (item.getItemId() == R.id.report_issue) {
            OpenURL(this, "https://github.com/the-weird-aquarian/IYPS/issues");
        }

        // About
        else if (item.getItemId() == R.id.about) {
            DisplayFragment("About");
        }

        return true;
    }

    private void ThemeBottomSheet(){
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetDialog.setCancelable(true);

        BottomSheetThemeBinding bottomSheetBinding;
        BottomSheetHeaderBinding headerBinding;
        bottomSheetBinding = BottomSheetThemeBinding.inflate(getLayoutInflater());
        headerBinding = BottomSheetHeaderBinding.bind(bottomSheetBinding.getRoot());
        bottomSheetDialog.setContentView(bottomSheetBinding.getRoot());

        // Default checked radio
        if (preferenceManager.getInt(THEME_PREF) == 0){
            if (Build.VERSION.SDK_INT >= 29){
                preferenceManager.setInt(THEME_PREF, R.id.option_default);
            }
            else{
                preferenceManager.setInt(THEME_PREF, R.id.option_light);
            }
        }
        bottomSheetBinding.optionsRadiogroup.check(preferenceManager.getInt(THEME_PREF));

        // Title
        headerBinding.bottomSheetTitle.setText(R.string.choose_theme_title);

        // Show system default option only on SDK 29 and above
        if (Build.VERSION.SDK_INT >= 29){
            bottomSheetBinding.optionDefault.setVisibility(View.VISIBLE);
        }
        else{
            bottomSheetBinding.optionDefault.setVisibility(View.GONE);
        }

        // On selecting option
        bottomSheetBinding.optionsRadiogroup
                .setOnCheckedChangeListener((radioGroup, checkedId) -> {

                    if (checkedId == R.id.option_default) {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                    }
                    else if (checkedId == R.id.option_light) {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    }
                    else if (checkedId == R.id.option_dark) {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    }

                    preferenceManager.setInt(THEME_PREF, checkedId);
                    bottomSheetDialog.dismiss();
                    this.recreate();
                });

        // Cancel
        bottomSheetBinding.cancelButton.setOnClickListener(view12 ->
                bottomSheetDialog.cancel());

        bottomSheetDialog.show();
    }


    @Override
    public void onBackPressed() {

        if (getSupportFragmentManager().getBackStackEntryCount() > 1){
            getSupportFragmentManager().popBackStack();
            Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.app_name);
            Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(false);
            activityBinding.divider.setVisibility(View.GONE);
        }
        else {
            finish();
        }
    }

}