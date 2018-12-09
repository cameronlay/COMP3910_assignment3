package com.webservice;

import java.sql.Date;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.entity.Employee;
import com.entity.Token;
import com.qualifier.Resource;

/**
 * Token service.
 * @author Sunguk Ham
 * @version 1.0
 */
@Path("/registration")
public class TokenService {

    @Inject
    private EntityManager em;

    /**
     * Constructor, initialize entity manager.
     */
    public TokenService() {
        em = Resource.getEntityManager();
    }

    /**
     * create token.
     * @param uriInfo uri information
     * @param employee employee object
     * @return response object
     */
    @Transactional
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createToken(
            @Context UriInfo uriInfo, Employee employee) {
        Map<String, String> responseMap = new HashMap<>();
        responseMap.put("uri", uriInfo.getAbsolutePath().toString());

        Employee currentEmployee;
        Token activeToken;
        try {
            currentEmployee = getEmployeeByUsernameAndPassword(
                    employee.getUserName(), employee.getPassword());
            if (currentEmployee == null) {
                throw new NullPointerException();
            }
            activeToken = getActiveTokenByUsername(employee.getUserName());
        } catch (NullPointerException e) {
            responseMap.put("message", Response.Status.UNAUTHORIZED.toString());
            responseMap.put("status", Response.Status.UNAUTHORIZED + "");
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(responseMap).build();
        }

        if (activeToken != null) {
            activeToken.setActive(false);
            em.getTransaction().begin();
            em.merge(activeToken);
            em.getTransaction().commit();
        }
        Token newToken = new Token();
        newToken.setToken(generateUuid());
        newToken.setActive(true);
        newToken.setAdmin(currentEmployee.isAdmin());
        newToken.setDateCreated(Date.valueOf(LocalDate.now()));
        newToken.setExpiryDate(Date.valueOf(LocalDate.now().plusMonths(2)));
        newToken.setEmployeeId(currentEmployee.getEmployeeId());
        newToken.setUsername(currentEmployee.getUserName());
        em.getTransaction().begin();
        em.persist(newToken);
        em.getTransaction().commit();
        return Response.status(Response.Status.OK).entity(newToken).build();
    }

    /**
     * get employee by username and password.
     * @param username username
     * @param password password
     * @return employee object
     */
    private Employee getEmployeeByUsernameAndPassword(
            String username, String password) {
        TypedQuery<Employee> query = em.createQuery(
                "select e from Employee e"
                + " where username=:username and password=:password",
                Employee.class);
        query.setParameter("username", username);
        query.setParameter("password", password);
        Employee currentEmployee;
        try {
            currentEmployee = query.getSingleResult();
        } catch (NoResultException e) {
            currentEmployee = null;
        }
        return currentEmployee;
    }

    /**
     * get active token by username.
     * @param username username
     * @return token object
     */
    private Token getActiveTokenByUsername(String username) {
        TypedQuery<Token> query = em.createQuery(
                "select t from Token t where username=:username and isactive=1",
                Token.class);
        query.setParameter("username", username);
        Token activeToken;
        try {
            activeToken = query.getSingleResult();
        } catch (NoResultException e) {
            activeToken = null;
        }
        return activeToken;
    }

    /**
     * generate uuid.
     * @return uuid string
     */
    private String generateUuid() {
        return UUID.randomUUID().toString();
    }

}
