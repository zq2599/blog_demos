package com.bolingcavalry.grabpush;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * @author willzhao
 * @version 1.0
 * @description TODO
 * @date 2021/12/13 7:06
 */
public class Test {

    public static List<String> folderMethod1(String path) {
        List<String> paths = new LinkedList<>();

        File file = new File(path);

        if (file.exists()) {
            File[] files = file.listFiles();

            for (File f : files) {
                if (f.isDirectory()) {
                    System.out.println("文件夹:" + f.getAbsolutePath());
                } else {
                    System.out.println("文件:" + f.getAbsolutePath());

                    paths.add(f.getAbsolutePath());
                }
            }
        }

        return paths;
    }

    public static void main(String[] args) {
        List<String> list = folderMethod1("E:\\temp\\202112\\14\\liu");
        System.out.println(list);
    }

}
