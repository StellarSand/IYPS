package com.iyps.activities;

import static com.iyps.fragments.main.AboutFragment.OpenURL;
import static com.iyps.preferences.PreferenceManager.THEME_PREF;

import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
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
import com.iyps.fragments.main.TimeHelpFragment;
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

    /*===========================================================================================*/

        // DISABLE SCREENSHOTS & SCREEN RECORDINGS
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);

        // TOOLBAR AS ACTIONBAR
        setSupportActionBar(activityBinding.toolbarMain);
        Objects.requireNonNull(getSupportActionBar()).setHomeAsUpIndicator(R.drawable.ic_back);
        activityBinding.toolbarMain.setNavigationOnClickListener(v -> onBackPressed());

        // DEFAULT FRAGMENT
        if (savedInstanceState == null) {
            DisplayFragment("Main");
        }

    }

    // SETUP FRAGMENTS
    public void DisplayFragment (String fragmentName)
    {

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        switch (fragmentName)
        {

            case "Main":
                Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.app_name_full);
                fragment = new MainFragment();
                break;

            case "Time Help":
                Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.help);
                fragment = new TimeHelpFragment();
                transaction.setCustomAnimations(R.anim.slide_from_end, R.anim.slide_to_start,
                                                R.anim.slide_from_start, R.anim.slide_to_end);
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

        // HIDE OVERFLOW MENU IN ABOUT FRAGMENT
        menu.findItem(R.id.action_settings).setVisible(getSupportFragmentManager().getBackStackEntryCount() <= 1);

        // THEME
        menu.findItem(R.id.theme).setOnMenuItemClickListener(item1 -> {
            ThemeBottomSheet();
            return true;
        });

        // REPORT AN ISSUE
        menu.findItem(R.id.report_issue).setOnMenuItemClickListener(item2 -> {

            OpenURL(this, "https://github.com/the-weird-aquarian/IYPS/issues");

            return true;
        });

        // ABOUT
        menu.findItem(R.id.about).setOnMenuItemClickListener(item3 -> {

            DisplayFragment("About");

            return true;
        });

        return true;

    }

    // THEME BOTTOM SHEET
    private void ThemeBottomSheet(){
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this, R.style.CustomBottomSheetTheme);
        bottomSheetDialog.setCancelable(true);

        BottomSheetThemeBinding bottomSheetBinding;
        BottomSheetHeaderBinding headerBinding;
        bottomSheetBinding = BottomSheetThemeBinding.inflate(getLayoutInflater());
        headerBinding = BottomSheetHeaderBinding.bind(bottomSheetBinding.getRoot());
        bottomSheetDialog.setContentView(bottomSheetBinding.getRoot());

        // DEFAULT CHECKED RADIO
        if (preferenceManager.getInt(THEME_PREF) == 0){
            if (Build.VERSION.SDK_INT >= 29){
                preferenceManager.setInt(THEME_PREF, R.id.option_default);
            }
            else{
                preferenceManager.setInt(THEME_PREF, R.id.option_light);
            }
        }
        bottomSheetBinding.optionsRadiogroup.check(preferenceManager.getInt(THEME_PREF));

        // TITLE
        headerBinding.bottomSheetTitle.setText(R.string.choose_theme_title);

        // CANCEL BUTTON
        bottomSheetBinding.cancelButton.setOnClickListener(view12 ->
                bottomSheetDialog.cancel());

        // SHOW SYSTEM DEFAULT OPTION ONLY ON SDK 29 AND ABOVE
        if (Build.VERSION.SDK_INT >= 29){
            bottomSheetBinding.optionDefault.setVisibility(View.VISIBLE);
        }
        else{
            bottomSheetBinding.optionDefault.setVisibility(View.GONE);
        }

        // ON SELECTING OPTION
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

        // SHOW BOTTOM SHEET WITH CUSTOM ANIMATION
        Objects.requireNonNull(bottomSheetDialog.getWindow()).getAttributes().windowAnimations = R.style.BottomSheetAnimation;
        bottomSheetDialog.show();
    }


    // ON BACK PRESSED
    @Override
    public void onBackPressed() {

        // IF NOT ON DEFAULT FRAGMENT, GO TO DEFAULT FRAGMENT
        if (getSupportFragmentManager().getBackStackEntryCount() > 1){
            getSupportFragmentManager().popBackStack();
            Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.app_name_full);
            Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(false);
            activityBinding.divider.setVisibility(View.GONE);
        }

        // IF ON DEFAULT FRAGMENT, FINISH ACTIVITY
        else {
            finish();
        }
    }

}