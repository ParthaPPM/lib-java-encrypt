package io.github.parthappm.encrypt;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.HexFormat;

record Keys(char[] password, String salt, String iv)
{
	Keys(char[] password, String salt, String iv)
	{
		this.password = password.clone();
		this.salt = salt;
		if (iv == null || iv.length() != 32)
		{
			byte[] ivBytes = new byte[16];
			new SecureRandom().nextBytes(ivBytes);
			this.iv = HexFormat.of().formatHex(ivBytes);
		}
		else
		{
			this.iv = iv;
		}

		// filling password array with garbage value
		Arrays.fill(password, '\0');
	}

	byte[] saltAsBytes()
	{
		return this.salt.getBytes(StandardCharsets.UTF_8);
	}

	byte[] ivAsBytes()
	{
		return HexFormat.of().parseHex(this.iv);
	}

	void erase()
	{
		// filling password array with garbage value
		Arrays.fill(password, '\0');
	}
}