import junit.framework.Assert;
import junit.framework.TestCase;

public class ClassInDefaultPackageTest extends TestCase {
	public void testClass() {
		ClassInDefaultPackage c = new ClassInDefaultPackage();
		ClassInDefaultPackageBinding b = new ClassInDefaultPackageBinding(c);
		b.name().set("c");
		Assert.assertEquals("c", c.name);
	}

}
