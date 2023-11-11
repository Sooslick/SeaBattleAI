package ru.sooslick.seabattle.job;

import ru.sooslick.seabattle.ai.AiHeatData;
import ru.sooslick.seabattle.entity.SeaBattlePosition;

import javax.imageio.ImageIO;
import javax.imageio.stream.FileImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Visualizator {

    public static void main(String[] args) throws IOException, NoSuchAlgorithmException {
        String dir = args.length > 0 ? args[0] : "aiData";
        String out = args.length > 1 ? args[1] : "output/";

        // read
        AiHeatData.init(dir);
        double[][] mults = new double[10][10];
        double maxMult = 0;
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                double cm = Math.max(AiHeatData.getMultiplier(new SeaBattlePosition(i, j)) - 1, 0);
                mults[i][j] = cm;
                if (cm > maxMult)
                    maxMult = cm;
            }
        }

        // write
        double normalizer = 255 / Math.max(maxMult, 0.001);
        BufferedImage img = new BufferedImage(300, 300, BufferedImage.TYPE_INT_RGB);
        Graphics g = img.createGraphics();
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                g.setColor(calcColor((int) (mults[i][j] * normalizer)));
                int x = j * 30;
                int y = i * 30;
                g.fillRect(x, y, 30, 30);
            }
        }

        // create image
        File outDir = new File(out);
        if (outDir.isFile())
            throw new IllegalArgumentException("bad output dir");
        outDir.mkdirs();

        // read last image
        File[] files = outDir.listFiles();
        assert files != null;
        File lastSaved = files[files.length - 1];

        // compare with new image
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(img, "jpg", baos);
        String md51 = new String(MessageDigest.getInstance("MD5").digest(Files.readAllBytes(lastSaved.toPath())));
        String md52 = new String(MessageDigest.getInstance("MD5").digest(baos.toByteArray()));
        if (md51.equalsIgnoreCase(md52)) {
            System.out.println("No changes in data");
            return;
        }
        FileImageOutputStream fios = new FileImageOutputStream(new File(out + System.currentTimeMillis() + ".jpg"));
        ImageIO.write(img, "jpg", fios);
    }

    private static Color calcColor(int normalValue) {
        int g = Math.max(255 - normalValue, 0);
        int r = Math.min(normalValue, 255);
        return new Color(r, g, 0);
    }
}
