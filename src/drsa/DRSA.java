package drsa;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.*;

/**
 * Deterministic RSA module that produces the parameters of a RSA key
 * pair from a N bytes pseudo-random seed.
 */
public class DRSA {

    private BigInteger p;
    private BigInteger q;
    private BigInteger n;
    private BigInteger e;
    private  BigInteger d;
    private BigInteger phi;


    /**
     * List of fixed prime numbers < 1000, in order to check that p and q will not be coprimes
     * with small primes.
     */
    static List<BigInteger> small_primes = new ArrayList<BigInteger>(
            Arrays.asList( new BigInteger("2"), new BigInteger("3"), new BigInteger("5"),
            new BigInteger("7"), new BigInteger("11"), new BigInteger("13"), new BigInteger("17"),
            new BigInteger("19"), new BigInteger("23"), new BigInteger("29"), new BigInteger("31"),
            new BigInteger("37"), new BigInteger("41"), new BigInteger("43"), new BigInteger("47"),
            new BigInteger("53"), new BigInteger("59"), new BigInteger("61"), new BigInteger("67"),
            new BigInteger("71"), new BigInteger("73"), new BigInteger("79"), new BigInteger("83"),
            new BigInteger("89"), new BigInteger("97"), new BigInteger("101"), new BigInteger("103"),
            new BigInteger("107"), new BigInteger("109"), new BigInteger("113"), new BigInteger("127"),
            new BigInteger("131"), new BigInteger("137"), new BigInteger("139"), new BigInteger("149"),
            new BigInteger("151"), new BigInteger("157"), new BigInteger("163"), new BigInteger("167"),
            new BigInteger("173"), new BigInteger("179"), new BigInteger("181"), new BigInteger("191"),
            new BigInteger("193"), new BigInteger("197"), new BigInteger("199"), new BigInteger("211"),
            new BigInteger("223"), new BigInteger("227"), new BigInteger("229"), new BigInteger("233"),
            new BigInteger("239"), new BigInteger("241"), new BigInteger("251"), new BigInteger("257"),
            new BigInteger("263"), new BigInteger("269"), new BigInteger("271"), new BigInteger("277"),
            new BigInteger("281"), new BigInteger("283"), new BigInteger("293"), new BigInteger("307"),
            new BigInteger("311"), new BigInteger("313"), new BigInteger("317"), new BigInteger("331"),
            new BigInteger("337"), new BigInteger("347"), new BigInteger("349"), new BigInteger("353"),
            new BigInteger("359"), new BigInteger("367"), new BigInteger("373"), new BigInteger("379"),
            new BigInteger("383"), new BigInteger("389"), new BigInteger("397"), new BigInteger("401"),
            new BigInteger("409"), new BigInteger("419"), new BigInteger("421"), new BigInteger("431"),
            new BigInteger("433"), new BigInteger("439"), new BigInteger("443"), new BigInteger("449"),
            new BigInteger("457"), new BigInteger("461"), new BigInteger("463"), new BigInteger("467"),
            new BigInteger("479"), new BigInteger("487"), new BigInteger("491"), new BigInteger("499"),
            new BigInteger("503"), new BigInteger("509"), new BigInteger("521"), new BigInteger("523"),
            new BigInteger("541"), new BigInteger("547"), new BigInteger("557"), new BigInteger("563"),
            new BigInteger("569"), new BigInteger("571"), new BigInteger("577"), new BigInteger("587"),
            new BigInteger("593"), new BigInteger("599"), new BigInteger("601"), new BigInteger("607"),
            new BigInteger("613"), new BigInteger("617"), new BigInteger("619"), new BigInteger("631"),
            new BigInteger("641"), new BigInteger("643"), new BigInteger("647"), new BigInteger("653"),
            new BigInteger("659"), new BigInteger("661"), new BigInteger("673"), new BigInteger("677"),
            new BigInteger("683"), new BigInteger("691"), new BigInteger("701"), new BigInteger("709"),
            new BigInteger("719"), new BigInteger("727"), new BigInteger("733"), new BigInteger("739"),
            new BigInteger("743"), new BigInteger("751"), new BigInteger("757"), new BigInteger("761"),
            new BigInteger("769"), new BigInteger("773"), new BigInteger("787"), new BigInteger("797"),
            new BigInteger("809"), new BigInteger("811"), new BigInteger("821"), new BigInteger("823"),
            new BigInteger("827"), new BigInteger("829"), new BigInteger("839"), new BigInteger("853"),
            new BigInteger("857"), new BigInteger("859"), new BigInteger("863"), new BigInteger("877"),
            new BigInteger("881"), new BigInteger("883"), new BigInteger("887"), new BigInteger("907"),
            new BigInteger("911"), new BigInteger("919"), new BigInteger("929"), new BigInteger("937"),
            new BigInteger("941"), new BigInteger("947"), new BigInteger("953"), new BigInteger("967"),
            new BigInteger("971"), new BigInteger("977"), new BigInteger("983"), new BigInteger("991"),
            new BigInteger("997")));


    /** Generates the p and q primes from the given seed, which is cut in half and converted to two
     * BigInteger instances. From these BigInteger instances, the next prime is calculated and attributed
     * to p qnd q after the small prime division verification. From p, q and the fixed public exponent e
     * (2^16+1), all the other parameters are generated.
     * @param seed A N byte seed.
     */
    public DRSA(byte[] seed) throws NoSuchAlgorithmException, InvalidKeySpecException, IOException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, NoSuchPaddingException {

        // Obtaining p and q from the seed
        byte[] seed1 = Arrays.copyOfRange(seed, 0, seed.length / 2);
        byte[] seed2 = Arrays.copyOfRange(seed, seed.length / 2, seed.length);

        BigInteger b1 = new BigInteger(1, seed1);
        BigInteger b2 = new BigInteger(1, seed2);

        BigInteger p = b1.nextProbablePrime();
        BigInteger q = b2.nextProbablePrime();

        // Verify that p and q are not divisible for small primes
        for (BigInteger b : DRSA.small_primes){
            if (p.remainder(b).equals(BigInteger.ZERO))
                p = p.nextProbablePrime();
            if (q.remainder(b).equals(BigInteger.ZERO))
                q = q.nextProbablePrime();
        }

        BigInteger ONE = BigInteger.ONE;

        // n and phi calculation
        BigInteger n = p.multiply(q);
        BigInteger phi = (p.subtract(ONE)).multiply(q.subtract(ONE));
        BigInteger e = new BigInteger("65537");

        assert e.compareTo(ONE) > 0;
        assert phi.gcd(e).equals(ONE);
        assert e.compareTo(phi) < 0;

        // private exponent calculation
        BigInteger d = e.modInverse(phi);

        // Done
        this.p = p;
        this.q = q;
        this.n = n;
        this.e = e;
        this.d = d;
        this.phi = phi;


/*        String input = "test";

        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, pub);
        byte[] encrypted = cipher.doFinal(input.getBytes("UTF-8"));
        System.out.println("encrypted: " + new String(encrypted));

        cipher.init(Cipher.DECRYPT_MODE, priv);
        byte[] decrypted = cipher.doFinal(encrypted);
        System.out.println("decrypted: " + new String(decrypted));*/
    }


    /** Retrieves the private parameters from the given drsa.DRSA instance (d, p, q).
     * @return A HashMap containing the pairs (parameter, value)
     */
    public Map<String, BigInteger> get_private_params() {
        Map<String, BigInteger> private_params = new HashMap<>();
        private_params.put("d", this.d);
        private_params.put("p", this.p);
        private_params.put("q", this.q);
        return private_params;
    }

    /** Retrieves the public parameters from the given drsa.DRSA instance (n, e).
     * @return A HashMap containing the pairs (parameter, value)
     */
    public Map<String, BigInteger> get_public_params() {
        Map<String, BigInteger> public_params = new HashMap<>();
        public_params.put("n", this.n);
        public_params.put("e", this.e);
        return public_params;
    }

    @Override
    public String toString() {
        return "drsa.DRSA{\n" +
                "p=" + p +
                ", \nq=" + q +
                ", \nn=" + n +
                ", \ne=" + e +
                ", \nd=" + d +
                ", \nphi=" + phi +
                '}';
    }
}