package com.ulisesbocchio.jasyptspringboot.encryptor;

import org.jasypt.salt.ByteArrayFixedSaltGenerator;

import java.util.Base64;

public class FixedBase64ByteArraySaltGenerator  extends ByteArrayFixedSaltGenerator {
    public FixedBase64ByteArraySaltGenerator(String base64Salt) {
        super(Base64.getDecoder().decode(base64Salt));
    }
}
