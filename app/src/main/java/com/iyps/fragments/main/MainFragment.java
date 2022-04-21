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

package com.iyps.fragments.main;

import static android.content.Context.CLIPBOARD_SERVICE;

import static com.iyps.preferences.PreferenceManager.CRACK_TIMES_PREF;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.iyps.R;
import com.iyps.activities.MainActivity;
import com.iyps.databinding.BottomSheetCrackTimesBinding;
import com.iyps.databinding.BottomSheetHeaderBinding;
import com.iyps.databinding.FragmentMainBinding;
import com.iyps.preferences.PreferenceManager;
import com.nulabinc.zxcvbn.Strength;
import com.nulabinc.zxcvbn.Zxcvbn;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class MainFragment extends Fragment {

    private PreferenceManager preferenceManager;
    private FragmentMainBinding fragmentBinding;
    private static int worstMeterColor, weakMeterColor, mediumMeterColor,
                       strongMeterColor, excellentMeterColor, emptyMeterColor,
                       baseScore, lengthScore, upperCaseScore, numScore,
                       specialCharScore, penaltyScore, totalScore, lengthCount,
                       upperCaseCount, lowerCaseCount, numCount, specialCharCount, passwordLength;
    private StringBuilder suggestionText;
    private boolean isExpanded;
    private int wait = 0;
    private Zxcvbn zxcvbn;
    private CountDownTimer clearClipboardTimer = null;
    private ClipboardManager clipboardManager;
    boolean copiedFromHere;
    private static String worst, weak, medium, strong,
                          excellent, worstPassWarning, weakPassWarning,
                          mediumPassWarning, not_applicable, passwordString, zero;
    private static final int lengthScoreMultiplier=3,
                             upperCaseScoreMultiplier=4,
                             numScoreMultiplier=5,
                             specialCharScoreMultiplier=5;


    public MainFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        setHasOptionsMenu(true);
        fragmentBinding = FragmentMainBinding.inflate(inflater, container, false);
        return fragmentBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        preferenceManager = new PreferenceManager(requireContext());

        suggestionText = new StringBuilder();
        wait = 0;
        baseScore = 0;
        lengthScore = 0;
        upperCaseScore = 0;
        numScore = 0;
        specialCharScore = 0;
        penaltyScore = 0;
        totalScore = 0;
        lengthCount = 0;
        upperCaseCount = 0;
        numCount = 0;
        specialCharCount = 0;
        zxcvbn = new Zxcvbn();

        worst = getString(R.string.worst);
        weak = getString(R.string.weak);
        medium = getString(R.string.medium);
        strong = getString(R.string.strong);
        excellent = getString(R.string.excellent);
        worstPassWarning = getString(R.string.worst_pass_warning);
        weakPassWarning = getString(R.string.weak_pass_warning);
        mediumPassWarning = getString(R.string.medium_pass_warning);
        not_applicable = getString(R.string.not_applicable);
        zero = getString(R.string.zero);

        emptyMeterColor = getColor(R.color.hintColor);
        worstMeterColor = getColor(R.color.worstMeterColor);
        weakMeterColor = getColor(R.color.weakMeterColor);
        mediumMeterColor = getColor(R.color.mediumMeterColor);
        strongMeterColor = getColor(R.color.strongMeterColor);
        excellentMeterColor = getColor(R.color.excellentMeterColor);

        clipboardManager = (ClipboardManager) requireActivity().getSystemService(CLIPBOARD_SERVICE);

        /*======================================================================================*/

        fragmentBinding.passwordText.addTextChangedListener(passwordTextWatcher);

        // ON CLICK TIME TO CRACK HELP ICON
        fragmentBinding.timeHelp.setOnClickListener(v ->
                ((MainActivity)requireActivity()).DisplayFragment("Time Help")
        );

        // ON CLICK TIME TO CRACK SETTINGS ICON
        fragmentBinding.timeSettings.setOnClickListener(v ->
                CrackTimesBottomSheet());

        // CRACK TIME TEXT
        if (preferenceManager.getInt(CRACK_TIMES_PREF) == 0
                || preferenceManager.getInt(CRACK_TIMES_PREF) == R.id.option_1) {
            fragmentBinding.crackTime.setText(getString(R.string.offline_slow));
        }
        else if (preferenceManager.getInt(CRACK_TIMES_PREF) == R.id.option_2) {
            fragmentBinding.crackTime.setText(getString(R.string.offline_fast));
        }
        else if (preferenceManager.getInt(CRACK_TIMES_PREF) == R.id.option_3) {
            fragmentBinding.crackTime.setText(getString(R.string.online_slow));
        }
        else if (preferenceManager.getInt(CRACK_TIMES_PREF) == R.id.option_4) {
            fragmentBinding.crackTime.setText(getString(R.string.online_fast));
        }

        // ON CLICK SCORE TEXT VIEW, EXPAND LAYOUT
        fragmentBinding.scoreTextView.setOnClickListener(v -> {
            if (!isExpanded) {
                fragmentBinding.expandedLayout.setVisibility(View.VISIBLE);
                isExpanded = true;
                fragmentBinding.scoreTextView.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_up_arrow,0, 0, 0);
            }
            else{
                fragmentBinding.expandedLayout.setVisibility(View.GONE);
                isExpanded = false;
                fragmentBinding.scoreTextView.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_down_arrow,0, 0, 0);
            }
        });

        // ON CLICK SCORE HELP ICON
        fragmentBinding.scoreHelp.setOnClickListener(v ->
                ((MainActivity)requireActivity()).DisplayFragment("Score Help")
        );


        // CLEAR CLIPBOARD AFTER 30 SECONDS IF COPIED FROM THIS APP
        clipboardManager.addPrimaryClipChangedListener(() -> {

            copiedFromHere = true;

            if (clearClipboardTimer != null) {
                clearClipboardTimer.cancel();
            }

            clearClipboardTimer = new CountDownTimer(30000, 1000) {

                public void onTick(long millisUntilFinished) {}

                // ON TIMER FINISH, PERFORM ACTION
                public void onFinish() {
                    clearClipboard();
                }
            }.start();

        });

    }

    // PASSWORD TEXT WATCHER
    private final TextWatcher passwordTextWatcher = new TextWatcher() {

        CountDownTimer delayTimer = null;

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            // INTRODUCE A SUBTLE DELAY,
            // SO PASSWORDS ARE CHECKED AFTER TYPING IS FINISHED
            if (delayTimer != null) {
                delayTimer.cancel();
            }

            delayTimer = new CountDownTimer(wait, 100) {

                public void onTick(long millisUntilFinished) {
                }

                // ON TIMER FINISH, PERFORM ACTION
                public void onFinish() {

                    passwordString = Objects.requireNonNull(fragmentBinding.passwordText.getText()).toString();
                    passwordLength = passwordString.length();
                    wait = 400;

                    // IF EDIT TEXT NOT EMPTY
                    if (!passwordString.equals("")) {

                        Strength strength = zxcvbn.measure(passwordString);
                        long crackTimeMilliSeconds = 0;
                        String timeToCrackString = null;

                        if (preferenceManager.getInt(CRACK_TIMES_PREF) == 0
                            || preferenceManager.getInt(CRACK_TIMES_PREF) == R.id.option_1) {
                            timeToCrackString = strength.getCrackTimesDisplay().getOfflineSlowHashing1e4perSecond();
                            crackTimeMilliSeconds = (long) ((strength
                                    .getCrackTimeSeconds()
                                    .getOfflineSlowHashing1e4perSecond()) * 1000);
                        }
                        else if (preferenceManager.getInt(CRACK_TIMES_PREF) == R.id.option_2) {
                            timeToCrackString = strength.getCrackTimesDisplay().getOfflineFastHashing1e10PerSecond();
                            crackTimeMilliSeconds = (long) ((strength
                                    .getCrackTimeSeconds()
                                    .getOfflineFastHashing1e10PerSecond()) * 1000);
                        }
                        else if (preferenceManager.getInt(CRACK_TIMES_PREF) == R.id.option_3) {
                            timeToCrackString = strength.getCrackTimesDisplay().getOnlineThrottling100perHour();
                            crackTimeMilliSeconds = (long) ((strength
                                    .getCrackTimeSeconds()
                                    .getOnlineThrottling100perHour()) * 1000);
                        }
                        else if (preferenceManager.getInt(CRACK_TIMES_PREF) == R.id.option_4) {
                            timeToCrackString = strength.getCrackTimesDisplay().getOnlineNoThrottling10perSecond();
                            crackTimeMilliSeconds = (long) ((strength
                                    .getCrackTimeSeconds()
                                    .getOnlineNoThrottling10perSecond()) * 1000);
                        }

                        switch (passwordCrackTimeResult(crackTimeMilliSeconds)) {

                            case "WORST":
                                WorstStrengthMeter();
                                break;

                            case "WEAK":
                                WeakStrengthMeter();
                                break;

                            case "MEDIUM":
                                MediumStrengthMeter();
                                break;

                            case "STRONG":
                                StrongStrengthMeter();
                                break;

                            case "EXCELLENT":
                                ExcellentStrengthMeter();
                                break;

                        }

                        // ESTIMATED TIME TO CRACK
                        // REPLACE HARDCODED STRINGS FROM THE LIBRARY FOR PROPER LANGUAGE SUPPORT
                        // SINCE DEVS OF ZXCVBN4J WON'T FIX IT, WE DO IT OURSELVES
                        assert timeToCrackString != null;
                        if (timeToCrackString.contains("less than a second")) {
                            timeToCrackString = timeToCrackString
                                    .replace("less than a second", getString(R.string.less_than_sec));
                        }

                        else if (timeToCrackString.contains("second")) {
                            timeToCrackString = timeToCrackString
                                    .replace("second", getString(R.string.second));
                        }

                        else if (timeToCrackString.contains("minute")) {
                            timeToCrackString = timeToCrackString
                                    .replace("minute", getString(R.string.minute));
                        }

                        else if (timeToCrackString.contains("hour")) {
                            timeToCrackString = timeToCrackString
                                    .replace("hour", getString(R.string.hour));
                        }

                        else if (timeToCrackString.contains("day")) {
                            timeToCrackString = timeToCrackString
                                    .replace("day", getString(R.string.day));
                        }

                        else if (timeToCrackString.contains("month")) {
                            timeToCrackString = timeToCrackString
                                    .replace("month", getString(R.string.month));
                        }

                        else if (timeToCrackString.contains("year")) {
                            timeToCrackString = timeToCrackString
                                    .replace("year", getString(R.string.year));
                        }

                        else if (timeToCrackString.contains("centuries")) {
                            timeToCrackString = timeToCrackString
                                    .replace("centuries", getString(R.string.centuries));
                        }
                        fragmentBinding.timeToCrackSubtitle.setText(timeToCrackString);

                        // WARNING
                        // IF EMPTY, SET CUSTOM WARNING MESSAGE
                        if (strength.getFeedback().getWarning(Locale.getDefault()).isEmpty()) {

                            switch (passwordCrackTimeResult(crackTimeMilliSeconds)) {
                                case "WORST":
                                    fragmentBinding.warningSubtitle.setText(worstPassWarning); // WORST WARNING
                                    break;

                                case "WEAK":
                                    fragmentBinding.warningSubtitle.setText(weakPassWarning); // WEAK WARNING
                                    break;

                                case "MEDIUM":
                                    fragmentBinding.warningSubtitle.setText(mediumPassWarning); // MEDIUM WARNING
                                    break;

                                default:
                                    fragmentBinding.warningSubtitle.setText(not_applicable); // FOR STRONG AND ABOVE
                                    break;
                            }
                        }

                        // IF NOT EMPTY, DISPLAY WARNING
                        else {
                            fragmentBinding.warningSubtitle.setText(strength.getFeedback().getWarning(Locale.getDefault()));
                        }

                        // SUGGESTIONS
                        List<String> suggestions = strength.getFeedback().getSuggestions(Locale.getDefault());

                        if (suggestions != null && suggestions.size() != 0) {
                            suggestionText.setLength(0);
                            for (int i = 0; i < suggestions.size(); i++) {

                                suggestionText = suggestionText.append("\u2022 ").append(suggestions.get(i)).append("\n");
                            }

                            fragmentBinding.suggestionsSubtitle.setText(suggestionText.toString());
                        }
                        else {
                            fragmentBinding.suggestionsSubtitle.setText(getString(R.string.not_applicable));
                        }

                        // SCORE
                        Score(passwordString, passwordLength);

                    }

                    // IF EDIT TEXT IS EMPTY OR CLEARED, RESET EVERYTHING
                    else {
                        DetailsReset();
                        ScoreReset();
                    }

                }

            }.start();

        }

        @Override
        public void afterTextChanged(Editable editable) {
        }

    };

    private int getColor(int color) {
        return getResources().getColor(color, requireActivity().getTheme());
    }

    // WORST STRENGTH METER
    private void WorstStrengthMeter() {
        fragmentBinding.strengthSubtitle.setText(worst);

        fragmentBinding.worstMeter.setIndicatorColor(worstMeterColor);
        fragmentBinding.weakMeter.setIndicatorColor(emptyMeterColor);
        fragmentBinding.mediumMeter.setIndicatorColor(emptyMeterColor);
        fragmentBinding.strongMeter.setIndicatorColor(emptyMeterColor);
        fragmentBinding.excellentMeter.setIndicatorColor(emptyMeterColor);

        fragmentBinding.worstMeter.setProgressCompat(100, true);
        fragmentBinding.weakMeter.setProgressCompat(0, true);
        fragmentBinding.mediumMeter.setProgressCompat(0, true);
        fragmentBinding.strongMeter.setProgressCompat(0, true);
        fragmentBinding.excellentMeter.setProgressCompat(0, true);
    }

    // WEAK STRENGTH METER
    private void WeakStrengthMeter() {
        fragmentBinding.strengthSubtitle.setText(weak);

        fragmentBinding.worstMeter.setIndicatorColor(weakMeterColor);
        fragmentBinding.weakMeter.setIndicatorColor(weakMeterColor);
        fragmentBinding.mediumMeter.setIndicatorColor(emptyMeterColor);
        fragmentBinding.strongMeter.setIndicatorColor(emptyMeterColor);
        fragmentBinding.excellentMeter.setIndicatorColor(emptyMeterColor);

        fragmentBinding.worstMeter.setProgressCompat(100, true);
        fragmentBinding.weakMeter.setProgressCompat(100, true);
        fragmentBinding.mediumMeter.setProgressCompat(0, true);
        fragmentBinding.strongMeter.setProgressCompat(0, true);
        fragmentBinding.excellentMeter.setProgressCompat(0, true);
    }

    // MEDIUM STRENGTH METER
    private void MediumStrengthMeter() {
        fragmentBinding.strengthSubtitle.setText(medium);

        fragmentBinding.worstMeter.setIndicatorColor(mediumMeterColor);
        fragmentBinding.weakMeter.setIndicatorColor(mediumMeterColor);
        fragmentBinding.mediumMeter.setIndicatorColor(mediumMeterColor);
        fragmentBinding.strongMeter.setIndicatorColor(emptyMeterColor);
        fragmentBinding.excellentMeter.setIndicatorColor(emptyMeterColor);

        fragmentBinding.worstMeter.setProgressCompat(100, true);
        fragmentBinding.weakMeter.setProgressCompat(100, true);
        fragmentBinding.mediumMeter.setProgressCompat(100, true);
        fragmentBinding.strongMeter.setProgressCompat(0, true);
        fragmentBinding.excellentMeter.setProgressCompat(0, true);
    }

    // STRONG STRENGTH METER
    private void StrongStrengthMeter() {
        fragmentBinding.strengthSubtitle.setText(strong);

        fragmentBinding.worstMeter.setIndicatorColor(strongMeterColor);
        fragmentBinding.weakMeter.setIndicatorColor(strongMeterColor);
        fragmentBinding.mediumMeter.setIndicatorColor(strongMeterColor);
        fragmentBinding.strongMeter.setIndicatorColor(strongMeterColor);
        fragmentBinding.excellentMeter.setIndicatorColor(emptyMeterColor);

        fragmentBinding.worstMeter.setProgressCompat(100, true);
        fragmentBinding.weakMeter.setProgressCompat(100, true);
        fragmentBinding.mediumMeter.setProgressCompat(100, true);
        fragmentBinding.strongMeter.setProgressCompat(100, true);
        fragmentBinding.excellentMeter.setProgressCompat(0, true);
    }

    // EXCELLENT STRENGTH METER
    private void ExcellentStrengthMeter() {
        fragmentBinding.strengthSubtitle.setText(excellent);

        fragmentBinding.worstMeter.setIndicatorColor(excellentMeterColor);
        fragmentBinding.weakMeter.setIndicatorColor(excellentMeterColor);
        fragmentBinding.mediumMeter.setIndicatorColor(excellentMeterColor);
        fragmentBinding.strongMeter.setIndicatorColor(excellentMeterColor);
        fragmentBinding.excellentMeter.setIndicatorColor(excellentMeterColor);

        fragmentBinding.worstMeter.setProgressCompat(100, true);
        fragmentBinding.weakMeter.setProgressCompat(100, true);
        fragmentBinding.mediumMeter.setProgressCompat(100, true);
        fragmentBinding.strongMeter.setProgressCompat(100, true);
        fragmentBinding.excellentMeter.setProgressCompat(100, true);
    }

    // PASSWORD SCORE
    private void Score(String passwordString, int passwordStringLength)
    {

        // SCORING SYSTEM :
        // IF PASSWORD LENGTH GREATER THAN 5, BASE SCORE = 10, ELSE 0
        // IF PASSWORD LENGTH GREATER THAN 5, LENGTH SCORE = 3 POINTS FOR EACH EXTRA CHARACTER, ELSE 0
        // UPPER CASE SCORE = 4 POINTS FOR EACH UPPER CASE CHAR
        // NUMBERS SCORE = 5 POINTS FOR EACH NUMBER
        // SPECIAL CHAR = 5 POINTS FOR EACH SPECIAL CHAR
        // PENALTY = IF ALL UPPER CASE/LOWER CASE/NUMBERS/SPECIAL CHAR, SUBTRACT 15 POINTS TOTAL

        if (passwordStringLength>5)
        {
            // BASE SCORE
            baseScore=10;
            fragmentBinding.baseScore.setText(String.valueOf(baseScore));

            for(int i=0; i < passwordStringLength; i++)
            {
                if (Character.isUpperCase(passwordString.charAt(i)))
                {
                    upperCaseCount++;
                }
                else if (Character.isLowerCase(passwordString.charAt(i)))
                {
                    lowerCaseCount++;
                }
                else if (Character.isDigit(passwordString.charAt(i)))
                {
                    numCount++;
                }
                else if (!Character.isDigit(passwordString.charAt(i))
                        && !Character.isLetter(passwordString.charAt(i))
                        && !Character.isWhitespace(passwordString.charAt(i)))
                {
                    specialCharCount++;
                }

            }

            for (int j=0; j < passwordStringLength - 5; j++)
            {
                lengthCount++;
            }

            if (upperCaseCount == passwordStringLength)
            {
                penaltyScore = -15;
                fragmentBinding.penaltyTitle.setText(getString(R.string.all_upper_penalty));
            }
            else if (lowerCaseCount == passwordStringLength)
            {
                penaltyScore = -15;
                fragmentBinding.penaltyTitle.setText(getString(R.string.all_lower_penalty));
            }
            else if (numCount == passwordStringLength)
            {
                penaltyScore = -15;
                fragmentBinding.penaltyTitle.setText(getString(R.string.all_num_penalty));
            }
            else if (specialCharCount == passwordStringLength)
            {
                penaltyScore = -15;
                fragmentBinding.penaltyTitle.setText(getString(R.string.all_special_char_penalty));
            }
            else
            {
                penaltyScore = 0;
                fragmentBinding.penaltyTitle.setText(zero);
            }

            lengthScore = lengthScoreMultiplier * lengthCount;
            upperCaseScore = upperCaseScoreMultiplier * upperCaseCount;
            numScore = numScoreMultiplier * numCount;
            specialCharScore = specialCharScoreMultiplier * specialCharCount;
            totalScore = baseScore+lengthScore+upperCaseScore+numScore+specialCharScore+penaltyScore;
        }

        // IF PASSWORD LENGTH LESS THAN 5, RESET ALL SCORES TO 0
        else
        {
            ScoreReset();
        }

        // LENGTH SCORE
        fragmentBinding.lengthScore.setText(String.valueOf(lengthScore));
        lengthCount = 0;
        lengthScore = 0;

        // UPPER CASE SCORE
        fragmentBinding.upperCaseScore.setText(String.valueOf(upperCaseScore));
        upperCaseCount = 0;
        upperCaseScore = 0;

        // NUMBERS SCORE
        fragmentBinding.numScore.setText(String.valueOf(numScore));
        numCount = 0;
        numScore = 0;

        // SPECIAL CHARACTERS SCORE
        fragmentBinding.specialCharScore.setText(String.valueOf(specialCharScore));
        specialCharCount = 0;
        specialCharScore = 0;

        // PENALTY SCORE
        if (penaltyScore!=0) {
            fragmentBinding.penaltyLayout.setVisibility(View.VISIBLE);
            fragmentBinding.penaltyScore.setText(String.valueOf(penaltyScore));
        }
        else {
            fragmentBinding.penaltyLayout.setVisibility(View.GONE);
        }
        penaltyScore = 0;
        lowerCaseCount = 0;

        // TOTAL SCORE
        fragmentBinding.totalScore.setText(String.valueOf(totalScore));
        totalScore=0;
    }

    // RESET DETAILS
    private void DetailsReset() {
        fragmentBinding.strengthSubtitle.setText(not_applicable);

        fragmentBinding.worstMeter.setIndicatorColor(emptyMeterColor);
        fragmentBinding.weakMeter.setIndicatorColor(emptyMeterColor);
        fragmentBinding.mediumMeter.setIndicatorColor(emptyMeterColor);
        fragmentBinding.strongMeter.setIndicatorColor(emptyMeterColor);
        fragmentBinding.excellentMeter.setIndicatorColor(emptyMeterColor);

        fragmentBinding.worstMeter.setProgressCompat(0, true);
        fragmentBinding.weakMeter.setProgressCompat(0, true);
        fragmentBinding.mediumMeter.setProgressCompat(0, true);
        fragmentBinding.strongMeter.setProgressCompat(0, true);
        fragmentBinding.excellentMeter.setProgressCompat(0, true);

        fragmentBinding.timeToCrackSubtitle.setText(not_applicable);
        fragmentBinding.warningSubtitle.setText(not_applicable);
        fragmentBinding.suggestionsSubtitle.setText(not_applicable);

    }

    // RESET SCORE
    private void ScoreReset()
    {
        fragmentBinding.totalScore.setText(zero);
        fragmentBinding.lengthScore.setText(zero);
        fragmentBinding.baseScore.setText(zero);
        fragmentBinding.upperCaseScore.setText(zero);
        fragmentBinding.numScore.setText(zero);
        fragmentBinding.specialCharScore.setText(zero);
        fragmentBinding.penaltyScore.setText(zero);

        fragmentBinding.penaltyLayout.setVisibility(View.GONE);
    }

    // CHECK PASSWORD CRACK TIME CUSTOM RESULT
    private String passwordCrackTimeResult(long crackTimeMilliSeconds) {
        String result = "";

        // TAKE DAYS IN:
        // MONTH=31, YEAR=365

        // WORST = LESS THAN/EQUAL TO 2 MINUTES
        if (crackTimeMilliSeconds < TimeUnit.MINUTES.toMillis(2)
                || crackTimeMilliSeconds == TimeUnit.MINUTES.toMillis(2)) {
            result = "WORST";
        }

        // WEAK = MORE THAN 2 MINUTES, LESS THAN/EQUAL TO 5 DAYS
        else if (crackTimeMilliSeconds > TimeUnit.MINUTES.toMillis(2)
                && crackTimeMilliSeconds < TimeUnit.DAYS.toMillis(5)
                || crackTimeMilliSeconds == TimeUnit.DAYS.toMillis(5)) {
            result = "WEAK";
        }

        // MEDIUM = MORE THAN 5 DAYS, LESS THAN/EQUAL TO 5 MONTHS
        else if (crackTimeMilliSeconds > TimeUnit.DAYS.toMillis(5)
                && crackTimeMilliSeconds < TimeUnit.DAYS.toMillis(155)
                || crackTimeMilliSeconds == TimeUnit.DAYS.toMillis(155)) {
            result = "MEDIUM";
        }

        // STRONG = MORE THAN 5 MONTHS, LESS THAN/EQUAL TO 5 YEARS
        else if (crackTimeMilliSeconds > TimeUnit.DAYS.toMillis(155)
                && crackTimeMilliSeconds < TimeUnit.DAYS.toMillis(1825)
                || crackTimeMilliSeconds == TimeUnit.DAYS.toMillis(1825)) {
            result = "STRONG";
        }

        // EXCELLENT = MORE THAN 5 YEARS
        else if (crackTimeMilliSeconds > TimeUnit.DAYS.toMillis(1825)) {
            result = "EXCELLENT";
        }

        return result;

    }

    // CLEAR PASSWORD COPIED TO CLIPBOARD
    private void clearClipboard(){
        if (Build.VERSION.SDK_INT >= 28) {
            clipboardManager.clearPrimaryClip();
        }
        else {
            clipboardManager.setPrimaryClip(ClipData.newPlainText(null, null));
        }

        copiedFromHere=false;
    }

    // CRACK TIMES BOTTOM SHEET
    private void CrackTimesBottomSheet(){
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext(), R.style.CustomBottomSheetTheme);
        bottomSheetDialog.setCancelable(true);

        BottomSheetCrackTimesBinding bottomSheetBinding;
        BottomSheetHeaderBinding headerBinding;
        bottomSheetBinding = BottomSheetCrackTimesBinding.inflate(getLayoutInflater());
        headerBinding = BottomSheetHeaderBinding.bind(bottomSheetBinding.getRoot());
        bottomSheetDialog.setContentView(bottomSheetBinding.getRoot());

        // DEFAULT CHECKED RADIO
        if (preferenceManager.getInt(CRACK_TIMES_PREF) == 0){
                preferenceManager.setInt(CRACK_TIMES_PREF, R.id.option_1);

        }
        bottomSheetBinding.crackTimesRadiogroup.check(preferenceManager.getInt(CRACK_TIMES_PREF));

        // TITLE
        headerBinding.bottomSheetTitle.setText(R.string.crack_times);

        // CANCEL BUTTON
        bottomSheetBinding.cancelButton.setOnClickListener(view12 ->
                bottomSheetDialog.cancel());

        // ON SELECTING OPTION
        bottomSheetBinding.crackTimesRadiogroup
                .setOnCheckedChangeListener((radioGroup, checkedId) -> {
                    preferenceManager.setInt(CRACK_TIMES_PREF, checkedId);
                    bottomSheetDialog.dismiss();
                    getParentFragmentManager().beginTransaction().detach(this).commitNow();
                    getParentFragmentManager().beginTransaction().attach(this).commitNow();
                });

        // SHOW BOTTOM SHEET WITH CUSTOM ANIMATION
        Objects.requireNonNull(bottomSheetDialog.getWindow()).getAttributes().windowAnimations = R.style.BottomSheetAnimation;
        bottomSheetDialog.show();
    }

    // CLEAR CLIPBOARD IMMEDIATELY WHEN FRAGMENT DESTROYED
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        clearClipboard();
        fragmentBinding = null;
    }

}