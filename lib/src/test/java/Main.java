import io.github.parthappm.cryptography.Engine;
import io.github.parthappm.cryptography.Key;

public class Main
{
	public static void main(String[] args)
	{
		Key encryptionKey = new Key("strong-password".toCharArray());
		System.out.println("++++++++++++++++++++++++++");
		System.out.println(encryptionKey.getSalt());
		System.out.println(encryptionKey.getIv());
		System.out.println("++++++++++++++++++++++++++");
		try
		{
			Engine encryptionEngine = new Engine(encryptionKey);

			encryptionEngine.addFile("path/to/any/file/or/folder");
			encryptionEngine.addFile("path/to/any/file/or/folder");
			encryptionEngine.addFile("path/to/any/file/or/folder");
			encryptionEngine.encrypt("encrypted_file.enc");
		}
		catch (Exception ignored) {}

		// the salt and the iv are obtained from encryptionKey object
		Key decryptionKey = new Key("strong-password".toCharArray(), "IWXRDMUYZEECEBKC", "a7b1bab03f2cbb8c172ed6448f7991a4");
		try
		{
			Engine decryptionEngine = new Engine(decryptionKey);

			decryptionEngine.addFile("encrypted_file.enc");
			decryptionEngine.decrypt("output");
		}
		catch (Exception ignored) {}
	}
}