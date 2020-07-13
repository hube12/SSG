package kaptainwutax.ssg.old;

import kaptainwutax.seedutils.lcg.rand.JRand;
import kaptainwutax.seedutils.mc.MCVersion;
import kaptainwutax.seedutils.mc.pos.CPos;
import kaptainwutax.seedutils.util.math.DistanceMetric;
import kaptainwutax.ssg.EyeSeeds;
import kaptainwutax.ssg.StrongholdGen;
import mjtb49.hashreversals.ChunkRandomReverser;

import java.io.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class StructureSeeds12Eyes {

	private static ChunkRandomReverser REVERSER = new ChunkRandomReverser();

	public static void main2(String[] args) throws IOException {
		MCVersion version = MCVersion.v1_15;

		System.out.println("Looking for population seeds...");
		List<Long> populationSeeds = EyeSeeds.getPopulationSeeds(12, version).collect(Collectors.toList());
		System.out.println(populationSeeds);
		System.out.println("Finished search with " + populationSeeds.size() + " seeds.");

		System.out.println("Creating chunk starts...");
		Set<CPos> starts = new HashSet<>();
		for(CPos ringStart: StrongholdGen.getAllStarts(0, StrongholdGen.DEFAULT_CONFIG)) {
			for(int xo = 0; xo < 53; xo++) {
				for(int zo = 0; zo < 53; zo++) {
					starts.add(new CPos(ringStart.getX() - 26 + xo, ringStart.getZ() - 26 + zo));
				}
			}
		}

		System.out.println("Finished with " + starts.size() + " starts.");

		System.out.println("Starting search...");
		BufferedWriter writer = new BufferedWriter(new FileWriter(new File("output.txt")));
		long count = 0;

		int i = 0;

		for(CPos start: starts) {
			for(long populationSeed: populationSeeds) {
				for(long structureSeed: REVERSER.reversePopulationSeed(populationSeed, start.getX() << 4, start.getZ() << 4, MCVersion.v1_16)) {
					//testStart(structureSeed, start, writer);
					//if(test(structureSeed, start)) {
					//	writer.write(structureSeed + " " + start.getX() + " " + start.getZ() + "\n");
					//	count++;
					//}
				}
			}

			i++;

			if(i % 64 == 0) {
				System.out.println(((float)(i) / starts.size()) * 100.0D + "% complete at " + System.currentTimeMillis());
			}

			writer.flush();
		}

		writer.close();
		System.out.println("Finished search with " + count + " seeds.");
	}

	public static void main(String[] args) throws Exception {
		JRand rand = new JRand(0L);
		BufferedReader reader = new BufferedReader(new FileReader(new File("output_16.txt")));
		BufferedWriter writer = new BufferedWriter(new FileWriter(new File("output2_16.txt")));

		long count = 0;

		while(reader.ready()) {
			String[] line = reader.readLine().split(Pattern.quote(" "));
			if(testStart(Long.parseLong(line[0]), new CPos(Integer.parseInt(line[1]), Integer.parseInt(line[2])), writer, rand))count++;
		}

		System.out.println(count);
	}

	public static boolean testStart(long structureSeed, CPos chunkPos, BufferedWriter writer, JRand rand) throws IOException {
		CPos start = StrongholdGen.getFirstStart(structureSeed, rand);

		if(start.distanceTo(chunkPos, DistanceMetric.CHEBYSHEV) <= 14) {
			writer.write(structureSeed + " " + chunkPos.getX() + " " + chunkPos.getZ() + " "
					+ start.getX() + " " + start.getZ() + "\n");
			writer.flush();
			return true;
		}

		return false;
	}

}
