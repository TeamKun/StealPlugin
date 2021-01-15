package jp.yukiat.stealplugin.enums;

public enum ArmorType
{
    // HELMET("帽子"),
    BOOTS("靴"),
    CHESTPLATE("上着"),
    LEGGINGS("パンツ"),
    ALL("");

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
