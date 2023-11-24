package com.example.testapplication;

import static android.content.ContentValues.TAG;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.Nullable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class DataBaseHelper extends SQLiteOpenHelper {

    private SQLiteDatabase db;
    private Context context;
    private static final String DATABASE_NAME = "TODOBASE.db";
    private static final String TABLE_NAME = "TODO_TABLE";
    private static final String COL_ID = "ID";
    private static final String COL_TASK = "TASK";
    private static final String COL_STATUS = "STATUS";
    private static final String COL_DESCRIPTION = "DESCRIPTION";
    private static final String COL_CREATION_DATE = "CREATION_DATE";
    private static final String COL_DEADLINE_DATE = "DEADLINE_DATE";
    private static final String COL_CATEGORY = "CATEGORY";
    private static final String COL_IS_ATTACHMENT = "IS_ATTACHMENT";
    private static final String COL_ATTACHMENT = "ATTACHMENT";
    private static final String COL_NOTIFICATION = "NOTIFICATION";

    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd.MM.yyyy", Locale.getDefault());

    public DataBaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, 1);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
    db.execSQL("CREATE TABLE IF NOT EXISTS " +
            TABLE_NAME +
            "(ID INTEGER PRIMARY KEY AUTOINCREMENT, "+
            COL_TASK +" TEXT, "
            + COL_STATUS +" INTEGER, "
            + COL_DESCRIPTION +" TEXT, "
            + COL_CREATION_DATE +" TEXT,"
            + COL_DEADLINE_DATE +" TEXT, "
            + COL_CATEGORY +" TEXT, "
            + COL_IS_ATTACHMENT +" INTEGER, "
            + COL_ATTACHMENT +" TEXT, "
            + COL_NOTIFICATION +" INTEGER)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
    db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
    onCreate(db);
    }
    public void insertTask(ToDoModel model)
    {
        db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_TASK,model.getTask());
        values.put(COL_STATUS,model.isDone());
        values.put(COL_DESCRIPTION,model.getDescription());

        //SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd.MM.yyyy", Locale.getDefault());
        String creationTimeString = sdf.format(model.getCreationDate());
        System.out.println(creationTimeString);
        values.put(COL_CREATION_DATE,creationTimeString);
        db.insert(TABLE_NAME,null,values);
    }
    public void updateTask(ToDoModel model) //Tu coś śmierdzi fuu
    {
        int id = model.getId();
        db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_TASK,model.getTask());
        values.put(COL_DESCRIPTION,model.getDescription());
        System.out.println("Updating");
        Log.d(TAG, "updateTask: "+model.getTask() + model.getDescription());

        if(model.getDeadlineDate() !=null)
        {
            //SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd.MM.yyyy", Locale.getDefault());
            String dateFormatted = sdf.format(model.getDeadlineDate());
            values.put(COL_DEADLINE_DATE,dateFormatted);
        }
        values.put(COL_CATEGORY,model.getCategory());
        boolean attachments = model.isAttachments();
        values.put(COL_IS_ATTACHMENT,attachments);
        if(attachments)
        {
            String att = model.getAttachmentURI().toString();
            values.put(COL_ATTACHMENT,att);
        }
        values.put(COL_NOTIFICATION,model.isNotification());
        db.update(TABLE_NAME,values,"ID=?",new String[]{String.valueOf(id)});
    }
    public void updateStatus(int id,int status) //todo
    {
        db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_STATUS,status);
        db.update(TABLE_NAME,values,"ID=?",new String[]{String.valueOf(id)});
    }
    public void deleteTask(int id)
    {
        db = this.getWritableDatabase();
        db.delete(TABLE_NAME,"ID=?",new String[]{String.valueOf(id)});
    }

//    public ArrayList<ToDoModel> getAllTasks(){
//        db = this.getWritableDatabase();
//        Cursor cursor = null;
//        ArrayList<ToDoModel> modelList = new ArrayList<>();
//        db.beginTransaction();
//        try{
//            cursor = db.query(TABLE_NAME,null,null,null,null,null,null);
//            if(cursor ==null)
//            {
//                db.endTransaction();
//                return modelList;
//            }
//            if(!cursor.moveToFirst()){
//                db.endTransaction();
//                cursor.close();
//                return modelList;
//            }
//            do{
//                ToDoModel task = new ToDoModel();
//                task.setId(cursor.getInt(cursor.getColumnIndex(COL_ID)));
//                task.setTask(cursor.getString(cursor.getColumnIndex(COL_TASK)));
//                task.setDescription(cursor.getString(cursor.getColumnIndex(COL_DESCRIPTION)));
//
//                int completed =cursor.getInt(cursor.getColumnIndex(COL_STATUS));
//                task.setDone(completed != 0);
//
//                String creationDateString = cursor.getString(cursor.getColumnIndex(COL_CREATION_DATE));
//                Date creationDate = sdf.parse(creationDateString);
//                task.setCreationDate(creationDate);
//
//                String deadlineDateString = cursor.getString(cursor.getColumnIndex(COL_DEADLINE_DATE));
//
//                if(deadlineDateString!=null)
//                {
//                    Date deadlineDate = sdf.parse(deadlineDateString);
//                    task.setDeadlineDate(deadlineDate);
//                }
//                task.setCategory(cursor.getString(cursor.getColumnIndex(COL_CATEGORY)));
//                int attachment =cursor.getInt(cursor.getColumnIndex(COL_IS_ATTACHMENT));
//                task.setAttachments(attachment!=0);
//                if(task.isAttachments())
//                {
//                    Uri uri = Uri.parse(cursor.getString(cursor.getColumnIndex(COL_ATTACHMENT)));
//                    task.setAttachmentURI(uri);
//                }
//
//
//                modelList.add(task);
//            }while (cursor.moveToNext());
//
//        } catch (ParseException e) {
//           e.printStackTrace();
//        } finally {
//            db.endTransaction();
//            cursor.close();
//        }
//        return modelList;
//    }

    public ArrayList<ToDoModel> getSpecificTasks(boolean hideDone, ViewCategories categories){
        db = this.getWritableDatabase();
        Cursor cursor = null;
        ArrayList<ToDoModel> modelList = new ArrayList<>();
        db.beginTransaction();
        try{
            String whereQuery="";
            ArrayList<String> argumentsArray = new ArrayList<>();
            if(hideDone){
                whereQuery= COL_STATUS + "= ?";
                argumentsArray.add("0");
            }
            if(hideDone && categories!= ViewCategories.ALL)
            {
                whereQuery+= " AND "+ COL_CATEGORY + "= ?";
                argumentsArray.add(categoryFromEnum(categories));
            }
            if(!hideDone && categories!= ViewCategories.ALL)
            {
                whereQuery= COL_CATEGORY + "= ?";
                argumentsArray.add(categoryFromEnum(categories));
            }
            if(!hideDone && categories== ViewCategories.ALL)
            {
                whereQuery = null;
                argumentsArray = null;
            }

            String[] arguments;
            if(argumentsArray!=null)
            {
                arguments =  argumentsArray.toArray(new String[0]);
            }
            else{
                arguments = null;
            }


            cursor = db.query(
                    TABLE_NAME,         // Table name
                    null,        // Columns to retrieve
                    whereQuery,              // Selection (WHERE clause)
                    arguments,              // Selection arguments
                    null,              // Group by
                    null,              // Having
                    null               // Order by
            );
            //cursor = db.query(TABLE_NAME,null,null,null,null,null,null);
            if(cursor ==null)
            {
               // db.endTransaction();
                return modelList;
            }
            if(!cursor.moveToFirst()){
                //db.endTransaction();
                cursor.close();
                return modelList;
            }
            do{
                ToDoModel task = new ToDoModel();
                task.setId(cursor.getInt(cursor.getColumnIndex(COL_ID)));
                task.setTask(cursor.getString(cursor.getColumnIndex(COL_TASK)));
                task.setDescription(cursor.getString(cursor.getColumnIndex(COL_DESCRIPTION)));

                int completed =cursor.getInt(cursor.getColumnIndex(COL_STATUS));
                task.setDone(completed != 0);

                String creationDateString = cursor.getString(cursor.getColumnIndex(COL_CREATION_DATE));
                Date creationDate = sdf.parse(creationDateString);
                task.setCreationDate(creationDate);

                String deadlineDateString = cursor.getString(cursor.getColumnIndex(COL_DEADLINE_DATE));

                if(deadlineDateString!=null)
                {
                    Date deadlineDate = sdf.parse(deadlineDateString);
                    task.setDeadlineDate(deadlineDate);
                }
                task.setCategory(cursor.getString(cursor.getColumnIndex(COL_CATEGORY)));
                int attachment =cursor.getInt(cursor.getColumnIndex(COL_IS_ATTACHMENT));
                task.setAttachments(attachment!=0);
                if(task.isAttachments())
                {
                    Uri uri = Uri.parse(cursor.getString(cursor.getColumnIndex(COL_ATTACHMENT)));
                    task.setAttachmentURI(uri);
                }


                modelList.add(task);
            }while (cursor.moveToNext());

        } catch (ParseException e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
            cursor.close();
        }
        return modelList;
    }

    private String categoryFromEnum(ViewCategories categories)
    {
        switch (categories)
        {
            case ALL:
                return "";
            case WORK:
                return "work";
            case STUDY:
                return "study";
            case PERSONAL:
                return "personal";
        }
        return "";
    }


}
