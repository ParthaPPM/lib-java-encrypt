package io.github.parthappm.cryptography;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class Engine
{
	// variables required for zipping files
	private final List<Map.Entry<String, File>> FILES_LIST;
	private final int COMPRESSION_BUFFER_SIZE;
	private final String TEMP_DIRECTORY;

	// variables required for encryption and decryption
	private final Key KEY;
	private final int ENCRYPTION_BUFFER_SIZE;

	public Engine(Key key) throws NoSuchAlgorithmException, InvalidKeySpecException
	{
		this.FILES_LIST = new ArrayList<>();
		this.COMPRESSION_BUFFER_SIZE = 1024;
		this.TEMP_DIRECTORY = System.getProperty("user.dir") + "/temp";

		this.KEY = key;
		this.ENCRYPTION_BUFFER_SIZE = 64;

		// generating the secret key
		KEY.generateSecretKey();
	}

	public void addFile(String fileName)
	{
		addFile(fileName, "");
	}

	private void addFile(String fileName, String directory)
	{
		File file = new File(fileName);
		if (file.isFile())
		{
			FILES_LIST.add(new AbstractMap.SimpleImmutableEntry<>((directory + "/" + file.getName()).substring(1), file));
		}
		else if (file.isDirectory())
		{
			String[] fileNamesList = file.list();
			if (fileNamesList != null)
			{
				for (String subFileName: fileNamesList)
				{
					addFile(fileName + "/" + subFileName, directory + "/" + file.getName());
				}
			}
		}
	}

	public void encrypt(String outputFileName) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, IOException
	{
		String tempZipFileName = TEMP_DIRECTORY + "/zipped_file.zip";
		Files.createDirectories(Path.of(TEMP_DIRECTORY));

		// creating the zip file
		try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(tempZipFileName)))
		{
			for (Map.Entry<String, File> entry : FILES_LIST)
			{
				zos.putNextEntry(new ZipEntry(entry.getKey()));
				try (FileInputStream fis = new FileInputStream(entry.getValue()))
				{
					byte[] bytes = new byte[COMPRESSION_BUFFER_SIZE];
					int length;
					while ((length = fis.read(bytes)) >= 0)
					{
						zos.write(bytes, 0, length);
					}
				}
				catch (IOException ignored) {}
				zos.closeEntry();
			}
		}
		catch (IOException ignored) {}

		// initializing the cipher
		Cipher cipher = Cipher.getInstance(KEY.CIPHER_TRANSFORMATION);
		cipher.init(Cipher.ENCRYPT_MODE, KEY.SECRET_KEY, new IvParameterSpec(KEY.IV));

		// encrypting the zipped file
		try (FileInputStream zippedFis = new FileInputStream(tempZipFileName);
			 FileOutputStream encryptedFos = new FileOutputStream(outputFileName))
		{
			byte[] buffer = new byte[ENCRYPTION_BUFFER_SIZE];
			int bytesRead;
			while ((bytesRead = zippedFis.read(buffer)) != -1)
			{
				byte[] output = cipher.update(buffer, 0, bytesRead);
				if (output != null)
				{
					encryptedFos.write(output);
				}
			}
			byte[] outputBytes = cipher.doFinal();
			if (outputBytes != null)
			{
				encryptedFos.write(outputBytes);
			}
		}
		catch (IOException ignored) {}

		// empty the files from the list
		FILES_LIST.clear();

		// delete the temp folder
		deleteFile(new File(TEMP_DIRECTORY));
	}

	public void decrypt(String outputFileName) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, IOException
	{
		String tempZipFileName = TEMP_DIRECTORY + "/zipped_file.zip";
		Files.createDirectories(Path.of(TEMP_DIRECTORY));

		// initializing the cipher
		Cipher cipher = Cipher.getInstance(KEY.CIPHER_TRANSFORMATION);
		cipher.init(Cipher.DECRYPT_MODE, KEY.SECRET_KEY, new IvParameterSpec(KEY.IV));

		// decrypt the zip file
		try (FileInputStream encryptedFis = new FileInputStream(FILES_LIST.get(0).getValue());
			 FileOutputStream decryptedFos = new FileOutputStream(tempZipFileName))
		{
			byte[] buffer = new byte[ENCRYPTION_BUFFER_SIZE];
			int bytesRead;
			while ((bytesRead = encryptedFis.read(buffer)) != -1)
			{
				byte[] output = cipher.update(buffer, 0, bytesRead);
				if (output != null)
				{
					decryptedFos.write(output);
				}
			}
			byte[] outputBytes = cipher.doFinal();
			if (outputBytes != null)
			{
				decryptedFos.write(outputBytes);
			}
		}
		catch (IOException ignored) {}

		// extract the zip file
		try (ZipInputStream zis = new ZipInputStream(new FileInputStream(tempZipFileName)))
		{
			while (true)
			{
				ZipEntry zipEntry = zis.getNextEntry();
				if (zipEntry == null)
				{
					break;
				}
				File file = new File(outputFileName + "/" + zipEntry.getName());
				Files.createDirectories(Path.of(file.getParentFile().getAbsolutePath()));
				try (FileOutputStream fos = new FileOutputStream(file))
				{
					byte[] bytes = new byte[COMPRESSION_BUFFER_SIZE];
					int length;
					while ((length = zis.read(bytes)) >= 0)
					{
						fos.write(bytes, 0, length);
					}
				}
				catch (IOException ignored) {}
			}
		}
		catch (IOException ignored) {}

		// empty the files from the list
		FILES_LIST.clear();

		// delete the temp folder
		deleteFile(new File(TEMP_DIRECTORY));
	}

	private void deleteFile(File file)
	{
		if (file.isDirectory())
		{
			File[] files = file.listFiles();
			if (files != null)
			{
				for (File file1 : files)
				{
					deleteFile(file1);
				}
			}
		}
		file.delete();
	}
}