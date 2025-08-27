package net.glasslauncher.glassbrigadier.api.argument.coordinate;

import net.glasslauncher.glassbrigadier.api.command.GlassCommandSource;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import org.lwjgl.util.vector.Vector2f;

public class Coordinate {
    final CoordinatePart x;
    final CoordinatePart y;
    final CoordinatePart z;

    Coordinate(CoordinatePart x, CoordinatePart y, CoordinatePart z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * Get the {@link Vec3d} of the absolute position represented by this Coordinate.
     * @param commandSource the commandSource whose position will be used to resolve relative coordinates.
     * @return the {@link Vec3d} of the position.
     */
    public Vec3d getVec3d(GlassCommandSource commandSource) {
        Vec3d sourceCoords = commandSource.getPosition();
        if (this.x.type == CoordinateType.LOCAL) {
            return fromLocal(commandSource);
        }
        return Vec3d.createCached(resolve(x, sourceCoords.x), resolve(y, sourceCoords.y), resolve(z, sourceCoords.z));
    }

    /**
     * Get the {@link Vec3i} of the absolute position represented by this Coordinate.
     * @param commandSource the commandSource whose position will be used to resolve relative coordinates.
     * @return the {@link Vec3i} of the position.
     */
    public Vec3i getVec3i(GlassCommandSource commandSource) {
        Vec3d sourceCoords = commandSource.getPosition();
        if (this.x.type == CoordinateType.LOCAL) {
            Vec3d res = fromLocal(commandSource);
            new Vec3i((int) res.x, (int) res.y, (int) res.z);
        }
        return new Vec3i((int) resolve(x, sourceCoords.x), (int) resolve(y, sourceCoords.y), (int) resolve(z, sourceCoords.z));
    }

    private Vec3d fromLocal(GlassCommandSource source) {
        Vector2f vec2f = source.getRotation();
        Vec3d vec3d = Vec3d.createCached(source.getPosition().x, source.getPosition().y, source.getPosition().z);
        float f = MathHelper.cos((vec2f.y + 90.0F) * 0.017453292F);
        float g = MathHelper.sin((vec2f.y + 90.0F) * 0.017453292F);
        float h = MathHelper.cos(-vec2f.x * 0.017453292F);
        float i = MathHelper.sin(-vec2f.x * 0.017453292F);
        float j = MathHelper.cos((-vec2f.x + 90.0F) * 0.017453292F);
        float k = MathHelper.sin((-vec2f.x + 90.0F) * 0.017453292F);
        Vec3d vec3d2 = Vec3d.createCached(f * h, i, g * h);
        Vec3d vec3d3 = Vec3d.createCached(f * j, k, g * j);
        Vec3d vec3d4 = multiply(crossProduct(vec3d2, vec3d3), -1.0D);
        double d = vec3d2.x * this.z.coord + vec3d3.x * this.y.coord + vec3d4.x * this.x.coord;
        double e = vec3d2.y * this.z.coord + vec3d3.y * this.y.coord + vec3d4.y * this.x.coord;
        double l = vec3d2.z * this.z.coord + vec3d3.z * this.y.coord + vec3d4.z * this.x.coord;
        return Vec3d.createCached((vec3d.x + d), (vec3d.y + e), (vec3d.z + l));
    }

    private static Vec3d multiply(Vec3d original, double amount) {
        return Vec3d.createCached(original.x*amount, original.y*amount, original.z*amount);
    }

    private static Vec3d crossProduct(Vec3d orig, Vec3d arg) {
        return Vec3d.createCached(orig.y * arg.z - orig.z * arg.y, orig.z * arg.x - orig.x * arg.z, orig.x * arg.y - orig.y * arg.x);
    }

    private static double resolve(CoordinatePart part, double relativeTo) {
        return part.type == CoordinateType.RELATIVE ? part.coord + relativeTo : part.coord;
    }

    public static class CoordinatePart {
        final double coord;
        final CoordinateType type;

        public CoordinatePart(double coord, CoordinateType type) {
            this.coord = coord;
            this.type = type;
        }

        /**
         * Checks that either all are {@link CoordinateType#LOCAL LOCAL}, or none are.
         *
         * @param x the first coordinate part
         * @param y the second coordinate part
         * @param z the third coordinate part
         *
         * @return whether all three match in locality
         */
        public static boolean allMatchLocality(CoordinatePart x, CoordinatePart y, CoordinatePart z) {
            return (x.type != CoordinateType.LOCAL || y.type == CoordinateType.LOCAL && z.type == CoordinateType.LOCAL)
                    && (y.type != CoordinateType.LOCAL || x.type == CoordinateType.LOCAL)
                    && (z.type != CoordinateType.LOCAL || x.type == CoordinateType.LOCAL);
        }
    }

    public enum CoordinateType {
        ABSOLUTE,
        RELATIVE,
        LOCAL
    }
}
