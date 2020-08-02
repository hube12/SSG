package kaptainwutax.ssg;

import kaptainwutax.featureutils.structure.Stronghold;
import kaptainwutax.featureutils.structure.generator.StrongholdGenerator;
import kaptainwutax.featureutils.structure.generator.piece.stronghold.PortalRoom;
import kaptainwutax.seedutils.lcg.LCG;
import kaptainwutax.seedutils.lcg.rand.JRand;
import kaptainwutax.seedutils.mc.MCVersion;
import kaptainwutax.seedutils.mc.pos.BPos;
import kaptainwutax.seedutils.mc.pos.CPos;
import kaptainwutax.seedutils.util.BlockBox;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

public class WorldSeedGenerator {

	private static final boolean DEBUG = false;
	private static final LCG RING_SKIP = LCG.JAVA.combine(4);

	public static void generate(BufferedReader reader, BufferedWriter writer, MCVersion version) throws IOException {
		JRand rand = new JRand(0L);
		AtomicInteger progress = new AtomicInteger();
		long startTime = System.nanoTime();

		reader.lines().parallel().forEach(s -> {
			String[] line = s.trim().split(Pattern.quote(" "));
			long structureSeed = Long.parseLong(line[0]);
			CPos _12eyeChunk = new CPos(Integer.parseInt(line[1]), Integer.parseInt(line[2]));
			CPos startChunk = new CPos(Integer.parseInt(line[3]), Integer.parseInt(line[4]));
			long rngSeed = RING_SKIP.nextSeed(structureSeed ^ LCG.JAVA.multiplier);

			if(DEBUG) {
				System.out.println("Structure seed " + structureSeed);
			}

			Collection<CPos> goodStarts = getGoodStarts(structureSeed, _12eyeChunk, startChunk, version);
			if(goodStarts.isEmpty())return; //No start in the area lands a 12 eye. ¯\_(ツ)_/¯
			int lastZero = getLastZero(rand, rngSeed); //The last value of n where nextInt(n) == 0.
			int lastX = goodStarts.stream().mapToInt(CPos::getX).max().getAsInt();
			int lastZ = goodStarts.stream().mapToInt(CPos::getZ).max().getAsInt();

			if(DEBUG) {
				System.out.println("Good one! " + goodStarts);
				System.out.println("Last zero " + lastZero + " / " + 3249);
			}

			for(long upperBits = 0; upperBits < 1L << 16; upperBits++) {
				long worldSeed = (upperBits << 48) | structureSeed;
				BiomeChecker source = new BiomeChecker(version, worldSeed);
				rand.setSeed(rngSeed, false);

				CPos start = source.getStrongholdStart(
						(startChunk.getX() << 4) + 8, (startChunk.getZ() << 4) + 8,
						Stronghold.VALID_BIOMES, rand, lastZero, lastX, lastZ);
				if(start == null || !goodStarts.contains(start))continue;

				BPos p = getPortalCenter(structureSeed, start, version);
				System.out.format("World seed %d /tp %d ~ %d\n", worldSeed, p.getX(), p.getZ());
			}

			onStructureSeedCompletion(startTime, progress);
		});
	}

	private static BPos getPortalCenter(long structureSeed, CPos start, MCVersion version) {
		StrongholdGenerator generator = new StrongholdGenerator(version);
		final BlockBox[] portalBB = new BlockBox[1];

		generator.generate(structureSeed, start.getX(), start.getZ(), piece -> {
			if(!(piece instanceof PortalRoom))return true;
			portalBB[0] = PortalFrame.getPortalBB((PortalRoom)piece);
			return false;
		});

		return new BPos(portalBB[0].minX + 1, 0, portalBB[0].minZ + 1);
	}

	private static Collection<CPos> getGoodStarts(long structureSeed, CPos eyeChunk, CPos startChunk, MCVersion version) {
		Collection<CPos> goodStarts = new HashSet<>();

		for(int ox = -13; ox <= 13; ox++) {
			for(int oz = -13; oz <= 13; oz++) {
				StrongholdGenerator generator = new StrongholdGenerator(version);
				CPos testStart = new CPos(startChunk.getX() + ox, startChunk.getZ() + oz);

				generator.generate(structureSeed, testStart.getX(), testStart.getZ(), piece -> {
					if(!(piece instanceof PortalRoom))return true;

					BlockBox chunkBB = new BlockBox(eyeChunk.getX() << 4, 0, eyeChunk.getZ() << 4,
							(eyeChunk.getX() << 4) + 15, 255, (eyeChunk.getZ() << 4) + 15);

					BlockBox portalBB = PortalFrame.getPortalBB((PortalRoom)piece);
					if(!portalBB.intersects(chunkBB))return false;

					for(Stronghold.Piece piece1: generator.pieceList) {
						if(piece1 == piece)continue;
						if(piece1.getBoundingBox().intersects(chunkBB))return false;
					}

					goodStarts.add(testStart);
					return false;
				});
			}
		}

		return goodStarts;
	}

	public static int getLastZero(JRand rand, long rngSeed) {
		rand.setSeed(rngSeed, false);
		int lastZero = 0;

		for(int i = 1; i < 3249; i++) {
			boolean b = rand.nextInt(i + 1) == 0;
			if(b)lastZero = i;
		}

		return lastZero;
	}

	public static void onStructureSeedCompletion(long startTime, AtomicInteger progress) {
		int i = progress.incrementAndGet();
		int total = 250389; //Maybe don't hardcode the line count... xD

		double seconds = (double)(System.nanoTime() - startTime) / 1000000000.0D;
		double speed = (double)i / seconds;
		double eta = (double)(total - i) / speed;
		System.err.format("Finished %d seeds out of %d in %fs. ETA %fs.\n", i, total, (float)seconds, (float)eta);
	}

}
