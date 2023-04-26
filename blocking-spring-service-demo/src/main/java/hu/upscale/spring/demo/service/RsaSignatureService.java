package hu.upscale.spring.demo.service;

import hu.upscale.spring.demo.exception.CryptographyException;
import org.springframework.stereotype.Service;

import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import static hu.upscale.spring.demo.util.ResourceUtil.readResourceFile;

/**
 * @author László Zoltán
 */
@Service
public class RsaSignatureService {

    private static final String SIGNATURE_ALGORITHM = "SHA512withRSA";
    private static final String RSA_KEY_FACTORY_ALGORITHM = "RSA";

    private final PrivateKey privateKey;
    private final PublicKey publicKey;

    public RsaSignatureService() {
        privateKey = getRsaPrivateKeyFromPkcs8EncodedKey(readResourceFile("rsa/privateKey.der"));
        publicKey = getRsaPublicKeyFromX509Certificate(readResourceFile("rsa/publicKey.der"));
    }

    public byte[] signData(byte[] dataToSign) {
        try {
            Signature privateSignature = Signature.getInstance(SIGNATURE_ALGORITHM);
            privateSignature.initSign(privateKey);
            privateSignature.update(dataToSign);

            return privateSignature.sign();
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            throw new CryptographyException("Failed to sign data", e);
        }
    }

    public boolean verifySignature(byte[] signature) {
        try {
            Signature publicSignature = Signature.getInstance(SIGNATURE_ALGORITHM);
            publicSignature.initVerify(publicKey);
            publicSignature.update(signature);

            return publicSignature.verify(signature);
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            throw new CryptographyException("Failed to verify data", e);
        }
    }

    private static PrivateKey getRsaPrivateKeyFromPkcs8EncodedKey(byte[] certificateBytes) {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance(RSA_KEY_FACTORY_ALGORITHM);
            PKCS8EncodedKeySpec keySpecPv = new PKCS8EncodedKeySpec(certificateBytes);

            return keyFactory.generatePrivate(keySpecPv);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new CryptographyException("Failed to load RSA private key", e);
        }
    }

    private static PublicKey getRsaPublicKeyFromX509Certificate(byte[] certificateBytes) {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance(RSA_KEY_FACTORY_ALGORITHM);
            return keyFactory.generatePublic(new X509EncodedKeySpec(certificateBytes));
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new CryptographyException("Failed to load RSA public key", e);
        }
    }
}
