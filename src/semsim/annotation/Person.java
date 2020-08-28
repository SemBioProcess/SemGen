package semsim.annotation;

import java.net.URI;

/**
 * Class for working with identifying data on individual humans.
 * @author mneal
 *
 */
public class Person {

	private String name = "";
	private String email = "";
	private URI accountName = null; // ORCIDs would go here
	private URI accountServiceHomepage = null;
	
	// Constructor to use if entering values for all identifier fields
	public Person(String name, String email, URI accountName, URI accountServiceHomepage) {
		this.name = name;
		this.email = email;
		this.accountName = accountName;
		this.accountServiceHomepage = accountServiceHomepage;
	}
	
	// Constructor to use if only an ORCID is used to identify a person
	public Person(URI accountName) {
		this.accountName = accountName;
	}
	
	// Constructor to use if only using name and email identifiers
	public Person(String name, String email) {
		this.name = name;
		this.email = email;
	}
	
	// Constructor to use if only using name identifier
	public Person(String name) {
		this.name = name;
	}
	
	
	/** @return The person's name */
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
}
