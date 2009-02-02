
Intro
=====

A data binding framework that generates type-safe binding classes.

Or, OGNL with no strings. A test case:

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

The point being that `eb.employer().name()` does not directly access the `name`, but instead returns a `StringBinding` that the web framework can bind values into/out of as it serves the request.

Annotations
===========

Bindgen is implemented as JDK6 annotation processor--when configured in your IDE (e.g. with project-specific settings in Eclipse), and as soon as you add a `@Bindable` annotation to a class `Foo`, and hit save, the IDE immediately invokes the Bindgen [processor][2] behind the scenes and `FooBinding` is created.

[2]: master/src/main/org/exigencecorp/bindgen/processor/BindgenAnnotationProcessor.java

Another Example
===============

This is a spike from a [Click][1]-like web framework I'm hacking around on:

    @Bindable
    public class HomePage extends AbstractPage {

        public Form form = new Form("Login");
        public String username = "blah";
        public String password;
        private HomePageBinding bind = new HomePageBinding(this);

        @Override
        public void onInit() {
            this.form.add(new TextField(this.bind.username()));
            this.form.add(new TextField(this.bind.password()));
            this.form.add(new SubmitField(this.bind.submit()));
        }

        public void submit() {
            // do stuff with this.username and this.password
        }
    }

The `HomePageBinding` class is auto-generated because of the `@Bindable` annotation on the `HomePage` class.

When the form POSTs, the TextFields call the `Binding.set` methods with their form values, which populates the `this.username` and `this.password` fields.

Fun things like type conversion using `Binding.getType()` method to go from strings -> whatever would be possible too.

[1]: http://click.sf.net

Todo
====

* Currently only `void methodName()` methods are recognized and wrapped as `Runnables`--it would be nice to give parameters to the `@Bindable` annotation for other method patterns to look for, e.g.:

A block:

    public interface TransactionBlock {
        boolean go(Transaction txn);
    }

With a usage of:

    @Bindable(recognize = { TransactionBlock.class })
    public class Foo {
        public boolean someMethod(Transaction txn) {
            ...
        }
    }

Then doing:

    new FooBinding(foo).someMethod()

Would return a `TransactionBlock` instance bound to `foo` that you could pass around and call `go(txn)` against later.

* No real tests--currently I just make changes and see if the `tests/example` use cases still work. Some (ugh) mock meta models/something might be more appropriate to get true unit test coverage going

* Somehow suppress the deprecation/raw type warnings that result from bindgen traversing into old APIs (e.g. the servlet API)

