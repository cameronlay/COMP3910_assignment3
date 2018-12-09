package com;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import com.webservice.EmployeeService;
import com.webservice.TimesheetService;
import com.webservice.TokenService;

/**
 * Base REST API class that extends javax application.
 * @author Sunguk Ham
 * @version 1.0
 */
@ApplicationPath("/v1")
public class App extends Application {
    private Set<Object> singletons = new HashSet<Object>();

    /**
     * constructor, initialize REST services.
     */
    public App() {
        singletons.add(new TokenService());
        singletons.add(new EmployeeService());
        singletons.add(new TimesheetService());

    }

    @Override
    public Set<Object> getSingletons() {
       return singletons;
    }

}
