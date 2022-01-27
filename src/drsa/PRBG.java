package drsa;

import drsa.utils.Buffer;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.List;

/**
 * Pseudo-random Byte Generator (PRGB) that uses PBKDF2 and a XorShift implementation
 * to produce pseudo-random bytes from a given password, confusion string and iteration
 * counter.
 */
public class PRBG {

    /**
     * The seed is a 64bit integer.
     */
    private long seed;
    private int iteration_count;
    private Buffer confusion_pattern;


    /**
     * Holds the sliding window of bytes to be compared with the confusion pattern, when the
     * generator is being setted up.
     */
    private Buffer current_pattern;
    private boolean setted_up;

    /** Initialization of the generator, which includes the computation of the confusion pattern and
     * the seed.
     * @param password A textual password
     * @param confusion_string A string to add complexity, which will influence the confusion pattern
     * @param iteration_count Number of times that the generator will search for the confusion pattern
     */
    public PRBG(String password, String confusion_string, int iteration_count) throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] bytes_seed = compute_seed(password, confusion_string, iteration_count);
        BigInteger bi = new BigInteger(bytes_seed);
        this.seed = bi.and(new BigInteger("4294967295")).longValue();
        this.iteration_count = iteration_count;
        this.confusion_pattern = get_confusion_pattern(confusion_string);
        this.current_pattern = new Buffer(this.confusion_pattern.getSize());
        this.setted_up = false;
    }

    public long getSeed() {
        return seed;
    }

    /*
    // Testing purposes
    public static void main(String args[]) throws Exception {
        drsa.PRBG prbg = new drsa.PRBG("abcdefg", "oi", 2);
        prbg.setup();

        ByteBuffer byte_buffer = ByteBuffer.allocate(512);
        for (int k = 0; k <512; k++) {
            long b = prbg.next_byte();
            if (b >= 128)
                b = b - 256;
            byte_buffer.put(Byte.parseByte(String.valueOf(b)));
        }

        drsa.DRSA drsa = new drsa.DRSA(byte_buffer.array());

        System.out.println(drsa);
    }*/

    /** Generates the next byte of the generator, according to the current seed, using
     * a XorShift approach.
     * @return A pseudo-random byte (0-255)
     */
    public long next_byte() {
        this.seed ^= this.seed << 13;
        this.seed ^= this.seed >> 17;
        this.seed ^= this.seed << 5;
        long final_byte = this.seed & 0xFF;
        return final_byte;
    }

    /** Resseeds the generator
     * @param seed An arbitrary array of bytes
     */
    public void reseed(byte[] seed) {
        BigInteger bi = new BigInteger(seed);
        this.seed = bi.and(new BigInteger("4294967295")).longValue();
    }

    /**
     * Sets up the generator to a state that can take an arbitrarily high computation time to reach.
     * The current state is changed by generating the next byte until the confusion pattern is found
     * amongst the last N bytes generated, for the given number of iterations. In the end of each iteration,
     * the generator is reseeded.
     */
    public void setup() {
        for (int i = 0; i < this.iteration_count; i++) {
            while (true){
                this.current_pattern.add(this.next_byte());
                if (this.confusion_pattern.equals(this.current_pattern)){
                    ByteBuffer byte_buffer = ByteBuffer.allocate(64);
                    for (int k = 0; k < 64; k++) {
                        long b = this.next_byte();
                        if (b >= 128)
                            b = b - 256;
                        byte_buffer.put(Byte.parseByte(String.valueOf(b)));
                    }
                    this.reseed(byte_buffer.array());
                    break;
                }
            }
        }
    }

    /** Computes the drsa.PRBG seed with the PBKDF2 method.
     * @param password A textual password
     * @param confusion_string A string to add complexity, which will influence the confusion pattern
     * @param iteration_count Number of times that the generator will search for the confusion pattern
     * @return An array of 512 bytes
     */
    private byte[] compute_seed(String password, String confusion_string, int iteration_count) throws NoSuchAlgorithmException, InvalidKeySpecException {

        PBEKeySpec spec0 = new PBEKeySpec(password.toCharArray(),
                confusion_string.getBytes(), iteration_count, 512);

        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        SecretKey spec1 = skf.generateSecret(spec0);
        return spec1.getEncoded();
    }

    /** Generates the confusion pattern, from the given confusion string.
     * The confusion pattern is the result of a slice of the SHA-256 digest of the
     * confusion string.
     * @param confusion_string A string to add complexity, which will influence the confusion pattern
     * @return A Buffer containing the confusion pattern
     */
    private Buffer get_confusion_pattern(String confusion_string) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] cs_bytes = digest.digest(confusion_string.getBytes());
        ArrayList<Integer> al = new ArrayList<>();
        int sum = 0;
        int count = 0;
        for (byte b : cs_bytes) {
            al.add(Byte.toUnsignedInt(b));
            sum += Byte.toUnsignedInt(b);
            count += 1;
        }
        int index = sum % (count - confusion_string.length());
        List<Integer> cs = al.subList(index,index+confusion_string.length());
        Buffer b = new Buffer(confusion_string.length());
        for(Integer i : cs) {
            b.add(i.longValue());
        }
        return b;
    }
}

