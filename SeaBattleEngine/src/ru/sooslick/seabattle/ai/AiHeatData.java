package ru.sooslick.seabattle.ai;

import org.apache.commons.io.FileUtils;
import ru.sooslick.seabattle.Log;
import ru.sooslick.seabattle.entity.SeaBattlePosition;
import ru.sooslick.seabattle.result.FieldResult;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Files;

public class AiHeatData implements Serializable {
    private final static long serialVersionUID = 1L;
    private final static String dataName = "heatmap.dat";
    private static String analyzeDir;
    private static String dataDir;
    private static AiHeatData instance;
    private static boolean saveEnable = true;

    private int total;
    private final int[][] scores;

    public static void init(String dir) {
        dataDir = dir;
        analyzeDir = dataDir + File.separator + "analyze";
        read();
    }

    public static void analyze() {
        File workDir = new File(analyzeDir);
        if (!workDir.exists()) {
            Log.warn("Cannot analyze: folder not exist");
            return;
        }
        try {
            Files.walk(workDir.toPath()).forEach(path -> {
                String content;
                try {
                    content = Files.readAllLines(path).get(0);
                } catch (Exception e) {
                    Log.warn("Error reading file " + path.toString() + ": " + e.getMessage());
                    return;
                }
                int row = 0;
                for (String line : content.split("\\.")) {
                    if (line.length() < 10)
                        continue;
                    for (int col = 0; col < 10; col++) {
                        if (line.charAt(col) == '1')
                            instance.scores[row][col]++;
                    }
                    row++;
                }
                instance.total++;
            });
        } catch (IOException e) {
            Log.warn("Error reading analyze directory");
            return;
        }
        try {
            FileUtils.cleanDirectory(workDir);
        } catch (IOException e) {
            Log.warn("Cannot clean directory after analyze, plz do it manually");
        }
        save();
    }

    public static void analyze(FieldResult fieldData) {
        instance.total++;
        int i = 0;
        for (FieldResult.Row row : fieldData.getRows()) {
            int j = 0;
            for (Integer col : row.getCols()) {
                if (col >= 2)
                    instance.scores[i][j]++;
                j++;
            }
            i++;
        }
        Log.info("Saving FieldResult to heatmap");
        save();
    }

    public static double getMultiplier(SeaBattlePosition position) {
        if (position.getCol() < 0 || position.getRow() < 0 || position.getCol() >= 10 || position.getRow() >= 10)
            return 1;
        return ((double) instance.scores[position.getRow()][position.getCol()] / instance.total) + 1;
    }

    private static void read() {
        File workDir = new File(dataDir);
        if (!workDir.exists()) {
            if (!workDir.mkdirs()) {
                Log.warn("Cannot create data folder! Head data will not be saved");
                saveEnable = false;
                instance = new AiHeatData();
                return;
            }
        }
        try {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(dataDir + File.separator + dataName));
            instance = (AiHeatData) ois.readObject();
            ois.close();
        } catch (Exception e) {
            Log.warn("Cannot deserialize data: " + e.getMessage());
            instance = new AiHeatData();
        }
    }

    private static void save() {
        if (!saveEnable)
            return;
        try {
            ObjectOutputStream ois = new ObjectOutputStream(new FileOutputStream(dataDir + File.separator + dataName));
            ois.writeObject(instance);
            ois.close();
            Log.info("Saved heat to file heatmap.dat");
        } catch (Exception e) {
            Log.warn("Cannot serialize data: " + e.getMessage());
        }
    }

    private AiHeatData() {
        total = 0;
        scores = new int[10][10];
        for (int i = 0; i < 10; i++)
            for (int j = 0; j < 10; j++)
                scores[i][j] = 0;
    }
}
