package com.sczn.wearlauncher.task.bean;

import com.google.gson.Gson;
import com.sczn.wearlauncher.socket.command.CmdCode;
import com.sczn.wearlauncher.socket.command.CommandResultCallback;
import com.sczn.wearlauncher.socket.command.bean.BasePostBean;
import com.sczn.wearlauncher.socket.command.post.BaseCommand;
import com.sczn.wearlauncher.util.TimeUtil;

public class TaskCmd extends BaseCommand {

    public TaskCmd(CommandResultCallback resultCallback) {
        super(resultCallback);
    }
    @Override
    public String send() {
        BasePostBean bean = new BasePostBean();
        bean.setA(0);
        bean.setType(CmdCode.TASK);
        bean.setTimestamp(TimeUtil.getCurrentTimeSec());
        return new Gson().toJson(bean);
    }
}
