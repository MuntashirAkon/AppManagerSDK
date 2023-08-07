package io.github.muntashirakon.amsdk;

import static io.github.muntashirakon.amsdk.internal.Constants.ACTIVITY_APP_DETAILS_POSTFIX;
import static io.github.muntashirakon.amsdk.internal.Constants.ACTIVITY_APP_INFO_POSTFIX;
import static io.github.muntashirakon.amsdk.internal.Constants.ACTIVITY_MANIFEST_VIEWER_POSTFIX;
import static io.github.muntashirakon.amsdk.internal.Constants.ACTIVITY_PACKAGE_INSTALLER_POSTFIX;
import static io.github.muntashirakon.amsdk.internal.Constants.ACTIVITY_SCANNER_POSTFIX;
import static io.github.muntashirakon.amsdk.internal.Constants.DEBUG_APP_ID;
import static io.github.muntashirakon.amsdk.internal.Constants.RELEASE_APP_ID;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class AppManagerSdk {
    public static class Config {
        public final boolean useDebug;
        public final boolean debugFirst;

        private Config(boolean useDebug, boolean debugFirst) {
            this.useDebug = useDebug;
            this.debugFirst = debugFirst;
        }

        public static class Builder {
            private boolean mUseDebug = false;
            private boolean mDebugFirst = false;

            /**
             * Use <b>AM Debug</b> releases alongside the production releases. Not recommended in a production
             * environment.
             *
             * @param useDebug Whether to use debug releases. Default value is {@code false}.
             * @see #setDebugFirst(boolean)
             */
            public void setUseDebug(boolean useDebug) {
                mUseDebug = useDebug;
            }

            /**
             * Prefer <b>AM Debug</b> releases over the production releases if both are installed.
             * <p>
             * <b>Note:</b> This does nothing if debug versions are disabled (which is the default) by
             * {@link #setUseDebug(boolean)}.
             *
             * @param debugFirst Whether to prefer debug releases over the production releases.
             */
            public void setDebugFirst(boolean debugFirst) {
                mDebugFirst = debugFirst;
            }

            /**
             * Build the SDK configurations to be initialized in {@link Application#onCreate()}
             */
            public Config build() {
                return new Config(mUseDebug, mDebugFirst);
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    private static AppManagerSdk instance;

    /**
     * Initialize App Manager SDK. Must be called from {@link Application#onCreate()}.
     */
    public static void init(@NonNull Context context, @NonNull Config config) {
        Objects.requireNonNull(context);
        Objects.requireNonNull(config);
        instance = new AppManagerSdk(context, config);
    }

    /**
     * Initialize App Manager SDK. Must be called from {@link Application#onCreate()}.
     */
    public static void init(@NonNull Context context) {
        Objects.requireNonNull(context);
        init(context, new Config.Builder().build());
    }

    /**
     * Get selected app ID based on the configurations and availability of the packages.
     *
     * @return Selected app ID (or package name) on success
     */
    @Nullable
    public static String getSelectedAppId() {
        if (instance.mPackageInfo == null) return null;
        return instance.mPackageInfo.packageName;
    }

    /**
     * Check if App Details page can be opened from this app.
     *
     * @return {@code true} if the page can be opened.
     */
    public static boolean isAppInfoEnabled() {
        ActivityInfo[] activityInfos = instance.getActivities();
        if (activityInfos == null) return false;
        for (ActivityInfo info : activityInfos) {
            if (info.exported && (info.name.endsWith(ACTIVITY_APP_INFO_POSTFIX) || info.name.endsWith(ACTIVITY_APP_DETAILS_POSTFIX))) {
                return instance.isEnabled(new ComponentName(info.packageName, info.name), true);
            }
        }
        return false;
    }

    /**
     * Check if Scanner page can be opened from this app.
     *
     * @return {@code true} if the page can be opened.
     */
    public static boolean isScannerEnabled() {
        return instance.isEnabled(ACTIVITY_SCANNER_POSTFIX);
    }

    /**
     * Check if Manifest Viewer page can be opened from this app.
     *
     * @return {@code true} if the page can be opened.
     */
    public static boolean isManifestViewerEnabled() {
        return instance.isEnabled(ACTIVITY_MANIFEST_VIEWER_POSTFIX);
    }

    /**
     * Check if Installer page can be opened from this app.
     *
     * @return {@code true} if the page can be opened.
     */
    public static boolean isInstallerEnabled() {
        return instance.isEnabled(ACTIVITY_PACKAGE_INSTALLER_POSTFIX);
    }

    /**
     * Resolve and get an {@link Intent} to open the App Details page.
     *
     * @param uri  The file to open
     * @param type Type of the file. Could be one of {@code application/vnd.android.package-archive},
     *             {@code application/vnd.apkm}, {@code application/xapk-package-archive},
     *             {@code application/octet-stream}.
     * @return Return the resolved intent if available, {@code null} otherwise.
     */
    @Nullable
    public static Intent getAppInfoIntent(@NonNull Uri uri, @Nullable String type) {
        Objects.requireNonNull(uri);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        if (type != null) {
            intent.setDataAndType(uri, type);
        } else intent.setData(uri);
        List<ResolveInfo> resolveInfoList = instance.getMatchingActivities(intent);
        for (ResolveInfo resolveInfo : resolveInfoList) {
            ActivityInfo activityInfo = resolveInfo.activityInfo;
            if (activityInfo.name.endsWith(ACTIVITY_APP_INFO_POSTFIX)
                    || activityInfo.name.endsWith(ACTIVITY_APP_DETAILS_POSTFIX)) {
                ComponentName componentName = new ComponentName(activityInfo.packageName, activityInfo.name);
                return intent.setComponent(componentName);
            }
        }
        return null;
    }

    /**
     * Resolve and get an {@link Intent} to open the Scanner page.
     *
     * @param uri  The APK file to open
     * @param type Type of the file. Could be {@code application/vnd.android.package-archive}
     * @return Return the resolved intent if available, {@code null} otherwise.
     */
    @Nullable
    public static Intent getScannerIntent(@NonNull Uri uri, @Nullable String type) {
        Objects.requireNonNull(uri);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        if (type != null) {
            intent.setDataAndType(uri, type);
        } else intent.setData(uri);
        return instance.getMatchingIntent(intent, ACTIVITY_SCANNER_POSTFIX);
    }

    /**
     * Resolve and get an {@link Intent} to open the Manifest Viewer page.
     *
     * @param uri  The APK file to open
     * @param type Type of the file. Could be {@code application/vnd.android.package-archive}
     * @return Return the resolved intent if available, {@code null} otherwise.
     */
    @Nullable
    public static Intent getManifestIntent(@NonNull Uri uri, @Nullable String type) {
        Objects.requireNonNull(uri);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        if (type != null) {
            intent.setDataAndType(uri, type);
        } else intent.setData(uri);
        return instance.getMatchingIntent(intent, ACTIVITY_MANIFEST_VIEWER_POSTFIX);
    }

    /**
     * Resolve and get an {@link Intent} to open the Installer page. After launching the Intent, you should check
     * whether the app was installed by listening to the broadcast actions {@link Intent#ACTION_PACKAGE_ADDED}
     * and {@link Intent#ACTION_PACKAGE_REPLACED}.
     *
     * @param uri  The file to open
     * @param type Type of the file. Could be one of {@code application/vnd.android.package-archive},
     *             {@code application/vnd.apkm}, {@code application/xapk-package-archive},
     *             {@code application/octet-stream}.
     * @return Return the resolved intent if available, {@code null} otherwise.
     */
    @Nullable
    public static Intent getInstallerIntent(@NonNull Uri uri, @Nullable String type) {
        Objects.requireNonNull(uri);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        if (type != null) {
            intent.setDataAndType(uri, type);
        } else intent.setData(uri);
        return instance.getMatchingIntent(intent, ACTIVITY_PACKAGE_INSTALLER_POSTFIX);
    }

    private final Context mContext;
    private final Config mConfig;

    @Nullable
    private PackageInfo mPackageInfo;

    private AppManagerSdk(@NonNull Context context, @NonNull Config config) {
        mContext = context.getApplicationContext();
        mConfig = config;
        mPackageInfo = getPreferredPackageInfo(mContext);
    }

    @Nullable
    private PackageInfo getPreferredPackageInfo(@NonNull Context context) {
        int flags = PackageManager.GET_ACTIVITIES;
        PackageManager pm = context.getPackageManager();
        if (!mConfig.useDebug) {
            try {
                return pm.getPackageInfo(RELEASE_APP_ID, flags);
            } catch (PackageManager.NameNotFoundException ignored) {
                return null;
            }
        }
        try {
            return pm.getPackageInfo(mConfig.debugFirst ? DEBUG_APP_ID : RELEASE_APP_ID, flags);
        } catch (PackageManager.NameNotFoundException ignored) {
        }
        try {
            return pm.getPackageInfo(mConfig.debugFirst ? RELEASE_APP_ID : DEBUG_APP_ID, flags);
        } catch (PackageManager.NameNotFoundException ignored) {
        }
        return null;
    }

    @Nullable
    private ActivityInfo[] getActivities() {
        return mPackageInfo != null ? mPackageInfo.activities : null;
    }

    @NonNull
    private List<ResolveInfo> getMatchingActivities(@NonNull Intent intent) {
        if (mPackageInfo == null) {
            return Collections.emptyList();
        }
        intent.setPackage(mPackageInfo.packageName);
        PackageManager pm = mContext.getPackageManager();
        List<ResolveInfo> resolveInfoList = pm.queryIntentActivities(intent, 0);
        return resolveInfoList;
    }

    @Nullable
    private Intent getMatchingIntent(@NonNull Intent intent, @NonNull String activityPostfix) {
        List<ResolveInfo> resolveInfoList = getMatchingActivities(intent);
        for (ResolveInfo resolveInfo : resolveInfoList) {
            ActivityInfo activityInfo = resolveInfo.activityInfo;
            if (activityInfo.name.endsWith(activityPostfix)) {
                ComponentName componentName = new ComponentName(activityInfo.packageName, activityInfo.name);
                return intent.setComponent(componentName);
            }
        }
        // Unavailable
        return null;
    }

    private boolean isEnabled(@NonNull String postfix) {
        ActivityInfo[] activityInfos = getActivities();
        if (activityInfos == null) return false;
        for (ActivityInfo info : activityInfos) {
            if (info.exported && info.name.endsWith(postfix)) {
                return isEnabled(new ComponentName(info.packageName, info.name), true);
            }
        }
        return false;
    }

    private boolean isEnabled(@NonNull ComponentName cn, boolean defaultValue) {
        PackageManager pm = mContext.getPackageManager();
        switch (pm.getApplicationEnabledSetting(cn.getPackageName())) {
            case PackageManager.COMPONENT_ENABLED_STATE_DEFAULT:
            case PackageManager.COMPONENT_ENABLED_STATE_ENABLED:
            default:
                break;
            case PackageManager.COMPONENT_ENABLED_STATE_DISABLED:
            case PackageManager.COMPONENT_ENABLED_STATE_DISABLED_UNTIL_USED:
            case PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER:
                return false;
        }
        switch (pm.getComponentEnabledSetting(cn)) {
            case PackageManager.COMPONENT_ENABLED_STATE_DEFAULT:
            default:
                return defaultValue;
            case PackageManager.COMPONENT_ENABLED_STATE_ENABLED:
                return true;
            case PackageManager.COMPONENT_ENABLED_STATE_DISABLED:
            case PackageManager.COMPONENT_ENABLED_STATE_DISABLED_UNTIL_USED:
            case PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER:
                return false;
        }
    }
}
