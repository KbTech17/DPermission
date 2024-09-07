package com.dp_th.dpermission.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public abstract class RationaleDialog extends Dialog {

    public RationaleDialog(@NonNull Context context) {
        super(context);
    }

    public RationaleDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    protected RationaleDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    abstract public @NonNull View getPositiveButton();

    abstract public @Nullable View getNegativeButton();

    abstract public @NonNull List<String> getPermissionsToRequest();

}