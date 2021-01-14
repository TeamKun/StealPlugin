package jp.yukiat.stealplugin.config;

public class Skin
{
    private final String value;
    private final String signature;

    public Skin(String value, String signature)
    {
        this.value = value;
        this.signature = signature;
    }

    public static Skin getEmptyObject()
    {
        return new Skin("", "");
    }

    public String getValue()
    {
        return value;
    }

    public String getSignature()
    {
        return signature;
    }

    public boolean isEmpty()
    {
        return value == null || signature == null || value.equals("") || signature.equals("");
    }
}
