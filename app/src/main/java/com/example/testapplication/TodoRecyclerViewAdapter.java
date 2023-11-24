package com.example.testapplication;

import static androidx.core.content.ContextCompat.startActivity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Observable;
//import java.util.Observer;
import androidx.lifecycle.Observer;
import androidx.annotation.Nullable;


public class TodoRecyclerViewAdapter extends RecyclerView.Adapter<TodoRecyclerViewAdapter.MyViewHolder> {
    Context context;
    static ArrayList<ToDoModel> toDoModels;
    private OnTaskListener monTaskListener;
    private DataBaseHelper myDB;

    private boolean hideCompleted = false;
//    private ViewCategories categoriesToVieW = ViewCategories.ALL;
    private MutableLiveData<NotificationTime> notificationsOptions = new MutableLiveData<>();


    public MutableLiveData<NotificationTime> getCurrentNotificationsSettings() {
        if (notificationsOptions == null) {
            notificationsOptions = new MutableLiveData<NotificationTime>();
        }
        return notificationsOptions;
    }

    public void setNotificationsOptions(NotificationTime options)
    {
        this.notificationsOptions.setValue(options);
    }



    public TodoRecyclerViewAdapter(Context context, ArrayList<ToDoModel> toDoModels,OnTaskListener onTaskListener,DataBaseHelper db){
        this.context = context;
        this.toDoModels = toDoModels;
        this.monTaskListener = onTaskListener;
        this.myDB = db;

    }

    @NonNull
    @Override
    public TodoRecyclerViewAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.item_todo,parent,false);
        return new TodoRecyclerViewAdapter.MyViewHolder(view,monTaskListener);
    }

    @Override
    public void onBindViewHolder(@NonNull TodoRecyclerViewAdapter.MyViewHolder holder, int position) {
        final ToDoModel item =toDoModels.get(position);
        holder.tvName.setText(toDoModels.get(position).getTask());
        holder.box.setChecked(toDoModels.get(position).isDone());
        holder.box.setText("Done");
        holder.box.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b) {
                    item.setDone(true);
                    myDB.updateStatus(item.getId(),1);
                    if(hideCompleted)
                    {
                        toDoModels.remove(item);
                        notifyItemRemoved(item.getId());
                    }
                }else{
                    item.setDone(false);
                    myDB.updateStatus(item.getId(),0);

                }
            }
        });
        holder.iconsSet(toDoModels.get(position));
    }
    public void setHideCompletedSetting(boolean status)
    {
        this.hideCompleted = status;
    }

    public void clearList()
    {
        int size = toDoModels.size();
        while (size > 0) {
            toDoModels.remove(0); // Remove the first item in the list
            notifyItemRemoved(0);
            size = toDoModels.size(); // Update the size of the list
        }
    }
    public void deleteTask(int position)
    {
        ToDoModel item = toDoModels.get(position);
        myDB.deleteTask(item.getId());
        toDoModels.remove(position);
        notifyItemRemoved(position);

    }
    public void editTask(int position)
    {
        ToDoModel item = toDoModels.get(position);

        myDB.updateTask(item);

        notifyItemChanged(position);

    }

    public Context getContext()
    {
        return this.context;
    }

    @Override
    public int getItemCount() {
        return toDoModels.size();
    }



    public static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        TextView tvName;
        ImageView iconAtt;
        CheckBox box;

        OnTaskListener onTaskListener;
        public MyViewHolder(@NonNull View itemView, OnTaskListener onTaskListener) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvTitle);
            box = itemView.findViewById(R.id.cbDone);
            iconAtt = itemView.findViewById(R.id.ivAttachment);
            this.onTaskListener = onTaskListener;
            itemView.setOnClickListener(this);

        }
        private void iconsSet(ToDoModel model) {
            if (model.isAttachments()) {
                iconAtt.setVisibility(View.VISIBLE);
            } else {
                iconAtt.setVisibility(View.GONE);
            }
        }

        @Override
        public void onClick(View view) {
            onTaskListener.onTaskClick(view, getBindingAdapterPosition());
        }
    }
    public interface OnTaskListener{
        void onTaskClick(View view, int position);
    }
}
