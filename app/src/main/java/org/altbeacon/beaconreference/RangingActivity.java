package org.altbeacon.beaconreference;

import java.io.File;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import android.app.Activity;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.RemoteException;
import android.util.Log;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;


import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

public class RangingActivity extends Activity implements BeaconConsumer {
    protected static final String TAG = "RangingActivity";
    private BeaconManager beaconManager = BeaconManager.getInstanceForApplication(this);
    private int x;
    private int time;
    private int y;
    private SQLiteDatabase db;
//    private long startTime;
    private int count;
    private int scanmod;
    private int period;
    private int betweenPriod;
    private Map<String, Integer> uuid4rssi_WeNeed = new HashMap<>();
    private boolean flag = false;
    //private Map<String, Integer> uuid4rssi;
//    private Map<String, Double> uuid4dis;
//    private Map<String, String> uuid4name;
    private Ringtone mRingtone;

    private Set<String> uuids;

    private Region mRegion;

    private void initMap() {
        for (String uuid : uuids) {
            //uuid4rssi.put(uuid,-100);
            uuid4rssi_WeNeed.put(uuid,-100);
//            uuid4dis.put(uuid, 0.0);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ranging);
        //不息屏
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        Intent intent = getIntent();
        if (intent != null) {
            x = intent.getIntExtra("x", 0);
            time = Integer.parseInt(intent.getStringExtra("times"));
            y = intent.getIntExtra("y", 0);
            period = intent.getIntExtra("period",150);
            betweenPriod = intent.getIntExtra("betweenPeriod",0);
            scanmod = intent.getIntExtra("scanmod",1);
        }
        mRegion = new Region("myRangingUniqueId", null, null, null);
        if (mRingtone == null) {
            Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            mRingtone = RingtoneManager.getRingtone(getApplicationContext(), uri);
        }
//        uuid4name = new TreeMap<>();

        //uuid4rssi = new HashMap<>();
//        uuid4dis = new HashMap<>();
        uuids = new TreeSet<>(); // 存储顺序与字段大小有关

//        uuid4name.put("13fda506-93a4-e24f-b1af-cfc6eb076401", "ble1");
//        uuid4name.put("13fda506-93a4-e24f-b1af-cfc6eb076402", "ble2");
//        uuid4name.put("13fda506-93a4-e24f-b1af-cfc6eb076404", "ble4");
        // TODO UUID
        //uuids.add("13fda506-93a4-e24f-b1af-cfc6eb076401");
        //uuids.add("13fda506-93a4-e24f-b1af-cfc6eb076402");
        //uuids.add("13fda506-93a4-e24f-b1af-cfc6eb076404");
        if (scanmod == 0) {
            uuids.add("e2c56db5-dffb-48d2-b060-d0f5a7109601");
            uuids.add("e2c56db5-dffb-48d2-b060-d0f5a7109602");
            uuids.add("e2c56db5-dffb-48d2-b060-d0f5a7109603");
        }
        if (scanmod == 1) {
            uuids.add("fda50693-a4e2-4fb1-afcf-c6eb07647821");
            uuids.add("fda50693-a4e2-4fb1-afcf-c6eb07647822");
            uuids.add("fda50693-a4e2-4fb1-afcf-c6eb07647823");
            uuids.add("fda50693-a4e2-4fb1-afcf-c6eb07647824");
            uuids.add("fda50693-a4e2-4fb1-afcf-c6eb07647825");
            uuids.add("fda50693-a4e2-4fb1-afcf-c6eb07647826");
            uuids.add("fda50693-a4e2-4fb1-afcf-c6eb07647827");
            uuids.add("fda50693-a4e2-4fb1-afcf-c6eb07647828");
            uuids.add("fda50693-a4e2-4fb1-afcf-c6eb07647829");
        }if (scanmod == 2) {
            uuids.add("fda50693-a4e2-4fb1-afcf-c6eb07640016");
            uuids.add("fda50693-a4e2-4fb1-afcf-c6eb07640024");
            uuids.add("fda50693-a4e2-4fb1-afcf-c6eb07640026");
            uuids.add("fda50693-a4e2-4fb1-afcf-c6eb07640031");
            uuids.add("fda50693-a4e2-4fb1-afcf-c6eb07640011");
            uuids.add("fda50693-a4e2-4fb1-afcf-c6eb07640046");
        }

        Log.e("uuids",uuids.toString());
//        beaconManager.setBackgroundScanPeriod(period);
//        beaconManager.setBackgroundBetweenScanPeriod(betweenPriod);
//        beaconManager.setForegroundBetweenScanPeriod(betweenPriod);
//        beaconManager.setForegroundScanPeriod(period);
//        try {
//            beaconManager.updateScanPeriods();
//        } catch (RemoteException e) {
//            e.printStackTrace();
//        }
//        delete(Environment.getExternalStorageDirectory() + "/bleInfo.db");
        db = SQLiteDatabase.openOrCreateDatabase(Environment.getExternalStorageDirectory() + "/bleInfo.db", null);
        createTable(db);
        count = 0;
    }

    private boolean delete(String path){
        File file = new File(path);
        if (!file.exists())
            return false;
        else{
            return file.delete();
        }
    }

    @Override 
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override 
    protected void onPause() {
        super.onPause();
        beaconManager.unbind(this);
    }

    @Override 
    protected void onResume() {
        super.onResume();
        beaconManager.bind(this);
//        startTime = System.currentTimeMillis();
    }



    @Override
    public void onBeaconServiceConnect() {
        RangeNotifier rangeNotifier = new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
//                long endsTime = System.currentTimeMillis();
                if (++count > time) {
                    try {
                        beaconManager.stopRangingBeaconsInRegion(mRegion);
                        beaconManager.removeAllRangeNotifiers();
                        Toast.makeText(RangingActivity.this, "Done!", Toast.LENGTH_SHORT).show();
                        mRingtone.play();
                        db.close();
                        count = 0;
                        finish();
//                    mRingtone = null;
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }

                } else {
                    Log.e("count", String.valueOf(count));
                    initMap();
                    if (beacons.size() > 0) {
                        for (Beacon beacon : beacons) {
                            // TODO filter beacons
                            Log.d(TAG, "rssi:" + beacon.getRssi());
                            String uuid = beacon.getId1().toString();
                            int rssi = beacon.getRssi();
//                       Log.d(TAG, "rssi Ave :" + beacon.getRunningAverageRssi());
//                       uuid4rssi.put(beacon.getId1().toString(), beacon.getRssi());
//                       uuid4dis.put(beacon.getId1().toString(), beacon.getDistance());
                            Log.e("uuid", uuid);
                            if (uuids.contains(uuid)) {
                                uuid4rssi_WeNeed.put(uuid, rssi);
                            }
                        }
                        Beacon firstBeacon = beacons.iterator().next();
                        logToDisplay("The first beacon " + firstBeacon.toString() + " is about " + firstBeacon.getDistance() + " meters away.");
                    }
                    Log.e("data",uuid4rssi_WeNeed.toString());
                    insertDB(x, y);
//                  Log.d(TAG, "didRangeBeaconsInRegion called with beacon count:  "+beacons.size());
                    //Beacon firstBeacon = beacons.iterator().next();
                    //logToDisplay("The first beacon " + firstBeacon.toString() + " is about " + firstBeacon.getDistance() + " meters away.");
                }
            }
        };


        try {
            beaconManager.startRangingBeaconsInRegion(mRegion);
            beaconManager.addRangeNotifier(rangeNotifier);
//            beaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
//            beaconManager.addRangeNotifier(rangeNotifier);
        } catch (RemoteException e) {   }
    }



    private void logToDisplay(final String line) {
        runOnUiThread(new Runnable() {
            public void run() {
                EditText editText = (EditText)RangingActivity.this.findViewById(R.id.rangingText);
                editText.append(line+"\n");
            }
        });
    }

    private void createTable(SQLiteDatabase db) {
        StringBuilder APBuilder = new StringBuilder();
        for (String uuid : uuids) {
            APBuilder.append("\"").append(uuid).append("\"");
            //APBuilder.append(uuid);
            APBuilder.append(" smallint,");
        }
        APBuilder.deleteCharAt(APBuilder.length() - 1);
        String str = APBuilder.toString();
        //创建表SQL语句
        String stu_table = "create table if not exists ble_table (id INTEGER primary key autoincrement not null," +
                "x smallint not null," +
                "y smallint not null," +
                str +
                ",date text)";
        Log.d("stu_table", stu_table);
        //执行SQL语句
        db.execSQL(stu_table);
    }

    private void insertDB(int x, int y) {
        //获取日期
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE) + 1;
        int second = calendar.get(Calendar.SECOND);
        int milliSecond = calendar.get(Calendar.MILLISECOND);
        String dateStr = hour +
                ":" + minute +
                ":" + second +
                " " + milliSecond;
        ContentValues cValue = new ContentValues();
        cValue.put("x", x);
        cValue.put("y", y);
        for (Map.Entry<String, Integer> entry : uuid4rssi_WeNeed.entrySet()) {
            cValue.put("\""+entry.getKey()+"\"", entry.getValue());
        }
        cValue.put("date", dateStr);
        db.insert("ble_table", null, cValue);

        cValue.clear();

        //实例化常量值

        // 添加数据
//        cValue.put("name", name);
//        cValue.put("uuid", uuid);

//        cValue.put("rssi", rssi);
//        cValue.put("dis", dis);

        //添加日期
//        cValue.put("date", dateStr);
        //调用insert()方法插入数据
    }
}
