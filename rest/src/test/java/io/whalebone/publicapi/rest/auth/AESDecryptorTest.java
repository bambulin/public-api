package io.whalebone.publicapi.rest.auth;

import org.testng.annotations.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class AESDecryptorTest {

    @Test
    public void decrypt_success() throws Exception {
        String decrypted = AESDecryptor.decrypt("BRL89uYbACkqmwVaD7rARQ==");

        assertThat(decrypted, is("very secret"));
    }

    @Test(expectedExceptions = DecryptionException.class)
    public void decrypt_failed() throws Exception {
        AESDecryptor.decrypt("NliLrDkyEHVuKAKu8E0sdA==");
    }
}
