package bindgen.org.exigencecorp.bindgen.example;

import bindgen.java.lang.StringBinding;
import org.exigencecorp.bindgen.Binding;
import org.exigencecorp.bindgen.example.FieldExample;

public class FieldExampleBinding implements Binding<FieldExample> {

    private FieldExample value;
    private StringBinding name;

    public FieldExampleBinding() {
    }

    public FieldExampleBinding(FieldExample value) {
        this.set(value);
    }

    public void set(FieldExample value) {
        this.value = value;
    }

    public FieldExample get() {
        return this.value;
    }

    public String getName() {
        return "";
    }

    public Class<?> getType() {
        return FieldExample.class;
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
            return FieldExampleBinding.this.get().name;
        }
        public void set(String name) {
            FieldExampleBinding.this.get().name = name;
        }
    }

}
