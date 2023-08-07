package io.github.muntashirakon.AppManager.sdk;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.FileUtils;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.FragmentActivity;

import com.google.android.material.button.MaterialButton;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import io.github.muntashirakon.amsdk.AppManagerSdk;

public class MainActivity extends FragmentActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MaterialButton testAppInfo = findViewById(R.id.test_app_info);
        MaterialButton testScanner = findViewById(R.id.test_scanner);
        MaterialButton testManifestViewer = findViewById(R.id.test_manifest_viewer);
        MaterialButton testInstaller = findViewById(R.id.test_installer);

        ApplicationInfo applicationInfo = getApplicationInfo();
        File apkFile = new File(applicationInfo.publicSourceDir);
        File apkFileTarget = new File(getFilesDir(), apkFile.getName());
        try {
            Files.copy(apkFile.toPath(), getFilesDir().toPath().resolve(apkFile.getName()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Uri apkUri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider", apkFileTarget);
        testAppInfo.setOnClickListener(v -> {
            Intent intent = AppManagerSdk.getAppInfoIntent(apkUri, "application/vnd.android.package-archive");
            if (intent != null) {
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(intent);
            } else Toast.makeText(this, "Cannot open App Details page", Toast.LENGTH_SHORT).show();
        });
        testScanner.setOnClickListener(v -> {
            Intent intent = AppManagerSdk.getScannerIntent(apkUri, "application/vnd.android.package-archive");
            if (intent != null) {
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(intent);
            } else Toast.makeText(this, "Cannot open Scanner page", Toast.LENGTH_SHORT).show();
        });
        testManifestViewer.setOnClickListener(v -> {
            Intent intent = AppManagerSdk.getManifestIntent(apkUri, "application/vnd.android.package-archive");
            if (intent != null) {
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(intent);
            } else Toast.makeText(this, "Cannot open Manifest Viewer page", Toast.LENGTH_SHORT).show();
        });
        testInstaller.setOnClickListener(v -> {
            Intent intent = AppManagerSdk.getInstallerIntent(apkUri, "application/vnd.android.package-archive");
            if (intent != null) {
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(intent);
            } else Toast.makeText(this, "Cannot open Installer page", Toast.LENGTH_SHORT).show();
        });
    }
}
