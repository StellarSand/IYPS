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

import com.iyps.R;
import com.iyps.activities.MainActivity;
import com.iyps.databinding.FragmentMainBinding;
import com.nulabinc.zxcvbn.Strength;
import com.nulabinc.zxcvbn.Zxcvbn;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class MainFragment extends Fragment {

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

        /*########################################################################################*/

        fragmentBinding.passwordText.addTextChangedListener(passwordTextWatcher);

        // On click score text view, expand layout
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

        // On click score help icon
        fragmentBinding.scoreHelp.setOnClickListener(v ->
                ((MainActivity)requireActivity()).DisplayFragment("Score Help")
        );


        // Clear clipboard after 1 minute if copied from this app
        clipboardManager.addPrimaryClipChangedListener(() -> {

            copiedFromHere = true;

            if (clearClipboardTimer != null) {
                clearClipboardTimer.cancel();
            }

            clearClipboardTimer = new CountDownTimer(60000, 1000) {

                public void onTick(long millisUntilFinished) {}

                // ON TIMER FINISH, PERFORM ACTION
                public void onFinish() {
                    clearClipboard();
                }
            }.start();

        });

    }

    // Password text watcher
    private final TextWatcher passwordTextWatcher = new TextWatcher() {

        CountDownTimer delayTimer = null;

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            // Introduce a subtle delay
            // So passwords are checked after typing is finished
            if (delayTimer != null) {
                delayTimer.cancel();
            }

            delayTimer = new CountDownTimer(wait, 100) {

                public void onTick(long millisUntilFinished) {
                }

                // On timer finish, perform task
                public void onFinish() {

                    passwordString = Objects.requireNonNull(fragmentBinding.passwordText.getText()).toString();
                    passwordLength = passwordString.length();
                    wait = 400;

                    // If edit text is not empty
                    if (!passwordString.equals("")) {

                        Strength strength = zxcvbn.measure(passwordString);
                        long crackTimeMilliSeconds;
                        String timeToCrackString;


                        timeToCrackString = strength.getCrackTimesDisplay().getOfflineSlowHashing1e4perSecond();
                        crackTimeMilliSeconds = (long) ((strength
                                                        .getCrackTimeSeconds()
                                                        .getOfflineSlowHashing1e4perSecond()) * 1000);

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

                        // Estimated time to crack
                        // Replace hardcoded strings form the library for proper language support
                        // Since devs of zxcvbn4j won't fix it, we do it ourselves
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

                        // Warning
                        // If empty, set to custom warning message
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
                        // If not empty, display inbuilt warning
                        else {
                            fragmentBinding.warningSubtitle.setText(strength.getFeedback().getWarning(Locale.getDefault()));
                        }

                        // Suggestions
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

                        // Score
                        Score(passwordString, passwordLength);

                    }

                    // If edit text is empty or cleared, reset everything
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

    // Worst strength meter
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

    // Weak strength meter
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

    // Medium strength meter
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

    // Strong strength meter
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

    // Excellent strength meter
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

    // Password score
    private void Score(String passwordString, int passwordStringLength)
    {

        // Scoring system:
        // If password length greater than 5, base score = 10, else 0
        // If password length greater than 5, length score = 3 points for each extra character, else 0
        // Upper case score = 4 points for each upper case char
        // Numbers score = 5 points for each number
        // Special char = 5 points for each special char
        // Penalty = if all upper case/lower case/numbers/special char, subtract 15 points total

        if (passwordStringLength > 5)
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

        // If password length less than 5, reset all scores to 0
        else
        {
            ScoreReset();
        }

        // Length score
        fragmentBinding.lengthScore.setText(String.valueOf(lengthScore));
        lengthCount = 0;
        lengthScore = 0;

        // Upper case score
        fragmentBinding.upperCaseScore.setText(String.valueOf(upperCaseScore));
        upperCaseCount = 0;
        upperCaseScore = 0;

        // Numbers score
        fragmentBinding.numScore.setText(String.valueOf(numScore));
        numCount = 0;
        numScore = 0;

        // Special char score
        fragmentBinding.specialCharScore.setText(String.valueOf(specialCharScore));
        specialCharCount = 0;
        specialCharScore = 0;

        // Penalty score
        if (penaltyScore!=0) {
            fragmentBinding.penaltyLayout.setVisibility(View.VISIBLE);
            fragmentBinding.penaltyScore.setText(String.valueOf(penaltyScore));
        }
        else {
            fragmentBinding.penaltyLayout.setVisibility(View.GONE);
        }
        penaltyScore = 0;
        lowerCaseCount = 0;

        // Total score
        fragmentBinding.totalScore.setText(String.valueOf(totalScore));
        totalScore=0;
    }

    // Reset details
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

    // Reset score
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

    // Check password crack time (custom result)
    private String passwordCrackTimeResult(long crackTimeMilliSeconds) {
        String result = "";

        // Tke days in:
        // Month = 31, Year = 365

        // Worst = less than/equal to 2 minutes
        if (crackTimeMilliSeconds < TimeUnit.MINUTES.toMillis(2)
                || crackTimeMilliSeconds == TimeUnit.MINUTES.toMillis(2)) {

            result = "WORST";
        }
        // Weak = more than 2 minutes, less than/equal to 30 days
        else if (crackTimeMilliSeconds > TimeUnit.MINUTES.toMillis(2)
                && crackTimeMilliSeconds <= TimeUnit.DAYS.toMillis(30)) {

            result = "WEAK";
        }
        // Medium = more than 30 days, less than/equal to 6 months
        else if (crackTimeMilliSeconds > TimeUnit.DAYS.toMillis(30)
                && crackTimeMilliSeconds <= TimeUnit.DAYS.toMillis(186)) {

            result = "MEDIUM";
        }
        // Strong = more than 6 months, less than/equal to 5 years
        else if (crackTimeMilliSeconds > TimeUnit.DAYS.toMillis(186)
                && crackTimeMilliSeconds <= TimeUnit.DAYS.toMillis(1825)) {

            result = "STRONG";
        }
        // Excellent = more than 5 years
        else if (crackTimeMilliSeconds > TimeUnit.DAYS.toMillis(1825)) {

            result = "EXCELLENT";
        }

        return result;

    }

    // Clear password from clipboard
    private void clearClipboard(){
        if (Build.VERSION.SDK_INT >= 28) {
            clipboardManager.clearPrimaryClip();
        }
        else {
            clipboardManager.setPrimaryClip(ClipData.newPlainText(null, null));
        }

        copiedFromHere=false;
    }

    // Clear clipboard immediately when fragment destroyed
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        clearClipboard();
        fragmentBinding = null;
    }

}