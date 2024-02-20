package com.example.testapplication;

import static android.app.PendingIntent.getActivity;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Objects;


enum NotificationTime {
    NONE,
    ATTIME,
    FIVEMINUTES,
    TENMINUTES

}

enum ViewCategories {
    ALL,
    PERSONAL,
    STUDY,
    WORK
}

public class MainActivity extends AppCompatActivity implements TodoRecyclerViewAdapter.OnTaskListener {
//We're gonna check recyclerView


    //todo notification repair
    private final String settingsFileName = "Settings.txt";
    public static final String NOTIFICATION_CHANNEL_ID = "6666";
    private final static String default_notification_channel_id = "default";
    private TodoRecyclerViewAdapter todoRecyclerViewAdapter;
    private ViewCategories categories = ViewCategories.ALL;
    private NotificationTime notificationTime = NotificationTime.NONE;
    private boolean hideCompleted = false;
    private ArrayList<ToDoModel> toDoModels = new ArrayList<>();
    private EditText taskName;
    private RecyclerView recyclerView;
    private DataBaseHelper myDB;
    private final DateFormat dateFormat = new SimpleDateFormat("HH:mm dd.MM.yyyy");
    private final ActivityResultLauncher<Intent> taskEditActivityLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() != 12) {
                        return;
                    }
                    Intent intent = result.getData();
                    if (intent == null) {
                        return;
                    }
                    int position = intent.getIntExtra("resIndex", -1);
                    ToDoModel toDoItem = toDoModels.get(position);
                    //Check if category has changed
                    String categoryBeforeSwapping = toDoItem.getCategory();
                    toDoItem.setCategory(intent.getStringExtra("resCategory"));
                    String att = (intent.getStringExtra("resAttachment"));
                    if (att != null) {
                        toDoItem.setAttachments(true);
                        toDoItem.setAttachmentURI(Uri.parse(att));
                        System.out.println(toDoItem.isAttachments());
                        Objects.requireNonNull(recyclerView.getAdapter()).notifyItemChanged(position);
                    }
                    toDoItem.setTask(intent.getStringExtra("resTitle"));
                    toDoItem.setDescription(intent.getStringExtra("resDescription"));
                    toDoItem.setDone(intent.getBooleanExtra("resStatus", false));
                    String dateString = intent.getStringExtra("resDueTime");
                    if (dateString.length() < 1) {
                        myDB.updateTask(toDoItem);
                        return;
                    }
                    try {
                        Date deadlineDate = dateFormat.parse(dateString);
                        toDoItem.setDeadlineDate(deadlineDate);
                        if(notificationTime!=NotificationTime.NONE)
                        {
//                            Toast.makeText(MainActivity.this, "Yoo bro, let's kill da hoe", Toast.LENGTH_SHORT).show();
                            setUpNotification(toDoItem,notificationTime);
                        }

                    } catch (ParseException e) {
                        throw new RuntimeException(e);
                    }

                    myDB.updateTask(toDoItem);
                    Objects.requireNonNull(recyclerView.getAdapter()).notifyItemChanged(position);

                }
            }
    );


    private final ActivityResultLauncher<Intent> settingsActivityLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() != 10) {
                        return;
                    }
                    Intent intent = result.getData();
                    if (intent == null) {
                        return;
                    }

                    boolean somethingChanged = false;
                    ViewCategories oldCategories = categories;
                    NotificationTime oldNotificationTime = notificationTime;
                    boolean oldHideCompleted = hideCompleted;


                    int intCategories = intent.getIntExtra("resCategoriesToView", 0);

                    categories = ViewCategories.values()[intCategories];

                    int intNotificationTime = intent.getIntExtra("resNotificationTime", 0);

                    notificationTime = NotificationTime.values()[intNotificationTime];

                    hideCompleted = intent.getBooleanExtra("resHideCompleted", false);

                    if (oldHideCompleted != hideCompleted || oldCategories != categories || oldNotificationTime != notificationTime) {
                        somethingChanged = true;
                    }

                    if (somethingChanged) {
                        todoRecyclerViewAdapter.setHideCompletedSetting(hideCompleted);
                        todoRecyclerViewAdapter.setNotificationsOptions(notificationTime);
                        refreshTasksFromDatabase();

                    }

//                    System.out.println("Values: "+ categories +": :" + notificationTime +intNotificationTime+ ": :" + hideCompleted);

                }
            });



    private void refreshTasksFromDatabase() {
        toDoModels.clear();
        //todoRecyclerViewAdapter.clearList()
        ArrayList<ToDoModel> list = myDB.getSpecificTasks(hideCompleted, categories);
        toDoModels.addAll(list);
        Collections.reverse(toDoModels);
        todoRecyclerViewAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recyclerView = findViewById(R.id.rvTodos); //TODOBASE
        taskName = findViewById(R.id.etTodo);
        //this.deleteDatabase("TODOBASE.db");

        myDB = new DataBaseHelper(this);
        toDoModels = myDB.getSpecificTasks(hideCompleted, categories);
        Collections.reverse(toDoModels);
        todoRecyclerViewAdapter = new TodoRecyclerViewAdapter(this, toDoModels, this, myDB);
        setUpTodo();

        recyclerView.setAdapter(todoRecyclerViewAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new RecyclerViewTouchHelper(todoRecyclerViewAdapter));
        itemTouchHelper.attachToRecyclerView(recyclerView);

        final Observer<NotificationTime> nameObserver = new Observer<NotificationTime>() {
            @Override
            public void onChanged(NotificationTime notificationTime) {

                setUpNotifications(notificationTime);
                System.out.println("Hello?");

            }
        };
        todoRecyclerViewAdapter.getCurrentNotificationsSettings().observe(this, nameObserver);

    loadSettings();


    }

    private void setUpNotifications(NotificationTime notificationTime) {
        //todo delete previously sheduled notifications
        switch (notificationTime) {
            case NONE:
                break;
            case ATTIME:
                scheduleNotifications(0);
                break;
            case FIVEMINUTES:
                scheduleNotifications(5);
                break;
            case TENMINUTES:
                scheduleNotifications(10);
                break;
        }
    }
    private void setUpNotification(ToDoModel item, NotificationTime notificationTime) {
        //todo delete previously sheduled notifications
        switch (notificationTime) {
            case NONE:
                break;
            case ATTIME:
                scheduleNotification(item,0);
                break;
            case FIVEMINUTES:
                scheduleNotification(item,5);
                break;
            case TENMINUTES:
                scheduleNotification(item,10);
                break;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        saveSettings();
    }
    private void saveSettings()
    {
        //Shared Preferences Thing
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        editor.putInt("NotificationSetting",notificationTime.ordinal());
        editor.putBoolean("HideCompleted",hideCompleted);
        editor.apply();
    }
    private void loadSettings()
    {
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        this.notificationTime = NotificationTime.values()[sharedPref.getInt("NotificationSetting",0)];
        this.hideCompleted = sharedPref.getBoolean("HideCompleted",false);
    }
    private void scheduleNotification(ToDoModel item, int beforeBuffer) {
        Notification notification = getNotification(item);
//        Intent intent = new Intent(this, TaskEditActivity.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        Intent notificationIntent = new Intent(this, NotificationPublisher.class);
        //notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION_ID, 1);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION, notification);
        //notificationIntent.putExtra(NotificationPublisher.TASK, item);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this,
                0,
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        Date currentDate = new Date();//getCurrentDateIn24Format();
        Date deadlineDate = item.getDeadlineDate();
        if(currentDate.after(deadlineDate))
        {
            return;
        }
        long timeDifferenceInMillis = SystemClock.elapsedRealtime() +deadlineDate.getTime() - currentDate.getTime();
        //Toast.makeText(getApplicationContext(), "diff: " + timeDifferenceInMillis, Toast.LENGTH_SHORT).show();
        System.out.println("currDate: " + currentDate);
        System.out.println("deadDate: " + deadlineDate);

        //delay
        timeDifferenceInMillis -= (long) beforeBuffer * 60 * 1000;
        if(timeDifferenceInMillis<0)
        {
            return;
        }



        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        //Toast.makeText(getApplicationContext(), "Alarm: ", Toast.LENGTH_SHORT).show();

        assert alarmManager != null;
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, timeDifferenceInMillis, pendingIntent);
    }

    private Date getCurrentDateIn24Format()
    {
        try{

            Date currentDate = new Date();
            String dateString = dateFormat.format(currentDate);
            return dateFormat.parse(dateString);
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }
        return new Date();

    }
    private Notification getNotification(ToDoModel item) {
        Intent intent = new Intent(this, TaskEditActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        intent.putExtra("Index", item.getId());
        intent.putExtra("Title", item.getTask());
        intent.putExtra("Description", item.getDescription());
        intent.putExtra("Status", item.isDone());
        intent.putExtra("Category", item.getCategory());

        if (item.getCreationDate() != null) {
            String creationTimeStr = dateFormat.format(item.getCreationDate());
            intent.putExtra("CreationTime", creationTimeStr);
        }
        if (item.getDeadlineDate() == null) {
            intent.putExtra("DueToTime", "");
        } else {

            String dueToTimeStr = dateFormat.format(item.getDeadlineDate());
            intent.putExtra("DueToTime", dueToTimeStr);
        }
        intent.putExtra("Tags", item.getCategory());
        if (item.isAttachments()) {
            intent.putExtra("Attachment", item.getAttachmentURI().toString());
        }


        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, default_notification_channel_id);
        builder.setContentTitle("!Do your task!");
        builder.setContentText(item.getTask());
        builder.setSmallIcon(R.drawable.ic_launcher_foreground);
        builder.setAutoCancel(true);
        builder.setChannelId(NOTIFICATION_CHANNEL_ID);
        builder.setContentIntent(pendingIntent);
        return builder.build();
    }


    public void scheduleNotifications(int dateShift) {

        for (ToDoModel item : toDoModels) {
            if (item.getDeadlineDate() == null) {
                continue;
            }
            scheduleNotification(item, dateShift);
            System.out.println(":Sheduled: "+ item.getTask() +" :");
        }
    }

    private void setUpTodo() {   //Here you can fill your data with fe. MSQL data (but for now I just leave it like that)

        if (toDoModels.size() > 0) {
            return;
        }
        String name = "Add new task";
        toDoModels.add(new ToDoModel(name, "Add new task using this app :D", false));
        String name2 = "Click on task to edit it";
        toDoModels.add(new ToDoModel(name2, "Now You're in an edit view", false));
        String name3 = "Swipe the task to delete";
        toDoModels.add(new ToDoModel(name3, "Please delete me", false));

    }

    public void addTask(View view) {

        String name = taskName.getText().toString();
        if (name.equals("")) {
            return;
        }
//        if(isInDataBase(name)) //todo it's optional but i can implement it
//        {
//            return;
//        }
        ToDoModel model = new ToDoModel(name, "None", false);
        myDB.insertTask(model);
        toDoModels.add(model);
        Objects.requireNonNull(recyclerView.getAdapter()).notifyItemChanged(0);


    }

    @Override
    public void onTaskClick(View view, int position) {

        Intent intent = new Intent(this, TaskEditActivity.class);

        ToDoModel todoItem = toDoModels.get(position);

        intent.putExtra("Index", position);
        intent.putExtra("Title", todoItem.getTask());
        intent.putExtra("Description", todoItem.getDescription());
        intent.putExtra("Status", todoItem.isDone());
        intent.putExtra("Category", todoItem.getCategory());

        if (todoItem.getCreationDate() != null) {
            String creationTimeStr = dateFormat.format(todoItem.getCreationDate());
            intent.putExtra("CreationTime", creationTimeStr);
        }
        if (todoItem.getDeadlineDate() == null) {
            intent.putExtra("DueToTime", "");
        } else {

            String dueToTimeStr = dateFormat.format(todoItem.getDeadlineDate());
            intent.putExtra("DueToTime", dueToTimeStr);
        }
        intent.putExtra("Tags", todoItem.getCategory());
        if (todoItem.isAttachments()) {
            intent.putExtra("Attachment", todoItem.getAttachmentURI().toString());
        }
        taskEditActivityLauncher.launch(intent);
    }

    public void goToSettings(View view) {
        Intent intent = new Intent(this, SettingsActivity.class);
        intent.putExtra("CategoriesToView", categories.ordinal());
        intent.putExtra("NotificationTime", notificationTime.ordinal());
        System.out.println("nt: " + notificationTime);
        intent.putExtra("HideCompleted", hideCompleted);
        settingsActivityLauncher.launch(intent);
    }


}