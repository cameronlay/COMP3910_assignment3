package com.webservice;

import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.entity.Employee;
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
    public Response getEmployee(@PathParam("id") int id) {
        em = Resource.getEntityManager();
        Employee employee = em.find(Employee.class, id);
        if (employee == null) {
            String returnCode = "{ Employee not found }";
            return Response.status(Response.Status.NOT_FOUND).entity(returnCode).build();
        }
        return Response.ok(employee.toString()).build();
    }
}
