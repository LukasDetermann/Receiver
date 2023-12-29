package org.example;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.tools.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static javax.lang.model.util.ElementFilter.constructorsIn;
import static javax.lang.model.util.ElementFilter.methodsIn;
import static javax.tools.Diagnostic.Kind.MANDATORY_WARNING;
import static javax.tools.JavaFileObject.Kind.SOURCE;
import static org.example.Main.COMPILED_CLASSES;

class CompileTime
{
   private static final JavaFileObject MY_ANNOTATION = createJavaFileObject("MyAnnotation.java",
                                                                            """
                                                                                  @java.lang.annotation.Target(java.lang.annotation.ElementType.TYPE_USE)
                                                                                  @java.lang.annotation.Retention(value = java.lang.annotation.RetentionPolicy.RUNTIME)
                                                                                  @interface MyAnnotation {}""");
   private static final JavaFileObject METHOD_EXAMPLE = createJavaFileObject("MethodExample.java", """
         public class MethodExample {
            public void receiver(@MyAnnotation MethodExample MethodExample.this) {}
         }
         """);
   private static final JavaFileObject CONSTRUCTOR_EXAMPLE = createJavaFileObject("ConstructorExample.java", """
         public class ConstructorExample {
            public class Inner {
               public Inner(@MyAnnotation ConstructorExample ConstructorExample.this) {}
            }
         }
         """);

   static void compileTime()
   {
      compile(List.of(MY_ANNOTATION, METHOD_EXAMPLE, CONSTRUCTOR_EXAMPLE), new AbstractProcessor()
      {
         @Override
         public SourceVersion getSupportedSourceVersion()
         {
            return SourceVersion.latestSupported();
         }

         @Override
         public Set<String> getSupportedAnnotationTypes()
         {
            return Collections.singleton("*");
         }

         @Override
         public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv)
         {
            if (!roundEnv.processingOver())
            {
               return false;
            }

            processingEnv.getMessager().printMessage(MANDATORY_WARNING, "Annotation Processing MethodExample");

            processingEnv.getMessager().printMessage(MANDATORY_WARNING,
                                                     "parameter count: " +
                                                     methodsIn(processingEnv.getElementUtils()
                                                                            .getTypeElement("MethodExample")
                                                                            .getEnclosedElements())
                                                           .get(0)
                                                           .getParameters()
                                                           .size());


            processingEnv.getMessager().printMessage(MANDATORY_WARNING, "Annotation Processing ConstructorExample");

            processingEnv.getMessager().printMessage(MANDATORY_WARNING,
                                                     "parameter count: " +
                                                     constructorsIn(processingEnv.getElementUtils()
                                                                                 .getTypeElement("ConstructorExample.Inner")
                                                                                 .getEnclosedElements())
                                                           .get(0)
                                                           .getParameters()
                                                           .size());

            return false;
         }
      });
   }

   private static void compile(Iterable<JavaFileObject> javaFileObjects, Processor processor)
   {
      JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
      Objects.requireNonNull(compiler);
      RuntimeFileManager fileManager = new RuntimeFileManager(compiler.getStandardFileManager(null, null, null));
      JavaCompiler.CompilationTask compilerTask = compiler.getTask(null,
                                                                   fileManager,
                                                                   null,
                                                                   null,
                                                                   Collections.singletonList(Object.class.getName()),
                                                                   javaFileObjects);

      compilerTask.setProcessors(Collections.singletonList(processor));
      compilerTask.call();
   }

   private static JavaFileObject createJavaFileObject(String fileName, String sourceCode)
   {
      return new SimpleJavaFileObject(URI.create(fileName), SOURCE)
      {
         @Override
         public CharSequence getCharContent(boolean b)
         {
            return sourceCode;
         }
      };
   }

   private static class RuntimeFileManager extends ForwardingJavaFileManager<JavaFileManager>
   {
      RuntimeFileManager(JavaFileManager fileManager)
      {
         super(fileManager);
      }

      @Override
      public JavaFileObject getJavaFileForOutput(Location location, String className, JavaFileObject.Kind kind, FileObject sibling)
      {
         return new SimpleJavaFileObject(URI.create(className), kind)
         {
            @Override
            public ByteArrayOutputStream openOutputStream()
            {
               return new ByteArrayOutputStream()
               {
                  @Override
                  public void close() throws IOException
                  {
                     super.close();
                     COMPILED_CLASSES.put(className, toByteArray());
                  }
               };
            }
         };
      }
   }
}
