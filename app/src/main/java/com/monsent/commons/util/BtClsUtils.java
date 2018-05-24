package com.monsent.commons.util;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * 蓝牙2.0配对函数
 */
public class BtClsUtils {

    /**
     * 与设备配对 参考源码：platform/packages/apps/Settings.git
     * /Settings/src/com/android/settings/bluetooth/CachedBluetoothDevice.java
     */
    public static boolean createBond(Class<? extends BluetoothDevice> btClass, BluetoothDevice btDevice) throws Exception {
        Method createBondMethod = btClass.getMethod("createBond");
        return (Boolean) createBondMethod.invoke(btDevice);
    }

    /**
     * 与设备解除配对 参考源码：platform/packages/apps/Settings.git
     * /Settings/src/com/android/settings/bluetooth/CachedBluetoothDevice.java
     */
    public static boolean removeBond(Class<? extends BluetoothDevice> btClass, BluetoothDevice btDevice) throws Exception {
        Method removeBondMethod = btClass.getMethod("removeBond");
        return (Boolean) removeBondMethod.invoke(btDevice);
    }

    /**
     * 取消配对
     *
     * @param btClass
     * @param device
     * @return
     * @throws Exception
     */
    public static boolean cancelBondProcess(Class<? extends BluetoothDevice> btClass, BluetoothDevice device) throws Exception {
        Method cancelBondProcessMethod = btClass.getMethod("cancelBondProcess");
        return (Boolean) cancelBondProcessMethod.invoke(device);
    }

    /**
     * 设置PIN码
     *
     * @param btClass
     * @param btDevice 设备
     * @param pin      PIN码
     * @return 是否成功
     */
    public static boolean setPin(Class<?> btClass, BluetoothDevice btDevice, String pin) throws Exception {
        Method setPinMethod = btClass.getDeclaredMethod("setPin", new Class[]{byte[].class});
        return (Boolean) setPinMethod.invoke(btDevice, new Object[]{pin.getBytes()});

    }

    /**
     * 取消用户输入
     *
     * @param btClass
     * @param device
     * @return
     * @throws Exception
     */
    public static boolean cancelPairingUserInput(Class<? extends BluetoothDevice> btClass, BluetoothDevice device) throws Exception {
        Method cancelPairingUserInputMethod = btClass.getMethod("cancelPairingUserInput");
//         cancelBondProcess(btClass, device);
        return (Boolean) cancelPairingUserInputMethod.invoke(device);
    }

    /**
     * 确认配对
     *
     * @param device
     * @param confirmed
     * @return
     */
    public static boolean setPairingConfirmation(Class<? extends BluetoothDevice> btClass, BluetoothDevice device, boolean confirmed) throws Exception {
        Method setPairingConfirmationMethod = btClass.getDeclaredMethod("setPairingConfirmation", boolean.class);
        return (Boolean) setPairingConfirmationMethod.invoke(device, confirmed);
    }

    /**
     * 创建socket
     *
     * @param device
     * @return
     */
    public static BluetoothSocket createRfcommSocket(Class<? extends BluetoothDevice> btClass, BluetoothDevice device) throws Exception {
        Method createRfcommSocketMethod = btClass.getMethod("createRfcommSocket", new Class[]{int.class});
        return (BluetoothSocket) createRfcommSocketMethod.invoke(device, 1);
    }

    /**
     * @param clsShow
     */
    public static void printAllInform(Class clsShow) {
        try {
            // 取得所有方法
            Method[] hideMethod = clsShow.getMethods();
            int i = 0;
            for (; i < hideMethod.length; i++) {
                Log.e("method name", hideMethod[i].getName() + ";and the i is:" + i);
            }
            // 取得所有常量
            Field[] allFields = clsShow.getFields();
            for (i = 0; i < allFields.length; i++) {
                Log.e("Field name", allFields[i].getName());
            }
        } catch (SecurityException e) {
            // throw new RuntimeException(e.getMessage());
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            // throw new RuntimeException(e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}  
