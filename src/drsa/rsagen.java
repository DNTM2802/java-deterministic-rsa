package drsa;

// External libs for command line parsing and PEM write
import org.apache.commons.cli.*;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemWriter;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.Arrays;
import java.util.Map;

/**
 * This application implements the drsa.DRSA module (drsa.DRSA).
 * It receives the first N pseudo-random bytes from the stdin (with a limit of
 * 1M bytes) which are used to generate deterministic private and public parameters
 * for an RSA key, using the drsa.DRSA module.
 * The parameters are then used to convert the key pair to the PEM format,
 * using an external library (bouncycastle). The keys are then exported to a file with the
 * given name.
 */
public class rsagen {
    public static void main(String[] args) throws IOException, IllegalBlockSizeException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeySpecException, BadPaddingException, InvalidKeyException {

        // Parse command line options
        Options options = new Options();
        Option opt_kn = Option.builder("kn")
                .argName("Key name")
                .required(true)
                .longOpt("key_name")
                .desc("Name for the generated key (textual)")
                .hasArg()
                .build();
        options.addOption(opt_kn);
        CommandLineParser parser = new DefaultParser();

        // Default output key name
        String kn = "key";

        try {
            CommandLine cmds = parser.parse(options, args, true);
            // Key name given by user
            if (cmds.hasOption("kn")) {
                kn = cmds.getOptionValue("kn");
            }

        } catch (ParseException e) {
            System.err.println("Error parsing command line options");
            System.err.println(e.getMessage());
            System.exit(1);
        }

        // Read seed from stdin
        ByteBuffer buf = ByteBuffer.allocate(1000000); // 1M bytes max
        ReadableByteChannel channel = Channels.newChannel(System.in);
        while (channel.read(buf) >= 0)
            ;
        buf.flip();
        byte[] seed = Arrays.copyOf(buf.array(), buf.limit());

        // Initialize drsa.DRSA with specified seed
        DRSA drsa = new DRSA(seed);

        // Retrieve RSA parameters
        Map<String, BigInteger> private_params = drsa.get_private_params();
        Map<String, BigInteger> public_params = drsa.get_public_params();
        BigInteger d = private_params.get("d");
        BigInteger p = private_params.get("p");
        BigInteger q = private_params.get("q");
        BigInteger n = public_params.get("n");
        BigInteger e = public_params.get("e");

        /// PRIVATE KEY ///

        // Create private key spec from parameters
        RSAPrivateKeySpec rsa_priv_spec = new RSAPrivateKeySpec(n, d);

        // Convert to PKCS#1
        KeyFactory factory = KeyFactory.getInstance("RSA");
        PrivateKey priv = factory.generatePrivate(rsa_priv_spec);
        byte[] privBytes = priv.getEncoded();
        PrivateKeyInfo pkInfo = PrivateKeyInfo.getInstance(privBytes);
        ASN1Encodable encodable = pkInfo.parsePrivateKey();
        ASN1Primitive primitive_priv = encodable.toASN1Primitive();
        byte[] privateKeyPKCS1 = primitive_priv.getEncoded();

        // Convert to PEM
        PemObject pemObject = new PemObject("RSA PRIVATE KEY", privateKeyPKCS1);
        StringWriter stringWriter = new StringWriter();
        PemWriter pemWriter = new PemWriter(stringWriter);
        pemWriter.writeObject(pemObject);
        pemWriter.close();
        String pemString_priv = stringWriter.toString();

        // Write to file
        BufferedWriter writer = new BufferedWriter(new FileWriter(String.format("%s_priv_key.pem", kn)));
        writer.write(pemString_priv);
        writer.close();
        System.out.printf("Private key saved to %s_priv_key.pem.%n", kn);

        /// PUBLIC KEY ///

        // Create public key spec from parameters
        RSAPublicKeySpec rsa_pub_spec = new RSAPublicKeySpec(n, e);

        // Convert to PKCS#1
        PublicKey pub = factory.generatePublic(rsa_pub_spec);
        byte[] pubBytes = pub.getEncoded();
        SubjectPublicKeyInfo spkInfo = SubjectPublicKeyInfo.getInstance(pubBytes);
        ASN1Primitive primitive_pub = spkInfo.parsePublicKey();
        byte[] publicKeyPKCS1 = primitive_pub.getEncoded();

        // Convert to PEM
        PemObject pemObject_pub = new PemObject("RSA PUBLIC KEY", publicKeyPKCS1);
        StringWriter stringWriter_pub = new StringWriter();
        PemWriter pemWriter_pub = new PemWriter(stringWriter_pub);
        pemWriter_pub.writeObject(pemObject_pub);
        pemWriter_pub.close();
        String pemString_pub = stringWriter_pub.toString();

        // Write to file
        BufferedWriter writer_pub = new BufferedWriter(new FileWriter(String.format("%s_pub_key.pem", kn)));
        writer_pub.write(pemString_pub);
        writer_pub.close();
        System.out.printf("Public key saved to %s_pub_key.pem.%n", kn);
    }
}
