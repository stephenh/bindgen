package org.exigencecorp.bindgen.processor;

import static org.exigencecorp.bindgen.processor.CurrentEnv.getFiler;
import static org.exigencecorp.bindgen.processor.CurrentEnv.getMessager;
import static org.exigencecorp.bindgen.processor.CurrentEnv.getOption;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.TypeElement;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import javax.tools.Diagnostic.Kind;

public class GenerationQueue {

    // From what I can tell, both Eclipse and javac will use the same processor across all of the rounds, so this should be cumulative
    private final Set<String> written = new HashSet<String>();
    private final List<TypeElement> queue = new ArrayList<TypeElement>();
    private final boolean log;
    private final boolean skipExistingBindingCheck;
    private final boolean skipBindKeyword;

    public GenerationQueue() {
        this.log = this.isEnabled("bindgen.log");
        this.skipExistingBindingCheck = this.isEnabled("bindgen.skipExistingBindingCheck");
        this.skipBindKeyword = this.isEnabled("bindgen.skipBindKeyword");
    }

    public void enqueueForcefully(TypeElement element) {
        // Even when done forcefully, we can only touch elements once/round
        if (this.hasAlreadyBeenWrittenByThisRound(element)) {
            return;
        }
        this.enqueue(element);
    }

    public void enqueueIfNew(TypeElement element) {
        if (this.hasAlreadyBeenWrittenByThisRound(element) || this.hasAlreadyBeenWrittenByAPreviousRound(element)) {
            return;
        }
        this.enqueue(element);
    }

    public void processQueue() {
        while (this.queue.size() != 0) {
            new ClassGenerator(this, this.queue.remove(0)).generate();
        }
    }

    public void updateBindKeywordClass() {
        if (this.skipBindKeyword) {
            return;
        }
        new BindKeywordGenerator(this).generate(this.written);
    }

    public void log(String message) {
        if (this.log) {
            System.out.println(message + " in " + this);
        }
    }

    private void enqueue(TypeElement element) {
        this.queue.add(element);
        this.written.add(element.toString());
    }

    // We recursively walk into bindings, so first check our in-memory set of what we've
    // seen so far this round. Eclipse resets our processor every time, so this only has
    // things from this round. -- By "round" here, I think I mean compile-cycle, so
    // technically multiple, successive rounds.
    private boolean hasAlreadyBeenWrittenByThisRound(TypeElement element) {
        return this.written.contains(element.toString());
    }

    // If we haven't seen it this round, see if we've already output awhile ago, in which
    // case we can skip it. If we really needed to do this one again, Eclipse would have
    // deleted our last output and this check wouldn't find anything. But this does not
    // work for javac, so we don't do it if told not to. Would be nice to auto-detect javac.
    private boolean hasAlreadyBeenWrittenByAPreviousRound(TypeElement element) {
        if (this.skipExistingBindingCheck) {
            return false;
        }
        try {
            ClassName bindingClassName = new Property(element.asType()).getBindingType();
            FileObject fo = getFiler().getResource(
                StandardLocation.SOURCE_OUTPUT,
                bindingClassName.getPackageName(),
                bindingClassName.getSimpleName() + ".java");
            return fo.getLastModified() > 0; // exists already
        } catch (IOException io) {
            getMessager().printMessage(Kind.ERROR, io.getMessage());
            return false;
        }
    }

    private boolean isEnabled(String property) {
        return "true".equals(getOption(property));
    }

}
