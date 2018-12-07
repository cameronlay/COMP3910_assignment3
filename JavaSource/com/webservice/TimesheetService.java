package com.webservice;

import java.sql.Date;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

/**
 * Timesheet service for REST API.
 * @author Sunguk Ham
 * @version 1.0
 *
 */
@Path("/timesheet")
public class TimesheetService {

    private static final int LEFT_DAYS = 6;

    @Inject
    private EntityManager em;

    /**
     * TimesheetService constructor.
     */
    public TimesheetService() {
        em = Resource.getEntityManager();
    }

    /**
     * get timesheet by week number, returning all if empty.
     * @param token user token
     * @param weekNumber week number
     * @param uriInfo uri info
     * @return response object
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getTimesheet(
            @HeaderParam("Authorization") String token,
            @QueryParam("weekNumber") Integer weekNumber,
            @Context UriInfo uriInfo) {

        token = token.replace("Bearer ", "");
        if (!validateToken(token)) {
            Map<String, String> errorMap = new HashMap<>();
            errorMap.put("uri", uriInfo.getAbsolutePath().toString());
            errorMap.put("message", "Unauthorized");
            errorMap.put("status", Response.Status.UNAUTHORIZED + "");
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(errorMap).build();
        }
        Employee currentEmployee = getEmployeeByToken(token);
        if (currentEmployee == null) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Employee does not exist in database").build();
        }

        if (weekNumber == null) {
            List<Timesheet> timesheets =
                    getTimesheetsByEmployeeId(currentEmployee.getEmployeeId());
            if (timesheets == null) {
                return Response.status(Response.Status.NO_CONTENT)
                        .entity("Timesheet does not exist").build();
            }
            for (Timesheet timesheet : timesheets) {
                List<TimesheetRow> timesheetRows =
                        getTimesheetRowsByTimesheetId(
                                timesheet.getTimesheetId());
                if (timesheetRows == null) {
                    continue;
                }
                timesheet.setTimesheetRows(timesheetRows);
            }
            return Response.status(Response.Status.OK)
                    .entity(timesheets).build();
        } else {
            Timesheet timesheet = getTimesheetByEmployeeIdWeekNumber(
                    currentEmployee.getEmployeeId(), weekNumber);
            if (timesheet == null) {
                return Response.status(Response.Status.NO_CONTENT).
                        entity("Timesheet does not exist in that week").build();
            }
            timesheet.setTimesheetRows(getTimesheetRowsByTimesheetId(
                    timesheet.getTimesheetId()));
            return Response.status(Response.Status.OK)
                    .entity(timesheet).build();
        }
    }

    /**
     * get current timesheet.
     * @param token user token
     * @return response object
     */
    @GET
    @Path("/current")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getCurrentTimesheet(
            @HeaderParam("Authorization") String token) {
        token = token.replace("Bearer ", "");
        if (!validateToken(token)) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("Unauthorized").build();
        }
        Employee currentEmployee = getEmployeeByToken(token);
        if (currentEmployee == null) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Employee does not exist in database").build();
        }
        Timesheet timesheet = getTimesheetByEmployeeIdWeekNumber(
                currentEmployee.getEmployeeId(), getWeekNumber());
        if (timesheet == null) {
            return Response.status(Response.Status.NO_CONTENT)
                    .entity("Current timesheet does not exist").build();
        }
        timesheet.setTimesheetRows(getTimesheetRowsByTimesheetId(
                timesheet.getTimesheetId()));
        return Response.status(Response.Status.OK).entity(timesheet).build();
    }

    /**
     * save timesheet by week number, save current timesheet if empty.
     * @param token user token
     * @param timesheet timesheet object
     * @param weekNumber integer
     * @return response object
     */
    @Transactional
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response saveTimesheet(
            @HeaderParam("Authorization") String token,
            Timesheet timesheet,
            @QueryParam("weekNumber") Integer weekNumber) {
        token = token.replace("Bearer ", "");
        if (!validateToken(token)) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("Unauthorized").build();
        }
        Employee currentEmployee = getEmployeeByToken(token);
        if (currentEmployee == null) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Employee does not exist in database").build();
        }
        timesheet.setStartWeek(getStartWeek());
        timesheet.setEndWeek(getEndWeek());
        timesheet.setEmployeeId(currentEmployee.getEmployeeId());
        List<TimesheetRow> timesheetRows = timesheet.getTimesheetRows();
        Integer weekNum = null;
        if (weekNumber == null) {
            weekNum = getWeekNumber();
        } else {
            weekNum = weekNumber;
        }
        Timesheet currentTimesheet = getTimesheetByEmployeeIdWeekNumber(
                currentEmployee.getEmployeeId(), weekNum);
        if (currentTimesheet == null) {
            em.getTransaction().begin();
            em.persist(timesheet);
            em.getTransaction().commit();
            currentTimesheet = getTimesheetByEmployeeIdWeekNumber(
                    currentEmployee.getEmployeeId(), getWeekNumber());
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
        return Response.status(Response.Status.OK)
                .entity(currentTimesheet).build();
    }

    /**
     * get start week, saturday.
     * @return saturday
     */
    private Date getStartWeek() {
        Calendar c = new GregorianCalendar();
        int currentDay = c.get(Calendar.DAY_OF_WEEK);
        int leftDays = Calendar.FRIDAY - currentDay - LEFT_DAYS;
        c.add(Calendar.DATE, leftDays);
        return new Date(c.getTime().getTime());
    }

    /**
     * get end week, friday.
     * @return friday
     */
    private Date getEndWeek() {
        Calendar c = new GregorianCalendar();
        int currentDay = c.get(Calendar.DAY_OF_WEEK);
        int leftDays = Calendar.FRIDAY - currentDay;
        c.add(Calendar.DATE, leftDays);
        return new Date(c.getTime().getTime());
    }

    /**
     * get current week number.
     * @return week number
     */
    private int getWeekNumber() {
        Calendar c = new GregorianCalendar();
        int currentDay = c.get(Calendar.DAY_OF_WEEK);
        int leftDays = Calendar.FRIDAY - currentDay;
        c.add(Calendar.DATE, leftDays);
        c.setTime(new Date(c.getTime().getTime()));
        c.setFirstDayOfWeek(Calendar.SATURDAY);
        return c.get(Calendar.WEEK_OF_YEAR) - 1;
    }

    /**
     * get timesheet by employee id and week number.
     * @param employeeId employee id
     * @param weekNumber week number
     * @return timesheet object
     */
    private Timesheet getTimesheetByEmployeeIdWeekNumber(
            long employeeId, int weekNumber) {
        Date saturday = getSaturdayByWeekNumber(weekNumber);
        TypedQuery<Timesheet> query = em.createQuery(
                "select t from Timesheet t "
                + "where startWeek=:startweek and employeeId=:employeeid",
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

    /**
     * get saturday by week number.
     * @param weekNumber week number
     * @return week number
     */
    private Date getSaturdayByWeekNumber(int weekNumber) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.WEEK_OF_YEAR, weekNumber);
        cal.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
        return new Date(cal.getTimeInMillis());
    }

    /**
     * get token by token.
     * @param token token string
     * @return token object
     */
    private Token getTokenByToken(String token) {
        TypedQuery<Token> query = em.createQuery(
                "select t from Token t "
                + "where token=:token and isactive=:isactive",
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

    /**
     * validate token.
     * @param token string
     * @return true if valid token
     */
    private boolean validateToken(String token) {
        Token activeToken = getTokenByToken(token);
        return !(activeToken == null);
    }

    /**
     * get employee by token.
     * @param token string
     * @return employee object
     */
    private Employee getEmployeeByToken(String token) {
        Token activeToken = em.find(Token.class, token);
        return em.find(Employee.class, activeToken.getEmployeeId());
    }

    /**
     * get timesheets by employee id.
     * @param employeeId employee id
     * @return timesheets
     */
    private List<Timesheet> getTimesheetsByEmployeeId(long employeeId) {
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

    /**
     * get timesheet rows by timesheet id.
     * @param timesheetId timesheet id
     * @return timesheet rows
     */
    private List<TimesheetRow> getTimesheetRowsByTimesheetId(long timesheetId) {
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

    /**
     * delete timesheet rows by timesheet id.
     * @param timesheetId timesheet id
     */
    @Transactional
    private void deleteTimesheetRowsByTimesheetId(long timesheetId) {
        Query query = em.createQuery(
                "delete from TimesheetRow t where t.timesheetId=:timesheetid");
        em.getTransaction().begin();
        query.setParameter("timesheetid", timesheetId).executeUpdate();
        em.getTransaction().commit();
    }

}
