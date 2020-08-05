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
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Checker {
    private static final LCG RING_SKIP = LCG.JAVA.combine(4);

    public static void main(String[] args) throws IOException {
        MCVersion version = MCVersion.v1_16;
        File file = new File("valid.txt");
        if (!file.exists()) {

            if (!file.createNewFile()) {
                System.out.println("File was not created");
                System.exit(-1);
            }
        }
        System.out.println(file.getAbsolutePath());
        FileWriter fileWriter = new FileWriter(file);
        long startTime = System.nanoTime();
        InputStream in = WorldSeedGenerator.class.getResourceAsStream("/input12eyes.txt");
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        List<String> eyes = reader.lines().collect(Collectors.toList());
        for (String s : eyes) {
            String[] line = s.trim().split(Pattern.quote(" "));
            long structureSeed = Long.parseLong(line[0]);
            CPos _12eyeChunk = new CPos(Integer.parseInt(line[1]), Integer.parseInt(line[2]));
            CPos startChunk = new CPos(Integer.parseInt(line[3]), Integer.parseInt(line[4]));
            long rngSeed = RING_SKIP.nextSeed(structureSeed ^ LCG.JAVA.multiplier);


            Collection<CPos> goodStarts = getGoodStarts(structureSeed, _12eyeChunk, startChunk, version);
            if (goodStarts.isEmpty()) {
                continue;
            }
            fileWriter.write(s + "\n");
            printTime(startTime);
        }
    }

    private static void printTime(long start) {
        System.out.println((System.nanoTime() - start) / 1.0e9 + "s");
    }

    private static Collection<CPos> getGoodStarts(long structureSeed, CPos eyeChunk, CPos startChunk, MCVersion version) {
        Collection<CPos> goodStarts = new HashSet<>();

        for (int ox = -13; ox <= 13; ox++) {
            for (int oz = -13; oz <= 13; oz++) {
                StrongholdGenerator generator = new StrongholdGenerator(version);
                CPos testStart = new CPos(startChunk.getX() + ox, startChunk.getZ() + oz);

                generator.generate(structureSeed, testStart.getX(), testStart.getZ(), piece -> {
                    if (!(piece instanceof PortalRoom)) return true;

                    BlockBox chunkBB = new BlockBox(eyeChunk.getX() << 4, 0, eyeChunk.getZ() << 4, (eyeChunk.getX() << 4) + 15, 255, (eyeChunk.getZ() << 4) + 15);

                    BlockBox portalBB = PortalFrame.getPortalBB((PortalRoom) piece);
                    if (!portalBB.intersects(chunkBB)) return false;

                    for (Stronghold.Piece piece1 : generator.pieceList) {
                        if (piece1 == piece) continue;
                        if (piece1.getBoundingBox().intersects(chunkBB)) return false;
                    }

                    goodStarts.add(testStart);
                    return false;
                });
            }
        }

        return goodStarts;
    }
}
