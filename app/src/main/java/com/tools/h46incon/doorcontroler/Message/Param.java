package com.tools.h46incon.doorcontroler.Message;

/**
 * Created by h46incon on 2015/2/24.
 * Store param when communication
 */
class Param {
	public final static byte[] startBytes = new byte[]{(byte) 0xFD, (byte) 0xB1, (byte) 0x85, (byte) 0x40};
	public final static byte[] endBytes = new byte[]{(byte) 0x40, (byte) 0x85, (byte) 0xB1, (byte) 0xFD};
	public final static int headerLen = 2;
	public final static int CRCLen = 4;
	public final static int packageMinLen = endBytes.length + headerLen + CRCLen;
	public final static int randomLenInLoad = 1;
	public final static String encryptAlgorithm = "RSA/ECB/PKCS1Padding";
	public final static String keyFactoryAlgorithm = "RSA";

	public final static String decodePrivateKey =
			"" +
					"MIICdXAgIIBBAAADAKBNBgQgkDNqhUFkiSPG9oRw0JjBAJPQEZWFAwDASS5C" +
					"AijmADcwgyLgJ81cAEsgEeyAAVNoGb/BAKmM1waQVQgI+TNhEQZmMzik9gZl" +
					"bvbAN7ZLmhGKMI1Nz6xIvaIzUfcSxvM7JaWU1eqv8hNqbH+Bp44CBSlM1ddB" +
					"nsZOKmjBmHB9vDytmWUEYx+jXx7rFe5ohq79y/K8xNopZ2r6qyjE0QIf7T0j" +
					"hejKVCc12qqxmtdaM0ScEv0PJWvZTfHH7ySHtEx7mULrvLk8ofC2jfZavq9K" +
					"NYsAh+tPRqI6MOsJyT9qqUW13QURKbE/Rrxa9L38ffQJILBTFXlQstHuRYH8" +
					"JaR9mCQr1IDizAQ62ABogAo6xGAP1GaRZxYBR1psSj6vElqvdEx9AmIsFlye" +
					"WTl0d0kgdXQpEdRJAOYgM6+BAn+AEMRCg+1YAhPZrqjFjzlWmItPqF4Woj6T" +
					"G5LYiUgXJX0OXy5SRhydBp91EZ95jvtr6WKf4AYxHsw7WPKE+dnqPbdOUozi" +
					"0JNXi0QPraJktX4SBrlfThfLmScHKl1n1iCn1jHesrAdB8+XU77sEIbm2Yx7" +
					"5B8LFceaijI1E915cz41mZeTXBSZDQApd1MoqXr3rHQ2rV13sLmKk+jVz+4U" +
					"LW3Os1qdNegqzEdBlNLc6YsISRlXjP6aIBb3NG82DjletxLACA7lyyQVEmbH" +
					"SOEnH3/ioXGdxCEwGVYa9ytEXqklwrbVFo7+D3sC2nGvIQIq3DAEBQAQVBBC" +
					"dJtkbu+UmSxpGWoYvtRQJOXBANZPqk1WV2Qq96XpOaKmSt6LC9qwM97f5Cpo" +
					"BFclS1CX2zrU4HT8LasoDwZX+XO3kiEQOl4wX2iIgNzekdgbZ3rxAQAn+pc8" +
					"flRLXBAr2oGaaAAUcaxS+74HObaf6bVe+UqWLb4pLZoylEv1oenlg9y+8xCn" +
					"Auk1xEb6aPcIGNb089ICQqaQDI4RvhI6UMBdeX+PH1w45UykYoariPY1YYz2" +
					"PC0kNVHO91Lz7y0Gbpxbd4qrpHc2CcBsQmvfaRF5+5cQoFRm1fgBjwtEury3" +
					"SKtsvxA4qUFjmQnnvZGcB1JFlqRZ0mL1M0Cz5QQ7YD6PqllS5avyGaTlLpk1" +
					"eiwnisD3ZH+RgaAnAZUkAlRHcr+NN4+eHYJgfOQH2gvIENbZNKTKdv4JheOL" +
					"h4xr7b1DHcMfJcKbjm2Q5kTAiXBATEHYppmR2utGaOiHPuKk/NJOrmyzA+xc" +
					"zUgsYkkFtk9j6IuuJMLJvyzKPv2boF91/lO6qPCQ16Apd1/det53EEDDdsF/" +
					"VyIXsHpo7G2tAcQkBJ/pSvHr5y1Vf69DhmmkDlHRzEvekhzRTn+XKnvAxlib" +
					"46SKw8pHqdal4JYwUPvZaJwrANcnYembxiB5OtP8CAkC7EAi20b4N+lnkHXM" +
					"PjxE++OzEZGAMK4sKtWz3Nj+z5DdmTvozc+/cxmZt23aNa6jKdg8/rEh0H2J" +
					"Wuf1tkKzAJtkEQYAmxLY5t0V3rLjz+KKbo5zep70d3A/7RZ4MWdboNTqaM+e" +
					"Ue2PXD6b6kud7chuYpSxV9XUgp4Act2tGUYnJJwcMJAVrB39GDTDaXhvO4Hy" +
					"+x9+PiBBwGTvYSnjMSYzKS4Uqa+3CwxFw3yMWW4pM0OS8QI21gE1N2KFaUdp" +
					"ihmHWhzA==";

	/** decode public key:
				 -----BEGIN PUBLIC KEY-----
				 MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDNUFSPoRJjJPZWwDS5ijDcyL81
				 EseyVNb/KmwaQgTNQZzigZvb7ZhGI16xaIfcvMaWeqhNH+44SlddsZmjHBDyWUx+
				 x7e5q7/KNo2ryjQIT0ejCcqqtd0Sv0WvfHySExULLkfCfZq9Ys+tqIOsT9UWQUbE
				 rxL3fQLBXltHYHaRCQIDAQAB
				 -----END PUBLIC KEY-----
	*/

	public static final String encodePubicKey =
			"" +
					"MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCxoAqI42Z6qOySucQpQ8XMr+2O" +
					"cFLFIZ9ddDBCmo91ktmYYF5Nt0qNScJ1In+3hL2JKxqPSWGAA82/f9EJPqPkeaUI" +
					"LXMWtF7FcPDOfJsnT9sl1k7aHrXVVbyle4Wh2iET63lTJktrYUGlBJ9v0ZVFjT1W" +
					"VgHpjVItvKs8+UomNQIDAQAB";
}
