package com.rpapps.termux_launcher;

import android.Manifest;
import android.os.Bundle;
import android.os.Environment;

import android.app.Activity;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends Activity implements EasyPermissions.PermissionCallbacks{
    public static final String LOG_TAG = "termux-applist";

    public static final String EXTERNAL_PATH_NAME = "termux_launcher";

    public static final String EXTERNAL_ALIAS_FILE_NAME = ".apps-list";

    /**
     * termux intent
     */
    private static Intent myIntent;

    /**
     * Public external storage path name
     */
    private static File sExternalPath = new File(Environment.getExternalStorageDirectory() + File.separator + EXTERNAL_PATH_NAME);

    /**
     * Alias file
     **/
    private static File sAliasFile;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkPermission();


        createAliasFile();
        createTermuxIntent();
//        startActivity(myIntent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> list) {
        // Some permissions have been granted
        // ...
        createTermuxIntent();
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {

    }

    @Override
    public void onResume() {
        super.onResume();
        createTermuxIntent();
//        startActivity(myIntent);
    }


    @Override
    public void onPause() {
        super.onPause();
        createTermuxIntent();
//        startActivity(myIntent);
    }


    private void createTermuxIntent() {
        if(checkPermission()){

            if (null == myIntent) {
                myIntent = getPackageManager().getLaunchIntentForPackage("com.termux");

            }
            startActivity(myIntent);
        }
    }

    public boolean checkPermission() {
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            Log.d(LOG_TAG, "onCreate: PERMISSION NOT GRANTED");
            EasyPermissions.requestPermissions(this, "Need write permission for creating app list", 1, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            return false;
//            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
//                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
//                // Show an explanation to the user *asynchronously* -- don't block
//                // this thread waiting for the user's response! After the user
//                // sees the explanation, try again to request the permission.
//            } else {
//                // No explanation needed; request the permission
//                ActivityCompat.requestPermissions(MainActivity.this,
//                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
//                        1);
//
//                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
//                // app-defined int constant. The callback method gets the
//                // result of the request.
//            }

        } else {

            Log.d(LOG_TAG, "onCreate: PERMISSION GRANTED");
            return true;
        }

    }

    private static void createExternalPath() {
        if (!sExternalPath.exists()) {

//            Log.d(LOG_TAG, "WRITE PERMISSION : "  + checkWriteExternalPermission());
            Log.d(LOG_TAG, "Start Creating DIR : " + sExternalPath.getAbsolutePath());
            boolean result = sExternalPath.mkdir();
            Log.d(LOG_TAG, "createExternalPath: CAN WRITE" + sExternalPath.canWrite());

            Log.d(LOG_TAG, "createExternalPath: DIRECTORY CREATED" + result);
        }
    }


    private void createAliasFile() {
        sAliasFile = new File(sExternalPath, EXTERNAL_ALIAS_FILE_NAME);


        new Thread() {
            public void run() {

                try {
                    // Always up to date
                    if (sAliasFile.exists()) {
                        Log.d(LOG_TAG, "File DELETED ");
                        sAliasFile.delete();
                    }

                    createExternalPath();
                    Log.d(LOG_TAG, "Start writing to file");
                    final FileOutputStream fos = new FileOutputStream(sAliasFile);
                    final PrintStream printer = new PrintStream(fos);
                    final PackageManager pm = getApplicationContext().getPackageManager();
                    List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
                    Map<String, String> appNames = new HashMap<>();
                    for (ApplicationInfo pkg : packages) {
                        String pkgName = pkg.packageName;
                        String appName = pkg.loadLabel(pm).toString();
                        Intent intent = pm.getLaunchIntentForPackage(pkgName);
//                        boolean isSystemApp = (pkg.flags & ApplicationInfo.FLAG_SYSTEM) == 1;
//                        Log.d(LOG_TAG, "[" + intent + "] : [" + pkgName + "] : [" + isSystemApp + "] : [" + "] : [" + appName + "]");
                        if (intent == null) {
                            continue;
                        }
                        String componentName = intent.getComponent().flattenToShortString();
                        appName = appName.replace("'", "");
                        appName = appName.replace("\"", "");
                        appName = appName.replace("(", "");
                        appName = appName.replace(")", "");
                        appName = appName.replace("&", "");
                        appName = appName.replace("{", "");
                        appName = appName.replace("}", "");
                        appName = appName.replace("$", "");
                        appName = appName.replace("!", "");
                        appName = appName.replace("<", "");
                        appName = appName.replace(">", "");
                        appName = appName.replace("#", "");
                        appName = appName.replace("+", "");
                        appName = appName.replace("*", "");
//                        appName = appName.replace(" ", "-");

                        appNames.put(appName.toLowerCase(), componentName);
                    }
                    appNames = sortHashMap(appNames);
                    ArrayList<String> apps = new ArrayList<>(appNames.keySet());
                    for (String app : apps) {
                        printer.println(app + "|" + appNames.get(app));
//                        printer.print( appName + "|" + LaunchComponent + "|" + packageName + "|" + isSystemApp + "\n");
                    }
                    printer.flush();
                    printer.close();
                    fos.flush();
                    fos.close();
                } catch (IOException ioe) {
                    Log.e(LOG_TAG, "Could not write to " + sAliasFile.toString() + ioe);
                }

            }

        }.start();
    }

    public Map<String, String> sortHashMap(Map<String, String> appPackageName) {
        ArrayList<String> sortedKeys = new ArrayList<>(appPackageName.keySet());
        Collections.sort(sortedKeys);
        LinkedHashMap<String, String> sortedHashMap = new LinkedHashMap<>();
        for (String key : sortedKeys) {
//            Log.d(LOG_TAG, "sortHashMap:  " + key);
            sortedHashMap.put(key, appPackageName.get(key));
        }
        for (String k: sortedHashMap.keySet()){

            Log.d(LOG_TAG, "sortHashMap:  " + k);
        }
        return sortedHashMap;
    }
}