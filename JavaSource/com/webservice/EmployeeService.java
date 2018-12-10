package com.webservice;

import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.entity.Employee;
import com.entity.Token;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.qualifier.Resource;

/**
 * Service class which defines REST endpoints for the Employee object.
 * @author Cameron
 * @version 1.0
 */
@Path("/user")
public class EmployeeService {
    
    @Inject
    private EntityManager em;
    
    /**
     * Constructor which assigns the entity manager.
     */
    public EmployeeService() {
        em = Resource.getEntityManager();
    }
    
    /**
     * Gets all employees.
     * @param token Authentication token used in rest header.
     * @return Response for rest call
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getEmployees(@HeaderParam("Authorization") String token) {
        token = token.replace("Bearer ", "");
        if (!validateToken(token)) {
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        }
        Employee currentEmployee = getEmployeeByToken(token);
        
        if (currentEmployee == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        
        if (!currentEmployee.isAdmin()) {
            throw new WebApplicationException(Response.Status.FORBIDDEN);
        }
        String response = null;

        List<Employee> list = getAllEmployees();
        response = list.toString();
        return Response.ok(response).build();
    }
    
    /**
     * Gets a single employee.
     * @param token Authentication token.
     * @param id ID of employee to be found.
     * @return Response for rest call.
     */
    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getEmployee(@HeaderParam("Authorization") String token, 
            @PathParam("id") long id) {
        token = token.replace("Bearer ", "");
        if (!validateToken(token)) {
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        }
        Employee currentEmployee = getEmployeeByToken(token);
        
        if (currentEmployee == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        
        Employee employeeToView = getEmployeeById(id);
        
        if (!currentEmployee.isAdmin() 
                || currentEmployee.getEmployeeId() != id) {
            throw new WebApplicationException(Response.Status.FORBIDDEN);
        }
        
        return Response.ok(employeeToView).build();
    }
    
    /**
     * Creates a new employee.
     * @param token Authentication token.
     * @param payload String containing all fields to create a new employee
     * @return Response for REST call
     */
    @Transactional
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createEmployee(@HeaderParam("Authorization") String token,
            String payload) {
        token = token.replace("Bearer ", "");
        if (!validateToken(token)) {
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        }
        
        Employee currentEmployee = getEmployeeByToken(token);
        
        if (currentEmployee == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        
        if (!currentEmployee.isAdmin()) {
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        }
        List<Employee> allEmployees = getAllEmployees();
        GsonBuilder gsonBuilder = new GsonBuilder();
        Gson gson = gsonBuilder.create();
        Employee employeeToBeAdded = gson.fromJson(payload, Employee.class);
        for (Employee e : allEmployees) {
            if (e.getUserName()
                    .equalsIgnoreCase(employeeToBeAdded.getUserName())) {
                throw new WebApplicationException(Response.Status.CONFLICT);
            }
        }
      
        String returnCode = "200";
        em = Resource.getEntityManager();

        try {
            em.getTransaction().begin();
            em.persist(employeeToBeAdded);
            em.flush();
            em.refresh(employeeToBeAdded);
            em.getTransaction().commit();

            returnCode = "{" + "\"href\":\"http://localhost:8080/"
                    + "COMP3910_assignment3/v1/user/" 
                    + employeeToBeAdded.getEmployeeId()
                    + "\"," + "\"message\":\"New Employee "
                            + "successfully created.\"" + "}";
        } catch (IllegalArgumentException err) {
            err.printStackTrace();
            returnCode = "{\"status\":\"500\"," + "\"message\":"
                    + "\"Resource not created.\"" + "\"developerMessage\":\""
                    + err.getMessage() + "\"" + "}";
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(returnCode).build();

        }
        return Response.status(Response.Status.CREATED)
                .entity(returnCode).build();
    }
    
    /**
     * Deletes a single employee.
     * @param token Authentication token
     * @param id ID of employee to be deleted
     * @return Response of REST call
     */
    @Transactional
    @DELETE
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("delete/{id}")
    public Response deleteEmployee(@HeaderParam("Authorization") String token,
                                    @PathParam("id") long id) {
        token = token.replace("Bearer ", "");
        if (!validateToken(token)) {
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        }
        
        Employee currentEmployee = getEmployeeByToken(token);
        
        if (currentEmployee == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        
        if (!currentEmployee.isAdmin()) {
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
//            em.close();
            returnCode = "{" + "\"message\":\"Employee succesfully deleted\"" 
                    + "}";
        } catch (WebApplicationException err) {
            err.printStackTrace();
            returnCode = "{\"status\":\"500\"," + "\"message\":"
                    + "\"Resource not deleted.\"" 
                    + "\"developerMessage\":\""
                    + err.getMessage() + "\"" + "}";
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(returnCode).build();
        }
        return Response.ok(returnCode).build();
    }
    
    /**
     * Updates an existing employee object.
     * @param token Authentication token
     * @param id ID of employee to be updated
     * @param payload String containing fields to be updated
     * @return Response of REST call
     */
    @Transactional
    @PUT
    @Path("employees/{id}")
    @Consumes("application/json")
    public Response updateEmployee(@HeaderParam("Authorization") String token, 
            @PathParam("id") int id, String payload) {
        token = token.replace("Bearer ", "");
        if (!validateToken(token)) {
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        }
        
        Employee currentEmployee = getEmployeeByToken(token);
        
        if (currentEmployee == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        
        if (!currentEmployee.isAdmin() 
                || currentEmployee.getEmployeeId() != id) {
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        }
        
        GsonBuilder gsonBuilder = new GsonBuilder();
        Gson gson = gsonBuilder.create();
        Employee employee = gson.fromJson(payload, Employee.class);

        System.out.println(employee);
        em = Resource.getEntityManager();
        em.getTransaction().begin();
        Employee entity = em.find(Employee.class, id);
        if (entity == null) {
            String returnCode = "{ Employee not found }";
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(returnCode).build();
        }

        entity.setPassword(employee.getPassword());
        em.persist(entity);
        em.flush();
        em.getTransaction().commit();
        String returnCode = "{" + employee.getEmployeeId() 
            + ":\"Employee successfully edited.\"" + "}";
        return Response.status(Response.Status.CREATED)
                .entity(returnCode).build();
    }
    
    /**
     * Gets the token from the database.
     * @param token Token to get from the database.
     * @return Token
     */
    private Token getToken(String token) {
        TypedQuery<Token> query = em.createQuery(
              "select t from Token t where token=:token and isactive=:isactive",
              Token.class);
        query.setParameter("token", token);
        query.setParameter("isactive", true);
        Token activeToken;
        try {
            activeToken = query.getSingleResult();
        } catch (NoResultException e) {
            e.printStackTrace();
            activeToken = null;
        }
        return activeToken;
    }

    /**
     * Validates if the token is an active token.
     * @param token to validate
     * @return true if it token is not null
     */
    private boolean validateToken(String token) {
        Token activeToken = getToken(token);
        return !(activeToken == null);
    }
    
    /**
     * Gets the employee from the DB using the token.
     * @param token of employee to be found
     * @return Employee object
     */
    private Employee getEmployeeByToken(String token) {
        Token activeToken = getToken(token);
        long employeeId = activeToken.getEmployeeId();
        TypedQuery<Employee> query = em.createQuery(
                "select e from Employee e where employeeid=:employeeid",
                Employee.class);
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
    
    /**
     * Gets the employee from the DB by ID.
     * @param employeeId of employee to be found
     * @return Employee object
     */
    private Employee getEmployeeById(long employeeId) {
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
    
    /**
     * Gets a list of all employees in the db.
     * @return List of all employee in the DB
     */
    private List<Employee> getAllEmployees() {
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
