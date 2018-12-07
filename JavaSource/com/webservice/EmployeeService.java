package com.webservice;

import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
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
import com.entity.Supplier;
import com.entity.Token;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.qualifier.Resource;

@Path("/user")
public class EmployeeService {
    
    @Inject
    EntityManager em;
    
    public EmployeeService() {
        em = Resource.getEntityManager();
    }
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSuppliers(@HeaderParam("authentication") String token) {
        if(!validateToken(token)) {
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        }
        Employee currentEmployee = getEmployeeByToken(token);
        
        if(currentEmployee == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        
        if(!currentEmployee.isAdmin()) {
            throw new WebApplicationException(Response.Status.FORBIDDEN);
        }
        String response = null;

        List<Employee> list = getAllEmployees();
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
        
        return Response.ok(employeeToView).build();
    }
    
    @Transactional
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createEmployee(@HeaderParam("token") String token,
            String payload) {
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
        GsonBuilder gsonBuilder = new GsonBuilder();
        Gson gson = gsonBuilder.create();
        Employee employeeToBeAdded = gson.fromJson(payload, Employee.class);
      
        String returnCode = "200";
        em = Resource.getEntityManager();

        try {
            em.getTransaction().begin();
            em.persist(employeeToBeAdded);
            em.flush();
            em.refresh(employeeToBeAdded);
            em.getTransaction().commit();
            em.close();

            returnCode = "{" + "\"href\":\"http://localhost:8080/rest/supplierservice/supplier/" + employeeToBeAdded.getName()
                    + "\"," + "\"message\":\"New Supplier successfully created.\"" + "}";
        } catch (Exception err) {
            err.printStackTrace();
            returnCode = "{\"status\":\"500\"," + "\"message\":\"Resource not created.\"" + "\"developerMessage\":\""
                    + err.getMessage() + "\"" + "}";
            return Response.status(404).entity(returnCode).build();

        }
        return Response.status(201).entity(returnCode).build();
    }
    
    @Transactional
    @DELETE
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("delete/{id}")
    public Response deleteEmployee(@HeaderParam("token") String token,
                                    @PathParam("id") long id) {
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
        
        if (currentEmployee.getEmployeeId() == id) {
            throw new WebApplicationException(Response.Status.FORBIDDEN);
        }
        
        em = Resource.getEntityManager();
        String returnCode = "";
        try {
            em.getTransaction().begin();
            Employee existingEmployee = em.find(Employee.class, id);
            em.remove(existingEmployee);
            em.getTransaction().commit();
            em.close();
            returnCode = "{" + "\"message\":\"Supplier succesfully deleted\"" + "}";
        } catch (WebApplicationException err) {
            err.printStackTrace();
            returnCode = "{\"status\":\"500\"," + "\"message\":\"Resource not deleted.\"" 
                    + "\"developerMessage\":\""
                    + err.getMessage() + "\"" + "}";
            return Response.status(500).entity(returnCode).build();
        }
        return Response.ok(returnCode).build();
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
    
    private final List<Employee> getAllEmployees() {
        TypedQuery<Employee> query = em.createQuery(
                "select e from Employee e",
                Employee.class);
        List<Employee> employees;
        try {
            employees = query.getResultList();
        } catch (NoResultException e) {
            e.printStackTrace();
            employees = null;
        }
        return employees;
    }
    
}
