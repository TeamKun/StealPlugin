package jp.yukiat.stealplugin.enums;

public enum ArmorType
{
    HELMET("帽子"),
    CHESTPLATE("服"),
    LEGGINGS("パンツ"),
    BOOTS("靴");

    private final String displayName;

    ArmorType(String displayName)
    {
        this.displayName = displayName;
    }

    public String getDisplayName()
    {
        return displayName;
    }
}