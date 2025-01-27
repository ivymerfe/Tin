package me.ivy.tin;

import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public class GeometryUtils {
	public static Vec3d mcLineBoxQuery(Box box, Vec3d start, Vec3d dir)
	{
		Vec3d center = box.getCenter();
		// Map box coordinates to 0 0 0
		double[] o = {start.x-center.x, start.y-center.y, start.z-center.z};
		double[] d = {dir.x, dir.y, dir.z};
		double[] b = {box.getLengthX()/2,box.getLengthY()/2,box.getLengthZ()/2};
		double[] result = lineBoxQuery(o, d, b);
		// Unmap coordinates
		return center.add(result[0], result[1], result[2]);
	}

	// From https://gamedev.stackexchange.com/questions/166450/get-closest-point-on-box-to-line

	// Returns the closest point from an infinite 3D line to a 3D box,  plus the closest "t" along o+t*d
	//   o - the origin of the segment
	//   d - the direction along the segment (which does not need to be normalized)
	//   b - the box radius (3 half side lengths)
	private static double[] lineBoxQuery(double[] o, double[] d, double[] b)
	{
		// Transform the line direction to the first octant using reflections.
		int[] reflected = {d[0] < 0 ? -1 : 1, d[1] < 0 ? -1 : 1, d[2] < 0 ? -1 : 1};
		mul(o, reflected);
		mul(d, reflected);

		// point minus extent
		double[] PmE = {o[0] - b[0], o[1] - b[1], o[2] - b[2]};

		// face indices
		int[] i;

		// line intersects planes x or z
		if (d[1] * PmE[0] >= d[0] * PmE[1])
			// line intersects x = e0 if true, z = e2 if false
			i = (d[2] * PmE[0] >= d[0] * PmE[2]) ? new int[]{0, 1, 2} : new int[]{2, 0, 1};
			// line intersects planes y or z
		else
			// line intersects y = e1 if true, z = e2 if false
			i = (d[2] * PmE[1] >= d[1] * PmE[2]) ? new int[]{1, 2, 0} : new int[]{2, 0, 1};

		// Query closest point on face
		double[] closest = lineFaceQuery(i, o, d, b);

		// Account for previously applied reflections.
		mul(closest, reflected);
		return closest;
	}

	private static void mul(double[] a, int[] b) {
		a[0] *= b[0];
		a[1] *= b[1];
		a[2] *= b[2];
	}

	// Returns the closest point from an infinite 3D line to 3D box face, plus the closest "t" along o+t*d
	// Based on https://www.geometrictools.com/Documentation/DistanceLine3Rectangle3.pdf
	// The box is axis aligned, centered at the origin, and d is expected to point towards
	// the first octant (reflected s.t. all d components are positive).
	//   i - the indirection indices for the face
	//   o - the origin of the segment
	//   d - the direction along the segment (which does not need to be normalized)
	//   b - the box radius (3 half side lengths)
	private static double[] lineFaceQuery(int[] i, double[] o, double[] d, double[] b)
	{
//		double[] PmE = o - b;
//		double[] PpE = o + b;
		double[] PmE = {o[0] - b[0], o[1] - b[1], o[2] - b[2]};
		double[] PpE = {o[0] + b[0], o[1] + b[1], o[2] + b[2]};

		double[] bi = {b[i[0]], b[i[1]], b[i[2]]};
		double[] oi = {o[i[0]], o[i[1]], o[i[2]]};
		double[] di = {d[i[0]], d[i[1]], d[i[2]]};
		double[] PmEi = {PmE[i[0]], PmE[i[1]], PmE[i[2]]};
		double[] PpEi = {PpE[i[0]], PpE[i[1]], PpE[i[2]]};

		double[] c;
		if (di[0] * PpEi[1] >= di[1] * PmEi[0])
		{
			if (di[0] * PpEi[2] >= di[2] * PmEi[0])
			{
				// v[i1] >= -e[i1], v[i2] >= -e[i2] (distance = 0)
				c = new double[]{bi[0], oi[1] - di[1] * PmEi[0] / di[0], oi[2] - di[2] * PmEi[0] / di[0], -PmEi[0] / di[0]};
			}
			else
			{
				// v[i1] >= -e[i1], v[i2] < -e[i2]
				double lenSqr = di[0] * di[0] + di[2] * di[2];
				double tmp = lenSqr * PpEi[1] - di[1] * (di[0] * PmEi[0] + di[2] * PpEi[2]);
				if (tmp <= 2. * lenSqr * bi[1])
				{
					double t = tmp / lenSqr;
					lenSqr += di[1] * di[1];
					tmp = PpEi[1] - t;
					double delta = di[0] * PmEi[0] + di[1] * tmp + di[2] * PpEi[2];
					c = new double[]{bi[0], t - bi[1], -bi[2], -delta / lenSqr};
				}
				else
				{
					lenSqr += di[1] * di[1];
					double delta = di[0] * PmEi[0] + di[1] * PmEi[1] + di[2] * PpEi[2];
					c = new double[]{bi[0], bi[1], -bi[2], -delta / lenSqr};
				}
			}
		}
		else
		{
			if (di[0] * PpEi[2] >= di[2] * PmEi[0])
			{
				// v[i1] < -e[i1], v[i2] >= -e[i2]
				double lenSqr = di[0] * di[0] + di[1] * di[1];
				double tmp = lenSqr * PpEi[2] - di[2] * (di[0] * PmEi[0] + di[1] * PpEi[1]);
				if (tmp <= 2. * lenSqr * bi[2])
				{
					double t = tmp / lenSqr;
					lenSqr += di[2] * di[2];
					tmp = PpEi[2] - t;
					double delta = di[0] * PmEi[0] + di[1] * PpEi[1] + di[2] * tmp;
					c = new double[]{bi[0], -bi[1], t - bi[2], -delta / lenSqr};
				}
				else
				{
					lenSqr += di[2] * di[2];
					double delta = di[0] * PmEi[0] + di[1] * PpEi[1] + di[2] * PmEi[2];
					c = new double[]{bi[0], -bi[1], bi[2], -delta / lenSqr};
				}
			}
			else
			{
				// v[i1] < -e[i1], v[i2] < -e[i2]
				double lenSqr = di[0] * di[0] + di[2] * di[2];
				double tmp = lenSqr * PpEi[1] - di[1] * (di[0] * PmEi[0] + di[2] * PpEi[2]);
				if (tmp >= 0.)
				{
					// v[i1]-edge is c
					if (tmp <= 2. * lenSqr * bi[1])
					{
						double t = tmp / lenSqr;
						lenSqr += di[1] * di[1];
						tmp = PpEi[1] - t;
						double delta = di[0] * PmEi[0] + di[1] * tmp + di[2] * PpEi[2];
						c = new double[]{bi[0], t - bi[1], -bi[2], -delta / lenSqr};
					}
					else
					{
						lenSqr += di[1] * di[1];
						double delta = di[0] * PmEi[0] + di[1] * PmEi[1] + di[2] * PpEi[2];
						c = new double[]{bi[0], bi[1], -bi[2], -delta / lenSqr};
					}
				}
				else {
					lenSqr = di[0] * di[0] + di[1] * di[1];
					tmp = lenSqr * PpEi[2] - di[2] * (di[0] * PmEi[0] + di[1] * PpEi[1]);
					if (tmp >= 0.)
					{
						// v[i2]-edge is c
						if (tmp <= 2. * lenSqr * bi[2])
						{
							double t = tmp / lenSqr;
							lenSqr += di[2] * di[2];
							tmp = PpEi[2] - t;
							double delta = di[0] * PmEi[0] + di[1] * PpEi[1] + di[2] * tmp;
							c = new double[]{bi[0], -bi[1], t - bi[2], -delta / lenSqr};
						}
						else
						{
							lenSqr += di[2] * di[2];
							double delta = di[0] * PmEi[0] + di[1] * PpEi[1] + di[2] * PmEi[2];
							c = new double[]{bi[0], -bi[1], bi[2], -delta / lenSqr};
						}
					}
					else {
						// (v[i1],v[i2])-corner is c
						lenSqr += di[2] * di[2];
						double delta = di[0] * PmEi[0] + di[1] * PpEi[1] + di[2] * PpEi[2];
						c = new double[]{bi[0], -bi[1], -bi[2], -delta / lenSqr};
					}
				}
			}
		}

		int[] map = new int[3];
		map[i[0]] = 0;
		map[i[1]] = 1;
		map[i[2]] = 2;
		return new double[]{c[map[0]], c[map[1]], c[map[2]], c[3]};
	}
}
