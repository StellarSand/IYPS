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

        // Version
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

        // Authors
        fragmentBinding.authors
                .setOnClickListener(v ->
                        AuthorsBottomSheet());

        // Contributors
        fragmentBinding.contributors
                .setOnClickListener(v ->
                        OpenURL(requireActivity(), "https://github.com/the-weird-aquarian/IYPS/blob/master/CONTRIBUTORS.md"));

        // Privacy policy
        fragmentBinding.privacyPolicy
                .setOnClickListener(v ->
                        OpenURL(requireActivity(), "https://github.com/the-weird-aquarian/IYPS/blob/master/PRIVACY.md"));

        // Licenses
        fragmentBinding.licenses
                .setOnClickListener(v ->
                        OpenURL(requireActivity(), "https://github.com/the-weird-aquarian/IYPS/blob/master/LICENSE"));

        // View on GitHub
        fragmentBinding.viewOnGit
                .setOnClickListener(v ->
                        OpenURL(requireActivity(), "https://github.com/the-weird-aquarian/IYPS"));

    }

    private void AuthorsBottomSheet(){
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext());
        bottomSheetDialog.setCancelable(true);

        BottomSheetAuthorsBinding bottomSheetBinding;
        BottomSheetHeaderBinding headerBinding;
        bottomSheetBinding = BottomSheetAuthorsBinding.inflate(getLayoutInflater());
        headerBinding = BottomSheetHeaderBinding.bind(bottomSheetBinding.getRoot());
        bottomSheetDialog.setContentView(bottomSheetBinding.getRoot());

        // Title
        headerBinding.bottomSheetTitle.setText(getString(R.string.authors));

        // Author 1
        bottomSheetBinding.author1.setOnClickListener(v -> {
            OpenURL(requireActivity(), "https://github.com/the-weird-aquarian");
            bottomSheetDialog.dismiss();
        });

        // Author 2
        bottomSheetBinding.author2.setOnClickListener(v -> {
            OpenURL(requireActivity(), "https://github.com/parveshnarwal");
            bottomSheetDialog.dismiss();
        });

        // Cancel
        bottomSheetBinding.cancelButton.setOnClickListener(view12 ->
                bottomSheetDialog.cancel());

        bottomSheetDialog.show();
    }

    // Open links
    public static void OpenURL(Activity activityFrom, String URL) {

        try
        {
            activityFrom.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(URL)));
        }
        // If browsers not installed, show toast
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
