package transit.trips.core.util;

public final class MaskingUtil {

	private MaskingUtil() {
	}

	public static String maskPan(String pan) {
		if (pan == null || pan.length() < 4) {
			return "****";
		}
		String last4 = pan.substring(pan.length() - 4);
		return "****-****-****-" + last4;
	}

}
