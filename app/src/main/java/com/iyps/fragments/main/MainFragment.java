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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.iyps.R;
import com.iyps.activities.MainActivity;
import com.nulabinc.zxcvbn.Strength;
import com.nulabinc.zxcvbn.Zxcvbn;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class MainFragment extends Fragment {

    private TextInputEditText passwordEditText;
    private TextView scoreTextView;
    private LinearLayout expandedLayout, penaltyLayout;
    private TextView strengthSubtitle, timeToCrackSubtitle, warningSubtitle, suggestionsSubtitle,
                    totalScoreText, baseScoreText, lengthScoreText, upperCaseScoreText,
                    numScoreText, specialCharScoreText, penaltyScoreTitle, penaltyScoreText;
    private LinearProgressIndicator worstMeter, weakMeter, mediumMeter, strongMeter, excellentMeter;
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        passwordEditText = view.findViewById(R.id.password_input);
        strengthSubtitle = view.findViewById(R.id.strength_subtitle);
        timeToCrackSubtitle = view.findViewById(R.id.time_to_crack_subtitle);
        warningSubtitle = view.findViewById(R.id.warning_subtitle);
        suggestionsSubtitle = view.findViewById(R.id.suggestions_subtitle);
        worstMeter = view.findViewById(R.id.worst_meter);
        weakMeter = view.findViewById(R.id.weak_meter);
        mediumMeter = view.findViewById(R.id.medium_meter);
        strongMeter = view.findViewById(R.id.strong_meter);
        excellentMeter = view.findViewById(R.id.excellent_meter);
        scoreTextView = view.findViewById(R.id.score_text_view);
        ImageView scoreDetails = view.findViewById(R.id.score_details_img);
        totalScoreText = view.findViewById(R.id.total_score);
        baseScoreText = view.findViewById(R.id.base_score);
        lengthScoreText = view.findViewById(R.id.length_score);
        upperCaseScoreText = view.findViewById(R.id.upper_case_score);
        numScoreText = view.findViewById(R.id.num_score);
        specialCharScoreText = view.findViewById(R.id.special_char_score);
        expandedLayout = view.findViewById(R.id.expanded_layout);
        penaltyLayout = view.findViewById(R.id.penalty_layout);
        penaltyScoreTitle = view.findViewById(R.id.penalty_title);
        penaltyScoreText = view.findViewById(R.id.penalty_score);

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

        worst = getResources().getString(R.string.worst);
        weak = getResources().getString(R.string.weak);
        medium = getResources().getString(R.string.medium);
        strong = getResources().getString(R.string.strong);
        excellent = getResources().getString(R.string.excellent);
        worstPassWarning = getResources().getString(R.string.worst_pass_warning);
        weakPassWarning = getResources().getString(R.string.weak_pass_warning);
        mediumPassWarning = getResources().getString(R.string.medium_pass_warning);
        not_applicable = getResources().getString(R.string.not_applicable);
        zero = getResources().getString(R.string.zero);

        emptyMeterColor = getColor(R.color.hintColor);
        worstMeterColor = getColor(R.color.worstMeterColor);
        weakMeterColor = getColor(R.color.weakMeterColor);
        mediumMeterColor = getColor(R.color.mediumMeterColor);
        strongMeterColor = getColor(R.color.strongMeterColor);
        excellentMeterColor = getColor(R.color.excellentMeterColor);

        clipboardManager = (ClipboardManager) requireActivity().getSystemService(CLIPBOARD_SERVICE);

        /*======================================================================================*/

        passwordEditText.addTextChangedListener(passwordTextWatcher);

        // ON CLICK SCORE TEXT VIEW, EXPAND LAYOUT
        scoreTextView.setOnClickListener(v -> {
            if (!isExpanded) {
                expandedLayout.setVisibility(View.VISIBLE);
                isExpanded = true;
                scoreTextView.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_up_arrow,0, 0, 0);
            }
            else{
                expandedLayout.setVisibility(View.GONE);
                isExpanded = false;
                scoreTextView.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_down_arrow,0, 0, 0);
            }
        });

        // ON CLICK INFO ICON
        scoreDetails.setOnClickListener(v ->
                ((MainActivity)requireActivity()).DisplayFragment("Score Details")
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

                    passwordString = Objects.requireNonNull(passwordEditText.getText()).toString();
                    passwordLength = passwordString.length();
                    wait = 400;

                    // IF EDIT TEXT NOT EMPTY
                    if (!passwordString.equals("")) {

                        Strength strength = zxcvbn.measure(passwordString);

                        long crackTimeMilliSeconds = (long) ((strength
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

                        // ESTIMATED TIME TO CRACK
                        timeToCrackSubtitle.setText(strength.getCrackTimesDisplay().getOfflineSlowHashing1e4perSecond());

                        // WARNING
                        // IF EMPTY, SET CUSTOM WARNING MESSAGE
                        if (strength.getFeedback().getWarning(Locale.getDefault()).isEmpty()) {

                            switch (passwordCrackTimeResult(crackTimeMilliSeconds)) {
                                case "WORST":
                                    warningSubtitle.setText(worstPassWarning); // WORST WARNING
                                    break;

                                case "WEAK":
                                    warningSubtitle.setText(weakPassWarning); // WEAK WARNING
                                    break;

                                case "MEDIUM":
                                    warningSubtitle.setText(mediumPassWarning); // MEDIUM WARNING
                                    break;

                                default:
                                    warningSubtitle.setText(not_applicable); // FOR STRONG AND ABOVE
                                    break;
                            }
                        }

                        // IF NOT EMPTY, DISPLAY WARNING
                        else {
                            warningSubtitle.setText(strength.getFeedback().getWarning(Locale.getDefault()));
                        }

                        // SUGGESTIONS
                        List<String> suggestions = strength.getFeedback().getSuggestions(Locale.getDefault());

                        if (suggestions != null && suggestions.size() != 0) {
                            suggestionText.setLength(0);
                            for (int i = 0; i < suggestions.size(); i++) {

                                suggestionText = suggestionText.append("\u2022 ").append(suggestions.get(i)).append("\n");
                            }

                            suggestionsSubtitle.setText(suggestionText.toString());
                        }
                        else {
                            suggestionsSubtitle.setText(getResources().getString(R.string.not_applicable));
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
        strengthSubtitle.setText(worst);

        worstMeter.setIndicatorColor(worstMeterColor);
        weakMeter.setIndicatorColor(emptyMeterColor);
        mediumMeter.setIndicatorColor(emptyMeterColor);
        strongMeter.setIndicatorColor(emptyMeterColor);
        excellentMeter.setIndicatorColor(emptyMeterColor);

        worstMeter.setProgressCompat(100, true);
        weakMeter.setProgressCompat(0, true);
        mediumMeter.setProgressCompat(0, true);
        strongMeter.setProgressCompat(0, true);
        excellentMeter.setProgressCompat(0, true);
    }

    // WEAK STRENGTH METER
    private void WeakStrengthMeter() {
        strengthSubtitle.setText(weak);

        worstMeter.setIndicatorColor(weakMeterColor);
        weakMeter.setIndicatorColor(weakMeterColor);
        mediumMeter.setIndicatorColor(emptyMeterColor);
        strongMeter.setIndicatorColor(emptyMeterColor);
        excellentMeter.setIndicatorColor(emptyMeterColor);

        worstMeter.setProgressCompat(100, true);
        weakMeter.setProgressCompat(100, true);
        mediumMeter.setProgressCompat(0, true);
        strongMeter.setProgressCompat(0, true);
        excellentMeter.setProgressCompat(0, true);
    }

    // MEDIUM STRENGTH METER
    private void MediumStrengthMeter() {
        strengthSubtitle.setText(medium);

        worstMeter.setIndicatorColor(mediumMeterColor);
        weakMeter.setIndicatorColor(mediumMeterColor);
        mediumMeter.setIndicatorColor(mediumMeterColor);
        strongMeter.setIndicatorColor(emptyMeterColor);
        excellentMeter.setIndicatorColor(emptyMeterColor);

        worstMeter.setProgressCompat(100, true);
        weakMeter.setProgressCompat(100, true);
        mediumMeter.setProgressCompat(100, true);
        strongMeter.setProgressCompat(0, true);
        excellentMeter.setProgressCompat(0, true);
    }

    // STRONG STRENGTH METER
    private void StrongStrengthMeter() {
        strengthSubtitle.setText(strong);

        worstMeter.setIndicatorColor(strongMeterColor);
        weakMeter.setIndicatorColor(strongMeterColor);
        mediumMeter.setIndicatorColor(strongMeterColor);
        strongMeter.setIndicatorColor(strongMeterColor);
        excellentMeter.setIndicatorColor(emptyMeterColor);

        worstMeter.setProgressCompat(100, true);
        weakMeter.setProgressCompat(100, true);
        mediumMeter.setProgressCompat(100, true);
        strongMeter.setProgressCompat(100, true);
        excellentMeter.setProgressCompat(0, true);
    }

    // EXCELLENT STRENGTH METER
    private void ExcellentStrengthMeter() {
        strengthSubtitle.setText(excellent);

        worstMeter.setIndicatorColor(excellentMeterColor);
        weakMeter.setIndicatorColor(excellentMeterColor);
        mediumMeter.setIndicatorColor(excellentMeterColor);
        strongMeter.setIndicatorColor(excellentMeterColor);
        excellentMeter.setIndicatorColor(excellentMeterColor);

        worstMeter.setProgressCompat(100, true);
        weakMeter.setProgressCompat(100, true);
        mediumMeter.setProgressCompat(100, true);
        strongMeter.setProgressCompat(100, true);
        excellentMeter.setProgressCompat(100, true);
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
            baseScoreText.setText(String.valueOf(baseScore));

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
                penaltyScoreTitle.setText(getResources().getString(R.string.all_upper_penalty));
            }
            else if (lowerCaseCount == passwordStringLength)
            {
                penaltyScore = -15;
                penaltyScoreTitle.setText(getResources().getString(R.string.all_lower_penalty));
            }
            else if (numCount == passwordStringLength)
            {
                penaltyScore = -15;
                penaltyScoreTitle.setText(getResources().getString(R.string.all_num_penalty));
            }
            else if (specialCharCount == passwordStringLength)
            {
                penaltyScore = -15;
                penaltyScoreTitle.setText(getResources().getString(R.string.all_special_char_penalty));
            }
            else
            {
                penaltyScore = 0;
                penaltyScoreText.setText(zero);
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
        lengthScoreText.setText(String.valueOf(lengthScore));
        lengthCount = 0;
        lengthScore = 0;

        // UPPER CASE SCORE
        upperCaseScoreText.setText(String.valueOf(upperCaseScore));
        upperCaseCount = 0;
        upperCaseScore = 0;

        // NUMBERS SCORE
        numScoreText.setText(String.valueOf(numScore));
        numCount = 0;
        numScore = 0;

        // SPECIAL CHARACTERS SCORE
        specialCharScoreText.setText(String.valueOf(specialCharScore));
        specialCharCount = 0;
        specialCharScore = 0;

        // PENALTY SCORE
        if (penaltyScore!=0) {
            penaltyLayout.setVisibility(View.VISIBLE);
            penaltyScoreText.setText(String.valueOf(penaltyScore));
        }
        else {
            penaltyLayout.setVisibility(View.GONE);
        }
        penaltyScore = 0;
        lowerCaseCount = 0;

        // TOTAL SCORE
        totalScoreText.setText(String.valueOf(totalScore));
        totalScore=0;
    }

    // RESET DETAILS
    private void DetailsReset() {
        strengthSubtitle.setText(not_applicable);

        worstMeter.setIndicatorColor(emptyMeterColor);
        weakMeter.setIndicatorColor(emptyMeterColor);
        mediumMeter.setIndicatorColor(emptyMeterColor);
        strongMeter.setIndicatorColor(emptyMeterColor);
        excellentMeter.setIndicatorColor(emptyMeterColor);

        worstMeter.setProgressCompat(0, true);
        weakMeter.setProgressCompat(0, true);
        mediumMeter.setProgressCompat(0, true);
        strongMeter.setProgressCompat(0, true);
        excellentMeter.setProgressCompat(0, true);

        timeToCrackSubtitle.setText(not_applicable);
        warningSubtitle.setText(not_applicable);
        suggestionsSubtitle.setText(not_applicable);

    }

    // RESET SCORE
    private void ScoreReset()
    {
        totalScoreText.setText(zero);
        lengthScoreText.setText(zero);
        baseScoreText.setText(zero);
        upperCaseScoreText.setText(zero);
        numScoreText.setText(zero);
        specialCharScoreText.setText(zero);
        penaltyScoreText.setText(zero);

        penaltyLayout.setVisibility(View.GONE);
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

    // CLEAR CLIPBOARD IMMEDIATELY WHEN FRAGMENT DESTROYED
    @Override
    public void onDestroyView() {
        super.onDestroyView();

        clearClipboard();

    }
}