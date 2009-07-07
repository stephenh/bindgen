package org.exigencecorp.bindgen.processor.generators;

import static org.exigencecorp.bindgen.processor.CurrentEnv.getElementUtils;
import static org.exigencecorp.bindgen.processor.CurrentEnv.getFiler;
import static org.exigencecorp.bindgen.processor.CurrentEnv.getMessager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Writer;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import javax.tools.Diagnostic.Kind;

import org.exigencecorp.bindgen.processor.GenerationQueue;
import org.exigencecorp.bindgen.processor.util.BoundClass;
import org.exigencecorp.bindgen.processor.util.ClassName;

import joist.sourcegen.GClass;
import joist.sourcegen.GMethod;
import joist.util.Copy;
import joist.util.Join;

public class BindKeywordGenerator {

    private final GenerationQueue queue;
    private final GClass bindClass = new GClass("bindgen.BindKeyword");
    private final Set<String> classNames = new HashSet<String>();

    public BindKeywordGenerator(GenerationQueue queue) {
        this.queue = queue;
    }

    public void generate(Set<String> newlyWritten) {
        this.readExistingBindKeywordFile();
        this.classNames.addAll(newlyWritten);
        this.addBindMethods();
        this.writeBindKeywordFile();
        this.writeBindKeywordClass();
    }

    private void addBindMethods() {
        for (String className : this.classNames) {
            TypeElement e = getElementUtils().getTypeElement(className);
            this.addBindMethod(className, (DeclaredType) e.asType());
        }
    }

    private void addBindMethod(String className, DeclaredType type) {
        ClassName bindingType = new BoundClass(type).getBindingClassName();
        if (type.getTypeArguments().size() > 0) {
            GMethod method = this.bindClass.getMethod("bind({}<{}> o)", className, Join.commaSpace(bindingType.getGenericPartWithoutBrackets()));
            method.returnType("{}", bindingType);
            method.typeParameters(Join.commaSpace(bindingType.getGenericsWithBounds()));
            method.setStatic();
            method.body.line("return new {}(o);", bindingType);
        } else {
            GMethod method = this.bindClass.getMethod("bind({} o)", className);
            method.returnType(bindingType.toString());
            method.setStatic();
            method.body.line("return new {}(o);", bindingType);
        }
    }

    private void readExistingBindKeywordFile() {
        try {
            this.queue.log("READING BindKeyword.txt");
            FileObject fo = getFiler().getResource(StandardLocation.SOURCE_OUTPUT, "bindgen", "BindKeyword.txt");
            if (fo.getLastModified() > 0) {
                String line;
                BufferedReader input = new BufferedReader(new InputStreamReader(fo.openInputStream()));
                while ((line = input.readLine()) != null) {
                    this.classNames.add(line);
                }
                input.close();
                this.queue.log("WAS THERE");
            } else {
                this.queue.log("NOT THERE");
            }
        } catch (IOException io) {
            getMessager().printMessage(Kind.ERROR, io.getMessage());
        }
    }

    private void writeBindKeywordFile() {
        try {
            this.queue.log("WRITING BindKeyword.txt");
            List<String> sorted = Copy.list(this.classNames);
            Collections.sort(sorted);
            FileObject fo = getFiler().createResource(StandardLocation.SOURCE_OUTPUT, "bindgen", "BindKeyword.txt");
            OutputStream output = fo.openOutputStream();
            for (String className : sorted) {
                output.write(className.getBytes());
                output.write("\n".getBytes());
            }
            output.close();
        } catch (IOException io) {
            this.queue.log("ERROR: " + io.getMessage());
            getMessager().printMessage(Kind.ERROR, io.getMessage());
        }
    }

    private void writeBindKeywordClass() {
        try {
            this.queue.log("WRITING BindKeyword.java");
            JavaFileObject jfo = getFiler().createSourceFile(this.bindClass.getFullClassNameWithoutGeneric());
            Writer w = jfo.openWriter();
            w.write(this.bindClass.toCode());
            w.close();
        } catch (IOException io) {
            this.queue.log("ERROR: " + io.getMessage());
            getMessager().printMessage(Kind.ERROR, io.getMessage());
        }
    }

}
