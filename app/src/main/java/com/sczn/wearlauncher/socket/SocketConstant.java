package com.sczn.wearlauncher.socket;

import com.sczn.wearlauncher.sp.SPKey;
import com.sczn.wearlauncher.sp.SPUtils;

/**
 * Description:Socket的配置
 * Created by Bingo on 2019/1/14.
 */
public class SocketConstant {

    /**
     * 主机IP地址(默认)
     */
    public static final String host = "120.78.235.49";
    /**
     * 端口号(默认)
     */
    public static final int port = 8888;

    /**
     * 获取地址
     *
     * @return
     */
    public static String getHost() {
        return (String) SPUtils.getParam(SPKey.IP, host);
    }

    /**
     * 获取地址
     *
     * @return
     */
    public static int getPort() {
        return (int) SPUtils.getParam(SPKey.PORT, port);
    }

    //********************************************************************************************//
    /**
     * Http通讯的地址
     * 业务服务器 120.78.235.49:8081；
     *    文件服务器 120.78.235.49:8071；
     */
    public static final String httpHostFile = "http://120.78.235.49:8071";
    public static final String httpHost = "http://120.78.235.49:8081";

    /**
     * 聊天文件获取地址
     *
     * @param filePath
     * @return
     */
    public static String chatFileAddress(String filePath) {
        return httpHostFile + "/resources/watch/" + filePath;
    }

    //上传文件
    public static final String fileUpload = httpHost + "/fileUpload/uploadWatchPhoto";
    //获取群列表
    public static final String listGroup = httpHost + "/watchChat/listGroup";
    //上传群语音
    public static final String uploadGroupVoice = httpHost + "/watchChat/uploadGroupVoice";
    //发送消息
    public static final String sendGroupMsg = httpHost + "/watchChat/sendGroupMsg";
    //获取群最新消息
    public static final String getGroupMsg = httpHost + "/watchChat/getGroupMsg";
    //获取群历史消息
    public static final String listGroupMsg = httpHost + "/watchChat/listGroupMsg";
    //退出摇一摇群
    public static final String quitShakeGroupMsg = httpHost + "/watchChat/quitShakeGroup";
}
