package com.clocktower.lullaby.model;

public class Users {

    private String image, name;
    private boolean userIsAdmin;

    public Users() {
    }

    public Users(String image, String name, boolean userIsAdmin) {
        this.image = image;
        this.name = name;
        this.userIsAdmin = userIsAdmin;
    }

    public String getImage() {
        return image;
    }

    public String getName() {
        return name;
    }

    public boolean isUserIsAdmin() {
        return userIsAdmin;
    }

    @Override
    public String toString() {
        return "User: " +name + ", "+ image + ", " + userIsAdmin;
    }
}
