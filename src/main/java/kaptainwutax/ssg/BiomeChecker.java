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

	public CPos getStrongholdStart(int centerX, int centerZ, Collection<Biome> biomes, JRand rand, int lastZero) {
		int lowerX = centerX - STRONGHOLD_RADIUS >> 2;
		int lowerZ = centerZ - STRONGHOLD_RADIUS >> 2;
		int upperX = centerX + STRONGHOLD_RADIUS >> 2;
		int upperZ = centerZ + STRONGHOLD_RADIUS >> 2;
		int sizeX = upperX - lowerX + 1;
		int sizeZ = upperZ - lowerZ + 1;
		CPos pos = null;
		int p = 0;

		for(int oz = 0; oz < sizeZ; ++oz) {
			for(int ox = 0; ox < sizeX; ++ox) {
				int x = lowerX + ox;
				int z = lowerZ + oz;

				if(biomes.contains(this.getBiomeForNoiseGen(x, 0, z))) {
					if(pos == null || rand.nextInt(p + 1) == 0) {
						pos = new CPos(x >> 2, z >> 2);
						if(p == lastZero)return pos;
					}

					p++;
				}
			}
		}

		return new CPos(centerX >> 4, centerZ >> 4);
	}

}
