package utils;

import java.util.Map;

public class BandecosLista {

    private static final Map<String, String> bandecos = Map.of(
            "1", "EACH",
            "2", "Central",
            "3", "Química",
            "4", "Física"
    );

    public static String get(String chave) {
        return bandecos.get(chave);
    }
}
