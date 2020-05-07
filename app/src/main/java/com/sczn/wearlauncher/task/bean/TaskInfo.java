package com.sczn.wearlauncher.task.bean;

import org.litepal.crud.LitePalSupport;

/**
 * Description:
 * Created by Bingo on 2019/2/22.
 */
public class TaskInfo extends LitePalSupport {
    private int id;
    private String task;
    private String startTime;
    private String endTime;
    private int classDays;
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTask() {
        return task;
    }

    public void setTask(String task) {
        this.task = task;
    }

    public int getClassDays() {
        return classDays;
    }

    public void setClassDays(int classDays) {
        this.classDays = classDays;
    }
    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    @Override
    public String toString() {
        return "TaskInfo{" +
                "id=" + id +
                ", task='" + task + '\'' +
                ", startTime='" + startTime + '\'' +
                ", endTime='" + endTime + '\'' +
                ", classDays=" + classDays +
                '}';
    }
}
