package com.wisp.finance.security;
import java.text.Normalizer;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.*;
/** Wisp Finance recovery phrase v1 (not BIP39). 23 random bytes + 1 checksum byte.
 * Each token encodes one byte as two localized words joined with a hyphen.
 * Never reorder or modify the frozen dictionaries: old written phrases must remain valid.
 */
public final class RecoveryPhrase {
    private static final Map<String,String[][]> WORDS = new LinkedHashMap<>();
    static {
        WORDS.put("en", new String[][]{"amber april arctic autumn light calm cedar clear cobalt coral crystal dawn garden gold green moon".split(" "), "anchor bird bridge cloud field forest harbor island lake leaf meadow night river stone sun willow".split(" ")});
        WORDS.put("ru", new String[][]{"янтарь апрель арктика осень свет покой кедр ясность кобальт коралл кристалл рассвет сад золото зелень луна".split(" "), "якорь птица мост облако поле лес гавань остров озеро лист луг ночь река камень солнце ива".split(" ")});
        WORDS.put("uk", new String[][]{"бурштин квітень арктика осінь світло спокій кедр ясність кобальт корал кристал світанок сад золото зелень місяць".split(" "), "якір птах міст хмара поле ліс гавань острів озеро лист луг ніч річка камінь сонце верба".split(" ")});
        WORDS.put("de", new String[][]{"bernstein april arktis herbst licht ruhe zeder klarheit kobalt koralle kristall morgen garten gold grün mond".split(" "), "anker vogel brücke wolke feld wald hafen insel see blatt wiese nacht fluss stein sonne weide".split(" ")});
        WORDS.put("es", new String[][]{"ámbar abril ártico otoño luz calma cedro claridad cobalto coral cristal alba jardín oro verde luna".split(" "), "ancla ave puente nube campo bosque puerto isla lago hoja prado noche río piedra sol sauce".split(" ")});
        WORDS.put("fr", new String[][]{"ambre avril arctique automne lumière calme cèdre clarté cobalt corail cristal aube jardin or vert lune".split(" "), "ancre oiseau pont nuage champ forêt port île lac feuille prairie nuit rivière pierre soleil saule".split(" ")});
        WORDS.put("it", new String[][]{"ambra aprile artico autunno luce calma cedro chiarezza cobalto corallo cristallo alba giardino oro verde luna".split(" "), "ancora uccello ponte nuvola campo foresta porto isola lago foglia prato notte fiume pietra sole salice".split(" ")});
        WORDS.put("nl", new String[][]{"amber april noordpool herfst licht rust ceder helderheid kobalt koraal kristal dageraad tuin goud groen maan".split(" "), "anker vogel brug wolk veld bos haven eiland meer blad weide nacht rivier steen zon wilg".split(" ")});
        WORDS.put("pl", new String[][]{"bursztyn kwiecień arktyka jesień światło spokój cedr jasność kobalt koral kryształ świt ogród złoto zieleń księżyc".split(" "), "kotwica ptak most chmura pole las port wyspa jezioro liść łąka noc rzeka kamień słońce wierzba".split(" ")});
        WORDS.put("pt", new String[][]{"âmbar abril ártico outono luz calma cedro clareza cobalto coral cristal alvorada jardim ouro verde lua".split(" "), "âncora ave ponte nuvem campo floresta porto ilha lago folha prado noite rio pedra sol salgueiro".split(" ")});
    }
    private RecoveryPhrase() {}
    public static byte[] generate() { byte[] b=new byte[23]; new SecureRandom().nextBytes(b); return b; }
    private static byte checksum(byte[] b) {
        try {
            MessageDigest digest=MessageDigest.getInstance("SHA-256");
            digest.update("Wisp Finance recovery v1".getBytes(StandardCharsets.UTF_8));
            return digest.digest(b)[0];
        } catch (GeneralSecurityException e) { throw new IllegalStateException(e); }
    }
    public static String normalize(String input) {
        return Normalizer.normalize(input,Normalizer.Form.NFKD).replaceAll("\\p{M}", "")
            .toLowerCase(Locale.ROOT).replace('–','-').replace('—','-').replace('‑','-')
            .trim().replaceAll("[\\s\\p{Z}]+"," ");
    }
    public static String format(byte[] entropy,String language) {
        if(entropy.length!=23) throw new IllegalArgumentException("Invalid recovery key");
        String[][] dict=WORDS.getOrDefault(language.split("-")[0],WORDS.get("en"));
        byte[] all=Arrays.copyOf(entropy,24); all[23]=checksum(entropy);
        List<String> tokens=new ArrayList<>();
        for(byte b:all) { int v=b&255; tokens.add(dict[0][v>>>4]+"-"+dict[1][v&15]); }
        return String.join(" ",tokens);
    }
    public static byte[] parse(String input) {
        String[] tokens=normalize(input).split(" ");
        if(tokens.length!=24) throw new IllegalArgumentException("Expected 24 tokens");
        byte[] found=null;
        for(String[][] dict:WORDS.values()) {
            byte[] all=new byte[24]; boolean valid=true;
            for(int i=0;i<24;i++) {
                String[] pair=tokens[i].split("-",-1);
                if(pair.length!=2) { valid=false; break; }
                int left=-1,right=-1;
                for(int j=0;j<16;j++) {
                    if(normalize(dict[0][j]).equals(pair[0])) left=j;
                    if(normalize(dict[1][j]).equals(pair[1])) right=j;
                }
                if(left<0||right<0) { valid=false; break; }
                all[i]=(byte)((left<<4)|right);
            }
            byte[] seed=Arrays.copyOf(all,23);
            if(valid&&checksum(seed)==all[23]) {
                if(found!=null&&!Arrays.equals(found,seed)) throw new IllegalArgumentException("Ambiguous phrase");
                found=seed;
            }
        }
        if(found==null) throw new IllegalArgumentException("Invalid recovery phrase");
        return found;
    }
}
