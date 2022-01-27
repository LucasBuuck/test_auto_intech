package com.intech.comptabilite.service.entityservice;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.intech.comptabilite.model.CompteComptable;

public class CompteComptableServiceTest {
		
	private CompteComptableService compteComptableService = new CompteComptableService();

	@Test
	 public void getByNumero() {

		List<CompteComptable> pList = new ArrayList<>();
		pList.add(new CompteComptable(1, "one"));
		pList.add(new CompteComptable(2, "two"));
		pList.add(new CompteComptable(3, "three"));
		pList.add(new CompteComptable(4, "four"));		
		
		Integer foundAccountNumber = 3;
		Integer notFoundAccountNumber = 5;
		
		CompteComptable foundAccount = compteComptableService.getByNumero(pList, foundAccountNumber);
		CompteComptable notFoundAccount = compteComptableService.getByNumero(pList, notFoundAccountNumber);
		
        Assertions.assertEquals("three", foundAccount.getLibelle());
        Assertions.assertNull(notFoundAccount);
	    }
}
