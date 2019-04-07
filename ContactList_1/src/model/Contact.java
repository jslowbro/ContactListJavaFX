package model;

public class Contact {
    private String name;
    private String surname;
    private String number;
    private String photoPath;
    private boolean favourite;

    public Contact(String name, String surname, String number, String photoPath, boolean favourite) {
        this.name = name;
        this.surname = surname;
        this.number = number;
        this.photoPath = photoPath;
        this.favourite = favourite;
    }

    public Contact(){

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getPhotoPath() {
        return photoPath;
    }

    public void setPhotoPath(String photoPath) {
        this.photoPath = photoPath;
    }

    public boolean isFavourite() {
        return favourite;
    }

    public void setFavourite(boolean favourite) {
        this.favourite = favourite;
    }

    @Override
    public String toString() {
        return this.name +" "+ this.surname;
    }
}
