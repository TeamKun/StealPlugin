package jp.yukiat.stealplugin.utils;

import java.util.*;

public class RandomUtil
{
    @SafeVarargs
    public static <T> T pickRandom(T... t)
    {
        return t[new Random().nextInt(t.length)];
    }
}
