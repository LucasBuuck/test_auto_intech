package com.intech.comptabilite.service.entityservice;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.intech.comptabilite.model.JournalComptable;

public class JournalComptableServiceTest {
	
	private JournalComptableService journalComptableService = new JournalComptableService();

	@Test
	 public void getByCode() {
		List<JournalComptable> pList = new ArrayList<>();
		pList.add(new JournalComptable("j1", "one"));
		pList.add(new JournalComptable("j2", "two"));
		pList.add(new JournalComptable("j3", "three"));
		pList.add(new JournalComptable("j4", "four"));		
		
		String foundJournalCode = "j3";
		String notFoundJournalCode = "j5";
		
		JournalComptable foundJournal = journalComptableService.getByCode(pList, foundJournalCode);
		JournalComptable notfoundJournal = journalComptableService.getByCode(pList, notFoundJournalCode);
		
        Assertions.assertEquals("three", foundJournal.getLibelle());
        Assertions.assertNull(notfoundJournal);
	    }
}
