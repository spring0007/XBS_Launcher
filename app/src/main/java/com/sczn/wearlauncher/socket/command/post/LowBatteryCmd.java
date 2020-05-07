package com.sczn.wearlauncher.socket.command.post;

import com.sczn.wearlauncher.socket.command.CmdCode;
import com.sczn.wearlauncher.socket.command.CommandResultCallback;
import com.sczn.wearlauncher.socket.command.bean.BasePostBean;
import com.sczn.wearlauncher.util.GsonHelper;
import com.sczn.wearlauncher.util.TimeUtil;

public class LowBatteryCmd extends BaseCommand {

    public LowBatteryCmd(CommandResultCallback resultCallback) {
        super(resultCallback);
    }

    @Override
    public String send() {
        //return Base.compose(content);
        BasePostBean  basePostBean = new BasePostBean();
        basePostBean.setTimestamp(TimeUtil.getCurrentTimeSec());
        basePostBean.setType(CmdCode.LOW_POWER);
        return GsonHelper.getInstance().getGson().toJson(basePostBean);
    }
}
