package com.samsung.remotespen.core.device.ble.constants;

import java.util.UUID;

/* loaded from: classes.dex */
public class BleSpenUuid {
    public static final String UUID_REPORT = "00002a4d-0000-1000-8000-00805f9b34fb";
    public static final UUID BUTTON_EVENT = UUID.fromString("6c290d2e-1c03-aca1-ab48-a9b908bae79e");
    public static final UUID BATTERY_LEVEL = UUID.fromString("5a87b4ef-3bfa-76a8-e642-92933c31434f");
    public static final UUID BATTERY_LEVEL_RAW = UUID.fromString("5b87b4ef-3bfa-76a8-e642-92933c31435f");
    public static final UUID FW_VER = UUID.fromString("a1a6932a-2f2e-1c03-2e1c-03ac2e1c03ac");
    public static final UUID MODE = UUID.fromString("aca1ab48-08ba-e79e-ab48-a9b9e6429293");
    public static final UUID CHARGE_STATUS = UUID.fromString("92933c31-41d8-bda6-3c31-434fab48a9b9");
    public static final UUID SELF_TEST = UUID.fromString("8BD867D3-D619-45D9-8EE0-3814DBD5B3F0");
    public static final UUID RAW_SENSOR_DATA = UUID.fromString("DDB42396-CA00-4DB3-B87D-2EE458279360");
    public static final UUID EASY_CONNECT_ID = UUID.fromString("9659309c-0a26-48c9-b182-3161bda237cb");
    public static final UUID FMM_CONFIG = UUID.fromString("8f10aea6-74fe-4aca-946d-71723d9ecd2d");
    public static final UUID LED_STATE = UUID.fromString("2287a97c-17d7-4845-ace9-1f42f4bd034a");
    public static final UUID PEN_TIP_APPROACH = UUID.fromString("47cd9300-cf99-4899-be73-59c7f1c0557c");
    public static final UUID PEN_LOG = UUID.fromString("fe3c10ee-16dd-4b73-9a37-e1e6024a3848");
    public static final UUID PEN_FREQUENCY = UUID.fromString("740052ae-ac12-47f1-89c8-9f2e23588bca");
    public static final UUID OBFUSCATION_TABLE = UUID.fromString("ade287e3-256c-4e7d-b0c6-03c7e5bfc4c1");
    public static final UUID PEN_SYNC_CLOCK = UUID.fromString("f641f992-3d9a-495f-9e07-19fe2418b867");
    public static final UUID CHARACTERISTIC_CONFIG = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    public static String getName(String str) {
        return str == null ? "null" : BUTTON_EVENT.toString().equals(str) ? "UUID_BUTTON_EVENT" : BATTERY_LEVEL.toString().equals(str) ? "UUID_BATTERY_LEVEL" : BATTERY_LEVEL_RAW.toString().equals(str) ? "UUID_BATTERY_LEVEL_RAW" : FW_VER.toString().equals(str) ? "UUID_FW_VER" : MODE.toString().equals(str) ? "UUID_MODE" : CHARGE_STATUS.toString().equals(str) ? "UUID_CHARGE_STATUS" : CHARACTERISTIC_CONFIG.toString().equals(str) ? "UUID_CHARACTERISTIC_CONFIG" : SELF_TEST.toString().equals(str) ? "UUID_SELF_TEST" : RAW_SENSOR_DATA.toString().equals(str) ? "UUID_RAW_SENSOR_DATA" : EASY_CONNECT_ID.toString().equals(str) ? "UUID_EASY_CONNECT_ID" : FMM_CONFIG.toString().equals(str) ? "UUID_FMM_CONFIG" : PEN_TIP_APPROACH.toString().equals(str) ? "UUID_PEN_TIP_APPROACH" : LED_STATE.toString().equals(str) ? "UUID_LED_STATE" : PEN_LOG.toString().equals(str) ? "UUID_PEN_LOG" : PEN_FREQUENCY.toString().equals(str) ? "UUID_PEN_FREQUENCY" : OBFUSCATION_TABLE.toString().equals(str) ? "UUID_OBFUSCATION_TABLE" : PEN_SYNC_CLOCK.toString().equals(str) ? "UUID_PEN_SYNC_CLOCK" : "UNKNOWN_UUID";
    }

    public static String getName(UUID uuid) {
        return uuid == null ? "null" : getName(uuid.toString());
    }
}
