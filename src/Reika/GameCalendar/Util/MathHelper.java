package Reika.GameCalendar.Util;


public class MathHelper {

	public static double py3d(double dx, double dy, double dz) {
		double val;
		val = dx*dx+dy*dy+dz*dz;
		return Math.sqrt(val);
	}

	public static int logbase2(long inp) {
		return inp > 0 ? 63-Long.numberOfLeadingZeros(inp) : 0;
	}

	public static int ceil2exp(int val) {
		if (val <= 0)
			return 0;
		val--;
		val = (val >> 1) | val;
		val = (val >> 2) | val;
		val = (val >> 4) | val;
		val = (val >> 8) | val;
		val = (val >> 16) | val;
		val++;
		return val;
	}

	public static double getThousandBase(double val) {
		if (Math.abs(val) == Double.POSITIVE_INFINITY)
			return val;
		if (val == Double.NaN)
			return val;
		boolean neg = val < 0;
		val = Math.abs(val);
		while (val >= 1000) {
			val /= 1000D;
		}
		while (val < 1 && val > 0) {
			val *= 1000D;
		}
		return neg ? -val : val;
	}

	public static int factorial(int val) {
		int base = 1;
		for (int i = val; i > 0; i--) {
			base *= i;
		}
		return base;
	}

	public static int roundToNearestX(int multiple, int val) {
		return ((val+multiple/2)/multiple)*multiple;
	}

	public static boolean isPointInsideEllipse(double x, double y, double z, double ra, double rb, double rc) {
		return (ra > 0 ? ((x*x)/(ra*ra)) : 0) + (rb > 0 ? ((y*y)/(rb*rb)) : 0) + (rc > 0 ? ((z*z)/(rc*rc)) : 0) <= 1;
	}

	public static double linterpolate(double x, double x1, double x2, double y1, double y2) {
		return y1+(x-x1)/(x2-x1)*(y2-y1);
	}

	public static int bitRound(int val, int bits) {
		return (val >> bits) << bits;
	}

	public static boolean isValueInsideBoundsIncl(double low, double hi, double val) {
		return val <= hi && val >= low;
	}

	public static double cosInterpolation(double min, double max, double val) {
		if (!isValueInsideBoundsIncl(min, max, val))
			return 0;
		double size = (max-min)/2D;
		double mid = min+size;
		if (val == mid) {
			return 1;
		}
		else {
			return 0.5+0.5*Math.cos(Math.toRadians((val-mid)/size*180));
		}
	}

	public static double cosInterpolation(double min, double max, double val, double y1, double y2) {
		return y1+(y2-y1)*cosInterpolation(min, max, val);
	}

	public static int getNBitflags(int length) {
		return intpow2(2, length)-1;
	}

	public static int intpow2(int base, int pow) {
		int val = 1;
		for (int i = 0; i < pow; i++) {
			val *= base;
		}
		return val;
	}

	public static double normalizeToBounds(double val, double min, double max) {
		return normalizeToBounds(val, min, max, -1, 1);
	}

	public static double normalizeToBounds(double val, double min, double max, double low, double high) {
		return min+((max-min)*(val-low)/(high-low));
	}

	public static float roundToDecimalPlaces(float f, int i) {
		float pow = (float)Math.pow(10, i);
		return Math.round(f*pow)/pow;
	}

	public static double roundToNearestFraction(double val, double frac) {
		double fac = 1D/frac;
		return Math.round(val*fac)/fac;
	}

	public static double roundUpToFraction(double val, double frac) {
		double fac = 1D/frac;
		return Math.ceil(val*fac)/fac;
	}

	public static double roundDownToFraction(double val, double frac) {
		double fac = 1D/frac;
		return Math.floor(val*fac)/fac;
	}

	public static int cycleBitsLeft(int num, int n) {
		n = n&31;
		return (num << n) | (num >> (32-n));
	}

	public static long cycleBitsLeft(long num, int n) {
		n = n&63;
		return (num << n) | (num >> (64-n));
	}

	public static int cycleBitsRight(int num, int n) {
		n = n&31;
		return (num >> n) | (num << (32-n));
	}

	public static long cycleBitsRight(long num, int n) {
		n = n&63;
		return (num >> n) | (num << (64-n));
	}

	public static double ellipticalInterpolation(double x, double x1, double x2, double y1, double y2) {
		return (y2-y1)*Math.sqrt(Math.pow(x2-x1, 2)-Math.pow(x-x1, 2))/(x2-x1);
	}

	public static double powerInterpolation(double x, double x1, double x2, double y1, double y2, double power) {
		return (y2-y1)*Math.pow(Math.pow(x2-x1, power)-Math.pow(x-x1, power), 1D/power)/(x2-x1);
	}

	public static long cantorCombine(long... vals) {
		long ret = cantorCombine(vals[0], vals[1]);
		for (int i = 2; i < vals.length; i++) {
			ret = cantorCombine(ret, vals[i]);
		}
		return ret;
	}

	public static long cantorCombine(long a, long b) {
		long k1 = a*2;
		long k2 = b*2;
		if (a < 0)
			k1 = a*-2-1;
		if (b < 0)
			k2 = b*-2-1;
		return (long)(0.5*(k1 + k2)*(k1 + k2 + 1) + k2);
	}

	public int floor(double d) {
		return (int)Math.floor(d);
	}

}
