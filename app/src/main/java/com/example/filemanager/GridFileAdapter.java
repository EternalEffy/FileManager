package com.example.filemanager;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.util.List;

public class GridFileAdapter extends BaseAdapter {
    private Context mContext;
    private List<File> list;
    private File file;
    private boolean size,color;


    public GridFileAdapter(Context c, List<File> list,boolean size,boolean color) {
        mContext = c;
        this.list = list;
        this.size = size;
        this.color = color;
    }

    public int getCount() {
        return list.size();
    }

    public Object getItem(int position) {
        return list.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        View grid;
        file = list.get(position);

        if (convertView == null) {
            grid = new View(mContext);
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
            if(size == true) {
                grid = inflater.inflate(R.layout.grid_item, parent, false);
            }else grid = inflater.inflate(R.layout.big_grid_item,parent,false);
        } else {
            grid = (View) convertView;
        }
        LinearLayout gridLay = grid.findViewById(R.id.gridLay);
        ImageView imageView = (ImageView) grid.findViewById(R.id.imagePart);
        TextView textView = (TextView) grid.findViewById(R.id.textPart);
        if(color){
            textView.setTextColor(Color.WHITE);
        }else textView.setTextColor(Color.BLACK);
        if (file.isDirectory()){
        ((ImageView) grid.findViewById(R.id.imagePart)).setImageResource(R.mipmap.dir);
        } else {
        ((ImageView) grid.findViewById(R.id.imagePart)).setImageResource(R.mipmap.empty);
        }
        ((TextView) grid.findViewById(R.id.textPart)).setText(file.getName());

        if (file.getName().endsWith(".txt")) {
        ((ImageView) grid.findViewById(R.id.imagePart)).setImageResource(R.mipmap.text);
        } else if (file.getName().endsWith(".mp3") | file.getName().endsWith(".mp4")) {
        ((ImageView) grid.findViewById(R.id.imagePart)).setImageResource(R.mipmap.music);
        } else if (file.getName().endsWith(".jpg") | file.getName().endsWith(".png") | file.getName().endsWith(".jpeg") | file.getName().endsWith(".gif")) {
        ((ImageView) grid.findViewById(R.id.imagePart)).setImageResource(R.mipmap.image);
        } else if (file.getName().endsWith(".zip")) {
        ((ImageView) grid.findViewById(R.id.imagePart)).setImageResource(R.mipmap.zip);
        } else if (file.getName().endsWith(".pdf") | file.getName().endsWith(".rtf")) {
        ((ImageView) grid.findViewById(R.id.imagePart)).setImageResource(R.mipmap.pdf);
        }

        return grid;
    }

}