package boozilla.asset.crypto;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class AESCrypt {
    public static String alg = "AES/CBC/PKCS5Padding";

    private final String iv;

    public AESCrypt(final String key)
    {
        this.iv = key.substring(0, 16);
    }

    public byte[] encrypt(final byte[] text) throws IllegalBlockSizeException, BadPaddingException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException
    {
        final var cipher = Cipher.getInstance(alg);
        final var keySpec = new SecretKeySpec(iv.getBytes(), "AES");
        final var ivParamSpec = new IvParameterSpec(iv.getBytes());
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivParamSpec);

        final var encrypted = cipher.doFinal(text);
        return Base64.getEncoder().encode(encrypted);
    }

    public String decrypt(final String cipherText) throws NoSuchPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, InvalidKeyException
    {
        final var cipher = Cipher.getInstance(alg);
        final var keySpec = new SecretKeySpec(iv.getBytes(), "AES");
        final var ivParamSpec = new IvParameterSpec(iv.getBytes());
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivParamSpec);

        final var decodedBytes = Base64.getDecoder().decode(cipherText);
        final var decrypted = cipher.doFinal(decodedBytes);

        return new String(decrypted, StandardCharsets.UTF_8);
    }
}
