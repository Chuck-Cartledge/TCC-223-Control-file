import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class Base64Helper {
    public static String encode(String plainString) {
        Base64.Encoder encoder = Base64.getEncoder();
        return encoder.encodeToString(plainString.getBytes(StandardCharsets.UTF_8));
    }

    public static String decode(String encodedString) {
        Base64.Decoder decoder = Base64.getDecoder();
        return new String(decoder.decode(encodedString), StandardCharsets.UTF_8);
    }
}
