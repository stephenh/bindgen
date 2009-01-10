package org.exigencecorp.bindgen.example.subpackage;

import bindgen.java.lang.StringBinding;
import org.exigencecorp.bindgen.Binding;

public class PackageExampleBinding implements Binding<PackageExample> {

    private PackageExample value;
    private StringBinding name;

    public PackageExampleBinding() {
    }

    public PackageExampleBinding(PackageExample value) {
        this.set(value);
    }

    public void set(PackageExample value) {
        this.value = value;
    }

    public PackageExample get() {
        return this.value;
    }

    public String getName() {
        return "";
    }

    public Class<PackageExample> getType() {
        return PackageExample.class;
    }

    public StringBinding name() {
        if (this.name == null) {
            this.name = new MyNameBinding();
        }
        return this.name;
    }

    public class MyNameBinding extends StringBinding {
        public String getName() {
            return "name";
        }
        public String get() {
            return PackageExampleBinding.this.get().name;
        }
        public void set(String name) {
            PackageExampleBinding.this.get().name = name;
        }
    }

}
