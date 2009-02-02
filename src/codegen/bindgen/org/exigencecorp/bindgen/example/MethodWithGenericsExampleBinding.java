package bindgen.org.exigencecorp.bindgen.example;

import bindgen.java.util.ListBinding;
import java.util.List;
import org.exigencecorp.bindgen.Binding;
import org.exigencecorp.bindgen.example.MethodWithGenericsExample;

public class MethodWithGenericsExampleBinding implements Binding<MethodWithGenericsExample> {

    private MethodWithGenericsExample value;
    private ListBinding<String> list;

    public MethodWithGenericsExampleBinding() {
    }

    public MethodWithGenericsExampleBinding(MethodWithGenericsExample value) {
        this.set(value);
    }

    public void set(MethodWithGenericsExample value) {
        this.value = value;
    }

    public MethodWithGenericsExample get() {
        return this.value;
    }

    public String getName() {
        return "";
    }

    public Class<?> getType() {
        return MethodWithGenericsExample.class;
    }

    public ListBinding<String> list() {
        if (this.list == null) {
            this.list = new MyListBinding();
        }
        return this.list;
    }

    public class MyListBinding extends ListBinding<String> {
        public String getName() {
            return "list";
        }
        public List<String> get() {
            return MethodWithGenericsExampleBinding.this.get().getList();
        }
        public void set(List<String> list) {
            MethodWithGenericsExampleBinding.this.get().setList(list);
        }
    }

}
