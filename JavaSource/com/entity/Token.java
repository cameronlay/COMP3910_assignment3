package com.entity;

import java.io.Serializable;
import java.sql.Date;

import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * The Persistent class for the Employee database table.
 * @author Cameron
 * @version 1.0
 */
@XmlRootElement(name="token")
@Entity
@Table(name = "Token")
@TransactionManagement(TransactionManagementType.BEAN)
public class Token implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name="token")
    private String token;
    @Column(name="employeeid")
    private Long employeeId;
    @Column(name="datecreated")
    private Date dateCreated;
    @Column(name="expirydate")
    private Date expiryDate;
    @Column(name="isadmin")
    private boolean admin;
    @Column(name="username")
    private String username;
    @Column(name="isactive")
    private boolean active;

    public Token() {

    }

    @XmlAttribute
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @XmlElement(name="employeeId")
    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    @XmlElement(name="dateCreated")
    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    @XmlElement(name="expiryDate")
    public Date getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(Date expiryDate) {
        this.expiryDate = expiryDate;
    }

    @XmlElement(name="isAdmin")
    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    @XmlElement(name="userName")
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @XmlElement(name="isActive")
    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

}
