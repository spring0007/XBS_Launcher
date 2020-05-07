package com.sczn.wearlauncher.socket.command.obtain;

/**
 * Description:
 * Created by Bingo on 2019/3/20.
 */
public class LoginObtain {

    /**
     * createTime : 1551669103000
     * dv : null
     * imei : 3333
     * model : 11
     * phone : 13049859352
     * sd : null
     * watchId : 18
     */

    private long createTime;
    private String dv;
    private String imei;
    private String model;
    private String phone;
    private String sd;
    private int watchId;

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public String getDv() {
        return dv;
    }

    public void setDv(String dv) {
        this.dv = dv;
    }

    public String getImei() {
        return imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getSd() {
        return sd;
    }

    public void setSd(String sd) {
        this.sd = sd;
    }

    public int getWatchId() {
        return watchId;
    }

    public void setWatchId(int watchId) {
        this.watchId = watchId;
    }

    @Override
    public String toString() {
        return "LoginObtain{" +
                "createTime=" + createTime +
                ", dv='" + dv + '\'' +
                ", imei='" + imei + '\'' +
                ", model='" + model + '\'' +
                ", phone='" + phone + '\'' +
                ", sd='" + sd + '\'' +
                ", watchId=" + watchId +
                '}';
    }
}
