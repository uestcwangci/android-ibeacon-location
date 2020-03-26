package org.altbeacon.beaconreference;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;

import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;



import org.altbeacon.beacon.BeaconManager;

import java.net.InetAddress;

/**
 * 
 * @author dyoung
 * @author Matt Tyler
 */
public class MonitoringActivity extends Activity  {
	protected static final String TAG = "MonitoringActivity";
	private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;

	EditText editX;
	EditText editTimes;
	EditText editY;
	EditText scanmod;
	EditText ScanPeriod;
	EditText BetweenScanPeriod;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_monitoring);
		verifyBluetooth();
		editX = findViewById(R.id.x);
		editTimes = findViewById(R.id.times);
		editY = findViewById(R.id.y);
		ScanPeriod = findViewById(R.id.ScanPeriod);
		scanmod = findViewById(R.id.scanmod);
		BetweenScanPeriod = findViewById(R.id.BetweenScanPeriod);
		checkPermission();

	}

	private void checkPermission() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			// Android M Permission check
			String[] permissions = new String[]{
					Manifest.permission.ACCESS_COARSE_LOCATION,
					Manifest.permission.READ_EXTERNAL_STORAGE,
					Manifest.permission.WRITE_EXTERNAL_STORAGE,
					Manifest.permission.ACCESS_FINE_LOCATION,
//					Manifest.permission.ACCESS_BACKGROUND_LOCATION
			};
			// 检查是否有相应的权限
			boolean isAllGranted = checkPermissionAllGranted(permissions);
			if (!isAllGranted) {
				final AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle("This app needs location access");
				builder.setMessage("Please grant location access so this app can detect beacons in the background.");
				builder.setPositiveButton(android.R.string.ok, null);
				builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

					@TargetApi(23)
					@Override
					public void onDismiss(DialogInterface dialog) {
						requestPermissions(new String[]{
										Manifest.permission.ACCESS_COARSE_LOCATION,
										Manifest.permission.READ_EXTERNAL_STORAGE,
										Manifest.permission.WRITE_EXTERNAL_STORAGE,
										Manifest.permission.ACCESS_FINE_LOCATION},
								PERMISSION_REQUEST_COARSE_LOCATION);
					}

				});
				builder.show();
			}
		}
	}

	/**
	 * 检查是否拥有指定的所有权限
	 */
	private boolean checkPermissionAllGranted(String[] permissions) {
		for (String permission : permissions) {
			if (ContextCompat.checkSelfPermission(getApplicationContext(), permission) != PackageManager.PERMISSION_GRANTED) {
				// 只要有一个权限没有被授予, 则直接返回 false
				return false;
			}
		}
		return true;
	}

	@Override
	public void onRequestPermissionsResult(int requestCode,
										   String permissions[], int[] grantResults) {
		switch (requestCode) {
			case PERMISSION_REQUEST_COARSE_LOCATION: {
				if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					Log.d(TAG, "coarse location permission granted");
				} else {
					final AlertDialog.Builder builder = new AlertDialog.Builder(this);
					builder.setTitle("Functionality limited");
					builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons when in the background.");
					builder.setPositiveButton(android.R.string.ok, null);
					builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

						@Override
						public void onDismiss(DialogInterface dialog) {
						}

					});
					builder.show();
				}
				return;
			}
		}
	}

	public void onRangingClicked(View view) {
		Intent myIntent = new Intent(this, RangingActivity.class);
		myIntent.putExtra("x", Integer.parseInt(editX.getText().toString()));
		myIntent.putExtra("times", editTimes.getText().toString());
		myIntent.putExtra("y", Integer.parseInt(editY.getText().toString()));
//		myIntent.putExtra("period",Integer.parseInt(ScanPeriod.getText().toString()));
//		myIntent.putExtra("scanmod",Integer.parseInt(scanmod.getText().toString()));
//		myIntent.putExtra("betweenPeriod",Integer.parseInt(BetweenScanPeriod.getText().toString()));
		this.startActivity(myIntent);
	}


	public void onEnableClicked(View view) {
		BeaconReferenceApplication application = ((BeaconReferenceApplication) this.getApplicationContext());
		if (BeaconManager.getInstanceForApplication(this).getMonitoredRegions().size() > 0) {
			application.disableMonitoring();
			((Button)findViewById(R.id.enableButton)).setText("Re-Enable Monitoring");
		}
		else {
			((Button)findViewById(R.id.enableButton)).setText("Disable Monitoring");
			application.enableMonitoring();
		}

	}

	public void onMapClicked(View view) {
		Intent intent = new Intent(this, MapActivity.class);
		this.startActivity(intent);
	}

    @Override
    public void onResume() {
        super.onResume();
        BeaconReferenceApplication application = ((BeaconReferenceApplication) this.getApplicationContext());
        application.setMonitoringActivity(this);
        updateLog(application.getLog());
    }

    @Override
    public void onPause() {
        super.onPause();
        ((BeaconReferenceApplication) this.getApplicationContext()).setMonitoringActivity(null);
    }

	private void verifyBluetooth() {

		try {
			if (!BeaconManager.getInstanceForApplication(this).checkAvailability()) {
				final AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle("Bluetooth not enabled");			
				builder.setMessage("Please enable bluetooth in settings and restart this application.");
				builder.setPositiveButton(android.R.string.ok, null);
				builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
					@Override
					public void onDismiss(DialogInterface dialog) {
						//finish();
			            //System.exit(0);
					}					
				});
				builder.show();
			}			
		}
		catch (RuntimeException e) {
			final AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Bluetooth LE not available");			
			builder.setMessage("Sorry, this device does not support Bluetooth LE.");
			builder.setPositiveButton(android.R.string.ok, null);
			builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

				@Override
				public void onDismiss(DialogInterface dialog) {
					//finish();
		            //System.exit(0);
				}
				
			});
			builder.show();
			
		}
		
	}	

    public void updateLog(final String log) {
    	runOnUiThread(new Runnable() {
    	    public void run() {
    	    	EditText editText = (EditText)MonitoringActivity.this
    					.findViewById(R.id.monitoringText);
       	    	editText.setText(log);
    	    }
    	});
    }



}
