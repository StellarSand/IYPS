package com.iyps.fragments.main;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.iyps.R;
import com.iyps.databinding.BottomSheetAuthorsBinding;
import com.iyps.databinding.BottomSheetHeaderBinding;
import com.iyps.databinding.FragmentAboutBinding;

import java.util.Objects;

public class AboutFragment extends Fragment {

    private static String version;
    private FragmentAboutBinding fragmentBinding;

    public AboutFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        setHasOptionsMenu(true);
        fragmentBinding = FragmentAboutBinding.inflate(inflater, container, false);
        return fragmentBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        // VERSION
        try {
            version = getResources().getString(R.string.app_version)
                          + " "
                          + requireContext().getPackageManager()
                                             .getPackageInfo(requireContext()
                                             .getPackageName(), 0)
                                             .versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        fragmentBinding.version.setText(version);

        // AUTHORS
        fragmentBinding.authors
                .setOnClickListener(v ->
                        AuthorsBottomSheet());

        // CONTRIBUTORS
        fragmentBinding.contributors
                .setOnClickListener(v ->
                        OpenURL(requireActivity(), "https://github.com/the-weird-aquarian/IYPS/blob/master/CONTRIBUTORS.md"));

        // PRIVACY POLICY
        fragmentBinding.privacyPolicy
                .setOnClickListener(v ->
                        OpenURL(requireActivity(), "https://github.com/the-weird-aquarian/IYPS/blob/master/PRIVACY.md"));

        // LICENSES
        fragmentBinding.licenses
                .setOnClickListener(v ->
                        OpenURL(requireActivity(), "https://github.com/the-weird-aquarian/IYPS/blob/master/LICENSE"));

        // VIEW ON GITHUB
        fragmentBinding.viewOnGit
                .setOnClickListener(v ->
                        OpenURL(requireActivity(), "https://github.com/the-weird-aquarian/IYPS"));

    }

    // AUTHORS BOTTOM SHEET
    private void AuthorsBottomSheet(){
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext(), R.style.CustomBottomSheetTheme);
        bottomSheetDialog.setCancelable(true);

        BottomSheetAuthorsBinding bottomSheetBinding;
        BottomSheetHeaderBinding headerBinding;
        bottomSheetBinding = BottomSheetAuthorsBinding.inflate(getLayoutInflater());
        headerBinding = BottomSheetHeaderBinding.bind(bottomSheetBinding.getRoot());
        bottomSheetDialog.setContentView(bottomSheetBinding.getRoot());

        // TITLE
        headerBinding.bottomSheetTitle.setText(getString(R.string.authors));

        // AUTHOR 1
        bottomSheetBinding.author1.setOnClickListener(v -> {
            OpenURL(requireActivity(), "https://github.com/the-weird-aquarian");
            bottomSheetDialog.dismiss();
        });

        // AUTHOR 2
        bottomSheetBinding.author2.setOnClickListener(v -> {
            OpenURL(requireActivity(), "https://github.com/parveshnarwal");
            bottomSheetDialog.dismiss();
        });

        // SHOW BOTTOM SHEET WITH CUSTOM ANIMATION
        Objects.requireNonNull(bottomSheetDialog.getWindow()).getAttributes().windowAnimations = R.style.BottomSheetAnimation;
        bottomSheetDialog.show();
    }

    // OPEN LINKS
    public static void OpenURL(Activity activityFrom, String URL) {

        try
        {
            activityFrom.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(URL)));
        }
        // IF BROWSERS NOT INSTALLED, SHOW TOAST
        catch (ActivityNotFoundException e)
        {
            Toast.makeText(activityFrom, activityFrom.getResources().getString(R.string.no_browsers), Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        fragmentBinding = null;
    }


}
