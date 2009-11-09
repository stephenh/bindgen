package org.exigencecorp.bindgen.processor;

import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import javax.tools.Diagnostic.Kind;

import org.exigencecorp.bindgen.Bindable;

@SupportedAnnotationTypes( { "org.exigencecorp.bindgen.Bindable" })
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class BindgenAnnotationProcessor extends AbstractProcessor {

    private GenerationQueue queue;
    private boolean hasUpdatedKeywordClass = false;

    @Override
    public void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        CurrentEnv.set(this.processingEnv);
        this.queue = new GenerationQueue();
        this.hasUpdatedKeywordClass = false;
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        try {
            for (Element element : roundEnv.getElementsAnnotatedWith(Bindable.class)) {
                if (element.getKind() == ElementKind.CLASS || element.getKind() == ElementKind.INTERFACE) {
                    this.queue.enqueueForcefully((TypeElement) element);
                } else {
                    this.processingEnv.getMessager().printMessage(Kind.WARNING, "Unhandled element " + element);
                }
            }
            this.queue.processQueue();
            this.updateKeywordClassIfLastRound(roundEnv);
        } catch (Exception e) {
            this.logExceptionToTextFile(e);
        }
        return true;
    }

    /**
     * Updating the Keyword class on the official processingOver() round did not
     * work in Eclipse, but we seem to get an "empty" round before the official
     * "over" round, so detect that and update the keyword class there.
     */
    private void updateKeywordClassIfLastRound(RoundEnvironment roundEnv) {
        boolean emptyRound = roundEnv.getElementsAnnotatedWith(Bindable.class).size() == 0;
        if (emptyRound && !this.hasUpdatedKeywordClass) {
            this.queue.updateBindKeywordClass();
            this.hasUpdatedKeywordClass = true;
        }
    }

    /** Logs <code>e</code> to <code>SOURCE_OUTPUT/bindgen-errors.txt</code> */
    private void logExceptionToTextFile(Exception e) {
        try {
            FileObject fo = this.processingEnv.getFiler().createResource(StandardLocation.SOURCE_OUTPUT, "", "bindgen-exception.txt");
            OutputStream out = fo.openOutputStream();
            e.printStackTrace(new PrintStream(out));
            // Specifically for Eclipse's AbortCompilation exception which has a useless printStackTrace output
            if (e.getClass().getField("problem") != null) {
                Field f = e.getClass().getField("problem");
                Object problem = f.get(e);
                out.write(problem.toString().getBytes());
            }
            out.close();
        } catch (Exception e2) {
            this.processingEnv.getMessager().printMessage(Kind.ERROR, "Error writing out error message " + e2.getMessage());
        }
    }

}
