package main.java;
import java.awt.*;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Scanner;

public class Utils {
    public static ArrayList<Student> wczytajPlik(File file)
    {
        try {
            int i = 0;
//            Color[] colors = {
//                    new Color(0x7AF5C6),
//                    new Color(0xF5EB98),
//                    new Color(0xAD98F5),
//                    new Color(0xF5A798),
//                    new Color(0x8E89A1),
//                    new Color(0x687872),
//            };
            Color[] colors = {
                    new Color(0x25F591),
                    new Color(0xF5D507),
                    new Color(0x816EF5),
                    new Color(0xF5412A),
                    new Color(0x2387F1),
                    new Color(0x228053),
            };
            Scanner odczyt = new Scanner(file, StandardCharsets.UTF_8);
            ArrayList<Student> Lista = new ArrayList<>();
            while(odczyt.hasNextLine()) {
                String line = odczyt.nextLine();
                String[] nazwy = line.split(" ");
                StringBuilder fullname = new StringBuilder(nazwy[0]);
                for (int j = 1; j < nazwy.length-1; j++)
                {
                    fullname.append(" ").append(nazwy[j]);
                }
                double weight = Double.parseDouble(nazwy[nazwy.length-1]);
                Color color = colors[i%colors.length];
                i++;
                Lista.add(new Student(fullname.toString(), weight, color));
            }
            return Lista;
        }
        catch (Exception e)
        {
            System.out.println("error");
            return new ArrayList<>();
        }

    }
    public static void main(String[] args) {
        /*ArrayList<Student> lista = Utils.wczytajPlik();
        for(Student student : lista)
        {
            System.out.println(student.fullName + "\n" + student.weight + "\n" +student.color);
        }

         */
    }
}
