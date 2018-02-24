package com.test;

import java.io.Serializable;

public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private String name;

    public User() {
        super();
    }

    public User(int i, String username) {
        super();
        this.id = i;
        this.name = username;
    }

    public int getId() {
        return id;
    }

    public void setId(int i) {
        this.id = i;
    }

    public String getName() {
        return name;
    }

    public void setName(String username) {
        this.name = username;
    }
/*
	@Override
	public String toString() {
		return JSON.toJSONString(this);
	}
	*/
}