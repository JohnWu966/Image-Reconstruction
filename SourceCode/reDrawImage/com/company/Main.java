package com.company;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;
import javax.imageio.ImageIO;

import org.imgscalr.Scalr;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;


public class Main {

    static Graphics canvas;
    static final int SquareLength = 25;
    //multithreading code adapted from https://www.tutorialspoint.com/java/java_multithreading.htm
    static class artThread extends Thread {
        private Thread t;

        Random ran = new Random();
        static final int SquareLength = Main.SquareLength;
        private String Name;
        int y;

        //given a certain color hexcode, return a url to an image that is predominately that color.
        String getImage(String hexCode) throws IOException {
            int x = ran.nextInt(10);
            try{
                while(1==1){
                    Document doc = Jsoup.connect("https://www.designspiration.com/color/" + hexCode).get();
                    Element img = doc.select("img").get(x);
                    String src = img.attr("src");
//                   System.out.println(src);
                    if (src.length()>70){
                        return src;
                    }

                }


            }catch (Exception e){
                return ("https://www.colorhexa.com/"+hexCode+".png");
            }

        }

        //gets the color of a given pixel of pixelatedImage
        String getColor(int x, int y) throws IOException {
//            BufferedImage image = ImageIO.read(new File("pixelatedImage.png"));
            BufferedImage image = ImageIO.read(new File("public/pictures/pixelatedImage.png"));
            Color color = new Color(image.getRGB(x, y));
            String redHex = String.format("%2s", Integer.toHexString(color.getRed())).replace(' ', '0');
            ;
            String greenHex = String.format("%2s", Integer.toHexString(color.getGreen())).replace(' ', '0');
            ;
            String blueHex = String.format("%2s", Integer.toHexString(color.getBlue())).replace(' ', '0');
            ;
//            System.out.println(String.format("DEBUG: X:%-5d Y:%-5d RGB:%s",x,y,redHex+greenHex+blueHex));
            return redHex + greenHex + blueHex;
        }

        artThread(String name, int y) {
            Name = name;
            System.out.println("Creating " + Name);
            this.y = y;
        }

        public void run() {
            try {
                drawRow();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void drawRow() throws IOException {
//            File input = new File("pixelatedImage.png");
            File input = new File("public/pictures/pixelatedImage.png");
            BufferedImage image = ImageIO.read(input);
            int width = image.getWidth();
            int height = image.getHeight();

            BufferedImage img1;



            int xCounter = 0;
            for (int x = 0; x < width; x += SquareLength) {
                String url = getImage(getColor(x,y));

                if (xCounter % 10 == 0){
                    String outputString = String.format("-6%s has completed %d %s",this.Name,x*100/width, "%");
                    System.out.println(this.Name + " has completed " + x*100 / width + " %");
                }
                xCounter++;





//                System.out.println("X:"+x+" Y:"+y+" "+ url);
                img1 = ImageIO.read(new URL(url));
                img1 = Scalr.resize(img1, Scalr.Method.QUALITY, Scalr.Mode.FIT_EXACT, SquareLength, SquareLength);
                canvas.drawImage(img1, x, y, null);
            }

        }
    }

    //
//}
    static void testParseImage() {
        BufferedImage image;
        int width;
        int height;
        try {
//            File input = new File("pixelatedImage.png");
            File input = new File("public/pictures/pixelatedImage.png");
            image = ImageIO.read(input);
            width = image.getWidth();
            height = image.getHeight();
            for (int y = 0; y < height / SquareLength; y++) {

                for (int x = 0; x < width / SquareLength; x++) {
                    Color color = new Color(image.getRGB(x * SquareLength, y * SquareLength));
                    String redHex = String.format("%2s", Integer.toHexString(color.getRed())).replace(' ', '0');
                    ;
                    String greenHex = String.format("%2s", Integer.toHexString(color.getGreen())).replace(' ', '0');
                    ;
                    String blueHex = String.format("%2s", Integer.toHexString(color.getBlue())).replace(' ', '0');
                    ;
                    String outputString = String.format("X:%-7dY:%-7d%-8s", x * SquareLength, y * SquareLength, redHex + greenHex + blueHex);
                    System.out.println(outputString);
                }
            }
        } catch (Exception e) {
            System.out.println("Error reading File");
            System.out.println(e);
        }
    }

    public static void main(String args[]) throws Exception {
//        File input = new File("pixelatedImage.png");
        File input = new File("public/pictures/pixelatedImage.png");
        BufferedImage image = ImageIO.read(input);
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage canvasImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        canvas = canvasImage.getGraphics();
        canvas.drawImage(image, 0, 0, null);


        ArrayList<artThread> threads = new ArrayList<>();
        for (int y = 0; y < height; y += SquareLength) {
            artThread thread = new artThread("Thread-" + y, y);
            thread.start();
            threads.add(thread);
        }

        for (int yCounter = 0; yCounter < height/SquareLength; yCounter++) {
            System.out.println("Waiting on Thread: "+yCounter*SquareLength);
            threads.get(yCounter).join();
        }

        try {
            // retrieve image
//            File outputfile = new File("reConstitutedImage.png");
            File outputfile = new File("public/pictures/reConstitutedImage.png");
            ImageIO.write(canvasImage, "png", outputfile);
        } catch (IOException e) {
            System.out.println("Something else went wrong");
        }

        System.out.println("Finished");
    }
}

