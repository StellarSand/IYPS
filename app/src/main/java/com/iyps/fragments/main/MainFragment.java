package com.iyps.fragments.main;

import android.os.Bundle;
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

import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class MainFragment extends Fragment {

    private TextInputEditText passwordEditText;
    private TextView strengthSubtitle, timeToCrackSubtitle, warningSubtitle, suggestionsSubtitle;
    private LinearProgressIndicator worstMeter, weakMeter, mediumMeter, strongMeter, excellentMeter;

    public MainFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        passwordEditText = view.findViewById(R.id.password_input);
        passwordEditText.addTextChangedListener(passwordTextWatcher);
        strengthSubtitle= view.findViewById(R.id.strength_subtitle);
        timeToCrackSubtitle= view.findViewById(R.id.time_to_crack_subtitle);
        warningSubtitle = view.findViewById(R.id.warning_subtitle);
        suggestionsSubtitle = view.findViewById(R.id.suggestions_subtitle);
        worstMeter= view.findViewById(R.id.worst_meter);
        weakMeter= view.findViewById(R.id.weak_meter);
        mediumMeter= view.findViewById(R.id.medium_meter);
        strongMeter= view.findViewById(R.id.strong_meter);
        excellentMeter= view.findViewById(R.id.excellent_meter);

    /*========================================================================================*/

    }

    // PASSWORD TEXT WATCHER
    private final TextWatcher passwordTextWatcher =new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            String passwordString= Objects.requireNonNull(passwordEditText.getText()).toString();
            Zxcvbn zxcvbn = new Zxcvbn();
            Strength strength = zxcvbn.measure(passwordString);


            /*List<String> sanitizedInputs = new ArrayList();
            sanitizedInputs.add("nulab");
            sanitizedInputs.add("backlog");
            sanitizedInputs.add("cacoo");
            sanitizedInputs.add("typetalk");*/

            // IF EDIT TEXT NOT EMPTY
            if (!passwordString.equals("")) {

                long crackTimeSeconds= (long) ((strength.getCrackTimeSeconds().getOfflineSlowHashing1e4perSecond())*1000);

                // STRENGTH
                // WORST
                if (crackTimeSeconds < TimeUnit.MINUTES.toMillis(2)
                    || crackTimeSeconds == TimeUnit.MINUTES.toMillis(2))
                {
                    strengthSubtitle.setText(getString(R.string.worst));
                    worstMeter.setIndicatorColor(getResources().getColor(R.color.worstMeterColor, requireActivity().getTheme()));
                    weakMeter.setIndicatorColor(getResources().getColor(R.color.hintColor, requireActivity().getTheme()));
                    mediumMeter.setIndicatorColor(getResources().getColor(R.color.hintColor, requireActivity().getTheme()));
                    strongMeter.setIndicatorColor(getResources().getColor(R.color.hintColor, requireActivity().getTheme()));
                    excellentMeter.setIndicatorColor(getResources().getColor(R.color.hintColor, requireActivity().getTheme()));
                    worstMeter.setProgress(100);
                    weakMeter.setProgress(0);
                    mediumMeter.setProgress(0);
                    strongMeter.setProgress(0);
                    excellentMeter.setProgress(0);
                }
                // WEAK
                else if (crackTimeSeconds > TimeUnit.MINUTES.toMillis(2)
                         && crackTimeSeconds < TimeUnit.DAYS.toMillis(5)
                         || crackTimeSeconds == TimeUnit.DAYS.toMillis(5))
                {
                    strengthSubtitle.setText(getString(R.string.weak));
                    worstMeter.setIndicatorColor(getResources().getColor(R.color.weakMeterColor, requireActivity().getTheme()));
                    weakMeter.setIndicatorColor(getResources().getColor(R.color.weakMeterColor, requireActivity().getTheme()));
                    mediumMeter.setIndicatorColor(getResources().getColor(R.color.hintColor, requireActivity().getTheme()));
                    strongMeter.setIndicatorColor(getResources().getColor(R.color.hintColor, requireActivity().getTheme()));
                    excellentMeter.setIndicatorColor(getResources().getColor(R.color.hintColor, requireActivity().getTheme()));
                    worstMeter.setProgress(100);
                    weakMeter.setProgress(100);
                    mediumMeter.setProgress(0);
                    strongMeter.setProgress(0);
                    excellentMeter.setProgress(0);
                }
                // MEDIUM
                else if (crackTimeSeconds > TimeUnit.DAYS.toMillis(5)
                        && crackTimeSeconds < TimeUnit.DAYS.toMillis(155)
                        || crackTimeSeconds == TimeUnit.DAYS.toMillis(155))
                {
                    strengthSubtitle.setText(getString(R.string.medium));
                    worstMeter.setIndicatorColor(getResources().getColor(R.color.mediumMeterColor, requireActivity().getTheme()));
                    weakMeter.setIndicatorColor(getResources().getColor(R.color.mediumMeterColor, requireActivity().getTheme()));
                    mediumMeter.setIndicatorColor(getResources().getColor(R.color.mediumMeterColor, requireActivity().getTheme()));
                    strongMeter.setIndicatorColor(getResources().getColor(R.color.hintColor, requireActivity().getTheme()));
                    excellentMeter.setIndicatorColor(getResources().getColor(R.color.hintColor, requireActivity().getTheme()));
                    worstMeter.setProgress(100);
                    weakMeter.setProgress(100);
                    mediumMeter.setProgress(100);
                    strongMeter.setProgress(0);
                    excellentMeter.setProgress(0);
                }
                // STRONG
                else if (crackTimeSeconds > TimeUnit.DAYS.toMillis(155)
                        && crackTimeSeconds < TimeUnit.DAYS.toMillis(1825)
                        || crackTimeSeconds == TimeUnit.DAYS.toMillis(1825))
                {
                    strengthSubtitle.setText(getString(R.string.strong));
                    worstMeter.setIndicatorColor(getResources().getColor(R.color.strongMeterColor, requireActivity().getTheme()));
                    weakMeter.setIndicatorColor(getResources().getColor(R.color.strongMeterColor, requireActivity().getTheme()));
                    mediumMeter.setIndicatorColor(getResources().getColor(R.color.strongMeterColor, requireActivity().getTheme()));
                    strongMeter.setIndicatorColor(getResources().getColor(R.color.strongMeterColor, requireActivity().getTheme()));
                    excellentMeter.setIndicatorColor(getResources().getColor(R.color.hintColor, requireActivity().getTheme()));
                    worstMeter.setProgress(100);
                    weakMeter.setProgress(100);
                    mediumMeter.setProgress(100);
                    strongMeter.setProgress(100);
                    excellentMeter.setProgress(0);
                }
                // EXCELLENT
                else if (crackTimeSeconds > TimeUnit.DAYS.toMillis(1825))
                {
                    strengthSubtitle.setText(getString(R.string.excellent));
                    worstMeter.setIndicatorColor(getResources().getColor(R.color.excellentMeterColor, requireActivity().getTheme()));
                    weakMeter.setIndicatorColor(getResources().getColor(R.color.excellentMeterColor, requireActivity().getTheme()));
                    mediumMeter.setIndicatorColor(getResources().getColor(R.color.excellentMeterColor, requireActivity().getTheme()));
                    strongMeter.setIndicatorColor(getResources().getColor(R.color.excellentMeterColor, requireActivity().getTheme()));
                    excellentMeter.setIndicatorColor(getResources().getColor(R.color.excellentMeterColor, requireActivity().getTheme()));
                    worstMeter.setProgress(100);
                    weakMeter.setProgress(100);
                    mediumMeter.setProgress(100);
                    strongMeter.setProgress(100);
                    excellentMeter.setProgress(100);
                }

                // TIME TO CRACK
                timeToCrackSubtitle.setText(strength.getCrackTimesDisplay().getOfflineSlowHashing1e4perSecond());

                // WARNING
                // IF EMPTY, SET CUSTOM WARNING MESSAGE
                if (strength.getFeedback().getWarning().isEmpty()){

                    if (strengthSubtitle.getText().equals(getString(R.string.worst)))
                    {
                        warningSubtitle.setText(getString(R.string.worst_pass_warning)); // WORST WARNING
                    }
                    else if (strengthSubtitle.getText().equals(getString(R.string.weak)))
                    {
                        warningSubtitle.setText(getString(R.string.weak_pass_warning)); // WEAK WARNING
                    }
                    else
                    {
                        warningSubtitle.setText(getString(R.string.not_applicable)); // FOR MEDIUM AND ABOVE
                    }
                }

                // IF NOT EMPTY, DISPLAY WARNING
                else {
                    warningSubtitle.setText(strength.getFeedback().getWarning());
                }

                // SUGGESTIONS

            }

            // IF EDIT TEXT IS EMPTY OR CLEARED, RESET EVERYTHING
            else
            {
                strengthSubtitle.setText(getString(R.string.worst));
                worstMeter.setIndicatorColor(getResources().getColor(R.color.worstMeterColor, requireActivity().getTheme()));
                weakMeter.setIndicatorColor(getResources().getColor(R.color.hintColor, requireActivity().getTheme()));
                mediumMeter.setIndicatorColor(getResources().getColor(R.color.hintColor, requireActivity().getTheme()));
                strongMeter.setIndicatorColor(getResources().getColor(R.color.hintColor, requireActivity().getTheme()));
                excellentMeter.setIndicatorColor(getResources().getColor(R.color.hintColor, requireActivity().getTheme()));
                worstMeter.setProgress(100);
                weakMeter.setProgress(0);
                mediumMeter.setProgress(0);
                strongMeter.setProgress(0);
                excellentMeter.setProgress(0);
                timeToCrackSubtitle.setText(getString(R.string.less_than_a_second));
                warningSubtitle.setText(getString(R.string.worst_pass_warning));
            }
        }

        @Override
        public void afterTextChanged(Editable editable) {
            //new Handler(Looper.getMainLooper()).postDelayed(this::NewReminderBottomSheet, 300);
        }
    };

}
