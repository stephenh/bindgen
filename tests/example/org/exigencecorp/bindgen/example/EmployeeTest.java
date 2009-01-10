package org.exigencecorp.bindgen.example;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.exigencecorp.bindgen.Binding;

public class EmployeeTest extends TestCase {

    public void testEmployee() {
        Employee e = new Employee();
        e.name = "bob";
        e.department = "accounting";
        EmployeeBinding eb = new EmployeeBinding(e);
        Assert.assertEquals("bob", textBox(eb.name()).toString());
        Assert.assertEquals("accounting", textBox(eb.department()).toString());

        Assert.assertEquals("name", textBox(eb.name()).getName());
        Assert.assertEquals("department", textBox(eb.department()).getName());
    }

    public void testEmployer() {
        Employer e = new Employer();
        e.name = "at&t";
        EmployerBinding eb = new EmployerBinding(e);
        Assert.assertEquals("at&t", textBox(eb.name()).toString());
        Assert.assertEquals("name", textBox(eb.name()).getName());
    }

    public void testEmployerThroughEmployee() {
        Employer er = new Employer();
        er.name = "at&t";

        Employee ee = new Employee();
        ee.name = "bob";
        ee.department = "accounting";
        ee.employer = er;

        EmployeeBinding eb = new EmployeeBinding(ee);
        Assert.assertEquals("bob", textBox(eb.name()).toString());
        Assert.assertEquals("accounting", textBox(eb.department()).toString());
        Assert.assertEquals("at&t", textBox(eb.employer().name()).toString());

        Assert.assertEquals("employer", textBox(eb.employer()).getName());

        textBox(eb.employer().name()).set("fromTheBrowser");
        textBox(eb.employer().name()).set("fromTheBrowser");
        Assert.assertEquals("fromTheBrowser", er.name);
    }

    public void testSetEmployer() {
        Employer er1 = new Employer();
        er1.name = "at&t";

        Employer er2 = new Employer();
        er2.name = "exigence";

        Employee ee = new Employee();
        ee.employer = er1;

        EmployeeBinding eb = new EmployeeBinding(ee);
        Assert.assertEquals("at&t", textBox(eb.employer().name()).toString());

        eb.employer().set(er2);
        Assert.assertEquals("exigence", textBox(eb.employer().name()).toString());
    }

    public void testDelayedEmployee() {
        Employee e1 = new Employee();
        e1.name = "bob";

        Employee e2 = new Employee();
        e2.name = "fred";

        EmployeeBinding eb = new EmployeeBinding();
        TextBox<String> tb = textBox(eb.name());

        eb.set(e1);
        Assert.assertEquals("bob", tb.toString());

        eb.set(e2);
        Assert.assertEquals("fred", tb.toString());
    }

    public static <T> TextBox<T> textBox(Binding<T> binding) {
        return new TextBox<T>(binding);
    }

    public static class TextBox<T> {
        Binding<T> binding;

        public TextBox(Binding<T> binding) {
            this.binding = binding;
        }

        public String getName() {
            return this.binding.getName();
        }

        public String toString() {
            return this.binding.get().toString();
        }

        public void set(String value) {
            T changed = (T) value;
            this.binding.set(changed);
        }
    }

}
