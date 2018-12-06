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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * The Persistent class for the Employee database table.
 * @author Cameron
 * @version 1.0
 */
@XmlRootElement(name="employee")
@Entity
@Table(name = "Employee")
@TransactionManagement(TransactionManagementType.BEAN)
public class Employee implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "employeeid")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long employeeId;

    @Column(name = "name")
    private String name;

    @Column(name = "username")
    private String userName;

    @Column(name = "password")
    private String password;

    @Column(name = "createddate")
    private Date createdDate;

    @Column(name = "isadmin")
    private boolean admin;

    /**
     * No arguments constructor.
     */
    public Employee() {

    }

    /**
     * Gets the employeeID.
     * @return employeeID
     */
    @XmlAttribute
    public Long getEmployeeId() {
        return employeeId;
    }

    /**
     * Sets the employeeID.
     * @param employeeId new employeeID
     */
    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    /**
     * Gets the employee name.
     * @return employee name
     */
    @XmlElement
    public String getName() {
        return name;
    }

    /**
     * Sets the employee name.
     * @param name new employee name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the employee username.
     * @return employee username
     */
    @XmlElement(name="userName")
    public String getUserName() {
        return userName;
    }

    /**
     * Sets the employee username.
     * @param userName new employee username
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * Gets the employee password.
     * @return employee password
     */
    @XmlElement
    public String getPassword() {
        return password;
    }

    /**
     * Sets the employee password.
     * @param password new employee password
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Gets the date employee was created.
     * @return date employee was created
     */
    @XmlElement(name="createdDate")
    public Date getCreatedDate() {
        return createdDate;
    }

    /**
     * Sets the date employee was created.
     * @param createdDate new createdDate
     */
    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    /**
     * Check to see if employee is an admin.
     * @return true if the employee is an admin
     */
    @XmlElement(name="isAdmin")
    public boolean isAdmin() {
        return admin;
    }

    /**
     * Sets if the employee is an admin.
     * @param admin true if employee is an admin
     */
    public void setAdmin(boolean admin) {
        this.admin = admin;
    }
    
    @Override
    public String toString() {
        Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
        String json = gson.toJson(this);  
        return json + "\n";
    }

}
