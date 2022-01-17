package com.example.mystorage;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity
public class ResourceData implements Serializable {
    @PrimaryKey (autoGenerate = true)
    private long id;
    private String resource;
    private String login = "";
    private String password;
    private String description = "";

    public ResourceData (String resource, String login, String password, String description){
        //this.id = id;
        this.resource = resource;
        if (!login.equals(""))
            this.login = login;
        this.password = password;
        if (!description.equals(""))
            this.description = description;
    };

    @Ignore
    public ResourceData (ResourceData resourceData){
        this.resource = resourceData.resource;
        this.login = resourceData.login;
        this.password = resourceData.password;
        this.description = resourceData.description;
    };



    public long getId() { return id; };

    public String getResource(){
        return resource;
    };

    public String getLogin(){
        return login;
    };

    public String getPassword(){
        return password;
    };

    public String getDescription(){
        return description;
    };

    public void setId(long id) {
        this.id = id;
    }

    public void setResource(String resource){
        this.resource = resource;
    };

    public void setLogin(String login){
        this.login = login;
    };

    public void setPassword(String password){
        this.password = password;
    };

    public void setDescription(String description){
        this.description = description;
    };

    @Override
    public String toString(){
        return this.resource;
    }
}


