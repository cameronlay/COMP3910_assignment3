package com;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import com.webservice.EmployeeService;
import com.webservice.TimesheetService;
import com.webservice.TokenService;

@ApplicationPath("/v1")
public class App extends Application {
    private Set<Object> singletons = new HashSet<Object>();

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
