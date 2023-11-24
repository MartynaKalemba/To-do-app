package com.example.testapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;


public class SettingsActivity extends AppCompatActivity {


    private Spinner spinnerNotifications;
    private Spinner spinnerCategoryVisible;
    private SwitchCompat notificationSwitch;
    private SwitchCompat hideCompletedSwitch;



    //Settings to exchange with MainActivity
    private boolean hideCompleted = false;
    private NotificationTime selectedTime = NotificationTime.NONE;
    private ViewCategories viewCategories = ViewCategories.ALL;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        getCurrentSettings();


        setUpNotificationSpinner();
        setUpViewCategoriesSpinner();

        setUpNotificationSwitch();
        setUpHideCompletedSwitch();

    }

    public void setUpNotificationSwitch()
    {
        notificationSwitch = findViewById(R.id.switchNotifications);

        if(selectedTime!= NotificationTime.NONE)
        {
            notificationSwitch.setChecked(true);
        }


        if(!notificationSwitch.isChecked())
        {
            selectedTime = NotificationTime.NONE;
        }

        spinnerNotifications.setEnabled(notificationSwitch.isChecked()); //todo to będzie trzeba zmienic


        notificationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    spinnerNotifications.setEnabled(true);
                    selectedTime = NotificationTime.ATTIME;

                } else {

                    spinnerNotifications.setEnabled(false);
                    selectedTime = NotificationTime.NONE;

                }
            }
        });
    }

    public void setUpHideCompletedSwitch()
    {
        hideCompletedSwitch = findViewById(R.id.switch_hide_tasks);


        hideCompletedSwitch.setChecked(hideCompleted);


        hideCompletedSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                hideCompleted = isChecked;
            }
        });
    }

    public void setUpNotificationSpinner()
    {
        spinnerNotifications = findViewById(R.id.spinnerTimeChoose);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.Notifications_timer, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerNotifications.setAdapter(adapter);


        spinnerNotifications.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String choosenNotificationTime = parent.getItemAtPosition(position).toString();
                if(spinnerNotifications.isEnabled())
                {
                    selectedTime = setNotifications(choosenNotificationTime);
                    System.out.println(selectedTime +" : : "+ choosenNotificationTime);
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

                spinnerNotifications.setPrompt("Select notifications time");

            }
        });
    }

    public NotificationTime setNotifications(String timeCategory)
    {
        //generalnie da sie to zrobić ładniej przez odwołanie się do string, ale nie chce mi się już
        //i to nie konieczne

        switch(timeCategory)
        {
            case "at time":
                return NotificationTime.ATTIME;
            case "5 min before":
                return NotificationTime.FIVEMINUTES;
            case "10 min before":
                return NotificationTime.TENMINUTES;
        }
        return NotificationTime.NONE;
    }

    public void setUpViewCategoriesSpinner()
    {
        spinnerCategoryVisible = findViewById(R.id.spinnerCategoryChoose);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.show_categories, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategoryVisible.setAdapter(adapter);
        //spinnerCategoryVisible.setPrompt("Select Categories");

        spinnerCategoryVisible.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String category = parent.getItemAtPosition(position).toString();

                viewCategories = setCategory(category);
//                System.out.println(category +" : : "+ viewCategories);

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                spinnerCategoryVisible.setPrompt("Select Categories");

            }
        });
    }

    public ViewCategories setCategory(String textCategory)
    {
        switch(textCategory)
        {
            case "personal":
                return ViewCategories.PERSONAL;
            case "study":
                return ViewCategories.STUDY;
            case "work":
                return ViewCategories.WORK;
        }
        return ViewCategories.ALL;
    }

    private void getCurrentSettings()
    {
        Intent intent = getIntent();
        int notifications = intent.getIntExtra("NotificationTime",0);

        selectedTime = NotificationTime.values()[notifications];
        System.out.println("Notification: " +selectedTime + ": : " + notifications);

        int category = intent.getIntExtra("CategoriesToView",0);
        viewCategories = ViewCategories.values()[category];

        System.out.println("Category: " + viewCategories + ": : " + category);

        hideCompleted = intent.getBooleanExtra("HideCompleted",false);

    }

    public void saveSettings(View view) {
        Intent intent = new Intent();
        intent.putExtra("resNotificationTime",selectedTime.ordinal());
        intent.putExtra("resCategoriesToView",viewCategories.ordinal()); //todo problably wrong that I'm putting enums
        intent.putExtra("resHideCompleted",hideCompleted);
        System.out.println("Notification Settings Save: " +selectedTime);
        setResult(10,intent);
        finish();

    }


}
