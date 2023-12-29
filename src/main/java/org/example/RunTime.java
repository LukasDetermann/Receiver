package org.example;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;

import static org.example.Main.COMPILED_CLASSES;

public class RunTime
{
   static void runtime()
   {
      defineClasses();

      try
      {
         System.out.println("Reflection MethodExample");

         Method method = Arrays.stream(Thread.currentThread().getContextClassLoader().loadClass("MethodExample").getMethods())
                               .filter(method1 -> method1.getName().equals("receiver"))
                               .findAny()
                               .orElseThrow();
         System.out.println("parameter count: " + method.getParameterCount());


         System.out.println("Reflection ConstructorExample");

         Constructor<?> constructor = Thread.currentThread().getContextClassLoader().loadClass("ConstructorExample$Inner").getConstructors()[0];
         System.out.println("parameter count: " + constructor.getParameterCount());
         System.out.println("parameters are: " + Arrays.toString(constructor.getParameters()));
      }
      catch (ClassNotFoundException e)
      {
         throw new RuntimeException(e);
      }
   }

   private static void defineClasses()
   {
      Thread.currentThread().setContextClassLoader(new ClassLoader()
      {
         @Override
         protected Class<?> findClass(String name) throws ClassNotFoundException
         {
            byte[] bytes = COMPILED_CLASSES.get(name);
            if (bytes != null)
            {
               return defineClass(name, bytes, 0, bytes.length);
            }
            return super.findClass(name);
         }
      });
   }
}
