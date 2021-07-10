package kaptainwutax.ssg;


import kaptainwutax.mcutils.util.math.DistanceMetric;
import kaptainwutax.mcutils.util.pos.CPos;
import kaptainwutax.seedutils.rand.JRand;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.util.regex.Pattern;

public class StructureSeedStartFilter {

	public static void filter(BufferedReader reader, BufferedWriter writer) throws Exception {
		JRand rand = new JRand(0L);

		long totalCount = 0;
		long filteredCount = 0;

		while(reader.ready()) {
			String[] line = reader.readLine().split(Pattern.quote(" "));

			long structureSeed = Long.parseLong(line[0]);
			CPos chunkPos = new CPos(Integer.parseInt(line[1]), Integer.parseInt(line[2]));
			CPos start = getStartIfValid(structureSeed, chunkPos, rand);

			if(start != null) {
				writer.write(structureSeed + " " + chunkPos.getX() + " " + chunkPos.getZ() + " "
						+ start.getX() + " " + start.getZ() + "\n");
				writer.flush();
				filteredCount++;
			}

			totalCount++;
		}

		System.out.println(filteredCount + " out of " + totalCount + " were filtered.");
	}

	public static CPos getStartIfValid(long structureSeed, CPos chunkPos, JRand rand) {
		CPos start = StrongholdGen.getFirstStart(structureSeed, rand);
		return start.distanceTo(chunkPos, DistanceMetric.CHEBYSHEV) <= 14 ? start : null;
	}

}
