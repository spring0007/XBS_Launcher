package com.sczn.wearlauncher.contact;

import com.google.gson.Gson;
import com.sczn.wearlauncher.socket.command.CmdCode;
import com.sczn.wearlauncher.socket.command.CommandResultCallback;
import com.sczn.wearlauncher.socket.command.bean.BasePostBean;
import com.sczn.wearlauncher.socket.command.post.BaseCommand;
import com.sczn.wearlauncher.util.TimeUtil;

public class ContactCmd extends BaseCommand {

    public ContactCmd(CommandResultCallback resultCallback) {
        super(resultCallback);
    }
    @Override
    public String send() {
        BasePostBean bean = new BasePostBean();
        bean.setA(0);
        bean.setType(CmdCode.LINKMAN);
        bean.setTimestamp(TimeUtil.getCurrentTimeSec());
        return new Gson().toJson(bean);
    }
}

