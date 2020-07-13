package kaptainwutax.ssg;

import kaptainwutax.biomeutils.Biome;
import kaptainwutax.biomeutils.source.OverworldBiomeSource;
import kaptainwutax.seedutils.lcg.rand.JRand;
import kaptainwutax.seedutils.mc.MCVersion;
import kaptainwutax.seedutils.mc.pos.BPos;
import kaptainwutax.seedutils.mc.pos.CPos;

import java.util.Collection;

public class BiomeChecker extends OverworldBiomeSource {

	public static final int STRONGHOLD_RADIUS = 112;

	public BiomeChecker(MCVersion version, long worldSeed) {
		super(version, worldSeed);
	}

	public boolean hasGoodStrongholdStart(int centerX, int centerZ, Collection<Biome> biomes,
	                                      Collection<CPos> goodStarts, JRand rand, int lastZero) {
		if(goodStarts.isEmpty())return false;

		int lowerX = centerX - STRONGHOLD_RADIUS >> 2;
		int lowerZ = centerZ - STRONGHOLD_RADIUS >> 2;
		int upperX = centerX + STRONGHOLD_RADIUS >> 2;
		int upperZ = centerZ + STRONGHOLD_RADIUS >> 2;
		int sizeX = upperX - lowerX + 1;
		int sizeZ = upperZ - lowerZ + 1;
		BPos blockPos = null;
		int p = 0;

		for(int oz = 0; oz < sizeZ; ++oz) {
			for(int ox = 0; ox < sizeX; ++ox) {
				int x = lowerX + ox;
				int z = lowerZ + oz;

				if(biomes.contains(this.getBiomeForNoiseGen(x, 0, z))) {
					if(blockPos == null || rand.nextInt(p + 1) == 0) {
						blockPos = new BPos(x << 2, 0, z << 2);
						if(p == lastZero)return goodStarts.contains(blockPos.toChunkPos());
					}

					p++;
				}
			}
		}

		if(blockPos == null)blockPos = new BPos(centerX, 0, centerZ);
		return goodStarts.contains(blockPos.toChunkPos());
	}

}
