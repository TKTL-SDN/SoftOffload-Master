/**
*    Copyright 2013 University of Helsinki
*
*    Licensed under the Apache License, Version 2.0 (the "License"); you may
*    not use this file except in compliance with the License. You may obtain
*    a copy of the License at
*
*         http://www.apache.org/licenses/LICENSE-2.0
*
*    Unless required by applicable law or agreed to in writing, software
*    distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
*    WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
*    License for the specific language governing permissions and limitations
*    under the License.
**/



package net.floodlightcontroller.mobilesdn;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.util.MACAddress;

/**
 * Class for Wireless client:
 * used for recording and managing client info
 *
 * @author Yanhe Liu <yanhe.liu@cs.helsinki.fi>
 *
 */
public class Client implements Comparable<Object> {
    protected static Logger log = LoggerFactory.getLogger(Client.class);

    private final MACAddress hwAddress;
    private InetAddress ipAddress;
    private String app = "trivial";
    private double upRate;
    private double downRate;
    private long connectTime;

    private IOFSwitch ofSwitch = null;      // not initialized
    private APAgent agent;
    // used to record nearby ap signal levels
    private Map<String, List<Integer>> apSignalLevelMap = new ConcurrentHashMap<String, List<Integer>>();
    private int apScanningTime = 0;

    private Timer switchTimer;

    // defaults
    static private final long SECONDS = 3 * 60 * 1000;

    // currently not used anymore, for testing before
    private void initializeClientTimer() {

        switchTimer = new Timer();    // set the timer

        switchTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                // set up message data
                byte[] mac = hwAddress.toBytes();
                byte[] b1 = "c".getBytes();
                byte[] b2 = "switch|sdntest1|open|\n".getBytes();

                byte[] message = new byte[b1.length + b2.length + mac.length];

                System.arraycopy(b1, 0, message, 0, b1.length);
                System.arraycopy(mac, 0, message, b1.length, mac.length);
                System.arraycopy(b2, 0, message, b1.length + mac.length, b2.length);

                agent.send(message);
                log.info("Send message to agent for client switching");
            }
        }, SECONDS);
    }

    /**
     * construct a client instance
     *
     * @param hwAddress Client's hw address
     * @param ipv4Address Client's IPv4 address
     */
    public Client(MACAddress hwAddress, InetAddress ipAddress, APAgent agt) {
        this.hwAddress = hwAddress;
        this.ipAddress = ipAddress;
        this.agent = agt;

        // initializeClientTimer();
        initConnectTime();
    }

    /**
     * construct a client instance
     *
     * @param hwAddress Client's hw address
     * @param ipv4Address Client's IPv4 address
     */
    public Client(String hwAddress, String ipAddress, APAgent agt) throws UnknownHostException {
        this.hwAddress = MACAddress.valueOf(hwAddress);
        this.ipAddress = InetAddress.getByName(ipAddress);
        this.agent = agt;

        // initializeClientTimer();
        initConnectTime();
    }

    /**
     * construct a client instance
     *
     * @param hwAddress Client's hw address
     * @param ipv4Address Client's IPv4 address
     */
    public Client(MACAddress hwAddress, InetAddress ipAddress, IOFSwitch sw, APAgent agt) {
        this.hwAddress = hwAddress;
        this.ipAddress = ipAddress;
        this.ofSwitch = sw;
        this.agent = agt;

        // initializeClientTimer();
        initConnectTime();
    }

    /**
     * construct a client instance
     *
     * @param hwAddress Client's hw address
     * @param ipv4Address Client's IPv4 address
     */
    public Client(String hwAddress, String ipAddress, IOFSwitch sw, APAgent agt) throws UnknownHostException {
        this.hwAddress = MACAddress.valueOf(hwAddress);
        this.ipAddress = InetAddress.getByName(ipAddress);
        this.ofSwitch = sw;
        this.agent = agt;

        // initializeClientTimer();
        initConnectTime();
    }

    /**
     * Set the client's first connecting time
     */
    public void initConnectTime() {
        this.connectTime = System.currentTimeMillis();
    }

    /**
     * Get the client's first connecting time
     * @return this.connectTime
     */
    public long getConnectTime() {
        return this.connectTime;
    }

    /**
     * Get the client's MAC address.
     * @return
     */
    public MACAddress getMacAddress() {
        return this.hwAddress;
    }

    /**
     * Get the client's IP address.
     * @return
     */
    public InetAddress getIpAddress() {
        return ipAddress;
    }

    /**
     * Set the client's IP address
     * @param addr
     */
    public void setIpAddress(InetAddress addr) {
        this.ipAddress = addr;
    }

    /**
     * Set the client's IP address
     * @param String addr
     * @throws UnknownHostException
     */
    public void setIpAddress(String addr) throws UnknownHostException {
        this.ipAddress = InetAddress.getByName(addr);
    }

    /**
     * get client's uprate value
     * @return
     */
    public synchronized double getUpRate() {
        return this.upRate;
    }

    /**
     * get client's downrate value
     * @return
     */
    public synchronized double getDownRate() {
        return this.downRate;
    }

    /**
     * Set the client's up rate value
     * @param r
     */
    public synchronized void updateUpRate(double r) {
        if (upRate != 0) { // test whether this is explicitly initialized
            upRate = (upRate + r) / 2;
        } else {
            upRate = r;
        }
    }

    /**
     * Set the client's down rate value
     * @param r
     */
    public synchronized void updateDownRate(double r) {
        if (downRate != 0) { // // test whether this is explicitly initialized
            downRate = (r + downRate) / 2;
        } else {
            downRate = r;
        }
        
    }

    /**
     * get client's corresponding openflow switch instance
     * @return
     */
    public IOFSwitch getSwitch() {
        return this.ofSwitch;
    }

    /**
     * Set the client's association openflow switch instance
     *
     * @param sw
     */
    public void setSwitch(IOFSwitch sw) {
        this.ofSwitch = sw;
    }

    /**
     * Set the client's running application
     *
     * @param app
     */
    public void setApp(String app) {
        this.app = app;
    }

    /**
     * Get the client's running app
     * @return app
     */
    public String getApp() {
        return app;
    }

    /**
     * Get the client's corresponding AP Agent.
     * @return
     */
    public APAgent getAgent() {
        return agent;
    }

    /**
     * clear the task for the timer
     */
    public void cancelTask() {
        this.switchTimer.cancel();
        this.switchTimer.purge();
    }
    
    /**
     * update current record of ap signal levels
     * the input follows this type: ssid1&bssid1&level1|ssid2&bssid2&level2|...
     *
     * @param fields: this is the context collect from the client
     */
    public synchronized void updateLocationInfo(String[] fields) {
        apScanningTime++;  // add one for every time it receive scanning results
        
        for (int i = 0; i < fields.length; i++) {
            String[] info = fields[i].split("&");
            // String ssid = info[0];
            String bssid = info[1].toLowerCase();
            int level = Integer.parseInt(info[2]);

            if (apSignalLevelMap.containsKey(bssid)) {
                // make sure every bssid list has the same size
                // user the same value for missing ones
                int size = apSignalLevelMap.get(bssid).size();
                for (int j = size; j < (apScanningTime - 1); j++) {
                    apSignalLevelMap.get(bssid).add(level);
                }
                apSignalLevelMap.get(bssid).add(level); 
            } else {
                List<Integer> signalLevelList = new ArrayList<Integer>();
                for (int j = 1; j < apScanningTime; j++) {
                    // set the same value for missing ones
                    signalLevelList.add(level);
                }
                signalLevelList.add(level);
                apSignalLevelMap.put(bssid, signalLevelList);
            }
        }
        
    }
    
    public Set<String> getNearbyAPSet() {
        return apSignalLevelMap.keySet();
    }
    
    public int getAPScanningTime() {
        return apScanningTime;
    }
    
    /**
     * calculate client mobility metric
     *
     * @param bssid
     */
    public double mobilityPrediction(String bssid) {
        List<Integer> signalLevelList = apSignalLevelMap.get(bssid);
        double mobility, level;
        
        if (signalLevelList == null) {
            throw new RuntimeException("invalid parameter for evaluation");
        }
        
        int s1 = signalLevelList.get(0);
        int s2 = signalLevelList.get(1);
        int s3 = signalLevelList.get(2);
        
        if (s1 <= s2 && s2 <= s3 && s3 - s1 > 3) { // definitely getting closer
            mobility = 1;
        } else if (s1 >= s2 && s2 >= s3 && s1 - s3 > 3) { // getting further
            mobility = 0.7;
        } else if (s1 <= s2 && s1 - s3 > 3) { // signal level first increases, but finally drops
            mobility = 0.8;
        } else if (s1 > s2 && s3 - s1 > 3) { // signal level first drops, but finally increases
            mobility = 0.9;
        } else {  // moving direction is not very clear
            mobility = 0.85;
        }
        
        if (s3 > -70) {
            level = 1;
        } else { // bad signal level
            level = 0.5;
        }
        
        log.info("mobility records for ap " + bssid + ": " + s1 + ", " + s2 + ", " + s3);
        log.info("mobility predition for ap " + bssid + ": " + mobility + " * " + level);
        return mobility * level;
    }
    
    public double signalEvaluation(String bssid) {
        List<Integer> signalLevelList = apSignalLevelMap.get(bssid);
        double result;
        
        if (signalLevelList == null) {
            throw new RuntimeException("invalid parameter for evaluation");
        }
        
        int s = signalLevelList.get(2);
        if (s >= -50) {
            s = -40;
        }
        result = mobilityPrediction(bssid) * (s + 100) / 90;
        
        log.info("signal evaluation for ap " + bssid + ": signalLevel=" + s + ", result=" + result);
        return result;
    }
    
    

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        builder.append("Client " + hwAddress.toString() + ", ipAddr="
                + ipAddress.getHostAddress() + ", uprate="
                + Double.toString(upRate) + ", downrate=" + Double.toString(downRate)
                + ", dpid=" + Long.toString(ofSwitch.getId()));

        return builder.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Client))
            return false;

        if (obj == this)
            return true;

        Client that = (Client) obj;

        return (this.hwAddress.equals(that.getMacAddress()));
    }


    @Override
    public int compareTo(Object o) {
        assert (o instanceof Client);

        if (this.hwAddress.toLong() == ((Client)o).getMacAddress().toLong())
            return 0;

        if (this.hwAddress.toLong() > ((Client)o).getMacAddress().toLong())
            return 1;

        return -1;
    }

}
