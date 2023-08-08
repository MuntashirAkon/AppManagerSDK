package io.github.muntashirakon.amsdk.internal;

import androidx.annotation.RestrictTo;

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
public final class Constants {
    public static final String PKG_PREFIX = "io.github.muntashirakon.AppManager";
    public static final String RELEASE_APP_ID = PKG_PREFIX;
    public static final String DEBUG_APP_ID = PKG_PREFIX + ".debug";
    public static final String ACTIVITY_PACKAGE_INSTALLER_POSTFIX = ".PackageInstallerActivity";
    public static final String ACTIVITY_APP_INFO_POSTFIX = ".AppInfoActivity";
    public static final String ACTIVITY_APP_DETAILS_POSTFIX = ".AppDetailsActivity";
    public static final String ACTIVITY_SCANNER_POSTFIX = ".ScannerActivity";
    public static final String ACTIVITY_MANIFEST_VIEWER_POSTFIX = ".ManifestViewerActivity";
}
