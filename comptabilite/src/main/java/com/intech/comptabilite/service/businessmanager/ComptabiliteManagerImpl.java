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
    // TODO à implémenter et à tester
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
    // TODO à tester
    @Override
    public void checkEcritureComptable(EcritureComptable pEcritureComptable) throws FunctionalException {
        this.checkEcritureComptableUnit(pEcritureComptable);
        this.checkEcritureComptableContext(pEcritureComptable);
    }


    /**
     * {@inheritDoc}
     */
    // TODO tests à compléter
    public void checkEcritureComptableUnit(EcritureComptable pEcritureComptable) throws FunctionalException {
    	if(pEcritureComptable == null) {
    		return;
    	}
    	//System.out.println(pEcritureComptable);

        // ===== Vérification des contraintes unitaires sur les attributs de l'écriture
        Set<ConstraintViolation<EcritureComptable>> vViolations = getConstraintValidator().validate(pEcritureComptable);
        if (!vViolations.isEmpty()) {
            throw new FunctionalException("L'écriture comptable ne respecte pas les règles de gestion.",
                                          new ConstraintViolationException(
                                              "L'écriture comptable ne respecte pas les contraintes de validation",
                                              vViolations));
        }

        // ===== RG_Compta_2 : Pour qu'une écriture comptable soit valide, elle doit être équilibrée
        if (!ecritureComptableService.isEquilibree(pEcritureComptable)) {
            throw new FunctionalException("L'écriture comptable n'est pas équilibrée.");
        }

        // ===== RG_Compta_3 : une écriture comptable doit avoir au moins 2 lignes d'écriture (1 au débit, 1 au crédit)
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
        // On test le nombre de lignes car si l'écriture à une seule ligne
        //      avec un montant au débit et un montant au crédit ce n'est pas valable
        if (pEcritureComptable.getListLigneEcriture().size() < 2
            || vNbrCredit < 1
            || vNbrDebit < 1) {
            throw new FunctionalException(
                "L'écriture comptable doit avoir au moins deux lignes : une ligne au débit et une ligne au crédit.");
        }

        checkRgCompta5(pEcritureComptable);
    }

    public void checkRgCompta5 (EcritureComptable pEcritureComptable) throws FunctionalException {
    	if(pEcritureComptable.getReference() == null) return;
        String[] journalReference = pEcritureComptable.getReference().split("\\p{Punct}");
	    String code = journalReference[0];	       
	    String year = journalReference[1];

        if(!pEcritureComptable.getJournal().getCode().equals(code)) {
            throw new FunctionalException("Le code journal de référence différent du code journal.");
        }

        if((pEcritureComptable.getDate().getYear() + 1900) != Integer.parseInt(year)) {
            throw new FunctionalException("Année de référence différent de l'année d'écriture");
        }
    }

    /**
     * Vérifie que l'Ecriture comptable respecte les règles de gestion liées au contexte
     * (unicité de la référence, année comptable non cloturé...)
     *
     * @param pEcritureComptable -
     * @throws FunctionalException Si l'Ecriture comptable ne respecte pas les règles de gestion
     */
    protected void checkEcritureComptableContext(EcritureComptable pEcritureComptable) throws FunctionalException {
        // ===== RG_Compta_6 : La référence d'une écriture comptable doit être unique
        if (StringUtils.isNoneEmpty(pEcritureComptable.getReference())) {
            try {
                // Recherche d'une écriture ayant la même référence
                EcritureComptable vECRef = ecritureComptableService.getEcritureComptableByRef(pEcritureComptable.getReference());

                // Si l'écriture à vérifier est une nouvelle écriture (id == null),
                // ou si elle ne correspond pas à l'écriture trouvée (id != idECRef),
                // c'est qu'il y a déjà une autre écriture avec la même référence
                if (pEcritureComptable.getId() == null
                    || !pEcritureComptable.getId().equals(vECRef.getId())) {
                    throw new FunctionalException("Une autre écriture comptable existe déjà avec la même référence.");
                }
            }  catch (NotFoundException vEx) {
                // Dans ce cas, c'est bon, ça veut dire qu'on n'a aucune autre écriture avec la même référence.
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
