package com.example.filemanager;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Handler;

public class FileListAdapter extends ArrayAdapter<File> {
    private Context context;
    private List<File> files;
    private boolean size,color;

    public FileListAdapter(Context context, int resource, List<File> objects,boolean size,boolean color) {
        super(context, resource, objects);
        this.context = context;
        files = objects;
        this.size = size;
        this.color = color;
    }

    @TargetApi(Build.VERSION_CODES.O)
    @SuppressLint("SetTextI18n")
    public View getView(int position, View convertView, ViewGroup parent) {
        File file = files.get(position);
        Date date = new Date(file.lastModified());
        String day,month;
        if(date.getDate()<=9){
            day = "0"+date.getDate();
        }else day = date.getDate()+"";

        if(date.getMonth()<=8){
            month = "0"+(date.getMonth()+1);
        }else  month = (date.getMonth()+1)+"";

        String creation = day+"."+month+"."+(date.getYear()+1900);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(MainActivity.LAYOUT_INFLATER_SERVICE);
        @SuppressLint("ViewHolder") View view = inflater.inflate(R.layout.list_item, null);
        ImageView imageView = view.findViewById(R.id.file_icon);
        TextView textView = view.findViewById(R.id.file_name);
        if(!size){
            ViewGroup.LayoutParams lp = imageView.getLayoutParams();
            textView.setTextSize(19);
            lp.height = 150;
            lp.width = 150;
        }

            try {
                file.listFiles();
                imageView.setImageResource(R.mipmap.dir);
                ((TextView) view.findViewById(R.id.file_name)).setText(file.getName() + "\n" + file.listFiles().length + " " + getWordAddition(file.listFiles().length) + " | " + creation);
                if(color){
                    ((TextView) view.findViewById(R.id.file_name)).setTextColor(Color.WHITE);
                }else ((TextView) view.findViewById(R.id.file_name)).setTextColor(Color.BLACK);
            } catch (NullPointerException e) {
                imageView.setImageResource(R.mipmap.empty);
                ((TextView) view.findViewById(R.id.file_name)).setText(file.getName() + "\n" + creation);
                if(color){
                    ((TextView) view.findViewById(R.id.file_name)).setTextColor(Color.WHITE);
                }else ((TextView) view.findViewById(R.id.file_name)).setTextColor(Color.BLACK);
            }

            if (file.getName().endsWith(".txt")) {
                imageView.setImageResource(R.mipmap.text);
            } else if (file.getName().endsWith(".mp3") | file.getName().endsWith(".mp4")) {
                imageView.setImageResource(R.mipmap.music);
            } else if (file.getName().endsWith(".jpg") | file.getName().endsWith(".png") | file.getName().endsWith(".jpeg") | file.getName().endsWith(".gif")) {
                imageView.setImageResource(R.mipmap.image);
            } else if (file.getName().endsWith(".zip")) {
                imageView.setImageResource(R.mipmap.zip);
            } else if (file.getName().endsWith(".pdf") | file.getName().endsWith(".rtf")) {
                imageView.setImageResource(R.mipmap.pdf);
            }

        return view;
    }

    public String getWordAddition(int num) {

        int preLastDigit = num % 100 / 10;

        if (preLastDigit == 1) {
            return "объектов";
        }

        switch (num % 10) {
            case 1:
                return "объект";
            case 2:
            case 3:
            case 4:
                return "объекта";
            default:
                return "объектов";
        }

    }



}
