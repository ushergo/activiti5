package com.example.activitifive.utils;

import java.util.Map;

public class ReflectionExecutor {

    private String functionName;
    private Map<String,Object> functionValue;
    private String className;

    /**
     * 生成需要执行的类和对应的函数的反射类
     * @param className 类名称
     * @param functionName 函数名称
     * @param functionValue 函数的参数
     */
    public ReflectionExecutor(String className,String functionName,Map<String,Object> functionValue) {
        this.functionName = functionName;
        this.functionValue = functionValue;
        this.className = className;
    }

    public String getFunctionName() {
        return functionName;
    }

    public void setFunctionName(String functionName) {
        this.functionName = functionName;
    }

    public Map<String, Object> getFunctionValue() {
        return functionValue;
    }

    public void setFunctionValue(Map<String, Object> functionValue) {
        this.functionValue = functionValue;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }
}
