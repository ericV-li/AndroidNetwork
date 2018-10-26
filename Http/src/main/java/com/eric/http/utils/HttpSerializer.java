package com.eric.http.utils;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author li
 * @Package com.eric.http.utils
 * @Title: HttpSerializer
 * @Description: Copyright (c)
 * Create DateTime: 2017/11/29
 */
public class HttpSerializer {
    private static final String LOG_TAG = HttpSerializer.class.getSimpleName();

    /**
     * 序列化对象转Map
     *
     * @param object 序列化对象
     * @param map    Map对象
     * @param <T>    泛型
     */
    public static <T> void serializeObject2Map(T object, Map<String, Object> map) {
        if (object == null || map == null) {
            return;
        }
        Class<?> objectClass = object.getClass();
        Class<?> superClass = objectClass.getSuperclass();
        Field[] fields = null;
        if (superClass != null) {
            fields = superClass.getDeclaredFields();
            for (Field field : fields) {
                readValue(field, object, map);
            }
        }
        fields = objectClass.getDeclaredFields();
        for (Field field : fields) {
            readValue(field, object, map);
        }
    }

    /**
     * 拷贝数组
     *
     * @param des   目标字节数组
     * @param bytes 源字节数组
     */
    public static void setByteToCollector(byte[] des, byte[] bytes) {
        for (int i = 0; i < bytes.length; i++) {
            des[i] = bytes[i];
        }
    }

    /**
     * Deserializes json data to object.
     *
     * @param object 泛型目标对象
     * @param json   JSONObject对象
     */
    public static <T> void deserializeJSONObject(T object, JSONObject json) {
        if (object == null || json == null) {
            return;
        }
        Class<?> objectClass = object.getClass();
        Class<?> superClass = objectClass.getSuperclass();
        Field[] fields = null;
        if (superClass != null) {
            fields = superClass.getDeclaredFields();
            for (Field field : fields) {
                writeValue(field, object, json);
            }
        }
        fields = objectClass.getDeclaredFields();
        for (Field field : fields) {
            writeValue(field, object, json);
        }
    }

    /**
     * Deserializes json array data to list.
     *
     * @param elementClassType Class字节码类型
     * @param array            JSONArray对象
     * @return
     */
    public static <T> List<T> deserializeJSONArray(Class<T> elementClassType, JSONArray array) {
        List<T> list = new ArrayList<T>();
        if (elementClassType == null || array == null || array.length() == 0) {
            return list;
        }
        for (int i = 0; i < array.length(); i++) {
            try {
                T object = elementClassType.newInstance();
                deserializeJSONObject(object, array.getJSONObject(i));
                list.add(object);
            } catch (JSONException e) {
                Log.e(LOG_TAG, "deserializeJSONArray.JSONException: " + e.getMessage());
            } catch (IllegalAccessException e) {
                Log.e(LOG_TAG, "deserializeJSONArray.IllegalAccessException: " + e.getMessage());
                break;
            } catch (InstantiationException e) {
                Log.e(LOG_TAG, "deserializeJSONArray.InstantiationException: " + e.getMessage());
                break;
            }
        }
        return list;
    }

    /**
     * 反射序列化写值
     *
     * @param field  域
     * @param object 泛型对象
     * @param bytes  字节数组
     * @param <T>    泛型
     */
    private static <T> void writeValue(Field field, T object, byte[] bytes) {
        Object value = null;

        try {
            value = new byte[1024 * 1024 * 2];
            Class<?> actualClass = field.getType();
            // String name = field.getName();
            field.setAccessible(true);

            if (actualClass == byte[].class) {
                value = bytes;
            }
            field.set(object, value);

        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    /**
     * 反射序列化写值
     *
     * @param field  域
     * @param object 泛型对象
     * @param json   JSONObject对象
     * @param <T>    泛型对象
     */
    private static <T> void writeValue(Field field, T object, JSONObject json) {
        Object value = null;
        try {
            Class<?> actualClass = field.getType();
            String name = field.getName();
            if (json.isNull(name)) {
                return;
            }
            field.setAccessible(true);
            if (actualClass == int.class || actualClass == Integer.class) {
                value = json.optInt(name);
            } else if (actualClass == String.class) {
                value = json.optString(name);
            } else if (actualClass == boolean.class || actualClass == Boolean.class) {
                value = json.optBoolean(name);
            } else if (actualClass == double.class || actualClass == Double.class) {
                value = json.optDouble(name);
            } else if (actualClass == float.class || actualClass == Float.class) {
                value = (float) json.optDouble(name);
            } else if (actualClass == long.class || actualClass == Long.class) {
                value = json.optLong(name);
            } else if (List.class.isAssignableFrom(actualClass)) {
                JSONSerializeAnnotation attr = field.getAnnotation(JSONSerializeAnnotation.class);
                if (attr != null) {
                    Class<?> subClass = attr.actualClass();
                    value = deserializeJSONArray(subClass, json.getJSONArray(name));
                }
            } else if (actualClass == JSONObject.class) {
                value = json.getJSONObject(name);
            } else if (actualClass == JSONArray.class) {
                value = json.getJSONArray(name);
            } else if (actualClass == Object.class) {
                value = field.get(object);
                if (value != null) {
                    if (value instanceof String) {
                        value = json.getJSONObject(name).toString();
                    } else {
                        deserializeJSONObject(value, json.getJSONObject(name));
                    }
                } else {
                    value = json.opt(name);
                }
            } else {
                value = field.get(object);
                // when the field object is null, new the object
                if (value == null) {
                    value = actualClass.newInstance();
                }
                deserializeJSONObject(value, json.getJSONObject(name));

            }
            field.set(object, value);
        } catch (IllegalAccessException e) {
            Log.w(LOG_TAG, "writeValue.IllegalAccessException: " + e.getMessage());
        } catch (InstantiationException e) {
            Log.w(LOG_TAG, "writeValue.InstantiationException: " + e.getMessage());
        } catch (JSONException e) {
            Log.w(LOG_TAG, "writeValue.JSONException: " + e.getMessage());
        }
    }

    /**
     * 反射序列化读值
     *
     * @param field  域
     * @param object 泛型对象
     * @param map    目标Map对象
     * @param <T>    泛型
     */
    private static <T> void readValue(Field field, T object, Map<String, Object> map) {
        try {
            Class<?> actualClass = field.getType();
            String name = field.getName();
            field.setAccessible(true);
            if (actualClass == int.class) {
                int value = field.getInt(object);
                map.put(name, String.valueOf(value));
            } else if (actualClass == Integer.class) {
                Object value = field.get(object);
                if (value != null) {
                    map.put(name, String.valueOf(value));
                }
            } else if (actualClass == String.class) {
                Object value = field.get(object);
                if (value != null) {
                    map.put(name, String.valueOf(value));
                }
            } else if (actualClass == boolean.class) {
                boolean value = field.getBoolean(object);
                map.put(name, String.valueOf(value));
            } else if (actualClass == Boolean.class) {
                Object value = field.get(object);
                if (value != null) {
                    map.put(name, String.valueOf(value));
                }
            } else if (actualClass == double.class) {
                double value = field.getDouble(object);
                map.put(name, String.valueOf(value));
            } else if (actualClass == Double.class) {
                Object value = field.get(object);
                if (value != null) {
                    map.put(name, String.valueOf(value));
                }
            } else if (actualClass == float.class) {
                float value = field.getFloat(object);
                map.put(name, String.valueOf(value));
            } else if (actualClass == Float.class) {
                Object value = field.get(object);
                if (value != null) {
                    map.put(name, String.valueOf(value));
                }
            } else if (actualClass == long.class) {
                long value = field.getLong(object);
                map.put(name, String.valueOf(value));
            } else if (actualClass == Long.class) {
                Object value = field.get(object);
                if (value != null) {
                    map.put(name, String.valueOf(value));
                }
            } else if (List.class.isAssignableFrom(actualClass)) {
                List<?> list = (List<?>) field.get(object);
                if (list != null && !list.isEmpty()) {
                    Object[] array = new Object[list.size()];
                    for (int i = 0; i < list.size(); i++) {
                        Map<String, Object> subMap = new HashMap<String, Object>();
                        serializeObject2Map(list.get(i), subMap);
                        if (!subMap.isEmpty()) {
                            array[i] = subMap;
                        }
                    }
                    map.put(name, array);
                }
            } else {
                // empty
            }
        } catch (IllegalAccessException e) {
            Log.w(LOG_TAG, "writeValue.IllegalAccessException: " + e.getMessage());
        }
    }

    /**
     * 反射序列化对象到目标JsonObject对象
     *
     * @param object 泛型对象
     * @param json   JSONObject对象
     * @param <T>    泛型
     */
    public static <T> void serializeObject2JSONObject(T object, JSONObject json) {
        if (object == null || json == null) {
            return;
        }
        Class<?> objectClass = object.getClass();
        Class<?> superClass = objectClass.getSuperclass();
        Field[] fields = null;
        if (superClass != null) {
            fields = superClass.getDeclaredFields();
            for (Field field : fields) {
                Object obj = readValue(field, object);
                if (obj != null) {
                    String name = field.getName();
                    try {
                        json.put(name, obj);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        fields = objectClass.getDeclaredFields();
        for (Field field : fields) {
            Object obj = readValue(field, object);
            if (obj != null) {
                String name = field.getName();
                try {
                    json.put(name, obj);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 反射读域值
     *
     * @param field  域
     * @param object 泛型对象
     * @param <T>    泛型
     * @return
     */
    private static <T> Object readValue(Field field, T object) {
        try {
            Class<?> actualClass = field.getType();
            field.setAccessible(true);
            if (List.class.isAssignableFrom(actualClass)) {
                List<?> list = (List<?>) field.get(object);
                if (list != null && !list.isEmpty()) {
                    JSONArray array = new JSONArray();
                    for (int i = 0; i < list.size(); i++) {
                        JSONObject json = new JSONObject();
                        serializeObject2JSONObject(list.get(i), json);
                        array.put(json);
                    }
                    return array;
                }
            } else {
                return field.get(object);
            }
        } catch (IllegalAccessException e) {
            Log.w(LOG_TAG, "writeValue.IllegalAccessException: " + e.getMessage());
        }
        return null;
    }

    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    public static @interface JSONSerializeAnnotation {
        public Class<?> actualClass();
    }
}
