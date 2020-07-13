package kaptainwutax.ssg;

import kaptainwutax.seedutils.lcg.rand.JRand;
import kaptainwutax.seedutils.mc.pos.CPos;

import java.util.*;

public class StrongholdGen {

	private static final Map<Integer, Set<CPos>> RING_TO_CHUNKS = new HashMap<>();
	public static final Config DEFAULT_CONFIG = new Config(32, 128, 3);

	public static CPos getFirstStart(long worldSeed, JRand rand) {
		rand.setSeed(worldSeed);
		double randomRadian = rand.nextDouble() * Math.PI * 2.0D;
		double multiplier = 128.0D + (rand.nextDouble() - 0.5D) * 80.0D;
		int x = (int)Math.round(Math.cos(randomRadian) * multiplier);
		int z = (int)Math.round(Math.sin(randomRadian) * multiplier);
		return new CPos(x, z);
	}

	public static Set<CPos> getAllStarts(int ring, Config config) {
		Set<CPos> chunks = RING_TO_CHUNKS.get(ring);
		if(chunks != null)return chunks;

		chunks = new HashSet<>();

		double base = config.distance * (4 + ring * 6);
		double min = base + (0.0D - 0.5D) * (double)config.distance * 2.5D;
		double max = base + (1.0D - 0.5D) * (double)config.distance * 2.5D;

		//Wtf are those magic .0006 and .05? Brain, please tell me.
		for(double d = 0.0F; d < Math.PI * 2.0D; d += 0.0006D) {
			for(double d2 = min; d2 < max; d2 += 0.05D) {
				int x = (int)Math.round(Math.cos(d) * d2);
				int z = (int)Math.round(Math.sin(d) * d2);
				chunks.add(new CPos(x, z));
			}
		}

		chunks = Collections.unmodifiableSet(chunks);
		RING_TO_CHUNKS.put(ring, chunks);
		return chunks;
	}

	public static class Config {
		public final int distance;
		public final int count;
		public final int spread;

		public Config(int distance, int count, int spread) {
			this.distance = distance;
			this.count = count;
			this.spread = spread;
		}
	}

}
