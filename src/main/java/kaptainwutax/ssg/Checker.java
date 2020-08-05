package kaptainwutax.ssg;

import javax.sound.sampled.Line;
import java.io.*;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Checker {

    public static void main(String[] args) throws IOException {
        File file = new File("nonpresent.txt");
        if (!file.exists()) {

            if (!file.createNewFile()) {
                System.out.println("File was not created");
                System.exit(-1);
            }
        }
        System.out.println(file.getAbsolutePath());
        InputStream in = WorldSeedGenerator.class.getResourceAsStream("validation.txt");
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        HashMap<Long,String>input=new HashMap<>();
        for (Object line : reader.lines().toArray()) {
            String[] lines=((String)line).trim().split(Pattern.quote(" "));
            input.put(Long.parseLong(lines[0]),((String)line));
        }
        HashMap<Long,String> track=new HashMap<>(input);

        InputStream in2 = WorldSeedGenerator.class.getResourceAsStream("final.txt");
        BufferedReader reader2 = new BufferedReader(new InputStreamReader(in2));
        HashMap<Long,String>full=new HashMap<>();
        for (Object line : reader2.lines().toArray()) {
            String[] lines=((String)line).trim().split(Pattern.quote(" "));
            Long numer=Long.parseLong(lines[2]);
            if (full.containsKey(numer)){
                //System.out.println("ALREADY CONTAINED "+ line);
                if (!full.get(numer).trim().split(Pattern.quote(" "))[6].equals(lines[6]) || !full.get(numer).trim().split(Pattern.quote(" "))[4].equals(lines[4])){
                    System.out.println("ERROR !! "+line+ "\n  "+full.get(numer));
                }
            }else{
                full.put(numer,((String)line));
            }
        }

        long count=0;
        System.out.println(full.size());
        System.out.println(input.size());
        long startTime = System.nanoTime();
        File out = new File("output.txt");
        if (!out.exists()) {

            if (!out.createNewFile()) {
                System.out.println("File was not created");
                System.exit(-1);
            }
        }
        System.out.println(out.getAbsolutePath());
        FileWriter fileOut = new FileWriter(out);
        for (Long worldseed : full.keySet()) {
            long struct=worldseed&0xFFFF_FFFF_FFFFL;
            if (input.containsKey(struct)){
                track.remove(struct);
                fileOut.write(full.get(worldseed)+"\n");


            }else{
               // System.out.println("WARNING the worldseed was not even a valid struct seed "+worldseed);
            }
            count++;
            if (count%10000==0){
                System.out.println(count/11000_000.0*100);
                printTime(startTime);
            }
        }
        fileOut.flush();
        fileOut.close();
        FileWriter fileWriter = new FileWriter(file);
        for (Long aLong : track.keySet()) {
           // System.out.println("MISSING "+aLong);
            fileWriter.write(aLong+"\n");
        }
    }

    private static void printTime(long start) {
        System.out.println((System.nanoTime() - start) / 1.0e9 + "s");
    }


}
