
# Deterministic RSA (DRSA) with Java

A DRSA module capable of generating deterministic keys and a PRBG (Pseudo-Random Byte Generator) done with Java.



## Documentation

Complete documentation in the HTML format built with Javadoc can be found in `javadoc/index.html`. 

## Installation

The modules were compiled and ran using javac/java 11.0.3, wich you can get by installing the following package:
```bash
sudo apt install openjdk-11-jdk
```

Compile all the classes with the following command:
```bash
mkdir out
javac -cp external/bcprov-jdk15on-170.jar:external/commons-cli-1.3.1.jar:external/jfreechart-1.5.3.jar src/drsa/*.java src/drsa/utils/*.java -d out
```

The result will reside inside the `out/` folder.

These external libraries are used to:
- Draw charts and images for statistical purposes (jfreechart).
- Export the RSA generated keys to the PEM format (bouncycastle).
- Parse command line options (commons-cli)

The implemented functionality and logic is not dependent on these libraries.
    
## Usage/Examples

### randgen

The randgen module can be used to:
- Perform benchmarking tests on the PRBG setup (may take a long time)
- Output a given ammount of pseudo-random bytes, according to the password, confusion string an iteration count parameters provided.

#### Benchmarking

The program will perform 28 setups of the PRBG for each combination of confusion string size [1-4] / iteration count [1, 5, 10, 20, 50, 100, 200].
The statistical outputs will be saved in the form of an histogram (`t_per_cs_icX.png`) and of a text file (`times-YYYY-MM-DD_HH-mm-SS.txt`), in the source folder.

To perform benchmarking, run:

```bash
java -cp out:external/bcprov-jdk15on-170.jar:external/commons-cli-1.3.1.jar:external/jfreechart-1.5.3.jar drsa.randgen -bmk
```

#### Output pseudo-random bytes

Run:

```bash
java -cp out:external/bcprov-jdk15on-170.jar:external/commons-cli-1.3.1.jar:external/jfreechart-1.5.3.jar drsa.randgen -pwd <your_password> -cs <your_confusion_string> -ic <your_iteration_count> -nob <number_of_bytes>
```

The generator will be setted up according to your parameters, which will influence the setup time. After the setup, the bytes will be outputed through the stdout.
To output an infinite sequence of pseudo-random bytes, choose `-nob -1`.

### rsagen

The rsagen module implements the DRSA module, giving it pseudo-random bytes as it's input through stdin and later exporting the resulting DRSA key parameters to the PEM format.

To use it, we can first generate some pseudo-random bytes using the randgen module and then feed it's output to rsagen.

For example, the following commands:
```bash
java -cp out:external/bcprov-jdk15on-170.jar:external/commons-cli-1.3.1.jar:external/jfreechart-1.5.3.jar drsa.randgen -pwd ola -cs o -ic 2 -nob 512 > 512_random_bytes
java -cp out:external/bcprov-jdk15on-170.jar:external/commons-cli-1.3.1.jar:external/jfreechart-1.5.3.jar drsa.rsagen -kn java_512 < 512_random_bytes
```

Will save a 4096bit RSA key pair to the files `java_512_pub_key.pem` and `java_512_priv_key.pem`, as the ones below:

Private Key*:
```bash
-----BEGIN RSA PRIVATE KEY-----
MIIEHgIBAAKCAgEAuN4IBvRZvVxw3bj/0nzKUMMhganHabrolqLUSnwKhqtKyLGP
Ezaf3W8sKWpclGgY1QRL9fd4BVQrMjc1YcG3rJzKg8K5n6GQrNgCxh7XmxTLk406
sxgG7lbH+9ARjS87aootL7abH2u6Or4fTRoXHuUl3DB2yitIwd2Ly0geISi+8jyc
OEh2E2X1c3mnJ5W/7dt9eBWGmoLf2mXq0F1eLAWVsGxxiCPwPrRL/v0lU4MBNGsN
cw1r7DCjvTNV7jrampHN+CuOvr76/eKfXHmekt9AzBO9ml6fTmBlQXbvZS/58jt0
ZcJkx1Xerdbcejk2S3Q8RZ57r7Z2r7jp88CU/wi3p9qYEllxf+n4/lWfpVt9i9NA
zESURy6GJDqGjlCalistvOcrMJ+1gSYb4dCEO35dseRiKWzlOSGziO5I/hwIwsBP
3AhubqYGcUQrKPK1qfvA26T8j0PV52E6dRmC1ye4Hh7dJqWm596XS5XY9b7OFIFl
raCx1l8CN93T0KfUc3vIE+Dk9IT7okT6OhrgFnpw+NWSbsninytoJHFZPxHTLEMT
rBwlkNB/icfuGpeTlOm5y5CCWiN8A3+6w+5eZYsJwr76IC+mPrfFIk7heyGiqe0p
Le7+44tmD8y94UhSmsOy/laKtxi7kdgvQQlezID70Jk23U9b8RLrrnvNFKECAQAC
ggIAWwQlm5buH8bFYsRUX0NFMGIBux7lqO522MMuFcXgQNFQrmKM+H6qUpELgxB0
qDRgFXBaHa1LEASSh9SamZiQX5GKOzjBvxACdLadFwHUUOv6Fpkvz0EDGJDtQNL9
8S7R5/BDsvwKf3vAYm98Za90tqdG7pOFyJ9q4Ne8ncbR3q165TRmDWfPillmcQXy
ABFAP1ETAu5GKQhKQfJvm0p8tNvDCOs7MjXPcG/AAriLigQzsaULKBvxiIWh9+Kw
aevb+MHUUFzRmKPaWukaYKMYKWN1OYR2kuwHLsVS1NpDnuPyAQq4kekVSPbD/X6N
ajeEvb1qdzRB3Q7OfFFkDbhBIkCmOAVZwQZgHFq7Mm/DL6YQLUfq2BkQ42GpcFLZ
AX5PxoylFX9cQuDJmFmeaaNaJc37t4Y+2yUP8/SX7faBCSwe6783fWyGu3e1z6td
UAHa7buVpNHRqfIFOXhgII6SvMp3Dn3ob1IEUJqFdNXQFJUef797QyT2IpUrOG5p
OT4j9gPZPNB39KdhoLpH9w4rZ+BWqVk5VG7JzSzQY3CRvVnOsZlKH0o5bUZz33/J
YEjmT+ObmCoFOVe4g2dYmZl2PawZoLGYavzaG8req5IcidNmnygEKVmdFLeX9QQ3
bb8+b5lm5Oe4b8ZzalJ66tU3gxTBCtkTMBlWBPm+fspzSOECAQACAQACAQACAQAC
AQA=
-----END RSA PRIVATE KEY-----
```

Public Key:
```bash
-----BEGIN RSA PUBLIC KEY-----
MIICCgKCAgEAuN4IBvRZvVxw3bj/0nzKUMMhganHabrolqLUSnwKhqtKyLGPEzaf
3W8sKWpclGgY1QRL9fd4BVQrMjc1YcG3rJzKg8K5n6GQrNgCxh7XmxTLk406sxgG
7lbH+9ARjS87aootL7abH2u6Or4fTRoXHuUl3DB2yitIwd2Ly0geISi+8jycOEh2
E2X1c3mnJ5W/7dt9eBWGmoLf2mXq0F1eLAWVsGxxiCPwPrRL/v0lU4MBNGsNcw1r
7DCjvTNV7jrampHN+CuOvr76/eKfXHmekt9AzBO9ml6fTmBlQXbvZS/58jt0ZcJk
x1Xerdbcejk2S3Q8RZ57r7Z2r7jp88CU/wi3p9qYEllxf+n4/lWfpVt9i9NAzESU
Ry6GJDqGjlCalistvOcrMJ+1gSYb4dCEO35dseRiKWzlOSGziO5I/hwIwsBP3Ahu
bqYGcUQrKPK1qfvA26T8j0PV52E6dRmC1ye4Hh7dJqWm596XS5XY9b7OFIFlraCx
1l8CN93T0KfUc3vIE+Dk9IT7okT6OhrgFnpw+NWSbsninytoJHFZPxHTLEMTrBwl
kNB/icfuGpeTlOm5y5CCWiN8A3+6w+5eZYsJwr76IC+mPrfFIk7heyGiqe0pLe7+
44tmD8y94UhSmsOy/laKtxi7kdgvQQlezID70Jk23U9b8RLrrnvNFKECAwEAAQ==
-----END RSA PUBLIC KEY-----
```

In this case the `rsagen` execution was feeded with the `randgen` outputed pseudo-bytes, but any source of randomness can be tested through the stdin, with the limit of 1M bytes.

The produced private RSA key can be checked with the following command*:
```bash
openssl rsa -check -noout -in java_512_priv_key.pem -text
```
## Authors

- [Duarte Mortágua - 92963](mailto:duarte.ntm@ua.pt)


## Bugs
* \*The private key that is being generated is not currently valid, although the private parameters are fine and the public key is fine. Maybe it's some problem with the bountycastle jar version.
