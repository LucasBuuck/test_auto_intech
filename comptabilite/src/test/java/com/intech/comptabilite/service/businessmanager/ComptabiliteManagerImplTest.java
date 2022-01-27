package com.intech.comptabilite.service.businessmanager;

import java.math.BigDecimal;
import java.util.Date;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.intech.comptabilite.model.CompteComptable;
import com.intech.comptabilite.model.EcritureComptable;
import com.intech.comptabilite.model.JournalComptable;
import com.intech.comptabilite.model.LigneEcritureComptable;
import com.intech.comptabilite.service.exceptions.FunctionalException;

@SpringBootTest
public class ComptabiliteManagerImplTest {

	@Autowired
    private ComptabiliteManagerImpl manager;

    @Test
    public void checkEcritureComptableUnit() throws Exception {
        EcritureComptable vEcritureComptable;
        vEcritureComptable = new EcritureComptable();
        vEcritureComptable.setJournal(new JournalComptable("AC", "Achat"));
        vEcritureComptable.setDate(new Date());
        vEcritureComptable.setLibelle("Libelle");
        vEcritureComptable.getListLigneEcriture().add(new LigneEcritureComptable(new CompteComptable(1),
                                                                                 null, new BigDecimal(123),
                                                                                 null));
        vEcritureComptable.getListLigneEcriture().add(new LigneEcritureComptable(new CompteComptable(2),
                                                                                 null, null,
                                                                                 new BigDecimal(123)));
        manager.checkEcritureComptableUnit(vEcritureComptable);
    }

    @Test
    public void checkEcritureComptableUnitViolation() throws Exception {
        EcritureComptable vEcritureComptable;
        vEcritureComptable = new EcritureComptable();
        Assertions.assertThrows(FunctionalException.class,
        		() -> {
        		manager.checkEcritureComptableUnit(vEcritureComptable);}
        );        
    }

    @Test
    public void checkEcritureComptableUnitRG2() throws Exception {
        EcritureComptable vEcritureComptable;
        vEcritureComptable = new EcritureComptable();
        vEcritureComptable.setJournal(new JournalComptable("AC", "Achat"));
        vEcritureComptable.setDate(new Date());
        vEcritureComptable.setLibelle("Libelle");
        vEcritureComptable.getListLigneEcriture().add(new LigneEcritureComptable(new CompteComptable(1),
                                                                                 null, new BigDecimal(123),
                                                                                 null));
        vEcritureComptable.getListLigneEcriture().add(new LigneEcritureComptable(new CompteComptable(2),
                                                                                 null, null,
                                                                                 new BigDecimal(1234)));
       
        Assertions.assertThrows(FunctionalException.class,
        		() -> {
        		manager.checkEcritureComptableUnit(vEcritureComptable);}
        );
    }

    @Test
    public void checkEcritureComptableUnitRG3() throws Exception {
        EcritureComptable vEcritureComptable;
        vEcritureComptable = new EcritureComptable();
        vEcritureComptable.setJournal(new JournalComptable("AC", "Achat"));
        vEcritureComptable.setDate(new Date());
        vEcritureComptable.setLibelle("Libelle");
        vEcritureComptable.getListLigneEcriture().add(new LigneEcritureComptable(new CompteComptable(1),
                                                                                 null, new BigDecimal(123),
                                                                                 null));
        vEcritureComptable.getListLigneEcriture().add(new LigneEcritureComptable(new CompteComptable(1),
                                                                                 null, new BigDecimal(123),
                                                                                 null));
        
        Assertions.assertThrows(FunctionalException.class,
        		() -> {
        			manager.checkEcritureComptableUnit(vEcritureComptable);
        		}
        );
                
    }
   
    @Test
    public void checkRgCompta5 () throws Exception {
        EcritureComptable vEcritureComptable;
        vEcritureComptable = new EcritureComptable();
        vEcritureComptable.setJournal(new JournalComptable("AC", "Achat"));
        vEcritureComptable.setDate(new Date());
        vEcritureComptable.setLibelle("Libelle");

        checkRgCompta5DateDiff(vEcritureComptable);
        checkRgCompta5CodeDiff(vEcritureComptable);
    }
    
    private void checkRgCompta5DateDiff (EcritureComptable vEcritureComptable) throws Exception {
        vEcritureComptable.setReference("AC-2021/00001");
     //Throw happens because 2021 != 2022 (new Date()) 
        Assertions.assertThrows(FunctionalException.class,
        		() -> {
        			manager.checkRgCompta5(vEcritureComptable);
        		}
        );
    }
    
    private void checkRgCompta5CodeDiff (EcritureComptable vEcritureComptable) throws Exception {
        vEcritureComptable.setReference("BQ-2022/00001");
     //Throw happens because "BQ" != "AC"
        Assertions.assertThrows(FunctionalException.class,
        		() -> {
        			manager.checkRgCompta5(vEcritureComptable);
        		}
        );
    }
    
    
    @Test
    public void addReference () throws Exception {
    	  EcritureComptable vEcritureComptable;
          vEcritureComptable = new EcritureComptable();
          vEcritureComptable.setJournal(new JournalComptable("AC", "Achat"));
          vEcritureComptable.setDate(new Date());
          vEcritureComptable.setLibelle("Libelle");

        manager.addReference(vEcritureComptable);
        Assertions.assertNotNull(vEcritureComptable.getReference());
        Assertions.assertEquals("AC-"+ (vEcritureComptable.getDate().getYear() + 1900) +"/00001", vEcritureComptable.getReference());
        manager.addReference(vEcritureComptable);
        Assertions.assertEquals("AC-"+ (vEcritureComptable.getDate().getYear() + 1900) +"/00002", vEcritureComptable.getReference());

    }


}
