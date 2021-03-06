package kaptainwutax.ssg;

import kaptainwutax.featureutils.structure.Stronghold;
import kaptainwutax.featureutils.structure.generator.piece.stronghold.PortalRoom;
import kaptainwutax.featureutils.structure.generator.structure.StrongholdGenerator;
import kaptainwutax.mcutils.rand.ChunkRand;
import kaptainwutax.mcutils.util.block.BlockBox;
import kaptainwutax.mcutils.util.pos.BPos;
import kaptainwutax.mcutils.util.pos.CPos;
import kaptainwutax.mcutils.version.MCVersion;
import kaptainwutax.seedutils.lcg.LCG;
import kaptainwutax.seedutils.rand.JRand;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class WorldSeedGenerator implements Runnable {
	private final ArrayList<String> eyes;
	private final int threadId;
	private final MCVersion version;
	private final int workId;
	public final static int MAX_WORK_NB = 1024;

	public WorldSeedGenerator(ArrayList<String> eyes, int threadId, MCVersion version, int workId) {
		this.eyes = eyes;
		this.threadId = threadId;
		this.version = version;
		this.workId = workId;
	}

	public static void main(String[] args) throws IOException, InterruptedException {
		if (args.length < 2) {
			System.out.println("usage is *.jar <numberThreads> <workunit>*");
			return;
		}
		int numberThreads = Integer.parseInt(args[0]);

		InputStream in = WorldSeedGenerator.class.getResourceAsStream("/input12eyes.txt");
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		List<String> eyes = reader.lines().collect(Collectors.toList());
		int size = eyes.size();
		int len = size / MAX_WORK_NB + 1; // length of a workid (missing the potentially 1023)
		int strides = len / numberThreads; // length of a stride in the length of a workid
		for (int id = 1; id < args.length; id++) {
			int workId = Integer.parseInt(args[id]);
			ArrayList<Thread> threads = new ArrayList<>();
			for (int i = 0; i < numberThreads; i++) {
				ArrayList<String> eye_stride = new ArrayList<>();
				for (int j = strides * i; j < strides * (i + 1); j++) {
					eye_stride.add(eyes.get(j + len * workId));
				}
				Thread thread = new Thread(new WorldSeedGenerator(eye_stride, i, MCVersion.v1_16, workId));
				thread.start();
				threads.add(thread);
			}
			for (Thread thread : threads) {
				thread.join();
			}
			// leftover
			System.out.println("Missing " + (len - strides * numberThreads) + " seeds due to non integer, doing them rn on single thread");
			int count = 0;
			ArrayList<String> eye_stride = new ArrayList<>();
			for (int i = strides * numberThreads; i < len; i++) {
				count++;
				eye_stride.add(eyes.get(i + size / MAX_WORK_NB * workId));
			}
			if (count != 0) {
				System.out.println("There is " + count + " untapped seeds, we are covering them now!");
				Thread thread = new Thread(new WorldSeedGenerator(eye_stride, numberThreads, MCVersion.v1_16, workId));
				thread.start();
				thread.join();
			}
			File file = new File("finalOutput_" + workId + ".txt");


			FileWriter fileWriter = new FileWriter(file);
			for (int i = 0; i < (count == 0 ? numberThreads : numberThreads + 1); i++) {
				BufferedReader readerOut = new BufferedReader(new FileReader("output_" + workId + "_" + i + ".txt"));
				readerOut.lines().forEach(s -> {
					try {
						fileWriter.write(s + "\n");
					} catch (IOException e) {
						e.printStackTrace();
					}
				});
				fileWriter.flush();
				readerOut.close();
				File file1 = new File("output_" + workId + "_" + i + ".txt");
				file1.delete();

			}
			fileWriter.close();
		}

	}

	private final boolean DEBUG = false;
	private final LCG RING_SKIP = LCG.JAVA.combine(4);

	public void generate() throws IOException {
		File file = new File("output_" + workId + "_" + threadId + ".txt");
		if (!file.exists()) {

			if (!file.createNewFile()) {
				System.out.println("File was not created");
				System.exit(-1);
			}
		}
		System.out.println(file.getAbsolutePath());
		FileWriter fileWriter = new FileWriter(file);
		JRand rand = new JRand(0L);
		AtomicInteger progress = new AtomicInteger();
		long startTime = System.nanoTime();
		for (String s : eyes) {
			System.out.println(s);
			String[] line = s.trim().split(Pattern.quote(" "));
			long structureSeed = Long.parseLong(line[0]);
			CPos _12eyeChunk = new CPos(Integer.parseInt(line[1]), Integer.parseInt(line[2]));
			CPos startChunk = new CPos(Integer.parseInt(line[3]), Integer.parseInt(line[4]));
			long rngSeed = RING_SKIP.nextSeed(structureSeed ^ LCG.JAVA.multiplier);

			if (DEBUG) {
				System.out.println("Structure seed " + structureSeed);
			}

			Collection<CPos> goodStarts = getGoodStarts(structureSeed, _12eyeChunk, startChunk, version);
			if (goodStarts.isEmpty()) {
				progress.incrementAndGet();
				continue;
			}
			int lastZero = getLastZero(rand, rngSeed); //The last value of n where nextInt(n) == 0.
			int lastX = goodStarts.stream().mapToInt(CPos::getX).max().getAsInt();
			int lastZ = goodStarts.stream().mapToInt(CPos::getZ).max().getAsInt();

			if (DEBUG) {
				System.out.println("Good one! " + goodStarts);
				System.out.println("Last zero " + lastZero + " / " + 3249);
			}

			for (long upperBits = 0; upperBits < 1L << 16; upperBits++) {
				long worldSeed = (upperBits << 48) | structureSeed;
				BiomeChecker source = new BiomeChecker(version, worldSeed);
				rand.setSeed(rngSeed, false);

				CPos start = source.getStrongholdStart(
						(startChunk.getX() << 4) + 8, (startChunk.getZ() << 4) + 8,
						Stronghold.VALID_BIOMES_16, rand, lastZero, lastX, lastZ);
				if (start == null || !goodStarts.contains(start)) continue;

				BPos p = getPortalCenter(structureSeed, start, version);
				String msg = String.format("%d:World seed %d /tp %d ~ %d\n", threadId, worldSeed, p.getX(), p.getZ());
				System.out.print(msg);
				try {
					fileWriter.write(msg);
				} catch (IOException e) {
					e.printStackTrace();
					System.exit(-1);
				}
			}
			try {
				fileWriter.flush();
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(-1);
			}
			onStructureSeedCompletion(startTime, progress);
		}
		fileWriter.close();
	}

	private static BPos getPortalCenter(long structureSeed, CPos start, MCVersion version) {
		StrongholdGenerator generator = new StrongholdGenerator(version);
		final BlockBox[] portalBB = new BlockBox[1];

		generator.populateStructure(structureSeed, start.getX(), start.getZ(),new ChunkRand(), piece -> {
			if (!(piece instanceof PortalRoom)) return true;
			portalBB[0] = PortalFrame.getPortalBB((PortalRoom) piece);
			return false;
		},true);

		return new BPos(portalBB[0].minX + 1, 0, portalBB[0].minZ + 1);
	}

	private static Collection<CPos> getGoodStarts(long structureSeed, CPos eyeChunk, CPos startChunk, MCVersion version) {
		Collection<CPos> goodStarts = new HashSet<>();

		for (int ox = -13; ox <= 13; ox++) {
			for (int oz = -13; oz <= 13; oz++) {
				StrongholdGenerator generator = new StrongholdGenerator(version);
				CPos testStart = new CPos(startChunk.getX() + ox, startChunk.getZ() + oz);

				generator.populateStructure(structureSeed, testStart.getX(), testStart.getZ(), new ChunkRand(), piece -> {
					if (!(piece instanceof PortalRoom)) return true;

					BlockBox chunkBB = new BlockBox(eyeChunk.getX() << 4, 0, eyeChunk.getZ() << 4, (eyeChunk.getX() << 4) + 15, 255, (eyeChunk.getZ() << 4) + 15);

					BlockBox portalBB = PortalFrame.getPortalBB((PortalRoom) piece);
					if (!portalBB.intersects(chunkBB)) return false;

					for (Stronghold.Piece piece1 : generator.pieceList) {
						if (piece1 == piece) continue;
						if (piece1.getBoundingBox().intersects(chunkBB)) return false;
					}

					goodStarts.add(testStart);
					return false;
				}, true);
			}
		}

		return goodStarts;
	}

	public static int getLastZero(JRand rand, long rngSeed) {
		rand.setSeed(rngSeed, false);
		int lastZero = 0;

		for (int i = 1; i < 3249; i++) {
			boolean b = rand.nextInt(i + 1) == 0;
			if (b) lastZero = i;
		}

		return lastZero;
	}

	public void onStructureSeedCompletion(long startTime, AtomicInteger progress) {
		int i = progress.incrementAndGet();
		int total = eyes.size();

		double seconds = (double) (System.nanoTime() - startTime) / 1000000000.0D;
		double speed = (double) i / seconds;
		double eta = (double) (total - i) / speed;
		System.out.printf("Finished %d seeds out of %d in %fs. ETA %fs.\n", i, total, (float) seconds, (float) eta);
	}

	@Override
	public void run() {
		long id = Thread.currentThread().getId();
		System.out.println("Thread id:" + id + " is starting for task: " + threadId + " with payload of size " + eyes.size());
		try {
			generate();
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("HUGE ERROR");
			System.exit(-2);
		}
		System.out.println("END " + workId);
	}
}
