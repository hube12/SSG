package kaptainwutax.ssg;

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
import java.util.ArrayList;
import java.util.List;
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

			List<CPos> goodStarts = getGoodStarts(structureSeed, _12eyeChunk, startChunk, version);
			if(goodStarts.isEmpty())continue; //No start in the area lands a 12 eye. ¯\_(ツ)_/¯

			for(long upperBits = 0; upperBits < 1L << 16; upperBits++) {
				long worldSeed = (upperBits << 48) | structureSeed;
				BiomeChecker source = new BiomeChecker(version, worldSeed);
				rand.setSeed(rngSeed, false);
			}
		}
	}

	private static List<CPos> getGoodStarts(long structureSeed, CPos eyeChunk, CPos startChunk, MCVersion version) {
		List<CPos> goodStarts = new ArrayList<>();
		StrongholdGenerator generator = new StrongholdGenerator(version);

		for(int ox = -13; ox <= 13; ox++) {
			for(int oz = -13; oz <= 13; oz++) {
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

}
