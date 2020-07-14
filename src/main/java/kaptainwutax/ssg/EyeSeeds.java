package kaptainwutax.ssg;

import kaptainwutax.seedutils.lcg.LCG;
import kaptainwutax.seedutils.lcg.rand.JRand;
import kaptainwutax.seedutils.mc.MCVersion;
import randomreverser.call.java.NextFloat;
import randomreverser.device.Lattice;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class EyeSeeds {

	private static Map<Integer, Lattice<JRand>> LATTICES = new HashMap<>();
	private static LCG BACK = LCG.JAVA.combine(-760);

	public static Lattice<JRand> getLattice(int eyeCount) {
		Lattice<JRand> lattice = LATTICES.get(eyeCount);
		if(lattice != null)return lattice;

		lattice = new Lattice<>(LCG.JAVA);

		for(int i = 0; i < eyeCount; i++) {
			lattice.addCall(NextFloat.inRange(0.9F, 1.0F));
		}

		lattice.build();

		LATTICES.put(eyeCount, lattice);
		return lattice;
	}

	public static Stream<Long> getEyeSeeds(int eyeCount) {
		return getLattice(eyeCount).streamSolutions();
	}

	public static Stream<Long> getPopulationSeeds(int eyeCount, MCVersion version) {
		return getLattice(eyeCount).streamSolutions().map(eyeSeed -> toPopulationSeed(eyeSeed, version));
	}

	public static long toPopulationSeed(long eyeSeed, MCVersion version) {
		long decoratorSeed = BACK.nextSeed(eyeSeed);
		decoratorSeed ^= LCG.JAVA.multiplier;
		return version.isOlderThan(MCVersion.v1_13) ? decoratorSeed : decoratorSeed - getStrongholdSalt(version);
	}

	public static long getStrongholdSalt(MCVersion version) {
		return version.isOlderThan(MCVersion.v1_16) ? 20003L : 50000L;
	}

}
