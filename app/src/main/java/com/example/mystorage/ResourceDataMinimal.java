package com.example.mystorage;

public class ResourceDataMinimal {
    long id;
    String resource;

    public ResourceDataMinimal(long id, String resource){
        this.id = id;
        this.resource = resource;
    }

    public ResourceDataMinimal(ResourceDataMinimal resourceDataMinimal){
        this.id = resourceDataMinimal.id;
        this.resource = resourceDataMinimal.resource;
    }

    @Override
    public String toString(){
        return this.resource;
    }
}
