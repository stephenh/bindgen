package bindgen.java.lang;

import org.exigencecorp.bindgen.Binding;

public class StringBinding implements Binding<String> {

    private String value;

    public StringBinding() {
    }

    public StringBinding(String value) {
        this.set(value);
    }

    public void set(String value) {
        this.value = value;
    }

    public String get() {
        return this.value;
    }

    public String getName() {
        return "";
    }

    public Class<?> getType() {
        return String.class;
    }

}
