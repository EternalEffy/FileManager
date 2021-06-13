package com.example.filemanager;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.text.InputFilter;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class MainActivity extends Activity {
    private Button display,displaySize,settings,search;
    private SharedPreferences app_setings;
    private ListView fileListView;
    private Switch themeSwitch,commercial;
    private TextView author;
    private LinearLayout settingsPage,mainPage;
    private GridView gridListView;
    private List<File> fileList = new ArrayList<>();
    private File root;
    private String path;
    private Boolean flag=true,size = true,color,ad;
    private int selected = Integer.MAX_VALUE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getActionBar().hide();
        initRes();
        loadAppSetting();
        loadPage(root.getPath());
        loadLogoAnim();
        setOnClick();
    }

    private void loadLogoAnim() {

        ImageView logo = findViewById(R.id.logo);
        TextView app_name = findViewById(R.id.appName);
        if(color){
            app_name.setTextColor(Color.WHITE);
        }else app_name.setTextColor(Color.BLACK);

        Animation animation = AnimationUtils.loadAnimation(this, R.anim.hide_anim);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                LinearLayout anim = findViewById(R.id.logo_amin);
                anim.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        logo.startAnimation(animation);
        app_name.startAnimation(animation);

    }


    public void initRes(){
        String sdState = android.os.Environment.getExternalStorageState();
        if (sdState.equals(android.os.Environment.MEDIA_MOUNTED)) {
            root = Environment.getExternalStorageDirectory();
            path = root.getAbsolutePath();
        } else {
            Toast.makeText(MainActivity.this,"Не найдена sd карта",Toast.LENGTH_SHORT).show();
            MainActivity.this.onDestroy();
        }
        app_setings = getSharedPreferences("APP_DATA", Context.MODE_PRIVATE);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.context_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        selected = (info.position);
        path = fileList.get(info.position).getPath();
        switch (item.getItemId()) {
            case R.id.edit:
                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(MainActivity.this);
                LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
                View renameView = inflater.inflate(R.layout.rename_window,null);
                alertBuilder.setView(renameView);
                final EditText rename_file = (EditText) renameView.findViewById(R.id.file_rename);
                String [] split = fileList.get(info.position).getName().split("\\.");
                rename_file.setText(split[0]);
                rename_file.setFilters(new InputFilter[] {
                        (source, start, end, dest, dstart, dend) -> {
                            if(source.toString().matches("[a-zA-Zа-яА-Я-0-9]+")){
                                return source;
                            }
                            return "";
                        }
                });
                alertBuilder
                        .setTitle("Введите новое имя файла")
                        .setCancelable(false)
                        .setPositiveButton("ok",(dialog,id)->{
                            if(rename_file.getText().length()>0){
                                if(rename(fileList.get(selected).getAbsolutePath(),String.valueOf(rename_file.getText()))){
                                    Toast.makeText(MainActivity.this,"Файл успешно переименован",Toast.LENGTH_SHORT).show();
                                    loadPage(path);
                                }else Toast.makeText(MainActivity.this,"Некорректное имя файла",Toast.LENGTH_SHORT).show();
                            }else{
                                Toast.makeText(MainActivity.this,"Имя файла не может быть пустым",Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton("отмена",(dialog, which) -> {
                            dialog.cancel();
                        });
                AlertDialog alertDialog = alertBuilder.create();
                alertDialog.show();
                return true;
            case R.id.delete:
                if(selected!=Integer.MAX_VALUE){
                    AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
                    alert
                            .setMessage("Вы уверены, что хотите удалить этот файл?")
                            .setCancelable(false)
                            .setPositiveButton("ок",(dialog, id) -> {
                                if(delete(new File(path).getAbsolutePath())){
                                    Toast.makeText(MainActivity.this,"Файл успешно удалён",Toast.LENGTH_SHORT).show();
                                }else {
                                    Toast.makeText(MainActivity.this,"Ошибка удаления",Toast.LENGTH_SHORT).show();
                                }
                                loadPage(path);
                            })
                            .setNegativeButton("отмена", (dialog, which) -> {
                                dialog.cancel();
                            });
                    AlertDialog removeDialog = alert.create();
                    removeDialog.show();
                }
                return true;
            case R.id.file_info:
                if(selected!=Integer.MAX_VALUE){
                    String message = null;
                    if(fileList.get(selected).isDirectory()){
                        message = "Размер папки: "+folderSize(fileList.get(selected))+" KB"+"\n"+fileList.get(selected).listFiles().length+" "+
                        getWordAddition(fileList.get(selected).listFiles().length)+"\nДата создания: "+getCreationDate(fileList.get(selected));
                    }else message = "Размер файла: "+fileList.get(selected).length()/1024+" KB"+"\nДата создания: "+getCreationDate(fileList.get(selected));
                    AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
                    alert
                            .setTitle(fileList.get(selected).getName())
                            .setMessage(message)
                            .setIcon(getIcon(fileList.get(selected)))
                            .setCancelable(true)
                            .setPositiveButton("ок",(dialog, id) -> {
                                dialog.cancel();
                            });
                    AlertDialog removeDialog = alert.create();
                    removeDialog.show();
                }
                return true;
            default:
                return super.onContextItemSelected(item);
        }

    }

    private String getCreationDate(File file) {
        Date date = new Date(file.lastModified());
        String day,month;
        if(date.getDate()<=9){
            day = "0"+date.getDate();
        }else day = date.getDate()+"";

        if(date.getMonth()<=9){
            month = "0"+(date.getMonth()+1);
        }else  month = (date.getMonth()+1)+"";

        return day+"."+month+"."+(date.getYear()+1900);
    }


    @SuppressLint("SdCardPath")
    public List<File> loadFiles(String path){
            return Arrays.asList(new File(path).listFiles());
    }

    public void loadPage(String filePath){
        TextView info = setTextView(R.id.info, getMemoryInfo(getTotalSize(), getAvailableSize()));
        TextView no_objects = setTextView(R.id.no_objects, "Пусто");
        int image;
        settings = setButton(R.id.settings,R.mipmap.settings);
        search = setButton(R.id.search,R.mipmap.search);
        settingsPage = findViewById(R.id.settingsPage);
        mainPage = findViewById(R.id.main_Page);
        settingsPage.setVisibility(View.GONE);
        fileList = loadFiles(filePath);
        author = setTextView(R.id.author,"Об авторе");
        themeSwitch = findViewById(R.id.theme_switch);
        commercial = findViewById(R.id.commercial_switch);
        themeSwitch.setChecked(color);
        commercial.setChecked(ad);
        setThemeColor(color);
        fileListView = setFileList(R.id.listFiles, fileList);
        gridListView = setGridList(R.id.gridFiles,fileList);


        if(flag) {
            gridListView.setVisibility(View.GONE);
            fileListView.setVisibility(View.VISIBLE);
            image = R.mipmap.tile;
            if(loadFiles(filePath).size()>0) {
                registerForContextMenu(fileListView);
                no_objects.setVisibility(View.GONE);
            }else {
                no_objects.setVisibility(View.VISIBLE);
            }
        }else {
            fileListView.setVisibility(View.GONE);
            gridListView.setVisibility(View.VISIBLE);
            image = R.mipmap.list;
            if(loadFiles(filePath).size()>0) {
                registerForContextMenu(gridListView);
                no_objects.setVisibility(View.GONE);
            }else {
                no_objects.setVisibility(View.VISIBLE);
            }
        }
        display = setButton(R.id.display, image);
        int icon;
        if(size){
            icon = R.mipmap.big;
        }else icon = R.mipmap.standart;
        displaySize = setButton(R.id.displaySize,icon);
        TextView pathText = setTextView(R.id.path,(path.replace(root.getPath(),"Внутреннее хранилище")));
        setSDcardData();

    }

    private String getMemoryInfo(long total,long available){
        if(total / (1024 * 1024 * 1024) == 0){
            return "Память\n"+getAvailableSize()/(1024*1024)+"MB/"+getTotalSize()/(1024*1024)+"MB";
        }else return "Память\n"+getAvailableSize()/(1024*1024*1024)+"GB/"+getTotalSize()/(1024*1024*1024)+"GB";
    }

    private TextView setTextView(int info, String text) {
        TextView textView = findViewById(info);
        textView.setText(text);
        return textView;
    }


    public Button setButton(int id, int image){
        Button btn = findViewById(id);
        btn.setBackgroundResource(image);
        return btn;
    }

    private GridView setGridList(int gridFiles, List<File> fileList) {
        GridView gridView = findViewById(gridFiles);
        gridView.setAdapter(new GridFileAdapter(this,fileList,size,color));
        return gridView;
    }

    public ListView setFileList(int id, List<File> list){
        ListView listView = findViewById(id);
        FileListAdapter adapter = new FileListAdapter(this, android.R.layout.simple_list_item_2,list,size,color);
        listView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        return listView;
    }

    public static long getAvailableSize(){

            File sdcardDir = Environment.getExternalStorageDirectory();
            StatFs sf = new StatFs(sdcardDir.getPath());
            long bSize = sf.getBlockSize();
            long availBlocks = sf.getAvailableBlocks();

            return bSize * availBlocks;

    }

    public static long getTotalSize(){
            File sdcardDir = Environment.getExternalStorageDirectory();
            StatFs sf = new StatFs(sdcardDir.getPath());
            long bSize = sf.getBlockSize();
            long bCount = sf.getBlockCount();

            return bSize * bCount;
    }

    public static long folderSize(File directory) {
        long length = 0;
        for (File file : directory.listFiles()) {
            if (file.isFile())
                length += file.length()/1024;
            else
                length += folderSize(file);
        }
        return length;
    }

    @TargetApi(Build.VERSION_CODES.Q)
    public void openFile(File file){
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri uri = Uri.parse("file://" + file.getAbsolutePath());
        String type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(uri.toString()));
        intent.setDataAndType(uri, type == null ? "*/*" : type);
        startActivity(intent);
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


    public int getIcon(File file){
        int id = 0;
        if(file.getName().endsWith(".txt")){
            id = (R.mipmap.text);
        }else if(file.getName().endsWith(".mp3") | file.getName().endsWith(".mp4")){
            id = (R.mipmap.music);
        }else if(file.getName().endsWith(".jpg") | file.getName().endsWith(".png") | file.getName().endsWith(".jpeg") | file.getName().endsWith(".gif")){
            id = (R.mipmap.image);
        }else if(file.getName().endsWith(".zip")){
            id = (R.mipmap.zip);
        }else if(file.getName().endsWith(".pdf") | file.getName().endsWith(".rtf")){
           id = (R.mipmap.pdf);
        }else if(file.isDirectory()){
            id = (R.mipmap.dir);
        }else id = (R.mipmap.empty);
        return id;
    }

    @Override
    public void onBackPressed() {
        if(mainPage.getVisibility() == View.VISIBLE) {
            if (!path.equals(root.getAbsolutePath())) {
                path = new File(path).getParent();
                loadPage(path);
                fileListView.smoothScrollToPositionFromTop(selected, 0);
            } else {
                super.onBackPressed();
            }
        }else {
            mainPage.setVisibility(View.VISIBLE);
            settingsPage.setVisibility(View.GONE);
            loadPage(path);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    public void setOnClick(){

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater li = LayoutInflater.from(MainActivity.this);
                View searchWindow = li.inflate(R.layout.search,null);
                AlertDialog.Builder searchBuilder = new AlertDialog.Builder(MainActivity.this);
                searchBuilder.setView(searchWindow);
                final EditText fileSearch = (EditText) searchWindow.findViewById(R.id.file_name_search);
                fileSearch.setHint("введите имя файла");
                String title = "Поиск";
                String ok = "Ок";
                String cancel = "Отмена";

                searchBuilder
                        .setTitle(title)
                        .setCancelable(false)
                        .setPositiveButton(ok,
                                (dialog, id) -> {
                                    if (searchFile(fileSearch.getText().toString(),root)!=null) {
                                        File searchFile = searchFile(fileSearch.getText().toString(),root);
                                        path = searchFile.getParent();
                                        List<File> list = new ArrayList<>();
                                        list.add(searchFile);
                                        loadPage(path);
                                        if (flag) {
                                            setFileList(R.id.listFiles, list);
                                        } else
                                            setGridList(R.id.gridFiles, list);
                                    }else Toast.makeText(MainActivity.this,"Файл не найден",Toast.LENGTH_SHORT).show();
                                })
                        .setNegativeButton(cancel,
                                (dialog, id) -> dialog.cancel());

                AlertDialog alertDialog = searchBuilder.create();

                alertDialog.show();
            }
        });

        commercial.setOnClickListener(v -> {
            ad = !ad;
            Log.d("cccc",ad+"");
            saveAppSetting();
        });

        themeSwitch.setOnClickListener(v -> {
            color = !color;
            setThemeColor(color);
            Log.d("theme_switch",color+"");
            saveAppSetting();
        });


        settings.setOnClickListener(v -> {
            mainPage.setVisibility(View.GONE);
            settingsPage.setVisibility(View.VISIBLE);
        });


        displaySize.setOnClickListener(v -> {
            size =!(size);
            loadPage(path);
        });

        display.setOnClickListener(v -> {
            flag=!(flag);
            loadPage(path);
        });

        fileListView.setOnItemClickListener((parent, view, position, id) -> {
            view.setBackgroundColor(Color.argb(1,103, 203, 253));
            try {
                path = fileList.get(position).getAbsolutePath();
                selected = position;
                loadPage(path);
            }catch (NullPointerException e){

                openFile(fileList.get(position));
                path = new File(path).getParent();
            }
        });

        gridListView.setOnItemClickListener((parent, view, position, id) -> {
            try {
                path = fileList.get(position).getAbsolutePath();
                selected = position;
                loadPage(path);
            }catch (NullPointerException e){

                openFile(fileList.get(position));
                path = new File(path).getParent();
            }
        });


    }

    public File searchFile(String name,File file) {
        File result = null;
        File[] dirlist  = file.listFiles();

        if (dirlist != null) {
            for(int i = 0; i < dirlist.length; i++) {
                if(dirlist[i].isDirectory()) {
                    result = searchFile(name, dirlist[i]);
                    if (result!=null)
                        break;
                } else if(dirlist[i].getName().startsWith(name)) {
                    return dirlist[i];
                }
            }
        }
        return result;
    }

    private void setThemeColor(boolean checked){
        LinearLayout all = findViewById(R.id.all);
        TextView procent = findViewById(R.id.procent);
        TextView info = findViewById(R.id.info);
        TextView path = findViewById(R.id.path);
        TextView noObjects = findViewById(R.id.no_objects);
        TextView theme = findViewById(R.id.theme);
        TextView commercial = findViewById(R.id.commercial);
        if (checked){
            all.setBackgroundColor(Color.BLACK);
            procent.setTextColor(Color.WHITE);
            info.setTextColor(Color.WHITE);
            path.setTextColor(Color.BLACK);
            noObjects.setTextColor(Color.WHITE);
            author.setTextColor(Color.WHITE);
            theme.setTextColor(Color.WHITE);
            commercial.setTextColor(Color.WHITE);
        }
        else{
            all.setBackgroundColor(Color.WHITE);
            procent.setTextColor(Color.BLACK);
            info.setTextColor(Color.BLACK);
            path.setTextColor(Color.WHITE);
            noObjects.setTextColor(Color.BLACK);
            author.setTextColor(Color.BLACK);
            theme.setTextColor(Color.BLACK);
            commercial.setTextColor(Color.BLACK);
        }
        fileListView = setFileList(R.id.listFiles, fileList);
        gridListView = setGridList(R.id.gridFiles,fileList);
    }

    public boolean delete(String path) {
        this.path = new File(path).getParent();
        return new File(path).delete();
    }

    public boolean rename(String path, String newPath){
        File from = new File(path);
        String[] split = from.getName().split("\\.");
        File to;
        if(split.length>1) {
            String ext = split[1];
            to = new File(from.getParent(), newPath + "." + ext);
        }else {
            to = new File(from.getParent(), newPath);
        }
        return from.renameTo(to);
    }

    @TargetApi(Build.VERSION_CODES.O)
    private void setSDcardData(){
        int progress;
        ProgressBar memory = findViewById(R.id.bar);
        if((getTotalSize()/(1024*1024*1024))==0){
            long min = ((getTotalSize()/(1024*1024)) - (getAvailableSize()/(1024*1024)));
            long max = (getTotalSize()/(1024*1024));
            long onePros = max / 100;
            progress = Math.toIntExact(onePros * min);
        }else {
            double min = ((getTotalSize()/(1024*1024*1024)) - (getAvailableSize()/(1024*1024*1024)));
            double max = (getTotalSize()/(1024*1024*1024));
            double onePros = max / 100;
            progress = (int) (onePros * min);
        }
        memory.setProgress(progress);
        memory.getProgressDrawable().setColorFilter(Color.DKGRAY, PorterDuff.Mode.SRC_IN);
        TextView procent = findViewById(R.id.procent);
        procent.setText(progress+"%");
    }

    private void saveAppSetting(){
        Log.d("save_color",color+"");
        app_setings.edit().putBoolean("theme",color).apply();
        Log.d("save_commercial",ad+"");
        app_setings.edit().putBoolean("commercial",ad).apply();
        Log.d("save_size",size+"");
        app_setings.edit().putBoolean("size",size).apply();
    }

    private void loadAppSetting(){
        color = app_setings.getBoolean("theme",false);
        size = app_setings.getBoolean("size",true);
        ad = app_setings.getBoolean("commercial",false);
        Log.d("load_color",color+"");
        Log.d("save_size",size+"");
        Log.d("save_commercial",ad+"");
    }


    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        saveAppSetting();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}