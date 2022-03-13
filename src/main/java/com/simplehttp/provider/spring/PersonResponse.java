package com.simplehttp.provider.spring;

import java.util.List;

public class PersonResponse {
    List<Person> people;

    public List<Person> getPeople() {
        return people;
    }

    public void setPeople(List<Person> people) {
        this.people = people;
    }
}
