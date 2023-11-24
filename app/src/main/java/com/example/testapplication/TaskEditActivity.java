package com.example.testapplication;

import static android.content.ContentValues.TAG;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.util.Calendar;
import java.util.Locale;

public class TaskEditActivity extends AppCompatActivity {

    private int year;
    private int month;
    private int day;
    private int hour;
    private int minute;

    private EditText etTitle;
    private EditText etDescription;
    private TextView tvCreationDate;
    private TextView tvDueTime;
    private TextView tvCategory;
    private TextView tvStatus;

    private TextView tvAttachment;
    private Spinner spinner;
    private int position;

    private Uri attachment = null;
    private String chosenCategory;
    private boolean status;


    private final ActivityResultLauncher<Intent> activityLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if(result.getData()!=null)
                {
                    Intent intent = result.getData();
                    //Do something

                    Uri uri = intent.getData();
                    System.out.println("Attachment set: "+ uri);
                    attachment = uri;
                    String path = uri.getPath();
                    File file = new File(path);
                    setAttachment();
                }
            }
    );

    private final ActivityResultLauncher<Intent> seeAttachmentLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
         result -> {
        if(result.getData()!=null)
        {
            Intent intent = result.getData();
            //Do something

            Uri uri = intent.getData();
            System.out.println("Attachment set: "+ uri);
            attachment = uri;
            String path = uri.getPath();
            File file = new File(path);
            setAttachment();
        }
    }
    );

    private void setAttachment() {
        String textToView = "Click to see Attachment";
        tvAttachment.setText(textToView);
        tvAttachment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("Att: "+attachment);
                openFile(attachment);
            }
        });
    }


    private void openFile(Uri pickerInitialUri) {

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(pickerInitialUri, "image/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

//        List<ResolveInfo> resInfoList = getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
//        for (ResolveInfo resolveInfo : resInfoList) {
//            String packageName = resolveInfo.activityInfo.packageName;
//            grantUriPermission(packageName, pickerInitialUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
//        }
        // Check if there is an app available to handle the intent
        if (intent.resolveActivity(getPackageManager()) != null) {
            try {
                // Attempt to launch the image viewer
                startActivity(intent);
            } catch (SecurityException e) {
                // Handle the security exception
                // You can show a message to the user or take appropriate action
            }
        } else {
            // Handle the case where no app is available to open the image
            // You can show a message to the user or take appropriate action
        }
        //todo
        //startActivity(intent);
    }

    private void loadTaskInfo()
    {

        Intent intent = getIntent();


        position = intent.getIntExtra("Index",-1);
        String title = intent.getStringExtra("Title");
        String dest = intent.getStringExtra("Description");
        String category = intent.getStringExtra("Category");

        String att = intent.getStringExtra("Attachment");
        if(att!=null)
        {
            Log.d(TAG, "fillContent: "+att);
            attachment = Uri.parse(att);
            setAttachment();
        }

        String categoryText = "None";
        if(category != null)
        {
           categoryText = category;
           setSpinnerSelection(category);

        }
        String cat = getString(R.string.category, categoryText);
        tvCategory.setText(cat);

        boolean checked = intent.getBooleanExtra("Status",false);
        status = checked;

        if(status)
        {
           tvStatus.setText(getString(R.string.status_d));
        }
        else{
            tvStatus.setText(getString(R.string.status_ud));
        }

        etTitle.setText(title);
        etDescription.setText(dest);

        //Extracting the data
        String creationTimeText = tvCreationDate.getText().toString();
        String dueTimeText = tvDueTime.getText().toString();
        creationTimeText +=" " +intent.getStringExtra("CreationTime");
        dueTimeText +=" " +intent.getStringExtra("DueToTime");
        tvCreationDate.setText(creationTimeText);
        tvDueTime.setText(dueTimeText);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.task_edit);
        etTitle = findViewById(R.id.etTaskTitle);
        etDescription = findViewById(R.id.etTaskDesc);
        tvStatus = findViewById(R.id.tvStatus);
        tvCategory = findViewById(R.id.tvCategory);
        //changeDateButton = findViewById(R.id.changeDateButt);
        tvCreationDate = findViewById(R.id.tvCreationTime);
        tvDueTime = findViewById(R.id.tvDueTime);
        tvAttachment = findViewById(R.id.tvAttachments);
        //spinner part
        setUpSpinner();

        loadTaskInfo();
    }

    public void setUpSpinner()
    {
        spinner = findViewById(R.id.spinnerCategory);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.spinner_items, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setPrompt(getString(R.string.select_category));

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                chosenCategory = parent.getItemAtPosition(position).toString();
                //Toast.makeText(getApplicationContext(), "Selected: " + selectedItem, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Handle the case when nothing is selected
                spinner.setPrompt(getString(R.string.select_category));
                chosenCategory = null;
            }
        });
    }
    public void setSpinnerSelection(String category)
    {
        for(int i=0;i<3;i++)
        {
            String item = (String) spinner.getItemAtPosition(i);
            if(item.equals(category))
            {
                spinner.setSelection(i);
            }
        }

    }


    public void popTimePicker(View view){

        Calendar calendar =Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog.OnDateSetListener onDateSetListener = new DatePickerDialog.OnDateSetListener(){

            @Override
            public void onDateSet(DatePicker datePicker, int selectedYear, int selectedMonth, int SelectedDayOfMonth) {
                year = selectedYear;
                month = selectedMonth+1;
                day = SelectedDayOfMonth;

                String timeText = tvDueTime.getText().toString();
                timeText +=" "+ String.format(Locale.getDefault(),"%02d.%02d.%02d",day,month,year);
                tvDueTime.setText(timeText);
            }
        };

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,onDateSetListener,year,month,day);
        datePickerDialog.setTitle("Select Date");
        datePickerDialog.show();

        TimePickerDialog.OnTimeSetListener onTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                hour = selectedHour;
                minute = selectedMinute;
                String timeText = tvDueTime.getText().toString();
                timeText ="Due to time: "+ String.format(Locale.getDefault(),"%02d:%02d",hour,minute);
                tvDueTime.setText(timeText);
            }
        };
        TimePickerDialog timePickerDialog = new TimePickerDialog(this,onTimeSetListener,hour,minute,true);
        timePickerDialog.setTitle("Select Time");
        timePickerDialog.show();
    }

    public void addAttachment(View view){
        showFileChooser();
    }
    public void showFileChooser(){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        try{
            activityLauncher.launch(Intent.createChooser(intent,"Select a file"));
//            startActivityForResult(Intent.createChooser(intent,"Select a file"),100);
        }catch(Exception e)
        {
            Toast.makeText(this, "Install File manager u dumbass", Toast.LENGTH_SHORT).show();
        }
    }
    public void saveChanges(View view){
        Intent intent = new Intent();
        intent.putExtra("resIndex",position);
        intent.putExtra("resStatus",status);
        intent.putExtra("resCategory",chosenCategory);
        intent.putExtra("resTitle",etTitle.getText().toString());
        intent.putExtra("resDescription",etDescription.getText().toString());
        if(attachment!=null)
        {
            intent.putExtra("resAttachment",attachment.toString());
        }
        String time = tvDueTime.getText().toString();
        time = time.replace("Due to time: ","");
        time = time.replace("null","");
        intent.putExtra("resDueTime",time);

        setResult(12,intent);
        finish();
    }

}
