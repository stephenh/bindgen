package org.exigencecorp.bindgen.processor;

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
        for (Element element : roundEnv.getElementsAnnotatedWith(Bindable.class)) {
            if (element.getKind() == ElementKind.CLASS) {
                this.queue.enqueueForcefully((TypeElement) element);
            } else {
                this.processingEnv.getMessager().printMessage(Kind.WARNING, "Unhandled element " + element);
            }
        }
        this.queue.processQueue();
        this.updateKeywordClassIfLastRound(roundEnv);
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

}
