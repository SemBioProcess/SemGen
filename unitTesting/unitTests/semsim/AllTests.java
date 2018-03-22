package unitTests.semsim;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

//JUnit test suite for SemGen reading and writing tests. 
//Just run this class to run all read/write tests at one time.

@RunWith(Suite.class)
@SuiteClasses({ ReadingAndWritingCellMLFilesTests.class, ReadingAndWritingSBMLFilesTests.class, ReadingAndWritingOWLFilesTests.class, ReadingAndWritingModFilesTests.class })
public class AllTests {

}
