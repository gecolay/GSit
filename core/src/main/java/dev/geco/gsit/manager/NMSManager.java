package dev.geco.gsit.manager;

import java.util.*;
import java.lang.reflect.*;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class NMSManager {

    private static final Map<Class<?>, Class<?>> CORRESPONDING_TYPES = new HashMap<Class<?>, Class<?>>();

    private static Class<?> getPrimitiveType(Class<?> Class) { return CORRESPONDING_TYPES.getOrDefault(Class, Class); }

    private static Class<?>[] toPrimitiveTypeArray(Class<?>[] Classes) {
        int L = Classes != null ? Classes.length : 0;
        Class<?>[] T = new Class<?>[L];
        for(int i = 0; i < L; i++) T[i] = getPrimitiveType(Classes[i]);
        return T;
    }

    private static boolean equalsTypeArray(Class<?>[] Value1, Class<?>[] Value2) {
        if(Value1.length != Value2.length) return false;
        for(int i = 0; i < Value1.length; i++) if(!Value1[i].equals(Value2[i]) && !Value1[i].isAssignableFrom(Value2[i])) return false;
        return true;
    }

    private static boolean classListEqual(Class<?>[] Value1, Class<?>[] Value2) {
        if(Value1.length != Value2.length) return false;
        for(int i = 0; i < Value1.length; i++) if(Value1[i] != Value2[i]) return false;
        return true;
    }

    public static String getClassVersion() {
        String V = Bukkit.getServer().getClass().getPackage().getName();
        return V.substring(V.lastIndexOf('.') + 1);
    }

    public static String getPackageVersion() {
        return getClassVersion() + (isVersion(17, 1) ? "_2" : "");
    }

    public static String getVersion() {
        return Bukkit.getBukkitVersion().substring(0, Bukkit.getBukkitVersion().indexOf('-'));
    }

    public static boolean isNewerOrVersion(long Version, int SubVersion) {
        String[] V = getVersion().split("\\.");
        return V.length > 1 && (V.length > 2 ? Long.parseLong(V[1]) >= Version && Long.parseLong(V[2]) >= SubVersion : Long.parseLong(V[1]) >= Version);
    }

    public static boolean isVersion(long Version, int SubVersion) {
        String[] V = getVersion().split("\\.");
        return V.length > 1 && (V.length > 2 ? Long.parseLong(V[1]) == Version && Long.parseLong(V[2]) == SubVersion : Long.parseLong(V[1]) == Version);
    }

    public static boolean isNMSCompatible() {
        try {
            Class.forName("net.minecraft.server.level.EntityPlayer");
            return true;
        } catch(Exception e) {
            try {
                Class.forName("net.minecraft.server." + getClassVersion() + ".Entity");
                return true;
            } catch(Exception ex) { }
        }
        return false;
    }

    public static Field getField(Class<?> Class, String Field) {
        try {
            Field F = Class.getDeclaredField(Field);
            F.setAccessible(true);
            return F;
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Object getPackageObject(String Name, String ClassName, Object O) {
        try {
            Class<?> sm = Class.forName("dev.geco." + Name + ".mcv." + NMSManager.getPackageVersion() + "." + ClassName);
            return O == null ? sm.getConstructor().newInstance() : sm.getConstructor(O.getClass()).newInstance(O);
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Class<?> getNMSClass(String ClassName) {
        Class<?> C = null;
        try { return Class.forName("net.minecraft.server." + getClassVersion() + "." + ClassName); } catch (Exception e) { }
        return C;
    }

    public static Class<?> getOBCClass(String ClassName) {
        Class<?> C = null;
        try { return Class.forName("org.bukkit.craftbukkit." + getClassVersion() + "." + ClassName); } catch (Exception e) { }
        return C;
    }

    public static Method getMethod(Class<?> Class, String ClassName, Class<?>... Parameters) {
        for(Method M : Class.getMethods()) if(M.getName().equals(ClassName) && (Parameters.length == 0 || classListEqual(Parameters, M.getParameterTypes()))) {
            M.setAccessible(true);
            return M;
        }
        return null;
    }

    public static Method getMethod(String MethodName, Class<?> Class, Class<?>... Parameters) {
        Class<?>[] T = toPrimitiveTypeArray(Parameters);
        for(Method M : Class.getMethods()) if(M.getName().equals(MethodName) && equalsTypeArray(toPrimitiveTypeArray(M.getParameterTypes()), T)) return M;
        return null;
    }

    public static Class<?> getClass(Class<?> Class, String Search) {
        for(Class<?> C : Class.getClasses()) if(C.getSimpleName().equals(Search)) return C;
        return null;
    }

    public static Object invokeMethod(String MethodName, Object Parameter) {
        try { return getMethod(MethodName, Parameter.getClass()).invoke(Parameter); }
        catch (Exception e) {
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

    public static void sendPacket(Player P, Object Packet) { sendPacket(getHandle(P), Packet); }

    public static void sendPacket(Object P, Object Packet) {
        try {
            Object p = P.getClass().getField("playerConnection").get(P);
            p.getClass().getMethod("sendPacket", getNMSClass("Packet")).invoke(p, Packet);
        } catch(Exception e) { e.printStackTrace(); }
    }

    public static boolean set(Object Object, String Field, Object Value) {
        Class<?> C = Object.getClass();
        while(C != null) {
            try {
                Field F = C.getDeclaredField(Field);
                F.setAccessible(true);
                F.set(Object, Value);
                return true;
            } catch (NoSuchFieldException e) { C = C.getSuperclass(); } catch (Exception e) { throw new IllegalStateException(e); }
        }
        return false;
    }

}