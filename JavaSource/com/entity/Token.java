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
 * Token object.
 * @author Cameron
 * @version 1.0
 *
 */
@XmlRootElement(name = "token")
@Entity
@Table(name = "Token")
@TransactionManagement(TransactionManagementType.BEAN)
public class Token implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "token")
    private String token;
    @Column(name = "employeeid")
    private Long employeeId;
    @Column(name = "datecreated")
    private Date dateCreated;
    @Column(name = "expirydate")
    private Date expiryDate;
    @Column(name = "isadmin")
    private boolean admin;
    @Column(name = "username")
    private String username;
    @Column(name = "isactive")
    private boolean active;

    /**
     * Basic constructor.
     */
    public Token() {

    }

    /**
     * token getter.
     * @return token
     */
    @XmlAttribute
    public String getToken() {
        return token;
    }

    /**
     * token setter.
     * @param token to be set
     */
    public void setToken(String token) {
        this.token = token;
    }

    /**
     * employeeId getter.
     * @return employeeId
     */
    @XmlElement(name = "employeeId")
    public Long getEmployeeId() {
        return employeeId;
    }

    /**
     * employeeId setter.
     * @param employeeId to be set
     */
    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    /**
     * dateCreated getter.
     * @return dateCreated
     */
    @XmlElement(name = "dateCreated")
    public Date getDateCreated() {
        return dateCreated;
    }

    /**
     * dateCreated setter.
     * @param dateCreated to be set
     */
    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    /**
     * expiryDate getter.
     * @return expiryDate
     */
    @XmlElement(name = "expiryDate")
    public Date getExpiryDate() {
        return expiryDate;
    }

    /**
     * Sets expiry date.
     * @param expiryDate to be set.
     */
    public void setExpiryDate(Date expiryDate) {
        this.expiryDate = expiryDate;
    }

    /**
     * Admin getter.
     * @return isAdmin
     */
    @XmlElement(name = "isAdmin")
    public boolean isAdmin() {
        return admin;
    }

    /**
     * Admin setter.
     * @param admin new admin value
     */
    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    /**
     * Username getter.
     * @return username
     */
    @XmlElement(name = "userName")
    public String getUsername() {
        return username;
    }

    /**
     * Username setter.
     * @param username username to be set
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Active getter.
     * @return active value
     */
    @XmlElement(name = "isActive")
    public boolean isActive() {
        return active;
    }

    /**
     * Active setter.
     * @param active boolean to be set
     */
    public void setActive(boolean active) {
        this.active = active;
    }

}
