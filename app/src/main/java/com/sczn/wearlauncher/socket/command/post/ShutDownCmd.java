package com.sczn.wearlauncher.socket.command.post;

import com.google.gson.Gson;
import com.sczn.wearlauncher.Config;
import com.sczn.wearlauncher.location.LocationCallBackHelper;
import com.sczn.wearlauncher.location.bean.LocationInfo;
import com.sczn.wearlauncher.socket.command.CmdCode;
import com.sczn.wearlauncher.socket.command.CommandResultCallback;
import com.sczn.wearlauncher.socket.command.bean.GpsInfo;
import com.sczn.wearlauncher.socket.command.bean.Reset;
import com.sczn.wearlauncher.socket.command.bean.WiFiAndBaseStationInfo;
import com.sczn.wearlauncher.util.GsonHelper;
import com.sczn.wearlauncher.util.TimeUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Description:
 * Created by Bingo on 2019/3/12.
 */
public class ShutDownCmd extends BaseCommand {
    LocationInfo mInfo;
    public ShutDownCmd(LocationInfo info, CommandResultCallback resultCallback) {
        super(resultCallback);
        mInfo = info;
    }

    @Override
    public String send() {
        if (mInfo != null) {
            if (mInfo.getType() == 2) {//gps数据
                List<GpsInfo.DataBean.GpsBean> beans = new ArrayList<>();
                GpsInfo.DataBean.GpsBean bean = new GpsInfo.DataBean.GpsBean();
                bean.setLat(mInfo.getLat());
                bean.setLng(mInfo.getLng());
                bean.setAccuracy(mInfo.getAccuracy());
                bean.setTimestamp(TimeUtil.getCurrentTimeSec());
                beans.add(bean);

                GpsInfo.DataBean data = new GpsInfo.DataBean();
                data.setGps(beans);
                data.setLocationType(2);
                //data.setOptUserId(LocationCallBackHelper.optUserId);

                GpsInfo gpsInfo = new GpsInfo();
                gpsInfo.setTimestamp(TimeUtil.getCurrentTimeSec());
                gpsInfo.setType(CmdCode.SHUTDOWN_NTY);
                gpsInfo.setData(data);
                return GsonHelper.getInstance().getGson().toJson(gpsInfo);
            } else {//基站或者WiFi数据
                WiFiAndBaseStationInfo.DataBean.LbsBean lbs = new WiFiAndBaseStationInfo.DataBean.LbsBean();
                lbs.setSmac("");
                lbs.setServerip("");
                lbs.setImsi("");
                lbs.setNetwork("");

                lbs.setCdma(mInfo.getCdma() == null ? 0 : Integer.parseInt(mInfo.getCdma()));

                lbs.setBts(mInfo.getBts());
                lbs.setNearbts(mInfo.getNearbts());

                lbs.setMmac(mInfo.getMmac());
                lbs.setMacs(mInfo.getMacs());
                lbs.setTimestamp(TimeUtil.getCurrentTimeSec());

                WiFiAndBaseStationInfo.DataBean data = new WiFiAndBaseStationInfo.DataBean();
                data.setLocationType(mInfo.getType());
                //data.setOptUserId(LocationCallBackHelper.optUserId);
                data.setLbs(lbs);

                WiFiAndBaseStationInfo info = new WiFiAndBaseStationInfo();
                info.setType(CmdCode.SHUTDOWN_NTY);
                info.setTimestamp(TimeUtil.getCurrentTimeSec());
                info.setData(data);
                return GsonHelper.getInstance().getGson().toJson(info);
            }
        }
        return "";
    }
}
