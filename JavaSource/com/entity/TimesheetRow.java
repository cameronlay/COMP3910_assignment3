package com.entity;

import java.io.Serializable;

import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Timesheet row model class.
 * @author Sunguk Ham
 * @version 1.0
 */
@XmlRootElement(name = "timesheetRow")
@Entity
@Table(name = "TimesheetRow")
@TransactionManagement(TransactionManagementType.BEAN)
public class TimesheetRow implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "timesheetrowid")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long timesheetRowId;

    @Column(name = "timesheetid")
    private Long timesheetId;

    @Column(name = "projectid")
    private Long projectId = 0L;

    @Column(name = "workpackage")
    private String workPackageId = "";

    @Column(name = "monday")
    private Integer monday = 0;

    @Column(name = "tuesday")
    private Integer tuesday = 0;

    @Column(name = "wednesday")
    private Integer wednesday = 0;

    @Column(name = "thursday")
    private Integer thursday = 0;

    @Column(name = "friday")
    private Integer friday = 0;

    @Column(name = "saturday")
    private Integer saturday = 0;

    @Column(name = "sunday")
    private Integer sunday = 0;

    @Column(name = "notes")
    private String notes = "";

    /**
     * Default constructor.
     */
    public TimesheetRow() {

    }

    /**
     * get timesheet row id.
     * @return id of timesheet row
     */
    @XmlAttribute
    public Long getTimesheetRowId() {
        return timesheetRowId;
    }

    /**
     * set timesheet row id.
     * @param timesheetRowId timesheet row id
     */
    public void setTimesheetRowId(Long timesheetRowId) {
        this.timesheetRowId = timesheetRowId;
    }

    /**
     * get timesheet id.
     * @return timesheet id
     */
    @XmlElement(name = "timesheetId")
    public Long getTimesheetId() {
        return timesheetId;
    }

    /**
     * set timesheet id.
     * @param timesheetId timesheet id
     */
    public void setTimesheetId(Long timesheetId) {
        this.timesheetId = timesheetId;
    }

    /**
     * get project id.
     * @return project id
     */
    @XmlElement(name = "projectId")
    public Long getProjectId() {
        return projectId;
    }

    /**
     * set project id.
     * @param projectId project id
     */
    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    /**
     * get work package id.
     * @return work package id
     */
    @XmlElement(name = "workPackageId")
    public String getWorkPackageId() {
        return workPackageId;
    }

    /**
     * set work package id.
     * @param workPackageId work package id
     */
    public void setWorkPackageId(String workPackageId) {
        this.workPackageId = workPackageId;
    }

    /**
     * get monday hour.
     * @return monday hour
     */
    @XmlElement
    public Integer getMonday() {
        return monday;
    }

    /**
     * set monday hour.
     * @param monday monday hour
     */
    public void setMonday(Integer monday) {
        this.monday = monday;
    }

    /**
     * get tuesday hour.
     * @return tuesday hour
     */
    @XmlElement
    public Integer getTuesday() {
        return tuesday;
    }

    /**
     * set tuesday hour.
     * @param tuesday hour
     */
    public void setTuesday(Integer tuesday) {
        this.tuesday = tuesday;
    }

    /**
     * get wednesday hour.
     * @return wednesday hour
     */
    @XmlElement
    public Integer getWednesday() {
        return wednesday;
    }

    /**
     * set wednesday hour.
     * @param wednesday hour
     */
    public void setWednesday(Integer wednesday) {
        this.wednesday = wednesday;
    }

    /**
     * get thursday hour.
     * @return thursday hour
     */
    @XmlElement
    public Integer getThursday() {
        return thursday;
    }

    /**
     * set thursday hour.
     * @param thursday hour
     */
    public void setThursday(Integer thursday) {
        this.thursday = thursday;
    }

    /**
     * get friday hour.
     * @return friday hour
     */
    @XmlElement
    public Integer getFriday() {
        return friday;
    }

    /**
     * set friday hour.
     * @param friday hour
     */
    public void setFriday(Integer friday) {
        this.friday = friday;
    }

    /**
     * get saturday hour.
     * @return saturday hour
     */
    @XmlElement
    public Integer getSaturday() {
        return saturday;
    }

    /**
     * set saturday hour.
     * @param saturday hour
     */
    public void setSaturday(Integer saturday) {
        this.saturday = saturday;
    }

    /**
     * get sunday hour.
     * @return sunday hour
     */
    @XmlElement
    public Integer getSunday() {
        return sunday;
    }

    /**
     * set sunday hour.
     * @param sunday hour
     */
    public void setSunday(Integer sunday) {
        this.sunday = sunday;
    }

    /**
     * get notes.
     * @return notes
     */
    @XmlElement
    public String getNotes() {
        return notes;
    }

    /**
     * set notes.
     * @param notes of the timesheet row
     */
    public void setNotes(String notes) {
        this.notes = notes;
    }
}
