package com.iyps.fragments.main;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.iyps.R;
import com.nulabinc.zxcvbn.Strength;
import com.nulabinc.zxcvbn.Zxcvbn;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class MainFragment extends Fragment {

    private TextInputEditText passwordEditText;
    private TextView strengthSubtitle, timeToCrackSubtitle, warningSubtitle, suggestionsSubtitle;
    private LinearProgressIndicator worstMeter, weakMeter, mediumMeter, strongMeter, excellentMeter;
    private static int worstMeterColor, weakMeterColor, mediumMeterColor, strongMeterColor, excellentMeterColor, emptyMeterColor;
    private StringBuilder suggestionText;

    private Zxcvbn zxcvbn;

    private String worst, weak, medium, strong, excellent, not_applicable, passwordString;

    public MainFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        suggestionText = new StringBuilder();

        zxcvbn = new Zxcvbn();

        worst = getString(R.string.worst);
        weak = getString(R.string.weak);
        medium = getString(R.string.medium);
        strong = getString(R.string.strong);
        excellent = getString(R.string.excellent);
        not_applicable =getString(R.string.not_applicable);

        // Inflate the layout for this fragment
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
        worstMeterColor = getColor(R.color.worstMeterColor);
        weakMeterColor = getColor(R.color.weakMeterColor);
        mediumMeterColor = getColor(R.color.mediumMeterColor);
        strongMeterColor = getColor(R.color.strongMeterColor);
        excellentMeterColor = getColor(R.color.excellentMeterColor);
        emptyMeterColor = getColor(R.color.hintColor);



        passwordEditText.addTextChangedListener(passwordTextWatcher);

    }

    // PASSWORD TEXT WATCHER
    private final TextWatcher passwordTextWatcher =new TextWatcher() {

        CountDownTimer delayTimer = null;

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            // INTRODUCE A SUBTLE DELAY,
            // SO PASSWORDS ARE CHECKED AFTER TYPING IS FINISHED
            if (delayTimer != null) {
                delayTimer.cancel();
            }

            delayTimer = new CountDownTimer(300, 100) {

                public void onTick(long millisUntilFinished) {}

                // ON TIMER FINISH, PERFORM ACTION
                public void onFinish() {

                    passwordString= Objects.requireNonNull(passwordEditText.getText()).toString();

                    // IF EDIT TEXT NOT EMPTY
                    if (!passwordString.equals("")) {

                        Strength strength = zxcvbn.measure(passwordString);

                        long crackTimeMilliSeconds= (long) ((strength
                                .getCrackTimeSeconds()
                                .getOfflineSlowHashing1e4perSecond())*1000);

                        switch (passwordCrackTimeResult(crackTimeMilliSeconds)){

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

                        // TIME TO CRACK
                        timeToCrackSubtitle.setText(strength.getCrackTimesDisplay().getOfflineSlowHashing1e4perSecond());

                        // WARNING
                        // IF EMPTY, SET CUSTOM WARNING MESSAGE
                        if (strength.getFeedback().getWarning().isEmpty()){

                            switch (passwordCrackTimeResult(crackTimeMilliSeconds)){
                                case "WORST":
                                    warningSubtitle.setText(getString(R.string.worst_pass_warning)); // WORST WARNING
                                    break;

                                case "WEAK":
                                    warningSubtitle.setText(getString(R.string.weak_pass_warning)); // WEAK WARNING
                                    break;

                                case "MEDIUM":
                                    warningSubtitle.setText(getString(R.string.medium_pass_warning)); // MEDIUM WARNING
                                    break;

                                default:
                                    warningSubtitle.setText(not_applicable); // FOR STRONG AND ABOVE
                                    break;
                            }
                        }

                        // IF NOT EMPTY, DISPLAY WARNING
                        else {
                            warningSubtitle.setText(strength.getFeedback().getWarning());
                        }

                        // SUGGESTIONS
                        List<String> suggestions = strength.getFeedback().getSuggestions();

                        if(suggestions != null && suggestions.size() != 0){
                            suggestionText.setLength(0);
                            for (int i = 0; i < suggestions.size(); i++) {

                                suggestionText = new StringBuilder(suggestionText + "\u2022 " + suggestions.get(i) + "\n");
                            }

                            suggestionsSubtitle.setText(suggestionText.toString());
                        }
                        else{
                            suggestionsSubtitle.setText(not_applicable);
                        }

                    }

                    // IF EDIT TEXT IS EMPTY OR CLEARED, RESET EVERYTHING
                    else{
                        Reset();
                    }

                }

            }.start();

        }

        @Override
        public void afterTextChanged(Editable editable) {}

    };

    private int getColor(int color){
        return  getResources().getColor(color, requireActivity().getTheme());
    }

    // WORST STRENGTH METER
    private void WorstStrengthMeter()
    {
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
    private void WeakStrengthMeter()
    {
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
    private void MediumStrengthMeter()
    {
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
    private void StrongStrengthMeter()
    {
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
    private void ExcellentStrengthMeter()
    {
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

    // RESET WHEN EMPTY EDIT TEXT
    private void Reset(){
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

    // CHECK PASSWORD CRACK TIME CUSTOM RESULT
    private String passwordCrackTimeResult(long crackTimeMilliSeconds)
    {
        String result="";

        // TAKE DAYS IN:
        // MONTH=31, YEAR=365

        // WORST = LESS THAN/EQUAL TO 2 MINUTES
        if(crackTimeMilliSeconds < TimeUnit.MINUTES.toMillis(2)
           || crackTimeMilliSeconds == TimeUnit.MINUTES.toMillis(2))
        {
            result="WORST";
        }

        // WEAK = MORE THAN 2 MINUTES, LESS THAN/EQUAL TO 5 DAYS
        else if(crackTimeMilliSeconds > TimeUnit.MINUTES.toMillis(2)
                && crackTimeMilliSeconds < TimeUnit.DAYS.toMillis(5)
                || crackTimeMilliSeconds == TimeUnit.DAYS.toMillis(5))
        {
            result="WEAK";
        }

        // MEDIUM = MORE THAN 5 DAYS, LESS THAN/EQUAL TO 5 MONTHS
        else if(crackTimeMilliSeconds > TimeUnit.DAYS.toMillis(5)
                && crackTimeMilliSeconds < TimeUnit.DAYS.toMillis(155)
                || crackTimeMilliSeconds == TimeUnit.DAYS.toMillis(155))
        {
            result="MEDIUM";
        }

        // STRONG = MORE THAN 5 MONTHS, LESS THAN/EQUAL TO 5 YEARS
        else if(crackTimeMilliSeconds > TimeUnit.DAYS.toMillis(155)
                && crackTimeMilliSeconds < TimeUnit.DAYS.toMillis(1825)
                || crackTimeMilliSeconds == TimeUnit.DAYS.toMillis(1825))
        {
            result="STRONG";
        }

        // EXCELLENT = MORE THAN 5 YEARS
        else if(crackTimeMilliSeconds > TimeUnit.DAYS.toMillis(1825))
        {
            result="EXCELLENT";
        }

        return result;

    }
}