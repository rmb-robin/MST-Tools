import java.util.Set;

import org.junit.Test;

import com.mst.dao.RedisManagerImpl;
import static  org.junit.Assert.*;

public class RedisConnectivityTest {

	@Test
	public void testRedis(){
		RedisManagerImpl impl = new RedisManagerImpl();
		impl.addToSet("Test", "Value");
		impl.addToSet("Test", "Value1");
		impl.addToSet("Test", "Value2");
		impl.addToSet("Test", "Value2");
		
		
		Set<String> vals = impl.getSet("Test");
		assertEquals(3, vals.size());
	}

	
}
