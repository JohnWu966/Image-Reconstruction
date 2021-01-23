package com.company;
import org.imgscalr.*;

import javax.imageio.ImageIO;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.awt.Graphics2D;
import javax.imageio.*;

//resizing the image is done by the imgscalr library, available at https://github.com/rkalla/imgscalr
public class Main {


    public static void main(String[] args) {
        final int SquareLength = 100;
        BufferedImage img = null;
        try {
            File DebugFile = new File("public/pictures/image.png");
//            System.out.println("Reading Image at: "+ DebugFile.getAbsolutePath());
            img = ImageIO.read(new File("public/pictures/image.png"));

        } catch (Exception e) {
            System.out.println("Something went wrong");
        }

        int width  = img.getWidth();
        int height = img.getHeight();
        int higher;
        if (width < height){higher = width;}
        else{higher = height;}

        //if a small image is provided, scale the image such that the smaller dimension is at least 1000px
        if (higher < 500){
            if (higher < 100){           //lower is between 0 and 200
                height *= 5;
                width *= 5;
            }else if (higher < 250){     //lower is between 100 and 250
                height *= 3;
                width *= 3;
            }else {
                height *= 2;
                width *= 2;
            }

        }
        int targetWidth = (width/SquareLength) * SquareLength;      //round the width to the closest multiple of 100
        if (width % SquareLength >SquareLength/2){
            targetWidth += SquareLength;
        }
        int targetHeight = (height/SquareLength) * SquareLength;      //round the height to the closest multiple of 100
        if (height % SquareLength >SquareLength/2){
            targetHeight+= SquareLength;
        }

        img = Scalr.resize(img, Scalr.Method.QUALITY, Scalr.Mode.FIT_EXACT,targetWidth,targetHeight);
        try {
            // retrieve image
            File outputfile = new File("public/pictures/resizedImage.png");
            ImageIO.write(img, "png", outputfile);
        } catch (IOException e) {
            System.out.println("IO Exception" + e);
        }

        try {
            img = ImageIO.read(new File("public/pictures/filler.png"));
            File outputfile = new File("public/pictures/reConstitutedImage.png");
            ImageIO.write(img, "png", outputfile);
        } catch (Exception e){
            System.out.println("Error when copying over filler image");
        }
    }

}
