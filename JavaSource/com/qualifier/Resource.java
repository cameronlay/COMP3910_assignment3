package com.qualifier;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.ws.rs.Produces;

public class Resource {
    private static EntityManagerFactory mgr;
    
    @Produces
    public static EntityManager getEntityManager() {
        if (mgr == null) {
            mgr = Persistence.createEntityManagerFactory("assignment3");
        }
        EntityManager em = mgr.createEntityManager();
        return em;
    }
}
