package io.github.parthappm.encrypt;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

class Zip
{
	private final String INPUT_FILE_NAME;
	private final String OUTPUT_FILE_NAME;
	private final int BUFFER_SIZE;

	Zip(String inputFileName, String outputFileName)
	{
		try
		{
			inputFileName = new File(inputFileName).getCanonicalPath();
			outputFileName = new File(outputFileName).getCanonicalPath();
		}
		catch (IOException ignore)
		{
			inputFileName = null;
			outputFileName = null;
		}
		INPUT_FILE_NAME = inputFileName;
		OUTPUT_FILE_NAME = outputFileName;
		this.BUFFER_SIZE = 1024;
	}

	private void addToZipFile(ZipOutputStream zos, String directory)
	{
		if (INPUT_FILE_NAME == null || OUTPUT_FILE_NAME == null)
		{
			return;
		}

		File newFile = new File(INPUT_FILE_NAME + "/" + directory);
		if (newFile.canRead())
		{
			if (newFile.isFile())
			{
				try(FileInputStream fis = new FileInputStream(newFile))
				{
					zos.putNextEntry(new ZipEntry(directory.isEmpty() ? newFile.getName() : directory));
					byte[] bytes = new byte[BUFFER_SIZE];
					int length;
					while ((length = fis.read(bytes)) >= 0)
					{
						zos.write(bytes, 0, length);
					}
					zos.closeEntry();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
			else if (newFile.isDirectory())
			{
				String[] fileNamesList = newFile.list();
				if (fileNamesList != null)
				{
					for (String subFileName: fileNamesList)
					{
						addToZipFile(zos, directory.isEmpty() ? subFileName : directory + "/" + subFileName);
					}
				}
			}
		}
	}

	private void extractFromZipFile(ZipInputStream zis) throws IOException
	{
		if (INPUT_FILE_NAME == null || OUTPUT_FILE_NAME == null)
		{
			return;
		}

		ZipEntry zipEntry;
		while ((zipEntry = zis.getNextEntry()) != null)
		{
			if (!zipEntry.isDirectory())
			{
				File newFile = new File(OUTPUT_FILE_NAME + "/" + zipEntry.getName());
				if (newFile.getParentFile().exists() || newFile.getParentFile().mkdirs())
				{
					try (FileOutputStream fos = new FileOutputStream(newFile))
					{
						byte[] bytes = new byte[BUFFER_SIZE];
						int length;
						while ((length = zis.read(bytes)) >= 0)
						{
							fos.write(bytes, 0, length);
						}
					}
					catch (IOException e)
					{
						e.printStackTrace();
					}
				}
			}
		}
	}

	void compress()
	{
		try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(OUTPUT_FILE_NAME)))
		{
			addToZipFile(zos, "");
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	void decompress()
	{
		try (ZipInputStream zis = new ZipInputStream(new FileInputStream(INPUT_FILE_NAME)))
		{
			extractFromZipFile(zis);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}