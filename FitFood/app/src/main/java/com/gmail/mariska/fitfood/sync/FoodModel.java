package com.gmail.mariska.fitfood.sync;

import java.util.Date;

/**
 * Model of food data. Received from server. Immutable for application
 */
public class FoodModel {
    private int id;
    private String name;
    private String text;
    private int rating;
    private String author;
    private Date created;
    private Date updated;
    private byte[] img;

    private FoodModel() {
        //jackson
    }

    public FoodModel(int id, String name, String text, String author){
        this();
        this.id = id;
        this.name = name;
        this.text = text;
        this.author = author;
        this.created = new Date();
        this.updated = new Date();
    }

    public FoodModel(int id, String name, String text, String author, byte[] img, int rating){
        this(id, name, text, author);
        this.img = img;
        this.rating = rating;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getText() {
        return text;
    }

    public int getRating() {
        return rating;
    }

    public String getAuthor() {
        return author;
    }

    public Date getCreated() {
        return created;
    }

    public Date getUpdated() {
        return updated;
    }

    public byte[] getImg() {
        return img;
    }

}
