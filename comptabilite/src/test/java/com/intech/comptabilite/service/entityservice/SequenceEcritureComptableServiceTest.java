package com.intech.comptabilite.service.entityservice;

import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.intech.comptabilite.model.SequenceEcritureComptable;
import com.intech.comptabilite.model.SequenceId;
import com.intech.comptabilite.repositories.SequenceEcritureComptableRepository;
import com.intech.comptabilite.service.exceptions.NotFoundException;

@SpringBootTest
public class SequenceEcritureComptableServiceTest {
	
	private SequenceEcritureComptableService sequenceEcritureComptableService = new SequenceEcritureComptableService();
	
	@MockBean
	private SequenceEcritureComptableRepository repository;
	
	@Test
	public void getDernierValeurByCodeAndAnnee() throws NotFoundException {
		String journalCode = "AC";
		Integer annee = 2022;
		Mockito.when(repository.findById(new SequenceId(journalCode, annee))).thenReturn(Optional.of(new SequenceEcritureComptable("AC", 2022, 1)));
		Assertions.assertEquals(1, sequenceEcritureComptableService.getDernierValeurByCodeAndAnnee(journalCode, annee));
	}
	@Test
	public void getDernierValeurByCodeAndAnneeEmpty() throws NotFoundException {
		String journalCode = "AX";
		Integer annee = 2022;
		Mockito.when(repository.findById(new SequenceId(journalCode, annee))).thenReturn(Optional.empty());
		   Assertions.assertThrows(java.lang.NullPointerException.class,
	        		() -> {
	        			sequenceEcritureComptableService.getDernierValeurByCodeAndAnnee(journalCode, annee);
	        		}
	        );
	}

}
