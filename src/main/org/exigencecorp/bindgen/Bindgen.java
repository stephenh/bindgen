package org.exigencecorp.bindgen;

import java.io.IOException;
import java.io.Writer;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.FilerException;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;
import javax.tools.Diagnostic.Kind;

@SupportedAnnotationTypes( { "org.exigencecorp.bindgen.Bindable" })
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class Bindgen extends AbstractProcessor {

    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        System.out.println("HERE");

        try {
            JavaFileObject blah = this.processingEnv.getFiler().createSourceFile("Blah");
            Writer w = blah.openWriter();
            w.append("public class Blah {}");
            w.flush();
            w.close();

            for (Object e : roundEnv.getRootElements()) {
                System.out.println("root=" + e.toString());
            }
            for (Object e : annotations) {
                System.out.println("annotations=" + e.toString());
            }

            this.processingEnv.getMessager().printMessage(Kind.NOTE, "foo");
        } catch (FilerException fe) {
            // pass
        } catch (IOException io) {
            throw new RuntimeException(io);
        }

        return false;
    }
}
