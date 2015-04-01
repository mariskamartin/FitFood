package com.gmail.mariska.fitfood.sync;

import java.util.Date;

/**
 * Created by mar on 31. 3. 2015.
 */
public class Food {
    private int id;
    private String name;
    private String text;
    private int rating;
    private String author;
    private Date created;
    private Date updated;
    private byte[] img;

    private Food() {
        //jackson
    }

    public Food(int id, String name, String text, String author){
        this(id, name, text, author, null, 0);
    }

    public Food(int id, String name, String text, String author, byte[] img, int rating){
        this.id = id;
        this.name = name;
        this.text = text;
        this.author = author;
        this.created = new Date();
        this.updated = new Date();
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


/*          FoodEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                FoodEntry.COLUMN_NAME+ " TEXT NOT NULL, " +
                FoodEntry.COLUMN_TEXT + " TEXT NOT NULL, " +
                FoodEntry.COLUMN_AUTHOR + " TEXT NOT NULL, " +
                FoodEntry.COLUMN_CREATED + " INTEGER NOT NULL, " +
                FoodEntry.COLUMN_UPDATED + " INTEGER NOT NULL, " +
                FoodEntry.COLUMN_RATING + " INTEGER, " +
                FoodEntry.COLUMN_IMG + " BLOB " +*/
}
