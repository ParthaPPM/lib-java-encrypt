package io.github.parthappm.cryptography;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.HexFormat;

/**
 * Record to store the password, salt and initialisation vector that are required for encryption or decryption.
 */
public class Key
{
	final String KEY_GENERATOR_ALGORITHM;
	final int KEY_GENERATOR_ITERATION;
	final int KEY_LENGTH;
	final String ENCRYPTION_ALGORITHM;
	final String CIPHER_TRANSFORMATION;

	private final char[] PASSWORD;
	private final byte[] SALT;
	final byte[] IV;
	SecretKey SECRET_KEY;

	/**
	 * @param password Password required for encryption.
	 */
	public Key(char[] password)
	{
		this.KEY_GENERATOR_ALGORITHM = "PBKDF2WithHmacSHA256";
		this.KEY_GENERATOR_ITERATION = 65536;
		this.KEY_LENGTH = 256;
		this.ENCRYPTION_ALGORITHM = "AES";
		this.CIPHER_TRANSFORMATION = "AES/CBC/PKCS5Padding";

		this.PASSWORD = password;
		this.SALT = new byte[16];
		this.IV = new byte[16];
		this.SECRET_KEY = null;

		// generating random bytes for salt
		int[] salt = new SecureRandom().ints(SALT.length, 65, 91).toArray();
		for (int i=0; i<SALT.length; i++)
		{
			SALT[i] = (byte) salt[i];
		}

		// generating random bytes for initialisation vector
		new SecureRandom().nextBytes(IV);
	}

	/**
	 * @param password Password required for encryption or decryption.
	 * @param salt Salt string required for encryption.
	 */
	public Key(char[] password, String salt)
	{
		this(password);

		// converting salt string to salt array
		try
		{
			for (int i = 0; i < SALT.length; i++)
			{
				SALT[i] = (byte) salt.charAt(i);
			}
		}
		catch (StringIndexOutOfBoundsException ignored) {}
	}

	/**
	 * @param password Password required for encryption or decryption.
	 * @param salt Salt string required for encryption or decryption.
	 * @param iv Initialisation vector required for decryption.
	 */
	public Key(char[] password, String salt, String iv)
	{
		this(password, salt);

		// converting iv string to iv byte array
		try
		{
			System.arraycopy(HexFormat.of().parseHex(iv), 0, IV, 0, IV.length);
		}
		catch (ArrayIndexOutOfBoundsException | IllegalArgumentException ignored) {}
	}

	void generateSecretKey() throws NoSuchAlgorithmException, InvalidKeySpecException
	{
		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(KEY_GENERATOR_ALGORITHM);
		KeySpec keySpec = new PBEKeySpec(PASSWORD, SALT, KEY_GENERATOR_ITERATION, KEY_LENGTH);
		SECRET_KEY = new SecretKeySpec(keyFactory.generateSecret(keySpec).getEncoded(), ENCRYPTION_ALGORITHM);
	}

	public String getSalt()
	{
		return new String(SALT, StandardCharsets.UTF_8);
	}

	public String getIv()
	{
		return HexFormat.of().formatHex(IV);
	}
}