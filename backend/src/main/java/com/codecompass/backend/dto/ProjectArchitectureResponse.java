package com.codecompass.backend.dto;

import java.util.List;

public class ProjectArchitectureResponse {

    private List<String> applications;

    private List<String> restControllers;

    private List<String> controllers;

    private List<String> services;

    private List<String> repositories;

    private List<String> entities;

    private List<String> configurations;

    private List<String> components;

    private List<String> exceptionHandlers;

    private List<String> otherClasses;


    public ProjectArchitectureResponse(

            List<String> applications,

            List<String> restControllers,

            List<String> controllers,

            List<String> services,

            List<String> repositories,

            List<String> entities,

            List<String> configurations,

            List<String> components,

            List<String> exceptionHandlers,

            List<String> otherClasses
    ) {

        this.applications = applications;

        this.restControllers = restControllers;

        this.controllers = controllers;

        this.services = services;

        this.repositories = repositories;

        this.entities = entities;

        this.configurations = configurations;

        this.components = components;

        this.exceptionHandlers = exceptionHandlers;

        this.otherClasses = otherClasses;
    }


    public List<String> getApplications() {
        return applications;
    }


    public List<String> getRestControllers() {
        return restControllers;
    }


    public List<String> getControllers() {
        return controllers;
    }


    public List<String> getServices() {
        return services;
    }


    public List<String> getRepositories() {
        return repositories;
    }


    public List<String> getEntities() {
        return entities;
    }


    public List<String> getConfigurations() {
        return configurations;
    }


    public List<String> getComponents() {
        return components;
    }


    public List<String> getExceptionHandlers() {
        return exceptionHandlers;
    }


    public List<String> getOtherClasses() {
        return otherClasses;
    }
}