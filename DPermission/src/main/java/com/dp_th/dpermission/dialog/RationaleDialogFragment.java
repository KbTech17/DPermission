package com.dp_th.dpermission.dialog;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.util.List;

public abstract class RationaleDialogFragment extends DialogFragment {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            dismiss();
        }
    }

    abstract public @NonNull View getPositiveButton();

    abstract public @Nullable View getNegativeButton();

    abstract public @NonNull List<String> getPermissionsToRequest();

}