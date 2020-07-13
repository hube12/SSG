package kaptainwutax.ssg;

import kaptainwutax.featureutils.structure.Stronghold;
import kaptainwutax.featureutils.structure.generator.StrongholdGenerator;
import kaptainwutax.featureutils.structure.generator.piece.stronghold.PortalRoom;
import kaptainwutax.seedutils.lcg.LCG;
import kaptainwutax.seedutils.lcg.rand.JRand;
import kaptainwutax.seedutils.mc.MCVersion;
import kaptainwutax.seedutils.mc.pos.BPos;
import kaptainwutax.seedutils.mc.pos.CPos;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class StrongholdBiome {

	public static void main(String[] args) throws Exception {
		BufferedReader reader = new BufferedReader(new FileReader(new File("output2_16.txt")));
		BufferedWriter writer = new BufferedWriter(new FileWriter(new File("output3_16.txt")));
		JRand rand = new JRand(0L);

		int count = 0;

		while(reader.ready()) {
			String[] line = reader.readLine().split(Pattern.quote(" "));
			long structureSeed = Long.parseLong(line[0]);
			BPos _12eye = new CPos(Integer.parseInt(line[1]), Integer.parseInt(line[2])).toBlockPos();
			BPos start = new CPos(Integer.parseInt(line[3]), Integer.parseInt(line[4])).toBlockPos().add(8, 0, 8);

			long rngSeed = LCG.JAVA.combine(4).nextSeed(structureSeed ^ LCG.JAVA.multiplier);

			Map<CPos, Boolean> starts = new HashMap<>();

			for(long upperBits = 0; upperBits < 1L << 10; upperBits++) {
				long worldSeed = (upperBits << 48) | structureSeed;
				BiomeChecker source = new BiomeChecker(MCVersion.v1_15, worldSeed);
				source.build();

				rand.setSeed(rngSeed, false);

				BPos moved = source.locateBiome(start.getX(), start.getZ(), 112, Stronghold.VALID_BIOMES, rand);
				CPos movedStart = moved == null ? start.toChunkPos() : moved.toChunkPos();

				Boolean v = starts.get(movedStart);

				if(v != null && v) {
					System.out.println("in list: " + worldSeed + " at " + _12eye);
					System.out.println(starts);
					continue;
				}

				StrongholdGenerator stronghold = new StrongholdGenerator(MCVersion.v1_16);
				final boolean[] portalRoomAligned = {false};

				stronghold.generate(worldSeed, movedStart.getX(), movedStart.getZ(), piece -> {
					if(piece instanceof PortalRoom) {
						if(piece.getBoundingBox().intersectsXZ(_12eye.getX(), _12eye.getZ(), _12eye.getX() + 15, _12eye.getZ() + 15)) {
							System.out.println(worldSeed + " at " + _12eye);
							portalRoomAligned[0] = true;
						}

						return false;
					}

					return true;
				});

				starts.put(movedStart, portalRoomAligned[0]);
			}

			//count++;
			//System.out.println(count);
		}
	}

}
