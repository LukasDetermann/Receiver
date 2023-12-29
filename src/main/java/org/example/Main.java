package org.example;

import java.util.HashMap;
import java.util.Map;

import static org.example.CompileTime.compileTime;
import static org.example.RunTime.runtime;

public class Main
{
   static final Map<String, byte[]> COMPILED_CLASSES = new HashMap<>();

   public static void main(String[] args)
   {
      compileTime();

      runtime();
   }
}