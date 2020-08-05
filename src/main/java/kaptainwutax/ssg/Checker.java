package kaptainwutax.ssg;

import kaptainwutax.featureutils.structure.Stronghold;
import kaptainwutax.featureutils.structure.generator.StrongholdGenerator;
import kaptainwutax.featureutils.structure.generator.piece.stronghold.PortalRoom;
import kaptainwutax.seedutils.lcg.LCG;
import kaptainwutax.seedutils.lcg.rand.JRand;
import kaptainwutax.seedutils.mc.MCVersion;
import kaptainwutax.seedutils.mc.pos.CPos;
import kaptainwutax.seedutils.util.BlockBox;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Checker {
    private static final LCG RING_SKIP = LCG.JAVA.combine(4);

    public static void main(String[] args) throws IOException {
        MCVersion version = MCVersion.v1_16;
        File file = new File("nonpresent.txt");
        if (!file.exists()) {

            if (!file.createNewFile()) {
                System.out.println("File was not created");
                System.exit(-1);
            }
        }
        System.out.println(file.getAbsolutePath());
        FileWriter fileWriter = new FileWriter(file);
        long startTime = System.nanoTime();
        InputStream in = WorldSeedGenerator.class.getResourceAsStream("/validation.txt");
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        Set<Long> eyes = reader.lines().map(el->{
            String[] line=el.trim().split(Pattern.quote(" "));
            return Long.parseLong(line[0]); // 258366710815118 77 -145 68 -153
        }).collect(Collectors.toSet());
        Set<Long> track=new HashSet<>(eyes);
        InputStream in2 = WorldSeedGenerator.class.getResourceAsStream("/final.txt");
        BufferedReader reader2 = new BufferedReader(new InputStreamReader(in));
        List<Long> full = reader.lines().map(el->{
            String[] line=el.trim().split(Pattern.quote(" "));
            return Long.parseLong(line[2]); // 3:World seed 60933461081917201 /tp 1751 ~ -1768
        }).collect(Collectors.toList());
        for (Long worldseed : full) {
            long struct=worldseed&0xFFFF_FFFF_FFFFL;
            if (eyes.contains(struct)){
                track.remove(struct);
            }else{
                System.out.println("WARNING the worldseed was not even a valid struct seed "+worldseed);
            }
        }
        for (Long aLong : track) {
            System.out.println("MISSING "+aLong);
        }

    }

    private static void printTime(long start) {
        System.out.println((System.nanoTime() - start) / 1.0e9 + "s");
    }


}
