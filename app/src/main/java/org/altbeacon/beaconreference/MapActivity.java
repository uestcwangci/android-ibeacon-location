package org.altbeacon.beaconreference;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.iBeaconData;
import org.altbeacon.utils.DBManager;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;

public class MapActivity extends Activity implements BeaconConsumer {
    protected static final String TAG = "MapActivity";
    private BeaconManager beaconManager = BeaconManager.getInstanceForApplication(this);

//    private long startTime;
    private int scanmod;
    private int period;
    private int betweenPriod;

    //private Map<String, Integer> uuid4rssi;
//    private Map<String, Double> uuid4dis;
//    private Map<String, String> uuid4name;
    private Ringtone mRingtone;

    private Set<String> uuids;

    private Region mRegion;
    private RangeNotifier rangeNotifier;

    private static final int FIRST_SCAN = 44871;
    private static final int START_SCAN = 276;
    private static final int STOP_SCAN = 722;
    private static final double XMAX = 9.2;
    private static final double YMAX = 7.8;
    private static final double FIX_LOC = 0.6;
    private int A = 3;// 取前A个有效AP
    private int K = 3;// 取前K个最近距离
    private static final int L = 5;// 窗口长度，在线定位L个点合作一个点
    private static final double RSSI_WEIGHT = 0.5;


    private Map<double[], Map<String, Double>> fingerMap;
    private Map<String, LinkedList<Integer>> uuid4rssi_scanAll;

    private int flag = 0;
    private int countL = 0; // 用于记录当前扫描成功的组数
    private boolean isScanSucc = false;
    private double[] curLoc, lastLoc;
    private int iconH, iconW;
    private double perH, perW;
    private double perX, perY;
    private String checkWhich = "";

    TextView info;
    ImageView locIcon;
    Bitmap bitmap;
    EditText editX, editY;
    Switch kalman;
    ToggleButton positionTogBtn;
    LinearLayout kbMap;


    private void initOnlineMap() {
        uuid4rssi_scanAll = new HashMap<>();
        for (String uuid : uuids) {
            uuid4rssi_scanAll.put(uuid, new LinkedList<Integer>());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map343);
        //不息屏
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        Intent intent = getIntent();
        if (intent != null) {
            period = intent.getIntExtra("period",100);
            betweenPriod = intent.getIntExtra("betweenPeriod",100);
            scanmod = intent.getIntExtra("scanmod",1);
        }
        initUI();
        initParams();
        initBeacon();
    }

    private void initParams() {
        uuids = new TreeSet<>(); // 存储顺序与字段大小有关

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
        initOnlineMap();

    }

    private long lastDate = 0;
    private long nowDate = 0;

    private void initBeacon() {
        mRegion = new Region("myRangingUniqueId", null, null, null);

//        beaconManager.setBackgroundScanPeriod(period);
//        beaconManager.setBackgroundBetweenScanPeriod(betweenPriod);
//        beaconManager.setForegroundBetweenScanPeriod(betweenPriod);
//        beaconManager.setForegroundScanPeriod(period);
//
//        try {
//            beaconManager.updateScanPeriods();
//        } catch (RemoteException e) {
//            e.printStackTrace();
//        }
//        deleteDB(Environment.getExternalStorageDirectory() + "/bleInfo.db");

        rangeNotifier = new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                if (beacons.size() > 0) {
                    if (countL < L) {
                        for (Beacon beacon : beacons) {
                            String uuid = beacon.getId1().toString();
                            int rssi = beacon.getRssi();

                            if (uuids.contains(uuid)) {
                                isScanSucc = true;
                                uuid4rssi_scanAll.get(uuid).add(rssi);
                            }
                        }
                        if (isScanSucc) {countL++;}
                    } else {
                        countL = 0;
                        Message msg = new Message();
                        msg.what = flag;
                        handler.sendMessage(msg);
                        //获取当前时间
                        lastDate = nowDate;
                        nowDate = System.currentTimeMillis();
                        if (lastDate != 0) {
//                            Toast.makeText(MapActivity.this, String.format("%d", nowDate - lastDate), Toast.LENGTH_SHORT).show();
                        }
                    }
//                    Beacon firstBeacon = beacons.iterator().next();
//                    logToDisplay("The first beacon " + firstBeacon.toString() + " is about " + firstBeacon.getDistance() + " meters away.");
                }
                else {
//                    Toast.makeText(MapActivity.this, "没有扫描到iBeacon", Toast.LENGTH_SHORT).show();
                }
//                mRingtone.play();
            }
        };
    }


    private void initUI() {
        ButtonListener buttonListener = new ButtonListener();
        Button init = findViewById(R.id.init_bt);
        Button testBt = findViewById(R.id.test_bt);
        init.setOnClickListener(buttonListener);
        testBt.setOnClickListener(buttonListener);

        locIcon = findViewById(R.id.loc_icon);
        positionTogBtn = findViewById(R.id.wknn);
        kalman = findViewById(R.id.kalman);
        info = findViewById(R.id.textInfo);
        editX = findViewById(R.id.x);
        editY = findViewById(R.id.y);

        kbMap = findViewById(R.id.kbmap);
        // 获取屏幕宽高,转换图与屏幕大小
        DisplayMetrics dm = getResources().getDisplayMetrics();
        int screenWidth = dm.widthPixels;
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) kbMap.getLayoutParams();
        layoutParams.width = screenWidth;
        bitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.keb343);
        int height = bitmap.getHeight();
        int width = bitmap.getWidth();
        width -= locIcon.getWidth();
        float scale = (float) screenWidth / width;
        layoutParams.height = (int) (height * scale);
        kbMap.setLayoutParams(layoutParams);


        // 设置start监听事件
        positionTogBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                try {
                    if (isChecked) {
                        initOnlineMap();
                        beaconManager.startRangingBeaconsInRegion(mRegion);
                        beaconManager.addRangeNotifier(rangeNotifier);
                        if (TextUtils.isEmpty(editX.getText()) || TextUtils.isEmpty(editY.getText())) {
                            // 没有赋予初始点
                            flag = FIRST_SCAN;
                        } else {
                            // 初始点已经赋值
                            curLoc = new double[]{Double.parseDouble(editX.getText().toString()),
                                    Double.parseDouble(editY.getText().toString())};
                            flag = START_SCAN;
                        }
                    } else {
                        flag = STOP_SCAN;
                        beaconManager.stopRangingBeaconsInRegion(mRegion);
                        beaconManager.removeAllRangeNotifiers();
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });

        // 响铃初始化
        if (mRingtone == null) {
            Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            mRingtone = RingtoneManager.getRingtone(getApplicationContext(), uri);
        }
    }

    class ButtonListener implements View.OnClickListener{

        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.init_bt) {
                initSys();
            } else if (v.getId() == R.id.init_bt) {
                TranslateAnimation animation = new TranslateAnimation(
                        (float) 0, (float) (4.6 * perW - iconW / 2), (float) 0, (float) (1.0 * perH - iconH / 2));
                animation.setDuration(100); // 移动时间
                animation.setFillAfter(true);// 移动后停留
                locIcon.startAnimation(animation);
            }
        }
    }

    private void initSys() {
        //获取地图及图表宽高,只有在onCreate执行后才行
        int mapH = kbMap.getHeight();
        int mapW = kbMap.getWidth();
        iconH = locIcon.getHeight();
        iconW = locIcon.getWidth();
        perH = mapH / YMAX;
        perW = mapW / XMAX;
        readFingerDatabase();
        initOnlineMap();
        Toast.makeText(MapActivity.this, "初始化成功", Toast.LENGTH_SHORT).show();
    }


    private void readFingerDatabase() {
        DBManager dbManager = new DBManager(this);
        SQLiteDatabase sqLiteDatabase = dbManager.manage("esp_buy_map.db");
        fingerMap = dbManager.queryFinger(sqLiteDatabase, "new_343", uuids.toArray(new String[0]), null, null);
        sqLiteDatabase.close();
    }


    private boolean deleteDB(String path){
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
//        initSys();
//        startTime = System.currentTimeMillis();
    }

    @Override
    public void onBeaconServiceConnect() {
//        try {
//            beaconManager.startRangingBeaconsInRegion(mRegion);
//            beaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
//            beaconManager.addRangeNotifier(rangeNotifier);
//        } catch (RemoteException e) {   }
    }



    private void logToDisplay(final String line) {
        runOnUiThread(new Runnable() {
            public void run() {
                EditText editText = (EditText) MapActivity.this.findViewById(R.id.rangingText);
                editText.append(line+"\n");
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private double[] Awknn() {
        StringBuilder sb = new StringBuilder();
        // 对窗口得到的rssi滤波处理，并只留下信号最强的前A个的信息
        // uuid4rssi是LinkedHashMap，根据rssi升序排序
        Map<String, Double> uuid4rssi_WeNeed = filterRssi();
        uuid4rssi_WeNeed = sortMap(uuid4rssi_WeNeed);
        Map<String, Double> Aweight = funAweight(uuid4rssi_WeNeed);
        double[] curLoc = new double[2];

        for (Map.Entry<String, Double> entry :
                uuid4rssi_WeNeed.entrySet()) {
            sb.append(String.format("%s: %2.2f  ", entry.getKey().substring(34), entry.getValue()));
        }

        Map<double[], Double> disMap = new HashMap<>();
        // 只对前A个进行运算
        for (Map.Entry<double[], Map<String, Double>>finger : fingerMap.entrySet()) {
            double disNum = 0.0;
            for (Map.Entry<String, Double> entry:
                 uuid4rssi_WeNeed.entrySet()) {
                String uuid = entry.getKey();
                disNum += Aweight.get(uuid) * Math.abs(entry.getValue() - finger.getValue().get(uuid));
            }
            disMap.put(finger.getKey(), disNum);
        }

        Map<double[], Double> sortDis = sortMap2(disMap, K);
        double disSum = 0.0;
        for (Map.Entry<double[], Double> entry :
                sortDis.entrySet()) {
            sb.append(String.format("%s: %2.2f  ", Arrays.toString(entry.getKey()), entry.getValue()));
            disSum += 1.0 / entry.getValue();
        }
        checkWhich = sb.toString();
        for (Map.Entry<double[], Double> entry :
                sortDis.entrySet()) {
            curLoc[0] += (1.0 * entry.getKey()[0] / entry.getValue()) / disSum;
            curLoc[1] += (1.0 * entry.getKey()[1] / entry.getValue()) / disSum;
        }
        return curLoc;
    }

    // 根据窗口长度对rssi滤波
    private Map<String, Double> filterRssi() {
        // 暂时只做均值处理
        Map<String, Double> result = new HashMap<>(uuid4rssi_scanAll.size());
        for (Map.Entry<String, LinkedList<Integer>> entry :
                uuid4rssi_scanAll.entrySet()) {
            if (entry.getValue().size() == 0) {
                continue;
            }
            int sum = 0;
            int count = 0;
            for (Integer rssi : entry.getValue()) {
                sum += rssi;
                count++;
            }
            result.put(entry.getKey(),(1.0 * sum) / count);
        }

        return result;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private Map<String, Double> sortMap(Map<String, Double> tempMap) {
        Comparator<Map.Entry<String, Double>> cmp = new Comparator<Map.Entry<String, Double>>() {
            public int compare(Map.Entry<String, Double> a, Map.Entry<String, Double> b) {
                double temp = Math.abs(a.getValue()) - Math.abs(b.getValue());
                if (temp < 0) {
                    return -1;
                } else if (temp > 0) {
                    return 1;
                } else {
                    return 0;
                }
            }
        };
        Queue<Map.Entry<String, Double>> priQ = new PriorityQueue<>(cmp);
        priQ.addAll(tempMap.entrySet());
        Map<String, Double> result = new LinkedHashMap<>(uuid4rssi_scanAll.size());

        A = Math.min(priQ.size(), A);
        int a = 0;
        while (!priQ.isEmpty() && a++ < A) {
            Map.Entry<String, Double> entry = priQ.poll();
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private Map<double[], Double> sortMap2(Map<double[], Double> tempMap, int num) {
        Comparator<Map.Entry<double[], Double>> cmp = new Comparator<Map.Entry<double[], Double>>() {
            public int compare(Map.Entry<double[], Double> a, Map.Entry<double[], Double> b) {
                double temp = Math.abs(a.getValue()) - Math.abs(b.getValue());
                if (temp < 0) {
                    return -1;
                } else if (temp > 0) {
                    return 1;
                } else {
                    return 0;
                }
            }
        };
        Queue<Map.Entry<double[], Double>> priQ = new PriorityQueue<>(cmp);
        priQ.addAll(tempMap.entrySet());
        num = Math.min(priQ.size(), num);
        Map<double[], Double> result = new LinkedHashMap<>(num);

        int n = 0;
        while (!priQ.isEmpty() && n++ < num) {
            Map.Entry<double[], Double> entry = priQ.poll();
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    private Map<String, Double> funAweight(Map<String, Double> uuid4rssi) {
        double sumWeight = 0.0;
        Map<String, Double> result = new HashMap<>();
        for (Map.Entry<String, Double> entry:
                uuid4rssi.entrySet()){
            sumWeight += Math.abs(1.0 / entry.getValue());
        }
        for (Map.Entry<String, Double> entry :
                uuid4rssi.entrySet()) {
            result.put(entry.getKey(), (1.0 / Math.abs(entry.getValue())) / sumWeight);
        }
        return result;
    }


    private double[] kalNextP = new double[]{1, 1};//X、Y方向后验估计的方差

    private void kalmanFilter(double[] last, double[] cur) {
        double R = 0.0002;// R测量方差，反应当前的测量精度
        double Q = 0.004;// Q过程方差，反应连续两个时刻数据方差
        double K;// 卡尔曼增益，反应了测量结果与过程模型（即当前时刻与下一时刻位置相同这一模型）的可信程度
        for (int i = 0; i < cur.length; i++) {
            double currentX = cur[i];// currentX是当前值，和真实值的存在一定高斯噪声误差
            double hatminus;// 位置的先验估计。即在k-1时刻，对k时刻位置做出的估计
            double currentP;// 先验估计的方差
            // 初始化
            hatminus = last[i];
            // 滤波
            currentP = kalNextP[i] + Q;
            K = currentP / (currentP + R);
            cur[i] = hatminus + K * (currentX - hatminus);
            kalNextP[i] = (1 - K) * currentP;
        }
    }

    private void MoveIcon(double[] last,double[] cur) {
        TranslateAnimation animation = new TranslateAnimation(
                (float) (last[0] * perW - iconW / 2), (float) (cur[0] * perW - iconW / 2),
                (float) (last[1] * perH - iconH / 2), (float) (cur[1] * perH - iconH / 2));
        animation.setDuration(beaconManager.getBackgroundScanPeriod() * L); // 移动时间
        animation.setFillAfter(true);// 移动后停留
        locIcon.startAnimation(animation);
    }

    private double evaluateDistance(double[] last, double[] current) {
        double distance = 0.0;
        for (int i = 0; i < last.length; i++) {
            distance += Math.pow(last[i] - current[i], 2);
        }
        return Math.sqrt(distance);
    }




    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
        @RequiresApi(api = Build.VERSION_CODES.N)
        public void handleMessage(Message msg) {
            //获取当前时间
            SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss SSS");
            Date curDate = new Date(System.currentTimeMillis());
            String str = formatter.format(curDate);
            switch (msg.what) {
                case FIRST_SCAN:
                    // 没有赋予初始点，第一个点不展示UI
                    curLoc = Awknn();
                    info.setText(String.format("X:%1.2f\tY:%1.2f\tTime: %s\n%s", curLoc[0], curLoc[1], str, checkWhich));
                    Toast.makeText(MapActivity.this, "以获取第一个点", Toast.LENGTH_SHORT).show();
                    flag = START_SCAN;
                    initOnlineMap();

                    break;
                case START_SCAN:
                    double[] temp = null;
                    if (lastLoc != null) {
                        temp = lastLoc.clone();
                    }
                    lastLoc = curLoc.clone();
                    curLoc = Awknn();
                    // 两点差距过大时，修正距离
                    double distance = evaluateDistance(lastLoc, curLoc);
                    StringBuilder sb = new StringBuilder(checkWhich);
                    sb.append(String.format("\n%1.2f", distance));
//                    if (distance >= FIX_LOC && temp != null) {
//                        curLoc = temp;
//                        sb.append(" fix: ");
//                        sb.append(String.format("%1.2f", evaluateDistance(lastLoc, curLoc)));
//                    }
                    if (distance >= FIX_LOC) {
                        double[] fixLoc = new double[lastLoc.length];
                        double sin = (curLoc[1] - lastLoc[1]) / distance;
                        double cos = (curLoc[0] - lastLoc[0]) / distance;
                        fixLoc[0] = lastLoc[0] + FIX_LOC * cos;
                        fixLoc[1] = lastLoc[1] + FIX_LOC * sin;
                        curLoc = fixLoc;
                        sb.append(" now: ");
                        sb.append(String.format("%1.2f", evaluateDistance(lastLoc, curLoc)));
                    }

                    checkWhich = sb.toString();


                    if (kalman.isChecked()) {
                        kalmanFilter(lastLoc, curLoc);
                    }


                    // 不会定位在桌子上
//                    if (3 < curLoc[1] && curLoc[1] < 5) {
//                        curLoc[0] = 0.5 * Math.random() + 1;
//                    }
                    info.setText(String.format("X:%1.2f\tY:%1.2f\tTime: %s\n%s", curLoc[0], curLoc[1], str, checkWhich));

                    MoveIcon(lastLoc, curLoc);
                    Log.d("iconL", "\nlastLoc: " + Arrays.toString(lastLoc) + "\ncurLoc: " + Arrays.toString(curLoc));
                    initOnlineMap();

                    break;
                case STOP_SCAN:
                    Toast.makeText(MapActivity.this, "Stop Scan", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }
    };


}
