package com.github.linshenkx.beanFactory;


import com.github.linshenkx.annotation.Autowired;
import com.github.linshenkx.annotation.Component;
import com.github.linshenkx.annotation.Qualifier;
import org.apache.commons.lang.StringUtils;
import com.github.linshenkx.util.ClassUtil;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * @version V1.0
 * @author: lin_shen
 * @date: 18-11-28
 * @Description: TODO
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class ClassPathAnnotationApplicationContext {


    private static Logger log=Logger.getLogger(ClassPathAnnotationApplicationContext.class.getName());

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
        scanPackage();
    }


    /**
     * 根据beanId查找对象
     * @param beanId
     * @return
     * @throws Exception
     */
    public Object getBean(Class type,String beanId,boolean force){

        System.out.println("getBean,type:"+toLowerCaseFirstOne(type.getName())+",name:"+beanId);

        Map<String,Class> beanClassMap = beanDefinationFactory.get(toLowerCaseFirstOne(type.getName()));

        //如果没有此类型则直接报错
        if(beanClassMap.isEmpty()){
            throw new RuntimeException("没有找到类型为:"+toLowerCaseFirstOne(type.getName())+"的bean");
        }

        if(force){
            //如果是强匹配则要求beanId必须存在
            if(beanClassMap.get(beanId)==null){
                throw new RuntimeException("没有找到类型为:"+toLowerCaseFirstOne(type.getName())+" 指定名为："+beanId+"的bean");
            }
        }else {
            //如果不是强匹配则允许beanId不存在，但此时对应类型的bean只能有一个，将beanId修改为仅有的那一个的id
            if(beanClassMap.get(beanId)==null ){
                if(beanClassMap.size()!=1){
                    throw new RuntimeException("无法分辨多个同类不同名对象，类型"+toLowerCaseFirstOne(type.getName()));
                }else {
                    beanId=beanClassMap.keySet().iterator().next();
                }
            }
        }
        Class targetClass=beanDefinationFactory.get(toLowerCaseFirstOne(type.getName())).get(beanId);

        Object targetBean=singletonbeanFactory.get(targetClass.getName());

        if(targetBean!=null){
            return targetBean;
        }

        //不存在则初始化并收入管理
        try {

            System.out.println("初始化type为:"+toLowerCaseFirstOne(type.getName())+",name为："+beanId+"的类");

            targetBean = initBean(type,beanId);
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return targetBean;
    }

    /**
     * 使用反射机制获取该包下所有的类已经存在bean的注解类
     */
    private void scanPackage() {
        beanDefinationFactory=new HashMap<>();
        // 1.使用反射机制获取该包下所有的类
        if (StringUtils.isEmpty(packageName)) {
            throw new RuntimeException("扫包地址不能为空!");
        }
        // 2.使用反射技术获取当前包下所有的类
        List<Class<?>> classesByPackageName = ClassUtil.getClasses(packageName);
        // 3.对存在注解的类进行记录
        for (Class classInfo : classesByPackageName) {
            Component component = (Component) classInfo.getDeclaredAnnotation(Component.class);
            if (component != null) {
                System.out.println("|classInfo:"+toLowerCaseFirstOne(classInfo.getName()));

                //存入按类型存取的单例BeanFactory(接口和非object父类和自身类型)
                classInfo.getAnnotatedInterfaces();
                Class[] interfaces = classInfo.getInterfaces();
                List<Class> superClassList = getSuperClassList(classInfo);
                superClassList.addAll(Arrays.asList(interfaces));
                superClassList.add(classInfo);

                System.out.println("superClassListSize:"+superClassList.size());

                for (Class aClass : superClassList) {
                    Map<String, Class> beanDefinationMap=beanDefinationFactory.get(toLowerCaseFirstOne(aClass.getName()));
                    if(beanDefinationMap==null){
                        beanDefinationMap=new HashMap<>();
                    }

                    System.out.println("Type:"+toLowerCaseFirstOne(aClass.getName()));

                    if(StringUtils.isNotEmpty(component.value())){
                        //如果component有值则使用该值（对应本类classInfo的信息）
                        if (beanDefinationMap.get(getComponentName(classInfo))!=null){
                            throw new RuntimeException("出现无法通过name区分的重复类型:"+toLowerCaseFirstOne(aClass.getName())+" "+getComponentName(classInfo));
                        }
                        //存入按指定名存取的单例BeanFactory
                        beanDefinationMap.put(getComponentName(classInfo),classInfo);
                        System.out.println("putName:"+getComponentName(classInfo));
                    }else {
                        //如果component没有值则使用当前类型名
                        if (beanDefinationMap.get(toLowerCaseFirstOne(aClass.getName()))!=null){
                            throw new RuntimeException("出现无法通过name区分的重复类型:"+toLowerCaseFirstOne(aClass.getName()));
                        }
                        beanDefinationMap.put(toLowerCaseFirstOne(aClass.getSimpleName()),classInfo);
                        System.out.println("putType:"+toLowerCaseFirstOne(aClass.getName()));
                    }

                    beanDefinationFactory.put(toLowerCaseFirstOne(aClass.getName()),beanDefinationMap);

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
     * @param beanId
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    public Object initBean(Class type,String beanId)
            throws InstantiationException, IllegalAccessException {

        //如果不存在该类的真类信息，则抛出异常
        Class<?> clazz = beanDefinationFactory.get(toLowerCaseFirstOne(type.getName())).get(beanId);
        if(clazz==null){
            throw new RuntimeException("没有找到type为:"+toLowerCaseFirstOne(type.getName())+",name为："+beanId+"的类");
        }
        //如果存在真类对应的实例，则直接返回
        Object targetObject=singletonbeanFactory.get(clazz.getName());
        if(targetObject!=null){
            return targetObject;
        }

        //进入创建流程

        //创建cacheBean用于避免循环引用
        Object cacheBean = cacheBeanFactory.get(clazz.getName());
        if(cacheBean!=null){
            //在创建bean的过程bean又被创建，说明存在循环引用，抛出异常
            throw new RuntimeException("循环引用");
        }

        try {
            //创建目标bean，并放入缓存map，防止循环引用
            targetObject=clazz.newInstance();
            cacheBeanFactory.put(clazz.getName(),targetObject);

            //正式进入初始化，给@Autowired的field赋值
            Field[] declaredFields = clazz.getDeclaredFields();
            for (Field declaredField : declaredFields) {
                Autowired autowired = declaredField.getAnnotation(Autowired.class);
                if(autowired!=null){
                    //判断是否有Qualifier注解
                    Qualifier qualifier = declaredField.getAnnotation(Qualifier.class);
                    declaredField.setAccessible(true);
                    //对该field赋值
                    if(qualifier==null){
                        //如果没有Qualifier注解则对beanId无强制要求，取默认beanId为域对象的值，在匹配类型仅有一个实现实例时忽略beanId要求
                        declaredField.set(targetObject,getBean(declaredField.getType(),declaredField.getName(),false));
                    }else {
                        //如果有Qualifier注解则强制要求beanId为Qualifier注解value，在匹配类型仅有一个实现实例时如果beanId不匹配仍会报错
                        declaredField.set(targetObject,getBean(declaredField.getType(),qualifier.value(),true));
                    }
                }
            }
            singletonbeanFactory.put(clazz.getName(),targetObject);
            return targetObject;
        }  finally {
            cacheBeanFactory.remove(clazz.getName());
        }

    }

    // 首字母转小写
    public static String toLowerCaseFirstOne(String s) {
        if (Character.isLowerCase(s.charAt(0))) {
            return s;
        } else {
            return Character.toLowerCase(s.charAt(0)) + s.substring(1);
        }
    }

    /**
     * 取出clazz上Component注解的非默认值
     * @param clazz
     * @return
     */
    public static String getComponentName(Class clazz){
        Component component = (Component) clazz.getDeclaredAnnotation(Component.class);
        if(component==null){
            throw new RuntimeException("缺少Component注解");
        }
        if(StringUtils.isEmpty(component.value())){
            throw new RuntimeException("Component注解的值不能为空");
        }
        return component.value();
    }


    public List<Class> getSuperClassList(Class clazz){
        List<Class> superClassList=new ArrayList();
        for(Class superClass = clazz.getSuperclass(); ((superClass!=null)&&(!"Object".equals(superClass.getSimpleName()))); superClass=superClass.getSuperclass()){
            superClassList.add(superClass);
        }
        return superClassList;
    }


}