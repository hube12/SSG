package kaptainwutax.ssg;

import com.seedfinding.latticg.reversal.DynamicProgram;
import com.seedfinding.latticg.reversal.calltype.java.JavaCalls;
import kaptainwutax.mcutils.version.MCVersion;
import kaptainwutax.seedutils.lcg.LCG;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class EyeSeeds {

	private static final Map<Integer, DynamicProgram> LATTICES = new HashMap<>();
	private static final LCG BACK = LCG.JAVA.combine(-760);

	public static DynamicProgram getLattice(int eyeCount) {
		DynamicProgram program = LATTICES.get(eyeCount);
		if (program != null) return program;
		program = DynamicProgram.create(com.seedfinding.latticg.util.LCG.JAVA);

		for (int i = 0; i < eyeCount; i++) {
			program.add(JavaCalls.nextFloat().betweenII(0.9F, 1.0F));
		}
		program.setVerbose(false);

		LATTICES.put(eyeCount, program);
		return program;
	}

	public static Stream<Long> getEyeSeeds(int eyeCount) {
		return getLattice(eyeCount).reverse().boxed();
	}

	public static Stream<Long> getPopulationSeeds(int eyeCount, MCVersion version) {
		return getLattice(eyeCount).reverse().boxed().map(eyeSeed -> toPopulationSeed(eyeSeed, version));
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
