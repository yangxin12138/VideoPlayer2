package com.twd.videoplayer2;

import java.io.File;
import java.util.HashMap;
import java.util.List;

public class FileReader {
    HashMap<String,String> videoPaths = new HashMap<>();
    public List<String> readVideoFiles(File directory, List<String> videoItems){
        int originalSize = videoItems.size(); // 记录原始列表大小
        File[] files = directory.listFiles();
        if (files != null){
            for (File file : files){
                if (file.isDirectory()){
                    readVideoFiles(file,videoItems); //递归读取子目录
                }else {
                    String fileName = file.getName();
                    if (isVideoFile(fileName)){
                        if (!videoItems.contains(fileName)){
                            videoItems.add(fileName);
                        }
                    }
                }
            }
        }
        return videoItems;
    }

    public HashMap<String, String> getAbsolutionPaths(File directory,List<String> videoItems,HashMap<String,String> videoPaths){
        File[] files = directory.listFiles();
        if (files != null){
            for (File file : files){
                if (file.isDirectory()){
                    getAbsolutionPaths(file, videoItems, videoPaths); // 递归读取子目录
                }else {
                    String fileName = file.getName();
                    if (isVideoFile(fileName)){
                        String path = file.getAbsolutePath();

                        videoPaths.put(fileName,path);
                    }
                }
            }
        }
        return videoPaths;
    }

    private static boolean isVideoFile(String fileName){
        return fileName.endsWith(".mp4") || fileName.endsWith(".avi") || fileName.endsWith(".mkv");
    }
}
