package dev.geco.gsit.manager;

import java.util.*;
import java.lang.reflect.*;

import org.bukkit.*;

public class NMSManager {

    private static final Map<Class<?>, Class<?>> CORRESPONDING_TYPES = new HashMap<>();

    private static Class<?> getPrimitiveType(Class<?> Class) { return CORRESPONDING_TYPES.getOrDefault(Class, Class); }

    private static Class<?>[] toPrimitiveTypeArray(Class<?>[] Classes) {

        int length = Classes != null ? Classes.length : 0;

        Class<?>[] type = new Class<?>[length];

        for(int count = 0; count < length; count++) type[count] = getPrimitiveType(Classes[count]);

        return type;
    }

    private static boolean equalsTypeArray(Class<?>[] Type, Class<?>[] OtherType) {

        if(Type.length != OtherType.length) return false;

        for(int count = 0; count < Type.length; count++) if(!Type[count].equals(OtherType[count]) && !Type[count].isAssignableFrom(OtherType[count])) return false;

        return true;
    }

    private static boolean classListEqual(Class<?>[] Type, Class<?>[] OtherType) {

        if(Type.length != OtherType.length) return false;

        for(int count = 0; count < Type.length; count++) if(Type[count] != OtherType[count]) return false;

        return true;
    }

    public static String getClassVersion() {

        String version = Bukkit.getServer().getClass().getPackage().getName();

        return version.substring(version.lastIndexOf('.') + 1);
    }

    public static String getPackageVersion() {
        return getClassVersion() + (isVersion(17, 1) ? "_2" : "");
    }

    public static String getVersion() { return Bukkit.getBukkitVersion().substring(0, Bukkit.getBukkitVersion().indexOf('-')); }

    public static boolean isNewerOrVersion(long Version, int SubVersion) {

        String[] version = getVersion().split("\\.");

        return version.length > 1 && (version.length > 2 ? Long.parseLong(version[1]) >= Version && Long.parseLong(version[2]) >= SubVersion : Long.parseLong(version[1]) >= Version);
    }

    public static boolean isVersion(long Version, int SubVersion) {

        String[] version = getVersion().split("\\.");

        return version.length > 1 && (version.length > 2 ? Long.parseLong(version[1]) == Version && Long.parseLong(version[2]) == SubVersion : Long.parseLong(version[1]) == Version);
    }

    public static Field getField(Class<?> Class, String Field) {

        try {

            Field F = Class.getDeclaredField(Field);

            F.setAccessible(true);

            return F;
        } catch (Exception e) {

            e.printStackTrace();

            return null;
        }
    }

    public static Object getPackageObject(String Name, String ClassName, Object O) {

        try {

            Class<?> sm = Class.forName("dev.geco." + Name + ".mcv." + NMSManager.getPackageVersion() + "." + ClassName);

            return O == null ? sm.getConstructor().newInstance() : sm.getConstructor(O.getClass()).newInstance(O);
        } catch (Exception e) { return null; }
    }

    public static Class<?> getNMSClass(String ClassName) {

        Class<?> C = null;

        try { return Class.forName("net.minecraft.server." + getClassVersion() + "." + ClassName); } catch (Exception ignored) { }

        return null;
    }

    public static Class<?> getOBCClass(String ClassName) {

        Class<?> C = null;

        try { return Class.forName("org.bukkit.craftbukkit." + getClassVersion() + "." + ClassName); } catch (Exception ignored) { }

        return null;
    }

    public static Method getMethod(Class<?> Class, String ClassName, Class<?>... Parameters) {

        for(Method method : Class.getMethods()) if(method.getName().equals(ClassName) && (Parameters.length == 0 || classListEqual(Parameters, method.getParameterTypes()))) {

            method.setAccessible(true);

            return method;
        }

        return null;
    }

    public static Method getMethod(String MethodName, Class<?> Class, Class<?>... Parameters) {

        Class<?>[] type = toPrimitiveTypeArray(Parameters);

        for(Method method : Class.getMethods()) if(method.getName().equals(MethodName) && equalsTypeArray(toPrimitiveTypeArray(method.getParameterTypes()), type)) return method;

        return null;
    }

    public static Class<?> getClass(Class<?> Class, String Search) {

        for(Class<?> sClass : Class.getClasses()) if(sClass.getSimpleName().equals(Search)) return sClass;

        return null;
    }

    public static Object invokeMethod(String MethodName, Object Parameter) {

        try {

            return getMethod(MethodName, Parameter.getClass()).invoke(Parameter);
        } catch (Exception e) {

            e.printStackTrace();

            return null;
        }
    }

    public static Object getHandle(Object O) {

        try {

            Method m = O.getClass().getDeclaredMethod("getHandle");

            m.setAccessible(true);

            return m.invoke(O);
        } catch (Exception e) {

            e.printStackTrace();

            return O;
        }
    }

    public static boolean set(Object Object, String Field, Object Value) {

        Class<?> C = Object.getClass();

        while(C != null) {

            try {

                Field F = C.getDeclaredField(Field);

                F.setAccessible(true);

                F.set(Object, Value);

                return true;
            } catch (NoSuchFieldException e) {

                C = C.getSuperclass();
            } catch (Exception e) {

                throw new IllegalStateException(e);
            }
        }

        return false;
    }

}