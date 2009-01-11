package bindgen.java.util;

import java.util.List;
import org.exigencecorp.bindgen.Binding;

public class ListBinding<E> implements Binding<List<E>> {

    private List<E> value;
    private Runnable clear;

    public ListBinding() {
    }

    public ListBinding(List<E> value) {
        this.set(value);
    }

    public void set(List<E> value) {
        this.value = value;
    }

    public List<E> get() {
        return this.value;
    }

    public String getName() {
        return "";
    }

    public Class<?> getType() {
        return List.class;
    }

    public Runnable clear() {
        if (this.clear == null) {
            this.clear = new MyClearBinding();
        }
        return this.clear;
    }

    public class MyClearBinding implements Runnable {
        public void run() {
            ListBinding.this.get().clear();
        }
    }

}
