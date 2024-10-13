package io.github.parthappm.encrypt;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

class Encrypt
{
	private final String KEY_GENERATOR_ALGORITHM;
	private final int KEY_GENERATOR_ITERATION;
	private final int KEY_LENGTH;
	private final String ENCRYPTION_ALGORITHM;
	private final String CIPHER_TRANSFORMATION;

	private final String INPUT_FILE_NAME;
	private final String OUTPUT_FILE_NAME;

	Encrypt(String inputFileName, String outputFileName)
	{
		this.KEY_GENERATOR_ALGORITHM = "PBKDF2WithHmacSHA256";
		this.KEY_GENERATOR_ITERATION = 65536;
		this.KEY_LENGTH = 256;
		this.ENCRYPTION_ALGORITHM = "AES";
		this.CIPHER_TRANSFORMATION = "AES/CBC/PKCS5Padding";

		this.INPUT_FILE_NAME = inputFileName;
		this.OUTPUT_FILE_NAME = outputFileName;
	}

	private SecretKey getSecretKey(Keys keys) throws NoSuchAlgorithmException, InvalidKeySpecException
	{
		KeySpec keySpec = new PBEKeySpec(keys.password(), keys.saltAsBytes(), KEY_GENERATOR_ITERATION, KEY_LENGTH);
		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(KEY_GENERATOR_ALGORITHM);
		return new SecretKeySpec(keyFactory.generateSecret(keySpec).getEncoded(), ENCRYPTION_ALGORITHM);
	}

	void encrypt(Keys keys)
	{
		try
		{
			// initializing the cipher
			Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
			cipher.init(Cipher.ENCRYPT_MODE, getSecretKey(keys), new IvParameterSpec(keys.ivAsBytes()));

			FileInputStream inputStream = new FileInputStream(INPUT_FILE_NAME);
			FileOutputStream outputStream = new FileOutputStream(OUTPUT_FILE_NAME);
			byte[] buffer = new byte[64];
			int bytesRead;
			while ((bytesRead = inputStream.read(buffer)) != -1)
			{
				byte[] output = cipher.update(buffer, 0, bytesRead);
				if (output != null)
				{
					outputStream.write(output);
				}
			}
			byte[] outputBytes = cipher.doFinal();
			if (outputBytes != null)
			{
				outputStream.write(outputBytes);
			}
			inputStream.close();
			outputStream.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	void decrypt(Keys keys)
	{
		try
		{
			// initializing the cipher
			Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
			cipher.init(Cipher.DECRYPT_MODE, getSecretKey(keys), new IvParameterSpec(keys.ivAsBytes()));

			FileInputStream inputStream = new FileInputStream(INPUT_FILE_NAME);
			FileOutputStream outputStream = new FileOutputStream(OUTPUT_FILE_NAME);
			byte[] buffer = new byte[64];
			int bytesRead;
			while ((bytesRead = inputStream.read(buffer)) != -1)
			{
				byte[] output = cipher.update(buffer, 0, bytesRead);
				if (output != null)
				{
					outputStream.write(output);
				}
			}
			byte[] outputBytes = cipher.doFinal();
			if (outputBytes != null)
			{
				outputStream.write(outputBytes);
			}
			inputStream.close();
			outputStream.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}