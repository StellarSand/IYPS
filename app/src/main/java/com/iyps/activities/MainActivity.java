package com.iyps.activities;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.iyps.R;
import com.iyps.fragments.main.MainFragment;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FragmentTransaction transaction=getSupportFragmentManager().beginTransaction();
        transaction
                .replace(R.id.activity_host_fragment, new MainFragment())
                .commitNow();
    }
}
