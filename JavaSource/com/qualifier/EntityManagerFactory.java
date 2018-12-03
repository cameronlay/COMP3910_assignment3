package com.qualifier;

import java.io.Serializable;

import javax.inject.Inject;
import javax.persistence.EntityManager;

public class EntityManagerFactory implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @Inject private Resource resource;
    
    public EntityManager gEntityManager() {
        return resource.getEntityManager();
    }
}
