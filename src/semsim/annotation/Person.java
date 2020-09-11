package semsim.annotation;

import java.net.URI;

import semsim.SemSimObject;
import semsim.definitions.SemSimTypes;

/**
 * Class for working with identifying data on humans.
 * @author mneal
 *
 */
public class Person extends SemSimObject {

	private String name = ""; // e.g. "John Smith"
	private String email = "";
	private URI accountName = null; // ORCIDs would go here
	private URI accountServiceHomepage = null;
	
	// Constructor to use if entering values for all identifier fields
	public Person(String name, String email, URI accountName, URI accountServiceHomepage) {
		super(SemSimTypes.PERSON);
		this.name = name;
		this.email = email;
		this.accountName = accountName;
		this.accountServiceHomepage = accountServiceHomepage;
	}
	
	// Constructor to use if only an ORCID is used to identify a person
	public Person(URI accountName) {
		super(SemSimTypes.PERSON);
		this.accountName = accountName;
	}
	
	// Constructor to use if only using name and email identifiers
	public Person(String name, String email) {
		super(SemSimTypes.PERSON);
		this.name = name;
		this.email = email;
	}
	
	// Constructor to use if only using name identifier
	public Person(String name) {
		super(SemSimTypes.PERSON);
		this.name = name;
	}
	
	// Constructor to initialize empty person
	public Person() {
		super(SemSimTypes.PERSON);
	}
	
	
	/** @return The person's name */
	@Override
	public String getName() {
		return name;
	}
	
	
	/**
	 * Set the person's name
	 * @param name The person's name
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	
	/**
	 * @return Whether the name has been set
	 */
	public boolean hasName() {
		return (getName() != null && ! getName().equals(""));
	}
	
	
	/** @return The person's email address	*/
	public String getEmail() {
		return email;
	}
	
	
	/**
	 * Set the person's email address
	 * @param email The email address
	 */
	public void setEmail(String email) {
		this.email = email;
	}
	
	
	/**
	 * @return Whether the email address has been set
	 */
	public boolean hasEmail() {
		return (getEmail() != null && ! getEmail().equals(""));
	}
	
	
	/** @return The account name identifier for the person */
	public URI getAccountName() {
		return accountName;
	}
	
	
	/**
	 * Set the person's account name
	 * @param accountName The account name
	 */
	public void setAccountName(URI accountName) {
		this.accountName = accountName;
	}
	
	
	/**
	 * @return Whether the account name has been set for this Person
	 */
	public boolean hasAccountName() {
		if(getAccountName()==null) return false;
		else return ! getAccountName().toString().trim().equals("");
	}
	
	
	/** @return The service homepage for the person's account name */
	public URI getAccountServiceHomepage() {
		return accountServiceHomepage;
	}
	
	
	/**
	 * Set the account service homepage
	 * @param accountServiceHomepage The account service homepage
	 */
	public void setAccountServiceHomepage(URI accountServiceHomepage) {
		this.accountServiceHomepage = accountServiceHomepage;
	}
	
	
	/**
	 * @return Whether the account service homepage has been set for this Person
	 */
	public boolean hasAccountServicesHomepage() {
		if(getAccountServiceHomepage()==null) return false;
		else return ! getAccountServiceHomepage().toString().trim().equals("");
	}
	
	
	/**
	 * Overrides SemSimObject method and returns an empty string
	 */
	@Override
	public String getDescription() {
		return "";
	}
	
	
	/**
	 * @return True if only account name for the Person has been set.
	 */
	public boolean onlyAccountNameIsSet() {
		return (hasAccountName() && ! hasName() && ! hasEmail() && ! hasAccountServicesHomepage());
	}
}
