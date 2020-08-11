package utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * @author: kascas
 * @date: 2020/8/10-20:20
 * @description:
 */
public class FileIO {
    
    public static List<String> fileReader(String file) {
        FileReader fr = null;
        BufferedReader br = null;
        String line = null;
        ArrayList<String> strList = new ArrayList<>();
        try {
            fr = new FileReader(file);
            br = new BufferedReader(fr);
            while ((line = br.readLine()) != null) {
                strList.add(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (br != null)
                    br.close();
                if (fr != null)
                    fr.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return strList;
    }
    
    public static void fileWriter(String file, List<String> strList) {
        FileWriter fw = null;
        BufferedWriter bw = null;
        try {
            fw = new FileWriter(file);
            bw = new BufferedWriter(fw);
            for (String str : strList)
                bw.write(str + "\n");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (bw != null) {
                    bw.flush();
                    bw.close();
                }
                if (fw != null)
                    fw.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}