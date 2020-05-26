package com.example.butterkinftool;

import android.app.Activity;
import android.view.View;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ViewInjust {

    public static void bind(Activity activity) {
        bindView(activity);
        bindClick(activity);
    }

    private static void bindView(Activity activity) {
        Class aClass = activity.getClass();
        Field[] declaredFields = aClass.getDeclaredFields();
        for (Field declaredField : declaredFields) {
            if (declaredField.isAnnotationPresent(ViewBind.class)) {
                ViewBind annotation = declaredField.getAnnotation(ViewBind.class);
                int viewId = annotation.value();
                View view = activity.findViewById(viewId);
                declaredField.setAccessible(true);
                try {
                    declaredField.set(activity, view);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static void bindClick(final Activity activity) {
        Class aClass = activity.getClass();
        Method[] declaredMethods = aClass.getDeclaredMethods();
        for (final Method declaredMethod : declaredMethods) {
            if (declaredMethod.isAnnotationPresent(OnClick.class)) {
                OnClick annotation = declaredMethod.getAnnotation(OnClick.class);
                int viewId = annotation.value();
                final View view = activity.findViewById(viewId);
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        declaredMethod.setAccessible(true);
                        try {
                            declaredMethod.invoke(activity, view);
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        } catch (InvocationTargetException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }
    }
}
