package com.webservice;

import java.sql.Date;
import java.time.LocalDate;
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
import javax.ws.rs.core.MediaType;

import com.entity.Employee;
import com.entity.Token;
import com.qualifier.Resource;

@Path("/registration")
public class TokenService {

    @Inject
    private EntityManager em;

    public TokenService() {
        em = Resource.getEntityManager();
    }

    @Transactional
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Token createToken(Employee employee) {
        String username;
        String password;
        try {
            username = employee.getUserName();
            password = employee.getPassword();
        } catch (NullPointerException e) {
            return null;
        }
        Employee currentEmployee = getEmployeeByUsernameAndPassword(username, password);
        Token activeToken = getActiveTokenByUsername(username);
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
        LocalDate now = LocalDate.now();
        newToken.setDateCreated(Date.valueOf(now));
        LocalDate expiryDate = now.plusMonths(2);
        newToken.setExpiryDate(Date.valueOf(expiryDate));
        newToken.setEmployeeId(currentEmployee.getEmployeeId());
        newToken.setUsername(currentEmployee.getUserName());
        em.getTransaction().begin();
        em.persist(newToken);
        em.getTransaction().commit();
        return newToken;
    }

    private final Employee getEmployeeByUsernameAndPassword(String username, String password) {
        TypedQuery<Employee> query = em.createQuery(
                "select e from Employee e where username=:username and password=:password",
                Employee.class);
        query.setParameter("username", username);
        query.setParameter("password", password);
        return query.getSingleResult();
    }

    private final Token getActiveTokenByUsername(String username) {
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

    private final String generateUuid() {
        return UUID.randomUUID().toString();
    }

}
