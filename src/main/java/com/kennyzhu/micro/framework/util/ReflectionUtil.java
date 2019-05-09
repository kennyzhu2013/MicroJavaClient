package com.kennyzhu.micro.framework.util;

/**
 *  
 *  * All rights Reserved, Designed By kennyzhu 13802885090@139.com
 *  * @projectName micro.helo
 *  * @title     ReflectionUtil   
 *  * @package    com.kennyzhu.micro.framework.util  
 *  * @description    ${TODO}  
 *  * @author kennyzhu     
 *  * @date   2019/5/8 11:42  
 *  * @version V1.0.1
 *  * @copyright 2019 www.chinamobile.com
 *  * 注意 本内容仅限于 中移互联网有限公司，禁止外泄以及用于其他的商业 
 *  
 */
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class ReflectionUtil {
    //hacky and dirty, augment if you find the need...
    public static Class<?> findSubClassParameterType(Object instance, int parameterIndex)
            throws ClassNotFoundException {

        if (instance instanceof MockMethodHandler) {
            if (parameterIndex == 0) {
                return ((MockMethodHandler) instance).getRequestType();
            } else {
                return ((MockMethodHandler) instance).getResponseType();
            }
        }

        Class<?> clazz = instance.getClass();
        Type[] genericInterfaces = clazz.getGenericInterfaces();

        if (genericInterfaces.length > 0) {
            return retrieveGenericParameterTypes(genericInterfaces, parameterIndex);
        } else if (clazz.getSuperclass() != null) {
            return retrieveGenericParameterTypes(
                    clazz.getSuperclass().getGenericInterfaces(),
                    parameterIndex
            );
        } else {
            return null;
        }
    }

    private static Class<?> retrieveGenericParameterTypes(
            final Type[] genericInterfaces,
            final Integer parameterIndex
    ) throws ClassNotFoundException {
        int count = 0;
        for (Type genericInterface : genericInterfaces) {
            if (genericInterface instanceof ParameterizedType) {
                Type[] genericTypes = ((ParameterizedType) genericInterface).getActualTypeArguments();
                for (Type genericType : genericTypes) {
                    if (parameterIndex == count) {
                        return Class.forName(genericType.getTypeName());
                    } else {
                        count++;
                    }
                }
            }
        }

        return null;
    }
}
