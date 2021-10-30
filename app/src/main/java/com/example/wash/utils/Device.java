package com.example.wash.utils;

public class Device {

    private static String address;
    private static String channel;
    private static String money;

    public static String getAddress() {
        return address;
    }

    public static void setAddress(String address) {
        Device.address = address;
    }

    public static String getChannel() {
        return channel;
    }

    public static void setChannel(String channel) {
        Device.channel = channel;
    }

    public static String getMoney() {
        return money;
    }

    public static void setMoney(String money) {
        Device.money = money;
    }
}
