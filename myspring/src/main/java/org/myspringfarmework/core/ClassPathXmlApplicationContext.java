package org.myspringfarmework.core;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClassPathXmlApplicationContext implements ApplicationContext{

    private static final Logger logger = LoggerFactory.getLogger(ClassPathXmlApplicationContext.class);
    private Map<String,Object> singletonObjects = new HashMap<>();

    /**
     * 解析myspring所有的配置文件，然后初始化所有的bean
     * ConfigLocation spring配置文件的路径，注意：使用ClassPathXmlApplicationContext，配置文件应当放到类路径下
     * @param configLocation
     * @return
     */

    public ClassPathXmlApplicationContext (String configLocation){
        try {
            //解析myspring.xml文件，然后实例化bean，将bean存放到singletonObjects集合中

        //dom4j解析XMl核心配置文件
        SAXReader saxReader = new SAXReader();
        //获取一个输入流，指向配置文件
        InputStream is = ClassLoader.getSystemClassLoader().getResourceAsStream(configLocation);
        //读文件

            Document document = saxReader.read(is);

            //获取所有的bean标签
            List<Node> nodes = document.selectNodes("//bean");
            //遍历bean标签
            nodes.forEach(node -> {
                try {
                    //向下转型是为了使用Element接口里更加丰富的方法
                Element beanElt = (Element) node;
                //获取id属性
                String id = beanElt.attributeValue("id");
                //获取class属性
                String classname = beanElt.attributeValue("class");
                logger.info("beanname:"+id);
                logger.info("beanClassName:"+classname);
                //通过反射机制创建对象，放到map集合中，提前曝光
                //获取class

                    Class<?> clazz = Class.forName(classname);
                    //获取无参构造方法
                    Constructor<?> defaultCon = clazz.getDeclaredConstructor();
                    //调用无参构造方法实例化bean
                    Object bean = defaultCon.newInstance();
                    //将bean曝光，加入map集合
                    singletonObjects.put(id,bean);

                    logger.info(singletonObjects.toString());
//                    System.out.println(singletonObjects.toString());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

            });

            nodes.forEach(node -> {
                try {//向下转型是为了使用Element接口里更加丰富的方法
                Element beanElt = (Element) node;
                //获取id属性
                String id = beanElt.attributeValue("id");
                //获取class属性
                String classname = beanElt.attributeValue("class");
                //获取class属性


                    Class<?> aClass = Class.forName(classname);
                    //获取该bean标签下的属性property标签
                    List<Element> propertys = beanElt.elements("property");
                    //遍历所有的属性标签
                    propertys.forEach( property->{

                        try {
                            //获取属性名
                            String propertyname = property.attributeValue("name");
                            //获取属性类型
                            Field field = aClass.getDeclaredField(propertyname);
                            logger.info("属性名："+propertyname);
                            //获取set方法名
                            String setMothodName = "set" + propertyname.toUpperCase().charAt(0) + propertyname.substring(1);
                            //获取set方法
                            Method setMethod = aClass.getDeclaredMethod(setMothodName,field.getType());
                            logger.info(setMothodName);
                            logger.info(setMethod + "");

                            //获取具体的值
                            String value = property.attributeValue("value");
                            Object propertyVal = null;
                            String ref = property.attributeValue("ref");
                            if (value != null){
                                // 该属性是简单属性
                                String propertyTypeSimpleName = field.getType().getSimpleName();
                                switch (propertyTypeSimpleName) {
                                    case "byte": case "Byte":
                                        propertyVal = Byte.valueOf(value);
                                        break;
                                    case "short": case "Short":
                                        propertyVal = Short.valueOf(value);
                                        break;
                                    case "int": case "Integer":
                                        propertyVal = Integer.valueOf(value);
                                        break;
                                    case "long": case "Long":
                                        propertyVal = Long.valueOf(value);
                                        break;
                                    case "float": case "Float":
                                        propertyVal = Float.valueOf(value);
                                        break;
                                    case "double": case "Double":
                                        propertyVal = Double.valueOf(value);
                                        break;
                                    case "boolean": case "Boolean":
                                        propertyVal = Boolean.valueOf(value);
                                        break;
                                    case "char": case "Character":
                                        propertyVal = value.charAt(0);
                                        break;
                                    case "String":
                                        propertyVal = value;
                                        break;
                                }

                                setMethod.invoke(singletonObjects.get(id),propertyVal);
                            }
                            if (ref != null){
                                setMethod.invoke(singletonObjects.get(id),singletonObjects.get(ref));
                            }
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }


                    } );
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }


            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }


    @Override
    public Object getBean(String beanName) {
        return singletonObjects.get(beanName);
    }
}
