package jp.yukiat.stealplugin.enums;

public enum MaterialType
{
    LEATHER("革の"),
    GOLD("金の"),
    CHAIN("チェーンの"),
    DIAMOND("ダイアモンドの");

    private final String displayName;

    MaterialType(String displayName)
    {
        this.displayName = displayName;
    }

    public String getDisplayName()
    {
        return displayName;
    }
}