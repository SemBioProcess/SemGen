package unitTests.semgen;
import java.io.IOException;

import org.junit.BeforeClass;

import semgen.SemGen;

/**
 * Base class for all UTs. Makes sure SemGen is setup properly before any tests are run.
 * @author Ryan
 *
 */
public class UnitTestBase {
	private static boolean _isSetup;
	
	/**
	 * Ensures SemGen is setup properly before any tests are run.
	 * 
	 * Note: Ideally we would use mocks so we wouldnt need to setup anything,
	 * but given that we're writing these tests after the fact and we need to write
	 * them quickly we're going down the path of least resistance
	 * and wouldnt need this
	 * @throws IOException
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 */
	@BeforeClass
	public static void initialize() throws IOException, NoSuchMethodException, SecurityException {
		if(_isSetup)
			return;
		
		SemGen.setup(new String[]{});
		_isSetup = true;
	}
}
