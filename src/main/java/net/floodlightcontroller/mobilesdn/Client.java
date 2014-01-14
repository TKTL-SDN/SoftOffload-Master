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

import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.util.MACAddress;

/**
 * Wireless client class
 * Used for recording and managing client info
 *
 * @author Yanhe Liu <yanhe.liu@cs.helsinki.fi>
 *
 */
public class Client implements Comparable<Object> {
    private final MACAddress hwAddress;
    private InetAddress ipAddress;
    private float upRate;
    private float downRate;
    private IOFSwitch ofSwitch = null;      // not initialized


    /**
     * construct a client instance
     *
     * @param hwAddress Client's hw address
     * @param ipv4Address Client's IPv4 address
     */
    public Client(MACAddress hwAddress, InetAddress ipAddress) {
        this.hwAddress = hwAddress;
        this.ipAddress = ipAddress;
    }

    /**
     * construct a client instance
     *
     * @param hwAddress Client's hw address
     * @param ipv4Address Client's IPv4 address
     */
    public Client(String hwAddress, String ipAddress) throws UnknownHostException {
        this.hwAddress = MACAddress.valueOf(hwAddress);
        this.ipAddress = InetAddress.getByName(ipAddress);
    }

    /**
     * construct a client instance
     *
     * @param hwAddress Client's hw address
     * @param ipv4Address Client's IPv4 address
     */
    public Client(MACAddress hwAddress, InetAddress ipAddress, IOFSwitch sw) {
        this.hwAddress = hwAddress;
        this.ipAddress = ipAddress;
        this.ofSwitch = sw;
    }

    /**
     * construct a client instance
     *
     * @param hwAddress Client's hw address
     * @param ipv4Address Client's IPv4 address
     */
    public Client(String hwAddress, String ipAddress, IOFSwitch sw) throws UnknownHostException {
        this.hwAddress = MACAddress.valueOf(hwAddress);
        this.ipAddress = InetAddress.getByName(ipAddress);
        this.ofSwitch = sw;
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
    public float getUpRate() {
        return this.upRate;
    }

    /**
     * get client's downrate value
     * @return
     */
    public float getDownRate() {
        return this.downRate;
    }

    /**
     * Set the client's up rate value
     * @param r
     */
    public void updateUpRate(float r) {
        this.upRate = r;
    }

    /**
     * Set the client's down rate value
     * @param r
     */
    public void updateDownRate(float r) {
        this.downRate = r;
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

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        builder.append("Client " + hwAddress.toString() + ", ipAddr="
                + ipAddress.getHostAddress() + ", uprate="
                + Float.toString(upRate) + ", downrate=" + Float.toString(downRate)
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

        return (this.hwAddress.equals(that.hwAddress));
    }


    @Override
    public int compareTo(Object o) {
        assert (o instanceof Client);

        if (this.hwAddress.toLong() == ((Client)o).hwAddress.toLong())
            return 0;

        if (this.hwAddress.toLong() > ((Client)o).hwAddress.toLong())
            return 1;

        return -1;
    }
}