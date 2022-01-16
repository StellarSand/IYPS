package com.iyps.fragments.main;

import android.content.res.Resources;
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

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class MainFragment extends Fragment {

    private TextInputEditText passwordEditText;
    private TextView strengthSubtitle, timeToCrackSubtitle, warningSubtitle, suggestionsSubtitle;
    private LinearProgressIndicator worstMeter, weakMeter, mediumMeter, strongMeter, excellentMeter;
    private int worstMeterColor, weakMeterColor, mediumMeterColor, strongMeterColor, excellentMeterColor, hintColor;
    private Zxcvbn zxcvbn;

    public MainFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Resources.Theme theme = requireActivity().getTheme();

        worstMeterColor = getColor(R.color.worstMeterColor, theme);
        weakMeterColor = getColor(R.color.weakMeterColor, theme);
        mediumMeterColor = getColor(R.color.mediumMeterColor, theme);
        strongMeterColor = getColor(R.color.strongMeterColor, theme);
        excellentMeterColor = getColor(R.color.excellentMeterColor, theme);
        hintColor = getColor(R.color.hintColor, theme);

        zxcvbn = new Zxcvbn();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        setHasOptionsMenu(true);

        Zxcvbn zxcvbn = new Zxcvbn();
        Strength strength = zxcvbn.measure("This is password");

        double d = strength.getCrackTimeSeconds().getOfflineFastHashing1e10PerSecond();

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

            // IF EDIT TEXT NOT EMPTY
            if (!passwordString.equals("")) {

                Strength strength = zxcvbn.measure(passwordString);

                long crackTimeSeconds= (long) ((strength.getCrackTimeSeconds().getOfflineSlowHashing1e4perSecond())*1000);

                PasswordType result = setPasswordType(crackTimeSeconds);

                switch (result){
                    case WORST:
                        setWorst();
                        break;

                    case WEAK:
                        setWeak();
                        break;

                    case MEDIUM:
                        setMedium();
                        break;

                    case STRONG:
                        setStrong();
                        break;

                    case EXCELLENT:
                        setExcellent();
                        break;

                }


                // TIME TO CRACK
                timeToCrackSubtitle.setText(strength.getCrackTimesDisplay().getOfflineSlowHashing1e4perSecond());

                // WARNING
                // IF EMPTY, SET CUSTOM WARNING MESSAGE
                if (strength.getFeedback().getWarning().isEmpty()){

                    if (result == PasswordType.WORST )
                    {
                        warningSubtitle.setText(getString(R.string.worst_pass_warning)); // WORST WARNING
                    }
                    else if (result == PasswordType.WEAK)
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
                List<String> suggestions = strength.getFeedback().getSuggestions();

                if(suggestions != null && suggestions.size() != 0){
                    suggestionsSubtitle.setText(suggestions.get(suggestions.size()-1));
                }
                else{
                    suggestionsSubtitle.setText(R.string.not_available);
                }

            }

            // IF EDIT TEXT IS EMPTY OR CLEARED, RESET EVERYTHING
            else
                setEmpty();
        }

        @Override
        public void afterTextChanged(Editable editable) {
            //new Handler(Looper.getMainLooper()).postDelayed(this::NewReminderBottomSheet, 300);
        }
    };

    private int getColor(int color, Resources.Theme theme){
        return  getResources().getColor(color, theme);
    }

    private void setWorst(){
        strengthSubtitle.setText(getString(R.string.worst));
        worstMeter.setIndicatorColor(worstMeterColor);
        weakMeter.setIndicatorColor(hintColor);
        mediumMeter.setIndicatorColor(hintColor);
        strongMeter.setIndicatorColor(hintColor);
        excellentMeter.setIndicatorColor(hintColor);
        worstMeter.setProgress(100);
        weakMeter.setProgress(0);
        mediumMeter.setProgress(0);
        strongMeter.setProgress(0);
        excellentMeter.setProgress(0);
    }

    private void setWeak(){
        strengthSubtitle.setText(getString(R.string.weak));
        worstMeter.setIndicatorColor(weakMeterColor);
        weakMeter.setIndicatorColor(weakMeterColor);
        mediumMeter.setIndicatorColor(hintColor);
        strongMeter.setIndicatorColor(hintColor);
        excellentMeter.setIndicatorColor(hintColor);
        worstMeter.setProgress(100);
        weakMeter.setProgress(100);
        mediumMeter.setProgress(0);
        strongMeter.setProgress(0);
        excellentMeter.setProgress(0);
    }

    private void setMedium(){
        strengthSubtitle.setText(getString(R.string.medium));
        worstMeter.setIndicatorColor(mediumMeterColor);
        weakMeter.setIndicatorColor(mediumMeterColor);
        mediumMeter.setIndicatorColor(mediumMeterColor);
        strongMeter.setIndicatorColor(hintColor);
        excellentMeter.setIndicatorColor(hintColor);
        worstMeter.setProgress(100);
        weakMeter.setProgress(100);
        mediumMeter.setProgress(100);
        strongMeter.setProgress(0);
        excellentMeter.setProgress(0);
    }

    private void setStrong(){
        strengthSubtitle.setText(getString(R.string.strong));
        worstMeter.setIndicatorColor(strongMeterColor);
        weakMeter.setIndicatorColor(strongMeterColor);
        mediumMeter.setIndicatorColor(strongMeterColor);
        strongMeter.setIndicatorColor(strongMeterColor);
        excellentMeter.setIndicatorColor(hintColor);
        worstMeter.setProgress(100);
        weakMeter.setProgress(100);
        mediumMeter.setProgress(100);
        strongMeter.setProgress(100);
        excellentMeter.setProgress(0);
    }

    private void setExcellent(){
        strengthSubtitle.setText(getString(R.string.excellent));
        worstMeter.setIndicatorColor(excellentMeterColor);
        weakMeter.setIndicatorColor(excellentMeterColor);
        mediumMeter.setIndicatorColor(excellentMeterColor);
        strongMeter.setIndicatorColor(excellentMeterColor);
        excellentMeter.setIndicatorColor(excellentMeterColor);
        worstMeter.setProgress(100);
        weakMeter.setProgress(100);
        mediumMeter.setProgress(100);
        strongMeter.setProgress(100);
        excellentMeter.setProgress(100);
    }

    private void setEmpty(){
        strengthSubtitle.setText(getString(R.string.worst));
        worstMeter.setIndicatorColor(worstMeterColor);
        weakMeter.setIndicatorColor(hintColor);
        mediumMeter.setIndicatorColor(hintColor);
        strongMeter.setIndicatorColor(hintColor);
        excellentMeter.setIndicatorColor(hintColor);
        worstMeter.setProgress(100);
        weakMeter.setProgress(0);
        mediumMeter.setProgress(0);
        strongMeter.setProgress(0);
        excellentMeter.setProgress(0);
        timeToCrackSubtitle.setText(getString(R.string.less_than_a_second));
        warningSubtitle.setText(getString(R.string.worst_pass_warning));
    }

    private PasswordType setPasswordType(long crackTimeSeconds){

        PasswordType result = PasswordType.WORST;

        if(crackTimeSeconds < TimeUnit.MINUTES.toMillis(2) || crackTimeSeconds == TimeUnit.MINUTES.toMillis(2))
            result = PasswordType.WORST;

        else if(crackTimeSeconds > TimeUnit.MINUTES.toMillis(2)
                && crackTimeSeconds < TimeUnit.DAYS.toMillis(5)
                || crackTimeSeconds == TimeUnit.DAYS.toMillis(5))
            result = PasswordType.WEAK;

        else if(crackTimeSeconds > TimeUnit.DAYS.toMillis(5)
                && crackTimeSeconds < TimeUnit.DAYS.toMillis(155)
                || crackTimeSeconds == TimeUnit.DAYS.toMillis(155))
            result = PasswordType.MEDIUM;

        else if(crackTimeSeconds > TimeUnit.DAYS.toMillis(155)
                && crackTimeSeconds < TimeUnit.DAYS.toMillis(1825)
                || crackTimeSeconds == TimeUnit.DAYS.toMillis(1825))
            result = PasswordType.STRONG;

        else if(crackTimeSeconds > TimeUnit.DAYS.toMillis(1825))
            result = PasswordType.EXCELLENT;

        return result;
    }

    enum PasswordType{
        WORST,
        WEAK,
        MEDIUM,
        STRONG,
        EXCELLENT
    }

}
