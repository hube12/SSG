package kaptainwutax.ssg;

import kaptainwutax.seedutils.mc.MCVersion;

import java.io.*;

public class Main {
    public static void main(String[] args) throws IOException {
        WorldSeedGenerator.generate(
                new BufferedReader(new FileReader(new File("output2_16.txt"))),
                new BufferedWriter(new FileWriter(new File("output3_16.txt"))),
                MCVersion.v1_16
        );
    }
}
