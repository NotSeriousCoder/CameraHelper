package com.bingor.camerahelper;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.hardware.camera2.CameraCharacteristics;
import android.os.Build;
import android.os.Bundle;
import android.util.Size;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;

import com.bingor.camerahelperlib.CameraActivity20;
import com.bingor.camerahelperlib.CameraActivity21;
import com.bingor.camerahelperlib.helper.CameraHelper21;
import com.bingor.utillib.system.PermissionApplier;

public class MainActivity extends AppCompatActivity {
    private TextureView surfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        surfaceView = findViewById(R.id.sv_test);
        findViewById(R.id.bt_go_pic).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!PermissionApplier.checkPermissions(MainActivity.this, 123, Manifest.permission.CAMERA)) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        CameraHelper21 helper = new CameraHelper21(MainActivity.this, null);
                        helper.setPreviewSize(new Size(1920, 1080));
                        helper.openCamera(CameraCharacteristics.LENS_FACING_BACK, ImageFormat.JPEG);
                    } else {
                        startActivity(new Intent(MainActivity.this, CameraActivity20.class));
                    }
                }
            }
        });
    }
}
