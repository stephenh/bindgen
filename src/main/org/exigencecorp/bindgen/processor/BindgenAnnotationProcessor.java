package org.exigencecorp.bindgen.processor;

import java.io.IOException;
import java.io.Writer;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.FilerException;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;
import javax.tools.Diagnostic.Kind;

@SupportedAnnotationTypes( { "org.exigencecorp.bindgen.Bindable" })
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class BindgenAnnotationProcessor extends AbstractProcessor {

    private boolean hasWritten = false;

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (this.hasWritten) {
            return true;
        }

        try {
            JavaFileObject blah = this.processingEnv.getFiler().createSourceFile("org.exigencecorp.bindgen.example.Blah");
            Writer w = blah.openWriter();
            w.append("package org.exigencecorp.bindgen.example;\n");
            w.append("public class Blah {\n");
            w.append("public String blah = \"blah\";\n");
            w.append("}\n");
            w.flush();
            w.close();

            this.hasWritten = true;

            for (Object e : roundEnv.getRootElements()) {
                System.out.println("root=" + e.toString());
            }
            for (Object e : annotations) {
                System.out.println("annotations=" + e.toString());
            }
            for (Object e : roundEnv.getElementsAnnotatedWith(annotations.iterator().next())) {
                System.out.println("annotated=" + e.toString());
                if (e instanceof PackageElement) {
                    for (Element f : ((PackageElement) e).getEnclosedElements()) {
                        System.out.println("inpackage=" + f.toString());
                    }
                }
            }

            this.processingEnv.getMessager().printMessage(Kind.NOTE, "foo");
        } catch (FilerException fe) {
            // pass
            System.out.println("FilerException: " + fe.getMessage());
        } catch (IOException io) {
            throw new RuntimeException(io);
        }

        return true;
    }
}
