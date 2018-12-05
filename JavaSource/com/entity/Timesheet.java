package com.entity;

import java.io.Serializable;
import java.sql.Date;

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
 * Timesheet model.
 * @author Sunguk Ham
 * @version 1.0
 */
@XmlRootElement(name="timesheet")
@Entity
@Table(name = "Timesheet")
@TransactionManagement(TransactionManagementType.BEAN)
public class Timesheet implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "timesheetid")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long timesheetId;

    @Column(name = "employeeid")
    private Long employeeId;

    @Column(name = "endweek")
    private Date endWeek;

    @Column(name = "startweek")
    private Date startWeek;

    /**
     * Default constructor.
     */
    public Timesheet() {

    }

    /**
     * get timesheet id.
     * @return timesheet id
     */
    @XmlAttribute
    public Long getTimesheetId() {
        return timesheetId;
    }

    /**
     * set timesheet id.
     * @param timesheetId id of timesheet
     */
    public void setTimesheetId(Long timesheetId) {
        this.timesheetId = timesheetId;
    }

    /**
     * get employee id.
     * @return employee id
     */
    @XmlElement(name="employeeId")
    public Long getEmployeeId() {
        return employeeId;
    }

    /**
     * set employee id.
     * @param employeeId id of employee
     */
    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    /**
     * get end of the week.
     * @return end of the week
     */
    @XmlElement(name="endWeek")
    public Date getEndWeek() {
        return endWeek;
    }

    /**
     * set end of the week.
     * @param endWeek end of the week
     */
    public void setEndWeek(Date endWeek) {
        this.endWeek = endWeek;
    }

    /**
     * get start of the week.
     * @return start of the week
     */
    @XmlElement(name="startWeek")
    public Date getStartWeek() {
        return startWeek;
    }

    /**
     * set start of the week.
     * @param startWeek start of the week
     */
    public void setStartWeek(Date startWeek) {
        this.startWeek = startWeek;
    }


}
