package UnitTests;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class NetworkTests {
	private KAT.KATClient client;

	@Before
	public void setUp() throws Exception {
		this.client = new KAT.KATClient("localhost", 8888);
		client.connect();
		try { Thread.sleep(2000); } catch( Exception e ){ }
	}

	@After
	public void tearDown() throws Exception {
		client.disconnect();
	}

	@Test
	public void test() {
		client.sendLogin("TestUser", "RED", 4);
	}
}
