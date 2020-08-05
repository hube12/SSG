package kaptainwutax.ssg;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

public class SSG {
    public static class Data {
        private final int posX;
        private final int posZ;
        private final long seed;
        private final double distance;

        public Data(long seed, int posX, int posZ) {
            this.posX = posX;
            this.posZ = posZ;
            this.seed = seed;
            this.distance = posX * posX + posZ * posZ;
        }

        public double getDistance() {
            return distance;
        }
        public int getPosX() {
            return posX;
        }
        public int getPosZ() {
            return posZ;
        }
        public long getSeed() {
            return seed;
        }

        @Override
        public String toString() {
            return "/tp @p " + posX + " ~ " + posZ +
                    " , seed: " + seed +
                    " , distance: " + distance;
        }
    }

    public static void main(String[] args) {
        List<Data> seeds = new ArrayList<>();
        InputStream in = WorldSeedGenerator.class.getResourceAsStream("all12eyes1.16.txt");
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        for (Object line : reader.lines().toArray()) {
            String[] lines = ((String) line).trim().split(Pattern.quote(" "));
            // World seed 8705982457764242402 /tp -308 ~ -2346
            seeds.add(new Data(Long.parseLong(lines[2]), Integer.parseInt(lines[4]), Integer.parseInt(lines[6])));
        }
        for (Data seed : seeds) {

        }
        seeds.sort(Comparator.comparingDouble(Data::getDistance));
        for (int i = 0; i < 10000; i++) {
            System.out.println(seeds.get(i));
        }
    }
}
