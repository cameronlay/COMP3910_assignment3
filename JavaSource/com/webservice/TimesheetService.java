package com.webservice;

import java.sql.Date;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

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
    public Response getTimesheet(
            @HeaderParam("Authorization") String token,
            @QueryParam("weekNumber") Integer weekNumber,
            @Context UriInfo uriInfo) {

        token = token.replace("Bearer ", "");
        if (!validateToken(token)) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("unauthorized").build();
        }
        Employee currentEmployee = getEmployeeByToken(token);
        if (currentEmployee == null) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Employee does not exist in database").build();
        }

        if (weekNumber == null) {
            List<Timesheet> timesheets = getTimesheetsByEmployeeId(currentEmployee.getEmployeeId());
            if (timesheets == null) {
                return Response.status(Response.Status.NO_CONTENT).entity("Timesheet does not exist").build();
            }
            for (Timesheet timesheet : timesheets) {
                List<TimesheetRow> timesheetRows = getTimesheetRowsByTimesheetId(timesheet.getTimesheetId());
                if (timesheetRows == null) {
                    continue;
                }
                timesheet.setTimesheetRows(timesheetRows);
            }
            return Response.status(Response.Status.OK).entity(timesheets).build();
        } else {
            Timesheet timesheet = getTimesheetByEmployeeIdWeekNumber(currentEmployee.getEmployeeId(), weekNumber);
            if (timesheet == null) {
                return Response.status(Response.Status.NO_CONTENT).entity("Timesheet does not exist in that week").build();
            }
            timesheet.setTimesheetRows(getTimesheetRowsByTimesheetId(timesheet.getTimesheetId()));
            return Response.status(Response.Status.OK).entity(timesheet).build();
        }

    }

    @GET
    @Path("/current")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getCurrentTimesheet(@HeaderParam("Authorization") String token) {
        token = token.replace("Bearer ", "");
        if (!validateToken(token)) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Unauthorized").build();
        }
        Employee currentEmployee = getEmployeeByToken(token);
        if (currentEmployee == null) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Employee does not exist in database").build();
        }
        Timesheet timesheet = getTimesheetByEmployeeIdWeekNumber(currentEmployee.getEmployeeId(), getWeekNumber());
        if (timesheet == null) {
            return Response.status(Response.Status.NO_CONTENT).entity("Current timesheet does not exist").build();
        }
        timesheet.setTimesheetRows(getTimesheetRowsByTimesheetId(timesheet.getTimesheetId()));
        return Response.status(Response.Status.OK).entity(timesheet).build();
    }

    @Transactional
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response saveCurrentTimesheet(@HeaderParam("Authorization") String token, Timesheet timesheet) {
        token = token.replace("Bearer ", "");
        if (!validateToken(token)) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Unauthorized").build();
        }
        Employee currentEmployee = getEmployeeByToken(token);
        if (currentEmployee == null) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Employee does not exist in database").build();
        }
        timesheet.setStartWeek(getStartWeek());
        timesheet.setEndWeek(getEndWeek());
        timesheet.setEmployeeId(currentEmployee.getEmployeeId());
        List<TimesheetRow> timesheetRows = timesheet.getTimesheetRows();
        Timesheet currentTimesheet = getTimesheetByEmployeeIdWeekNumber(currentEmployee.getEmployeeId(), getWeekNumber());
        if (currentTimesheet == null) {
            em.getTransaction().begin();
            em.persist(timesheet);
            em.getTransaction().commit();
            currentTimesheet = getTimesheetByEmployeeIdWeekNumber(currentEmployee.getEmployeeId(), getWeekNumber());
        } else {
            deleteTimesheetRowsByTimesheetId(currentTimesheet.getTimesheetId());
        }
        for (TimesheetRow timesheetRow : timesheetRows) {
            timesheetRow.setTimesheetId(currentTimesheet.getTimesheetId());
            em.getTransaction().begin();
            em.persist(timesheetRow);
            em.getTransaction().commit();
        }
        currentTimesheet.setTimesheetRows(timesheetRows);
        return Response.status(Response.Status.OK).entity(currentTimesheet).build();
    }

    private final Date getStartWeek() {
        Calendar c = new GregorianCalendar();
        int currentDay = c.get(Calendar.DAY_OF_WEEK);
        int leftDays = Calendar.FRIDAY - currentDay - 6;
        c.add(Calendar.DATE, leftDays);
        return new Date(c.getTime().getTime());
    }

    private final Date getEndWeek() {
        Calendar c = new GregorianCalendar();
        int currentDay = c.get(Calendar.DAY_OF_WEEK);
        int leftDays = Calendar.FRIDAY - currentDay;
        c.add(Calendar.DATE, leftDays);
        return new Date(c.getTime().getTime());
    }

    private final int getWeekNumber() {
        Calendar c = new GregorianCalendar();
        int currentDay = c.get(Calendar.DAY_OF_WEEK);
        int leftDays = Calendar.FRIDAY - currentDay;
        c.add(Calendar.DATE, leftDays);
        c.setTime(new Date(c.getTime().getTime()));
        c.setFirstDayOfWeek(Calendar.SATURDAY);
        return c.get(Calendar.WEEK_OF_YEAR) - 1;
    }

    private final Timesheet getTimesheetByEmployeeIdWeekNumber(long employeeId, int weekNumber) {
        Date saturday = getSaturdayByWeekNumber(weekNumber);
        TypedQuery<Timesheet> query = em.createQuery(
                "select t from Timesheet t where startWeek=:startweek and employeeId=:employeeid",
                Timesheet.class);
        query.setParameter("startweek", saturday);
        query.setParameter("employeeid", employeeId);
        Timesheet timesheet;
        try {
            timesheet = query.getSingleResult();
        } catch (NoResultException e) {
            timesheet = null;
        }
        return timesheet;
    }

    private final Date getSaturdayByWeekNumber(int weekNumber) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.WEEK_OF_YEAR, weekNumber);
        cal.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
        return new Date(cal.getTimeInMillis());
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
        Token activeToken = em.find(Token.class, token);
        return em.find(Employee.class, activeToken.getEmployeeId());
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

    @Transactional
    private final void deleteTimesheetRowsByTimesheetId(long timesheetId) {
        Query query = em.createQuery("delete from TimesheetRow t where t.timesheetId=:timesheetid");
        em.getTransaction().begin();
        query.setParameter("timesheetid", timesheetId).executeUpdate();
        em.getTransaction().commit();
    }

}
