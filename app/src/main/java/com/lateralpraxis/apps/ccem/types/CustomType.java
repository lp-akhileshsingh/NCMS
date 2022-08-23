package com.lateralpraxis.apps.ccem.types;

public class CustomType {
	private String Id;
    private String Name;
    
    public CustomType ( String Id , String Name ) {
        this.Id = Id;
        this.Name = Name;
    }
    
    public void setId(String id){
        this.Id = id;
    }
 
    public void setName(String name){
        this.Name = name;
    }
    
    public String getId () {
        return Id;
    }

    public String getName () {
        return Name;
    }

    @Override
    public String toString () {
        return Name;
    } 
}
