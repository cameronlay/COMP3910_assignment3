package com.webservice;

import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.entity.Employee;
import com.entity.Token;
import com.qualifier.Resource;

@Path("/user")
public class EmployeeService {
    
    @Inject
    EntityManager em;
    
    public EmployeeService() {
        em = Resource.getEntityManager();
    }
    
    @GET
    @Path("status")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getStatus() {
        return Response.ok("{\"status\":\"Running..\"}").build();
    }
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSuppliers(@HeaderParam("authentication") String token) {
        if(!validateToken(token)) {
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        }
        String response = null;
        em = Resource.getEntityManager();
        Query query = em.createQuery("From com.entity.Employee");
        List<Employee> list = query.getResultList();
        em.close();
        response = list.toString();
        return Response.ok(response).build();
    }
    
    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getEmployee(@HeaderParam("token") String token, @PathParam("id") long id) {
        if(!validateToken(token)) {
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        }
        Employee currentEmployee = getEmployeeByToken(token);
        
        if(currentEmployee == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        
        Employee employeeToView = getEmployeeById(id);
        
        if(!currentEmployee.isAdmin() || currentEmployee.getEmployeeId() != employeeToView.getEmployeeId()) {
            throw new WebApplicationException(Response.Status.FORBIDDEN);
        }
        
        if (employeeToView == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);   
        }

        return Response.ok(employeeToView).build();
    }
    
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createEmployee(@HeaderParam("token") String token,
            @FormParam("username") String username,
            @FormParam("password") String password,
            @FormParam("confirmpassword") String confirmPassword,
            @FormParam("firstname") String firstName,
            @FormParam("lastname") String lastName) {
        if(!validateToken(token)) {
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        }
        
        Employee currentEmployee = getEmployeeByToken(token);
        
        if(currentEmployee == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        
        if(!currentEmployee.isAdmin()) {
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        }
        
        if (!password.equals(confirmPassword)) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }

        if (username == null || password == null || confirmPassword == null
                || firstName == null || lastName == null) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
        return Response.ok().build();
    }
    
    private Token getToken(String token) {
        Token activeToken = null;
        TypedQuery<Token> query = em.createQuery(
                "select t from Token t where token=:token and isactive=:isactive",
                Token.class);
        query.setParameter("token", token);
        query.setParameter("isactive", true);
        try {
            activeToken = query.getSingleResult();
        } catch (NoResultException e) {
            e.printStackTrace();
        }
        return activeToken;
    }

    private boolean validateToken(String token) {
        Token activeToken = getToken(token);
        return activeToken == null;
    }
    
    private Employee getEmployeeByToken(String token) {
        Token activeToken = getToken(token);
        long employeeId = activeToken.getEmployeeId();
        TypedQuery<Employee> query = em.createQuery(
                "select e from Employee e where employeeid=:employeeid", Employee.class);
        query.setParameter("employeeid", employeeId);
        Employee currentEmployee;
        try {
            currentEmployee = query.getSingleResult();
        } catch (NoResultException e) {
            e.printStackTrace();
            currentEmployee = null;
        }
        return currentEmployee;
    }
    
    private final Employee getEmployeeById(long employeeId) {
        TypedQuery<Employee> query = em.createQuery(
                "select e from Employee e where employeeid=:employeeid",
                Employee.class);
        query.setParameter("employeeid", employeeId);
        Employee employee;
        try {
            employee = query.getSingleResult();
        } catch (NoResultException e) {
            e.printStackTrace();
            employee = null;
        }
        return employee;
    }
    
}
