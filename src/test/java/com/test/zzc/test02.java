package com.test.zzc;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.Buffer;

/**
 * ImageIo 输出图片
 */
public class test02 {
    public static void main(String[] args) throws IOException {
//        ImageIO.write(image, "jpeg", new File("E://helloImage.jpeg"));
        File file = new File("D:\\zhangzechun\\Desktop\\getqrcodeimg (2).jpg");
        InputStream inputStream = new FileInputStream(file);
        BufferedImage bufferedImage = ImageIO.read(inputStream);

        int width = bufferedImage.getWidth(null);
        int heigh = bufferedImage.getHeight(null);
        JLabel jLabel = new JLabel(new ImageIcon(bufferedImage));
        JFrame jFrame = new JFrame();
        jFrame.getContentPane().add(jLabel,BorderLayout.CENTER);
        jFrame.setSize(width+100,heigh+100);
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jFrame.setLocationRelativeTo(null);
        jFrame.setVisible(true);
    }
}
