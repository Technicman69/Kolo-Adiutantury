package main.java;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;
import java.io.FileNotFoundException;

public class Utils {
    public static ArrayList<Student> wczytajPlik(File file)
    {
        try {
            int i = 0;
            Color colors[] = {Color.GREEN, Color.YELLOW, Color.BLUE, Color.RED};
            Scanner odczyt = new Scanner(file);
            ArrayList<Student> Lista = new ArrayList<>();
            while(odczyt.hasNextLine()) {
                String line = odczyt.nextLine();
                String[] nazwy = line.split(" ");
                String fullname = (nazwy[0] + " " + nazwy[1]);
                double weight = Double.parseDouble(nazwy[2]);
                Color color = colors[i%4];
                i++;
                Lista.add(new Student(fullname, weight, color));

            }
            return Lista;
        }
        catch (Exception e)
        {
            System.out.println("error");
            return new ArrayList<Student>();
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
