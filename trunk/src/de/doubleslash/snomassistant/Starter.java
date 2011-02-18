package de.doubleslash.snomassistant;
import com.jdotsoft.jarloader.JarClassLoader;

public class Starter {

    public static void main(String[] args) {
        JarClassLoader jcl = new JarClassLoader();
        try {
            jcl.invokeMain("de.doubleslash.snomassistant.SnomAssistant", args);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    } // main()
    
} // class MyAppLauncher
