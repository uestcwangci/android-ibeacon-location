package org.altbeacon.utils;

import android.os.Build;
import android.support.annotation.RequiresApi;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;

public class AlgorithmUtils {
    private int A, K;
    private Map<double[], Map<String, Double>> fingerMap;
    private Map<String, LinkedList<Integer>> uuid4rssi_scanAll;

    public AlgorithmUtils(int A, int K, Map<double[], Map<String, Double>> fingerMap) {
        this.A = A;
        this.K = K;
        this.fingerMap = fingerMap;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public double[] Awknn(Map<String, LinkedList<Integer>> uuid4rssi_scanAll) {
        this.uuid4rssi_scanAll = uuid4rssi_scanAll;
        // 对窗口得到的rssi滤波处理，并只留下信号最强的前A个的信息
        // uuid4rssi是LinkedHashMap，根据rssi升序排序
        Map<String, Double> uuid4rssi_WeNeed = filterRssi();
        uuid4rssi_WeNeed = sortMap(uuid4rssi_WeNeed);
        Map<String, Double> Aweight = funAweight(uuid4rssi_WeNeed);
        double[] curLoc = new double[2];


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
            disSum += 1.0 / entry.getValue();
        }
        for (Map.Entry<double[], Double> entry :
                sortDis.entrySet()) {
            curLoc[0] += (1.0 * entry.getKey()[0] / entry.getValue()) / disSum;
            curLoc[1] += (1.0 * entry.getKey()[1] / entry.getValue()) / disSum;
        }
        return curLoc;
    }

    // 根据窗口长度对rssi滤波
    private  Map<String, Double> filterRssi() {
        // 暂时只做均值滤波
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
    private  Map<String, Double> sortMap(Map<String, Double> tempMap) {
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
    private  Map<double[], Double> sortMap2(Map<double[], Double> tempMap, int num) {
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

    private  Map<String, Double> funAweight(Map<String, Double> uuid4rssi) {
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
}
