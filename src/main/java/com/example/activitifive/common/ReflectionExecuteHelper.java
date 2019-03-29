package com.example.activitifive.common;

import com.example.activitifive.utils.ReflectionExecutor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

public class ReflectionExecuteHelper {

    public void executeSomething(List<ReflectionExecutor> reflectionExecutors){

        for(int i=0,j=reflectionExecutors.size();i<j;i++){
            ReflectionExecutor test = reflectionExecutors.get(i);
            String classz = test.getClassName();
            System.out.println("截取的类名:"+classz);
            String method = test.getFunctionName();
            System.out.println("截取的方法名:"+method);
            try {
                Class<?> t_class =  Class.forName(classz);
                Method m = t_class.getDeclaredMethod(method, String.class);
                Object object = m.invoke(t_class.newInstance(),test.getFunctionValue());
                System.out.println(object);
            } catch (ClassNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (SecurityException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (InstantiationException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    }
}
