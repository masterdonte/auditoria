package br.gov.ma.emap.auditoria.utils;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.io.UnsupportedEncodingException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class AuditoriaUtils {

    private static final Pattern CONTRATO_PATTERN = Pattern.compile("^(\\d{1,3})/(\\d{4})(?:/\\d{2})?$");

    private AuditoriaUtils() {
    }

    public static String normalizar(String contrato) {
        if (contrato == null || contrato.isBlank()) {
            return null;
        }
        Matcher matcher = CONTRATO_PATTERN.matcher(contrato.trim());
        if (!matcher.matches()) {
            return null;
        }
        int numero = Integer.parseInt(matcher.group(1));
        String ano = matcher.group(2);
        return String.format("%03d/%s", numero, ano);
    }

    public static String gerarSecretOld() {
        SecretKey key = Jwts.SIG.HS256.key().build();
        return Encoders.BASE64.encode(key.getEncoded());
    }

    private SecretKey getSigningKeyOld(String secret) {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public static String gerarSecret() {
        SecretKey key = Jwts.SIG.HS256.key().build();
        return Encoders.BASE64URL.encode(key.getEncoded());
    }

    private SecretKey getSigningKey(String secret) {
        byte[] keyBytes = Decoders.BASE64URL.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public static void main(String[] args) throws UnsupportedEncodingException {
        String secret = gerarSecret();
        System.out.println(secret);

        //System.out.println(Encoders.BASE64.encode(key.getEncoded()));
        //System.out.println(secret.equals("FOjQxZT3+d3t2lJiGClAiVhHBkidiGxpBkhhuZ4Qz3E="));

        //byte[] keyBytes = Decoders.BASE64.decode(secret);
        //System.out.println(keyBytes);
    }

}