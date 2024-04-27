package com.lukkass.simplebluetoothmanagerle.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;

import java.util.HashSet;
import java.util.Set;

public class Permissions {

    public static final int  REQUEST_CODE = 111;
    public static String[] PERMISSIONS;
    static {
        if (android.os.Build.VERSION.SDK_INT > 30) {

            PERMISSIONS = new String[]{

                    Manifest.permission.BLUETOOTH_CONNECT
                    , Manifest.permission.BLUETOOTH_SCAN
            };
        } else {
            PERMISSIONS = new String[]{
                    Manifest.permission.BLUETOOTH_ADMIN
                    , Manifest.permission.BLUETOOTH
                    , Manifest.permission.ACCESS_FINE_LOCATION
                    , Manifest.permission.ACCESS_COARSE_LOCATION
            };
        }
    }
    
   /** solicita as permiçoes que não foram aceitas */
    public static boolean checkPermisionStatus(Activity _activity) {
        //     return _activity.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
        
        //lista de permiçoes que precisam ser aceitas
        Set<String> permission_list = new HashSet<>();
        boolean PERMISSIONS_GRANTED = true;

        for (String permission : PERMISSIONS) {
            if (!checkSelfPermission(_activity, permission)) {
                permission_list.add(permission);
                //PERMISSION_DENIED
                PERMISSIONS_GRANTED = false;
            }
        }

        if (!PERMISSIONS_GRANTED)
            requestPermission(_activity, permission_list.toArray(new String[0]));

        return PERMISSIONS_GRANTED;
    }

    /**
     * verifica se a permiçao foi ou não aceita
     */
    public static boolean checkSelfPermission(Context _context, String permission) {
        return ActivityCompat.checkSelfPermission(_context, permission) == PackageManager.PERMISSION_GRANTED;
    }
    
    /**
     * solicita permição ao usuario
     * @TODO CONVERÇÃO DE CONTEXTO EM ACTIVITY
     */
    public static void requestPermission(Activity _activity, String[] permission) {
        ActivityCompat.requestPermissions(_activity, permission, REQUEST_CODE);

    //    if (!ActivityCompat.shouldShowRequestPermissionRationale(_activity, permission))
     //       Toast.makeText(_activity, _activity.getResources().getText(R.string.aviso_perm_negada), Toast.LENGTH_SHORT).show();
    }
    
  /*  
    


    final String[] PERMISSIONS = {
            
      //      Manifest.permission.BLUETOOTH_CONNECT,
        //    Manifest.permission.BLUETOOTH_SCAN,
            
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.BLUETOOTH,

            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    private ActivityResultContracts.RequestMultiplePermissions multiplePermissionsContract;
    private ActivityResultLauncher<String[]> multiplePermissionLauncher;


    protected void onCreate(Context savedInstanceState) {

   

        multiplePermissionsContract = new ActivityResultContracts.RequestMultiplePermissions();
        multiplePermissionLauncher = savedInstanceState.registerForActivityResult(multiplePermissionsContract, isGranted -> {
            Log.d("PERMISSIONS", "Launcher result: " + isGranted.toString());
            if (isGranted.containsValue(false)) {
                Log.d("PERMISSIONS", "At least one of the permissions was not granted, launching again...");
                multiplePermissionLauncher.launch(PERMISSIONS);
            }
        });

        askPermissions(multiplePermissionLauncher);
    }

    private void askPermissions(ActivityResultLauncher<String[]> multiplePermissionLauncher) {
        if (!hasPermissions(PERMISSIONS)) {
            Log.d("PERMISSIONS", "Launching multiple contract permission launcher for ALL required permissions");
            multiplePermissionLauncher.launch(PERMISSIONS);
        } else {
            Log.d("PERMISSIONS", "All permissions are already granted");
        }
    }

    private boolean hasPermissions(String[] permissions) {
        if (permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    Log.d("PERMISSIONS", "Permission is not granted: " + permission);
                    return false;
                }
                Log.d("PERMISSIONS", "Permission already granted: " + permission);
            }
            return true;
        }
        return false;
    }


   */
}