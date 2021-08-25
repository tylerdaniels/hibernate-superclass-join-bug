package com.tylerdaniels.hibernatejoinbug.domain;

import java.util.Set;

import javax.persistence.Embeddable;
import javax.persistence.OneToMany;

@Embeddable
public class CustomerDetails
{
	@OneToMany(mappedBy = "customer")
	private Set<Address> addresses;

	public CustomerDetails()
	{
	}

}
