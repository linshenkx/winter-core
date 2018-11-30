package com.github.linshenkx.beanFactory;


import com.github.linshenkx.annotation.Autowired;
import com.github.linshenkx.annotation.Component;
import com.github.linshenkx.annotation.Qualifier;
import com.github.linshenkx.util.StringUtil;
import org.apache.commons.lang.StringUtils;
import com.github.linshenkx.util.ClassUtil;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


/**
 * @version V1.0
 * @author: lin_shen
 * @date: 18-11-28
 * @Description: TODO
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class ClassPathAnnotationApplicationContext {

    /**
     * 扫包范围
     */
    private String packageName;

    /**
     * 类的缓存map，用于避免单例情况下的循环引用
     */
    private Map<String,Object> cacheBeanFactory=new ConcurrentHashMap<>();

    /**
     * 用于存储真正的类信息
     * 全称类名(type)+自定义类名（beanId）==> 真类信息
     */
    private Map<String,Map<String,Class>> beanDefinationFactory;

    /**
     * 由真类信息的全称类名确定唯一单例对象
     */
    private Map<String,Object> singletonbeanFactory=new ConcurrentHashMap<>();

    /**
     * 构造方法，传入基础扫描包地址
     * @param packageName
     */
    public ClassPathAnnotationApplicationContext(String packageName) {
        this.packageName = packageName;
        scanPackage(packageName);
    }


    /**
     * 获取指定 Bean 实例
     * @param type 类型
     * @param beanId beanId，即指定名
     * @param force 在该类型只存在一个 Bean 实例的时候是否按照 必须按照 beanId 匹配（如为false则可直接返回唯一实例）
     * @return
     */
    public Object getBean(Class type,String beanId,boolean force){

        System.out.println("getBean,type:"+type.getName()+",name:"+beanId);

        Map<String,Class> beanClassMap = beanDefinationFactory.get(type.getName());

        //如果没有此类型则直接报错
        if(beanClassMap.isEmpty()){
            throw new RuntimeException("没有找到类型为:"+type.getName()+"的bean");
        }

        if(force){
            //如果是强匹配则要求beanId必须存在
            if(beanClassMap.get(beanId)==null){
                throw new RuntimeException("没有找到类型为:"+type.getName()+" 指定名为："+beanId+"的bean");
            }
        }else {
            //如果不是强匹配则允许beanId不存在，但此时对应类型的bean只能有一个，将beanId修改为仅有的那一个的id
            if(beanClassMap.get(beanId)==null ){
                if(beanClassMap.size()!=1){
                    throw new RuntimeException("无法分辨多个同类不同名对象，类型"+type.getName());
                }else {
                    beanId=beanClassMap.keySet().iterator().next();
                }
            }
        }
        Class targetClass=beanDefinationFactory.get(type.getName()).get(beanId);

        Object targetBean=singletonbeanFactory.get(targetClass.getName());

        if(targetBean!=null){
            return targetBean;
        }

        //不存在则初始化并收入管理
        try {

            System.out.println("初始化type为:"+type.getName()+",name为："+beanId+"的类");

            targetBean = initBean(type,beanId);
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return targetBean;
    }

    /**
     * 使用反射机制获取该包下所有的类已经存在bean的注解类
     * @param packageName 扫描包路径
     */
    private void scanPackage(String packageName) {
        beanDefinationFactory=new HashMap<>();
        if (StringUtils.isEmpty(packageName)) {
            throw new RuntimeException("扫包地址不能为空!");
        }
        // 使用反射技术获取当前包下所有的类
        List<Class<?>> classesByPackageName = ClassUtil.getClasses(packageName);
        // 对存在注解的类进行记录
        for (Class classInfo : classesByPackageName) {
            Component component = (Component) classInfo.getDeclaredAnnotation(Component.class);
            if(component==null){
                continue;
            }
            System.out.println("|classInfo:"+StringUtil.toLowerCaseFirstOne(classInfo.getName()));

            //存入按类型存取的单例BeanFactory(接口和非object父类和自身类型)
            classInfo.getAnnotatedInterfaces();
            Class[] interfaces = classInfo.getInterfaces();
            List<Class> superClassList = ClassUtil.getSuperClassList(classInfo);
            superClassList.addAll(Arrays.asList(interfaces));
            superClassList.add(classInfo);

            System.out.println("superClassListSize:"+superClassList.size());

            for (Class aClass : superClassList) {
                Map<String, Class> beanDefinationMap = beanDefinationFactory.computeIfAbsent(StringUtil.toLowerCaseFirstOne(aClass.getName()), k -> new HashMap<>());

                System.out.println("Type:"+StringUtil.toLowerCaseFirstOne(aClass.getName()));

                if(StringUtils.isNotEmpty(component.value())){
                    //如果component有值则使用该值（对应本类classInfo的信息）
                    if (beanDefinationMap.get(getComponentName(classInfo))!=null){
                        throw new RuntimeException("出现无法通过name区分的重复类型:"+StringUtil.toLowerCaseFirstOne(aClass.getName())+" "+getComponentName(classInfo));
                    }
                    //存入按指定名存取的单例BeanFactory
                    beanDefinationMap.put(getComponentName(classInfo),classInfo);
                    System.out.println("putName:"+getComponentName(classInfo));
                }else {
                    //如果component没有值则使用当前类型名
                    if (beanDefinationMap.get(StringUtil.toLowerCaseFirstOne(aClass.getName()))!=null){
                        throw new RuntimeException("出现无法通过name区分的重复类型:"+StringUtil.toLowerCaseFirstOne(aClass.getName()));
                    }
                    beanDefinationMap.put(StringUtil.toLowerCaseFirstOne(aClass.getSimpleName()),classInfo);
                    System.out.println("putType:"+StringUtil.toLowerCaseFirstOne(aClass.getName()));
                }


            }
        }

        for (Map.Entry<String, Map<String, Class>> stringMapEntry : beanDefinationFactory.entrySet()) {
            System.out.println("Type:"+stringMapEntry.getKey());
            stringMapEntry.getValue().keySet().forEach(System.out::println);
        }
        System.out.println("------------");

    }


    /**
     * 完成类的初始化
     * @param type 类型
     * @param beanId 指定名
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    private Object initBean(Class type,String beanId)
            throws InstantiationException, IllegalAccessException {

        //如果不存在该类的真类信息，则抛出异常
        Class<?> clazz = beanDefinationFactory.get(type.getName()).get(beanId);
        if(clazz==null){
            throw new RuntimeException("没有找到type为:"+type.getName()+",name为："+beanId+"的类");
        }

        //进入创建流程
        try {
            //利用cacheBeanFactory识别循环引用
            Object targetObject = cacheBeanFactory.get(clazz.getName());
            if(targetObject!=null){
                //在创建bean的过程bean又被创建，说明存在循环引用，抛出异常
                throw new RuntimeException("循环引用");
            }else {
                targetObject=clazz.newInstance();
                cacheBeanFactory.put(clazz.getName(),targetObject);
            }

            //正式进入初始化，给@Autowired的field赋值
            Field[] declaredFields = clazz.getDeclaredFields();
            for (Field declaredField : declaredFields) {
                Autowired autowired = declaredField.getAnnotation(Autowired.class);
                if(autowired==null){
                    continue;
                }
                //判断是否有Qualifier注解
                Qualifier qualifier = declaredField.getAnnotation(Qualifier.class);
                declaredField.setAccessible(true);
                //对该field赋值
                if(qualifier==null){
                    //如果没有Qualifier注解则设置beanId为域对象的值，采用非强制匹配，在匹配类型仅有一个实现实例时忽略beanId要求
                    declaredField.set(targetObject,getBean(declaredField.getType(),declaredField.getName(),false));
                }else {
                    //如果有Qualifier注解则设置beanId为Qualifier注解的value，采用强制匹配，在匹配类型仅有一个实现实例时如果beanId不匹配仍会报错
                    declaredField.set(targetObject,getBean(declaredField.getType(),qualifier.value(),true));
                }
            }
            singletonbeanFactory.put(clazz.getName(),targetObject);
            return targetObject;
        }  finally {
            cacheBeanFactory.remove(clazz.getName());
        }

    }




    /**
     * 取出clazz上Component注解的非默认值，如非Component注解标识类或使用的是默认值则抛出运行时异常
     * @param clazz
     * @return
     */
    private static String getComponentName(Class clazz){
        Component component = (Component) clazz.getDeclaredAnnotation(Component.class);
        if(component==null){
            throw new RuntimeException("缺少Component注解");
        }
        if(StringUtils.isEmpty(component.value())){
            throw new RuntimeException("Component注解的值不能为空");
        }
        return component.value();
    }


}
