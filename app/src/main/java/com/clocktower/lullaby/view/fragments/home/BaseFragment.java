package com.clocktower.lullaby.view.fragments.home;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.clocktower.lullaby.interfaces.FragmentListener;

public abstract class BaseFragment extends Fragment {

    protected FragmentListener listener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        listener = (FragmentListener)context;
    }
}
