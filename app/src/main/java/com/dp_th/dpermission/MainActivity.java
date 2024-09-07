package com.dp_th.dpermission;

import static android.Manifest.permission.CALL_PHONE;
import static android.Manifest.permission.POST_NOTIFICATIONS;
import static android.Manifest.permission_group.CAMERA;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


import com.dp_th.dpermission.callback.RequestReasonCallback;
import com.dp_th.dpermission.callback.ManualSettingCallback;
import com.dp_th.dpermission.callback.OnPermissionCallback;

import java.util.List;

import com.dp_th.dpermission.request.ReasonToAsk;
import com.dp_th.dpermission.request.ManualScope;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btnAsk).setOnClickListener(view -> askPerm1());
        findViewById(R.id.btnAsk2).setOnClickListener(view -> askPerm2());
        findViewById(R.id.btnAsk3).setOnClickListener(view -> askPerm3());
        findViewById(R.id.btnAsk4).setOnClickListener(view -> askPerm4());
    }

    private void askPerm1() {
        DPermission.with(this)
                .permissions(POST_NOTIFICATIONS, CAMERA, CALL_PHONE)
                .request(new OnPermissionCallback() {
                    @Override
                    public void onPermissionResult(boolean isGranted, @NonNull List<String> grantedList, @NonNull List<String> deniedList) {
                        if (isGranted) {
                            Toast.makeText(MainActivity.this, "All permissions are granted", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(MainActivity.this, "These permissions are denied", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void askPerm2() {
        DPermission.with(this)
                .permissions(POST_NOTIFICATIONS, CAMERA, CALL_PHONE)
                .onReasonToRequest(new RequestReasonCallback() {
                    @Override
                    public void onExplainReason(@NonNull ReasonToAsk reason, @NonNull List<String> deniedList) {
                        reason.showReasonDialog(deniedList, "These permissions are required!", "OK", "Cancel");
                    }
                })
                .request(new OnPermissionCallback() {
                    @Override
                    public void onPermissionResult(boolean isGranted, @NonNull List<String> grantedList, @NonNull List<String> deniedList) {
                        if (isGranted) {
                            Toast.makeText(MainActivity.this, "All permissions are granted", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(MainActivity.this, "These permissions are denied", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void askPerm3() {
        DPermission.with(this)
                .permissions(POST_NOTIFICATIONS, CAMERA, CALL_PHONE)
                .onReasonToRequest(new RequestReasonCallback() {
                    @Override
                    public void onExplainReason(@NonNull ReasonToAsk reason, @NonNull List<String> deniedList) {
                        reason.showReasonDialog(deniedList, "These permissions are required!", "OK", "Cancel");
                    }
                }).onManualSettings(new ManualSettingCallback() {
                    @Override
                    public void onManualSettings(@NonNull ManualScope manual, @NonNull List<String> deniedList) {
                        manual.showManualSettingDialog(deniedList, "You need to allow necessary permissions in Settings manually", "OK", "Cancel");
                    }
                }).request(new OnPermissionCallback() {
                    @Override
                    public void onPermissionResult(boolean isGranted, @NonNull List<String> grantedList, @NonNull List<String> deniedList) {
                        if (isGranted) {
                            Toast.makeText(MainActivity.this, "All permissions are granted", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(MainActivity.this, "These permissions are denied", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }


    private void askPerm4() {
        DPermission.with(this)
                .permissions(POST_NOTIFICATIONS, CAMERA, CALL_PHONE)
                .explainBeforeRequest()
//        ..................YOUR_CODE......................
        ;
    }

}