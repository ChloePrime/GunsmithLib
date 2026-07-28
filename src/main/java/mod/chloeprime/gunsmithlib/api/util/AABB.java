package mod.chloeprime.gunsmithlib.api.util;

import cn.chloeprime.commons.math.LinearAlgebraTypes;
import org.joml.Vector3d;
import org.joml.Vector3i;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static cn.chloeprime.commons.math.LinearAlgebraTypes.*;

/**
 * 原版 {@link net.minecraft.world.phys.AABB} 的包装器。
 * 方法太多了写不动了 XD
 *
 * @since 6.3
 */
@SuppressWarnings("unused")
public record AABB(net.minecraft.world.phys.AABB impl) {

    // Getters

    public Vector3d center() {
        return moj2joml(this.impl.getCenter());
    }

    public boolean has_nan() {
        return this.impl.hasNaN();
    }

    public Vector3d max_pos() {
        return new Vector3d(this.impl.maxX, this.impl.maxY, this.impl.maxZ);
    }

    public Vector3d min_pos() {
        return new Vector3d(this.impl.minX, this.impl.minY, this.impl.minZ);
    }

    public Vector3d size() {
        return new Vector3d(this.impl.getXsize(), this.impl.getYsize(), this.impl.getZsize());
    }

    public double x_size() {
        return this.impl.getXsize();
    }

    public double y_size() {
        return this.impl.getYsize();
    }

    public double z_size() {
        return this.impl.getZsize();
    }

    /**
     * 参考大小。
     * 不知道为什么原版的 size 返回这个
     *
     * @return 实体的参考大小，通常用于 RPG 用途
     */
    public double referential_size() {
        return this.impl.getSize();
    }

    // Test / Compute Methods

    public @Nullable Vector3d clip(Vector3d from, Vector3d to) {
        return this.impl.clip(joml2moj(from), joml2moj(to))
                .map(LinearAlgebraTypes::moj2joml)
                .orElse(null);
    }

    /**
     * Returns if the supplied Vector3dD is completely inside the bounding box
     */
    public boolean contains(Vector3d vec) {
        return this.impl.contains(joml2moj(vec));
    }

    public boolean contains(double x, double y, double z) {
        return this.impl.contains(x, y, z);
    }

    public double distance_to_sqr(Vector3d target) {
        return this.impl.distanceToSqr(joml2moj(target));
    }

    /**
     * Checks if the bounding box intersects with another.
     */
    public boolean intersects(AABB other) {
        return this.impl.intersects(other.impl);
    }

    public boolean intersects(double x1, double y1, double z1, double x2, double y2, double z2) {
        return this.impl.intersects(x1, y1, z1, x2, y2, z2);
    }

    public boolean intersects(Vector3d min, Vector3d max) {
        return this.impl.intersects(joml2moj(min), joml2moj(max));
    }

    // Mutations

    public AABB with_min_x(double pMinX) {
        return new AABB(this.impl.setMinX(pMinX));
    }

    public AABB with_min_y(double pMinY) {
        return new AABB(this.impl.setMinY(pMinY));
    }

    public AABB with_min_z(double pMinZ) {
        return new AABB(this.impl.setMinZ(pMinZ));
    }

    public AABB with_max_x(double pMaxX) {
        return new AABB(this.impl.setMaxX(pMaxX));
    }

    public AABB with_max_y(double pMaxY) {
        return new AABB(this.impl.setMaxY(pMaxY));
    }

    public AABB with_max_z(double pMaxZ) {
        return new AABB(this.impl.setMaxZ(pMaxZ));
    }

    /**
     * Creates a new {@link AABB} that has been contracted by the given amount, with positive changes decreasing
     * max values and negative changes increasing min values.
     * <br/>
     * If the amount to contract by is larger than the length of a side, then the side will wrap (still creating a valid
     * AABB - see last sample).
     *
     * <h3>Samples:</h3>
     * <table>
     * <tr><th>Input</th><th>Result</th></tr>
     * <tr><td><pre><code>new AxisAlignedBB(0, 0, 0, 4, 4, 4).contract(2, 2, 2)</code></pre></td><td><pre><samp>box[0.0,
     * 0.0, 0.0 -> 2.0, 2.0, 2.0]</samp></pre></td></tr>
     * <tr><td><pre><code>new AxisAlignedBB(0, 0, 0, 4, 4, 4).contract(-2, -2, -
     * 2)</code></pre></td><td><pre><samp>box[2.0, 2.0, 2.0 -> 4.0, 4.0, 4.0]</samp></pre></td></tr>
     * <tr><td><pre><code>new AxisAlignedBB(5, 5, 5, 7, 7, 7).contract(0, 1, -1)</code></pre></td><td><pre><samp>box[5.0,
     * 5.0, 6.0 -> 7.0, 6.0, 7.0]</samp></pre></td></tr>
     * <tr><td><pre><code>new AxisAlignedBB(-2, -2, -2, 2, 2, 2).contract(4, -4, 0)</code></pre></td><td><pre><samp>box[-
     * 8.0, 2.0, -2.0 -> -2.0, 8.0, 2.0]</samp></pre></td></tr>
     * </table>
     *
     * <h3>See Also:</h3>
     * <ul>
     * <li>{@link #expand_towards(double, double, double)} - like this, except for expanding.</li>
     * <li>{@link #inflate(double, double, double)} and {@link #inflate(double)} - expands in all directions.</li>
     * <li>{@link #deflate(double)} - contracts in all directions (like {@link #inflate(double)})</li>
     * </ul>
     *
     * @return A new modified bounding box.
     */
    public AABB contract(double x, double y, double z) {
        return new AABB(this.impl.contract(x, y, z));
    }

    public AABB expand_towards(Vector3d delta) {
        return new AABB(this.impl.expandTowards(joml2moj(delta)));
    }

    /**
     * Creates a new {@link AABB} that has been expanded by the given amount, with positive changes increasing
     * max values and negative changes decreasing min values.
     *
     * <h3>Samples:</h3>
     * <table>
     * <tr><th>Input</th><th>Result</th></tr>
     * <tr><td><pre><code>new AxisAlignedBB(0, 0, 0, 1, 1, 1).expand(2, 2, 2)</code></pre></td><td><pre><samp>box[0, 0, 0
     * -> 3, 3, 3]</samp></pre></td><td>
     * <tr><td><pre><code>new AxisAlignedBB(0, 0, 0, 1, 1, 1).expand(-2, -2, -2)</code></pre></td><td><pre><samp>box[-2,
     * -2, -2 -> 1, 1, 1]</samp></pre></td><td>
     * <tr><td><pre><code>new AxisAlignedBB(5, 5, 5, 7, 7, 7).expand(0, 1, -1)</code></pre></td><td><pre><samp>box[5, 5,
     * 4, 7, 8, 7]</samp></pre></td><td>
     * </table>
     *
     * <h3>See Also:</h3>
     * <ul>
     * <li>{@link #contract(double, double, double)} - like this, except for shrinking.</li>
     * <li>{@link #inflate(double, double, double)} and {@link #inflate(double)} - expands in all directions.</li>
     * <li>{@link #deflate(double)} - contracts in all directions (like {@link #inflate(double)})</li>
     * </ul>
     *
     * @return A modified bounding box that will always be equal or greater in volume to this bounding box.
     */
    public AABB expand_towards(double dx, double dy, double dz) {
        return new AABB(this.impl.expandTowards(dx, dy, dz));
    }

    /**
     * Creates a new {@link AABB} that has been contracted by the given amount in both directions. Negative
     * values will shrink the AABB instead of expanding it.
     * <br/>
     * Side lengths will be increased by 2 times the value of the parameters, since both min and max are changed.
     * <br/>
     * If contracting and the amount to contract by is larger than the length of a side, then the side will wrap (still
     * creating a valid AABB - see last ample).
     *
     * <h3>Samples:</h3>
     * <table>
     * <tr><th>Input</th><th>Result</th></tr>
     * <tr><td><pre><code>new AxisAlignedBB(0, 0, 0, 1, 1, 1).grow(2, 2, 2)</code></pre></td><td><pre><samp>box[-2.0, -
     * 2.0, -2.0 -> 3.0, 3.0, 3.0]</samp></pre></td></tr>
     * <tr><td><pre><code>new AxisAlignedBB(0, 0, 0, 6, 6, 6).grow(-2, -2, -2)</code></pre></td><td><pre><samp>box[2.0,
     * 2.0, 2.0 -> 4.0, 4.0, 4.0]</samp></pre></td></tr>
     * <tr><td><pre><code>new AxisAlignedBB(5, 5, 5, 7, 7, 7).grow(0, 1, -1)</code></pre></td><td><pre><samp>box[5.0,
     * 4.0, 6.0 -> 7.0, 8.0, 6.0]</samp></pre></td></tr>
     * <tr><td><pre><code>new AxisAlignedBB(1, 1, 1, 3, 3, 3).grow(-4, -2, -3)</code></pre></td><td><pre><samp>box[-1.0,
     * 1.0, 0.0 -> 5.0, 3.0, 4.0]</samp></pre></td></tr>
     * </table>
     *
     * <h3>See Also:</h3>
     * <ul>
     * <li>{@link #expand_towards(double, double, double)} - expands in only one direction.</li>
     * <li>{@link #contract(double, double, double)} - contracts in only one direction.</li>
     * <li>{@link #inflate(double)} - version of this that expands in all directions from one parameter.</li>
     * <li>{@link #deflate(double)} - contracts in all directions</li>
     * </ul>
     *
     * @return A modified bounding box.
     */
    public AABB inflate(double x, double y, double z) {
        return new AABB(this.impl.inflate(x, y, z));
    }

    /**
     * Creates a new {@link AABB} that is expanded by the given value in all directions. Equivalent to {@link
     * #inflate(double, double, double)} with the given value for all 3 params. Negative values will shrink the AABB.
     * <br/>
     * Side lengths will be increased by 2 times the value of the parameter, since both min and max are changed.
     * <br/>
     * If contracting and the amount to contract by is larger than the length of a side, then the side will wrap (still
     * creating a valid AABB - see samples on {@link #inflate(double, double, double)}).
     *
     * @return A modified AABB.
     */
    public AABB inflate(double value) {
        return new AABB(this.impl.inflate(value));
    }

    /**
     * Creates a new {@link AABB} that is expanded by the given value in all directions. Equivalent to {@link
     * #inflate(double)} with value set to the negative of the value provided here. Passing a negative value to this method
     * values will grow the AABB.
     * <br/>
     * Side lengths will be decreased by 2 times the value of the parameter, since both min and max are changed.
     * <br/>
     * If contracting and the amount to contract by is larger than the length of a side, then the side will wrap (still
     * creating a valid AABB - see samples on {@link #inflate(double, double, double)}).
     *
     * @return A modified AABB.
     */
    public AABB deflate(double x, double y, double z) {
        return new AABB(this.impl.deflate(x, y, z));
    }

    /**
     * Creates a new {@link AABB} that is expanded by the given value in all directions. Equivalent to {@link
     * #inflate(double)} with value set to the negative of the value provided here. Passing a negative value to this method
     * values will grow the AABB.
     * <br/>
     * Side lengths will be decreased by 2 times the value of the parameter, since both min and max are changed.
     * <br/>
     * If contracting and the amount to contract by is larger than the length of a side, then the side will wrap (still
     * creating a valid AABB - see samples on {@link #inflate(double, double, double)}).
     *
     * @return A modified AABB.
     */
    public AABB deflate(double value) {
        return new AABB(this.impl.deflate(value));
    }

    public AABB intersect(AABB other) {
        return new AABB(this.impl.intersect(other.impl));
    }

    public AABB minmax(AABB other) {
        return new AABB(this.impl.minmax(other.impl));
    }

    /**
     * Offsets the current bounding box by the specified amount.
     */
    public AABB move(double x, double y, double z) {
        return new AABB(this.impl.move(x, y, z));
    }

    public AABB move(Vector3i pos) {
        return new AABB(this.impl.move(joml2moj(pos)));
    }

    public AABB move(Vector3d vec) {
        return new AABB(this.impl.move(joml2moj(vec)));
    }

    // Object 方法覆写

    @Override
    public boolean equals(Object other) {
        return other instanceof AABB babe && this.impl.equals(babe.impl);
    }

    @Override
    public int hashCode() {
        return this.impl.hashCode();
    }

    @Override
    public @Nonnull String toString() {
        return this.impl.toString();
    }
}
