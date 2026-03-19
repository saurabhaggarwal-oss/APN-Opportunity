package com.ttn.ck.queryprocessor.utils;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

@Service("beanInstance")
public final class Instance {
    /**
     * Central interface to provide configuration for an application.
     */
    private static ApplicationContext applicationContext;


    private Instance(ApplicationContext context) {
        applicationContext = context;
    }

    /**
     * Returns the Instance of Type t. It can be singleton as well as Prototype
     * Depends on user configuration.
     *
     * @param <T> the type parameter
     * @param t   the t
     * @return the t
     */
    public static <T> T of(Class<T> t) {
        return applicationContext.getBean(t);
    }


    public static Object of(String name) {
        return applicationContext.getBean(name);
    }
}