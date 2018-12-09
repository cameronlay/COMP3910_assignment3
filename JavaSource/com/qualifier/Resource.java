package com.qualifier;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.ws.rs.Produces;

/**
 * Resource initializes Entity Manager from persistence xml.
 * @author Sunguk Ham
 * @version 1.0
 */
public class Resource {
    private static EntityManagerFactory mgr;

    /**
     * get Entity Manager from persistence.
     * @return entity manager
     */
    @Produces
    public static EntityManager getEntityManager() {
        if (mgr == null) {
            mgr = Persistence.createEntityManagerFactory("assignment3");
        }
        EntityManager em = mgr.createEntityManager();
        return em;
    }
}
