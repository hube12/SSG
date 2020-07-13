package kaptainwutax.ssg;

import kaptainwutax.featureutils.structure.Stronghold;
import kaptainwutax.featureutils.structure.generator.StrongholdGenerator;
import kaptainwutax.featureutils.structure.generator.piece.stronghold.PortalRoom;
import kaptainwutax.seedutils.lcg.LCG;
import kaptainwutax.seedutils.lcg.rand.JRand;
import kaptainwutax.seedutils.mc.MCVersion;
import kaptainwutax.seedutils.mc.pos.CPos;
import kaptainwutax.seedutils.util.BlockBox;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.regex.Pattern;

public class WorldSeedGenerator {

	private static final LCG RING_SKIP = LCG.JAVA.combine(4);

	public static void generate(BufferedReader reader, BufferedWriter writer, MCVersion version) throws IOException {
		JRand rand = new JRand(0L);

		while(reader.ready()) {
			String[] line = reader.readLine().split(Pattern.quote(" "));
			long structureSeed = Long.parseLong(line[0]);
			CPos _12eyeChunk = new CPos(Integer.parseInt(line[1]), Integer.parseInt(line[2]));
			CPos startChunk = new CPos(Integer.parseInt(line[3]), Integer.parseInt(line[4]));
			long rngSeed = RING_SKIP.nextSeed(structureSeed ^ LCG.JAVA.multiplier);

			System.out.println("Structure seed " + structureSeed);

			Collection<CPos> goodStarts = getGoodStarts(structureSeed, _12eyeChunk, startChunk, version);
			if(goodStarts.isEmpty())continue; //No start in the area lands a 12 eye. ¯\_(ツ)_/¯
			int lastZero = getLastZero(rand, rngSeed); //The last value of n where nextInt(n) == 0.

			System.out.println("Good one! " + goodStarts);
			System.out.println("Last zero " + lastZero + " / " + 3249);

			for(long upperBits = 0; upperBits < 1L << 16; upperBits++) {
				long worldSeed = (upperBits << 48) | structureSeed;
				BiomeChecker source = new BiomeChecker(version, worldSeed);
				rand.setSeed(rngSeed, false);

				if(!source.hasGoodStrongholdStart(
						(startChunk.getX() << 4) + 8, (startChunk.getZ() << 4) + 8,
						Stronghold.VALID_BIOMES, goodStarts, rand, lastZero))continue;

				System.out.println("World seed " + worldSeed);
			}
		}
	}

	private static Collection<CPos> getGoodStarts(long structureSeed, CPos eyeChunk, CPos startChunk, MCVersion version) {
		Collection<CPos> goodStarts = new HashSet<>();

		for(int ox = -13; ox <= 13; ox++) {
			for(int oz = -13; oz <= 13; oz++) {
				StrongholdGenerator generator = new StrongholdGenerator(version);
				CPos testStart = new CPos(startChunk.getX() + ox, startChunk.getZ() + oz);

				generator.generate(structureSeed, testStart.getX(), testStart.getZ(), piece -> {
					if(!(piece instanceof PortalRoom))return true;
					BlockBox portalBB = PortalFrame.getPortalBB((PortalRoom)piece);
					if(!portalBB.intersectsXZ(eyeChunk.getX() << 4, eyeChunk.getZ() << 4,
							(eyeChunk.getX() << 4) + 15, (eyeChunk.getZ() << 4) + 15))return false;
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

}
