package com.togun.tmod.skin;

public class SkinData {
    private final String value;
    private final String signature;
    private final boolean official;

    public SkinData(String value, String signature) {
        this(value, signature, true);
    }

    public SkinData(String value, String signature, boolean official) {
        this.value = value;
        this.signature = signature;
        this.official = official;
    }

    public String getValue() {
        return value;
    }

    public String getSignature() {
        return signature;
    }

    public boolean isOfficial() {
        return official;
    }

    public boolean isValid() {
        return value != null && !value.isEmpty() && signature != null && !signature.isEmpty();
    }
}

