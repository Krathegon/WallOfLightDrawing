package com.example.drawingapp.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HexColorValidator {
    private static final String HEX_PATTERN = "^#([A-Fa-f0-9]{8}|[A-Fa-f0-9]{6}[A-Fa-f0-9]{4}||[A-Fa-f0-9]{3})$";

    public static boolean isValid(final String hexColorCode) {
        Pattern pattern = Pattern.compile(HEX_PATTERN);

        Matcher matcher = pattern.matcher(hexColorCode);
        return matcher.matches();

    }
}
