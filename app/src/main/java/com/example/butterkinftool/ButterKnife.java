package com.example.butterkinftool;

import android.app.Activity;
import android.view.View;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class ButterKnife {

    public static void bind(Activity context) {
        String className = context.getClass().getCanonicalName();
        try {
            Class<?> clz = context.getClassLoader().loadClass(className + "$BindView");
            Constructor constructor = clz.getConstructor(context.getClass());
            constructor.newInstance(context);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public static void bind(Object context, View sourceView) {
        String className = context.getClass().getCanonicalName();
        try {
            Class<?> clz = context.getClass().getClassLoader().loadClass(className + "$BindView");
            Constructor constructor = clz.getConstructor(context.getClass(), View.class);
            constructor.newInstance(context, sourceView);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

}
