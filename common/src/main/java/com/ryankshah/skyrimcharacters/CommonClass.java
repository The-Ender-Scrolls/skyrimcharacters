package com.ryankshah.skyrimcharacters;

import com.ryankshah.skyrimcharacters.network.Networking;

public class CommonClass
{
    public static void init() {
        Networking.load();
    }
}