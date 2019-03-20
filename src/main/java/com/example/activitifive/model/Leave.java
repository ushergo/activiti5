package com.example.activitifive.model;

import java.io.Serializable;
import java.util.Date;

public class Leave  implements Serializable {

    private static final long serialVersionUID = 2248469053125414262L;

    private String userId;

    private Boolean submit;

    private Date startDate;

    private Date endDate;

    private float totalDay;

    private String desc;

    private String taskId;

    private String taskName;

    private String approver1;

    private Boolean agree1;

    private String approveDesc1;

    private String approver2;

    private Boolean agree2;

    private String approveDesc2;

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Boolean getSubmit() {
        return submit;
    }

    public void setSubmit(Boolean submit) {
        this.submit = submit;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public float getTotalDay() {
        return totalDay;
    }

    public void setTotalDay(float totalDay) {
        this.totalDay = totalDay;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getApprover1() {
        return approver1;
    }

    public void setApprover1(String approver1) {
        this.approver1 = approver1;
    }

    public Boolean getAgree1() {
        return agree1;
    }

    public void setAgree1(Boolean agree1) {
        this.agree1 = agree1;
    }

    public String getApproveDesc1() {
        return approveDesc1;
    }

    public void setApproveDesc1(String approveDesc1) {
        this.approveDesc1 = approveDesc1;
    }

    public String getApprover2() {
        return approver2;
    }

    public void setApprover2(String approver2) {
        this.approver2 = approver2;
    }

    public Boolean getAgree2() {
        return agree2;
    }

    public void setAgree2(Boolean agree2) {
        this.agree2 = agree2;
    }

    public String getApproveDesc2() {
        return approveDesc2;
    }

    public void setApproveDesc2(String approveDesc2) {
        this.approveDesc2 = approveDesc2;
    }
}
