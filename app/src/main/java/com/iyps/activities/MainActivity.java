package com.iyps.activities;

import static com.iyps.preferences.PreferenceManager.THEME_PREF;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.iyps.R;
import com.iyps.fragments.main.MainFragment;
import com.iyps.fragments.main.ScoreDetailsFragment;
import com.iyps.fragments.settings.AboutFragment;
import com.iyps.preferences.PreferenceManager;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private PreferenceManager preferenceManager;
    private Fragment fragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        preferenceManager=new PreferenceManager(this);
        MaterialToolbar toolbar = findViewById(R.id.toolbar_main);

    /*===========================================================================================*/

        // DISABLE SCREENSHOTS & SCREEN RECORDINGS
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);

        // TOOLBAR AS ACTIONBAR
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setHomeAsUpIndicator(R.drawable.ic_back);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // DEFAULT FRAGMENT
        if (savedInstanceState==null) {
            DisplayFragment("Main");
        }

    }

    // SETUP FRAGMENTS
    public void DisplayFragment (String fragmentName)
    {

        FragmentTransaction transaction=getSupportFragmentManager().beginTransaction();

        switch (fragmentName)
        {

            case "Main":
                Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.app_name_full);
                fragment= new MainFragment();
                break;

            case "About":
                Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.about_title);
                fragment= new AboutFragment();
                transaction.setCustomAnimations(R.anim.slide_from_end, R.anim.slide_to_start,
                                                R.anim.slide_from_start, R.anim.slide_to_end);
                break;

            case "Score Details":
                Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.score_details);
                fragment= new ScoreDetailsFragment();
                transaction.setCustomAnimations(R.anim.slide_from_end, R.anim.slide_to_start,
                                                R.anim.slide_from_start, R.anim.slide_to_end);
                break;

        }

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(!fragmentName.equals("Main"));

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

            try
            {
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://github.com/the-weird-aquarian/IYPS/issues")));
            }
            // IF BROWSERS NOT INSTALLED, SHOW TOAST
            catch (ActivityNotFoundException e)
            {
                Toast.makeText(getApplicationContext(), getString(R.string.no_browsers), Toast.LENGTH_SHORT).show();
            }

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
    @SuppressLint("NonConstantResourceId")
    private void ThemeBottomSheet(){
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this, R.style.CustomBottomSheetTheme);
        bottomSheetDialog.setCancelable(true);

        @SuppressLint("InflateParams") final View view  = getLayoutInflater().inflate(R.layout.bottom_sheet_theme, null);
        bottomSheetDialog.setContentView(view);

        final RadioGroup themeRadioGroup = view.findViewById(R.id.options_radiogroup);

        // DEFAULT CHECKED RADIO
        if (preferenceManager.getInt(THEME_PREF)==0){
            if (Build.VERSION.SDK_INT>=29){
                preferenceManager.setInt(THEME_PREF, R.id.option_default);
            }
            else{
                preferenceManager.setInt(THEME_PREF, R.id.option_light);
            }
        }
        themeRadioGroup.check(preferenceManager.getInt(THEME_PREF));

        // TITLE
        ((TextView)view.findViewById(R.id.bottom_sheet_title)).setText(R.string.choose_theme_title);

        // CANCEL BUTTON
        view.findViewById(R.id.cancel_button).setOnClickListener(view12 ->
                bottomSheetDialog.cancel());

        // SHOW SYSTEM DEFAULT OPTION ONLY ON SDK 29 AND ABOVE
        if (Build.VERSION.SDK_INT>=29){
            view.findViewById(R.id.option_default).setVisibility(View.VISIBLE);
        }
        else{
            view.findViewById(R.id.option_default).setVisibility(View.GONE);
        }

        // ON SELECTING OPTION
        ((RadioGroup)view.findViewById(R.id.options_radiogroup))
                .setOnCheckedChangeListener((radioGroup, checkedId) -> {
                    switch (checkedId){
                        case R.id.option_default:
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                            break;

                        case R.id.option_light:
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                            break;

                        case R.id.option_dark:
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                            break;
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
        if (getSupportFragmentManager().getBackStackEntryCount()>1){
            getSupportFragmentManager().popBackStack();
            Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.app_name_full);
            Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(false);
        }

        // IF ON DEFAULT FRAGMENT, FINISH ACTIVITY
        else {
            finish();
        }
    }

}