package com.twd.videoplayer2;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.media.MediaPlayer;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ListView videoList;
    private VideoView videoView;
    private FrameLayout videoContent;
    private TextView noTextView;
    private DrawerLayout drawerLayout;
    private boolean isDrawerOpen = false;
    List<String> Items = new ArrayList<>();
    HashMap<String,String> videoPaths = new HashMap<>();
    Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onResume() {
        super.onResume();
        drawerLayout = findViewById(R.id.drawerLayout);
        videoList = findViewById(R.id.video_list);
        videoView = findViewById(R.id.video_view);
        videoContent = findViewById(R.id.video_content);
        noTextView = findViewById(R.id.no_video_text);
        checkPlayer();

        String usbPath = "/storage/usbotg"; // 替换为您的U盘路径
        File usbDirectory = new File(usbPath);
        FileReader fileReader = new FileReader();
        Items = fileReader.readVideoFiles(usbDirectory,Items);
        //showToast(String.valueOf(Items.size()));
        videoPaths = fileReader.getAbsolutionPaths(usbDirectory,Items,videoPaths);

        drawerLayout.setDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {

            }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {
                //showToast("抽屉打开");
                videoList.requestFocus();
            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {

            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });

        ItemAdapter adapter = new ItemAdapter(this,Items);
        videoList.setAdapter(adapter);
        videoList.setSelection(0);
        videoList.requestFocus();
        // 设置ListView的按键监听器
        videoList.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    //showToast("onKey: KeyEvent.ACTION_DOWN");
                    if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                        //showToast("onKey: KeyEvent.KEYCODE_DPAD_UP");
                        // 向上方向键，移动焦点到上一个列表项
                        int previousPosition = videoList.getSelectedItemPosition() - 1;
                        if (previousPosition >= 0) {
                            videoList.setSelection(previousPosition);
                            return true;
                        }
                    } else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                        //showToast("onKey: KeyEvent.KEYCODE_DPAD_DOWN");
                        // 向下方向键，移动焦点到下一个列表项
                        int nextPosition = videoList.getSelectedItemPosition() + 1;
                        if (nextPosition < videoList.getCount()) {
                            videoList.setSelection(nextPosition);
                            return true;
                        }
                    }
                }
                return false;
            }
        });
        videoList.setOnItemClickListener((parent, view, position, id) -> {
            String videoName = Items.get(position);
            String videoPath = videoPaths.get(videoName);
            //showToast("videoName = "+videoName+",videoPath = "+videoPath);
            playVideo(videoPath);
            noTextView.setVisibility(View.GONE);
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    drawerLayout.closeDrawer(GravityCompat.START);
                    isDrawerOpen = false;
                }
            },1000);
        });
        videoList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //showToast("当前在 " + position);
                adapter.setFocusedItem(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void checkPlayer(){
        if (videoView.isPlaying()){
            noTextView.setVisibility(View.GONE);
        }else {
            noTextView.setVisibility(View.VISIBLE);
        }
    }

    private void playVideo(String videoPath){
        String Path =  videoPath;
        //showToast(Path);
        videoView.setVideoURI(Uri.parse(Path));
        videoView.setOnPreparedListener(MediaPlayer::start);
        videoView.setOnCompletionListener(mp -> {
            mp.seekTo(0);
            mp.start();
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        int currentPosition =videoView.getCurrentPosition();
        int newPosition;
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_MENU:
                    drawerLayout.openDrawer(GravityCompat.START);
                    isDrawerOpen = true;
                    return true;
                case KeyEvent.KEYCODE_BACK:
                    if (isDrawerOpen) {
                        drawerLayout.closeDrawer(GravityCompat.START);
                        isDrawerOpen = false;
                        return true;
                    } else return super.onKeyDown(keyCode, event);
                case KeyEvent.KEYCODE_DPAD_LEFT:
                    if (!isDrawerOpen) {
                        if (noTextView.getVisibility()!=View.VISIBLE){
                            //后退5秒
                            newPosition = currentPosition - 5000;
                            if (newPosition < 0 )newPosition = 0;
                            videoView.seekTo(newPosition);
                            showToast("后退");
                        }
                        return true;
                    }else return super.onKeyDown(keyCode,event);
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                    if (!isDrawerOpen){
                        if (noTextView.getVisibility()!=View.VISIBLE){
                            //前进5秒
                            newPosition = currentPosition + 5000;
                            int duration = videoView.getDuration();
                            if (newPosition > duration) newPosition = duration;
                            videoView.seekTo(newPosition);
                            showToast("快进");
                        }
                        return true;
                    } else return super.onKeyDown(keyCode, event);
                case KeyEvent.KEYCODE_DPAD_CENTER:
                case KeyEvent.KEYCODE_ENTER:
                    if (!isDrawerOpen) {
                        if (videoView.isPlaying()){
                            videoView.pause();
                            showToast("暂停");
                        } else if(noTextView.getVisibility()!=View.VISIBLE){
                            videoView.start();
                            showToast("继续播放");
                        }
                        return true;
                    }else return super.onKeyDown(keyCode, event);
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    private void showToast(String text){
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.toast_custom_layout,null);
        TextView textView = layout.findViewById(R.id.toast_text);
        textView.setText(text);
        Toast customToast =new Toast(getApplicationContext());
        customToast.setView(layout);
        customToast.setDuration(Toast.LENGTH_SHORT);
        customToast.show();
    }

    public class ItemAdapter extends ArrayAdapter<String> {
        private LayoutInflater inflater;
        private int focusedItem = 0;
        public ItemAdapter(@NonNull Context context, List<String> Items) {
            super(context, 0,Items);
            inflater =LayoutInflater.from(context);
        }
        public void setFocusedItem(int position){
            focusedItem = position;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View itemView =convertView;
            if (itemView == null){
                itemView = inflater.inflate(R.layout.list_item_layout,parent,false);
            }
            TextView video_name =itemView.findViewById(R.id.video_name);
            String videoName = getItem(position);
            video_name.setTextColor(getResources().getColor(R.color.white));
            video_name.setText(videoName);

            if (position == focusedItem){
                itemView.setBackgroundColor(getResources().getColor(R.color.focused));
            }else {
                itemView.setBackgroundColor(Color.TRANSPARENT);
            }
            return itemView;
        }
    }
}