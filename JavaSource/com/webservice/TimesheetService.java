package com.webservice;

import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.entity.Employee;
import com.entity.Timesheet;
import com.entity.TimesheetRow;
import com.entity.Token;
import com.qualifier.Resource;

@Path("/timesheet")
public class TimesheetService {

    @Inject
    private EntityManager em;

    public TimesheetService() {
        em = Resource.getEntityManager();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public List<Timesheet> getAllTimesheets(@HeaderParam("authentication") String token) {
        if (!validateToken(token)) {
            return null;
        }
        Employee currentEmployee = getEmployeeByToken(token);
        if (currentEmployee == null) {
            return null;
        }
        List<Timesheet> timesheets = getTimesheetsByEmployeeId(currentEmployee.getEmployeeId());
        if (timesheets == null) {
            return null;
        }
        for (Timesheet timesheet : timesheets) {
            List<TimesheetRow> timesheetRows = getTimesheetRowsByTimesheetId(timesheet.getTimesheetId());
            if (timesheetRows == null) {
                continue;
            }
            timesheet.setTimesheetRows(timesheetRows);
        }
        return timesheets;
    }

    private final Token getTokenByToken(String token) {
        TypedQuery<Token> query = em.createQuery(
                "select t from Token t where token=:token and isactive=:isactive",
                Token.class);
        query.setParameter("token", token);
        query.setParameter("isactive", true);
        Token activeToken;
        try {
            activeToken = query.getSingleResult();
        } catch (NoResultException e) {
            activeToken = null;
        }
        return activeToken;
    }

    private final boolean validateToken(String token) {
        Token activeToken = getTokenByToken(token);
        if (activeToken == null) {
            return false;
        } else {
            return true;
        }
    }

    private final Employee getEmployeeByToken(String token) {
        Token activeToken = getTokenByToken(token);
        long employeeId = activeToken.getEmployeeId();
        TypedQuery<Employee> query = em.createQuery(
                "select e from Employee e where employeeid=:employeeid", Employee.class);
        query.setParameter("employeeid", employeeId);
        Employee currentEmployee;
        try {
            currentEmployee = query.getSingleResult();
        } catch (NoResultException e) {
            currentEmployee = null;
        }
        return currentEmployee;
    }

    private final List<Timesheet> getTimesheetsByEmployeeId(long employeeId) {
        TypedQuery<Timesheet> query = em.createQuery(
                "select t from Timesheet t where employeeid=:employeeid",
                Timesheet.class);
        query.setParameter("employeeid", employeeId);
        List<Timesheet> timesheets;
        try {
            timesheets = query.getResultList();
        } catch (NoResultException e) {
            timesheets = null;
        }
        return timesheets;
    }

    private final List<TimesheetRow> getTimesheetRowsByTimesheetId(long timesheetId) {
        TypedQuery<TimesheetRow> query = em.createQuery(
                "select t from TimesheetRow t where timesheetid=:timesheetid",
                TimesheetRow.class);
        query.setParameter("timesheetid", timesheetId);
        List<TimesheetRow> timesheetRows;
        try {
            timesheetRows = query.getResultList();
        } catch (NoResultException e) {
            timesheetRows = null;
        }
        return timesheetRows;
    }

}
