package com.example.trello;

import java.util.UUID;

public class UniqueCodeGenerator {
    public static String generateUniqueCode() {

        String uuid = UUID.randomUUID().toString();

        String withoutHyphens = uuid.replaceAll("-", "");

        String substring = withoutHyphens.substring(0, 20);

        //String upperCaseSubstring = substring.toUpperCase();

        return substring;
    }
}
