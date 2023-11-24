package com.example.testapplication;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.net.URI;
import java.util.Calendar;
import java.util.Date;
import java.util.Observable;

public class ToDoModel{
 //Tutaj mmożna rozbudowywać typy danych
    int id;
    private String task;
    private String description;
    private Date creationDate;
    private Date deadlineDate;
    private boolean done;
    private boolean notification;
    private String category;
    private boolean attachments; //Have no idea, how to implement it
    private Uri attachmentURI;




    public ToDoModel() {

    }

    public Uri getAttachmentURI() {
        return attachmentURI;
    }

    public void setAttachmentURI(Uri attachmentURI) {
        this.attachmentURI = attachmentURI;
    }

    public ToDoModel(String task, String description, boolean done) {
        this.task = task;
        this.description = description;
        this.done = done;
        Calendar calendar = Calendar.getInstance();
        this.creationDate = calendar.getTime();

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setTask(String task) {
        this.task = task;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }
    public void setDeadlineDate(Date deadlineDate) {
        this.deadlineDate = deadlineDate;
    }

    public void setDone(boolean done) {
        this.done = done;
    }

    public void setNotification(boolean notification) {
        this.notification = notification;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setAttachments(boolean attachments) {
        this.attachments = attachments;

    }

    public ToDoModel(String task, String description, Date creationDate, Date deadlineDate, boolean done, boolean notification, String category, boolean attachments) {
        this.task = task;
        this.description = description;
        this.creationDate = creationDate;
        this.deadlineDate = deadlineDate;
        this.done = done;
        this.notification = notification;
        this.category = category;
        this.attachments = attachments;
    }
    public String getTask() {
        return task;
    }

    public String getDescription() {
        return description;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public Date getDeadlineDate() {
        return deadlineDate;
    }

    public boolean isDone() {
        return done;
    }

    public boolean isNotification() {
        return notification;
    }

    public String getCategory() {
        return category;
    }

    public boolean isAttachments() {
        return attachments;
    }

}
