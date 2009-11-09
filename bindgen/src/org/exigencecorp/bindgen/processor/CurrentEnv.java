package org.exigencecorp.bindgen.processor;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

/** Provides static helper methods to get at the current {@link ProcessingEnvironment}.
 *
 * The {@link ProcessingEnvironment} used to get passed around as a method parameter,
 * but a whole lot of places need it, so putting it in one static location cut down
 * on the parameter cruft.
 *
 * This also installs several default options, specifically fixRawTypes and
 * skipAttribute settings to remove any warnings from pre-1.5 classes in the
 * <code>javax.servlet</code> classes.
 */
public class CurrentEnv {

    private static final Map<String, String> options = new HashMap<String, String>();
    private static ProcessingEnvironment current;

    public static void set(ProcessingEnvironment env) {
        current = env;
        setDefaultOptions();
        setUserOptions();
    }

    public static Filer getFiler() {
        return current.getFiler();
    }

    public static Messager getMessager() {
        return current.getMessager();
    }

    public static Elements getElementUtils() {
        return current.getElementUtils();
    }

    public static Types getTypeUtils() {
        return current.getTypeUtils();
    }

    public static String getOption(String key) {
        return options.get(key);
    }

    // Default properties--this is ugly, but I could not get a bindgen.properties to be found on the classpath
    private static void setDefaultOptions() {
        options.put("fixRawType.javax.servlet.ServletConfig.initParameterNames", "String");
        options.put("fixRawType.javax.servlet.ServletContext.attributeNames", "String");
        options.put("fixRawType.javax.servlet.ServletContext.initParameterNames", "String");
        options.put("fixRawType.javax.servlet.ServletRequest.attributeNames", "String");
        options.put("fixRawType.javax.servlet.ServletRequest.parameterNames", "String");
        options.put("fixRawType.javax.servlet.ServletRequest.locales", "Locale");
        options.put("fixRawType.javax.servlet.ServletRequest.parameterMap", "String, String[]");
        options.put("fixRawType.javax.servlet.http.HttpServletRequest.headerNames", "String");
        options.put("fixRawType.javax.servlet.http.HttpSession.attributeNames", "String");
        options.put("skipAttribute.javax.servlet.http.HttpSession.sessionContext", "true");
        options.put("skipAttribute.javax.servlet.http.HttpServletRequest.requestedSessionIdFromUrl", "true");
        options.put("skipAttribute.javax.servlet.ServletContext.servletNames", "true");
        options.put("skipAttribute.javax.servlet.ServletContext.servlets", "true");
        options.put("skipAttribute.java.lang.Object.getClass", "true");
        options.put("skipAttribute.java.lang.Object.notify", "true");
        options.put("skipAttribute.java.lang.Object.notifyAll", "true");
    }

    private static void setUserOptions() {
        for (Map.Entry<String, String> entry : current.getOptions().entrySet()) {
            options.put(entry.getKey(), entry.getValue());
        }
    }

}
