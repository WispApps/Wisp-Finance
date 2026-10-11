import com.wisp.finance.security.*;
import java.util.*;
import java.nio.charset.StandardCharsets;
public class RecoveryCryptoTest {
    private static void check(boolean v) { if(!v) throw new AssertionError(); }
    public static void main(String[] args) throws Exception {
        byte[] seed=new byte[23]; for(int i=0;i<23;i++)seed[i]=(byte)i;
        check(RecoveryPhrase.format(seed,"en").equals("amber-anchor amber-bird amber-bridge amber-cloud amber-field amber-forest amber-harbor amber-island amber-lake amber-leaf amber-meadow amber-night amber-river amber-stone amber-sun amber-willow april-anchor april-bird april-bridge april-cloud april-field april-forest april-harbor garden-stone"));
        String[] languages={"en","ru","uk","de","es","fr","it","nl","pl","pt"};
        for(String lang:languages) {
            String phrase=RecoveryPhrase.format(seed,lang);
            check(phrase.split(" ").length==24);
            check(Arrays.equals(seed,RecoveryPhrase.parse(phrase)));
            check(Arrays.equals(seed,RecoveryPhrase.parse(phrase.toUpperCase(Locale.ROOT).replace(" ","\n"))));
        }
        byte[] plain="{\"amountMinorUnits\":9223372036854775807,\"title\":\"Платёж €\"}".getBytes(StandardCharsets.UTF_8);
        byte[] encrypted=RecoveryCrypto.encrypt(plain,seed);
        check(Arrays.equals(plain,RecoveryCrypto.decrypt(encrypted,RecoveryPhrase.parse(RecoveryPhrase.format(seed,"pl")))));
        check(!Arrays.equals(encrypted,RecoveryCrypto.encrypt(plain,seed)));
        byte[] bad=seed.clone();bad[0]^=1;
        try {RecoveryCrypto.decrypt(encrypted,bad);throw new AssertionError("Wrong key accepted");}catch(java.security.GeneralSecurityException expected){}
        for(int i=0;i<encrypted.length;i++) {
            byte[] copy=encrypted.clone();copy[i]^=1;
            try {RecoveryCrypto.decrypt(copy,seed);throw new AssertionError("Tamper accepted at "+i);}catch(java.security.GeneralSecurityException|IllegalArgumentException expected){}
        }
        for(int n=0;n<encrypted.length;n++) {
            try {RecoveryCrypto.decrypt(Arrays.copyOf(encrypted,n),seed);throw new AssertionError("Truncation accepted");}catch(java.security.GeneralSecurityException|IllegalArgumentException expected){}
        }
        for(int i=0;i<100;i++) {
            byte[] random=RecoveryPhrase.generate();
            for(String lang:languages) check(Arrays.equals(random,RecoveryPhrase.parse(RecoveryPhrase.format(random,lang))));
        }
        System.out.println("PASS: 10 languages, 1000 phrase round trips, independent-key restore, random nonces, wrong key, byte tampering, truncation.");
        if(args.length>0) {
            java.nio.file.Files.write(java.nio.file.Path.of(args[0],"cipher.bin"),encrypted);
            java.nio.file.Files.write(java.nio.file.Path.of(args[0],"plain.bin"),plain);
        }
    }
}
