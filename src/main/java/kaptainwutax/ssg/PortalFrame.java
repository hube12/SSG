package kaptainwutax.ssg;

import kaptainwutax.featureutils.structure.generator.piece.StructurePiece;
import kaptainwutax.featureutils.structure.generator.piece.stronghold.PortalRoom;
import kaptainwutax.mcutils.util.block.BlockBox;
import kaptainwutax.mcutils.util.math.Vec3i;

public class PortalFrame {

	public static BlockBox getPortalBB(PortalRoom room) {
		Vec3i mins = applyVecTransform(room, new Vec3i(3,0,8));
		Vec3i maxes = applyVecTransform(room, new Vec3i(7,0,12));
		return new BlockBox(mins, maxes);
	}

	public static BlockBox getPortalFrameBB(PortalRoom room) {
		Vec3i mins = applyVecTransform(room, new Vec3i(4,0,9));
		Vec3i maxes = applyVecTransform(room, new Vec3i(6,0,11));
		return new BlockBox(mins, maxes);
	}

	protected static int applyXTransform(StructurePiece<?> piece, int x, int z) {
		if(piece.getFacing() == null)return x;

		switch(piece.getFacing()) {
			case NORTH:
			case SOUTH:
				return piece.getBoundingBox().minX + x;
			case WEST:
				return piece.getBoundingBox().maxX - z;
			case EAST:
				return piece.getBoundingBox().minX + z;
			default:
				return x;
		}
	}

	protected static int applyZTransform(StructurePiece<?> piece, int x, int z) {
		if(piece.getFacing() == null)return z;

		switch(piece.getFacing()) {
			case NORTH:
				return piece.getBoundingBox().maxZ - z;
			case SOUTH:
				return piece.getBoundingBox().minZ + z;
			case WEST:
			case EAST:
				return piece.getBoundingBox().minZ + x;
			default:
				return z;
		}
	}

	protected static Vec3i applyVecTransform(StructurePiece<?> piece, Vec3i vector) {
		int x = vector.getX(), z = vector.getZ();
		return new Vec3i(applyXTransform(piece, x, z), 0, applyZTransform(piece, x, z));
	}

}
