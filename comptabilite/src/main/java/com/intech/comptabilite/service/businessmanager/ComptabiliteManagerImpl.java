package com.intech.comptabilite.service.businessmanager;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import javax.validation.Configuration;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.intech.comptabilite.model.CompteComptable;
import com.intech.comptabilite.model.EcritureComptable;
import com.intech.comptabilite.model.JournalComptable;
import com.intech.comptabilite.model.LigneEcritureComptable;
import com.intech.comptabilite.model.SequenceEcritureComptable;
import com.intech.comptabilite.service.entityservice.CompteComptableService;
import com.intech.comptabilite.service.entityservice.EcritureComptableService;
import com.intech.comptabilite.service.entityservice.JournalComptableService;
import com.intech.comptabilite.service.entityservice.SequenceEcritureComptableService;
import com.intech.comptabilite.service.exceptions.FunctionalException;
import com.intech.comptabilite.service.exceptions.NotFoundException;

@Service
public class ComptabiliteManagerImpl implements ComptabiliteManager {

	@Autowired
	private EcritureComptableService ecritureComptableService;
	@Autowired
	private JournalComptableService journalComptableService;
	@Autowired
	private CompteComptableService compteComptableService;
	@Autowired
	private SequenceEcritureComptableService sequenceEcritureComptableService;

	/**
     * {@inheritDoc}
     */
    @Override
    public List<CompteComptable> getListCompteComptable() {
        return compteComptableService.getListCompteComptable();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<JournalComptable> getListJournalComptable() {
       return journalComptableService.getListJournalComptable();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<EcritureComptable> getListEcritureComptable() {
    	return ecritureComptableService.getListEcritureComptable();
    }

    /**
     * {@inheritDoc}
     */
    // TODO ?? impl??menter et ?? tester
    @Override
    public synchronized void addReference(EcritureComptable pEcritureComptable) {
	    
        Integer year = pEcritureComptable.getDate().getYear() + 1900;

		try {
			int num = sequenceEcritureComptableService.getDernierValeurByCodeAndAnnee(pEcritureComptable.getJournal().getCode(), year);
			
			SequenceEcritureComptable seq = new SequenceEcritureComptable(pEcritureComptable.getJournal().getCode(), year, num+1) ;
			String s = pEcritureComptable.getJournal().getCode() + "-" + year + "/";
			int nbDigits = (int) (Math.log10(num) + 1);
			for(int i = 0; i < 5-nbDigits; i++) {
				s+= "0"; 
			}
			s+= String.valueOf(num+1);
			pEcritureComptable.setReference(s);
			sequenceEcritureComptableService.upsert(seq);
		} catch (NotFoundException e) {
			SequenceEcritureComptable seq = new SequenceEcritureComptable(pEcritureComptable.getJournal().getCode(), year, 1) ;
			pEcritureComptable.setReference(pEcritureComptable.getJournal().getCode() + "-" + year + "/" + "00001");
			sequenceEcritureComptableService.upsert(seq);
		}
    }

    /**
     * {@inheritDoc}
     */
    // TODO ?? tester
    @Override
    public void checkEcritureComptable(EcritureComptable pEcritureComptable) throws FunctionalException {
        this.checkEcritureComptableUnit(pEcritureComptable);
        this.checkEcritureComptableContext(pEcritureComptable);
    }


    /**
     * {@inheritDoc}
     */
    // TODO tests ?? compl??ter
    public void checkEcritureComptableUnit(EcritureComptable pEcritureComptable) throws FunctionalException {
    	if(pEcritureComptable == null) {
    		return;
    	}
    	//System.out.println(pEcritureComptable);

        // ===== V??rification des contraintes unitaires sur les attributs de l'??criture
        Set<ConstraintViolation<EcritureComptable>> vViolations = getConstraintValidator().validate(pEcritureComptable);
        if (!vViolations.isEmpty()) {
            throw new FunctionalException("L'??criture comptable ne respecte pas les r??gles de gestion.",
                                          new ConstraintViolationException(
                                              "L'??criture comptable ne respecte pas les contraintes de validation",
                                              vViolations));
        }

        // ===== RG_Compta_2 : Pour qu'une ??criture comptable soit valide, elle doit ??tre ??quilibr??e
        if (!ecritureComptableService.isEquilibree(pEcritureComptable)) {
            throw new FunctionalException("L'??criture comptable n'est pas ??quilibr??e.");
        }

        // ===== RG_Compta_3 : une ??criture comptable doit avoir au moins 2 lignes d'??criture (1 au d??bit, 1 au cr??dit)
        int vNbrCredit = 0;
        int vNbrDebit = 0;
        for (LigneEcritureComptable vLigneEcritureComptable : pEcritureComptable.getListLigneEcriture()) {
            if (BigDecimal.ZERO.compareTo(ObjectUtils.defaultIfNull(vLigneEcritureComptable.getCredit(),
                                                                    BigDecimal.ZERO)) != 0) {
                vNbrCredit++;
            }
            if (BigDecimal.ZERO.compareTo(ObjectUtils.defaultIfNull(vLigneEcritureComptable.getDebit(),
                                                                    BigDecimal.ZERO)) != 0) {
                vNbrDebit++;
            }
        }
        // On test le nombre de lignes car si l'??criture ?? une seule ligne
        //      avec un montant au d??bit et un montant au cr??dit ce n'est pas valable
        if (pEcritureComptable.getListLigneEcriture().size() < 2
            || vNbrCredit < 1
            || vNbrDebit < 1) {
            throw new FunctionalException(
                "L'??criture comptable doit avoir au moins deux lignes : une ligne au d??bit et une ligne au cr??dit.");
        }

        checkRgCompta5(pEcritureComptable);
    }

    public void checkRgCompta5 (EcritureComptable pEcritureComptable) throws FunctionalException {
    	if(pEcritureComptable.getReference() == null) return;
        String[] journalReference = pEcritureComptable.getReference().split("\\p{Punct}");
	    String code = journalReference[0];	       
	    String year = journalReference[1];

        if(!pEcritureComptable.getJournal().getCode().equals(code)) {
            throw new FunctionalException("Le code journal de r??f??rence diff??rent du code journal.");
        }

        if((pEcritureComptable.getDate().getYear() + 1900) != Integer.parseInt(year)) {
            throw new FunctionalException("Ann??e de r??f??rence diff??rent de l'ann??e d'??criture");
        }
    }

    /**
     * V??rifie que l'Ecriture comptable respecte les r??gles de gestion li??es au contexte
     * (unicit?? de la r??f??rence, ann??e comptable non clotur??...)
     *
     * @param pEcritureComptable -
     * @throws FunctionalException Si l'Ecriture comptable ne respecte pas les r??gles de gestion
     */
    protected void checkEcritureComptableContext(EcritureComptable pEcritureComptable) throws FunctionalException {
        // ===== RG_Compta_6 : La r??f??rence d'une ??criture comptable doit ??tre unique
        if (StringUtils.isNoneEmpty(pEcritureComptable.getReference())) {
            try {
                // Recherche d'une ??criture ayant la m??me r??f??rence
                EcritureComptable vECRef = ecritureComptableService.getEcritureComptableByRef(pEcritureComptable.getReference());

                // Si l'??criture ?? v??rifier est une nouvelle ??criture (id == null),
                // ou si elle ne correspond pas ?? l'??criture trouv??e (id != idECRef),
                // c'est qu'il y a d??j?? une autre ??criture avec la m??me r??f??rence
                if (pEcritureComptable.getId() == null
                    || !pEcritureComptable.getId().equals(vECRef.getId())) {
                    throw new FunctionalException("Une autre ??criture comptable existe d??j?? avec la m??me r??f??rence.");
                }
            }  catch (NotFoundException vEx) {
                // Dans ce cas, c'est bon, ??a veut dire qu'on n'a aucune autre ??criture avec la m??me r??f??rence.
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void insertEcritureComptable(EcritureComptable pEcritureComptable) throws FunctionalException {
        this.checkEcritureComptable(pEcritureComptable);
        ecritureComptableService.insertEcritureComptable(pEcritureComptable);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateEcritureComptable(EcritureComptable pEcritureComptable) throws FunctionalException {
        ecritureComptableService.updateEcritureComptable(pEcritureComptable);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteEcritureComptable(Integer pId) {
       ecritureComptableService.deleteEcritureComptable(pId);
    }
    
    protected Validator getConstraintValidator() {
        Configuration<?> vConfiguration = Validation.byDefaultProvider().configure();
        ValidatorFactory vFactory = vConfiguration.buildValidatorFactory();
        Validator vValidator = vFactory.getValidator();
        return vValidator;
    }
}
